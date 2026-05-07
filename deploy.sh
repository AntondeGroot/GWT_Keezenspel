#!/bin/bash
set -e

stamp_css_version_for_cloudflare_cache_invalidation() {
  local v=$(date +%s)
  local html_files=(
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
    sed -i.bak "s/\.css\"/.css?v=$v\"/g" "$f"
  done
}

stamp_css_version_for_cloudflare_cache_invalidation

echo "🔨 Building and running all tests..."
mvn clean verify

echo "📦 Uploading..."
scp -i ~/.ssh/pi_deploy_key GWT_Keezenspel-server/target/GWT_Keezenspel-exec.jar my-pi:/home/ubuntu/keezen.jar

echo "📁 Installing..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo mkdir -p /opt/keezen && sudo mv /home/ubuntu/keezen.jar /opt/keezen/keezen.jar"

echo "⚙️  Ensuring systemd service exists..."
ssh -i ~/.ssh/pi_deploy_key my-pi "
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

echo "⚙️  Ensuring application override config exists..."
ssh -i ~/.ssh/pi_deploy_key my-pi "
if [ ! -f /opt/keezen/application-override.yaml ]; then
  sudo tee /opt/keezen/application-override.yaml > /dev/null << 'EOF'
server:
  servlet:
    context-path: /keezen
EOF
fi"

echo "🔄 Restarting..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo systemctl restart keezen"

echo "✅ Done."