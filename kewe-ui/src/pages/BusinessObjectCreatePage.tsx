import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBusinessObject, fetchBusinessObjectTypes, getErrorMessage } from '../api';
import type { BusinessObjectType } from '../api/types';

export function BusinessObjectCreatePage() {
  const navigate = useNavigate();
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState({
    typeCode: '',
    code: '',
    name: '',
    description: '',
    effectiveDate: '',
    includedHierarchies: '',
  });

  useEffect(() => {
    void fetchBusinessObjectTypes().then((typeValues) => {
      setTypes(typeValues);
      if (typeValues.length > 0) {
        setForm((current) => ({ ...current, typeCode: current.typeCode || typeValues[0].code }));
      }
    });
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const hierarchies = form.includedHierarchies
        .split(',')
        .map((value) => value.trim())
        .filter(Boolean)
        .map((value) => ({ hierarchyCode: value }));

      const created = await createBusinessObject({
        typeCode: form.typeCode,
        code: form.code,
        name: form.name,
        description: form.description || undefined,
        effectiveDate: form.effectiveDate ? new Date(form.effectiveDate).toISOString() : undefined,
        visibility: 'Active',
        hierarchies,
        roles: [],
      });
      navigate(`/business-objects/${created.id}`);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="page-section">
      <h2>New Business Dimension</h2>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>
          Business Dimension Type
          <select required value={form.typeCode} onChange={(event) => setForm({ ...form, typeCode: event.target.value })}>
            {types.map((typeValue) => (
              <option key={typeValue.code} value={typeValue.code}>{typeValue.name}</option>
            ))}
          </select>
        </label>
        <label>
          Code
          <input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
        </label>
        <label>
          Name
          <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
        </label>
        <label>
          Effective Date
          <input type="date" value={form.effectiveDate} onChange={(event) => setForm({ ...form, effectiveDate: event.target.value })} />
        </label>
        <label className="dimension-home-full-width">
          Definition
          <input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        <label className="dimension-home-full-width">
          Included in Hierarchies
          <input
            value={form.includedHierarchies}
            onChange={(event) => setForm({ ...form, includedHierarchies: event.target.value })}
            placeholder="Sciences, Administration"
          />
        </label>
        {error && <div className="message error">{error}</div>}
        <div>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
        </div>
      </form>
    </section>
  );
}
