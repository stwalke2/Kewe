import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createDraft, fetchInvoices, getErrorDetails } from '../api';
import type { InvoiceStatus, SupplierInvoice } from '../api/types';
import { CreateDraftModal } from '../components/CreateDraftModal';
import { StatusPill } from '../components/StatusPill';

const statuses: Array<InvoiceStatus | 'All'> = ['All', 'Draft', 'Submitted', 'Approved', 'Posted'];
type SortDirection = 'asc' | 'desc' | null;
type SortKey = 'invoiceNumber' | 'supplierId' | 'invoiceDate' | 'invoiceAmount' | 'status';

export function InvoiceListPage() {
  const [invoices, setInvoices] = useState<SupplierInvoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ReturnType<typeof getErrorDetails> | null>(null);
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'All'>('All');
  const [sortKey, setSortKey] = useState<SortKey | null>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(null);
  const [selectedInvoiceIds, setSelectedInvoiceIds] = useState<string[]>([]);
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    void loadInvoices();
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(query), 250);
    return () => window.clearTimeout(timer);
  }, [query]);

  const loadInvoices = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchInvoices();
      setInvoices(data);
    } catch (e) {
      setError(getErrorDetails(e));
    } finally {
      setLoading(false);
    }
  };

  const filteredAndSorted = useMemo(() => {
    const normalized = debouncedQuery.trim().toLowerCase();
    const filtered = invoices.filter((invoice) => {
      const matchesStatus = statusFilter === 'All' || invoice.status === statusFilter;
      if (!normalized) {
        return matchesStatus;
      }

      const matchesQuery = [invoice.invoiceNumber, invoice.supplierId, invoice.status]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized));

      return matchesStatus && matchesQuery;
    });

    if (!sortKey || !sortDirection) {
      return filtered;
    }

    return [...filtered].sort((a, b) => {
      const left = a[sortKey];
      const right = b[sortKey];

      if (left == null) return 1;
      if (right == null) return -1;

      if (typeof left === 'number' && typeof right === 'number') {
        return sortDirection === 'asc' ? left - right : right - left;
      }

      return sortDirection === 'asc'
        ? String(left).localeCompare(String(right))
        : String(right).localeCompare(String(left));
    });
  }, [invoices, debouncedQuery, statusFilter, sortKey, sortDirection]);

  const rowAnimationSeed = `${debouncedQuery}-${statusFilter}-${sortKey ?? 'none'}-${sortDirection ?? 'none'}`;
  const visibleInvoiceIds = filteredAndSorted.map((invoice) => invoice.id);
  const allVisibleSelected = visibleInvoiceIds.length > 0 && visibleInvoiceIds.every((id) => selectedInvoiceIds.includes(id));

  const toggleSelection = (invoiceId: string) => {
    setSelectedInvoiceIds((existing) =>
      existing.includes(invoiceId) ? existing.filter((id) => id !== invoiceId) : [...existing, invoiceId],
    );
  };

  const toggleSelectAllVisible = () => {
    setSelectedInvoiceIds((existing) => {
      if (allVisibleSelected) {
        return existing.filter((id) => !visibleInvoiceIds.includes(id));
      }

      return Array.from(new Set([...existing, ...visibleInvoiceIds]));
    });
  };

  const cycleSort = (column: SortKey) => {
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

  const sortClass = (column: SortKey, direction: Exclude<SortDirection, null>) => {
    if (sortKey === column && sortDirection === direction) {
      return 'sort-chevron active';
    }
    return 'sort-chevron';
  };

  const handleCreate = async (payload: Parameters<typeof createDraft>[0]) => {
    const created = await createDraft(payload);
    setShowModal(false);
    navigate(`/supplier-invoices/${created.id}`);
    return created;
  };

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Supplier Invoices</h2>
          <p>Track supplier invoices from draft creation through posting.</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => void loadInvoices()}>
            Refresh
          </button>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            New invoice
          </button>
        </div>
      </div>

      <div className="card table-card">
        <div className="filters-row">
          <input
            className="search"
            placeholder="Search invoice number, supplier, or status"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="segmented-control" role="tablist" aria-label="Filter by status">
            {statuses.map((status) => (
              <button
                key={status}
                className={statusFilter === status ? 'segment active' : 'segment'}
                onClick={() => setStatusFilter(status)}
              >
                {status}
              </button>
            ))}
          </div>
          <button className="btn btn-secondary">Date range</button>
        </div>

        {loading && <p>Loading invoices…</p>}

        {error && (
          <div className="message error">
            <strong>Request error</strong>
            <div>Status: {error.status ?? 'N/A'}</div>
            <div>Endpoint: {error.endpoint ?? 'N/A'}</div>
            <div>Message: {error.message}</div>
          </div>
        )}

        {!loading && !error && (
          <table className="clickable-rows align-table">
            <thead>
              <tr>
                <th className="checkbox-col">
                  <input
                    type="checkbox"
                    className="row-check"
                    aria-label="Select all visible invoices"
                    checked={allVisibleSelected}
                    onChange={toggleSelectAllVisible}
                  />
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('invoiceNumber')}>
                    Invoice #<span className="sort-icons"><span className={sortClass('invoiceNumber', 'asc')}>▲</span><span className={sortClass('invoiceNumber', 'desc')}>▼</span></span>
                  </button>
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('supplierId')}>
                    Supplier<span className="sort-icons"><span className={sortClass('supplierId', 'asc')}>▲</span><span className={sortClass('supplierId', 'desc')}>▼</span></span>
                  </button>
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('invoiceDate')}>
                    Invoice Date<span className="sort-icons"><span className={sortClass('invoiceDate', 'asc')}>▲</span><span className={sortClass('invoiceDate', 'desc')}>▼</span></span>
                  </button>
                </th>
                <th className="amount">
                  <button className="sort-header amount" onClick={() => cycleSort('invoiceAmount')}>
                    Amount<span className="sort-icons"><span className={sortClass('invoiceAmount', 'asc')}>▲</span><span className={sortClass('invoiceAmount', 'desc')}>▼</span></span>
                  </button>
                </th>
                <th>
                  <button className="sort-header" onClick={() => cycleSort('status')}>
                    Status<span className="sort-icons"><span className={sortClass('status', 'asc')}>▲</span><span className={sortClass('status', 'desc')}>▼</span></span>
                  </button>
                </th>
                <th className="actions-col" />
              </tr>
            </thead>
            <tbody>
              {filteredAndSorted.map((invoice, index) => (
                <tr
                  key={`${invoice.id}-${rowAnimationSeed}`}
                  className="animated-row"
                  style={{ animationDelay: `${Math.min(index * 24, 200)}ms` }}
                  onClick={() => navigate(`/supplier-invoices/${invoice.id}`)}
                >
                  <td className="checkbox-col" onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      className="row-check"
                      checked={selectedInvoiceIds.includes(invoice.id)}
                      aria-label={`Select invoice ${invoice.invoiceNumber}`}
                      onChange={() => toggleSelection(invoice.id)}
                    />
                  </td>
                  <td className="strong">{invoice.invoiceNumber}</td>
                  <td>{invoice.supplierId}</td>
                  <td>{invoice.invoiceDate ?? '—'}</td>
                  <td className="amount">{invoice.invoiceAmount?.toFixed(2) ?? '—'}</td>
                  <td>
                    <StatusPill status={invoice.status} />
                  </td>
                  <td className="actions-col">⋯</td>
                </tr>
              ))}
              {filteredAndSorted.length === 0 && (
                <tr>
                  <td colSpan={7}>
                    <div className="empty-state">
                      <p>No invoices matched your filters.</p>
                      <button className="btn btn-primary" onClick={() => setShowModal(true)}>
                        Create draft invoice
                      </button>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {showModal && <CreateDraftModal onClose={() => setShowModal(false)} onCreate={handleCreate} />}
    </section>
  );
}
