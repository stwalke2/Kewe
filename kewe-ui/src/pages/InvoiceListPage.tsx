import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createDraft, fetchInvoices, getErrorMessage } from '../api/client';
import type { SupplierInvoice } from '../api/types';
import { CreateDraftModal } from '../components/CreateDraftModal';
import { StatusPill } from '../components/StatusPill';

export function InvoiceListPage() {
  const [invoices, setInvoices] = useState<SupplierInvoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadInvoices();
  }, []);

  const loadInvoices = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchInvoices();
      setInvoices(data);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return invoices;
    }

    return invoices.filter((invoice) => {
      return [invoice.invoiceNumber, invoice.supplierId, invoice.status]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized));
    });
  }, [invoices, query]);

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
          <button className="secondary" onClick={loadInvoices}>
            Refresh
          </button>
        </div>

        {loading && <p>Loading invoices…</p>}
        {error && <p className="message error">{error}</p>}

        {!loading && !error && (
          <table>
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
            </tbody>
          </table>
        )}
      </div>

      {showModal && <CreateDraftModal onClose={() => setShowModal(false)} onCreate={handleCreate} />}
    </section>
  );
}
