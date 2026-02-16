import { FormEvent, useState } from 'react';
import type { CreateDraftRequest, SupplierInvoice } from '../api/types';

interface Props {
  onClose: () => void;
  onCreate: (payload: CreateDraftRequest) => Promise<SupplierInvoice>;
}

export function CreateDraftModal({ onClose, onCreate }: Props) {
  const [form, setForm] = useState<CreateDraftRequest>({
    supplierId: '',
    invoiceNumber: '',
    currency: 'USD',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await onCreate(form);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create draft');
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <h2>Create Draft Invoice</h2>
        <form onSubmit={submit} className="modal-form">
          <label>
            Supplier ID *
            <input
              required
              value={form.supplierId}
              onChange={(e) => setForm((prev) => ({ ...prev, supplierId: e.target.value }))}
            />
          </label>
          <label>
            Invoice Number *
            <input
              required
              value={form.invoiceNumber}
              onChange={(e) => setForm((prev) => ({ ...prev, invoiceNumber: e.target.value }))}
            />
          </label>
          <label>
            Invoice Date
            <input
              type="date"
              value={form.invoiceDate ?? ''}
              onChange={(e) => setForm((prev) => ({ ...prev, invoiceDate: e.target.value || undefined }))}
            />
          </label>
          <label>
            Currency
            <input
              value={form.currency ?? ''}
              onChange={(e) => setForm((prev) => ({ ...prev, currency: e.target.value || undefined }))}
            />
          </label>

          {error && <p className="message error">{error}</p>}

          <div className="modal-actions">
            <button type="button" className="secondary" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Creating…' : 'Create Draft'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
