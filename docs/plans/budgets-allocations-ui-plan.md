# Budgets & Allocations Page Plan

## Requirements
- Add a dedicated Budgets page in the UI that lists budget records by business dimension.
- Provide an **Add Budget** action that opens a popup form to create a new budget row.
- Show each budget row with `Business Dimension`, `Budget Plan`, `Budget`, and allocation actions.
- Support expanding a budget row to view a nested Allocations table.
- Provide an **Add Allocation** action that opens a popup form to add allocation records.
- Provide an **Edit Budget** action that opens a popup form to edit budget fields.
- Persist page edits in local in-memory state for this iteration.

## Non-goals
- Backend persistence or new API endpoints.
- Validation against accounting rules (e.g., budget over-allocation constraints).
- Cross-page integration into other setup workflows.

## Proposed API + Data Model
- No backend API changes.
- Front-end local model:
  - `BudgetRow`: `id`, `businessDimension`, `budgetPlan`, `budgetAmount`, `canAllocate`, `allocations`.
  - `AllocationRow`: `id`, `businessDimension`, `allocatedFrom`, `amount`.
- Form model:
  - Budget modal: `businessDimension`, `budgetPlan`, `budgetAmount`, `canAllocate`.
  - Allocation modal: `businessDimension`, `amount` (with `allocatedFrom` derived from parent budget).

## Test Plan
- Build the UI bundle (`npm run build`) to verify TypeScript and Vite compile.
- Verify row expansion and nested table rendering from state updates.
- Verify modal flows for Add Allocation and Edit Budget update visible table values.

## Risks / Open Questions
- Without backend persistence, changes reset on page reload.
- Allocation eligibility is currently controlled by a manual `canAllocate` checkbox.
- No server-side constraints yet for allocation amount or dimension validity.
