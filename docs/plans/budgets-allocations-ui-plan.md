# Budgets & Allocations Page Plan

## Requirements
- Add support for **editing and deleting budgets** directly from the budgets table.
- Add support for **editing and deleting allocations** from the nested allocations table.
- Restrict budget/allocation business dimension selection to values sourced from the **Business Dimensions** setup data (the same dataset shown on the Business Dimensions page).
- Keep this iteration in front-end state only (no persistence changes).

## Non-goals
- Backend persistence or new budget/allocation API endpoints.
- New accounting validation rules such as cross-budget balancing.
- Changes to the Business Dimensions maintenance workflows.

## Proposed API + Data Model
- No backend API changes.
- Reuse existing dimensions APIs used by the Business Dimensions page:
  - `fetchDimensionTypes()`
  - `fetchDimensionTree(typeCode)`
- Front-end model updates:
  - `BudgetRow`: `businessDimensionId`, `businessDimensionLabel`, plus existing budget fields.
  - `AllocationRow`: `businessDimensionId`, `businessDimensionLabel`, `allocatedFrom`, `amount`.
- UI behavior:
  - Budget and allocation modals use `<select>` controls populated from active dimension nodes.
  - Deleting budget removes the row and its nested allocations.
  - Editing allocation updates selected dimension and amount in place.

## Test Plan
- Run `npm run build` in `kewe-ui` to verify TypeScript compile and production build.
- Validate budget flows: add, edit, delete.
- Validate allocation flows: add, edit, delete.
- Validate dimension restriction: budget/allocation forms provide a dropdown list sourced from dimensions data only.

## Risks / Open Questions
- Without dedicated backend endpoints, all changes are lost on page reload.
- If dimensions fail to load, budget/allocation selection is temporarily unavailable.
- Current UX does not yet include confirmation prompts for deletes.
