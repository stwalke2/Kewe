import type { ButtonHTMLAttributes, ReactNode } from 'react';

type IconButtonVariant = 'primary' | 'secondary';

type IconActionButtonProps = {
  icon: ReactNode;
  label: string;
  variant?: IconButtonVariant;
  iconOnly?: boolean;
} & ButtonHTMLAttributes<HTMLButtonElement>;

export function IconActionButton({
  icon,
  label,
  variant = 'secondary',
  iconOnly = false,
  className = '',
  ...buttonProps
}: IconActionButtonProps) {
  const variantClass = variant === 'primary' ? 'btn-primary' : 'btn-secondary';
  const iconOnlyClass = iconOnly ? 'btn-icon-only' : '';
  return (
    <button
      {...buttonProps}
      className={`btn action-btn ${variantClass} ${iconOnlyClass} ${className}`.trim()}
      title={buttonProps.title ?? label}
      aria-label={buttonProps['aria-label'] ?? label}
    >
      <span className='action-icon' aria-hidden='true'>{icon}</span>
      {!iconOnly ? <span>{label}</span> : null}
    </button>
  );
}

export function SaveIcon() {
  return <svg viewBox='0 0 20 20' focusable='false'><path d='M3 2.75A1.75 1.75 0 0 1 4.75 1h8.69c.46 0 .9.18 1.23.51l2.82 2.82c.33.33.51.77.51 1.23v10.69A1.75 1.75 0 0 1 16.25 18h-11.5A1.75 1.75 0 0 1 3 16.25V2.75Zm2 .25V7h8V3H5Zm8 13v-4H7v4h6Z' fill='currentColor'/></svg>;
}

export function EditIcon() {
  return <svg viewBox='0 0 20 20' focusable='false'><path d='M13.98 2.43a2.5 2.5 0 0 1 3.54 3.54l-8.3 8.3a1 1 0 0 1-.46.26l-3.2.8a.75.75 0 0 1-.91-.91l.8-3.2a1 1 0 0 1 .26-.46l8.3-8.3Zm2.47 2.47a1 1 0 0 0-1.41-1.41L7.02 11.5l-.47 1.9 1.9-.47 8.01-8.02Z' fill='currentColor'/></svg>;
}

export function ExportIcon() {
  return <svg viewBox='0 0 20 20' focusable='false'><path d='M10 2a.75.75 0 0 1 .75.75V10l2.72-2.72a.75.75 0 0 1 1.06 1.06l-4 4a.75.75 0 0 1-1.06 0l-4-4a.75.75 0 1 1 1.06-1.06L9.25 10V2.75A.75.75 0 0 1 10 2ZM3 13.25a.75.75 0 0 1 .75.75v1.25c0 .55.45 1 1 1h10.5c.55 0 1-.45 1-1V14a.75.75 0 0 1 1.5 0v1.25A2.5 2.5 0 0 1 15.25 17.5H4.75a2.5 2.5 0 0 1-2.5-2.5V14a.75.75 0 0 1 .75-.75Z' fill='currentColor'/></svg>;
}
