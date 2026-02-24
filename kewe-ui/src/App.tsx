import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { InvoiceDetailPage } from './pages/InvoiceDetailPage';
import { InvoiceListPage } from './pages/InvoiceListPage';
import { BusinessObjectTypesPage } from './pages/BusinessObjectTypesPage';
import { BusinessObjectTypeDetailPage } from './pages/BusinessObjectTypeDetailPage';
import { BusinessObjectsPage } from './pages/BusinessObjectsPage';
import { BusinessObjectDetailPage } from './pages/BusinessObjectDetailPage';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/supplier-invoices" replace />} />
        <Route path="/supplier-invoices" element={<InvoiceListPage />} />
        <Route path="/supplier-invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="/business-object-types" element={<BusinessObjectTypesPage />} />
        <Route path="/business-object-types/:code" element={<BusinessObjectTypeDetailPage />} />
        <Route path="/business-objects" element={<BusinessObjectsPage />} />
        <Route path="/business-objects/:id" element={<BusinessObjectDetailPage />} />
        <Route path="*" element={<Navigate to="/supplier-invoices" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;
