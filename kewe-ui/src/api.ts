import axios from 'axios';
import type { CreateDraftRequest, SupplierInvoice, UpdateInvoiceRequest } from './api/types';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function fetchInvoices(): Promise<SupplierInvoice[]> {
  const response = await api.get<SupplierInvoice[]>('/supplier-invoices');
  return response.data;
}

export async function fetchInvoiceById(id: string): Promise<SupplierInvoice> {
  const response = await api.get<SupplierInvoice>(`/supplier-invoices/${id}`);
  return response.data;
}

export async function createDraft(payload: CreateDraftRequest): Promise<SupplierInvoice> {
  const response = await api.post<SupplierInvoice>('/supplier-invoices', payload);
  return response.data;
}

export async function updateInvoice(id: string, payload: UpdateInvoiceRequest): Promise<SupplierInvoice> {
  const response = await api.put<SupplierInvoice>(`/supplier-invoices/${id}`, payload);
  return response.data;
}

export async function transitionInvoice(
  id: string,
  action: 'submit' | 'approve' | 'post',
): Promise<void> {
  await api.put(`/supplier-invoices/${id}/${action}`);
}

export function toUpdatePayload(invoice: SupplierInvoice): UpdateInvoiceRequest {
  return {
    supplierId: invoice.supplierId,
    invoiceNumber: invoice.invoiceNumber,
    invoiceDate: invoice.invoiceDate,
    accountingDate: invoice.accountingDate,
    currency: invoice.currency,
    invoiceAmount: invoice.invoiceAmount,
    lines: invoice.lines,
    memo: invoice.memo,
    attachmentsMetadata: invoice.attachmentsMetadata,
  };
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const payload = error.response?.data as { message?: string } | undefined;
    if (payload?.message) {
      return payload.message;
    }
    if (error.response) {
      return `Request failed (${error.response.status})`;
    }
    return 'Unable to reach API server';
  }

  return 'Unexpected error';
}
