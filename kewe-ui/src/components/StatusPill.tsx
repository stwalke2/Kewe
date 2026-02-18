interface Props {
  status: string;
}

type StatusTone = 'success' | 'danger' | 'warning' | 'info' | 'neutral' | 'custom';

const STATUS_TONE_RULES: Array<{ tone: StatusTone; keywords: string[] }> = [
  { tone: 'success', keywords: ['success', 'active', 'approved', 'posted', 'complete', 'completed', 'paid'] },
  { tone: 'danger', keywords: ['error', 'danger', 'failed', 'rejected', 'blocked', 'stopped', 'cancelled'] },
  { tone: 'warning', keywords: ['warning', 'pending', 'review', 'hold'] },
  { tone: 'info', keywords: ['info', 'information', 'submitted', 'in progress', 'processing', 'open'] },
  { tone: 'neutral', keywords: ['neutral', 'draft', 'inactive', 'new', 'not started'] },
  { tone: 'custom', keywords: ['optional', 'custom'] },
];

function resolveStatusTone(status: string): StatusTone {
  const normalized = status.trim().toLowerCase();
  const exactMatch = STATUS_TONE_RULES.find((rule) => rule.keywords.includes(normalized));

  if (exactMatch) {
    return exactMatch.tone;
  }

  const partialMatch = STATUS_TONE_RULES.find((rule) => rule.keywords.some((keyword) => normalized.includes(keyword)));
  return partialMatch?.tone ?? 'neutral';
}

export function StatusPill({ status }: Props) {
  const tone = resolveStatusTone(status);
  return <span className={`status-pill status-${tone}`}>{status}</span>;
}
