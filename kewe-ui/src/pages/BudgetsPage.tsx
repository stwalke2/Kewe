import { Fragment, useState } from 'react';

type AllocationRow = {
  id: string;
  businessDimension: string;
  allocatedFrom: string;
  amount: number;
};

type BudgetRow = {
  id: string;
  businessDimension: string;
  budgetPlan: string;
  budgetAmount: number;
  canAllocate: boolean;
  allocations: AllocationRow[];
};

type BudgetForm = {
  businessDimension: string;
  budgetPlan: string;
  budgetAmount: string;
  canAllocate: boolean;
};

type AllocationForm = {
  businessDimension: string;
  amount: string;
};

const seedBudgets: BudgetRow[] = [
  {
    id: 'b-1',
    businessDimension: 'CC-Biology',
    budgetPlan: 'FY26 Operating',
    budgetAmount: 100000,
    canAllocate: true,
    allocations: [
      {
        id: 'a-1',
        businessDimension: 'PD Tom Jones',
        allocatedFrom: 'CC-Biology',
        amount: 100000,
      },
    ],
  },
];

function formatCurrency(amount: number): string {
  return amount.toLocaleString('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });
}

function emptyBudgetForm(): BudgetForm {
  return { businessDimension: '', budgetPlan: '', budgetAmount: '', canAllocate: false };
}

export function BudgetsPage() {
  const [budgets, setBudgets] = useState<BudgetRow[]>(seedBudgets);
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());
  const [budgetModalId, setBudgetModalId] = useState<string | null>(null);
  const [budgetModalMode, setBudgetModalMode] = useState<'create' | 'edit' | null>(null);
  const [allocationModalId, setAllocationModalId] = useState<string | null>(null);
  const [budgetForm, setBudgetForm] = useState<BudgetForm>(emptyBudgetForm());
  const [allocationForm, setAllocationForm] = useState<AllocationForm>({ businessDimension: '', amount: '' });

  const selectedBudgetForAllocation = allocationModalId ? budgets.find((budget) => budget.id === allocationModalId) ?? null : null;

  const toggleExpanded = (budgetId: string) => {
    setExpandedIds((current) => {
      const next = new Set(current);
      if (next.has(budgetId)) {
        next.delete(budgetId);
      } else {
        next.add(budgetId);
      }
      return next;
    });
  };

  const openCreateBudget = () => {
    setBudgetModalMode('create');
    setBudgetModalId(null);
    setBudgetForm(emptyBudgetForm());
  };

  const openEditBudget = (budget: BudgetRow) => {
    setBudgetModalMode('edit');
    setBudgetModalId(budget.id);
    setBudgetForm({
      businessDimension: budget.businessDimension,
      budgetPlan: budget.budgetPlan,
      budgetAmount: String(budget.budgetAmount),
      canAllocate: budget.canAllocate,
    });
  };

  const closeBudgetModal = () => {
    setBudgetModalMode(null);
    setBudgetModalId(null);
  };

  const openAddAllocation = (budget: BudgetRow) => {
    setAllocationModalId(budget.id);
    setAllocationForm({ businessDimension: '', amount: '' });
  };

  const saveBudget = () => {
    const parsedAmount = Number(budgetForm.budgetAmount.replace(/,/g, ''));
    if (!budgetForm.businessDimension.trim() || !budgetForm.budgetPlan.trim() || !Number.isFinite(parsedAmount) || parsedAmount < 0) {
      return;
    }

    if (budgetModalMode === 'create') {
      const createdBudget: BudgetRow = {
        id: `b-${Date.now()}`,
        businessDimension: budgetForm.businessDimension.trim(),
        budgetPlan: budgetForm.budgetPlan.trim(),
        budgetAmount: parsedAmount,
        canAllocate: budgetForm.canAllocate,
        allocations: [],
      };
      setBudgets((current) => [createdBudget, ...current]);
      closeBudgetModal();
      return;
    }

    if (!budgetModalId) return;
    setBudgets((current) => current.map((budget) => (budget.id === budgetModalId
      ? {
        ...budget,
        businessDimension: budgetForm.businessDimension.trim(),
        budgetPlan: budgetForm.budgetPlan.trim(),
        budgetAmount: parsedAmount,
        canAllocate: budgetForm.canAllocate,
      }
      : budget)));
    closeBudgetModal();
  };

  const saveAllocation = () => {
    if (!allocationModalId || !selectedBudgetForAllocation) return;
    const parsedAmount = Number(allocationForm.amount.replace(/,/g, ''));
    if (!allocationForm.businessDimension.trim() || !Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      return;
    }

    setBudgets((current) => current.map((budget) => {
      if (budget.id !== allocationModalId) return budget;
      const nextAllocation: AllocationRow = {
        id: `a-${Date.now()}`,
        businessDimension: allocationForm.businessDimension.trim(),
        allocatedFrom: selectedBudgetForAllocation.businessDimension,
        amount: parsedAmount,
      };
      return { ...budget, allocations: [...budget.allocations, nextAllocation] };
    }));
    setExpandedIds((current) => new Set([...current, allocationModalId]));
    setAllocationModalId(null);
  };

  return (
    <section className="page-section">
      <div className="page-header-row">
        <div>
          <h2>Budgets & Allocations</h2>
          <p>Set budgets by Business Dimension, then allocate those budgets through nested allocations.</p>
        </div>
        <div className="header-actions">
          <button type="button" className="btn btn-primary" onClick={openCreateBudget}>Add Budget</button>
        </div>
      </div>

      <article className="card table-card">
        <div className="table-scroll-wrap">
          <table className="budgets-table">
            <thead>
              <tr>
                <th className="expand-col" aria-label="Expand row" />
                <th>Business Dimension</th>
                <th>Budget Plan</th>
                <th className="amount">Budget</th>
                <th>Allocations</th>
              </tr>
            </thead>
            <tbody>
              {budgets.length === 0 && (
                <tr>
                  <td colSpan={5} className="subtle">No budgets yet. Use Add Budget to create your first budget.</td>
                </tr>
              )}
              {budgets.map((budget) => {
                const isExpanded = expandedIds.has(budget.id);
                return (
                  <Fragment key={budget.id}>
                    <tr>
                      <td className="expand-col">
                        <button type="button" className="expand-btn" onClick={() => toggleExpanded(budget.id)} aria-label={isExpanded ? 'Collapse allocations' : 'Expand allocations'}>
                          {isExpanded ? '▾' : '▸'}
                        </button>
                      </td>
                      <td className="strong">{budget.businessDimension}</td>
                      <td>{budget.budgetPlan}</td>
                      <td className="amount strong">{formatCurrency(budget.budgetAmount)}</td>
                      <td>
                        <div className="inline-actions">
                          <button type="button" className="btn btn-secondary" onClick={() => openEditBudget(budget)}>Edit Budget</button>
                          <button type="button" className="btn btn-primary" disabled={!budget.canAllocate} onClick={() => openAddAllocation(budget)}>Add Allocation</button>
                        </div>
                      </td>
                    </tr>
                    {isExpanded && (
                      <tr>
                        <td colSpan={5} className="nested-cell">
                          <div className="nested-header">Allocations</div>
                          <div className="table-scroll-wrap nested-table-wrap">
                            <table className="nested-table">
                              <thead>
                                <tr>
                                  <th>Business Dimension</th>
                                  <th>Allocated From</th>
                                  <th className="amount">Allocated Amount</th>
                                </tr>
                              </thead>
                              <tbody>
                                {budget.allocations.length === 0 && (
                                  <tr>
                                    <td colSpan={3} className="subtle">No allocations yet for this budget.</td>
                                  </tr>
                                )}
                                {budget.allocations.map((allocation) => (
                                  <tr key={allocation.id}>
                                    <td>{allocation.businessDimension}</td>
                                    <td>{allocation.allocatedFrom}</td>
                                    <td className="amount strong">{formatCurrency(allocation.amount)}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      </article>

      {budgetModalMode && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="budget-modal-title">
            <div className="modal-header">
              <h2 id="budget-modal-title">{budgetModalMode === 'create' ? 'Add Budget' : 'Edit Budget'}</h2>
              <p>
                {budgetModalMode === 'create'
                  ? 'Create a budget for a Business Dimension and enable allocations if needed.'
                  : 'Update the budget fields and allocation eligibility for this Business Dimension.'}
              </p>
            </div>
            <div className="modal-form">
              <label>
                Business Dimension
                <input value={budgetForm.businessDimension} onChange={(event) => setBudgetForm((current) => ({ ...current, businessDimension: event.target.value }))} />
              </label>
              <label>
                Budget Plan
                <input value={budgetForm.budgetPlan} onChange={(event) => setBudgetForm((current) => ({ ...current, budgetPlan: event.target.value }))} />
              </label>
              <label>
                Budget Amount
                <input value={budgetForm.budgetAmount} onChange={(event) => setBudgetForm((current) => ({ ...current, budgetAmount: event.target.value }))} placeholder="100000" />
              </label>
              <label className="inline-check">
                <input type="checkbox" checked={budgetForm.canAllocate} onChange={(event) => setBudgetForm((current) => ({ ...current, canAllocate: event.target.checked }))} />
                Allow allocations from this budget
              </label>
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={closeBudgetModal}>Cancel</button>
              <button type="button" className="btn btn-primary" onClick={saveBudget}>{budgetModalMode === 'create' ? 'Create Budget' : 'Save Budget'}</button>
            </div>
          </div>
        </div>
      )}

      {selectedBudgetForAllocation && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="add-allocation-title">
            <div className="modal-header">
              <h2 id="add-allocation-title">Add Allocation</h2>
              <p>
                Allocate budget from <strong>{selectedBudgetForAllocation.businessDimension}</strong>.
              </p>
            </div>
            <div className="modal-form">
              <label>
                Business Dimension
                <input value={allocationForm.businessDimension} onChange={(event) => setAllocationForm((current) => ({ ...current, businessDimension: event.target.value }))} placeholder="PD Tom Jones" />
              </label>
              <label>
                Allocated Amount
                <input value={allocationForm.amount} onChange={(event) => setAllocationForm((current) => ({ ...current, amount: event.target.value }))} placeholder="100000" />
              </label>
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={() => setAllocationModalId(null)}>Cancel</button>
              <button type="button" className="btn btn-primary" onClick={saveAllocation}>Save Allocation</button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
