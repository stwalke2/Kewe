# Kewe Business Object Framework

We are replacing the current "Accounting Dimensions" model with a unified Business Object framework.

All financial classification objects (Cost Center, Fund, Grant, Project, etc.) will inherit from a common abstract BusinessObject base class.

Business Objects serve as:
- Posting dimensions in accounting entries
- Budget control anchors
- Responsibility structures
- Funding sources
- Activity trackers
- Reporting slices

They are NOT just reporting dimensions. They have lifecycle, roles, accounting behavior, and default configuration.

---

## Core Design Principles

1. All Business Objects share a common structure.
2. All Business Objects allow multiple hierarchies.
3. All Business Objects have Roles.
4. All Business Objects have Accounting/Budget configuration.
5. Accounting/Budget configuration defaults at the OBJECT TYPE level.
6. Individual object instances may override defaults (controlled).
7. Ledger Account is separate and required.
8. Spend/Revenue Category drives behavior, not Ledger Account.

---

## Business Object Core Tabs

Every Business Object must support:

### 1. Basic Setup
- Code
- Name
- Description
- Effective Date
- Status (Active/Inactive)
- Visibility settings
- Hierarchies (multi-hierarchy support)
- Parent relationships

### 2. Roles
- Assigned Roles (e.g., Manager, Financial Analyst, Approver)
- Role-based permissions
- Effective dating

### 3. Accounting/Budget Setup
See separate specification file.
