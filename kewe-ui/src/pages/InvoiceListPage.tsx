import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createDraft, fetchInvoices, getErrorMessage } from '../api';
import type { InvoiceStatus, SupplierInvoice } from '../api/types';
import { CreateDraftModal } from '../components/CreateDraftModal';
import { StatusPill } from '../components/StatusPill';

const statuses: Array<InvoiceStatus | 'All'> = ['All', 'Draft', 'Submitted', 'Approved', 'Posted'];

export function InvoiceListPage() {
  const [invoices, setInvoices] = useState<SupplierInvoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | 'All'>('All');
  const [showModal, setShowModal] = useState(false);
  const [lastLoadedAt, setLastLoadedAt] = useState<Date | null>(null);
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
      setLastLoadedAt(new Date());
    } catch (e) {
      setError(getErrorMessage(e));
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
    navigate(`/invoices/${created.id}`);
    return created;
  };

  return (
    <section>
      <div className="page-header card">
        <div>
          <h2>Supplier Invoices</h2>
          <p>Track invoice workflow from Draft to Posted.</p>
          <p className="subtle">
            Last loaded:{' '}
            {lastLoadedAt ? lastLoadedAt.toLocaleString() : 'Never'}
          </p>
        </div>
        <button onClick={() => setShowModal(true)}>Create Draft</button>
      </div>

      <div className="card table-card">
        <div className="table-tools">
          <input
            className="search"
            placeholder="Search by invoice, supplier, or status"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as InvoiceStatus | 'All')}
          >
            {statuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
          <button className="secondary" onClick={() => void loadInvoices()}>
            Refresh
          </button>
        </div>

        {loading && <p>Loading invoices…</p>}
        {error && <p className="message error">Unable to load invoices: {error}</p>}

        {!loading && !error && (
          <table className="clickable-rows">
            <thead>
              <tr>
                <th>Invoice #</th>
                <th>Supplier</th>
                <th>Invoice Date</th>
                <th>Status</th>
                <th className="amount">Amount</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((invoice) => (
                <tr key={invoice.id} onClick={() => navigate(`/invoices/${invoice.id}`)}>
                  <td>{invoice.invoiceNumber}</td>
                  <td>{invoice.supplierId}</td>
                  <td>{invoice.invoiceDate ?? '—'}</td>
                  <td>
                    <StatusPill status={invoice.status} />
                  </td>
                  <td className="amount">{invoice.invoiceAmount?.toFixed(2) ?? '—'}</td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={5}>No invoices found for current filters.</td>
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
