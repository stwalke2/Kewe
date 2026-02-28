export type InvoiceStatus = 'Draft' | 'Submitted' | 'Approved' | 'Posted';

export interface InvoiceLine {
  description: string;
  amount: number;
}

export interface AttachmentMetadata {
  fileName: string;
  contentType: string;
  size: number;
}

export interface SupplierInvoice {
  id: string;
  status: InvoiceStatus;
  supplierId: string;
  invoiceNumber: string;
  invoiceDate?: string;
  accountingDate?: string;
  invoiceAmount?: number;
  currency?: string;
  lines: InvoiceLine[];
  memo?: string;
  attachmentsMetadata: AttachmentMetadata[];
}

export interface CreateDraftRequest {
  supplierId: string;
  invoiceNumber: string;
  invoiceDate?: string;
  accountingDate?: string;
  currency?: string;
  invoiceAmount?: number;
  lines?: InvoiceLine[];
  memo?: string;
}

export interface UpdateInvoiceRequest {
  supplierId: string;
  invoiceNumber: string;
  invoiceDate?: string;
  accountingDate?: string;
  currency?: string;
  invoiceAmount?: number;
  lines: InvoiceLine[];
  memo?: string;
  attachmentsMetadata?: AttachmentMetadata[];
}

export type EntryBehavior = 'REQUIRED' | 'OPTIONAL' | 'DERIVED';

export interface DimensionType {
  id: string;
  code: string;
  name: string;
  description?: string;
  status: string;
  hierarchical: boolean;
  maxDepth: number;
  entryBehavior: EntryBehavior;
}

export interface DimensionNode {
  id: string;
  typeCode: string;
  code: string;
  name: string;
  description?: string;
  status: string;
  parentId?: string;
  path: string;
  depth: number;
  sortOrder: number;
  attributes: Record<string, unknown>;
}

export interface DimensionMapping {
  id: string;
  mappingType: 'ITEM_TO_LEDGER' | 'COSTCENTER_TO_ORG' | 'AWARDDRIVER_TO_FUND' | 'DEFAULT_FUNCTION';
  sourceTypeCode: string;
  sourceNodeId?: string;
  sourceKey?: string;
  targetTypeCode: string;
  targetNodeId: string;
  status: string;
}


export interface ApiErrorDetails {
  endpoint?: string;
  status?: number;
  message: string;
}

export interface ConfiguredField<T> {
  defaultValue?: T;
  allowOverride: boolean;
  overrideReasonRequired: boolean;
}

export interface AccountingBudgetSetup {
  [key: string]: ConfiguredField<boolean | string | string[] | undefined>;
}

export interface BusinessObjectType {
  createdAt?: string;
  updatedAt?: string;
  id: string;
  code: string;
  name: string;
  objectKind: string;
  status: string;
  description?: string;
  requiredOnFinancialTransactions?: boolean;
  requiredBalancing?: boolean;
  budgetControlEnabled?: boolean;
  allowInstanceAccountingBudgetOverride?: boolean;
  accountingBudgetDefaults: AccountingBudgetSetup;
}

export interface HierarchyAssignment {
  hierarchyCode: string;
  parentObjectId?: string;
}

export interface BusinessObjectFieldOverride {
  value: boolean | string | string[];
  overrideReason?: string;
}

export interface BusinessObjectInstance {
  createdAt?: string;
  updatedAt?: string;
  id: string;
  typeCode: string;
  objectKind: string;
  code: string;
  name: string;
  description?: string;
  requiredOnFinancialTransactions?: boolean;
  requiredBalancing?: boolean;
  budgetControlEnabled?: boolean;
  effectiveDate?: string;
  visibility?: string;
  hierarchies?: HierarchyAssignment[];
  roles?: Array<{ roleCode?: string; actorObjectId?: string; effectiveDate?: string; endDate?: string; status?: string }>;
  status: string;
  accountingBudgetOverrides: Record<string, BusinessObjectFieldOverride>;
}


export interface RequisitionLine {
  lineNumber: number;
  description: string;
  quantity: number;
  uom?: string;
  unitPrice?: number;
  amount: number;
  supplierName: string;
  supplierUrl?: string;
  supplierSku?: string;
}

export interface RequisitionDraft {
  id: string;
  status: 'DRAFT' | 'SUBMITTED';
  title: string;
  memo?: string;
  requesterName: string;
  currency: string;
  needByDate?: string;
  chargingBusinessDimensionId?: string;
  chargingBusinessDimensionCode?: string;
  chargingBusinessDimensionName?: string;
  budgetPlanId?: string;
  lines: RequisitionLine[];
  totals: { subtotal: number };
}

export interface ChargingLocation {
  id: string;
  code: string;
  name: string;
  typeName: string;
  status: string;
}

export interface SupplierResult {
  supplierName: string;
  title: string;
  url: string;
  price?: number;
  sku?: string;
  snippet?: string;
}

export interface SupplierDebug {
  elapsedMs: number;
  blockedOrFailed: boolean;
  source: 'playwright' | 'searchProvider' | 'linkOnly';
  reason?: string;
}

export interface AgentCapabilities {
  provider: 'bing' | string;
  hasKey: boolean;
}

export interface AgentDraftResponse {
  parsed: { quantity: number; item: string; keywords: string[]; orgHint?: string; uom?: string };
  suggestedChargingDimension?: ChargingLocation;
  searchLinks: Record<string, string>;
  results: Record<string, SupplierResult[]>;
  draft: { title: string; memo: string; currency: string; lines: RequisitionLine[] };
  warnings: string[];
}

export interface FundingSnapshot {
  chargingDimension?: ChargingLocation;
  budgetPlan?: { id: string; name: string };
  budget?: { id: string; amount: number };
  allocationsFrom: Array<{ id: string; allocatedTo?: ChargingLocation; amount: number }>;
  totals: {
    budgetTotal?: number;
    allocatedFromTotal: number;
    remainingBeforeReq?: number;
  };
}
