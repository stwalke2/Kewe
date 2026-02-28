# Requisition Agent Search + Charging Locations Plan

## Requirements
- The **Find Items & Draft Requisition** action should perform real retrieval attempts and return supplier result rows for Amazon, Fisher Scientific, and Home Depot whenever possible.
- If direct supplier parsing yields no rows, the backend should use a fallback strategy that still finds product links and titles constrained to each supplier domain.
- Agent draft generation should continue pre-filling requisition lines from discovered supplier results.
- Charging Location options on the requisition page should include business dimensions referenced on the Budgets screen (budget rows + allocation targets), in addition to backend charging locations.

## Non-goals
- Building full browser automation against anti-bot flows or login-gated supplier experiences.
- Replacing current budgets screen local-storage model with a persisted backend model.
- Implementing vendor-specific paid search APIs.

## Proposed API + Data Model
- **Backend API:** no endpoint shape changes.
  - Enhance server-side supplier collection behavior in `AgentSearchService` to include fallback results from web search constrained to supplier domains.
- **Frontend data model:** no API type changes.
  - Extend requisition page loading logic to read local storage key `kewe.budgets`, collect businessDimensionIds from budget/allocation rows, and fetch matching business objects for labels.

## Test Plan
- Backend integration test for `/api/agent/requisition-draft`:
  - Keep fixture-driven supplier parsing for Fisher/Home Depot.
  - Add assertion that fallback can provide Amazon results when direct parsing is empty (using mocked HTML fetch for fallback endpoint).
- Frontend unit-level helper tests are optional; validate with TypeScript build/lint and existing app tests.

## Risks / Open Questions
- External fallback endpoint may vary its HTML structure; parsing should be defensive and capped.
- Local storage budgets may include stale or deleted dimension IDs; UI should tolerate missing dimensions.
- Potential duplicates between backend charging locations and local-storage-derived locations need deduplication by ID.
