import { useEffect, useState } from "react";
import { api } from "./api";

function App() {
  const [invoices, setInvoices] = useState<any[]>([]);

  useEffect(() => {
    api.get("/supplier-invoices")
      .then(res => setInvoices(res.data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div style={{ padding: 40 }}>
      <h1>Kewe Supplier Invoices</h1>

      {invoices.map(inv => (
        <div key={inv.id} style={{
          border: "1px solid #ddd",
          padding: 20,
          marginBottom: 20
        }}>
          <h3>{inv.invoiceNumber}</h3>
          <p>Status: {inv.status}</p>
          <p>Supplier: {inv.supplierId}</p>
          <p>Amount: ${inv.invoiceAmount}</p>
        </div>
      ))}
    </div>
  );
}

export default App;

