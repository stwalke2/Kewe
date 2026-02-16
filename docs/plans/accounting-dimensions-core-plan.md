# CORE Accounting Dimensions Plan

## Requirements
- Implement canonical accounting dimensions as generic Mongo-backed models (no per-dimension collections/entities).
- Seed canonical `DimensionType` standards:
  - Natural: `LEDGER_ACCOUNT`, `SPEND_ITEM`, `REVENUE_ITEM`
  - Responsibility: `ORGANIZATION`, `COST_CENTER`
  - Purpose: `PROGRAM`, `FUNCTION`
  - Funding: `FUND`
  - Award/authorization: `GIFT`, `GRANT`, `PROJECT`, `APPROPRIATION`
- Support core mapping/derivation rules:
  - Item (`SPEND_ITEM`/`REVENUE_ITEM`) -> `LEDGER_ACCOUNT`
  - `COST_CENTER` -> `ORGANIZATION`
  - Award Driver one-of `{GIFT, GRANT, PROJECT, APPROPRIATION, NONE}` derives `FUND`
  - Default `FUNCTION` derivation from `PROGRAM`/`ORGANIZATION`/`LEDGER_ACCOUNT` with precedence and override capability.
- Provide REST APIs for dimension type CRUD, node CRUD/tree/move/reorder/search, and mapping CRUD.
- Enforce constraints: unique `(typeCode, code)`, no tree cycles, same-type parent-child, depth <= type maxDepth, block delete with children.
- Add indexes for tree traversal, search, and mapping lookups.
- Provide frontend MVP pages for dimensions and mappings.
- Add seed nodes and sample mappings for demo value.
- Add integration tests for tree constraints and mapping lookups.

## Non-goals
- No optional ad-hoc reporting tags in this slice.
- No GraphQL or new state/state-management frameworks.
- No drag/drop tree interaction; use simple form/table workflows.

## Proposed API + Data Model
- `DimensionType` collection: `id`, `code`, `name`, `description`, `status`, `hierarchical`, `maxDepth`, `entryBehavior`, audit fields.
- `DimensionNode` collection: `id`, `typeCode`, `code`, `name`, `description`, `status`, `parentId`, `path`, `depth`, `sortOrder`, `attributes`, audit fields.
- `DimensionMapping` collection (generic): `id`, `mappingType`, `sourceTypeCode`, `sourceNodeId`, `sourceKey`, `targetTypeCode`, `targetNodeId`, `context`, `status`, audit fields.
- APIs under `/api`:
  - `/api/dimension-types`
  - `/api/dimensions/{typeCode}/nodes`
  - `/api/dimensions/{typeCode}/tree`
  - `/api/dimensions/{typeCode}/move`
  - `/api/dimensions/{typeCode}/reorder`
  - `/api/dimensions/{typeCode}/search?q=`
  - `/api/dimensions/mappings/item-to-ledger`
  - `/api/dimensions/mappings/costcenter-to-org`
  - `/api/dimensions/mappings/awarddriver-to-fund`
  - `/api/dimensions/mappings/default-function`

## Test plan
- Spring integration tests with Testcontainers MongoDB for:
  - create type and node
  - duplicate code rejection
  - cycle prevention
  - move updates materialized path for descendants
  - mapping lookups for item->ledger, cost center->organization, award driver->fund.
- Frontend build/typecheck with `npm run build`.

## Risks / Open questions
- Tree operations (move/reorder) must remain consistent and performant for deeper hierarchies.
- `NONE` award driver requires special handling because `driverNodeId` is nullable.
- Existing UI currently routes around supplier invoices only; adding dimensions may require lightweight navigation/layout updates.
