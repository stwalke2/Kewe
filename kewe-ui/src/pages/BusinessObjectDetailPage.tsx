import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchBusinessObject, updateBusinessObject } from '../api';
import type { BusinessObjectInstance } from '../api/types';
import { BusinessDimensionWorkspace } from '../components/BusinessDimensionWorkspace';

export function BusinessObjectDetailPage() {
  const { id = '' } = useParams();
  const [model, setModel] = useState<BusinessObjectInstance | null>(null);

  useEffect(() => {
    void fetchBusinessObject(id).then(setModel);
  }, [id]);

  const includedHierarchies = useMemo(
    () => model?.hierarchies?.map((value) => value.hierarchyCode).filter(Boolean) ?? [],
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
        onSave={async (draft) => {
          const updated = await updateBusinessObject(model.id, {
            typeCode: model.typeCode,
            code: model.code,
            name: draft.name,
            description: draft.definition,
            effectiveDate: model.effectiveDate,
            visibility: model.visibility,
            hierarchies: (draft.includedHierarchies ?? []).map((value) => ({ hierarchyCode: value })),
            roles: model.roles ?? [],
          });
          setModel(updated);
        }}
        isType={false}
      />
    </section>
  );
}
