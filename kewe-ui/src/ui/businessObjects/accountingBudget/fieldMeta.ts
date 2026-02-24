export type AccountingBudgetControlType = 'toggle' | 'select' | 'lookup' | 'text';

export interface AccountingBudgetFieldMeta {
  key: string;
  label: string;
  helpTerm: string;
  section:
    | 'Posting Controls'
    | 'Budget Controls'
    | 'Default Dimensions'
    | 'Encumbrance Controls'
    | 'Sponsored Controls'
    | 'Cash / Investment Controls'
    | 'Capitalization';
  controlType: AccountingBudgetControlType;
  selectOptions?: Array<{ value: string; label: string }>;
}

export const ACCOUNTING_BUDGET_FIELDS: AccountingBudgetFieldMeta[] = [
  { key: 'allowExpensePosting', label: 'Allow Expense Posting', helpTerm: 'allowExpensePosting', section: 'Posting Controls', controlType: 'toggle' },
  { key: 'allowRevenuePosting', label: 'Allow Revenue Posting', helpTerm: 'allowRevenuePosting', section: 'Posting Controls', controlType: 'toggle' },
  { key: 'allowAssetPosting', label: 'Allow Asset Posting', helpTerm: 'allowAssetPosting', section: 'Posting Controls', controlType: 'toggle' },
  { key: 'allowLiabilityPosting', label: 'Allow Liability Posting', helpTerm: 'allowLiabilityPosting', section: 'Posting Controls', controlType: 'toggle' },
  { key: 'allowBalanceSheetPosting', label: 'Allow Balance Sheet Posting', helpTerm: 'allowBalanceSheetPosting', section: 'Posting Controls', controlType: 'toggle' },
  { key: 'budgetRequired', label: 'Budget Required', helpTerm: 'budgetRequired', section: 'Budget Controls', controlType: 'toggle' },
  { key: 'budgetControlLevel', label: 'Budget Control Level', helpTerm: 'budgetControlLevel', section: 'Budget Controls', controlType: 'select', selectOptions: [{ value: 'HARD', label: 'Hard' }, { value: 'SOFT', label: 'Soft' }, { value: 'ADVISORY', label: 'Advisory' }, { value: 'NONE', label: 'None' }] },
  { key: 'allowBudgetOverride', label: 'Allow Budget Override', helpTerm: 'allowBudgetOverride', section: 'Budget Controls', controlType: 'toggle' },
  { key: 'allowCarryforward', label: 'Allow Carryforward', helpTerm: 'allowCarryforward', section: 'Budget Controls', controlType: 'toggle' },
  { key: 'budgetYearType', label: 'Budget Year Type', helpTerm: 'budgetYearType', section: 'Budget Controls', controlType: 'select', selectOptions: [{ value: 'FISCAL_YEAR', label: 'Fiscal Year' }, { value: 'GRANT_PERIOD', label: 'Grant Period' }, { value: 'PROJECT_PERIOD', label: 'Project Period' }] },
  { key: 'defaultCompanyId', label: 'Default Company ID', helpTerm: 'defaultCompanyId', section: 'Default Dimensions', controlType: 'lookup' },
  { key: 'defaultFunctionId', label: 'Default Function ID', helpTerm: 'defaultFunctionId', section: 'Default Dimensions', controlType: 'lookup' },
  { key: 'defaultLedgerAccountId', label: 'Default Ledger Account ID', helpTerm: 'defaultLedgerAccountId', section: 'Default Dimensions', controlType: 'lookup' },
  { key: 'restrictionType', label: 'Restriction Type', helpTerm: 'restrictionType', section: 'Default Dimensions', controlType: 'select', selectOptions: [{ value: 'UNRESTRICTED', label: 'Unrestricted' }, { value: 'DONOR_RESTRICTED', label: 'Donor Restricted' }, { value: 'AGENCY', label: 'Agency' }] },
  { key: 'defaultRestrictionType', label: 'Default Restriction Type', helpTerm: 'defaultRestrictionType', section: 'Default Dimensions', controlType: 'select', selectOptions: [{ value: 'UNRESTRICTED', label: 'Unrestricted' }, { value: 'DONOR_RESTRICTED', label: 'Donor Restricted' }, { value: 'AGENCY', label: 'Agency' }] },
  { key: 'netAssetClassMapping', label: 'Net Asset Class Mapping', helpTerm: 'netAssetClassMapping', section: 'Default Dimensions', controlType: 'select', selectOptions: [{ value: 'UNRESTRICTED', label: 'Unrestricted' }, { value: 'WITH_DONOR_RESTRICTIONS', label: 'With Donor Restrictions' }] },
  { key: 'enableEncumbrance', label: 'Enable Encumbrance', helpTerm: 'enableEncumbrance', section: 'Encumbrance Controls', controlType: 'toggle' },
  { key: 'enablePreEncumbrance', label: 'Enable Pre-Encumbrance', helpTerm: 'enablePreEncumbrance', section: 'Encumbrance Controls', controlType: 'toggle' },
  { key: 'encumbranceReleaseRule', label: 'Encumbrance Release Rule', helpTerm: 'encumbranceReleaseRule', section: 'Encumbrance Controls', controlType: 'select', selectOptions: [{ value: 'AT_PO_CLOSE', label: 'At PO Close' }, { value: 'AT_INVOICE_POST', label: 'At Invoice Post' }, { value: 'MANUAL', label: 'Manual' }] },
  { key: 'idcEligible', label: 'IDC Eligible', helpTerm: 'idcEligible', section: 'Sponsored Controls', controlType: 'toggle' },
  { key: 'sponsorApprovalRequired', label: 'Sponsor Approval Required', helpTerm: 'sponsorApprovalRequired', section: 'Sponsored Controls', controlType: 'toggle' },
  { key: 'allowCostTransfer', label: 'Allow Cost Transfer', helpTerm: 'allowCostTransfer', section: 'Sponsored Controls', controlType: 'toggle' },
  { key: 'cashManaged', label: 'Cash Managed', helpTerm: 'cashManaged', section: 'Cash / Investment Controls', controlType: 'toggle' },
  { key: 'investmentManaged', label: 'Investment Managed', helpTerm: 'investmentManaged', section: 'Cash / Investment Controls', controlType: 'toggle' },
  { key: 'unitized', label: 'Unitized', helpTerm: 'unitized', section: 'Cash / Investment Controls', controlType: 'toggle' },
  { key: 'capitalizable', label: 'Capitalizable', helpTerm: 'capitalizable', section: 'Capitalization', controlType: 'toggle' },
  { key: 'defaultDepreciationProfile', label: 'Default Depreciation Profile', helpTerm: 'defaultDepreciationProfile', section: 'Capitalization', controlType: 'text' },
];

export const ACCOUNTING_BUDGET_SECTIONS = ['Posting Controls', 'Budget Controls', 'Default Dimensions', 'Encumbrance Controls', 'Sponsored Controls', 'Cash / Investment Controls', 'Capitalization'] as const;

export function normalizeValue(value: unknown, controlType: AccountingBudgetControlType): string | boolean {
  if (controlType === 'toggle') return value === true || value === 'true';
  return value == null ? '' : String(value);
}

export function formatDisplayValue(value: unknown, field: AccountingBudgetFieldMeta): string {
  if (value == null || value === '') return '—';
  if (field.controlType === 'toggle') return value === true || value === 'true' ? 'Enabled' : 'Disabled';
  const option = field.selectOptions?.find((item) => item.value === String(value));
  return option?.label ?? String(value);
}
