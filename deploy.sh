#!/bin/bash
set -e

echo "🔨 Building..."
mvn clean package

echo "📦 Uploading..."
scp -i ~/.ssh/pi_deploy_key GWT_Keezenspel-server/target/GWT_Keezenspel.jar my-pi:/home/ubuntu/game-server.jar

echo "📁 Installing..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo mkdir -p /opt/game-server && sudo mv /home/ubuntu/game-server.jar /opt/game-server/game-server.jar"

echo "🔄 Restarting..."
ssh -i ~/.ssh/pi_deploy_key my-pi "sudo systemctl restart game-server"

echo "✅ Done."