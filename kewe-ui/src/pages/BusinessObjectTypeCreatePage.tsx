import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBusinessObjectType, getErrorMessage } from '../api';

export function BusinessObjectTypeCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    code: '',
    name: '',
    description: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const created = await createBusinessObjectType({
        code: form.code,
        name: form.name,
        objectKind: 'Business Dimension',
        description: form.description || undefined,
      });
      navigate(`/business-object-types/${created.code}`);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="page-section">
      <h2>New Business Dimension Type</h2>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>
          Code
          <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
        </label>
        <label>
          Name
          <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label className="dimension-home-full-width">
          Definition
          <input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        {error && <div className="message error">{error}</div>}
        <div>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
        </div>
      </form>
    </section>
  );
}
