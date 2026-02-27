import { useState, type PropsWithChildren } from 'react';
import { NavLink, useLocation } from 'react-router-dom';

function KiwiLogo() {
  return (
    <svg viewBox="0 0 40 40" aria-hidden="true" focusable="false">
      <circle cx="20" cy="20" r="19" fill="url(#kiwe-logo-bg)" />
      <path d="M11.2 22.4c.4-4.3 3.7-7.8 7.9-8.6 3.2-.6 6.6.6 8.7 3.1-.4 4.4-3.8 8.1-8.1 8.8-3.2.5-6.5-.7-8.5-3.3Z" fill="#fef7d2" />
      <path d="M13.4 22.6c.4-2.7 2.6-4.9 5.2-5.4 2.2-.4 4.6.4 6 2-.4 2.8-2.6 5.2-5.4 5.6-2.2.3-4.5-.5-5.8-2.2Z" fill="#4d3a2c" />
      <circle cx="21.4" cy="18.6" r="1" fill="#fff" />
      <path d="M24.6 19.5c2.5.1 4.5 2.2 4.5 4.7v.2h-5.7" stroke="#f2b167" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" fill="none" />
      <circle cx="9.8" cy="24.2" r="1" fill="#4d3a2c" />
      <defs>
        <linearGradient id="kiwe-logo-bg" x1="7" y1="6" x2="34" y2="34" gradientUnits="userSpaceOnUse">
          <stop stopColor="#7251d3" />
          <stop offset="1" stopColor="#4a2aa8" />
        </linearGradient>
      </defs>
    </svg>
  );
}

function InvoicesIcon() {
  return (
    <svg viewBox="0 0 20 20" aria-hidden="true" focusable="false">
      <path d="M4.5 3.5h11a1 1 0 0 1 1 1v11.8l-2-1.3-2 1.3-2-1.3-2 1.3-2-1.3-2 1.3V4.5a1 1 0 0 1 1-1Z" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M7 7h6M7 10h6M7 13h4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function DimensionsIcon() {
  return (
    <svg viewBox="0 0 20 20" aria-hidden="true" focusable="false">
      <rect x="2.75" y="2.75" width="6.5" height="6.5" rx="1.6" fill="none" stroke="currentColor" strokeWidth="1.5" />
      <rect x="10.75" y="2.75" width="6.5" height="6.5" rx="1.6" fill="none" stroke="currentColor" strokeWidth="1.5" />
      <rect x="6.75" y="10.75" width="6.5" height="6.5" rx="1.6" fill="none" stroke="currentColor" strokeWidth="1.5" />
      <path d="M10 9.25v1.5M9.25 10h1.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function SettingsIcon() {
  return (
    <svg viewBox="0 0 20 20" aria-hidden="true" focusable="false">
      <path d="M10 6.2a3.8 3.8 0 1 0 0 7.6 3.8 3.8 0 0 0 0-7.6Z" fill="none" stroke="currentColor" strokeWidth="1.5" />
      <path d="M16.4 11.2v-2.4l-1.65-.36a5.2 5.2 0 0 0-.45-1.08l.94-1.4-1.7-1.7-1.4.94c-.35-.19-.7-.34-1.08-.45L10.8 2.6H8.4l-.36 1.66c-.37.1-.73.26-1.08.45l-1.4-.94-1.7 1.7.94 1.4c-.19.35-.34.7-.45 1.08L2.6 8.8v2.4l1.66.36c.1.38.26.73.45 1.08l-.94 1.4 1.7 1.7 1.4-.94c.35.19.7.34 1.08.45l.36 1.65h2.4l.36-1.65c.38-.11.73-.26 1.08-.45l1.4.94 1.7-1.7-.94-1.4c.19-.35.34-.7.45-1.08l1.65-.36Z" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" />
    </svg>
  );
}

const pageMeta: Record<string, { title: string; subtitle: string }> = {
  '/supplier-invoices': {
    title: 'Supplier Invoices',
    subtitle: 'Manage invoice lifecycle, approvals, and posting in one place.',
  },
  '/business-object-types': {
    title: 'Business Dimension Types',
    subtitle: 'Configure default templates for Business Dimensions.',
  },
  '/business-objects': {
    title: 'Business Dimensions',
    subtitle: 'Manage Business Dimensions, hierarchy membership, and inherited defaults.',
  },
};

export function Layout({ children }: PropsWithChildren) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const location = useLocation();
  const isInvoiceDetail = location.pathname.startsWith('/supplier-invoices/');
  const isTypeDetail = location.pathname.startsWith('/business-object-types/');
  const isObjectDetail = location.pathname.startsWith('/business-objects/');
  const meta = isInvoiceDetail
    ? { title: 'Invoice Detail', subtitle: 'Review invoice details, lines, and workflow actions.' }
    : isTypeDetail
      ? { title: 'Business Dimension Type Detail', subtitle: 'Review the Business Dimension Type home and setup tabs.' }
      : isObjectDetail
        ? { title: 'Business Dimension Detail', subtitle: 'Review Business Dimension home and setup tabs.' }
        : pageMeta[location.pathname] ?? pageMeta['/supplier-invoices'];

  return (
    <div className={`app-shell${sidebarCollapsed ? ' sidebar-collapsed' : ''}`}>
      <aside className="sidebar">
        <button
          type="button"
          className="sidebar-toggle"
          onClick={() => setSidebarCollapsed((collapsed) => !collapsed)}
          aria-label={sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          aria-pressed={sidebarCollapsed}
        >
          <svg viewBox="0 0 20 20" aria-hidden="true" focusable="false">
            <path d="m11.6 4.8-4.2 5.2 4.2 5.2" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
        <div className="brand-block">
          <div className="brand-row">
            <span className="brand-logo"><KiwiLogo /></span>
            <div>
              <div className="brand">Kewe</div>
              <p className="subtle">Finance Workspace</p>
            </div>
          </div>
        </div>

        <div className="nav-group">
          <p className="nav-group-title">Main</p>
          <nav>
            <NavLink
              to="/supplier-invoices"
              className={({ isActive }) => (isActive || isInvoiceDetail ? 'nav-link active' : 'nav-link')}
            >
              <span className="nav-icon"><InvoicesIcon /></span>
              <span className="nav-label">Supplier Invoices</span>
            </NavLink>
            <NavLink
              to="/business-object-types"
              className={({ isActive }) => (isActive || isTypeDetail ? 'nav-link active' : 'nav-link')}
            >
              <span className="nav-icon"><DimensionsIcon /></span>
              <span className="nav-label">Business Dimension Types</span>
            </NavLink>
            <NavLink
              to="/business-objects"
              className={({ isActive }) => (isActive || isObjectDetail ? 'nav-link active' : 'nav-link')}
            >
              <span className="nav-icon"><DimensionsIcon /></span>
              <span className="nav-label">Business Dimensions</span>
            </NavLink>
          </nav>
        </div>

        <div className="nav-group">
          <p className="nav-group-title">Other</p>
          <span className="nav-link nav-link-muted"><span className="nav-icon"><SettingsIcon /></span><span className="nav-label">Settings (soon)</span></span>
        </div>
      </aside>

      <div className="content-shell">
        <header className="top-header">
          <div>
            <h1>{meta.title}</h1>
            <p>{meta.subtitle}</p>
          </div>
        </header>
        <main>{children}</main>
      </div>
    </div>
  );
}
