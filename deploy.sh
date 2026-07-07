#!/bin/bash
set -e

if [ -n "$1" ]; then
  TARGET="$1"
elif ssh -o ConnectTimeout=3 -o BatchMode=yes -o StrictHostKeyChecking=accept-new -o ConnectionAttempts=1 my-pi true 2>/dev/null; then
  TARGET=my-pi
else
  echo "⚠️  my-pi unreachable, falling back to my-pi-ext (Cloudflare Tunnel)..."
  TARGET=my-pi-ext
fi
SSH="ssh -i ~/.ssh/pi_deploy_key $TARGET"
SCP="scp -i ~/.ssh/pi_deploy_key"

echo "🔨 Building and running all tests (backend + GWT gate)..."
mvn clean verify

echo "🧬 Running mutation tests..."
mvn -pl GWT_Keezenspel-server pitest:mutationCoverage

# The Angular app is now what we deploy (see env-angular). ng build hashes asset
# filenames, so the old GWT ?v= cache-stamping is no longer needed. Served under /keezen:
# base-href=/keezen/ makes the browser request /keezen/* (bundles + the API/SSE/asset URLs
# the app builds from <base href> via basePath()). nginx STRIPS the /keezen/ prefix
# (proxy_pass http://localhost:4200/ with a trailing slash), so the backend itself serves
# at the ROOT — no context-path (see override below).
echo "🅰️  Testing + building the Angular app..."
# Angular unit/component tests (vitest) run headless; a failure aborts the deploy (set -e).
# Then build. (Java/GWT tests already ran above via `mvn clean verify`.)
( cd frontend && npm ci && CI=1 npx ng test --watch=false && npm run build -- --base-href=/keezen/ )

echo "📦 Packaging the server to serve the Angular app (env-angular; no GWT unpack)..."
mvn -pl GWT_Keezenspel-server -am -Penv-angular -DskipTests clean package

# Angular E2E (Playwright) gate. Playwright serves the UI itself (ng serve on :4300) but
# needs a backend with the API + /test seeding hooks on :4200 — so run the jar we just
# built there, run the suite against it, then shut it down. A failure aborts the deploy
# (set -e) before anything is uploaded. (`mvn clean verify` above already needs :4200 free,
# so it is free here too.)
echo "🎭 Running Angular E2E (Playwright)..."
E2E_JAR=GWT_Keezenspel-server/target/GWT_Keezenspel-exec.jar
java -jar "$E2E_JAR" --server.port=4200 --spring.profiles.active=test > /tmp/keezen-e2e-backend.log 2>&1 &
E2E_BACKEND_PID=$!
trap '[ -n "${E2E_BACKEND_PID:-}" ] && kill "$E2E_BACKEND_PID" 2>/dev/null' EXIT
echo "   waiting for the E2E backend on :4200..."
for i in $(seq 1 60); do curl -sf http://localhost:4200/game-options >/dev/null 2>&1 && break; sleep 2; done
curl -sf http://localhost:4200/game-options >/dev/null 2>&1 || {
  echo "❌ E2E backend failed to start"; tail -n 80 /tmp/keezen-e2e-backend.log; exit 1; }
( cd frontend && npx playwright install chromium >/dev/null 2>&1 && npm run e2e )
kill "$E2E_BACKEND_PID" 2>/dev/null; E2E_BACKEND_PID=""; trap - EXIT

echo "📦 Uploading..."
$SCP GWT_Keezenspel-server/target/GWT_Keezenspel-exec.jar $TARGET:/home/ubuntu/keezen.jar

echo "📁 Installing..."
$SSH "sudo mkdir -p /opt/keezen && sudo mv /home/ubuntu/keezen.jar /opt/keezen/keezen.jar"

echo "⚙️ Ensuring systemd service exists..."
$SSH "
if [ ! -f /etc/systemd/system/keezen.service ]; then
  sudo tee /etc/systemd/system/keezen.service > /dev/null << 'EOF'
[Unit]
Description=Keezen game server
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/keezen/keezen.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable keezen
fi"

# nginx STRIPS the /keezen/ prefix (proxy_pass .../ with a trailing slash), so the backend
# must serve at the ROOT — a context-path here would double the prefix and 404 every request.
# The app is still built with base-href=/keezen/ so the browser requests /keezen/* (which
# nginx strips back to /* before it reaches here). Written every deploy so any stale
# context-path override is cleared. The backend keeps its default port 4200.
echo "⚙️ Ensuring the backend serves at root (nginx strips /keezen)..."
$SSH "sudo tee /opt/keezen/application-override.yaml > /dev/null << 'EOF'
# Root serving — nginx strips the /keezen prefix before proxying here, so no context-path.
EOF
sudo chown ubuntu:ubuntu /opt/keezen/application-override.yaml"

echo "🔄 Restarting..."
$SSH "sudo systemctl restart keezen"

echo "✅ Done."