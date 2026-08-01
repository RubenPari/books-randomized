# Deploy su DigitalOcean Droplet con Docker Compose

## Contesto

Progetto "Books, Randomized": Angular 21 + Spring Boot 4.1 + PostgreSQL 16, stack Docker Compose (db, api, web/nginx). CI GitHub Actions esistente con test backend/frontend. Migrazioni DB gestite da Flyway (automatiche all'avvio dell'API).

**Obiettivo**: Deploy automatico su Droplet DigitalOcean via GitHub Actions su push a `main`. Accesso via IP su porta 80. HTTPS e dominio configurabili in seguito.

---

## Architettura di Deploy

```
GitHub push → GitHub Actions → Test CI → Build immagini → Push a GHCR → SSH sul Droplet → docker compose pull + up
```

- **Registry**: GitHub Container Registry (ghcr.io) — integrato con GitHub Actions
- **Deploy**: SSH con chiave dal runner GitHub al Droplet
- **Migrazioni DB**: Flyway le esegue automaticamente all'avvio del container `api`
- **Compose production**: File standalone `docker-compose.prod.yml` (non override)

---

## Task di Implementazione

### 1. Modificare `.github/workflows/ci.yml` — aggiungere `workflow_call`

Aggiungere `workflow_call` ai trigger per permettere il riuso dal workflow di deploy:

```yaml
on:
  push:
  pull_request:
  workflow_call:
```

### 2. Creare `docker-compose.prod.yml` (standalone)

File completo e indipendente per production — non è un override del compose base:

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: books
      POSTGRES_USER: ${DATABASE_USERNAME:-books}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 5s
      timeout: 5s
      retries: 10
    restart: unless-stopped

  api:
    image: ghcr.io/${GITHUB_REPOSITORY}/api:${IMAGE_TAG:-latest}
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://db:5432/books
      DATABASE_USERNAME: ${DATABASE_USERNAME:-books}
      DATABASE_PASSWORD: ${DATABASE_PASSWORD}
      AUTH_JWT_PRIVATE_KEY: ${AUTH_JWT_PRIVATE_KEY}
      AUTH_JWT_PUBLIC_KEY: ${AUTH_JWT_PUBLIC_KEY}
      AUTH_JWT_REQUIRE_KEYS: "true"
      AUTH_COOKIE_SECURE: "true"
      OPEN_LIBRARY_USER_AGENT: ${OPEN_LIBRARY_USER_AGENT:-BooksRandomized/1.0}
      PASSWORD_RESET_FROM: ${PASSWORD_RESET_FROM:-no-reply@books-randomized.invalid}
    healthcheck:
      test: ["CMD-SHELL", "curl -sf http://127.0.0.1:8080/actuator/health | grep -q UP"]
      interval: 10s
      timeout: 5s
      retries: 12
      start_period: 40s
    restart: unless-stopped

  web:
    image: ghcr.io/${GITHUB_REPOSITORY}/web:${IMAGE_TAG:-latest}
    depends_on:
      api:
        condition: service_healthy
    ports:
      - "80:80"
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://127.0.0.1/ >/dev/null"]
      interval: 10s
      timeout: 3s
      retries: 6
    restart: unless-stopped

volumes:
  db-data:
```

Differenze dal compose base:
- `image:` da GHCR al posto di `build:`
- `AUTH_COOKIE_SECURE: "true"`
- Porta `80:80` (standard HTTP)
- `restart: unless-stopped` su tutti i servizi
- `DATABASE_PASSWORD` senza default (obbligatoria in production)

### 3. Creare `.github/workflows/deploy.yml`

```yaml
name: deploy

on:
  push:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_TAG: ${{ github.sha }}

jobs:
  test:
    uses: ./.github/workflows/ci.yml

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4

      - uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - uses: docker/build-push-action@v6
        with:
          context: ./backend
          push: true
          tags: |
            ghcr.io/${{ github.repository }}/api:${{ env.IMAGE_TAG }}
            ghcr.io/${{ github.repository }}/api:latest

      - uses: docker/build-push-action@v6
        with:
          context: ./frontend
          push: true
          tags: |
            ghcr.io/${{ github.repository }}/web:${{ env.IMAGE_TAG }}
            ghcr.io/${{ github.repository }}/web:latest

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    steps:
      - name: Deploy via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.DO_HOST }}
          username: ${{ secrets.DO_USERNAME }}
          key: ${{ secrets.DO_SSH_KEY }}
          envs: IMAGE_TAG,GITHUB_REPOSITORY
          script: |
            cd /opt/books-randomized
            sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${IMAGE_TAG}/" .env
            docker compose -f docker-compose.prod.yml pull
            docker compose -f docker-compose.prod.yml up -d --remove-orphans
            docker image prune -f
```

### 4. Creare `scripts/setup-droplet.sh`

Script da eseguire una volta sul Droplet per prepararlo:

```bash
#!/usr/bin/env bash
set -euo pipefail

# Install Docker
curl -fsSL https://get.docker.com | sh

# Crea directory progetto
mkdir -p /opt/books-randomized

# Crea utente deploy dedicato
adduser --disabled-password --gecos "" deploy
usermod -aG docker deploy
mkdir -p /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
# Incollare qui la chiave pubblica SSH per il deploy

# Configura firewall
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

echo "Droplet configurato. Prossimi passi:"
echo "  1. Copiare docker-compose.prod.yml e .env in /opt/books-randomized/"
echo "  2. Configurare GitHub Secrets (DO_HOST, DO_USERNAME, DO_SSH_KEY)"
echo "  3. Push su main per triggerare il deploy"
```

### 5. Creare `.env.production` (template per il Droplet)

Template da copiare sul Droplet come `/opt/books-randomized/.env`:

```bash
# === Database ===
DATABASE_USERNAME=books
DATABASE_PASSWORD=<generare-password-sicura>

# === JWT (generare con openssl, incollare contenuto PEM) ===
AUTH_JWT_PRIVATE_KEY=<contenuto di private.pem>
AUTH_JWT_PUBLIC_KEY=<contenuto di public.pem>

# === App ===
OPEN_LIBRARY_USER_AGENT=BooksRandomized/1.0
PASSWORD_RESET_FROM=no-reply@books-randomized.invalid

# === Deploy (aggiornato automaticamente da GitHub Actions) ===
IMAGE_TAG=latest
GITHUB_REPOSITORY=<owner>/books-randomized
```

### 6. Aggiornare `.gitignore`

Aggiungere:
```
.env.production
```

---

## Configurazione GitHub Secrets

Impostare in **Settings → Secrets and variables → Actions**:

| Secret | Valore |
|--------|--------|
| `DO_HOST` | IP del Droplet |
| `DO_USERNAME` | `deploy` |
| `DO_SSH_KEY` | Chiave privata SSH (intero contenuto) |

---

## Setup Manuale (una tantum)

1. **Creare Droplet**: Ubuntu 24.04, minimo 2GB RAM
2. **SSH nel Droplet** ed eseguire `scripts/setup-droplet.sh`
3. **Copiare sul Droplet** in `/opt/books-randomized/`:
   - `docker-compose.prod.yml`
   - `.env` (dal template `.env.production`, con valori reali)
4. **Generare JWT keys** sul Droplet e incollarle nel `.env`
5. **Impostare GitHub Secrets** (tabella sopra)
6. **Push su `main`** → deploy automatico

---

## Aggiunta Futura di Dominio + HTTPS

1. Installare Nginx sul Droplet come reverse proxy (fuori da Docker)
2. Ottenere certificato Let's Encrypt: `certbot --nginx -d yourdomain.com`
3. Configurare Nginx per proxare `80` → container `web:80`
4. Cambiare porta in `docker-compose.prod.yml` da `80:80` a `8080:80` (interno)
5. `AUTH_COOKIE_SECURE` è già `true` nel compose production

---

## Validazione

- [ ] `docker compose config` passa localmente (compose base invariato)
- [ ] `docker compose -f docker-compose.prod.yml config` passa con `.env` compilato
- [ ] CI esistente passa (test backend + frontend)
- [ ] Workflow deploy builda e pusha immagini su GHCR
- [ ] SSH dal runner al Droplet funziona
- [ ] `curl http://<DROPLET_IP>/actuator/health` ritorna `UP`
- [ ] Frontend caricabile su `http://<DROPLET_IP>`

---

## File da Creare/Modificare

| File | Azione |
|------|--------|
| `docker-compose.prod.yml` | Creare |
| `.github/workflows/deploy.yml` | Creare |
| `.github/workflows/ci.yml` | Modificare (aggiungere `workflow_call`) |
| `scripts/setup-droplet.sh` | Creare |
| `.env.production` | Creare (template) |
| `.gitignore` | Aggiornare (aggiungere `.env.production`) |
