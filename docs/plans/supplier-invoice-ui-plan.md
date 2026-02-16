# Supplier Invoice UI Plan

## Requirements
- Build a Vite + React + TypeScript UI in `/kewe-ui`.
- Show Supplier Invoice list and detail workflow in browser.
- Wire UI to existing backend endpoints.
- Use Vite dev proxy so UI calls `/api/...`.
- Include loading and error states.
- Document run instructions.

## Non-goals
- No backend business rule changes.
- No advanced authentication/authorization.
- No server-side pagination/filtering.

## Proposed API + Data Model
- UI client targets:
  - `GET /api/supplier-invoices`
  - `GET /api/supplier-invoices/{id}`
  - `POST /api/supplier-invoices`
  - `PUT /api/supplier-invoices/{id}/submit|approve|post`
- Vite proxy rewrites `/api` -> backend root.
- UI TypeScript model mirrors `SupplierInvoiceDTO` fields:
  - id, status, supplierId, invoiceNumber, invoiceDate, accountingDate,
    currency, invoiceAmount, lines[], memo, attachmentsMetadata[]

## Test Plan
- Type-check/build frontend (`npm run build`).
- Validate list rendering and detail navigation manually in browser.
- Validate transition buttons only show valid next action.

## Risks / Open Questions
- Backend controller route prefix is `/supplier-invoices` (no `/api`),
  so proxy rewrite must stay enabled for local development.
- If npm registry access is blocked in environment, dependency install/build
  cannot be fully executed locally.
