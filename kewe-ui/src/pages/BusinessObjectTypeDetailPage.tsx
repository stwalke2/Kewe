import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { fetchBusinessObjectType, updateBusinessObjectType } from '../api';
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
        onSave={async (draft) => {
          const updated = await updateBusinessObjectType(model.code, {
            code: model.code,
            name: draft.name,
            objectKind: model.objectKind,
            description: draft.definition,
            allowInstanceAccountingBudgetOverride: model.allowInstanceAccountingBudgetOverride ?? false,
            accountingBudgetDefaults: model.accountingBudgetDefaults,
          });
          setModel(updated);
        }}
        isType
      />
    </section>
  );
}
