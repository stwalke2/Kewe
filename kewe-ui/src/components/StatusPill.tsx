interface Props {
  status: string;
}

export function StatusPill({ status }: Props) {
  const normalized = status.toLowerCase();
  return <span className={`status-pill status-${normalized}`}>{status}</span>;
}
