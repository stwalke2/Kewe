import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes, fetchBusinessObjects } from '../api';
import type { BusinessObjectInstance, BusinessObjectType } from '../api/types';
import { IconActionButton, EditIcon } from '../ui/actions';

export function BusinessObjectsPage() {
  const [objects, setObjects] = useState<BusinessObjectInstance[]>([]);
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    void Promise.all([fetchBusinessObjects(), fetchBusinessObjectTypes()]).then(([objectValues, typeValues]) => {
      setObjects(objectValues);
      setTypes(typeValues);
    });
  }, []);

  const typeByCode = useMemo(() => new Map(types.map((value) => [value.code, value.name])), [types]);

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Business Dimensions</h2>
          <p>Create and manage your Business Dimensions from seeded type defaults.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/business-objects/new')}>
          New Business Dimension
        </button>
      </div>

      <div className="card table-card">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Dimension Type</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {objects.map((objectValue) => (
              <tr key={objectValue.id}>
                <td>{objectValue.code}</td>
                <td>{objectValue.name}</td>
                <td>{typeByCode.get(objectValue.typeCode) ?? objectValue.typeCode}</td>
                <td>{objectValue.status}</td>
                <td>
                  <IconActionButton icon={<EditIcon />} label="Edit" onClick={() => navigate(`/business-objects/${objectValue.id}`)} />
                </td>
              </tr>
            ))}
            {objects.length === 0 && (
              <tr>
                <td colSpan={5}>
                  <div className="empty-state">
                    <p>No Business Dimensions yet.</p>
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
