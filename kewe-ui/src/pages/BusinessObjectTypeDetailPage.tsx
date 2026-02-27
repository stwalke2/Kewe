import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchBusinessObjectType } from '../api';
import type { BusinessObjectType } from '../api/types';
import { BusinessDimensionWorkspace } from '../components/BusinessDimensionWorkspace';

export function BusinessObjectTypeDetailPage() {
  const { code = '' } = useParams();
  const [model, setModel] = useState<BusinessObjectType | null>(null);

  useEffect(() => {
    void fetchBusinessObjectType(code).then(setModel);
  }, [code]);

  if (!model) return <p>Loading…</p>;

  return (
    <section className="page-section">
      <Link className="back-link" to="/business-object-types">← Back to Business Dimension Types</Link>
      <BusinessDimensionWorkspace
        title={model.name}
        definition={model.description}
        code={model.code}
        name={model.name}
        effectiveDate={model.createdAt}
        isType
      />
    </section>
  );
}
