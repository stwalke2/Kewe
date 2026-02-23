import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchDimensionTree, fetchDimensionTypes, getErrorDetails } from '../api';
import type { ApiErrorDetails, DimensionNode, DimensionType } from '../api/types';
import { StatusPill } from '../components/StatusPill';
import { getDimensionTypeLabel } from '../dimensionTypeLabels';

type SortDirection = 'asc' | 'desc' | null;
type NodeSortKey = 'code' | 'name' | 'description' | 'typeCode' | 'status' | 'parent';
type StatusFilter = 'all' | 'ACTIVE' | 'INACTIVE';

export function AccountingDimensionsPage() {
  const [types, setTypes] = useState<DimensionType[]>([]);
  const [selectedType, setSelectedType] = useState<string>('All');
  const [nodes, setNodes] = useState<DimensionNode[]>([]);
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [includeInactive, setIncludeInactive] = useState(false);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [showFilters, setShowFilters] = useState(false);
  const [sortKey, setSortKey] = useState<NodeSortKey | null>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(null);
  const [error, setError] = useState<ApiErrorDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    void loadTypes();
  }, []);

  useEffect(() => {
    if (types.length > 0) {
      void loadNodes();
    }
  }, [selectedType, includeInactive, types]);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(query), 250);
    return () => window.clearTimeout(timer);
  }, [query]);

  const nodeNameById = useMemo(() => {
    const map = new Map<string, string>();
    nodes.forEach((node) => map.set(node.id, `${node.code} ${node.name}`));
    return map;
  }, [nodes]);

  function getSortableValue(node: DimensionNode, column: NodeSortKey): string {
    switch (column) {
      case 'typeCode':
        return getDimensionTypeLabel(node.typeCode, types);
      case 'parent':
        return node.parentId ? nodeNameById.get(node.parentId) ?? '' : 'Top level';
      case 'description':
        return node.description ?? '';
      default:
        return node[column];
    }
  }

  const filteredAndSortedNodes = useMemo(() => {
    const normalized = debouncedQuery.trim().toLowerCase();
    const filtered = nodes.filter((node) => {
      const matchesType = selectedType === 'All' || node.typeCode === selectedType;
      if (!matchesType) {
        return false;
      }

      if (statusFilter !== 'all' && node.status !== statusFilter) {
        return false;
      }

      if (!normalized) {
        return true;
      }

      return [node.code, node.name, node.description ?? '', getDimensionTypeLabel(node.typeCode, types), node.status]
        .some((value) => value.toLowerCase().includes(normalized));
    });

    if (!sortKey || !sortDirection) {
      return filtered;
    }

    return [...filtered].sort((a, b) => {
      const left = getSortableValue(a, sortKey);
      const right = getSortableValue(b, sortKey);
      return sortDirection === 'asc' ? left.localeCompare(right) : right.localeCompare(left);
    });
  }, [nodes, selectedType, debouncedQuery, sortKey, sortDirection, types, statusFilter, nodeNameById]);

  async function loadTypes() {
    try {
      setError(null);
      const data = await fetchDimensionTypes();
      setTypes(data);
    } catch (e) {
      setError(getErrorDetails(e));
    }
  }

  async function loadNodes() {
    try {
      setLoading(true);
      setError(null);
      if (selectedType === 'All') {
        const allNodes = await Promise.all(types.map((type) => fetchDimensionTree(type.code, includeInactive)));
        setNodes(allNodes.flat());
      } else {
        const data = await fetchDimensionTree(selectedType, includeInactive);
        setNodes(data);
      }
    } catch (e) {
      setError(getErrorDetails(e));
    } finally {
      setLoading(false);
    }
  }

  const cycleSort = (column: NodeSortKey) => {
    if (sortKey !== column) return (setSortKey(column), setSortDirection('asc'));
    if (sortDirection === 'asc') return setSortDirection('desc');
    if (sortDirection === 'desc') return (setSortDirection(null), setSortKey(null));
    setSortDirection('asc');
  };

  const sortClass = (column: NodeSortKey, direction: Exclude<SortDirection, null>) =>
    sortKey === column && sortDirection === direction ? 'sort-chevron active' : 'sort-chevron';

  const escapeCsvCell = (value: string) => {
    const escaped = value.replace(/"/g, '""');
    return `"${escaped}"`;
  };

  const downloadAsCsv = () => {
    const headers = ['Code', 'Name', 'Description', 'Type', 'Status', 'Parent'];
    const rows = filteredAndSortedNodes.map((node) => [
      node.code,
      node.name,
      node.description ?? '',
      getDimensionTypeLabel(node.typeCode, types),
      node.status,
      node.parentId ? nodeNameById.get(node.parentId) ?? '' : 'Top level',
    ]);
    const csv = [headers, ...rows].map((row) => row.map((cell) => escapeCsvCell(cell)).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'business-objects.csv';
    link.click();
    URL.revokeObjectURL(link.href);
  };

  const renderSortableHeader = (label: string, column: NodeSortKey) => (
    <button className="sort-header" onClick={() => cycleSort(column)}>
      {label}
      <span className="sort-icons">
        <span className={sortClass(column, 'asc')}>▲</span>
        <span className={sortClass(column, 'desc')}>▼</span>
      </span>
    </button>
  );

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Business Objects</h2>
          <p>Search and review business objects.</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => void loadNodes()}>Refresh</button>
          <button className="btn btn-primary" onClick={() => navigate('/business-objects/new')}>New Business Object</button>
        </div>
      </div>

      {error && <div className="message error"><strong>Request error</strong><div>Message: {error.message}</div></div>}

      <div className="card table-card">
        <div className="filters-row">
          <input className="search accounting-search" value={query} placeholder="Search code, name, description, type, or status" onChange={(event) => setQuery(event.target.value)} />
          <div className="table-tools">
            <button className="btn btn-secondary" onClick={() => setShowFilters((current) => !current)}>
              Filter
            </button>
            <button className="btn btn-secondary" onClick={downloadAsCsv}>
              Download
            </button>
          </div>
        </div>

        <div className="type-tabs-scroll">
          <div className="segmented-control" role="tablist" aria-label="Filter dimension type">
            <button className={selectedType === 'All' ? 'segment active' : 'segment'} onClick={() => setSelectedType('All')}>All</button>
            {types.map((type) => (
              <button key={type.code} className={selectedType === type.code ? 'segment active' : 'segment'} onClick={() => setSelectedType(type.code)}>{type.name}</button>
            ))}
          </div>
        </div>

        {showFilters && (
          <div className="filter-panel">
            <label>
              Status
              <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as StatusFilter)}>
                <option value="all">All statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </label>
            <label className="inline-check">
              <input type="checkbox" checked={includeInactive} onChange={(event) => setIncludeInactive(event.target.checked)} />
              Show inactive from source
            </label>
            <button className="btn btn-secondary" onClick={() => setStatusFilter('all')}>Reset Filters</button>
          </div>
        )}

        {loading && <p>Loading dimensions…</p>}

        {!loading && (
          <div className="table-scroll-wrap">
            <table className="clickable-rows align-table dimensions-table">
              <thead>
                <tr>
                  <th>{renderSortableHeader('Code', 'code')}</th>
                  <th>{renderSortableHeader('Name', 'name')}</th>
                  <th>{renderSortableHeader('Description', 'description')}</th>
                  <th>{renderSortableHeader('Type', 'typeCode')}</th>
                  <th>{renderSortableHeader('Status', 'status')}</th>
                  <th>{renderSortableHeader('Parent', 'parent')}</th>
                </tr>
              </thead>
              <tbody>
                {filteredAndSortedNodes.map((node) => (
                  <tr key={node.id} onClick={() => navigate(`/business-objects/${node.id}`)}>
                    <td>{node.code}</td>
                    <td>{node.name}</td>
                    <td>{node.description || '—'}</td>
                    <td>{getDimensionTypeLabel(node.typeCode, types)}</td>
                    <td><StatusPill status={node.status} /></td>
                    <td>{node.parentId ? nodeNameById.get(node.parentId) ?? '—' : 'Top level'}</td>
                  </tr>
                ))}
                {filteredAndSortedNodes.length === 0 && <tr><td colSpan={6}><div className="empty-state"><p>No business objects match the current filters.</p></div></td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
