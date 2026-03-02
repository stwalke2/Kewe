import { useEffect, useMemo, useState } from 'react';
import {
  agentDraftRequisition,
  createRequisitionDraft,
  fetchAgentCapabilities,
  fetchChargingLocations,
  fetchFundingSnapshot,
  updateRequisitionDraft,
} from '../../api';
import type {
  AgentCapabilities,
  ChargingLocation,
  ProductSuggestion,
  RequisitionDraft,
  RequisitionLine,
} from '../../api/types';

function formatMoney(value?: number): string {
  if (value === undefined || value === null || Number.isNaN(value)) return '—';
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}


type FundingRow = {
  fundingLocation: string;
  chargingLocation: string;
  proposedAmount: number;
  projected: string;
};

function displayLocation(location?: ChargingLocation): string {
  return location ? `${location.code} — ${location.name}` : '—';
}

function inferChargingLocationFromPrompt(promptText: string, eligibleLocations: ChargingLocation[]): ChargingLocation | undefined {
  const promptValue = promptText.trim();
  if (!promptValue) return undefined;

  const chargeToMatch = promptValue.match(/charge to\s+([^.,;\n]+)/i);
  const inferredText = (chargeToMatch?.[1] ?? promptValue).trim();

  const codeMatch = eligibleLocations.find((location) => location.code.toLowerCase() === inferredText.toLowerCase());
  if (codeMatch) return codeMatch;

  const lowered = inferredText.toLowerCase();
  return eligibleLocations.find((location) => location.name.toLowerCase().includes(lowered));
}

export function CreateRequisitionPage() {
  const [draft, setDraft] = useState<RequisitionDraft | null>(null);
  const [prompt, setPrompt] = useState('');
  const [status, setStatus] = useState<string | null>(null);
  const [results, setResults] = useState<ProductSuggestion[]>([]);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [chargingLocations, setChargingLocations] = useState<ChargingLocation[]>([]);
  const [capabilities, setCapabilities] = useState<AgentCapabilities | null>(null);
  const [assistantLocation, setAssistantLocation] = useState<ChargingLocation | null>(null);
  const [allLinesChargingLocationId, setAllLinesChargingLocationId] = useState('');
  const [resultQuantities, setResultQuantities] = useState<Record<string, number>>({});
  const [fundingRows, setFundingRows] = useState<FundingRow[]>([]);

  useEffect(() => {
    void createRequisitionDraft().then(setDraft);
    void fetchAgentCapabilities().then(setCapabilities).catch(() => setCapabilities(null));
    void fetchChargingLocations().then(setChargingLocations);
  }, []);

  const subtotal = useMemo(() => draft?.lines.reduce((sum, line) => sum + (line.amount || 0), 0) ?? 0, [draft]);

  useEffect(() => {
    if (!draft?.id) return;
    const t = setTimeout(() => void updateRequisitionDraft(draft.id, { ...draft, totals: { subtotal } }), 500);
    return () => clearTimeout(t);
  }, [draft, subtotal]);

  useEffect(() => {
    if (!draft) {
      setFundingRows([]);
      return;
    }

    const groupedAmounts = draft.lines.reduce<Record<string, number>>((acc, line) => {
      if (!line.chargingBusinessDimensionId) return acc;
      acc[line.chargingBusinessDimensionId] = (acc[line.chargingBusinessDimensionId] ?? 0) + (line.amount || 0);
      return acc;
    }, {});

    const locationIds = Object.keys(groupedAmounts);
    if (!locationIds.length) {
      setFundingRows([]);
      return;
    }

    void Promise.all(
      locationIds.map(async (id) => ({
        id,
        amount: groupedAmounts[id],
        snapshot: await fetchFundingSnapshot(id, undefined, groupedAmounts[id]),
      })),
    ).then((rows) => {
      const nextRows: FundingRow[] = [];
      rows.forEach(({ amount, snapshot }) => {
        if (snapshot.fundingSources.length) {
          snapshot.fundingSources.forEach((source) => {
            const projected = `Charging rem ${formatMoney(source.projectedChargingAvailable)} • Source rem ${formatMoney(source.projectedFundingAvailable)}`;
            nextRows.push({
              fundingLocation: displayLocation(source.fundingLocation),
              chargingLocation: displayLocation(source.chargingLocation ?? snapshot.chargingDimension),
              proposedAmount: amount,
              projected,
            });
          });
          return;
        }

        nextRows.push({
          fundingLocation: displayLocation(snapshot.chargingDimension),
          chargingLocation: displayLocation(snapshot.chargingDimension),
          proposedAmount: amount,
          projected: `Remaining ${formatMoney(snapshot.totals.chargingRemainingBeforeReq === undefined ? undefined : snapshot.totals.chargingRemainingBeforeReq - amount)}`,
        });
      });
      setFundingRows(nextRows);
    });
  }, [draft]);


  function selectedLocation(locationId?: string): ChargingLocation | undefined {
    return chargingLocations.find((item) => item.id === locationId);
  }

  function applyDefaultCharging(line: RequisitionLine): RequisitionLine {
    const location = assistantLocation;
    if (!location) return line;
    return {
      ...line,
      chargingBusinessDimensionId: location.id,
      chargingBusinessDimensionCode: location.code,
      chargingBusinessDimensionName: location.name,
    };
  }

  async function runAgent() {
    if (!prompt.trim() || !draft) return;
    setWarnings([]);
    setStatus('Searching items…');
    try {
      const response = await agentDraftRequisition(prompt);
      setResults(response.results);
      const quantities: Record<string, number> = {};
      response.results.forEach((result) => { quantities[result.url] = 1; });
      setResultQuantities(quantities);
      setWarnings(response.warnings);

      const inferred = response.suggestedChargingLocation
        ? chargingLocations.find((location) => location.id === response.suggestedChargingLocation?.id)
        : inferChargingLocationFromPrompt(prompt, chargingLocations);

      if (inferred) {
        setAssistantLocation(inferred);
      }

      if (!draft.chargingBusinessDimensionId && inferred) {
        setDraft({
          ...draft,
          chargingBusinessDimensionId: inferred.id,
          chargingBusinessDimensionCode: inferred.code,
          chargingBusinessDimensionName: inferred.name,
        });
      }

      if (inferred) {
        setAllLinesChargingLocationId(inferred.id);
      }
    } catch {
      setWarnings(['Could not draft requisition. Verify backend is running and retry.']);
      setResults([]);
    } finally {
      setStatus(null);
    }
  }

  function addResult(result: ProductSuggestion) {
    if (!draft) return;
    const quantity = Math.max(1, Number(resultQuantities[result.url] ?? 1));
    const unitPrice = result.price;
    const line: RequisitionLine = applyDefaultCharging({
      lineNumber: draft.lines.length + 1,
      description: result.title,
      quantity,
      uom: 'ea',
      unitPrice,
      amount: (unitPrice ?? 0) * quantity,
      supplierName: result.supplier,
      supplierUrl: result.url,
    });
    setDraft({ ...draft, lines: [...draft.lines, line] });
  }

  function addManualLine() {
    if (!draft) return;
    const line = applyDefaultCharging({
      lineNumber: draft.lines.length + 1,
      description: '',
      quantity: 1,
      uom: 'ea',
      amount: 0,
      supplierName: 'Manual',
    });
    setDraft({ ...draft, lines: [...draft.lines, line] });
  }

  function applyChargingLocationToAllLines() {
    if (!draft || !allLinesChargingLocationId) return;
    const selected = selectedLocation(allLinesChargingLocationId);
    if (!selected) return;
    const lines = draft.lines.map((line) => ({
      ...line,
      chargingBusinessDimensionId: selected.id,
      chargingBusinessDimensionCode: selected.code,
      chargingBusinessDimensionName: selected.name,
    }));
    setDraft({ ...draft, lines });
  }


  if (!draft) return <div className="card">Loading draft…</div>;

  return (
    <div className="requisitions-page page-section">
      <div className="card section-card requisition-stack">
        <section className="card section-card">
          <h3>Purchasing Assistant</h3>
          <textarea
            value={prompt}
            placeholder="e.g., I need to purchase 6 5ml glass beakers and charge to Biology"
            onChange={(e) => setPrompt(e.target.value)}
            rows={4}
          />
          <div className="button-row">
            <button type="button" onClick={() => void runAgent()}>Find Items</button>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => { setPrompt(''); setResults([]); setWarnings([]); setAssistantLocation(null); }}
            >
              Clear Draft
            </button>
          </div>
          {status ? <p className="subtle">{status}</p> : null}
          {warnings.map((warning) => <p key={warning} className="text-danger">{warning}</p>)}
          {assistantLocation ? <p className="subtle">Inferred charging location: {displayLocation(assistantLocation)}</p> : null}
          {capabilities ? <p className="subtle">Agent: {capabilities.provider} ({capabilities.engine})</p> : null}
        </section>

        {results.length > 0 ? (
          <section className="card section-card">
            <h3>Suggested Items</h3>
            <div className="suggested-items-row">
              {results.map((result) => (
                <article className="suggested-item-card" key={result.url}>
                  <p className="subtle suggested-supplier">{result.supplier}</p>
                  {result.imageUrl ? <img src={result.imageUrl} alt={result.title} className="suggested-image" /> : <div className="suggested-image-placeholder">No image</div>}
                  <div className="suggested-title">{result.title}</div>
                  <strong>{formatMoney(result.price)}</strong>
                  <div className="suggested-controls">
                    <input
                      type="number"
                      min={1}
                      value={resultQuantities[result.url] ?? 1}
                      onChange={(e) => setResultQuantities({ ...resultQuantities, [result.url]: Math.max(1, Number(e.target.value) || 1) })}
                    />
                    <button type="button" onClick={() => addResult(result)}>+</button>
                  </div>
                </article>
              ))}
            </div>
          </section>
        ) : null}

        <section className="card section-card table-card">
          <div className="line-table-header">
            <h3>Requisition Lines</h3>
            <button type="button" className="btn-secondary" onClick={addManualLine}>Add Line</button>
          </div>

          <div className="bulk-controls">
            <label>
              Charging location for all lines
              <select value={allLinesChargingLocationId} onChange={(e) => setAllLinesChargingLocationId(e.target.value)}>
                <option value="">Select charging location</option>
                {chargingLocations.map((location) => (
                  <option key={location.id} value={location.id}>{displayLocation(location)}</option>
                ))}
              </select>
            </label>
            <button type="button" className="btn-secondary" onClick={applyChargingLocationToAllLines}>Apply to all lines</button>
          </div>

          <div className="table-scroll-wrap">
            <table>
              <thead>
                <tr>
                  <th>Description</th>
                  <th>Qty</th>
                  <th>UOM</th>
                  <th>Unit Price</th>
                  <th>Amount</th>
                  <th>Supplier</th>
                  <th>Link</th>
                  <th>Charging Location</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {draft.lines.map((line, idx) => (
                  <tr key={`${line.lineNumber}-${idx}`}>
                    <td><input value={line.description} onChange={(e) => {
                      const lines = [...draft.lines]; lines[idx] = { ...line, description: e.target.value }; setDraft({ ...draft, lines });
                    }} /></td>
                    <td><input type="number" min={0} value={line.quantity} onChange={(e) => {
                      const quantity = Number(e.target.value) || 0;
                      const lines = [...draft.lines];
                      lines[idx] = { ...line, quantity, amount: quantity * (line.unitPrice ?? 0) };
                      setDraft({ ...draft, lines });
                    }} /></td>
                    <td><input value={line.uom ?? ''} onChange={(e) => {
                      const lines = [...draft.lines]; lines[idx] = { ...line, uom: e.target.value }; setDraft({ ...draft, lines });
                    }} /></td>
                    <td><input type="number" min={0} value={line.unitPrice ?? ''} onChange={(e) => {
                      const unitPrice = Number(e.target.value);
                      const lines = [...draft.lines];
                      lines[idx] = {
                        ...line,
                        unitPrice: Number.isNaN(unitPrice) ? undefined : unitPrice,
                        amount: (Number.isNaN(unitPrice) ? 0 : unitPrice) * line.quantity,
                      };
                      setDraft({ ...draft, lines });
                    }} /></td>
                    <td>{formatMoney(line.amount)}</td>
                    <td><input value={line.supplierName} onChange={(e) => {
                      const lines = [...draft.lines]; lines[idx] = { ...line, supplierName: e.target.value }; setDraft({ ...draft, lines });
                    }} /></td>
                    <td>{line.supplierUrl ? <a href={line.supplierUrl} target="_blank" rel="noreferrer">Open</a> : '—'}</td>
                    <td>
                      <select value={line.chargingBusinessDimensionId ?? ''} onChange={(e) => {
                        const selected = selectedLocation(e.target.value);
                        const lines = [...draft.lines];
                        lines[idx] = {
                          ...line,
                          chargingBusinessDimensionId: selected?.id,
                          chargingBusinessDimensionCode: selected?.code,
                          chargingBusinessDimensionName: selected?.name,
                        };
                        setDraft({ ...draft, lines });
                      }}>
                        <option value="">Select</option>
                        {chargingLocations.map((location) => (
                          <option key={location.id} value={location.id}>{displayLocation(location)}</option>
                        ))}
                      </select>
                    </td>
                    <td><button type="button" onClick={() => {
                      setDraft({
                        ...draft,
                        lines: draft.lines.filter((_, i) => i !== idx).map((item, i) => ({ ...item, lineNumber: i + 1 })),
                      });
                    }}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <strong>Subtotal: {formatMoney(subtotal)}</strong>
          <p className="subtle">Dev: charging locations loaded = {chargingLocations.length}</p>
        </section>

        <section className="card section-card table-card">
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
              {fundingRows.length ? fundingRows.map((row, idx) => (
                <tr key={`${row.fundingLocation}-${row.chargingLocation}-${idx}`}>
                  <td>{row.fundingLocation}</td>
                  <td>{row.chargingLocation}</td>
                  <td>{formatMoney(row.proposedAmount)}</td>
                  <td>{row.projected}</td>
                </tr>
              )) : (
                <tr>
                  <td colSpan={4}>Assign charging locations to requisition lines to see funding impact.</td>
                </tr>
              )}
            </tbody>
          </table>
        </section>

        <section className="card section-card">
          <h3>Requisition Information</h3>
          <input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} placeholder="Requisition name" />
          <input value={draft.requesterName} onChange={(e) => setDraft({ ...draft, requesterName: e.target.value })} placeholder="Requestor" />
          <input value={draft.memo ?? ''} onChange={(e) => setDraft({ ...draft, memo: e.target.value })} placeholder="Business purpose / memo" />
        </section>
      </div>
    </div>
  );
}
