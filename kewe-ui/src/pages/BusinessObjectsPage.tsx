import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes, fetchBusinessObjects } from '../api';
import type { BusinessObjectInstance, BusinessObjectType } from '../api/types';

export function BusinessObjectsPage() {
  const [typeCode, setTypeCode] = useState('');
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const [objects, setObjects] = useState<BusinessObjectInstance[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    void fetchBusinessObjectTypes().then(setTypes);
  }, []);

  useEffect(() => {
    void fetchBusinessObjects(typeCode || undefined).then(setObjects);
  }, [typeCode]);

  if (types.length === 0) {
    return (
      <section className="page-section">
        <h2>Business Objects</h2>
        <div className="card empty-state">
          <p>Create a Business Object Type first.</p>
          <button className="btn btn-primary" onClick={() => navigate('/business-object-types')}>
            Go to Business Object Types
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="page-section">
      <div className="page-header-row">
        <h2>Business Objects</h2>
        <button className="btn btn-primary" onClick={() => navigate('/business-objects/new')}>
          New Object
        </button>
      </div>

      <label>
        Filter by type
        <select value={typeCode} onChange={(event) => setTypeCode(event.target.value)}>
          <option value="">All</option>
          {types.map((typeValue) => (
            <option key={typeValue.code} value={typeValue.code}>{typeValue.name}</option>
          ))}
        </select>
      </label>

      <div className="card table-card">
        <table className="clickable-rows">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Type</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {objects.map((objectValue) => (
              <tr key={objectValue.id} onClick={() => navigate(`/business-objects/${objectValue.id}`)}>
                <td>{objectValue.code}</td>
                <td>{objectValue.name}</td>
                <td>{objectValue.typeCode}</td>
                <td>{objectValue.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
