import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchBusinessObject } from '../api';
import type { BusinessObjectInstance } from '../api/types';
import { BusinessDimensionWorkspace } from '../components/BusinessDimensionWorkspace';

export function BusinessObjectDetailPage() {
  const { id = '' } = useParams();
  const [model, setModel] = useState<BusinessObjectInstance | null>(null);

  useEffect(() => {
    void fetchBusinessObject(id).then(setModel);
  }, [id]);

  const includedHierarchies = useMemo(
    () => model?.hierarchies?.map((value) => value.path ?? value.nodeId ?? '').filter(Boolean) ?? [],
    [model],
  );

  if (!model) return <p>Loading…</p>;

  return (
    <section className="page-section">
      <Link className="back-link" to="/business-objects">← Back to Business Dimensions</Link>
      <BusinessDimensionWorkspace
        title={model.name}
        definition={model.description}
        code={model.code}
        name={model.name}
        effectiveDate={model.effectiveDate ?? model.createdAt}
        includedHierarchies={includedHierarchies}
        isType={false}
      />
    </section>
  );
}
