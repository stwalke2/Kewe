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

type DraftNode = {
  typeCode: string;
  code: string;
  name: string;
  description?: string;
  parentId?: string;
};

function emptyDraft(): DraftNode {
  return { typeCode: '', code: '', name: '', description: '', parentId: undefined };
}

function nodeToDraft(node: DimensionNode): DraftNode {
  return {
    typeCode: node.typeCode,
    code: node.code,
    name: node.name,
    description: node.description ?? '',
    parentId: node.parentId,
  };
}

export function AccountingDimensionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const isNewRoute = useMatch('/accounting-dimensions/new');
  const isCreateMode = id === 'new' || Boolean(isNewRoute);
  const navigate = useNavigate();

  const [types, setTypes] = useState<DimensionType[]>([]);
  const [nodesByType, setNodesByType] = useState<Record<string, DimensionNode[]>>({});
  const [dimension, setDimension] = useState<DimensionNode | null>(null);
  const [draft, setDraft] = useState<DraftNode>(emptyDraft());
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(isCreateMode);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [errorDetails, setErrorDetails] = useState<ApiErrorDetails | null>(null);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const isEditing = isCreateMode || editMode;

  useEffect(() => {
    void loadPageData();
  }, [id, isCreateMode]);

  const availableHierarchyNodes = useMemo(() => {
    if (!draft.typeCode) return [];
    return (nodesByType[draft.typeCode] ?? []).filter((node) => node.id !== id);
  }, [draft.typeCode, id, nodesByType]);

  const nodeLabelById = useMemo(() => {
    const map = new Map<string, string>();
    availableHierarchyNodes.forEach((node) => map.set(node.id, `${node.code} ${node.name}`));
    return map;
  }, [availableHierarchyNodes]);

  const isUsedInHierarchy = useMemo(() => {
    if (!dimension) return false;
    return (nodesByType[dimension.typeCode] ?? []).some((node) => node.parentId === dimension.id);
  }, [dimension, nodesByType]);

  async function loadPageData() {
    setLoading(true);
    setError(null);
    setMessage(null);
    setErrorDetails(null);

    try {
      const loadedTypes = await fetchDimensionTypes();
      setTypes(loadedTypes);
      const treeEntries = await Promise.all(loadedTypes.map(async (type) => [type.code, await fetchDimensionTree(type.code, true)] as const));
      const trees = Object.fromEntries(treeEntries);
      setNodesByType(trees);

      if (isCreateMode) {
        setDimension(null);
        setDraft(emptyDraft());
        setEditMode(true);
      } else if (id) {
        const allNodes = treeEntries.flatMap((entry) => entry[1]);
        const selected = allNodes.find((node) => node.id === id) ?? null;
        if (!selected) {
          setError(`Dimension ${id} was not found.`);
          return;
        }
        setDimension(selected);
        setDraft(nodeToDraft(selected));
        setEditMode(false);
      }
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }

  const handleSave = async () => {
    if (!draft.typeCode || !draft.code.trim() || !draft.name.trim()) {
      setMessage({ kind: 'error', text: 'Dimension Type, Code, and Name are required.' });
      return;
    }

    setBusy(true);
    setMessage(null);
    setErrorDetails(null);

    try {
      const payload = {
        code: draft.code,
        name: draft.name,
        description: draft.description || undefined,
        parentId: draft.parentId,
        attributes: dimension?.attributes ?? {},
      };

      if (isCreateMode) {
        const created = await createDimensionNode(draft.typeCode, payload);
        navigate(`/accounting-dimensions/${created.id}`);
        return;
      }

      if (!id || !dimension) return;
      const saved = await updateDimensionNode(dimension.typeCode, id, payload);
      setDimension(saved);
      setDraft(nodeToDraft(saved));
      setEditMode(false);
      setMessage({ kind: 'success', text: 'Dimension updated.' });
      await loadPageData();
    } catch (e) {
      setErrorDetails(getErrorDetails(e));
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  const handleCancel = () => {
    if (isCreateMode) {
      navigate('/accounting-dimensions');
      return;
    }
    if (dimension) setDraft(nodeToDraft(dimension));
    setEditMode(false);
  };

  const handleToggleStatus = async () => {
    if (!dimension) return;
    setBusy(true);
    setMessage(null);
    setErrorDetails(null);
    try {
      const nextStatus = dimension.status === 'Active' ? 'Inactive' : 'Active';
      const updated = await setDimensionNodeStatus(dimension.typeCode, dimension.id, nextStatus);
      setDimension(updated);
      setDraft(nodeToDraft(updated));
      setMessage({ kind: 'success', text: `Dimension set to ${nextStatus}.` });
    } catch (e) {
      setErrorDetails(getErrorDetails(e));
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  const handleDelete = async () => {
    if (!dimension) return;
    setBusy(true);
    setMessage(null);
    setErrorDetails(null);
    try {
      await deleteDimensionNode(dimension.typeCode, dimension.id);
      navigate('/accounting-dimensions');
    } catch (e) {
      setErrorDetails(getErrorDetails(e));
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <Link to="/accounting-dimensions" className="back-link">← Back to list</Link>
          <h2>{isCreateMode ? 'Dimension Setup' : 'Dimension Setup'}</h2>
          <p>Create and maintain accounting dimensions.</p>
        </div>
      </div>

      {loading && <p>Loading dimension details…</p>}
      {error && <p className="message error">{error}</p>}
      {!loading && !error && (
        <div className="card section-card">
          <div className="form-grid">
            <label>
              Dimension Type
              <select value={draft.typeCode} disabled={busy || !isCreateMode} onChange={(event) => setDraft({ ...draft, typeCode: event.target.value, parentId: undefined })}>
                <option value="">Select type</option>
                {types.map((type) => <option key={type.code} value={type.code}>{type.name}</option>)}
              </select>
            </label>
            {!isCreateMode && <label>ID<input value={dimension?.id ?? ''} disabled /></label>}
            {!isCreateMode && <label>Status<div className="status-inline"><StatusPill status={dimension?.status ?? 'Draft'} /></div></label>}
            <label>Code<input value={draft.code} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, code: event.target.value })} /></label>
            <label>Name<input value={draft.name} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, name: event.target.value })} /></label>
            <label>Description<input value={draft.description ?? ''} disabled={busy || !isEditing} onChange={(event) => setDraft({ ...draft, description: event.target.value })} /></label>
            <label>
              Hierarchy
              <select value={draft.parentId ?? ''} disabled={busy || !isEditing || !draft.typeCode} onChange={(event) => setDraft({ ...draft, parentId: event.target.value || undefined })}>
                <option value="">Top level</option>
                {availableHierarchyNodes.map((node) => <option key={node.id} value={node.id}>{node.code} {node.name}</option>)}
              </select>
              {!draft.typeCode && <small>Select a type to choose a hierarchy parent</small>}
            </label>
            {!isCreateMode && <label>Current Parent<input value={draft.parentId ? nodeLabelById.get(draft.parentId) ?? '—' : 'Top level'} disabled /></label>}
          </div>

          <div className="actions-row">
            {isCreateMode && <>
              <button className="btn btn-primary" disabled={busy} onClick={() => void handleSave()}>{busy ? 'Creating…' : 'Create'}</button>
              <button className="btn btn-secondary" disabled={busy} onClick={handleCancel}>Cancel</button>
            </>}
            {!isCreateMode && !editMode && <>
              <button className="btn btn-primary" disabled={busy} onClick={() => setEditMode(true)}>Edit</button>
              <button className="btn btn-secondary" disabled={busy} onClick={() => navigate('/accounting-dimensions')}>Back</button>
              <button className="btn btn-secondary" onClick={() => void handleToggleStatus()} disabled={busy}>{dimension?.status === 'Active' ? 'Deactivate' : 'Activate'}</button>
              <button className="btn btn-danger" onClick={() => void handleDelete()} disabled={busy || isUsedInHierarchy} title={isUsedInHierarchy ? 'Cannot delete because this dimension is used as a hierarchy parent.' : 'Delete this dimension'}>
                Delete
              </button>
            </>}
            {!isCreateMode && editMode && <>
              <button className="btn btn-primary" disabled={busy} onClick={() => void handleSave()}>{busy ? 'Saving…' : 'Save'}</button>
              <button className="btn btn-secondary" disabled={busy} onClick={handleCancel}>Cancel</button>
            </>}
          </div>
          {errorDetails && <div className="message error"><strong>Request error</strong><div>Endpoint: {errorDetails.endpoint ?? '—'}</div><div>Status: {errorDetails.status ?? '—'}</div><div>Message: {errorDetails.message}</div></div>}
          {message && <p className={`message ${message.kind}`}>{message.text}</p>}
        </div>
      )}
    </section>
  );
}
