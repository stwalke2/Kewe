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

  budgetYearType: {
    label: 'Budget Year Type',
    definition: 'Defines the budget period used by this object, such as fiscal year, grant period, or project period.',
  },
  defaultLedgerAccountId: {
    label: 'Default Ledger Account',
    definition: 'The natural account Kewe will default when this object is used, unless overridden or derived by rules.',
  },

  defaultCompanyId: {
    label: 'Default Company',
    definition: 'The default company/legal entity applied when this object is used, unless overridden by downstream derivation rules.',
  },
  defaultFunctionId: {
    label: 'Default Function',
    definition: 'The default functional classification (e.g., NACUBO) applied when this object is used.',
  },

  allowAssetPosting: {
    label: 'Allow Asset Posting',
    definition: 'If enabled, this object can be used on asset-side entries and defaulted on transactions that create or adjust assets.',
  },
  allowLiabilityPosting: {
    label: 'Allow Liability Posting',
    definition: 'If enabled, this object can be used on liability-side entries for obligations, accruals, and settlement events.',
  },
  allowBalanceSheetPosting: {
    label: 'Allow Balance Sheet Posting',
    definition: 'If enabled, this object can post to balance sheet accounts where policy allows balance-sheet activity.',
  },
  restrictionType: {
    label: 'Restriction Type',
    definition: 'Indicates whether funds are unrestricted, donor restricted, or agency/held for others; used for reporting and posting rules.',
  },

  defaultRestrictionType: {
    label: 'Default Restriction Type',
    definition: 'Pre-populates a restriction type for downstream postings and reporting when this object is selected.',
  },
  netAssetClassMapping: {
    label: 'Net Asset Class Mapping',
    definition: 'Maps activity to the appropriate net asset classification for financial reporting.',
  },

  encumbranceReleaseRule: {
    label: 'Encumbrance Release Rule',
    definition: 'Controls when encumbrances are released or relieved (for example at PO close, invoice post, or by manual action).',
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

  chargeObjectEnabled: {
    label: 'Charge Object Enabled',
    definition: 'If enabled, this object can be selected as a charge target in spending workflows (requisitions, invoices, journals).',
  },
  directPostAllowed: {
    label: 'Direct Post Allowed',
    definition: 'If enabled, this object can be charged directly without requiring a separate bridging or funding object.',
  },
  requiresSpendAuthority: {
    label: 'Requires Spend Authority',
    definition: 'If enabled, Kewe requires an appropriate spend authority role/approval before charging this object.',
  },
  spendAuthorityRoleKey: {
    label: 'Spend Authority Role',
    definition: 'Specifies the role used to evaluate spend authority for charges to this object (e.g., Cost Center Manager, Fund Steward).',
  },
  liquidityRequired: {
    label: 'Liquidity Required',
    definition: 'If enabled, Kewe checks that sufficient liquidity is available before allowing charges to this object.',
  },
  liquiditySourceMode: {
    label: 'Liquidity Source',
    definition: 'Determines where liquidity is evaluated: on this object (Self), via a bridging object (Bridge), externally (External), or not evaluated (None).',
  },
  bridgingAllowed: {
    label: 'Bridging Allowed',
    definition: 'If enabled, this object may use a bridging object (such as a top-up or funding source) when charging.',
  },
  bridgingRequired: {
    label: 'Bridging Required',
    definition: 'If enabled, charges to this object must include a bridging object; direct charging is not permitted.',
  },
  bridgingObjectTypeCodes: {
    label: 'Allowed Bridging Types',
    definition: 'Restricts which business object types are permitted to act as bridging objects for charges.',
  },
  defaultBridgingObjectId: {
    label: 'Default Bridging Object',
    definition: 'Specifies a default bridging object that Kewe will suggest when charging this object (placeholder until lookup exists).',
  },
  fundingSplitAllowed: {
    label: 'Funding Split Allowed',
    definition: 'If enabled, a single charge may be split across multiple funding sources.',
  },
  fundingSplitMode: {
    label: 'Funding Split Mode',
    definition: 'Controls how splits are expressed: by dollar amount or by percentage (or none).',
  },
  budgetCheckPoint: {
    label: 'Budget Check Point',
    definition: 'Specifies when budget is evaluated for this object in workflows (e.g., at requisition, PO, invoice, or posting).',
  },
  allowedSpendCategoriesMode: {
    label: 'Spend Category Restriction',
    definition: 'Controls whether all spend categories are allowed, or whether Kewe enforces an allow list or deny list.',
  },
  allowedSpendCategoryIds: {
    label: 'Allowed Spend Categories',
    definition: 'Specifies which spend categories are permitted for charges to this object (allow list).',
  },
  deniedSpendCategoryIds: {
    label: 'Denied Spend Categories',
    definition: 'Specifies which spend categories are not permitted for charges to this object (deny list).',
  },
} as const;

export type HelpTerm = keyof typeof helpDefinitions;
