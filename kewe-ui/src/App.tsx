import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { InvoiceDetailPage } from './pages/InvoiceDetailPage';
import { InvoiceListPage } from './pages/InvoiceListPage';
import { AccountingDimensionsPage } from './pages/AccountingDimensionsPage';
import { AccountingDimensionDetailPage } from './pages/AccountingDimensionDetailPage';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/supplier-invoices" replace />} />
        <Route path="/supplier-invoices" element={<InvoiceListPage />} />
        <Route path="/supplier-invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="/business-objects" element={<AccountingDimensionsPage />} />
        <Route path="/business-objects/new" element={<AccountingDimensionDetailPage />} />
        <Route path="/business-objects/:id" element={<AccountingDimensionDetailPage />} />
        <Route path="/accounting-dimensions" element={<Navigate to="/business-objects" replace />} />
        <Route path="/accounting-dimensions/new" element={<Navigate to="/business-objects/new" replace />} />
        <Route path="/accounting-dimensions/:id" element={<AccountingDimensionDetailPage />} />
        <Route path="*" element={<Navigate to="/supplier-invoices" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;
