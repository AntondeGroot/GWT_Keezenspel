#!/bin/bash
set -e

SSH="ssh -i ~/.ssh/pi_deploy_key my-pi"

echo "📁 Creating directory..."
$SSH "sudo mkdir -p /opt/keezen"

echo "⚙️  Installing systemd service..."
$SSH "sudo tee /etc/systemd/system/keezen.service > /dev/null << 'EOF'
[Unit]
Description=Keezen game server
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/keezen/keezen.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF"
$SSH "sudo systemctl daemon-reload && sudo systemctl enable keezen"

echo "📝 Creating application override config..."
$SSH "sudo tee /opt/keezen/application-override.yaml > /dev/null << 'EOF'
# no context-path: Keezenspel serves at root, nginx handles routing
EOF"

echo ""
echo "✅ Pi setup complete."
echo "   Run ./deploy.sh to deploy the application."