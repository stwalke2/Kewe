# Kewe

Education-focused ERP/CRM platform built from first principles.

## Current Scope
This repository currently contains:
- Spring Boot backend (`/src`) with Supplier Invoice MVP endpoints.
- React + Vite frontend (`/kewe-ui`) for Supplier Invoice workflow UI.

## Backend (Spring Boot)
### Run
```bash
./gradlew bootRun
```

Default URL: `http://localhost:8080`

## Frontend (Vite + React)
### Run
```bash
cd kewe-ui
npm install
npm run dev
```

Default URL: `http://localhost:5173`

The frontend uses a Vite dev proxy and calls `/api/...`, which is rewritten to the backend root (`http://localhost:8080`).

For complete frontend details, see `kewe-ui/README.md`.
