import { useEffect, useMemo, useState } from 'react';
import {
  agentDraftRequisition,
  createRequisitionDraft,
  fetchBusinessObjects,
  fetchChargingLocations,
  fetchFundingSnapshot,
  updateRequisitionDraft,
} from '../../api';
import type { BusinessObjectInstance, ChargingLocation, RequisitionDraft, RequisitionLine, SupplierResult } from '../../api/types';

const SUPPLIERS = ['amazon', 'fisher', 'homedepot'] as const;
const BUDGETS_STORAGE_KEY = 'kewe.budgets';

type StoredAllocation = { businessDimensionId?: string };
type StoredBudget = { businessDimensionId?: string; allocations?: StoredAllocation[] };

export function CreateRequisitionPage() {
  const [draft, setDraft] = useState<RequisitionDraft | null>(null);
  const [prompt, setPrompt] = useState('');
  const [status, setStatus] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, SupplierResult[]>>({ amazon: [], fisher: [], homedepot: [] });
  const [links, setLinks] = useState<Record<string, string>>({});
  const [warnings, setWarnings] = useState<string[]>([]);
  const [activeSupplier, setActiveSupplier] = useState<(typeof SUPPLIERS)[number]>('fisher');
  const [chargingLocations, setChargingLocations] = useState<ChargingLocation[]>([]);
  const [funding, setFunding] = useState<any>(null);

  useEffect(() => {
    void createRequisitionDraft().then(setDraft);
    void loadChargingLocations();
  }, []);

  async function loadChargingLocations() {
    const [backendLocations, budgetDimensionIds] = await Promise.all([
      fetchChargingLocations(),
      loadBudgetDimensionIds(),
    ]);

    const locationById = new Map(backendLocations.map((location) => [location.id, location]));
    const missingBudgetIds = budgetDimensionIds.filter((id) => !locationById.has(id));

    if (missingBudgetIds.length > 0) {
      try {
        const businessDimensions = await fetchBusinessObjects();
        businessDimensions
          .filter((dimension) => missingBudgetIds.includes(dimension.id))
          .forEach((dimension) => {
            locationById.set(dimension.id, toChargingLocation(dimension));
          });
      } catch {
        // Keep backend locations only when business dimensions cannot be loaded.
      }
    }

    const allowedIds = new Set([...backendLocations.map((location) => location.id), ...budgetDimensionIds]);
    const mergedLocations = [...locationById.values()]
      .filter((location) => allowedIds.has(location.id))
      .sort((left, right) => `${left.code} ${left.name}`.localeCompare(`${right.code} ${right.name}`));

    setChargingLocations(mergedLocations);
  }

  async function loadBudgetDimensionIds(): Promise<string[]> {
    if (typeof window === 'undefined') {
      return [];
    }

    const rawBudgets = window.localStorage.getItem(BUDGETS_STORAGE_KEY);
    if (!rawBudgets) {
      return [];
    }

    try {
      const parsedBudgets = JSON.parse(rawBudgets);
      if (!Array.isArray(parsedBudgets)) {
        return [];
      }

      const ids = new Set<string>();
      (parsedBudgets as StoredBudget[]).forEach((budget) => {
        if (budget.businessDimensionId) {
          ids.add(budget.businessDimensionId);
        }
        budget.allocations?.forEach((allocation) => {
          if (allocation.businessDimensionId) {
            ids.add(allocation.businessDimensionId);
          }
        });
      });

      return [...ids];
    } catch {
      return [];
    }
  }

  const subtotal = useMemo(() => draft?.lines.reduce((sum, line) => sum + (line.amount || 0), 0) ?? 0, [draft]);

  useEffect(() => {
    if (!draft?.id) return;
    const t = setTimeout(() => void updateRequisitionDraft(draft.id, { ...draft, totals: { subtotal } }), 500);
    return () => clearTimeout(t);
  }, [draft, subtotal]);

  useEffect(() => {
    if (!draft?.chargingBusinessDimensionId) return;
    void fetchFundingSnapshot(draft.chargingBusinessDimensionId, draft.budgetPlanId, subtotal).then(setFunding);
  }, [draft?.chargingBusinessDimensionId, draft?.budgetPlanId, subtotal]);

  async function runAgent() {
    if (!prompt.trim() || !draft) return;
    setWarnings([]);
    try {
      setStatus('Parsing request…');
      await new Promise((r) => setTimeout(r, 300));
      setStatus('Searching suppliers…');
      const response = await agentDraftRequisition(prompt);
      setStatus('Building draft…');
      setResults(response.results);
      setLinks(response.searchLinks);
      setWarnings(response.warnings);
      setDraft({
        ...draft,
        title: response.draft.title,
        memo: response.draft.memo,
        currency: response.draft.currency,
        chargingBusinessDimensionId: response.suggestedChargingDimension?.id,
        chargingBusinessDimensionCode: response.suggestedChargingDimension?.code,
        chargingBusinessDimensionName: response.suggestedChargingDimension?.name,
        lines: response.draft.lines,
      });
    } catch {
      setWarnings(['Could not draft requisition. Verify backend is running and retry.']);
      setResults({ amazon: [], fisher: [], homedepot: [] });
      setLinks({});
    } finally {
      setStatus(null);
    }
  }

  function addResult(result: SupplierResult) {
    if (!draft) return;
    const existing = draft.lines.find((line) => line.supplierSku && line.supplierSku === result.sku && line.supplierName === result.supplier);
    let lines: RequisitionLine[];
    if (existing) {
      lines = draft.lines.map((line) => line === existing
        ? { ...line, quantity: line.quantity + 1, amount: (line.quantity + 1) * (line.unitPrice ?? 0) }
        : line);
    } else {
      const unitPrice = result.price ?? 0;
      lines = [...draft.lines, {
        lineNumber: draft.lines.length + 1,
        description: result.title,
        quantity: 1,
        uom: 'ea',
        unitPrice,
        amount: unitPrice,
        supplierName: result.supplier,
        supplierUrl: result.url,
        supplierSku: result.sku,
      }];
    }
    setDraft({ ...draft, lines });
  }

  if (!draft) return <div className="card">Loading draft…</div>;

  return (
    <div className="card requisitions-page">
      <h2>Create Requisition</h2>
      <textarea value={prompt} placeholder="e.g., I need to purchase 6 5ml glass beakers for biology" onChange={(e) => setPrompt(e.target.value)} rows={3} />
      <div className="button-row">
        <button type="button" onClick={() => void runAgent()}>Find Items & Draft Requisition</button>
        <button type="button" className="secondary" onClick={() => setDraft({ ...draft, lines: [] })}>Clear Draft</button>
      </div>
      {status && <p>{status}</p>}
      {warnings.map((warning) => <p key={warning} className="text-danger">{warning}</p>)}

      <div className="grid-two">
        <section>
          <h3>Results</h3>
          <div className="pill-row">{SUPPLIERS.map((s) => <button key={s} type="button" className={activeSupplier === s ? 'active' : ''} onClick={() => setActiveSupplier(s)}>{s}</button>)}</div>
          {(results[activeSupplier] ?? []).map((result) => (
            <div className="card" key={`${result.supplier}-${result.url}`}>
              <strong>{result.title}</strong>
              <div>{result.supplier} {result.price ? `• $${result.price.toFixed(2)}` : '• price n/a'}</div>
              <a href={result.url} target="_blank" rel="noreferrer">Open</a>
              <button type="button" onClick={() => addResult(result)}>Add to Requisition</button>
            </div>
          ))}
          {(results[activeSupplier] ?? []).length === 0 && links[activeSupplier] && <a href={links[activeSupplier]} target="_blank" rel="noreferrer">Open {activeSupplier} search</a>}
        </section>

        <section>
          <h3>Funding Snapshot</h3>
          <label>Charging Location</label>
          <select value={draft.chargingBusinessDimensionId ?? ''} onChange={(e) => setDraft({ ...draft, chargingBusinessDimensionId: e.target.value })}>
            <option value="">Select charging location</option>
            {chargingLocations.map((c) => <option key={c.id} value={c.id}>{`${c.code} ${c.name}`}</option>)}
          </select>
          <label>Budget Plan</label>
          <input value={draft.budgetPlanId ?? 'FY26-OPERATING'} onChange={(e) => setDraft({ ...draft, budgetPlanId: e.target.value })} />
          <div>Budget Total: {funding?.totals?.budgetTotal ?? '—'}</div>
          <div>Allocated: {funding?.totals?.allocatedFromTotal ?? 0}</div>
          <div>After this req: {funding?.totals?.remainingIfBudget ?? 'No budget found for this charging location in this plan'}</div>
        </section>
      </div>

      <h3>Header</h3>
      <input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} placeholder="Requisition title" />
      <input value={draft.requesterName} onChange={(e) => setDraft({ ...draft, requesterName: e.target.value })} placeholder="Requester" />
      <input value={draft.memo ?? ''} onChange={(e) => setDraft({ ...draft, memo: e.target.value })} placeholder="Business purpose / memo" />

      <h3>Lines</h3>
      <table>
        <thead><tr><th>#</th><th>Description</th><th>Qty</th><th>UOM</th><th>Unit Price</th><th>Amount</th><th>Supplier</th><th>Link</th><th>Actions</th></tr></thead>
        <tbody>
          {draft.lines.map((line, idx) => (
            <tr key={`${line.lineNumber}-${idx}`}>
              <td>{idx + 1}</td>
              <td><input value={line.description} onChange={(e) => {
                const lines = [...draft.lines]; lines[idx] = { ...line, description: e.target.value }; setDraft({ ...draft, lines });
              }} /></td>
              <td><input type="number" value={line.quantity} onChange={(e) => {
                const q = Number(e.target.value); const lines = [...draft.lines]; lines[idx] = { ...line, quantity: q, amount: q * (line.unitPrice ?? 0) }; setDraft({ ...draft, lines });
              }} /></td>
              <td><input value={line.uom ?? ''} onChange={(e) => { const lines = [...draft.lines]; lines[idx] = { ...line, uom: e.target.value }; setDraft({ ...draft, lines }); }} /></td>
              <td><input type="number" value={line.unitPrice ?? 0} onChange={(e) => {
                const p = Number(e.target.value); const lines = [...draft.lines]; lines[idx] = { ...line, unitPrice: p, amount: p * line.quantity }; setDraft({ ...draft, lines });
              }} /></td>
              <td>{line.amount.toFixed(2)}</td>
              <td>{line.supplierName}</td>
              <td>{line.supplierUrl ? <a href={line.supplierUrl} target="_blank" rel="noreferrer">Link</a> : '-'}</td>
              <td><button type="button" onClick={() => setDraft({ ...draft, lines: draft.lines.filter((_, i) => i !== idx).map((item, i) => ({ ...item, lineNumber: i + 1 })) })}>Delete</button></td>
            </tr>
          ))}
        </tbody>
      </table>
      <strong>Subtotal: ${subtotal.toFixed(2)}</strong>
    </div>
  );
}

function toChargingLocation(dimension: BusinessObjectInstance): ChargingLocation {
  return {
    id: dimension.id,
    code: dimension.code,
    name: dimension.name,
    typeName: dimension.typeCode,
    status: dimension.status,
  };
}
