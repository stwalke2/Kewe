# Kewe

Education-focused ERP/CRM platform built from first principles.

## Current Scope
This repository currently contains:
- Spring Boot backend (`/src`) with Supplier Invoice APIs.
- React + Vite frontend (`/kewe-ui`) for Supplier Invoice list/detail workflow.

## Backend (Spring Boot)
### Prerequisites
- Java 21+

### Run
```bash
./gradlew bootRun
```

Backend URL: `http://localhost:8080`

## Frontend (Vite + React)
### Prerequisites
- Node.js 20+
- npm 10+

### Run
```bash
cd kewe-ui
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## URLs and Proxy
- UI: `http://localhost:5173`
- Backend direct API: `http://localhost:8080/supplier-invoices`
- UI API base: `/api/...`

The frontend uses the Vite dev proxy (`kewe-ui/vite.config.ts`) and calls `/api/...`, which is rewritten to `http://localhost:8080/...`.

## Supplier Invoice End-to-End Test Flow
1. Start backend and frontend with the commands above.
2. Open `http://localhost:5173`.
3. Click **Create Draft** and fill required fields (`supplierId`, `invoiceNumber`).
4. Confirm the app navigates to the newly created invoice detail.
5. In detail page (Draft):
   - Edit header fields.
   - Add/edit/delete lines.
   - Confirm **Unsaved changes** appears, then click **Save**.
   - Try **Cancel** after additional edits to revert to the last loaded state.
6. Submit workflow:
   - Click **Submit** (available only in Draft).
   - Click **Approve** (available only after Submitted).
   - Click **Post** (available only after Approved).
7. Return to list page and use:
   - search filter,
   - status dropdown,
   - refresh button and **Last loaded** timestamp.

For frontend-specific notes, see `kewe-ui/README.md`.
