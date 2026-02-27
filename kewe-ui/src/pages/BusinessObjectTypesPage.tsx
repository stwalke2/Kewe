import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes } from '../api';
import type { BusinessObjectType } from '../api/types';
import { IconActionButton, EditIcon } from '../ui/actions';

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
          <h2>Business Dimension Types</h2>
          <p>Define default templates that Business Dimensions inherit from.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/business-object-types/new')}>
          New Business Dimension Type
        </button>
      </div>

      <div className="card table-card">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {types.map((typeValue) => (
              <tr key={typeValue.id}>
                <td>{typeValue.code}</td>
                <td>{typeValue.name}</td>
                <td>{typeValue.status}</td>
                <td>
                  <IconActionButton icon={<EditIcon />} label="Edit" onClick={() => navigate(`/business-object-types/${typeValue.code}`)} />
                </td>
              </tr>
            ))}
            {types.length === 0 && (
              <tr>
                <td colSpan={4}>
                  <div className="empty-state">
                    <p>No Business Dimension Types yet.</p>
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
