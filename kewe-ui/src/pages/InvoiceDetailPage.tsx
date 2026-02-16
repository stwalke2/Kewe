import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  fetchInvoiceById,
  getErrorMessage,
  toUpdatePayload,
  transitionInvoice,
  updateInvoice,
} from '../api';
import type { InvoiceLine, SupplierInvoice } from '../api/types';
import { StatusPill } from '../components/StatusPill';

const nextActionByStatus: Partial<Record<SupplierInvoice['status'], 'submit' | 'approve' | 'post'>> = {
  Draft: 'submit',
  Submitted: 'approve',
  Approved: 'post',
};

const emptyLine: InvoiceLine = { description: '', amount: 0 };

function cloneInvoice(invoice: SupplierInvoice): SupplierInvoice {
  return JSON.parse(JSON.stringify(invoice)) as SupplierInvoice;
}

export function InvoiceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [invoice, setInvoice] = useState<SupplierInvoice | null>(null);
  const [draft, setDraft] = useState<SupplierInvoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (id) {
      void loadInvoice(id);
    }
  }, [id]);

  const loadInvoice = async (invoiceId: string) => {
    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      const data = await fetchInvoiceById(invoiceId);
      setInvoice(data);
      setDraft(cloneInvoice(data));
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

  const isDraftStatus = invoice?.status === 'Draft';

  const hasUnsavedChanges = useMemo(() => {
    if (!invoice || !draft) {
      return false;
    }

    const initial = JSON.stringify(toUpdatePayload(invoice));
    const current = JSON.stringify(toUpdatePayload(draft));
    return initial !== current;
  }, [draft, invoice]);

  const lineTotal = useMemo(() => {
    if (!draft) {
      return 0;
    }
    return draft.lines.reduce((sum, line) => sum + (Number.isFinite(line.amount) ? line.amount : 0), 0);
  }, [draft]);

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

  const handleSave = async () => {
    if (!id || !draft || !isDraftStatus) {
      return;
    }

    setBusy(true);
    setMessage(null);
    try {
      const saved = await updateInvoice(id, toUpdatePayload(draft));
      setInvoice(saved);
      setDraft(cloneInvoice(saved));
      setMessage({ kind: 'success', text: 'Draft invoice saved.' });
    } catch (e) {
      setMessage({ kind: 'error', text: getErrorMessage(e) });
    } finally {
      setBusy(false);
    }
  };

  const handleCancel = () => {
    if (!invoice) {
      return;
    }
    setDraft(cloneInvoice(invoice));
    setMessage({ kind: 'success', text: 'Changes reverted to last loaded state.' });
  };

  const updateLine = (index: number, patch: Partial<InvoiceLine>) => {
    setDraft((prev) => {
      if (!prev) {
        return prev;
      }
      const lines = prev.lines.map((line, idx) => (idx === index ? { ...line, ...patch } : line));
      return { ...prev, lines };
    });
  };

  const deleteLine = (index: number) => {
    setDraft((prev) => {
      if (!prev) {
        return prev;
      }
      return { ...prev, lines: prev.lines.filter((_, idx) => idx !== index) };
    });
  };

  const addLine = () => {
    setDraft((prev) => {
      if (!prev) {
        return prev;
      }
      return { ...prev, lines: [...prev.lines, { ...emptyLine }] };
    });
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

      {loading && <p>Loading invoice details…</p>}
      {error && <p className="message error">Unable to load invoice: {error}</p>}

      {!loading && !error && invoice && draft && (
        <div className="detail-grid">
          <div className="card">
            <div className="row">
              <h3>{invoice.invoiceNumber}</h3>
              <StatusPill status={invoice.status} />
            </div>

            <div className="form-grid">
              <label>
                Supplier ID
                <input
                  value={draft.supplierId}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) => setDraft({ ...draft, supplierId: event.target.value })}
                />
              </label>
              <label>
                Invoice Number
                <input
                  value={draft.invoiceNumber}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) => setDraft({ ...draft, invoiceNumber: event.target.value })}
                />
              </label>
              <label>
                Invoice Date
                <input
                  type="date"
                  value={draft.invoiceDate ?? ''}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) =>
                    setDraft({ ...draft, invoiceDate: event.target.value ? event.target.value : undefined })
                  }
                />
              </label>
              <label>
                Accounting Date
                <input
                  type="date"
                  value={draft.accountingDate ?? ''}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) =>
                    setDraft({ ...draft, accountingDate: event.target.value ? event.target.value : undefined })
                  }
                />
              </label>
              <label>
                Currency
                <input
                  value={draft.currency ?? ''}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) =>
                    setDraft({ ...draft, currency: event.target.value ? event.target.value : undefined })
                  }
                />
              </label>
              <label>
                Header Amount
                <input
                  type="number"
                  step="0.01"
                  value={draft.invoiceAmount ?? ''}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) =>
                    setDraft({
                      ...draft,
                      invoiceAmount: event.target.value ? Number(event.target.value) : undefined,
                    })
                  }
                />
              </label>
              <label className="full-width">
                Memo
                <input
                  value={draft.memo ?? ''}
                  disabled={!isDraftStatus || busy}
                  onChange={(event) => setDraft({ ...draft, memo: event.target.value || undefined })}
                />
              </label>
            </div>

            {isDraftStatus && (
              <div className="actions-row">
                <button onClick={() => void handleSave()} disabled={!hasUnsavedChanges || busy}>
                  {busy ? 'Saving…' : 'Save'}
                </button>
                <button className="secondary" onClick={handleCancel} disabled={!hasUnsavedChanges || busy}>
                  Cancel
                </button>
                {hasUnsavedChanges && <span className="dirty-indicator">Unsaved changes</span>}
              </div>
            )}

            {!isDraftStatus && <p>This invoice is locked because it is no longer in Draft status.</p>}

            <div className="totals-box">
              <p>
                <strong>Line total (authoritative):</strong> {lineTotal.toFixed(2)}
              </p>
              <p>
                <strong>Header amount:</strong> {draft.invoiceAmount?.toFixed(2) ?? '—'}
              </p>
              {draft.invoiceAmount !== undefined && draft.invoiceAmount !== lineTotal && (
                <p className="message error">Header amount differs from lines. Submit requires an exact match.</p>
              )}
            </div>

            {nextAction && (
              <button onClick={() => void handleTransition()} disabled={busy || hasUnsavedChanges}>
                {busy ? 'Processing…' : nextAction.charAt(0).toUpperCase() + nextAction.slice(1)}
              </button>
            )}
            {!nextAction && <p>This invoice has no further actions.</p>}
            {hasUnsavedChanges && nextAction && <p>Save changes before running workflow actions.</p>}

            {message && <p className={`message ${message.kind}`}>{message.text}</p>}
          </div>

          <div className="card">
            <div className="row">
              <h3>Lines</h3>
              {isDraftStatus && (
                <button className="secondary" onClick={addLine} disabled={busy}>
                  Add line
                </button>
              )}
            </div>

            {draft.lines.length === 0 && <p>No lines available.</p>}
            {draft.lines.length > 0 && (
              <table>
                <thead>
                  <tr>
                    <th>Description</th>
                    <th className="amount">Amount</th>
                    {isDraftStatus && <th>Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {draft.lines.map((line, idx) => (
                    <tr key={`${idx}-${line.description}`}>
                      <td>
                        {isDraftStatus ? (
                          <input
                            value={line.description ?? ''}
                            disabled={busy}
                            onChange={(event) => updateLine(idx, { description: event.target.value })}
                          />
                        ) : (
                          line.description || '—'
                        )}
                      </td>
                      <td className="amount">
                        {isDraftStatus ? (
                          <input
                            type="number"
                            step="0.01"
                            value={line.amount}
                            disabled={busy}
                            onChange={(event) => updateLine(idx, { amount: Number(event.target.value) || 0 })}
                          />
                        ) : (
                          line.amount?.toFixed(2) ?? '—'
                        )}
                      </td>
                      {isDraftStatus && (
                        <td>
                          <button className="secondary" onClick={() => deleteLine(idx)} disabled={busy}>
                            Delete
                          </button>
                        </td>
                      )}
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
