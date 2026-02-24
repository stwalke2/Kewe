import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBusinessObjectType, getErrorMessage } from '../api';

const categories = ['Responsibility', 'Funding', 'Activity', 'Agent', 'Financial'] as const;

export function BusinessObjectTypeCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<{ code: string; name: string; objectKind: string; description: string; status: string }>({
    code: '',
    name: '',
    objectKind: categories[0],
    description: '',
    status: 'Active',
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
        objectKind: form.objectKind,
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
      <h2>New Business Object Type</h2>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>
          Code
          <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
        </label>
        <label>
          Name
          <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label>
          Category
          <select value={form.objectKind} onChange={(event) => setForm({ ...form, objectKind: event.target.value })}>
            {categories.map((category) => (
              <option key={category} value={category}>{category}</option>
            ))}
          </select>
        </label>
        <label>
          Description
          <input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        <label>
          Status
          <input value={form.status} readOnly />
        </label>
        {error && <div className="message error">{error}</div>}
        <div>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
        </div>
      </form>
    </section>
  );
}
