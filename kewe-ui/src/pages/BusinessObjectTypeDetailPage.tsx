import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchBusinessObjectType, fetchBusinessObjectTypes, fetchBusinessObjects, fetchDimensionTree, updateBusinessObjectType } from '../api';
import type { BusinessObjectInstance, BusinessObjectType, ConfiguredField, DimensionNode } from '../api/types';
import { HelpTip } from '../ui/help/HelpTip';
import {
  ACCOUNTING_BUDGET_FIELDS,
  ACCOUNTING_BUDGET_SECTIONS,
  normalizeValue,
  type AccountingBudgetFieldMeta,
} from '../ui/businessObjects/accountingBudget/fieldMeta';

type Tab = 'basic' | 'accounting' | 'roles';
type LookupOptions = Record<string, Array<{ value: string; label: string }>>;

export function BusinessObjectTypeDetailPage() {
  const { code = '' } = useParams();
  const [tab, setTab] = useState<Tab>('basic');
  const [model, setModel] = useState<BusinessObjectType | null>(null);
  const [lookupOptions, setLookupOptions] = useState<LookupOptions>({});
  const [allObjects, setAllObjects] = useState<BusinessObjectInstance[]>([]);
  const [allTypes, setAllTypes] = useState<BusinessObjectType[]>([]);

  useEffect(() => {
    void fetchBusinessObjectType(code).then(setModel);
  }, [code]);

  useEffect(() => {
    void (async () => {
      const [companies, functions, ledgerAccounts, types, objects] = await Promise.allSettled([
        fetchDimensionTree('COMPANY'),
        fetchDimensionTree('FUNCTION'),
        fetchDimensionTree('LEDGER_ACCOUNT'),
        fetchBusinessObjectTypes(),
        fetchBusinessObjects(),
      ]);
      setLookupOptions({
        defaultCompanyId: mapNodes(companies.status === 'fulfilled' ? companies.value : []),
        defaultFunctionId: mapNodes(functions.status === 'fulfilled' ? functions.value : []),
        defaultLedgerAccountId: mapNodes(ledgerAccounts.status === 'fulfilled' ? ledgerAccounts.value : []),
      });
      setAllTypes(types.status === 'fulfilled' ? types.value : []);
      setAllObjects(objects.status === 'fulfilled' ? objects.value : []);
    })();
  }, []);

  const groupedFields = useMemo(
    () => ACCOUNTING_BUDGET_SECTIONS.map((section) => ({ section, fields: ACCOUNTING_BUDGET_FIELDS.filter((field) => field.section === section) })),
    [],
  );

  const chargeObjectOptions = useMemo(() => {
    const typeByCode = new Map(allTypes.map((value) => [value.code, value]));
    return allObjects
      .filter((objectValue) => {
        const objectType = typeByCode.get(objectValue.typeCode);
        const overrideValue = objectValue.accountingBudgetOverrides?.chargeObjectEnabled?.value;
        const defaultValue = objectType?.accountingBudgetDefaults?.chargeObjectEnabled?.defaultValue;
        return normalizeValue(overrideValue ?? defaultValue, 'toggle') === true;
      })
      .map((objectValue) => ({ value: objectValue.id, label: `${objectValue.code} — ${objectValue.name}` }));
  }, [allObjects, allTypes]);

  if (!model) return <p>Loading…</p>;

  const updateField = (field: AccountingBudgetFieldMeta, patch: Partial<ConfiguredField<boolean | string | string[] | undefined>>) => {
    const current = model.accountingBudgetDefaults?.[field.key] ?? {
      allowOverride: false,
      overrideReasonRequired: false,
      defaultValue: field.controlType === 'toggle' ? false : field.controlType === 'list' || field.controlType === 'inboundAllocations' ? [] : '',
    };
    setModel({
      ...model,
      accountingBudgetDefaults: {
        ...model.accountingBudgetDefaults,
        [field.key]: {
          ...current,
          ...patch,
        },
      },
    });
  };

  const renderControl = (field: AccountingBudgetFieldMeta) => {
    const configured = model.accountingBudgetDefaults?.[field.key];
    const value = normalizeValue(configured?.defaultValue, field.controlType === 'inboundAllocations' ? 'list' : field.controlType);
    if (field.controlType === 'toggle') {
      return (
        <label className='switch'>
          <input
            type='checkbox'
            checked={Boolean(value)}
            onChange={(e) => updateField(field, { defaultValue: e.target.checked })}
          />
          <span className='switch-slider' />
        </label>
      );
    }

    if (field.controlType === 'lookup') {
      const options = lookupOptions[field.key] ?? [];
      if (!options.length) {
        return (
          <input
            value={String(value)}
            placeholder={`${field.label} (TODO: replace with lookup)`}
            onChange={(e) => updateField(field, { defaultValue: e.target.value })}
          />
        );
      }
      return (
        <select value={String(value)} onChange={(e) => updateField(field, { defaultValue: e.target.value })}>
          <option value=''>Select…</option>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      );
    }

    if (field.controlType === 'chargeObjectLookup') {
      return (
        <select value={String(value)} onChange={(e) => updateField(field, { defaultValue: e.target.value })}>
          <option value=''>Select charge object…</option>
          {chargeObjectOptions.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      );
    }

    if (field.controlType === 'inboundAllocations') {
      const listValue = Array.isArray(value) ? value : [];
      return (
        <div className='stack-xs'>
          <div className='stack-xs'>
            {listValue.map((sourceId, index) => (
              <div key={`${sourceId}-${index}`} className='inline-form-row'>
                <select
                  value={sourceId}
                  onChange={(e) => {
                    const next = [...listValue];
                    next[index] = e.target.value;
                    updateField(field, { defaultValue: next.filter(Boolean) });
                  }}
                >
                  <option value=''>Select source…</option>
                  {chargeObjectOptions.map((option) => (
                    <option key={option.value} value={option.value}>{option.label}</option>
                  ))}
                </select>
                <span className='micro-muted'>Priority {index + 1}</span>
                <button
                  type='button'
                  className='btn btn-secondary btn-inline'
                  onClick={() => updateField(field, { defaultValue: listValue.filter((_, rowIndex) => rowIndex !== index) })}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
          <select
            value=''
            onChange={(e) => {
              if (!e.target.value) return;
              updateField(field, { defaultValue: [...listValue, e.target.value] });
            }}
          >
            <option value=''>Add inbound allocation source…</option>
            {chargeObjectOptions.filter((option) => !listValue.includes(option.value)).map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>
      );
    }

    if (field.controlType === 'list') {
      const listValue = Array.isArray(value) ? value.join(', ') : '';
      return (
        <textarea
          value={listValue}
          rows={2}
          placeholder='Comma-separated values'
          onChange={(e) => updateField(field, { defaultValue: e.target.value.split(',').map((item) => item.trim()).filter(Boolean) })}
        />
      );
    }

    if (field.controlType === 'select') {
      return (
        <select value={String(value)} onChange={(e) => updateField(field, { defaultValue: e.target.value })}>
          <option value=''>Select…</option>
          {field.selectOptions?.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      );
    }

    return <input value={String(value)} onChange={(e) => updateField(field, { defaultValue: e.target.value })} />;
  };

  return (
    <section className='page-section'>
      <div className='page-header-row'>
        <h2>Business Object Type: {model.code}</h2>
        <button className='btn btn-primary' onClick={() => void updateBusinessObjectType(model.code, model)}>Save</button>
      </div>

      <div className='segmented-control'>
        <button className={tab === 'basic' ? 'segment active' : 'segment'} onClick={() => setTab('basic')}>Basic Setup</button>
        <button className={tab === 'accounting' ? 'segment active' : 'segment'} onClick={() => setTab('accounting')}>Accounting/Budget Setup</button>
        <button className={tab === 'roles' ? 'segment active' : 'segment'} onClick={() => setTab('roles')}>Roles Template</button>
      </div>

      {tab === 'basic' && (
        <div className='form-grid'>
          <label>Name<input value={model.name} onChange={(e) => setModel({ ...model, name: e.target.value })} /></label>
          <label>Category<input value={model.objectKind} onChange={(e) => setModel({ ...model, objectKind: e.target.value })} /></label>
        </div>
      )}

      {tab === 'accounting' && (
        <div className='page-section'>
          <div className='callout-panel'>
            This configuration defines default accounting and budget behavior for all objects of this type. Set the default,
            choose whether instances may override it, and require a reason when policy demands justification.
          </div>

          {groupedFields.map(({ section, fields }) => (
            <details key={section} className='budget-section-card' open>
              <summary>{section}</summary>
              <div className='budget-grid-header'>
                <span>Setting</span>
                <span>Default Value</span>
                <span>Allow Override</span>
                <span>Reason Required</span>
              </div>
              {fields.map((field) => {
                const configured = model.accountingBudgetDefaults?.[field.key];
                const allowOverride = Boolean(configured?.allowOverride);
                return (
                  <div className='budget-config-row' key={field.key}>
                    <div className='budget-label-cell'>{field.label}<HelpTip term={field.helpTerm as never} /></div>
                    <div>{renderControl(field)}</div>
                    <label className='switch switch-secondary'>
                      <input
                        type='checkbox'
                        checked={allowOverride}
                        onChange={(e) => updateField(field, { allowOverride: e.target.checked, overrideReasonRequired: e.target.checked ? configured?.overrideReasonRequired : false })}
                      />
                      <span className='switch-slider' />
                    </label>
                    <label className='switch switch-secondary'>
                      <input
                        type='checkbox'
                        checked={Boolean(configured?.overrideReasonRequired)}
                        disabled={!allowOverride}
                        onChange={(e) => updateField(field, { overrideReasonRequired: e.target.checked })}
                      />
                      <span className='switch-slider' />
                    </label>
                  </div>
                );
              })}
            </details>
          ))}
        </div>
      )}

      {tab === 'roles' && <p>Roles template placeholder.</p>}
      <button className='btn btn-primary' onClick={() => void updateBusinessObjectType(model.code, model)}>Save</button>
    </section>
  );
}

function mapNodes(nodes: DimensionNode[]): Array<{ value: string; label: string }> {
  return nodes.map((node) => ({ value: node.id, label: `${node.code} — ${node.name}` }));
}
