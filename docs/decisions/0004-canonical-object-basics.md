# ADR 0004 – Canonical Object Basics

## Status
Accepted

## Decision
All primary domain entities will be represented as **Canonical Objects** with a consistent core shape to support auditability, effective dating, extensibility, and cross-module consistency.

## CanonicalObject (Core Fields)

### Identity
- id (system-generated, immutable)
- referenceId (optional human/business identifier; unique within type when applicable)

### Classification
- type (e.g., SupplierInvoice, Supplier, Worker)
- subtype (optional; used for specialization without new tables/collections)

### Presentation (Optional)
- name (optional display name)

### Lifecycle / State
- status (object lifecycle; for workflow-enabled objects, includes workflow states)

### Effective Dating (Optional by type)
- effective:
  - effectiveFrom (optional)
  - effectiveTo (optional)

### Audit (Required)
- audit:
  - createdAt
  - createdBy
  - updatedAt
  - updatedBy
  - version (integer for optimistic locking / change tracking)

### Extensibility
- metadata (free-form key/value map for non-core attributes)
- tags / dimensions (normalized list of key/value pairs used for reporting/classification)

### Attachments (Placeholder in MVP)
- attachments[] (metadata only: filename, contentType, size, storageRef)

## Explicitly Deferred (Not in CanonicalObject Core for MVP)
- hierarchy (handled by specific domain objects that require it)
- visibility / security rules (handled by a separate authorization model, not embedded as core fields)
