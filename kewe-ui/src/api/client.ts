import axios from 'axios';
import type { CreateDraftRequest, SupplierInvoice } from './types';

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

export async function transitionInvoice(
  id: string,
  action: 'submit' | 'approve' | 'post',
): Promise<void> {
  await api.put(`/supplier-invoices/${id}/${action}`);
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
