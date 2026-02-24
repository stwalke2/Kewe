export const helpDefinitions = {
  budgetRequired: {
    label: 'Budget Required',
    definition: 'If enabled, transactions charged to this object must have an available budget (based on the configured control level).',
  },
  budgetControlLevel: {
    label: 'Budget Control Level',
    definition: 'Controls how strictly Kewe enforces budget availability. HARD blocks transactions, SOFT warns but can proceed (with approval or reason), ADVISORY is informational only, NONE disables budget checking.',
  },
  allowBudgetOverride: {
    label: 'Allow Budget Override',
    definition: 'If enabled, authorized users can proceed even when budget is insufficient (subject to control level and approvals).',
  },
  allowCarryforward: {
    label: 'Allow Carryforward',
    definition: 'If enabled, unused budget may carry forward into the next budget year according to policy.',
  },
  allowExpensePosting: {
    label: 'Allow Expense Posting',
    definition: 'If enabled, this object can be used on expense transactions (e.g., requisitions, invoices, journals).',
  },
  allowRevenuePosting: {
    label: 'Allow Revenue Posting',
    definition: 'If enabled, this object can be used on revenue transactions (e.g., deposits, billing, revenue journals).',
  },
  enableEncumbrance: {
    label: 'Encumbrance',
    definition: 'If enabled, Kewe reserves budget when commitments are made (e.g., requisitions/POs) and releases or relieves it based on lifecycle rules.',
  },
  enablePreEncumbrance: {
    label: 'Pre-Encumbrance',
    definition: 'If enabled, Kewe reserves budget earlier in the process (often at requisition) before a formal PO or obligation is created.',
  },
  defaultLedgerAccountId: {
    label: 'Default Ledger Account',
    definition: 'The natural account Kewe will default when this object is used, unless overridden or derived by rules.',
  },
  defaultFunctionId: {
    label: 'Default Function',
    definition: 'The default functional classification (e.g., NACUBO) applied when this object is used.',
  },
  restrictionType: {
    label: 'Restriction Type',
    definition: 'Indicates whether funds are unrestricted, donor restricted, or agency/held for others; used for reporting and posting rules.',
  },
  netAssetClassMapping: {
    label: 'Net Asset Class Mapping',
    definition: 'Maps activity to the appropriate net asset classification for financial reporting.',
  },
  idcEligible: {
    label: 'IDC Eligible',
    definition: 'If enabled, transactions may be included in indirect cost (F&A) calculations for sponsored activity, subject to sponsor terms.',
  },
  sponsorApprovalRequired: {
    label: 'Sponsor Approval Required',
    definition: 'If enabled, certain actions (e.g., budget changes or cost transfers) require sponsor or central sponsored-program approval.',
  },
  allowCostTransfer: {
    label: 'Allow Cost Transfer',
    definition: 'If enabled, costs may be moved onto or off of this object after posting, subject to policy, timing, and approvals.',
  },
  cashManaged: {
    label: 'Cash Managed',
    definition: 'If enabled, cash activity is centrally managed and may be restricted from direct use depending on policy.',
  },
  investmentManaged: {
    label: 'Investment Managed',
    definition: 'If enabled, balances are invested/managed centrally and may follow specific rules for spending and valuation.',
  },
  unitized: {
    label: 'Unitized',
    definition: 'If enabled, value/ownership is tracked in units (common for pooled endowment investing) rather than only by dollars.',
  },
  capitalizable: {
    label: 'Capitalizable',
    definition: 'If enabled, certain purchases may be treated as capital assets (subject to capitalization rules) instead of expense.',
  },
  defaultDepreciationProfile: {
    label: 'Depreciation Profile',
    definition: 'Defines default depreciation method/life for capital assets associated with this object (placeholder until fixed-asset module exists).',
  },
} as const;

export type HelpTerm = keyof typeof helpDefinitions;
