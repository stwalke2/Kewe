# ADR 0002 – MVP Slice: Supplier Invoice

## Status
Accepted

## Decision
We will use **Supplier Invoice** as the first vertical (end-to-end) slice of the system.

The MVP workflow will be:

**Draft → Submitted → Approved → Posted → Paid (stubbed/optional in early MVP)**

## Rationale
Supplier Invoice is a strong first slice because it is a core ERP transaction that is bounded but forces the foundational architecture to exist end-to-end. Specifically, it proves:

- The **canonical object model** (structure, validation, status, audit fields)
- A minimal **workflow/state machine** and transitions
- **Audit trail** and history of changes
- **Role-based approvals** (who can submit/approve)
- A **posting action** that produces a financial event/entries
- A minimal **ledger abstraction** (even if simplified)

## Scope for MVP Slice
Included:
- Create invoice (Draft)
- Submit invoice (transition + validation)
- Approve invoice (role check + transition)
- Post invoice (creates posting output + transition)
- Read invoice + status + audit history
- Tests covering state transitions and key validations

Explicitly excluded (for now):
- 3-way match (PO/Receipt matching)
- Complex tax/VAT logic
- Multi-currency
- OCR / invoice capture
- Routing rules beyond a
