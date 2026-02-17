import type { PropsWithChildren } from 'react';
import { NavLink, useLocation } from 'react-router-dom';

const pageMeta: Record<string, { title: string; subtitle: string }> = {
  '/supplier-invoices': {
    title: 'Supplier Invoices',
    subtitle: 'Manage invoice lifecycle, approvals, and posting in one place.',
  },
  '/accounting-dimensions': {
    title: 'Accounting Dimensions',
    subtitle: 'Configure dimensions, hierarchies, and mappings for accounting rules.',
  },
};

export function Layout({ children }: PropsWithChildren) {
  const location = useLocation();
  const isInvoiceDetail = location.pathname.startsWith('/supplier-invoices/');
  const isDimensionDetail = location.pathname.startsWith('/accounting-dimensions/');
  const meta = isInvoiceDetail
    ? { title: 'Invoice Detail', subtitle: 'Review invoice details, lines, and workflow actions.' }
    : isDimensionDetail
      ? { title: 'Dimension Detail', subtitle: 'Manage dimension setup, hierarchy, and status actions.' }
    : pageMeta[location.pathname] ?? pageMeta['/supplier-invoices'];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand">Kewe</div>
          <p className="subtle">Finance Workspace</p>
        </div>

        <div className="nav-group">
          <p className="nav-group-title">Main</p>
          <nav>
            <NavLink
              to="/supplier-invoices"
              className={({ isActive }) => (isActive || isInvoiceDetail ? 'nav-link active' : 'nav-link')}
            >
              Supplier Invoices
            </NavLink>
            <NavLink
              to="/accounting-dimensions"
              className={({ isActive }) => (isActive || isDimensionDetail ? 'nav-link active' : 'nav-link')}
            >
              Accounting Dimensions
            </NavLink>
          </nav>
        </div>

        <div className="nav-group">
          <p className="nav-group-title">Other</p>
          <span className="nav-link nav-link-muted">Settings (soon)</span>
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
