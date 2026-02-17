import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchDimensionTree, fetchDimensionTypes, getErrorDetails } from '../api';
import type { ApiErrorDetails, DimensionNode, DimensionType } from '../api/types';
import { StatusPill } from '../components/StatusPill';

type SortDirection = 'asc' | 'desc' | null;
type NodeSortKey = 'name' | 'typeCode' | 'status';

export function AccountingDimensionsPage() {
  const [types, setTypes] = useState<DimensionType[]>([]);
  const [selectedType, setSelectedType] = useState<string>('All');
  const [nodes, setNodes] = useState<DimensionNode[]>([]);
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [includeInactive, setIncludeInactive] = useState(false);
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
    nodes.forEach((node) => map.set(node.id, node.name));
    return map;
  }, [nodes]);

  const filteredAndSortedNodes = useMemo(() => {
    const normalized = debouncedQuery.trim().toLowerCase();
    const filtered = nodes.filter((node) => {
      const matchesType = selectedType === 'All' || node.typeCode === selectedType;
      if (!matchesType) {
        return false;
      }

      if (!normalized) {
        return true;
      }

      return [node.code, node.name, node.typeCode, node.status]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized));
    });

    if (!sortKey || !sortDirection) {
      return filtered;
    }

    return [...filtered].sort((a, b) => {
      const left = a[sortKey];
      const right = b[sortKey];
      return sortDirection === 'asc' ? left.localeCompare(right) : right.localeCompare(left);
    });
  }, [nodes, selectedType, debouncedQuery, sortKey, sortDirection]);

  const rowAnimationSeed = `${selectedType}-${debouncedQuery}-${sortKey ?? 'none'}-${sortDirection ?? 'none'}`;

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
    if (sortKey !== column) {
      setSortKey(column);
      setSortDirection('asc');
      return;
    }

    if (sortDirection === 'asc') {
      setSortDirection('desc');
      return;
    }

    if (sortDirection === 'desc') {
      setSortDirection(null);
      setSortKey(null);
      return;
    }

    setSortDirection('asc');
  };

  const sortClass = (column: NodeSortKey, direction: Exclude<SortDirection, null>) => {
    if (sortKey === column && sortDirection === direction) {
      return 'sort-chevron active';
    }
    return 'sort-chevron';
  };

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Accounting Dimensions</h2>
          <p>Search and review dimension hierarchy nodes.</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => void loadNodes()}>
            Refresh
          </button>
          <button className="btn btn-primary" onClick={() => navigate('/accounting-dimensions/new')}>
            New Dimension
          </button>
        </div>
      </div>

      {error && (
        <div className="message error">
          <strong>Request error</strong>
          <div>Status: {error.status ?? 'N/A'}</div>
          <div>Endpoint: {error.endpoint ?? 'N/A'}</div>
          <div>Message: {error.message}</div>
        </div>
      )}

      <div className="card table-card">
        <div className="filters-row">
          <input
            className="search"
            value={query}
            placeholder="Search code, name, type, or status"
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="segmented-control" role="tablist" aria-label="Filter dimension type">
            <button className={selectedType === 'All' ? 'segment active' : 'segment'} onClick={() => setSelectedType('All')}>
              All
            </button>
            {types.map((type) => (
              <button
                key={type.code}
                className={selectedType === type.code ? 'segment active' : 'segment'}
                onClick={() => setSelectedType(type.code)}
              >
                {type.name}
              </button>
            ))}
          </div>
          <label>
            <input type="checkbox" checked={includeInactive} onChange={(event) => setIncludeInactive(event.target.checked)} /> Show inactive
          </label>
        </div>

        {loading && <p>Loading dimensions…</p>}

        {!loading && (
          <table className="clickable-rows align-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('name')}>
                    Name
                    <span className="sort-icons">
                      <span className={sortClass('name', 'asc')}>▲</span>
                      <span className={sortClass('name', 'desc')}>▼</span>
                    </span>
                  </button>
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('typeCode')}>
                    Type
                    <span className="sort-icons">
                      <span className={sortClass('typeCode', 'asc')}>▲</span>
                      <span className={sortClass('typeCode', 'desc')}>▼</span>
                    </span>
                  </button>
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('status')}>
                    Status
                    <span className="sort-icons">
                      <span className={sortClass('status', 'asc')}>▲</span>
                      <span className={sortClass('status', 'desc')}>▼</span>
                    </span>
                  </button>
                </th>
                <th>Hierarchy</th>
              </tr>
            </thead>
            <tbody>
              {filteredAndSortedNodes.map((node, index) => (
                <tr
                  key={`${node.id}-${rowAnimationSeed}`}
                  className="animated-row"
                  style={{ animationDelay: `${Math.min(index * 22, 180)}ms` }}
                  onClick={() => navigate(`/accounting-dimensions/${node.id}`)}
                >
                  <td>{node.code}</td>
                  <td>{'· '.repeat(node.depth)}{node.name}</td>
                  <td>{node.typeCode}</td>
                  <td><StatusPill status={node.status} /></td>
                  <td>{node.parentId ? nodeNameById.get(node.parentId) ?? node.parentId : 'Top Level'}</td>
                </tr>
              ))}
              {filteredAndSortedNodes.length === 0 && (
                <tr>
                  <td colSpan={5}>
                    <div className="empty-state">
                      <p>No dimensions match the current filters.</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
