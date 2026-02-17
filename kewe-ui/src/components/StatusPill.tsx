import type { InvoiceStatus } from '../api/types';

interface Props {
  status: InvoiceStatus;
}

export function StatusPill({ status }: Props) {
  return <span className={`status-pill status-${status.toLowerCase()}`}>{status}</span>;
}
