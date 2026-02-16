import type { PropsWithChildren } from 'react';
import { NavLink } from 'react-router-dom';

export function Layout({ children }: PropsWithChildren) {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">Kewe</div>
        <nav>
          <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} end>
            Supplier Invoices
          </NavLink>
        </nav>
      </aside>
      <div className="content-shell">
        <header className="top-header">
          <h1>Supplier Invoice MVP</h1>
        </header>
        <main>{children}</main>
      </div>
    </div>
  );
}
