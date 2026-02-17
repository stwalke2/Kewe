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
