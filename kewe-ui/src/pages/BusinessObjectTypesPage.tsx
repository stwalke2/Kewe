import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes } from '../api';
import type { BusinessObjectType } from '../api/types';

export function BusinessObjectTypesPage() {
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    void fetchBusinessObjectTypes().then(setTypes);
  }, []);

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Business Object Types</h2>
          <p>Define reusable templates for institutional business objects.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/business-object-types/new')}>
          New Type
        </button>
      </div>

      <div className="card table-card">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Category</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {types.map((typeValue) => (
              <tr key={typeValue.id}>
                <td>{typeValue.code}</td>
                <td>{typeValue.name}</td>
                <td>{typeValue.objectKind}</td>
                <td>{typeValue.status}</td>
                <td>
                  <button className="btn btn-secondary" onClick={() => navigate(`/business-object-types/${typeValue.code}`)}>
                    View/Edit
                  </button>
                </td>
              </tr>
            ))}
            {types.length === 0 && (
              <tr>
                <td colSpan={5}>
                  <div className="empty-state">
                    <p>No Business Object Types yet.</p>
                    <button className="btn btn-primary" onClick={() => navigate('/business-object-types/new')}>
                      Create your first Type
                    </button>
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
