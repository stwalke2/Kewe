import { useEffect, useMemo, useState } from 'react';
import {
  agentDraftRequisition,
  createRequisitionDraft,
  fetchAgentCapabilities,
  fetchChargingLocations,
  fetchFundingSnapshot,
  updateRequisitionDraft,
} from '../../api';
import type { AgentCapabilities, ChargingLocation, FundingSnapshot, ProductSuggestion, RequisitionDraft, RequisitionLine } from '../../api/types';

function formatMoney(value?: number): string {
  if (value === undefined || value === null) return '—';
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

const budgetPlans = ['FY26 Operating', 'FY26 Capital', 'FY25 Operating'];

export function CreateRequisitionPage() {
  const [draft, setDraft] = useState<RequisitionDraft | null>(null);
  const [prompt, setPrompt] = useState('');
  const [status, setStatus] = useState<string | null>(null);
  const [results, setResults] = useState<ProductSuggestion[]>([]);
  const [links, setLinks] = useState<Record<string, string>>({});
  const [warnings, setWarnings] = useState<string[]>([]);
  const [chargingLocations, setChargingLocations] = useState<ChargingLocation[]>([]);
  const [funding, setFunding] = useState<FundingSnapshot | null>(null);
  const [capabilities, setCapabilities] = useState<AgentCapabilities | null>(null);
  const [parsedQuantity, setParsedQuantity] = useState(1);

  useEffect(() => {
    void createRequisitionDraft().then((nextDraft) => {
      setDraft(nextDraft.budgetPlanId ? nextDraft : { ...nextDraft, budgetPlanId: 'FY26 Operating' });
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
    setStatus('Searching web results…');
    try {
      const response = await agentDraftRequisition(prompt);
      setResults(response.results);
      setLinks(response.searchLinks);
      setWarnings(response.warnings);
      setParsedQuantity(response.parsed.quantity || 1);
      if (!draft.chargingBusinessDimensionId && response.suggestedChargingLocation) {
        setDraft({
          ...draft,
          chargingBusinessDimensionId: response.suggestedChargingLocation.id,
          chargingBusinessDimensionCode: response.suggestedChargingLocation.code,
          chargingBusinessDimensionName: response.suggestedChargingLocation.name,
        });
      }
    } catch {
      setWarnings(['Could not draft requisition. Verify backend is running and retry.']);
      setResults([]);
      setLinks({});
    } finally {
      setStatus(null);
    }
  }

  function addResult(result: ProductSuggestion) {
    if (!draft) return;
    const unitPrice = result.price;
    const amount = unitPrice ? unitPrice * parsedQuantity : 0;
    const lines: RequisitionLine[] = [...draft.lines, {
      lineNumber: draft.lines.length + 1,
      description: result.title,
      quantity: parsedQuantity,
      uom: 'ea',
      unitPrice,
      amount,
      supplierName: result.supplier,
      supplierUrl: result.url,
    }];
    setDraft({ ...draft, lines });
  }

  if (!draft) return <div className="card">Loading draft…</div>;

  return (
    <div className="requisitions-page">
      <div className="card">
        <h2>Create Requisition</h2>
        <p>Draft requisitions faster with web search suggestions (no auto-purchase).</p>
        <textarea value={prompt} placeholder="e.g., I need to purchase 6 5ml glass beakers for biology" onChange={(e) => setPrompt(e.target.value)} rows={3} />
        <div className="button-row">
          <button type="button" onClick={() => void runAgent()}>Find Items & Draft Requisition</button>
          <button type="button" className="secondary" onClick={() => { setDraft({ ...draft, lines: [] }); setResults([]); setWarnings([]); }}>Clear Draft</button>
        </div>
        <div>{status ?? ''}</div>
        {warnings.map((warning) => <p key={warning} className="text-danger">{warning}</p>)}
        <details>
          <summary>Dev</summary>
          {capabilities && <p>Provider: {capabilities.provider} • Key: {String(capabilities.hasKey)} • Engine: {capabilities.engine} • Count: {capabilities.count}</p>}
        </details>
      </div>

      <div className="grid-two">
        <section>
          <div className="card">
            <h3>Suggested Items</h3>
            <small>
              <a href={links.amazon} target="_blank" rel="noreferrer">Amazon search</a> •{' '}
              <a href={links.fisher} target="_blank" rel="noreferrer">Fisher search</a> •{' '}
              <a href={links.homedepot} target="_blank" rel="noreferrer">Home Depot search</a>
            </small>
            {results.map((result) => (
              <div className="card" key={result.url}>
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                  <img src={result.imageUrl || 'https://placehold.co/64x64?text=No+Image'} alt={result.title} width={56} height={56} />
                  <div style={{ flex: 1 }}>
                    <strong>{result.title}</strong>
                    <div><small>{result.snippet}</small></div>
                  </div>
                  <div>
                    <span>{result.supplier}</span>
                    <div>{result.price ? formatMoney(result.price) : 'Price n/a'}</div>
                  </div>
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                  <a href={result.url} target="_blank" rel="noreferrer" title="Open item">↗</a>
                  <button type="button" title="Add to requisition" onClick={() => addResult(result)}>＋</button>
                </div>
              </div>
            ))}
          </div>

          <div className="card">
            <h3>Requisition Lines</h3>
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
                    <td><input type="number" value={line.unitPrice ?? ''} onChange={(e) => {
                      const p = Number(e.target.value); const lines = [...draft.lines]; lines[idx] = { ...line, unitPrice: Number.isNaN(p) ? undefined : p, amount: (Number.isNaN(p) ? 0 : p) * line.quantity }; setDraft({ ...draft, lines });
                    }} /></td>
                    <td>{line.amount.toFixed(2)}</td>
                    <td>{line.supplierName}</td>
                    <td>{line.supplierUrl ? <a href={line.supplierUrl} target="_blank" rel="noreferrer">Link</a> : '-'}</td>
                    <td><button type="button" onClick={() => setDraft({ ...draft, lines: draft.lines.filter((_, i) => i !== idx).map((item, i) => ({ ...item, lineNumber: i + 1 })) })}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <strong>Subtotal: {formatMoney(subtotal)}</strong>
          </div>
        </section>

        <section>
          <div className="card">
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
            <select value={draft.budgetPlanId ?? ''} onChange={(e) => setDraft({ ...draft, budgetPlanId: e.target.value })}>
              <option value="">Select budget plan</option>
              {budgetPlans.map((plan) => <option key={plan} value={plan}>{plan}</option>)}
            </select>
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
          </div>

          <div className="card">
            <h3>Header</h3>
            <input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} placeholder="Requisition title" />
            <input value={draft.requesterName} onChange={(e) => setDraft({ ...draft, requesterName: e.target.value })} placeholder="Requester" />
            <input value={draft.memo ?? ''} onChange={(e) => setDraft({ ...draft, memo: e.target.value })} placeholder="Business purpose / memo" />
          </div>
        </section>
      </div>
    </div>
  );
}
