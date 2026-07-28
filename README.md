# Books, Randomized

Random literary discovery from Open Library. Angular 21 SPA + Spring Boot 4.1 API, packaged as one Docker Compose stack behind Nginx.

## Prerequisites

- Docker / Docker Compose
- Optional native backend: JDK 25 + Maven Wrapper (`backend/mvnw`)
- Optional native frontend: Node 22

## Quick start (Compose)

```bash
cp .env.example .env
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

Paste the PEM file contents into `AUTH_JWT_PRIVATE_KEY` and `AUTH_JWT_PUBLIC_KEY` in `.env` (quoted multi-line values are fine). Do not commit `.env` or the PEM files.

```bash
docker compose up --build
```

Open http://localhost:8080

Health: http://localhost:8080/actuator/health

Stop with `docker compose down`.

## Native development

Backend (Postgres required on `localhost:5432`, database/user/password `books`):

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (proxies `/api` to `http://127.0.0.1:8080`):

```bash
cd frontend
npm ci
npm start
```

## Tests

```bash
cd backend && ./mvnw verify
cd frontend && npm test -- --run && npm run build
cd frontend && npm run e2e -- --workers=1
```

## Attribution

Book metadata and covers come from [Open Library](https://openlibrary.org/). Please respect their API usage guidelines and identify your client via `OPEN_LIBRARY_USER_AGENT`.
