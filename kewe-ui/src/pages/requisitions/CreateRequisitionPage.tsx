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
  if (value === undefined || value === null || Number.isNaN(value)) return '—';
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
  const [allLinesChargingInstructions, setAllLinesChargingInstructions] = useState('');

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
    void fetchFundingSnapshot(draft.chargingBusinessDimensionId, draft.budgetPlanId, subtotal).then(setFunding);
  }, [draft?.chargingBusinessDimensionId, draft?.budgetPlanId, subtotal]);

  function chargingInstructionTemplate(nextDraft: RequisitionDraft): string {
    if (!nextDraft.chargingBusinessDimensionCode) return '';
    return `Charge to ${nextDraft.chargingBusinessDimensionCode} ${nextDraft.chargingBusinessDimensionName ?? ''}`.trim();
  }

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
      let nextDraft = draft;
      if (!draft.chargingBusinessDimensionId && response.suggestedChargingLocation) {
        nextDraft = {
          ...draft,
          chargingBusinessDimensionId: response.suggestedChargingLocation.id,
          chargingBusinessDimensionCode: response.suggestedChargingLocation.code,
          chargingBusinessDimensionName: response.suggestedChargingLocation.name,
        };
        setDraft(nextDraft);
      }
      setAllLinesChargingInstructions(chargingInstructionTemplate(nextDraft));
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
      chargingInstructions: allLinesChargingInstructions || chargingInstructionTemplate(draft),
    }];
    setDraft({ ...draft, lines });
  }

  function addManualLine() {
    if (!draft) return;
    const lines: RequisitionLine[] = [...draft.lines, {
      lineNumber: draft.lines.length + 1,
      description: '',
      quantity: 1,
      uom: 'ea',
      amount: 0,
      supplierName: 'Manual',
      chargingInstructions: allLinesChargingInstructions || chargingInstructionTemplate(draft),
    }];
    setDraft({ ...draft, lines });
  }

  function applyChargingInstructionsToAllLines() {
    if (!draft) return;
    const lines = draft.lines.map((line) => ({ ...line, chargingInstructions: allLinesChargingInstructions }));
    setDraft({ ...draft, lines });
  }

  if (!draft) return <div className="card">Loading draft…</div>;

  return (
    <div className="requisitions-page page-section">
      <div className="card section-card">
        <h2>Requisition Assistant</h2>
        <p className="subtle">Use AI suggestions to draft requisition lines. You can also add lines manually.</p>
        <textarea value={prompt} placeholder="e.g., I need to purchase 6 5ml glass beakers for biology" onChange={(e) => setPrompt(e.target.value)} rows={3} />
        <div className="button-row">
          <button type="button" onClick={() => void runAgent()}>Find Items</button>
          <button type="button" className="btn-secondary" onClick={addManualLine}>Add Line</button>
          <button type="button" className="btn-secondary" onClick={() => { setDraft({ ...draft, lines: [] }); setResults([]); setWarnings([]); }}>Clear Draft</button>
        </div>
        <div>{status ?? ''}</div>
        {warnings.map((warning) => <p key={warning} className="text-danger">{warning}</p>)}
        <details>
          <summary>Dev</summary>
          {capabilities && <p>Provider: {capabilities.provider} • Key: {String(capabilities.hasKey)} • Engine: {capabilities.engine} • Count: {capabilities.count}</p>}
        </details>
      </div>

      <div className="grid-two">
        <section className="page-section">
          <div className="card section-card">
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

          <div className="card table-card">
            <div className="row" style={{ marginBottom: 10 }}>
              <h3 style={{ margin: 0 }}>Requisition Lines</h3>
              <button type="button" className="btn-secondary" onClick={addManualLine}>Add line</button>
            </div>
            <div className="row" style={{ gap: 8, marginBottom: 10 }}>
              <input
                value={allLinesChargingInstructions}
                placeholder="Charging instructions for all lines"
                onChange={(e) => setAllLinesChargingInstructions(e.target.value)}
              />
              <button type="button" className="btn-secondary" onClick={applyChargingInstructionsToAllLines}>Apply to all lines</button>
            </div>
            <table>
              <thead><tr><th>#</th><th>Description</th><th>Qty</th><th>UOM</th><th>Unit Price</th><th>Amount</th><th>Supplier</th><th>Charging Instructions</th><th>Link</th><th>Actions</th></tr></thead>
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
                    <td><input value={line.chargingInstructions ?? ''} onChange={(e) => { const lines = [...draft.lines]; lines[idx] = { ...line, chargingInstructions: e.target.value }; setDraft({ ...draft, lines }); }} /></td>
                    <td>{line.supplierUrl ? <a href={line.supplierUrl} target="_blank" rel="noreferrer">Link</a> : '-'}</td>
                    <td><button type="button" onClick={() => setDraft({ ...draft, lines: draft.lines.filter((_, i) => i !== idx).map((item, i) => ({ ...item, lineNumber: i + 1 })) })}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
            <strong>Subtotal: {formatMoney(subtotal)}</strong>
          </div>

          <div className="card table-card">
            <h3>Funding Snapshot</h3>
            <table>
              <thead>
                <tr>
                  <th>Funding Location</th>
                  <th>Charging Location</th>
                  <th>Proposed Charging Amount</th>
                  <th>Projected Funding Available</th>
                </tr>
              </thead>
              <tbody>
                {(funding?.fundingSources.length ? funding.fundingSources : [{ proposedChargeAmount: subtotal }]).map((source, idx) => (
                  <tr key={idx}>
                    <td>{source.fundingLocation ? `${source.fundingLocation.code} ${source.fundingLocation.name}` : 'Direct budget'}</td>
                    <td>{source.chargingLocation ? `${source.chargingLocation.code} ${source.chargingLocation.name}` : (draft.chargingBusinessDimensionCode ? `${draft.chargingBusinessDimensionCode} ${draft.chargingBusinessDimensionName ?? ''}` : '—')}</td>
                    <td>{formatMoney(source.proposedChargeAmount)}</td>
                    <td>
                      {source.projectedChargingAvailable !== undefined || source.projectedFundingAvailable !== undefined
                        ? `${formatMoney(source.projectedChargingAvailable)} / ${formatMoney(source.projectedFundingAvailable)}`
                        : formatMoney(funding?.totals.chargingRemainingBeforeReq)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="page-section">
          <div className="card section-card">
            <h3>Funding Controls</h3>
            <label>Charging Location</label>
            <select
              value={draft.chargingBusinessDimensionId ?? ''}
              onChange={(e) => {
                const selected = chargingLocations.find((item) => item.id === e.target.value);
                const nextDraft = {
                  ...draft,
                  chargingBusinessDimensionId: selected?.id,
                  chargingBusinessDimensionCode: selected?.code,
                  chargingBusinessDimensionName: selected?.name,
                };
                setDraft(nextDraft);
                setAllLinesChargingInstructions(chargingInstructionTemplate(nextDraft));
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
            <div>Allocated Out: {formatMoney(funding?.totals?.allocatedFromTotal)}</div>
            <div>Allocated In: {formatMoney(funding?.totals?.allocatedToTotal)}</div>
            <div>Remaining (before req): {formatMoney(funding?.totals?.chargingRemainingBeforeReq)}</div>
          </div>

          <div className="card section-card">
            <h3>Requisition Information</h3>
            <input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} placeholder="Requisition title" />
            <input value={draft.requesterName} onChange={(e) => setDraft({ ...draft, requesterName: e.target.value })} placeholder="Requester" />
            <input value={draft.memo ?? ''} onChange={(e) => setDraft({ ...draft, memo: e.target.value })} placeholder="Business purpose / memo" />
          </div>
        </section>
      </div>
    </div>
  );
}
