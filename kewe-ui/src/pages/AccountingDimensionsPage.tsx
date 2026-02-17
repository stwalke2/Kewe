import { useEffect, useMemo, useState } from 'react';
import {
  createDimensionNode,
  deleteMapping,
  fetchDimensionTree,
  fetchDimensionTypes,
  fetchMappings,
  getErrorDetails,
  moveDimensionNode,
  reorderDimensionNodes,
  searchDimensionNodes,
  setDimensionNodeStatus,
  upsertMapping,
  updateDimensionNode,
} from '../api';
import type { ApiErrorDetails, DimensionMapping, DimensionNode, DimensionType } from '../api/types';

type MappingPath = 'item-to-ledger' | 'costcenter-to-org' | 'awarddriver-to-fund' | 'default-function';

type NodeForm = {
  id: string;
  code: string;
  name: string;
  parentId: string;
  attributesText: string;
};

const emptyNodeForm: NodeForm = { id: '', code: '', name: '', parentId: '', attributesText: '{}' };

export function AccountingDimensionsPage() {
  const [types, setTypes] = useState<DimensionType[]>([]);
  const [selectedType, setSelectedType] = useState('LEDGER_ACCOUNT');
  const [nodes, setNodes] = useState<DimensionNode[]>([]);
  const [query, setQuery] = useState('');
  const [includeInactive, setIncludeInactive] = useState(false);
  const [activeTab, setActiveTab] = useState<'tree' | 'mappings'>('tree');
  const [mappingPath, setMappingPath] = useState<MappingPath>('item-to-ledger');
  const [mappings, setMappings] = useState<Record<string, DimensionMapping[]>>({});
  const [nodeForm, setNodeForm] = useState<NodeForm>(emptyNodeForm);
  const [mappingForm, setMappingForm] = useState<Record<string, string>>({ itemTypeCode: 'SPEND_ITEM' });
  const [error, setError] = useState<ApiErrorDetails | null>(null);
  const [isSavingNode, setIsSavingNode] = useState(false);

  const mappingOptions = [
    { key: 'item-to-ledger', label: 'Item → Ledger Account' },
    { key: 'costcenter-to-org', label: 'Cost Center → Organization' },
    { key: 'awarddriver-to-fund', label: 'Award Driver → Fund' },
    { key: 'default-function', label: 'Default Function' },
  ] as const;

  useEffect(() => {
    void loadTypes();
  }, []);

  useEffect(() => {
    if (selectedType) {
      void loadNodes();
    }
  }, [selectedType, includeInactive]);

  useEffect(() => {
    if (activeTab === 'mappings') {
      void loadMappings();
    }
  }, [activeTab]);

  const nodeNameById = useMemo(() => {
    const map = new Map<string, string>();
    nodes.forEach((node) => map.set(node.id, node.name));
    Object.values(mappings).forEach((group) => group.forEach((row) => map.set(row.targetNodeId, map.get(row.targetNodeId) ?? row.targetNodeId)));
    return map;
  }, [nodes, mappings]);

  const mappingRows = useMemo(() => mappings[mappingPath] ?? [], [mappings, mappingPath]);

  async function loadTypes() {
    try {
      setError(null);
      const data = await fetchDimensionTypes();
      setTypes(data);
      if (data.length > 0 && !data.some((type) => type.code === selectedType)) {
        setSelectedType(data[0].code);
      }
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  async function loadNodes() {
    try {
      setError(null);
      const data = query.trim()
        ? await searchDimensionNodes(selectedType, query, includeInactive)
        : await fetchDimensionTree(selectedType, includeInactive);
      setNodes(data);
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  async function loadMappings() {
    try {
      setError(null);
      const [item, cc, award, func] = await Promise.all([
        fetchMappings('item-to-ledger'),
        fetchMappings('costcenter-to-org'),
        fetchMappings('awarddriver-to-fund'),
        fetchMappings('default-function'),
      ]);
      setMappings({ 'item-to-ledger': item, 'costcenter-to-org': cc, 'awarddriver-to-fund': award, 'default-function': func });
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  function selectNode(node: DimensionNode) {
    setNodeForm({
      id: node.id,
      code: node.code,
      name: node.name,
      parentId: node.parentId ?? '',
      attributesText: JSON.stringify(node.attributes ?? {}, null, 2),
    });
  }

  async function saveNode() {
    try {
      setError(null);
      setIsSavingNode(true);
      const attributes = JSON.parse(nodeForm.attributesText || '{}') as Record<string, unknown>;
      const payload = {
        code: nodeForm.code,
        name: nodeForm.name,
        parentId: nodeForm.parentId || undefined,
        attributes,
      };

      if (nodeForm.id) {
        await updateDimensionNode(selectedType, nodeForm.id, payload);
      } else {
        await createDimensionNode(selectedType, payload);
      }

      setNodeForm(emptyNodeForm);
      await loadNodes();
    } catch (e) {
      setError(getErrorDetails(e));
    } finally {
      setIsSavingNode(false);
    }
  }

  async function moveWithinHierarchy(node: DimensionNode, direction: 'up' | 'down') {
    const siblings = nodes
      .filter((candidate) => (candidate.parentId ?? '') === (node.parentId ?? ''))
      .sort((a, b) => a.sortOrder - b.sortOrder);
    const index = siblings.findIndex((candidate) => candidate.id === node.id);
    const swapIndex = direction === 'up' ? index - 1 : index + 1;

    if (index < 0 || swapIndex < 0 || swapIndex >= siblings.length) {
      return;
    }

    const reordered = [...siblings];
    [reordered[index], reordered[swapIndex]] = [reordered[swapIndex], reordered[index]];

    try {
      setError(null);
      await reorderDimensionNodes(selectedType, node.parentId, reordered.map((value) => value.id));
      await loadNodes();
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  async function saveMapping() {
    try {
      setError(null);
      if (mappingPath === 'item-to-ledger') {
        const sourceIds = (mappingForm.itemNodeId ?? '')
          .split(',')
          .map((value) => value.trim())
          .filter(Boolean);

        for (const sourceId of sourceIds) {
          await upsertMapping(mappingPath, {
            itemTypeCode: mappingForm.itemTypeCode,
            itemNodeId: sourceId,
            ledgerAccountNodeId: mappingForm.ledgerAccountNodeId,
          });
        }
      } else if (mappingPath === 'costcenter-to-org') {
        await upsertMapping(mappingPath, {
          costCenterNodeId: mappingForm.costCenterNodeId,
          organizationNodeId: mappingForm.organizationNodeId,
        });
      } else if (mappingPath === 'awarddriver-to-fund') {
        await upsertMapping(mappingPath, {
          driverTypeCode: mappingForm.driverTypeCode,
          driverNodeId: mappingForm.driverNodeId,
          fundNodeId: mappingForm.fundNodeId,
        });
      } else {
        await upsertMapping(mappingPath, {
          sourceTypeCode: mappingForm.sourceTypeCode,
          sourceNodeId: mappingForm.sourceNodeId,
          functionNodeId: mappingForm.functionNodeId,
        });
      }

      setMappingForm({ itemTypeCode: 'SPEND_ITEM' });
      await loadMappings();
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  async function deleteMappingRow(row: DimensionMapping) {
    try {
      setError(null);
      if (mappingPath === 'item-to-ledger') {
        await deleteMapping(mappingPath, { itemTypeCode: row.sourceTypeCode, itemNodeId: row.sourceNodeId ?? '' });
      } else if (mappingPath === 'costcenter-to-org') {
        await deleteMapping(mappingPath, { costCenterNodeId: row.sourceNodeId ?? '' });
      } else if (mappingPath === 'awarddriver-to-fund') {
        await deleteMapping(mappingPath, { driverTypeCode: row.sourceTypeCode, driverNodeId: row.sourceNodeId ?? '' });
      } else {
        await deleteMapping(mappingPath, { sourceTypeCode: row.sourceTypeCode, sourceNodeId: row.sourceNodeId ?? '' });
      }
      await loadMappings();
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Accounting Dimensions</h2>
          <p>Dimension setup, hierarchy management, and mapping maintenance.</p>
        </div>
      </div>

      <div className="card actions-row tab-row">
        <button className={activeTab === 'tree' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setActiveTab('tree')}>Hierarchy</button>
        <button className={activeTab === 'mappings' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setActiveTab('mappings')}>Mappings</button>
      </div>

      {error && (
        <div className="message error">
          <strong>Request error</strong>
          <div>Status: {error.status ?? 'N/A'}</div>
          <div>Endpoint: {error.endpoint ?? 'N/A'}</div>
          <div>Message: {error.message}</div>
        </div>
      )}

      {activeTab === 'tree' && (
        <div className="card">
          <div className="table-tools">
            <select value={selectedType} onChange={(e) => setSelectedType(e.target.value)}>
              {types.map((t) => (
                <option key={t.code} value={t.code}>{t.name}</option>
              ))}
            </select>
            <input className="search" value={query} placeholder="Search" onChange={(e) => setQuery(e.target.value)} />
            <label><input type="checkbox" checked={includeInactive} onChange={(e) => setIncludeInactive(e.target.checked)} /> Show inactive</label>
            <button className="btn btn-secondary" onClick={() => void loadNodes()}>Refresh</button>
          </div>

          <div className="detail-grid">
            <div>
              <h3>Hierarchy View</h3>
              <table className="clickable-rows">
                <thead><tr><th>Code</th><th>Name</th><th>Status</th><th>Parent</th><th>Hierarchy</th></tr></thead>
                <tbody>
                  {nodes.map((node) => (
                    <tr key={node.id} onClick={() => selectNode(node)}>
                      <td>{node.code}</td>
                      <td>{'· '.repeat(node.depth)}{node.name}</td>
                      <td>{node.status}</td>
                      <td>{node.parentId ? nodeNameById.get(node.parentId) ?? node.parentId : 'Top Level'}</td>
                      <td>
                        <button className="btn btn-secondary" onClick={(event) => { event.stopPropagation(); void moveWithinHierarchy(node, 'up'); }}>↑</button>{' '}
                        <button className="btn btn-secondary" onClick={(event) => { event.stopPropagation(); void moveWithinHierarchy(node, 'down'); }}>↓</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div>
              <h3>Dimension Setup</h3>
              <p className="subtle">Select a row to edit attributes. Actions are managed here.</p>
              <div className="form-grid">
                <label>Code<input value={nodeForm.code} onChange={(e) => setNodeForm({ ...nodeForm, code: e.target.value })} /></label>
                <label>Name<input value={nodeForm.name} onChange={(e) => setNodeForm({ ...nodeForm, name: e.target.value })} /></label>
                <label>Parent Node ID<input value={nodeForm.parentId} onChange={(e) => setNodeForm({ ...nodeForm, parentId: e.target.value })} /></label>
                <label className="full-width">Attributes (JSON)
                  <textarea value={nodeForm.attributesText} rows={6} onChange={(e) => setNodeForm({ ...nodeForm, attributesText: e.target.value })} />
                </label>
              </div>
              <div className="actions-row">
                <button className="btn btn-primary" onClick={() => void saveNode()} disabled={isSavingNode}>{nodeForm.id ? 'Update Dimension' : 'Add Dimension'}</button>
                <button className="btn btn-secondary" onClick={() => setNodeForm(emptyNodeForm)}>Clear</button>
                {!!nodeForm.id && (
                  <>
                    <button className="btn btn-secondary" onClick={() => void moveDimensionNode(selectedType, nodeForm.id, undefined).then(loadNodes)}>Move to Top Hierarchy</button>
                    <button className="btn btn-secondary" onClick={() => {
                      const current = nodes.find((n) => n.id === nodeForm.id);
                      if (current) {
                        void setDimensionNodeStatus(selectedType, nodeForm.id, current.status === 'Active' ? 'Inactive' : 'Active').then(loadNodes);
                      }
                    }}>Toggle Active</button>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'mappings' && (
        <div className="card">
          <div className="table-tools">
            <select value={mappingPath} onChange={(e) => setMappingPath(e.target.value as MappingPath)}>
              {mappingOptions.map((option) => (
                <option key={option.key} value={option.key}>{option.label}</option>
              ))}
            </select>
            {mappingPath === 'item-to-ledger' && (
              <>
                <select value={mappingForm.itemTypeCode ?? 'SPEND_ITEM'} onChange={(e) => setMappingForm({ ...mappingForm, itemTypeCode: e.target.value })}><option>SPEND_ITEM</option><option>REVENUE_ITEM</option></select>
                <input placeholder="itemNodeId(s): comma-separated" value={mappingForm.itemNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, itemNodeId: e.target.value })} />
                <input placeholder="ledgerAccountNodeId" value={mappingForm.ledgerAccountNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, ledgerAccountNodeId: e.target.value })} />
              </>
            )}
            {mappingPath === 'costcenter-to-org' && (
              <>
                <input placeholder="costCenterNodeId" value={mappingForm.costCenterNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, costCenterNodeId: e.target.value })} />
                <input placeholder="organizationNodeId" value={mappingForm.organizationNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, organizationNodeId: e.target.value })} />
              </>
            )}
            {mappingPath === 'awarddriver-to-fund' && (
              <>
                <select value={mappingForm.driverTypeCode ?? 'GIFT'} onChange={(e) => setMappingForm({ ...mappingForm, driverTypeCode: e.target.value })}><option>GIFT</option><option>GRANT</option><option>PROJECT</option><option>APPROPRIATION</option><option>NONE</option></select>
                <input placeholder="driverNodeId (blank for NONE)" value={mappingForm.driverNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, driverNodeId: e.target.value })} />
                <input placeholder="fundNodeId" value={mappingForm.fundNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, fundNodeId: e.target.value })} />
              </>
            )}
            {mappingPath === 'default-function' && (
              <>
                <select value={mappingForm.sourceTypeCode ?? 'PROGRAM'} onChange={(e) => setMappingForm({ ...mappingForm, sourceTypeCode: e.target.value })}><option>PROGRAM</option><option>ORGANIZATION</option><option>LEDGER_ACCOUNT</option></select>
                <input placeholder="sourceNodeId" value={mappingForm.sourceNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, sourceNodeId: e.target.value })} />
                <input placeholder="functionNodeId" value={mappingForm.functionNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, functionNodeId: e.target.value })} />
              </>
            )}
            <button className="btn btn-primary" onClick={() => void saveMapping()}>Save</button>
          </div>

          <table>
            <thead><tr><th>Mapping</th><th>Source</th><th>Target</th><th /></tr></thead>
            <tbody>
              {mappingRows.map((row) => (
                <tr key={row.id}>
                  <td>{row.mappingType}</td>
                  <td>{row.sourceTypeCode}: {nodeNameById.get(row.sourceNodeId ?? '') ?? row.sourceNodeId ?? row.sourceKey}</td>
                  <td>{row.targetTypeCode}: {nodeNameById.get(row.targetNodeId) ?? row.targetNodeId}</td>
                  <td><button className="btn btn-secondary" onClick={() => void deleteMappingRow(row)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
