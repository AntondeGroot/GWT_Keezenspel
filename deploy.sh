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

stamp_css_version_for_cloudflare_cache_invalidation() {
  local v=$(date +%s)
  html_files=(
    GWT_Keezenspel-server/src/main/resources/public/index.html
    GWT_Keezenspel-server/src/main/resources/public/mobile.html
  )

  restore_html() {
    for f in "${html_files[@]}"; do
      [ -f "$f.bak" ] && mv "$f.bak" "$f"
    done
  }
  trap restore_html EXIT

  for f in "${html_files[@]}"; do
    sed -i.bak \
      -e "s/\.css\"/.css?v=$v\"/g" \
      -e "s/mobile-ux\.js\"/mobile-ux.js?v=$v\"/g" \
      "$f"
  done
}

stamp_css_version_for_cloudflare_cache_invalidation

echo "🔨 Building and running all tests..."
mvn clean verify

echo "🧬 Running mutation tests..."
mvn -pl GWT_Keezenspel-server pitest:mutationCoverage

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

echo "⚙️ Ensuring application override config exists..."
$SSH "
if [ ! -f /opt/keezen/application-override.yaml ]; then
  sudo tee /opt/keezen/application-override.yaml > /dev/null << 'EOF'
server:
  servlet:
    context-path: /keezen
EOF
fi"

echo "🔄 Restarting..."
$SSH "sudo systemctl restart keezen"

echo "✅ Done."