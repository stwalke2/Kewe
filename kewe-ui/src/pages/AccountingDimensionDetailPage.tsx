import { useEffect, useMemo, useState } from 'react';
import { Link, useMatch, useNavigate, useParams } from 'react-router-dom';
import {
  createDimensionNode,
  deleteDimensionNode,
  fetchDimensionTree,
  fetchDimensionTypes,
  getErrorDetails,
  getErrorMessage,
  setDimensionNodeStatus,
  updateDimensionNode,
} from '../api';
import type { ApiErrorDetails, DimensionNode, DimensionType } from '../api/types';
import { StatusPill } from '../components/StatusPill';

type TabKey = 'basic' | 'roles' | 'accounting';

type DraftNode = {
  typeCode: string;
  code: string;
  name: string;
  description?: string;
  parentId?: string;
  rolesCsv: string;
  budgetRequired: boolean;
  allowExpense: boolean;
  accountingOverride: boolean;
};

function emptyDraft(): DraftNode {
  return { typeCode: '', code: '', name: '', description: '', parentId: undefined, rolesCsv: '', budgetRequired: false, allowExpense: true, accountingOverride: false };
}

function nodeToDraft(node: DimensionNode): DraftNode {
  const roles = Array.isArray(node.attributes?.roles) ? node.attributes.roles.join(', ') : '';
  return {
    typeCode: node.typeCode,
    code: node.code,
    name: node.name,
    description: node.description ?? '',
    parentId: node.parentId,
    rolesCsv: roles,
    budgetRequired: Boolean(node.attributes?.budgetRequired),
    allowExpense: node.attributes?.allowExpense !== false,
    accountingOverride: Boolean(node.attributes?.accountingOverride),
  };
}

export function AccountingDimensionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const isNewRoute = useMatch('/business-objects/new') || useMatch('/accounting-dimensions/new');
  const isCreateMode = id === 'new' || Boolean(isNewRoute);
  const navigate = useNavigate();

  const [types, setTypes] = useState<DimensionType[]>([]);
  const [nodesByType, setNodesByType] = useState<Record<string, DimensionNode[]>>({});
  const [dimension, setDimension] = useState<DimensionNode | null>(null);
  const [draft, setDraft] = useState<DraftNode>(emptyDraft());
  const [activeTab, setActiveTab] = useState<TabKey>('basic');
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(isCreateMode);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [errorDetails, setErrorDetails] = useState<ApiErrorDetails | null>(null);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const isEditing = isCreateMode || editMode;

  useEffect(() => { void loadPageData(); }, [id, isCreateMode]);

  const availableHierarchyNodes = useMemo(() => !draft.typeCode ? [] : (nodesByType[draft.typeCode] ?? []).filter((node) => node.id !== id), [draft.typeCode, id, nodesByType]);
  const isUsedInHierarchy = useMemo(() => !dimension ? false : (nodesByType[dimension.typeCode] ?? []).some((node) => node.parentId === dimension.id), [dimension, nodesByType]);

  async function loadPageData() {
    setLoading(true); setError(null); setMessage(null); setErrorDetails(null);
    try {
      const loadedTypes = await fetchDimensionTypes();
      setTypes(loadedTypes);
      const treeEntries = await Promise.all(loadedTypes.map(async (type) => [type.code, await fetchDimensionTree(type.code, true)] as const));
      setNodesByType(Object.fromEntries(treeEntries));
      if (isCreateMode) {
        setDimension(null); setDraft(emptyDraft()); setEditMode(true);
      } else if (id) {
        const selected = treeEntries.flatMap((entry) => entry[1]).find((node) => node.id === id) ?? null;
        if (!selected) { setError(`Business object ${id} was not found.`); return; }
        setDimension(selected); setDraft(nodeToDraft(selected)); setEditMode(false);
      }
    } catch (e) { setError(getErrorMessage(e)); } finally { setLoading(false); }
  }

  const handleSave = async () => {
    if (!draft.typeCode || !draft.code.trim() || !draft.name.trim()) {
      setMessage({ kind: 'error', text: 'Type, Code, and Name are required.' }); return;
    }
    setBusy(true); setMessage(null); setErrorDetails(null);
    try {
      const payload = {
        code: draft.code,
        name: draft.name,
        description: draft.description || undefined,
        parentId: draft.parentId,
        attributes: {
          roles: draft.rolesCsv.split(',').map((value) => value.trim()).filter(Boolean),
          budgetRequired: draft.budgetRequired,
          allowExpense: draft.allowExpense,
          accountingOverride: draft.accountingOverride,
        },
      };
      if (isCreateMode) {
        const created = await createDimensionNode(draft.typeCode, payload);
        navigate(`/business-objects/${created.id}`); return;
      }
      if (!id || !dimension) return;
      const saved = await updateDimensionNode(dimension.typeCode, id, payload);
      setDimension(saved); setDraft(nodeToDraft(saved)); setEditMode(false); setMessage({ kind: 'success', text: 'Business object updated.' });
      await loadPageData();
    } catch (e) { setErrorDetails(getErrorDetails(e)); setMessage({ kind: 'error', text: getErrorMessage(e) }); } finally { setBusy(false); }
  };

  return (
    <section className="page-section">
      <div className="page-header-row"><div><Link to="/business-objects" className="back-link">← Back to list</Link><h2>Business Object Setup</h2><p>Basic setup, roles, and accounting/budget controls.</p></div></div>
      {loading && <p>Loading business object details…</p>}
      {error && <p className="message error">{error}</p>}
      {!loading && !error && <div className="card section-card">
        <div className="segmented-control" role="tablist" aria-label="Business object tabs">
          <button className={activeTab === 'basic' ? 'segment active' : 'segment'} onClick={() => setActiveTab('basic')}>Basic Setup</button>
          <button className={activeTab === 'roles' ? 'segment active' : 'segment'} onClick={() => setActiveTab('roles')}>Roles</button>
          <button className={activeTab === 'accounting' ? 'segment active' : 'segment'} onClick={() => setActiveTab('accounting')}>Accounting/Budget</button>
        </div>

        {activeTab === 'basic' && <div className="form-grid">
          <label>Type<select value={draft.typeCode} disabled={busy || !isCreateMode} onChange={(event) => setDraft({ ...draft, typeCode: event.target.value, parentId: undefined })}><option value="">Select type</option>{types.map((type) => <option key={type.code} value={type.code}>{type.name}</option>)}</select></label>
          {!isCreateMode && <label>Status<div className="status-inline"><StatusPill status={dimension?.status ?? 'Draft'} /></div></label>}
          <label>Code<input value={draft.code} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, code: event.target.value })} /></label>
          <label>Name<input value={draft.name} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, name: event.target.value })} /></label>
          <label>Description<input value={draft.description ?? ''} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, description: event.target.value })} /></label>
          <label>Hierarchy<select value={draft.parentId ?? ''} disabled={busy || !isEditing || !draft.typeCode} onChange={(event) => setDraft({ ...draft, parentId: event.target.value || undefined })}><option value="">Top level</option>{availableHierarchyNodes.map((node) => <option key={node.id} value={node.id}>{node.code} {node.name}</option>)}</select></label>
        </div>}

        {activeTab === 'roles' && <div className="form-grid"><label>Assigned Roles (comma separated)
          <input value={draft.rolesCsv} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, rolesCsv: event.target.value })} placeholder="Manager, Financial Analyst" />
        </label></div>}

        {activeTab === 'accounting' && <div className="form-grid">
          <label className="inline-check"><input type="checkbox" checked={draft.budgetRequired} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, budgetRequired: event.target.checked })} />Budget Required</label>
          <label className="inline-check"><input type="checkbox" checked={draft.allowExpense} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, allowExpense: event.target.checked })} />Allow Expense Posting</label>
          <label className="inline-check"><input type="checkbox" checked={draft.accountingOverride} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, accountingOverride: event.target.checked })} />Override Defaults <strong>{draft.accountingOverride ? '(Override Active)' : '(Using Type Defaults)'}</strong></label>
        </div>}

        <div className="actions-row">
          {(isCreateMode || editMode) && <><button className="btn btn-primary" disabled={busy} onClick={() => void handleSave()}>{busy ? 'Saving…' : (isCreateMode ? 'Create' : 'Save')}</button><button className="btn btn-secondary" disabled={busy} onClick={() => navigate('/business-objects')}>Cancel</button></>}
          {!isCreateMode && !editMode && <><button className="btn btn-primary" disabled={busy} onClick={() => setEditMode(true)}>Edit</button><button className="btn btn-secondary" disabled={busy} onClick={() => navigate('/business-objects')}>Back</button><button className="btn btn-secondary" onClick={async () => { if (!dimension) return; const nextStatus = dimension.status === 'Active' ? 'Inactive' : 'Active'; const updated = await setDimensionNodeStatus(dimension.typeCode, dimension.id, nextStatus); setDimension(updated); setDraft(nodeToDraft(updated)); }} disabled={busy}>{dimension?.status === 'Active' ? 'Deactivate' : 'Activate'}</button><button className="btn btn-danger" onClick={async () => { if (!dimension) return; await deleteDimensionNode(dimension.typeCode, dimension.id); navigate('/business-objects'); }} disabled={busy || isUsedInHierarchy}>Delete</button></>}
        </div>
        {errorDetails && <div className="message error"><strong>Request error</strong><div>Endpoint: {errorDetails.endpoint ?? '—'}</div><div>Status: {errorDetails.status ?? '—'}</div><div>Message: {errorDetails.message}</div></div>}
        {message && <p className={`message ${message.kind}`}>{message.text}</p>}
      </div>}
    </section>
  );
}
