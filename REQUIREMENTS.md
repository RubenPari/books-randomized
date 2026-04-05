# Books Randomized - Documento dei Requisiti

## 1. Panoramica del Progetto

**Books Randomized** è un'applicazione web full-stack che permette agli utenti di scoprire libri casuali, salvare i preferiti in un vault personale e tracciare la cronologia delle scoperte. L'applicazione utilizza **ISBNdb API v2** (api2.isbndb.com) per ottenere dati sui libri e implementa un sistema di randomizzazione intelligente con filtri avanzati e prevenzione dei duplicati tra sessioni.

### Funzionalità Principali

- **Generazione casuale di libri** con filtri avanzati (categoria, lingua, rating minimo, intervallo anni)
- **Cronologia delle scoperte** (sessione corrente + storico persistente) per evitare duplicati
- **Vault personale** per salvare libri con note e valutazioni personali
- **Supporto multilingua** (Italiano, Inglese)
- **Anteprima libri** tramite cover image e sommario
- **Statistiche** (totale libri, rating medio, distribuzione per categoria/autore/lingua)
- **Autenticazione completa** con registrazione, conferma email, reset password e JWT
- **PWA** (Progressive Web App) con supporto offline
- **Import/Export** dei libri salvati in formato JSON

---

## 2. Tech Stack

### Frontend
emberjs

### Backend
spring boot

### Database

PostgreSQL

### Infrastruttura

- **Docker Compose** con 3 servizi: 
  - PostgreSQL (database)
  - Frontend (Ember.js)
  - Backend (Spring Boot)

### Servizi Esterni

| Servizio | Scopo |
|----------|-------|
| ISBNdb API v2 | Dati libri (titolo, autori, categorie/soggetti, cover, synopsis/excerpt; rating non disponibile dall'API) |
| Google Translate API | Traduzione descrizioni libri |
| Mailtrap | Email di conferma account e reset password |