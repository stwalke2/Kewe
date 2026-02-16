import axios from 'axios';
import type {
  CreateDraftRequest,
  DimensionMapping,
  DimensionNode,
  DimensionType,
  SupplierInvoice,
  UpdateInvoiceRequest,
} from './api/types';

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

export async function transitionInvoice(id: string, action: 'submit' | 'approve' | 'post'): Promise<void> {
  await api.put(`/supplier-invoices/${id}/${action}`);
}

export async function fetchDimensionTypes(): Promise<DimensionType[]> {
  const response = await api.get<DimensionType[]>('/dimension-types');
  return response.data;
}

export async function fetchDimensionTree(typeCode: string, includeInactive = false): Promise<DimensionNode[]> {
  const response = await api.get<DimensionNode[]>(`/dimensions/${typeCode}/tree`, { params: { includeInactive } });
  return response.data;
}

export async function searchDimensionNodes(typeCode: string, query: string, includeInactive = false): Promise<DimensionNode[]> {
  const response = await api.get<DimensionNode[]>(`/dimensions/${typeCode}/search`, { params: { q: query, includeInactive } });
  return response.data;
}

export async function createDimensionNode(typeCode: string, payload: Partial<DimensionNode>): Promise<DimensionNode> {
  const response = await api.post<DimensionNode>(`/dimensions/${typeCode}/nodes`, payload);
  return response.data;
}

export async function updateDimensionNode(typeCode: string, nodeId: string, payload: Partial<DimensionNode>): Promise<DimensionNode> {
  const response = await api.put<DimensionNode>(`/dimensions/${typeCode}/nodes/${nodeId}`, payload);
  return response.data;
}

export async function setDimensionNodeStatus(typeCode: string, nodeId: string, status: 'Active' | 'Inactive'): Promise<DimensionNode> {
  const response = await api.patch<DimensionNode>(`/dimensions/${typeCode}/nodes/${nodeId}/status`, { status });
  return response.data;
}

export async function moveDimensionNode(typeCode: string, nodeId: string, newParentId?: string): Promise<DimensionNode[]> {
  const response = await api.post<DimensionNode[]>(`/dimensions/${typeCode}/move`, { nodeId, newParentId });
  return response.data;
}

export async function reorderDimensionNodes(typeCode: string, parentId: string | undefined, nodeIds: string[]): Promise<DimensionNode[]> {
  const response = await api.post<DimensionNode[]>(`/dimensions/${typeCode}/reorder`, { parentId, nodeIds });
  return response.data;
}

export async function fetchMappings(path: string): Promise<DimensionMapping[]> {
  const response = await api.get<DimensionMapping[]>(`/dimensions/mappings/${path}`);
  return response.data;
}

export async function upsertMapping(path: string, payload: unknown): Promise<DimensionMapping> {
  const response = await api.post<DimensionMapping>(`/dimensions/mappings/${path}`, payload);
  return response.data;
}

export async function deleteMapping(path: string, params: Record<string, string>): Promise<void> {
  await api.delete(`/dimensions/mappings/${path}`, { params });
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
