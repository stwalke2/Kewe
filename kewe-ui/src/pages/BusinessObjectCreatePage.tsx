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
    status: 'Active',
    hierarchyPlacement: '',
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
      const created = await createBusinessObject({
        typeCode: form.typeCode,
        code: form.code,
        name: form.name,
        description: form.description || undefined,
        effectiveDate: form.effectiveDate ? new Date(form.effectiveDate).toISOString() : undefined,
        visibility: form.status,
        hierarchies: form.hierarchyPlacement
          ? [{ hierarchyTypeCode: 'MANUAL', path: form.hierarchyPlacement }]
          : [],
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
      <h2>New Business Object</h2>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>
          Business Object Type
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
          Description
          <input value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        <label>
          Effective date
          <input type="date" value={form.effectiveDate} onChange={(event) => setForm({ ...form, effectiveDate: event.target.value })} />
        </label>
        <label>
          Status
          <input value={form.status} readOnly />
        </label>
        <label>
          Hierarchy placement (optional)
          <input value={form.hierarchyPlacement} onChange={(event) => setForm({ ...form, hierarchyPlacement: event.target.value })} />
        </label>
        <p>Roles: add later.</p>
        {error && <div className="message error">{error}</div>}
        <div>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
        </div>
      </form>
    </section>
  );
}
