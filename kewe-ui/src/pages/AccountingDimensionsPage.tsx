import { useEffect, useMemo, useState } from 'react';
import {
  createDimensionNode,
  deleteMapping,
  fetchDimensionTree,
  fetchDimensionTypes,
  fetchMappings,
  getErrorMessage,
  moveDimensionNode,
  reorderDimensionNodes,
  searchDimensionNodes,
  setDimensionNodeStatus,
  upsertMapping,
  updateDimensionNode,
} from '../api';
import type { DimensionMapping, DimensionNode, DimensionType } from '../api/types';

export function AccountingDimensionsPage() {
  const [types, setTypes] = useState<DimensionType[]>([]);
  const [selectedType, setSelectedType] = useState('LEDGER_ACCOUNT');
  const [nodes, setNodes] = useState<DimensionNode[]>([]);
  const [query, setQuery] = useState('');
  const [includeInactive, setIncludeInactive] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'tree' | 'mappings'>('tree');
  const [mappingPath, setMappingPath] = useState<'item-to-ledger' | 'costcenter-to-org' | 'awarddriver-to-fund' | 'default-function'>('item-to-ledger');
  const [mappings, setMappings] = useState<Record<string, DimensionMapping[]>>({});

  const [nodeForm, setNodeForm] = useState({ id: '', code: '', name: '', parentId: '' });
  const [mappingForm, setMappingForm] = useState<Record<string, string>>({ itemTypeCode: 'SPEND_ITEM' });

  const mappingOptions = [
    { key: 'item-to-ledger', label: 'Item → Ledger Account' },
    { key: 'costcenter-to-org', label: 'Cost Center → Organization' },
    { key: 'awarddriver-to-fund', label: 'Award Driver → Fund' },
    { key: 'default-function', label: 'Default Function' },
  ] as const;

  useEffect(() => { void loadTypes(); }, []);
  useEffect(() => { if (selectedType) void loadNodes(); }, [selectedType, includeInactive]);
  useEffect(() => { if (activeTab === 'mappings') void loadMappings(); }, [activeTab]);

  async function loadTypes() {
    try {
      const data = await fetchDimensionTypes();
      setTypes(data);
      if (data.length > 0 && !data.some((type) => type.code === selectedType)) {
        setSelectedType(data[0].code);
      }
    } catch (e) { setError(getErrorMessage(e)); }
  }

  async function loadNodes() {
    try {
      const data = query.trim()
        ? await searchDimensionNodes(selectedType, query, includeInactive)
        : await fetchDimensionTree(selectedType, includeInactive);
      setNodes(data);
    } catch (e) { setError(getErrorMessage(e)); }
  }

  async function loadMappings() {
    try {
      const [item, cc, award, func] = await Promise.all([
        fetchMappings('item-to-ledger'),
        fetchMappings('costcenter-to-org'),
        fetchMappings('awarddriver-to-fund'),
        fetchMappings('default-function'),
      ]);
      setMappings({ 'item-to-ledger': item, 'costcenter-to-org': cc, 'awarddriver-to-fund': award, 'default-function': func });
    } catch (e) { setError(getErrorMessage(e)); }
  }

  async function saveNode() {
    try {
      if (nodeForm.id) {
        await updateDimensionNode(selectedType, nodeForm.id, nodeForm);
      } else {
        await createDimensionNode(selectedType, nodeForm);
      }
      setNodeForm({ id: '', code: '', name: '', parentId: '' });
      await loadNodes();
    } catch (e) { setError(getErrorMessage(e)); }
  }

  async function saveMapping() {
    try {
      if (mappingPath === 'item-to-ledger') {
        await upsertMapping(mappingPath, {
          itemTypeCode: mappingForm.itemTypeCode,
          itemNodeId: mappingForm.itemNodeId,
          ledgerAccountNodeId: mappingForm.ledgerAccountNodeId,
        });
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
    } catch (e) { setError(getErrorMessage(e)); }
  }

  async function deleteMappingRow(row: DimensionMapping) {
    try {
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
    } catch (e) { setError(getErrorMessage(e)); }
  }

  const mappingRows = useMemo(() => mappings[mappingPath] ?? [], [mappings, mappingPath]);

  return <section>
    <div className="page-header card"><div><h2>Accounting Dimensions</h2><p>Canonical dimensions and core mappings.</p></div></div>
    <div className="card actions-row">
      <button className={activeTab === 'tree' ? '' : 'secondary'} onClick={() => setActiveTab('tree')}>Tree</button>
      <button className={activeTab === 'mappings' ? '' : 'secondary'} onClick={() => setActiveTab('mappings')}>Mappings</button>
    </div>
    {error && <p className="message error">{error}</p>}

    {activeTab === 'tree' && <div className="card">
      <div className="table-tools">
        <select value={selectedType} onChange={(e) => setSelectedType(e.target.value)}>{types.map((t) => <option key={t.code} value={t.code}>{t.code}</option>)}</select>
        <input className="search" value={query} placeholder="Search" onChange={(e) => setQuery(e.target.value)} />
        <label><input type="checkbox" checked={includeInactive} onChange={(e) => setIncludeInactive(e.target.checked)} /> Show inactive</label>
        <button className="secondary" onClick={() => void loadNodes()}>Refresh</button>
      </div>
      <div className="form-grid">
        <label>Code<input value={nodeForm.code} onChange={(e) => setNodeForm({ ...nodeForm, code: e.target.value })} /></label>
        <label>Name<input value={nodeForm.name} onChange={(e) => setNodeForm({ ...nodeForm, name: e.target.value })} /></label>
        <label>Parent ID<input value={nodeForm.parentId} onChange={(e) => setNodeForm({ ...nodeForm, parentId: e.target.value })} /></label>
        <div className="actions-row"><button onClick={() => void saveNode()}>{nodeForm.id ? 'Update' : 'Add'}</button>
          <button className="secondary" onClick={() => setNodeForm({ id: '', code: '', name: '', parentId: '' })}>Clear</button>
          <button className="secondary" onClick={() => void reorderDimensionNodes(selectedType, undefined, nodes.filter((n) => !n.parentId).map((n) => n.id).reverse()).then(loadNodes)}>Reorder Roots</button>
        </div>
      </div>
      <table><thead><tr><th>Code</th><th>Name</th><th>Status</th><th>Depth</th><th>Parent</th><th>Actions</th></tr></thead>
        <tbody>{nodes.map((n) => <tr key={n.id}><td>{n.code}</td><td>{'· '.repeat(n.depth)}{n.name}</td><td>{n.status}</td><td>{n.depth}</td><td>{n.parentId ?? 'ROOT'}</td>
          <td><button className="secondary" onClick={() => setNodeForm({ id: n.id, code: n.code, name: n.name, parentId: n.parentId ?? '' })}>Edit</button>
          <button className="secondary" onClick={() => void setDimensionNodeStatus(selectedType, n.id, n.status === 'Active' ? 'Inactive' : 'Active').then(loadNodes)}>{n.status === 'Active' ? 'Inactivate' : 'Activate'}</button>
          <button className="secondary" onClick={() => void moveDimensionNode(selectedType, n.id, undefined).then(loadNodes)}>Move Root</button></td></tr>)}</tbody></table>
    </div>}

    {activeTab === 'mappings' && <div className="card">
      <div className="table-tools">
        <select value={mappingPath} onChange={(e) => setMappingPath(e.target.value as typeof mappingPath)}>{mappingOptions.map((o) => <option key={o.key} value={o.key}>{o.label}</option>)}</select>
        {mappingPath === 'item-to-ledger' && <>
          <select value={mappingForm.itemTypeCode ?? 'SPEND_ITEM'} onChange={(e) => setMappingForm({ ...mappingForm, itemTypeCode: e.target.value })}><option>SPEND_ITEM</option><option>REVENUE_ITEM</option></select>
          <input placeholder="itemNodeId" value={mappingForm.itemNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, itemNodeId: e.target.value })} />
          <input placeholder="ledgerAccountNodeId" value={mappingForm.ledgerAccountNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, ledgerAccountNodeId: e.target.value })} />
        </>}
        {mappingPath === 'costcenter-to-org' && <>
          <input placeholder="costCenterNodeId" value={mappingForm.costCenterNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, costCenterNodeId: e.target.value })} />
          <input placeholder="organizationNodeId" value={mappingForm.organizationNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, organizationNodeId: e.target.value })} />
        </>}
        {mappingPath === 'awarddriver-to-fund' && <>
          <select value={mappingForm.driverTypeCode ?? 'GIFT'} onChange={(e) => setMappingForm({ ...mappingForm, driverTypeCode: e.target.value })}><option>GIFT</option><option>GRANT</option><option>PROJECT</option><option>APPROPRIATION</option><option>NONE</option></select>
          <input placeholder="driverNodeId (blank for NONE)" value={mappingForm.driverNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, driverNodeId: e.target.value })} />
          <input placeholder="fundNodeId" value={mappingForm.fundNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, fundNodeId: e.target.value })} />
        </>}
        {mappingPath === 'default-function' && <>
          <select value={mappingForm.sourceTypeCode ?? 'PROGRAM'} onChange={(e) => setMappingForm({ ...mappingForm, sourceTypeCode: e.target.value })}><option>PROGRAM</option><option>ORGANIZATION</option><option>LEDGER_ACCOUNT</option></select>
          <input placeholder="sourceNodeId" value={mappingForm.sourceNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, sourceNodeId: e.target.value })} />
          <input placeholder="functionNodeId" value={mappingForm.functionNodeId ?? ''} onChange={(e) => setMappingForm({ ...mappingForm, functionNodeId: e.target.value })} />
        </>}
        <button onClick={() => void saveMapping()}>Save</button>
      </div>
      <table><thead><tr><th>Mapping</th><th>Source</th><th>Target</th><th /></tr></thead><tbody>
        {mappingRows.map((row) => <tr key={row.id}><td>{row.mappingType}</td><td>{row.sourceTypeCode}:{row.sourceNodeId ?? row.sourceKey}</td><td>{row.targetTypeCode}:{row.targetNodeId}</td>
          <td><button className="secondary" onClick={() => void deleteMappingRow(row)}>Delete</button></td></tr>)}
      </tbody></table>
    </div>}
  </section>;
}
