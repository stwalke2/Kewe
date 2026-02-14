# ADR 0005 – Supplier Invoice Minimal Data Model (MVP)

## Status
Accepted

## Decision
For the Supplier Invoice MVP slice, we will implement a minimal invoice object that supports:
Draft → Submitted → Approved → Posted.

## SupplierInvoice (MVP Fields)

### Identity
- id (system-generated)
- invoiceNumber (supplier-provided)
- referenceId (optional external reference)

### Dates
- invoiceDate
- accountingDate (set/required at Post time)

### Parties / Org
- supplierId
- companyId

### Amounts
- currency (default USD allowed)
- lines[]:
  - lineNumber
  - description
  - amount (or qty + unitPrice)
  - spendCategoryOrAccountId
- invoiceAmount (stored or derived; must equal sum(lines))

### Workflow
- status: Draft | Submitted | Approved | Posted
- assigneeWorkerId (optional)
- approvedByWorkerId (set on approval)

### Supporting
- memo (optional)
