# Requisition Form Usability Plan

## Requirements
- Rename the top drafting section to **Requisition Assistant** and keep manual line entry available.
- Add line-level **Charging Instructions** and support one-click apply-to-all updates.
- Keep charging locations limited to business dimensions with budget/allocation backing.
- Present **Funding Snapshot** as a table that shows funding location, charging location, proposed amount, and projected available balances.
- Move header fields into a clearly named **Requisition Information** section.

## Non-goals
- No visual restyling outside existing Kewe UI tokens/components.
- No change to approval/submission workflow.

## Proposed API + Data Model
- Add `chargingInstructions` to `RequisitionLine` (backend + UI type).
- Extend `GET /api/funding-snapshot` with optional `proposedAmount` query param.
- Extend funding snapshot response with:
  - `allocationsTo`
  - `fundingSources[]` rows for table rendering
  - totals for allocated-in and charging remaining.

## Test Plan
- Update/add integration test coverage for funding snapshot projected funding rows.
- Run backend integration tests for funding snapshot and charging locations.
- Run frontend lint to validate TypeScript updates.

## Risks / Open Questions
- Projected balances currently assume a single requisition subtotal applied per funding source row.
- If charging location has mixed direct budget plus inbound allocations, UI displays available balances but does not split line-by-line pro-rating.
