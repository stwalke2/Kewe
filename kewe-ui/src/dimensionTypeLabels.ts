import type { DimensionType } from './api/types';

const FALLBACK_LABELS: Record<string, string> = {
  LEDGER_ACCOUNT: 'Ledger Account',
  SPEND_ITEM: 'Spend Item',
  REVENUE_ITEM: 'Revenue Item',
  ORGANIZATION: 'Organization',
  COST_CENTER: 'Cost Center',
  PROGRAM: 'Program',
  FUNCTION: 'Function',
  FUND: 'Fund',
  GIFT: 'Gift',
  GRANT: 'Grant',
  PROJECT: 'Project',
  APPROPRIATION: 'Appropriation',
};

export function getDimensionTypeLabel(typeCode: string, types: DimensionType[]): string {
  const found = types.find((type) => type.code === typeCode);
  if (found?.name) {
    return found.name;
  }

  return FALLBACK_LABELS[typeCode] ?? typeCode;
}
