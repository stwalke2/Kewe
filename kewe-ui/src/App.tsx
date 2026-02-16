import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { InvoiceDetailPage } from './pages/InvoiceDetailPage';
import { InvoiceListPage } from './pages/InvoiceListPage';
import { AccountingDimensionsPage } from './pages/AccountingDimensionsPage';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<InvoiceListPage />} />
        <Route path="/invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="/accounting-dimensions" element={<AccountingDimensionsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;
