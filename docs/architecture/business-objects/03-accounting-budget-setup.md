# Accounting & Budget Setup Configuration

This configuration exists at the Business Object TYPE level and defaults to instances.

Instance-level override is allowed only if explicitly enabled.

---

## Core Controls

### Posting Controls
- Allow Expense
- Allow Revenue
- Allow Asset Posting
- Allow Liability Posting
- Balance Sheet Allowed
- Net Asset Classification Mapping

### Budget Controls
- Budget Required (Y/N)
- Budget Control Level (Hard/Soft/Advisory)
- Allow Budget Override
- Allow Carryforward
- Budget Year Type

### Default Dimensions
- Default Ledger Account
- Default Company
- Default Function
- Default Restriction Type

### Encumbrance Controls
- Enable Encumbrance
- Enable Pre-Encumbrance
- Release Rules

### Grant / Sponsored Controls (if applicable)
- IDC Eligible
- Sponsor Approval Required
- Allow Cost Transfer
- Reporting Frequency

### Cash / Investment Controls
- Cash Managed
- Investment Managed
- Unitized (for endowment pool)

### Capitalization
- Capitalizable
- Depreciation Profile Default

---

## Override Model

Each configurable field must support:
- Default Value
- Allow Override (Y/N)
- Override Requires Reason (Y/N)
