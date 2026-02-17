import type {
  ApiErrorDetails,
  CreateDraftRequest,
  DimensionMapping,
  DimensionNode,
  DimensionType,
  SupplierInvoice,
  UpdateInvoiceRequest,
} from './api/types';

const API_BASE_PATH = '/api';

async function request<T>(endpoint: string, init?: RequestInit): Promise<T> {
  const url = `${API_BASE_PATH}${endpoint}`;

  const isDev = typeof window !== 'undefined' && window.location.hostname === 'localhost';
  if (isDev) {
    console.debug(`[api] ${init?.method ?? 'GET'} ${url}`);
  }

  let response: Response;
  try {
    response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...(init?.headers ?? {}),
      },
      ...init,
    });
  } catch {
    throw {
      endpoint,
      message: 'Unable to reach API server',
    } satisfies ApiErrorDetails;
  }

  if (!response.ok) {
    throw await toApiError(response, endpoint);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

async function toApiError(response: Response, endpoint: string): Promise<ApiErrorDetails> {
  const contentType = response.headers.get('content-type') ?? '';
  let message = `Request failed (${response.status})`;

  if (contentType.includes('application/json')) {
    try {
      const payload = (await response.json()) as { message?: string; error?: string };
      message = payload.message ?? payload.error ?? message;
    } catch {
      // keep default
    }
  } else {
    try {
      const text = await response.text();
      if (text.trim()) {
        message = text.slice(0, 180);
      }
    } catch {
      // keep default
    }
  }

  return {
    endpoint,
    status: response.status,
    message,
  };
}

function toBody(payload: unknown): string {
  return JSON.stringify(payload);
}

export async function fetchInvoices(): Promise<SupplierInvoice[]> {
  return request<SupplierInvoice[]>('/supplier-invoices');
}

export async function fetchInvoiceById(id: string): Promise<SupplierInvoice> {
  return request<SupplierInvoice>(`/supplier-invoices/${id}`);
}

export async function createDraft(payload: CreateDraftRequest): Promise<SupplierInvoice> {
  return request<SupplierInvoice>('/supplier-invoices', { method: 'POST', body: toBody(payload) });
}

export async function updateInvoice(id: string, payload: UpdateInvoiceRequest): Promise<SupplierInvoice> {
  return request<SupplierInvoice>(`/supplier-invoices/${id}`, { method: 'PUT', body: toBody(payload) });
}

export async function transitionInvoice(id: string, action: 'submit' | 'approve' | 'post'): Promise<void> {
  return request<void>(`/supplier-invoices/${id}/${action}`, { method: 'PUT' });
}

export async function fetchDimensionTypes(): Promise<DimensionType[]> {
  return request<DimensionType[]>('/dimension-types');
}

export async function fetchDimensionTree(typeCode: string, includeInactive = false): Promise<DimensionNode[]> {
  return request<DimensionNode[]>(`/dimensions/${typeCode}/tree?includeInactive=${includeInactive}`);
}

export async function searchDimensionNodes(typeCode: string, query: string, includeInactive = false): Promise<DimensionNode[]> {
  return request<DimensionNode[]>(`/dimensions/${typeCode}/search?q=${encodeURIComponent(query)}&includeInactive=${includeInactive}`);
}

export async function createDimensionNode(typeCode: string, payload: Partial<DimensionNode>): Promise<DimensionNode> {
  return request<DimensionNode>(`/dimensions/${typeCode}/nodes`, { method: 'POST', body: toBody(payload) });
}

export async function updateDimensionNode(typeCode: string, nodeId: string, payload: Partial<DimensionNode>): Promise<DimensionNode> {
  return request<DimensionNode>(`/dimensions/${typeCode}/nodes/${nodeId}`, { method: 'PUT', body: toBody(payload) });
}

export async function deleteDimensionNode(typeCode: string, nodeId: string): Promise<void> {
  return request<void>(`/dimensions/${typeCode}/nodes/${nodeId}`, { method: 'DELETE' });
}

export async function setDimensionNodeStatus(typeCode: string, nodeId: string, status: 'Active' | 'Inactive'): Promise<DimensionNode> {
  return request<DimensionNode>(`/dimensions/${typeCode}/nodes/${nodeId}/status`, { method: 'PATCH', body: toBody({ status }) });
}

export async function moveDimensionNode(typeCode: string, nodeId: string, newParentId?: string): Promise<DimensionNode[]> {
  return request<DimensionNode[]>(`/dimensions/${typeCode}/move`, { method: 'POST', body: toBody({ nodeId, newParentId }) });
}

export async function reorderDimensionNodes(typeCode: string, parentId: string | undefined, nodeIds: string[]): Promise<DimensionNode[]> {
  return request<DimensionNode[]>(`/dimensions/${typeCode}/reorder`, { method: 'POST', body: toBody({ parentId, nodeIds }) });
}

export async function fetchMappings(path: string): Promise<DimensionMapping[]> {
  return request<DimensionMapping[]>(`/dimensions/mappings/${path}`);
}

export async function upsertMapping(path: string, payload: unknown): Promise<DimensionMapping> {
  return request<DimensionMapping>(`/dimensions/mappings/${path}`, { method: 'POST', body: toBody(payload) });
}

export async function deleteMapping(path: string, params: Record<string, string>): Promise<void> {
  const query = new URLSearchParams(params).toString();
  return request<void>(`/dimensions/mappings/${path}?${query}`, { method: 'DELETE' });
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
  const value = error as ApiErrorDetails | undefined;
  if (value?.status) {
    return `[${value.status}] ${value.message}`;
  }
  if (value?.message) {
    return value.message;
  }

  return 'Unexpected error';
}

export function getErrorDetails(error: unknown): ApiErrorDetails {
  const value = error as ApiErrorDetails | undefined;
  return {
    endpoint: value?.endpoint,
    status: value?.status,
    message: value?.message ?? getErrorMessage(error),
  };
}
