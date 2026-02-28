import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { InvoiceDetailPage } from './pages/InvoiceDetailPage';
import { InvoiceListPage } from './pages/InvoiceListPage';
import { BusinessObjectTypesPage } from './pages/BusinessObjectTypesPage';
import { BusinessObjectTypeDetailPage } from './pages/BusinessObjectTypeDetailPage';
import { BusinessObjectTypeCreatePage } from './pages/BusinessObjectTypeCreatePage';
import { BusinessObjectsPage } from './pages/BusinessObjectsPage';
import { BusinessObjectDetailPage } from './pages/BusinessObjectDetailPage';
import { BusinessObjectCreatePage } from './pages/BusinessObjectCreatePage';
import { BudgetsPage } from './pages/BudgetsPage';
import { CreateRequisitionPage } from './pages/requisitions/CreateRequisitionPage';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/supplier-invoices" replace />} />
        <Route path="/supplier-invoices" element={<InvoiceListPage />} />
        <Route path="/supplier-invoices/:id" element={<InvoiceDetailPage />} />
        <Route path="/business-object-types" element={<BusinessObjectTypesPage />} />
        <Route path="/business-object-types/new" element={<BusinessObjectTypeCreatePage />} />
        <Route path="/business-object-types/:code" element={<BusinessObjectTypeDetailPage />} />
        <Route path="/business-objects" element={<BusinessObjectsPage />} />
        <Route path="/business-objects/new" element={<BusinessObjectCreatePage />} />
        <Route path="/business-objects/:id" element={<BusinessObjectDetailPage />} />
        <Route path="/budgets" element={<BudgetsPage />} />
        <Route path="/requisitions/new" element={<CreateRequisitionPage />} />
        <Route path="*" element={<Navigate to="/supplier-invoices" replace />} />
      </Routes>
    </Layout>
  );
}

export default App;
