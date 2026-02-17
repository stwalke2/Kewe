import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  createDimensionNode,
  fetchDimensionTree,
  fetchDimensionTypes,
  getErrorMessage,
  setDimensionNodeStatus,
  updateDimensionNode,
} from '../api';
import type { DimensionNode, DimensionType } from '../api/types';
import { StatusPill } from '../components/StatusPill';

type DraftNode = {
  typeCode: string;
  code: string;
  name: string;
  description?: string;
  parentId?: string;
  attributes: Record<string, string>;
};

function emptyDraft(): DraftNode {
  return { typeCode: '', code: '', name: '', description: '', parentId: undefined, attributes: {} };
}

function cloneDraft(node: DraftNode): DraftNode {
  return JSON.parse(JSON.stringify(node)) as DraftNode;
}

function nodeToDraft(node: DimensionNode): DraftNode {
  const attributes = Object.entries(node.attributes ?? {}).reduce<Record<string, string>>((acc, [key, value]) => {
    acc[key] = typeof value === 'string' ? value : JSON.stringify(value);
    return acc;
  }, {});

  return {
    typeCode: node.typeCode,
    code: node.code,
    name: node.name,
    description: node.description ?? '',
    parentId: node.parentId,
    attributes,
  };
}

export function AccountingDimensionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const isCreateMode = id === 'new';
  const navigate = useNavigate();

  const [types, setTypes] = useState<DimensionType[]>([]);
  const [nodesByType, setNodesByType] = useState<Record<string, DimensionNode[]>>({});
  const [dimension, setDimension] = useState<DimensionNode | null>(null);
  const [draft, setDraft] = useState<DraftNode>(emptyDraft());
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(isCreateMode);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const [hierarchySearch, setHierarchySearch] = useState('');

  useEffect(() => {
    void loadPageData();
  }, [id]);

  useEffect(() => {
    if (!draft.typeCode) {
      setHierarchySearch('');
      return;
    }

    if (!draft.parentId) {
      setHierarchySearch('');
      return;
    }

    const parentName = availableHierarchyNodes.find((node) => node.id === draft.parentId)?.name ?? '';
    setHierarchySearch(parentName);
  }, [draft.parentId, draft.typeCode]);

  const availableHierarchyNodes = useMemo(() => {
    if (!draft.typeCode) {
      return [];
    }

    return (nodesByType[draft.typeCode] ?? []).filter((node) => node.id !== id);
  }, [draft.typeCode, id, nodesByType]);

  const attributeKeys = useMemo(() => {
    if (!draft.typeCode) {
      return [] as string[];
    }

    const fromType = (nodesByType[draft.typeCode] ?? []).flatMap((node) => Object.keys(node.attributes ?? {}));
    const fromDraft = Object.keys(draft.attributes);
    return Array.from(new Set([...fromType, ...fromDraft])).sort((a, b) => a.localeCompare(b));
  }, [draft.attributes, draft.typeCode, nodesByType]);

  const hasUnsavedChanges = useMemo(() => {
    if (isCreateMode) {
      return JSON.stringify(draft) !== JSON.stringify(emptyDraft());
    }

    if (!dimension) {
      return false;
    }

    return JSON.stringify(nodeToDraft(dimension)) !== JSON.stringify(draft);
  }, [dimension, draft, isCreateMode]);

  const canEdit = isCreateMode || dimension?.status === 'Active';

  async function loadPageData() {
    setLoading(true);
    setError(null);
    setMessage(null);

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
        }
        setDimension(selected);
        setDraft(selected ? nodeToDraft(selected) : emptyDraft());
        setEditMode(false);
      }
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }

  const handleSelectHierarchy = (nameOrEmpty: string) => {
    setHierarchySearch(nameOrEmpty);
    if (!nameOrEmpty.trim()) {
      setDraft((prev) => ({ ...prev, parentId: undefined }));
      return;
    }

    const matched = availableHierarchyNodes.find((node) => node.name.toLowerCase() === nameOrEmpty.trim().toLowerCase());
    setDraft((prev) => ({ ...prev, parentId: matched?.id }));
  };

  const handleSave = async () => {
    if (!draft.typeCode || !draft.code.trim() || !draft.name.trim()) {
      setMessage({ kind: 'error', text: 'Type, code, and name are required.' });
      return;
    }

    const attributes = Object.entries(draft.attributes).reduce<Record<string, unknown>>((acc, [key, value]) => {
      if (!value.trim()) {
        return acc;
      }
      acc[key] = value;
      return acc;
    }, {});

    setBusy(true);
    setMessage(null);

    try {
      const payload = {
        code: draft.code,
        name: draft.name,
        description: draft.description || undefined,
        parentId: draft.parentId,
        attributes,
      };

      if (isCreateMode) {
        const created = await createDimensionNode(draft.typeCode, payload);
        navigate(`/accounting-dimensions/${created.id}`);
        return;
      }

      if (!id) {
        return;
      }

      const saved = await updateDimensionNode(draft.typeCode, id, payload);
      setDimension(saved);
      setDraft(nodeToDraft(saved));
      setEditMode(false);
      setMessage({ kind: 'success', text: 'Dimension updated.' });
    } catch (e) {
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  const handleCancel = () => {
    if (isCreateMode) {
      setDraft(emptyDraft());
      return;
    }

    if (dimension) {
      setDraft(cloneDraft(nodeToDraft(dimension)));
    }
    setEditMode(false);
  };

  const handleToggleStatus = async () => {
    if (!dimension) {
      return;
    }

    setBusy(true);
    setMessage(null);
    try {
      const nextStatus = dimension.status === 'Active' ? 'Inactive' : 'Active';
      const updated = await setDimensionNodeStatus(dimension.typeCode, dimension.id, nextStatus);
      setDimension(updated);
      setDraft(nodeToDraft(updated));
      setMessage({ kind: 'success', text: `Dimension set to ${nextStatus}.` });
    } catch (e) {
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  const detailTitle = isCreateMode ? 'Create dimension' : 'Dimension detail';

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <Link to="/accounting-dimensions" className="back-link">← Back to list</Link>
          <h2>{detailTitle}</h2>
          <p>Manage dimension attributes and hierarchy placement.</p>
        </div>
      </div>

      {loading && <p>Loading dimension details…</p>}
      {error && <p className="message error">{error}</p>}

      {!loading && !error && (
        <div className="detail-grid align-detail">
          <div className="detail-main-column">
            <div className="card section-card">
              <div className="row">
                <h3>Attributes</h3>
                {!isCreateMode && dimension && <StatusPill status={dimension.status} />}
              </div>

              <div className="form-grid">
                <label>
                  Dimension Type
                  <select
                    value={draft.typeCode}
                    disabled={!isCreateMode || busy}
                    onChange={(event) => setDraft({ ...draft, typeCode: event.target.value, parentId: undefined, attributes: {} })}
                  >
                    <option value="">Select type</option>
                    {types.map((type) => <option key={type.code} value={type.code}>{type.name}</option>)}
                  </select>
                </label>

                <label>
                  Code
                  <input
                    value={draft.code}
                    disabled={busy || !editMode}
                    onChange={(event) => setDraft({ ...draft, code: event.target.value })}
                  />
                </label>

                <label>
                  Name
                  <input
                    value={draft.name}
                    disabled={busy || !editMode}
                    onChange={(event) => setDraft({ ...draft, name: event.target.value })}
                  />
                </label>

                <label>
                  Description
                  <input
                    value={draft.description ?? ''}
                    disabled={busy || !editMode}
                    onChange={(event) => setDraft({ ...draft, description: event.target.value })}
                  />
                </label>

                <label>
                  Hierarchy
                  <>
                    <input
                      list="hierarchy-options"
                      placeholder="Top level"
                      value={hierarchySearch}
                      disabled={busy || !editMode || !draft.typeCode}
                      onChange={(event) => handleSelectHierarchy(event.target.value)}
                    />
                    <datalist id="hierarchy-options">
                      {availableHierarchyNodes.map((node) => (
                        <option key={node.id} value={node.name} />
                      ))}
                    </datalist>
                  </>
                </label>
              </div>

              {draft.typeCode && (
                <>
                  <h4>Type attributes</h4>
                  <div className="form-grid">
                    {attributeKeys.length === 0 && <p className="subtle full-width">No attributes available for this type yet.</p>}
                    {attributeKeys.map((key) => (
                      <label key={key}>
                        {key}
                        <input
                          value={draft.attributes[key] ?? ''}
                          disabled={busy || !editMode}
                          onChange={(event) => setDraft((prev) => ({
                            ...prev,
                            attributes: {
                              ...prev.attributes,
                              [key]: event.target.value,
                            },
                          }))}
                        />
                      </label>
                    ))}
                  </div>
                </>
              )}

              {(isCreateMode || editMode) && (
                <div className="actions-row">
                  <button className="btn btn-primary" disabled={busy} onClick={() => void handleSave()}>
                    {busy ? 'Saving…' : 'Save'}
                  </button>
                  <button className="btn btn-secondary" disabled={busy || !hasUnsavedChanges} onClick={handleCancel}>
                    Cancel
                  </button>
                  {hasUnsavedChanges && <span className="dirty-indicator">Unsaved changes</span>}
                </div>
              )}
            </div>
          </div>

          <aside className="card detail-side-panel">
            <h3>Details</h3>
            <dl className="kv-list">
              <div><dt>ID</dt><dd>{dimension?.id ?? 'New'}</dd></div>
              <div><dt>Type</dt><dd>{draft.typeCode || '—'}</dd></div>
              <div><dt>Code</dt><dd>{draft.code || '—'}</dd></div>
              <div><dt>Status</dt><dd>{dimension ? <StatusPill status={dimension.status} /> : 'Draft'}</dd></div>
              <div><dt>Hierarchy</dt><dd>{draft.parentId ? availableHierarchyNodes.find((node) => node.id === draft.parentId)?.name ?? '—' : 'Top level'}</dd></div>
            </dl>

            {!isCreateMode && (
              <div className="actions-row">
                <button className="btn btn-secondary full-width" onClick={() => setEditMode((value) => !value)} disabled={busy || !canEdit}>
                  {editMode ? 'Stop editing' : 'Edit'}
                </button>
                <button className="btn btn-secondary full-width" onClick={() => void handleToggleStatus()} disabled={busy}>
                  {dimension?.status === 'Active' ? 'Deactivate' : 'Activate'}
                </button>
              </div>
            )}

            {message && <p className={`message ${message.kind}`}>{message.text}</p>}
          </aside>
        </div>
      )}
    </section>
  );
}
