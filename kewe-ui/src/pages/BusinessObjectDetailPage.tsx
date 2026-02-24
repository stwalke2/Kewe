import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchBusinessObject, fetchBusinessObjectType, fetchDimensionTree, updateBusinessObjectOverrides } from '../api';
import type { BusinessObjectInstance, BusinessObjectType, DimensionNode } from '../api/types';
import { HelpTip } from '../ui/help/HelpTip';
import {
  ACCOUNTING_BUDGET_FIELDS,
  ACCOUNTING_BUDGET_SECTIONS,
  formatDisplayValue,
  normalizeValue,
  type AccountingBudgetFieldMeta,
} from '../ui/businessObjects/accountingBudget/fieldMeta';

type LookupOptions = Record<string, Array<{ value: string; label: string }>>;

export function BusinessObjectDetailPage() {
  const { id = '' } = useParams();
  const [obj, setObj] = useState<BusinessObjectInstance | null>(null);
  const [type, setType] = useState<BusinessObjectType | null>(null);
  const [lookupOptions, setLookupOptions] = useState<LookupOptions>({});

  useEffect(() => {
    void (async () => {
      const loadedObject = await fetchBusinessObject(id);
      setObj(loadedObject);
      setType(await fetchBusinessObjectType(loadedObject.typeCode));
    })();
  }, [id]);

  useEffect(() => {
    void (async () => {
      const [companies, functions, ledgerAccounts] = await Promise.allSettled([
        fetchDimensionTree('COMPANY'),
        fetchDimensionTree('FUNCTION'),
        fetchDimensionTree('LEDGER_ACCOUNT'),
      ]);
      setLookupOptions({
        defaultCompanyId: mapNodes(companies.status === 'fulfilled' ? companies.value : []),
        defaultFunctionId: mapNodes(functions.status === 'fulfilled' ? functions.value : []),
        defaultLedgerAccountId: mapNodes(ledgerAccounts.status === 'fulfilled' ? ledgerAccounts.value : []),
      });
    })();
  }, []);

  const groupedFields = useMemo(
    () => ACCOUNTING_BUDGET_SECTIONS.map((section) => ({ section, fields: ACCOUNTING_BUDGET_FIELDS.filter((field) => field.section === section) })),
    [],
  );

  if (!obj || !type) return <p>Loading…</p>;

  const overrides = obj.accountingBudgetOverrides ?? {};

  const setOverrideEnabled = (field: AccountingBudgetFieldMeta, enabled: boolean) => {
    const defaultValue = type.accountingBudgetDefaults?.[field.key]?.defaultValue;
    const nextOverrides = { ...overrides };
    if (enabled) {
      nextOverrides[field.key] = { value: defaultValue === undefined ? (field.controlType === 'list' ? [] : '') : defaultValue };
    } else {
      delete nextOverrides[field.key];
    }
    setObj({
      ...obj,
      accountingBudgetOverrides: nextOverrides,
    });
  };

  const updateOverride = (fieldKey: string, patch: { value?: string | boolean | string[]; overrideReason?: string }) => {
    const existing = overrides[fieldKey];
    if (!existing) return;
    setObj({
      ...obj,
      accountingBudgetOverrides: {
        ...overrides,
        [fieldKey]: {
          ...existing,
          ...patch,
        },
      },
    });
  };

  const renderEffectiveControl = (field: AccountingBudgetFieldMeta) => {
    const defaultValue = type.accountingBudgetDefaults?.[field.key]?.defaultValue;
    const hasOverride = Boolean(overrides[field.key]);
    const effectiveValue = hasOverride ? overrides[field.key].value : defaultValue;
    const value = normalizeValue(effectiveValue, field.controlType);

    if (field.controlType === 'toggle') {
      return (
        <label className='switch'>
          <input
            type='checkbox'
            checked={Boolean(value)}
            disabled={!hasOverride}
            onChange={(e) => updateOverride(field.key, { value: e.target.checked })}
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
            disabled={!hasOverride}
            placeholder={`${field.label} (TODO: replace with lookup)`}
            onChange={(e) => updateOverride(field.key, { value: e.target.value })}
          />
        );
      }

      return (
        <select
          value={String(value)}
          disabled={!hasOverride}
          onChange={(e) => updateOverride(field.key, { value: e.target.value })}
        >
          <option value=''>Select…</option>
          {options.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      );
    }


    if (field.controlType === 'list') {
      const listValue = Array.isArray(value) ? value.join(', ') : '';
      return (
        <textarea
          value={listValue}
          rows={2}
          disabled={!hasOverride}
          placeholder='Comma-separated values'
          onChange={(e) => updateOverride(field.key, { value: e.target.value.split(',').map((item) => item.trim()).filter(Boolean) })}
        />
      );
    }

    if (field.controlType === 'select') {
      return (
        <select
          value={String(value)}
          disabled={!hasOverride}
          onChange={(e) => updateOverride(field.key, { value: e.target.value })}
        >
          <option value=''>Select…</option>
          {field.selectOptions?.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      );
    }

    return (
      <input
        value={String(value)}
        disabled={!hasOverride}
        onChange={(e) => updateOverride(field.key, { value: e.target.value })}
      />
    );
  };

  return (
    <section className='page-section'>
      <div className='page-header-row'>
        <h2>Business Object Instance: {obj.code}</h2>
        <button className='btn btn-primary' onClick={() => void updateBusinessObjectOverrides(obj.id, Object.fromEntries(Object.entries(obj.accountingBudgetOverrides ?? {}).filter(([, value]) => value)))}>Save</button>
      </div>

      <div className='callout-panel'>
        Defaults come from the Business Object Type. Enable override only where allowed, set the effective value, and provide a reason when policy requires it.
      </div>

      {groupedFields.map(({ section, fields }) => (
        <details key={section} className='budget-section-card' open>
          <summary>{section}</summary>
          <div className='budget-grid-header budget-grid-header-instance'>
            <span>Setting</span>
            <span>Default</span>
            <span>Override?</span>
            <span>Effective Value</span>
            <span>Reason</span>
          </div>
          {fields.map((field) => {
            const config = type.accountingBudgetDefaults?.[field.key];
            const override = overrides[field.key];
            const canOverride = Boolean(config?.allowOverride);
            const defaultValue = config?.defaultValue;
            const effectiveValue = override?.value ?? defaultValue;
            const isOverridden = Boolean(override) && String(effectiveValue ?? '') !== String(defaultValue ?? '');
            const reasonRequired = Boolean(config?.overrideReasonRequired && override);

            return (
              <div className='budget-config-row budget-config-row-instance' key={field.key}>
                <div className='budget-label-cell'>
                  {field.label} <HelpTip term={field.helpTerm as never} /> {isOverridden ? <span className='status-pill status-warning'>Overridden</span> : null}
                </div>
                <div className='read-only-cell'>{formatDisplayValue(defaultValue, field)}</div>
                <label className='switch switch-secondary'>
                  <input
                    type='checkbox'
                    checked={Boolean(override)}
                    disabled={!canOverride}
                    onChange={(e) => setOverrideEnabled(field, e.target.checked)}
                  />
                  <span className='switch-slider' />
                </label>
                <div>{renderEffectiveControl(field)}</div>
                <input
                  placeholder={reasonRequired ? 'Reason required' : 'Reason (optional)'}
                  value={override?.overrideReason ?? ''}
                  disabled={!override}
                  required={reasonRequired}
                  onChange={(e) => updateOverride(field.key, { overrideReason: e.target.value })}
                />
              </div>
            );
          })}
        </details>
      ))}

      <button className='btn btn-primary' onClick={() => void updateBusinessObjectOverrides(obj.id, Object.fromEntries(Object.entries(obj.accountingBudgetOverrides ?? {}).filter(([, value]) => value)))}>Save</button>
    </section>
  );
}

function mapNodes(nodes: DimensionNode[]): Array<{ value: string; label: string }> {
  return nodes.map((node) => ({ value: node.id, label: `${node.code} — ${node.name}` }));
}
