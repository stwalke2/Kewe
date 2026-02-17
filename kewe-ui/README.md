# Kewe UI (Supplier Invoice MVP)

React + Vite frontend for the Supplier Invoice workflow.

## Prerequisites
- Node.js 20+
- npm 10+
- Backend API running at `http://localhost:8080`

## Run Backend
From repository root:

```bash
./gradlew bootRun
```

The backend endpoints used by this UI are:
- `GET /api/supplier-invoices`
- `GET /api/supplier-invoices/{id}`
- `POST /api/supplier-invoices`
- `PUT /api/supplier-invoices/{id}`
- `PUT /api/supplier-invoices/{id}/submit`
- `PUT /api/supplier-invoices/{id}/approve`
- `PUT /api/supplier-invoices/{id}/post`

## Run Frontend
From `/kewe-ui`:

```bash
npm install
npm run dev
```

Open:
- UI: `http://localhost:5173`
- Backend direct (optional): `http://localhost:8080`

## API Proxy / CORS
The UI always calls `/api/...` via Axios.
Vite proxy rewrites `/api` to the backend root so:
- `/api/supplier-invoices` -> `http://localhost:8080/api/supplier-invoices`

Configured in `vite.config.ts`.

## Quick Manual QA Checklist
- List page supports search + status filter + refresh + last loaded timestamp.
- Create Draft opens detail for the newly created invoice.
- Detail page allows Draft-only editing for header fields and lines.
- Save persists edits; Cancel reverts to last loaded state.
- Workflow buttons show only the next valid action (`Submit` -> `Approve` -> `Post`).
- Success/error banners are shown after save/transition operations.
