# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack app for discovering random books, saving favorites to a personal vault, and tracking discovery history. Uses bookdatabase.io API for book data, Google Translate for translations, and Mailtrap for emails.

## Architecture

- **Frontend** (`frontend/`): Ember.js with TypeScript, Embroider + Vite build, Octane patterns with GTS/GJS authoring
- **Backend** (`backend/`): Spring Boot 4 (Java 25, Gradle), REST API on port 8080
- **Database**: PostgreSQL 16 (docker-compose service `db`, database `books_randomized`, user/pass: `books/books`)
- **API spec**: `docs-book-api.yaml` — OpenAPI spec for the external bookdatabase.io API

Backend layers: `controller` → `service` → `repository` (Spring Data JPA) + `client` (external API calls via `BookDatabaseClient`). Auth uses JWT (jjwt library) with `JwtAuthFilter`/`JwtService`. DTOs in `dto/`, entities in `model/`, security config in `config/SecurityConfig`.

## Common Commands

### Infrastructure
```bash
docker compose up -d db          # Start PostgreSQL only
docker compose up -d             # Start all services (db, backend, frontend)
```

### Frontend (from `frontend/`)
```bash
npm install
npm run start                    # Vite dev server at http://localhost:4200
npm run build                    # Production build
npm run test                     # Build + run all tests
npm run test -- --filter "name"  # Run single test by name
npm run test -- --module "name"  # Run single test module
npm run lint                     # All lint checks (js, hbs, css, types, format)
npm run lint:fix                 # Auto-fix lint + format
npm run lint:types               # TypeScript type check (ember-tsc)
```

For rapid test iteration: build once with `vite build --mode development`, then rerun `ember test --path dist --filter "..."`.

### Backend (from `backend/`)
```bash
./gradlew build                  # Build + test
./gradlew bootRun                # Run Spring Boot app (port 8080)
./gradlew test                   # Run tests only
```

## Frontend Conventions

- **TypeScript** by default; GTS/GJS for components and routes (strict authoring)
- **Prettier**: single quotes in JS/TS, double quotes in HTML/JSON/HBS, trailing commas (es5)
- **Indent**: 2 spaces, LF line endings
- **Imports**: ESM only. Separate vendor, app-local, and `import type` groups with blank lines
- **Components**: PascalCase class, kebab-case filenames. Templates: angle bracket syntax, named arguments
- **CSS**: stylelint-config-standard, component-scoped
- **Linting**: ESLint with Ember/Warp Drive/QUnit plugins; type-aware linting enabled

## Backend Conventions

- Package: `dev.rubenpari.backend`
- Config via environment variables with defaults in `application.properties`
- JPA with `ddl-auto=update` (schema auto-managed)
- External API client POJOs in `client/` package (`ExternalBook`, `ExternalEdition`, `ExternalLanguage`)

## Environment Variables

Required for full functionality (all have defaults for local dev):
`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET`, `BOOKDATABASE_BASE_URL`, `BOOKDATABASE_API_KEY`, `MAILTRAP_API_TOKEN`, `TRANSLATE_API_BASE_URL`, `TRANSLATE_API_KEY`
