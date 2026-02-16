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
  currency?: string;
  invoiceAmount?: number;
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
