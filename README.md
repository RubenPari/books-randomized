# Books Randomized

Discover random books, save favorites to your personal vault, and track your discovery history.

## Features

- **Random Book Discovery** with advanced filters (category, language, minimum rating, year range)
- **Discovery History** (current session + persistent) to avoid duplicates
- **Personal Vault** for saving books with notes and personal ratings
- **Statistics** (total books, average rating, distribution by category/author/language)
- **Multi-language Support** (Italian, English)
- **Book Preview** via cover images and summaries
- **Complete Authentication** with registration, email confirmation, password reset, and JWT
- **PWA Support** with offline capabilities
- **Import/Export** saved books in JSON format

## Tech Stack

- **Frontend**: Ember.js with TypeScript
- **Backend**: Spring Boot (Java)
- **Database**: PostgreSQL
- **External APIs**: ISBNdb (api2.isbndb.com), Google Translate

## Quick Start

### Prerequisites

- Node.js >= 20
- Docker & Docker Compose

### Development

```bash
# Start infrastructure
docker compose up -d postgres

# Start frontend
cd frontend
npm install
npm run start
```

Visit [http://localhost:4200](http://localhost:4200)

### Running Tests

```bash
cd frontend
npm run test
```

### Building for Production

```bash
cd frontend
npm run build
```

## Project Structure

```
books-randomized/
├── frontend/          # Ember.js application
├── docker-compose.yml
├── REQUIREMENTS.md    # Detailed requirements document
└── README.md
```

## License

MIT
