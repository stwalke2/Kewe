import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchInvoiceById, getErrorMessage, transitionInvoice } from '../api/client';
import type { SupplierInvoice } from '../api/types';
import { StatusPill } from '../components/StatusPill';

const nextActionByStatus: Partial<Record<SupplierInvoice['status'], 'submit' | 'approve' | 'post'>> = {
  Draft: 'submit',
  Submitted: 'approve',
  Approved: 'post',
};

export function InvoiceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [invoice, setInvoice] = useState<SupplierInvoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!id) {
      return;
    }
    loadInvoice(id);
  }, [id]);

  const loadInvoice = async (invoiceId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchInvoiceById(invoiceId);
      setInvoice(data);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  const nextAction = useMemo(() => {
    if (!invoice) {
      return null;
    }
    return nextActionByStatus[invoice.status] ?? null;
  }, [invoice]);

  const handleTransition = async () => {
    if (!id || !nextAction) {
      return;
    }
    setBusy(true);
    setMessage(null);
    try {
      await transitionInvoice(id, nextAction);
      await loadInvoice(id);
      setMessage({ kind: 'success', text: `Invoice ${nextAction}ed successfully.` });
    } catch (e) {
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <section>
      <div className="page-header card">
        <div>
          <p>
            <Link to="/">← Back to list</Link>
          </p>
          <h2>Invoice Detail</h2>
        </div>
      </div>

      {loading && <p>Loading invoice…</p>}
      {error && <p className="message error">{error}</p>}

      {!loading && !error && invoice && (
        <div className="detail-grid">
          <div className="card">
            <div className="row">
              <h3>{invoice.invoiceNumber}</h3>
              <StatusPill status={invoice.status} />
            </div>

            <dl className="invoice-fields">
              <dt>Supplier</dt>
              <dd>{invoice.supplierId}</dd>
              <dt>Invoice Date</dt>
              <dd>{invoice.invoiceDate ?? '—'}</dd>
              <dt>Accounting Date</dt>
              <dd>{invoice.accountingDate ?? '—'}</dd>
              <dt>Currency</dt>
              <dd>{invoice.currency ?? '—'}</dd>
              <dt>Total Amount</dt>
              <dd>{invoice.invoiceAmount?.toFixed(2) ?? '—'}</dd>
              <dt>Memo</dt>
              <dd>{invoice.memo ?? '—'}</dd>
            </dl>

            {nextAction && (
              <button onClick={handleTransition} disabled={busy}>
                {busy ? 'Processing…' : nextAction.charAt(0).toUpperCase() + nextAction.slice(1)}
              </button>
            )}
            {!nextAction && <p>This invoice has no further actions.</p>}

            {message && <p className={`message ${message.kind}`}>{message.text}</p>}
          </div>

          <div className="card">
            <h3>Lines</h3>
            {invoice.lines.length === 0 && <p>No lines available.</p>}
            {invoice.lines.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>Description</th>
                    <th className="amount">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {invoice.lines.map((line, idx) => (
                    <tr key={`${line.description}-${idx}`}>
                      <td>{line.description || '—'}</td>
                      <td className="amount">{line.amount?.toFixed(2) ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
