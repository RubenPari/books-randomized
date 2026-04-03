# AGENTS

This file guides coding agents working in this repository.
It summarizes how to build, test, lint, and follow project conventions.

## Project Overview
- This repo currently contains an Ember app under `frontend/`.
- Primary language: TypeScript with Ember Octane and GTS/GJS authoring.
- Node.js requirement: >= 20 (see `frontend/package.json`).
- No Cursor rules or Copilot instructions were found in this repo.

## Quick Start
- `cd frontend`
- `npm install`
- `npm run start` (Vite dev server, app at http://localhost:4200)

## Build Commands
- `cd frontend`
- Production build: `npm run build`
- Dev build: `npm exec vite build --mode development`

## Lint Commands
- `cd frontend`
- All lint checks: `npm run lint`
- Fix lint + format: `npm run lint:fix`
- JS/TS lint: `npm run lint:js`
- Template lint: `npm run lint:hbs`
- CSS lint: `npm run lint:css`
- Type check: `npm run lint:types`
- Format check: `npm run lint:format`
- Format write: `npm run format`

## Test Commands
- `cd frontend`
- All tests (builds then runs): `npm run test`
- Test UI: http://localhost:4200/tests (when dev server is running)

## Run a Single Test
Tests are executed via `ember test --path dist` after a Vite build.
Use QUnit filters/modules to target a single test or module.

- Run a single test by name:
  - `cd frontend`
  - `ember test --path dist --filter "my test name"`
  - Or through npm script: `npm run test -- --filter "my test name"`

- Run a single module:
  - `cd frontend`
  - `ember test --path dist --module "module name"`
  - Or through npm script: `npm run test -- --module "module name"`

Notes:
- `npm run test` performs a Vite build first; for rapid iteration, build once and rerun `ember test --path dist`.
- Test files live under `frontend/tests/`.

## Code Style and Conventions
### Language and Framework
- Use TypeScript by default; project is a TypeScript Ember app.
- Ember authoring format is strict: prefer GTS/GJS for components and routes.
- Use class-based Ember patterns and Octane style.

### Formatting (Prettier)
- Prettier is the source of truth for formatting.
- Single quotes in JS/TS; double quotes in HTML/JSON/HBS.
- Trailing commas for JS/TS and GJS/GTS (es5 style).
- For GJS/GTS templates: `templateSingleQuote = false`.
- Run `npm run format` or `npm run lint:fix` after code changes.

### EditorConfig
- Indent size: 2 spaces.
- Line endings: LF.
- Trim trailing whitespace.
- Final newline is required, except in `*.hbs`.

### Imports and Module Style
- Use ESM imports everywhere.
- Prefer explicit named imports; avoid default exports unless idiomatic.
- Keep import groups separated by blank lines:
  - Vendor/framework imports
  - App-local imports
  - Type-only imports (use `import type`)
- Prefer `import type` for types to keep runtime imports clean.

### TypeScript
- Type checking uses `ember-tsc` with `@ember/app-tsconfig`.
- Keep types narrow and local; avoid `any`.
- Use `InstanceType<typeof Foo>` for Ember factories when appropriate.
- Keep public interfaces in `frontend/types/` if shared.

### Templates (HBS/GTS/GJS)
- Follow `ember-template-lint` recommended rules.
- Prefer angle bracket components and named arguments.
- Keep templates simple; move logic to the backing class.

### CSS
- Stylelint extends `stylelint-config-standard`.
- Keep CSS minimal and component-scoped when possible.
- Prefer reusable class names over element selectors.

### Naming Conventions
- Components: `PascalCase` class names, `kebab-case` template filenames.
- Services and helpers: `camelCase` file names.
- Routes and controllers: follow Ember naming conventions.
- Tests: `*-test.ts` or `*-test.gts` under `tests/`.

### Error Handling
- Prefer throwing early with clear error messages for invalid state.
- Use Ember/QUnit assertions in tests (`assert.ok`, `assert.strictEqual`).
- For async code, return/await promises and let failures surface in tests.

### Linting Expectations
- ESLint configs include Ember, Warp Drive, QUnit, and Node plugins.
- Type-aware linting is enabled for TS files.
- Avoid disabling lint rules unless justified and scoped.

## File Layout
- App code: `frontend/app/`
- Tests: `frontend/tests/`
- Types: `frontend/types/`
- Config: `frontend/config/`
- Build config: `frontend/ember-cli-build.js`

## Ember CLI Notes
- This project uses Embroider + Vite.
- `frontend/.ember-cli` enforces strict authoring for components/routes.

## Working Agreements for Agents
- Keep changes focused and minimal.
- Update or add tests for new behavior when feasible.
- Run lint and relevant tests before finalizing.
- Do not add new tooling unless requested.

## No Additional Agent Rules
- No `.cursor/rules/`, `.cursorrules`, or Copilot instruction files exist.
- If these appear later, update this file to include their guidance.
