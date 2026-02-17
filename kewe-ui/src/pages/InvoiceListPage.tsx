import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createDraft, fetchInvoices, getErrorDetails } from '../api';
import type { InvoiceStatus, SupplierInvoice } from '../api/types';
import { CreateDraftModal } from '../components/CreateDraftModal';
import { StatusPill } from '../components/StatusPill';

const statuses: Array<InvoiceStatus | 'All'> = ['All', 'Draft', 'Submitted', 'Approved', 'Posted'];

export function InvoiceListPage() {
  const [invoices, setInvoices] = useState<SupplierInvoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ReturnType<typeof getErrorDetails> | null>(null);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'All'>('All');
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    void loadInvoices();
  }, []);

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

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();

    return invoices.filter((invoice) => {
      const matchesStatus = statusFilter === 'All' || invoice.status === statusFilter;
      if (!normalized) {
        return matchesStatus;
      }

      const matchesQuery = [invoice.invoiceNumber, invoice.supplierId, invoice.status]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized));

      return matchesStatus && matchesQuery;
    });
  }, [invoices, query, statusFilter]);

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
                <th>Invoice #</th>
                <th>Supplier</th>
                <th>Invoice Date</th>
                <th className="amount">Amount</th>
                <th>Status</th>
                <th className="actions-col" />
              </tr>
            </thead>
            <tbody>
              {filtered.map((invoice) => (
                <tr key={invoice.id} onClick={() => navigate(`/supplier-invoices/${invoice.id}`)}>
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
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={6}>
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
