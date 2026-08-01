#!/usr/bin/env bash
set -euo pipefail

curl -fsSL https://get.docker.com | sh

mkdir -p /opt/books-randomized

adduser --disabled-password --gecos "" deploy
usermod -aG docker deploy
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh

ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "Droplet configurato. Prossimi passi:"
echo "  1. Copiare docker-compose.prod.yml e .env in /opt/books-randomized/"
echo "  2. Configurare GitHub Secrets (DO_HOST, DO_USERNAME, DO_SSH_KEY)"
echo "  3. Push su main per triggerare il deploy"
