import { useEffect, useState } from 'react';
import { fetchFundingModelDebug, getErrorDetails } from '../api';
import type { ApiErrorDetails, FundingModelDebug } from '../api/types';

function valueOrDash(value: string | number | undefined | null): string {
  if (value === undefined || value === null || value === '') {
    return '-';
  }
  return String(value);
}

function formatNumber(value: number | undefined): string {
  if (value === undefined || Number.isNaN(value)) {
    return '-';
  }
  return value.toLocaleString('en-US', { maximumFractionDigits: 2 });
}

function formatDate(value: string | undefined): string {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function RawJsonCell({ rawJson }: { rawJson: string }) {
  return (
    <details>
      <summary>View JSON</summary>
      <pre className="debug-json">{rawJson}</pre>
    </details>
  );
}

export function FundingModelExplorerPage() {
  const [data, setData] = useState<FundingModelDebug | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiErrorDetails | null>(null);

  useEffect(() => {
    void load();
  }, []);

  async function load() {
    try {
      setLoading(true);
      setError(null);
      const payload = await fetchFundingModelDebug();
      setData(payload);
    } catch (requestError) {
      setError(getErrorDetails(requestError));
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return <section className="page-section"><article className="card">Loading funding model...</article></section>;
  }

  if (error) {
    return (
      <section className="page-section">
        <article className="message error">
          <strong>Unable to load funding model debug data.</strong>
          <div>{error.message}</div>
        </article>
      </section>
    );
  }

  if (!data) {
    return <section className="page-section"><article className="card">No data returned.</article></section>;
  }

  return (
    <section className="page-section explorer-page">
      <div className="page-header-row">
        <div>
          <h2>Funding Model Explorer</h2>
          <p>Debug view of persisted Mongo records and associations.</p>
        </div>
        <div className="header-actions">
          <button type="button" className="btn btn-secondary" onClick={() => void load()}>Refresh</button>
        </div>
      </div>

      <article className="card">
        <div className="summary-grid">
          <div className="summary-item"><span>Business Dimensions</span><strong>{data.counts.businessDimensionsCount}</strong></div>
          <div className="summary-item"><span>Budgets</span><strong>{data.counts.budgetsCount}</strong></div>
          <div className="summary-item"><span>Allocations</span><strong>{data.counts.allocationsCount}</strong></div>
          <div className="summary-item"><span>Requisitions</span><strong>{data.counts.requisitionsCount}</strong></div>
          <div className="summary-item"><span>Requisition Lines</span><strong>{data.counts.requisitionLinesCount}</strong></div>
          <div className="summary-item"><span>Eligible Charging Locations</span><strong>{data.counts.eligibleChargingLocationsCount}</strong></div>
        </div>
      </article>

      <article className="card">
        <h3>Relationship Checks</h3>
        {data.integrityWarnings.length === 0 ? (
          <p className="subtle">No integrity warnings.</p>
        ) : (
          <ul className="warning-list">
            {data.integrityWarnings.map((warning) => (
              <li key={warning}>{warning}</li>
            ))}
          </ul>
        )}
      </article>

      <article className="card table-card">
        <h3>Eligible Charging Locations</h3>
        <div className="explorer-subsection">
          <h4>Eligible from Budgets ({data.eligibleChargingLocations.eligibleFromBudgets.length})</h4>
          <div className="table-scroll-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Code</th><th>Name</th><th>Reason</th></tr>
              </thead>
              <tbody>
                {data.eligibleChargingLocations.eligibleFromBudgets.map((row) => (
                  <tr key={`eligible-budget-${row.id}`}>
                    <td>{valueOrDash(row.id)}</td>
                    <td>{valueOrDash(row.code)}</td>
                    <td>{valueOrDash(row.name)}</td>
                    <td>{row.eligibilityReason}</td>
                  </tr>
                ))}
                {data.eligibleChargingLocations.eligibleFromBudgets.length === 0 && <tr><td colSpan={4} className="subtle">None</td></tr>}
              </tbody>
            </table>
          </div>
        </div>

        <div className="explorer-subsection">
          <h4>Eligible from Allocation Destinations ({data.eligibleChargingLocations.eligibleFromAllocationDestinations.length})</h4>
          <div className="table-scroll-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Code</th><th>Name</th><th>Reason</th></tr>
              </thead>
              <tbody>
                {data.eligibleChargingLocations.eligibleFromAllocationDestinations.map((row) => (
                  <tr key={`eligible-allocation-${row.id}`}>
                    <td>{valueOrDash(row.id)}</td>
                    <td>{valueOrDash(row.code)}</td>
                    <td>{valueOrDash(row.name)}</td>
                    <td>{row.eligibilityReason}</td>
                  </tr>
                ))}
                {data.eligibleChargingLocations.eligibleFromAllocationDestinations.length === 0 && <tr><td colSpan={4} className="subtle">None</td></tr>}
              </tbody>
            </table>
          </div>
        </div>

        <div className="explorer-subsection">
          <h4>Final Union Set ({data.eligibleChargingLocations.finalEligibleCount})</h4>
          <div className="table-scroll-wrap">
            <table>
              <thead>
                <tr><th>ID</th><th>Code</th><th>Name</th><th>Why Eligible</th></tr>
              </thead>
              <tbody>
                {data.eligibleChargingLocations.finalUnionSet.map((row) => (
                  <tr key={`eligible-final-${row.id}`}>
                    <td>{valueOrDash(row.id)}</td>
                    <td>{valueOrDash(row.code)}</td>
                    <td>{valueOrDash(row.name)}</td>
                    <td>{row.eligibilityReason}</td>
                  </tr>
                ))}
                {data.eligibleChargingLocations.finalUnionSet.length === 0 && <tr><td colSpan={4} className="subtle">None</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      </article>

      <article className="card table-card">
        <h3>Business Dimensions</h3>
        <div className="table-scroll-wrap">
          <table className="explorer-table">
            <thead>
              <tr><th>ID</th><th>Code</th><th>Name</th><th>Type</th><th>Raw JSON</th></tr>
            </thead>
            <tbody>
              {data.businessDimensions.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{valueOrDash(row.code)}</td>
                  <td>{valueOrDash(row.name)}</td>
                  <td>{valueOrDash(row.type)}</td>
                  <td><RawJsonCell rawJson={row.rawJson} /></td>
                </tr>
              ))}
              {data.businessDimensions.length === 0 && <tr><td colSpan={5} className="subtle">No records</td></tr>}
            </tbody>
          </table>
        </div>
      </article>

      <article className="card table-card">
        <h3>Budgets</h3>
        <div className="table-scroll-wrap">
          <table className="explorer-table">
            <thead>
              <tr>
                <th>ID</th><th>Business Dimension ID</th><th>Business Dimension</th><th>Budget Plan</th>
                <th className="amount">Total Budget</th><th className="amount">Amount Used</th><th>Status</th><th>Raw JSON</th>
              </tr>
            </thead>
            <tbody>
              {data.budgets.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{valueOrDash(row.businessDimensionId)}</td>
                  <td>{valueOrDash(`${valueOrDash(row.businessDimensionCode)} ${valueOrDash(row.businessDimensionName)}`)}</td>
                  <td>{valueOrDash(row.budgetPlanName || row.budgetPlanId)}</td>
                  <td className="amount">{formatNumber(row.totalBudget)}</td>
                  <td className="amount">{formatNumber(row.amountUsed)}</td>
                  <td>{valueOrDash(row.status)}</td>
                  <td><RawJsonCell rawJson={row.rawJson} /></td>
                </tr>
              ))}
              {data.budgets.length === 0 && <tr><td colSpan={8} className="subtle">No records</td></tr>}
            </tbody>
          </table>
        </div>
      </article>

      <article className="card table-card">
        <h3>Allocations</h3>
        <div className="table-scroll-wrap">
          <table className="explorer-table">
            <thead>
              <tr>
                <th>ID</th><th>From Business Dimension ID</th><th>From Dimension</th><th>To Business Dimension ID</th><th>To Dimension</th>
                <th>Budget Plan</th><th className="amount">Allocated Amount</th><th className="amount">Amount Used</th><th>Status</th><th>Raw JSON</th>
              </tr>
            </thead>
            <tbody>
              {data.allocations.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{valueOrDash(row.fromBusinessDimensionId)}</td>
                  <td>{valueOrDash(`${valueOrDash(row.fromBusinessDimensionCode)} ${valueOrDash(row.fromBusinessDimensionName)}`)}</td>
                  <td>{valueOrDash(row.toBusinessDimensionId)}</td>
                  <td>{valueOrDash(`${valueOrDash(row.toBusinessDimensionCode)} ${valueOrDash(row.toBusinessDimensionName)}`)}</td>
                  <td>{valueOrDash(row.budgetPlanId)}</td>
                  <td className="amount">{formatNumber(row.allocatedAmount)}</td>
                  <td className="amount">{formatNumber(row.amountUsed)}</td>
                  <td>{valueOrDash(row.status)}</td>
                  <td><RawJsonCell rawJson={row.rawJson} /></td>
                </tr>
              ))}
              {data.allocations.length === 0 && <tr><td colSpan={10} className="subtle">No records</td></tr>}
            </tbody>
          </table>
        </div>
      </article>

      <article className="card table-card">
        <h3>Requisitions</h3>
        <div className="table-scroll-wrap">
          <table className="explorer-table">
            <thead>
              <tr><th>ID</th><th>Title</th><th>Requester</th><th>Memo</th><th>Created</th><th>Raw JSON</th></tr>
            </thead>
            <tbody>
              {data.requisitions.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{valueOrDash(row.title)}</td>
                  <td>{valueOrDash(row.requesterName)}</td>
                  <td>{valueOrDash(row.memo)}</td>
                  <td>{formatDate(row.createdAt)}</td>
                  <td><RawJsonCell rawJson={row.rawJson} /></td>
                </tr>
              ))}
              {data.requisitions.length === 0 && <tr><td colSpan={6} className="subtle">No records</td></tr>}
            </tbody>
          </table>
        </div>
      </article>

      <article className="card table-card">
        <h3>Requisition Lines</h3>
        <div className="table-scroll-wrap">
          <table className="explorer-table">
            <thead>
              <tr>
                <th>ID</th><th>Requisition ID</th><th>Requisition</th><th>Description</th><th className="amount">Qty</th><th className="amount">Unit Price</th>
                <th className="amount">Amount</th><th>Supplier</th><th>Charging Location ID</th><th>Charging Location</th><th>Link</th><th>Raw JSON</th>
              </tr>
            </thead>
            <tbody>
              {data.requisitionLines.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{valueOrDash(row.requisitionId)}</td>
                  <td>{valueOrDash(row.requisitionTitle)}</td>
                  <td>{valueOrDash(row.description)}</td>
                  <td className="amount">{formatNumber(row.qty)}</td>
                  <td className="amount">{formatNumber(row.unitPrice)}</td>
                  <td className="amount">{formatNumber(row.amount)}</td>
                  <td>{valueOrDash(row.supplier)}</td>
                  <td>{valueOrDash(row.chargingLocationId)}</td>
                  <td>{valueOrDash(`${valueOrDash(row.chargingLocationCode)} ${valueOrDash(row.chargingLocationName)}`)}</td>
                  <td>{row.link ? <a href={row.link} target="_blank" rel="noreferrer">Open</a> : '-'}</td>
                  <td><RawJsonCell rawJson={row.rawJson} /></td>
                </tr>
              ))}
              {data.requisitionLines.length === 0 && <tr><td colSpan={12} className="subtle">No records</td></tr>}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  );
}
