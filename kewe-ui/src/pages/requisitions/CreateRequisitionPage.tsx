import { useEffect, useMemo, useState } from 'react';
import {
  agentDraftRequisition,
  createRequisitionDraft,
  fetchAgentCapabilities,
  fetchChargingLocations,
  fetchFundingSnapshot,
  updateRequisitionDraft,
} from '../../api';
import type { AgentCapabilities, ChargingLocation, FundingSnapshot, RequisitionDraft, RequisitionLine, SupplierResult } from '../../api/types';

const SUPPLIERS = ['amazon', 'fisher', 'homedepot'] as const;

function formatMoney(value?: number): string {
  if (value === undefined || value === null) return '—';
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export function CreateRequisitionPage() {
  const [draft, setDraft] = useState<RequisitionDraft | null>(null);
  const [prompt, setPrompt] = useState('');
  const [status, setStatus] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, SupplierResult[]>>({ amazon: [], fisher: [], homedepot: [] });
  const [links, setLinks] = useState<Record<string, string>>({});
  const [warnings, setWarnings] = useState<string[]>([]);
  const [activeSupplier, setActiveSupplier] = useState<(typeof SUPPLIERS)[number]>('fisher');
  const [chargingLocations, setChargingLocations] = useState<ChargingLocation[]>([]);
  const [funding, setFunding] = useState<FundingSnapshot | null>(null);
  const [stubMode, setStubMode] = useState(false);
  const [capabilities, setCapabilities] = useState<AgentCapabilities | null>(null);
  const [parsedQuantity, setParsedQuantity] = useState(1);

  useEffect(() => {
    void createRequisitionDraft().then((nextDraft) => {
      setDraft(nextDraft);
      if (!nextDraft.budgetPlanId) {
        setDraft({ ...nextDraft, budgetPlanId: 'FY26 Operating' });
      }
    });
    void fetchChargingLocations().then(setChargingLocations);
    void fetchAgentCapabilities().then(setCapabilities).catch(() => setCapabilities(null));
  }, []);

  const subtotal = useMemo(() => draft?.lines.reduce((sum, line) => sum + (line.amount || 0), 0) ?? 0, [draft]);

  useEffect(() => {
    if (!draft?.id) return;
    const t = setTimeout(() => void updateRequisitionDraft(draft.id, { ...draft, totals: { subtotal } }), 500);
    return () => clearTimeout(t);
  }, [draft, subtotal]);

  useEffect(() => {
    if (!draft?.chargingBusinessDimensionId) {
      setFunding(null);
      return;
    }
    void fetchFundingSnapshot(draft.chargingBusinessDimensionId, draft.budgetPlanId).then(setFunding);
  }, [draft?.chargingBusinessDimensionId, draft?.budgetPlanId]);

  async function runAgent() {
    if (!prompt.trim() || !draft) return;
    setWarnings([]);
    try {
      setStatus('Searching suppliers…');
      const response = await agentDraftRequisition(prompt, stubMode ? 'stub' : undefined);
      setResults(response.results);
      setLinks(response.searchLinks);
      setWarnings(response.warnings);
      setParsedQuantity(response.parsed.quantity || 1);
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
    const unitPrice = result.price ?? 0;
    const lines: RequisitionLine[] = [...draft.lines, {
      lineNumber: draft.lines.length + 1,
      description: result.title,
      quantity: parsedQuantity,
      uom: 'ea',
      unitPrice,
      amount: unitPrice * parsedQuantity,
      supplierName: result.supplierName,
      supplierUrl: result.url,
      supplierSku: result.sku,
    }];
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
      <label><input type="checkbox" checked={stubMode} onChange={(e) => setStubMode(e.target.checked)} /> Use stub results (dev)</label>
      {capabilities && <p>Capabilities: Provider {capabilities.provider} • Key {String(capabilities.hasKey)}</p>}
      {status && <p>{status}</p>}
      {warnings.map((warning) => <p key={warning} className="text-danger">{warning}</p>)}

      <div className="grid-two">
        <section>
          <h3>Results</h3>
          <div className="pill-row">{SUPPLIERS.map((s) => <button key={s} type="button" className={activeSupplier === s ? 'active' : ''} onClick={() => setActiveSupplier(s)}>{s}</button>)}</div>
          {(results[activeSupplier] ?? []).map((result) => (
            <div className="card" key={`${result.supplierName}-${result.url}`}>
              <strong>{result.title}</strong>
              <div>{result.supplierName} {result.price ? `• $${result.price.toFixed(2)}` : '• price n/a'}</div>
              {result.snippet && <small>{result.snippet}</small>}
              <a href={result.url} target="_blank" rel="noreferrer">Open</a>
              <button type="button" onClick={() => addResult(result)}>Add to Requisition</button>
            </div>
          ))}
          {(results[activeSupplier] ?? []).length === 0 && (
            <div className="card">
              <strong>No fetched results</strong>
              <div>Open supplier search directly.</div>
              {links[activeSupplier] && <a href={links[activeSupplier]} target="_blank" rel="noreferrer">Open {activeSupplier} search</a>}
            </div>
          )}
        </section>

        <section>
          <h3>Funding Snapshot</h3>
          <label>Charging Location</label>
          <select
            value={draft.chargingBusinessDimensionId ?? ''}
            onChange={(e) => {
              const selected = chargingLocations.find((item) => item.id === e.target.value);
              setDraft({
                ...draft,
                chargingBusinessDimensionId: selected?.id,
                chargingBusinessDimensionCode: selected?.code,
                chargingBusinessDimensionName: selected?.name,
              });
            }}
          >
            <option value="">Select charging location</option>
            {chargingLocations.map((c) => <option key={c.id} value={c.id}>{`${c.code} ${c.name}`}</option>)}
          </select>
          <label>Budget Plan</label>
          <input value={draft.budgetPlanId ?? ''} onChange={(e) => setDraft({ ...draft, budgetPlanId: e.target.value })} placeholder="FY26 Operating" />
          <div>Budget Total: {formatMoney(funding?.totals?.budgetTotal)}</div>
          <div>Allocated: {formatMoney(funding?.totals?.allocatedFromTotal)}</div>
          <div>Remaining (before req): {formatMoney(funding?.totals?.remainingBeforeReq)}</div>
          {funding?.allocationsFrom?.length ? (
            <ul>
              {funding.allocationsFrom.map((allocation) => (
                <li key={allocation.id}>{allocation.allocatedTo?.code} — {allocation.allocatedTo?.name}: {formatMoney(allocation.amount)}</li>
              ))}
            </ul>
          ) : <p>No allocations from this charging dimension.</p>}
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
