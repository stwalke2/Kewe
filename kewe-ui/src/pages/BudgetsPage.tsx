import { Fragment, useEffect, useMemo, useState } from 'react';
import { fetchBusinessObjects, getErrorDetails } from '../api';
import type { ApiErrorDetails, BusinessObjectInstance } from '../api/types';

type AllocationRow = {
  id: string;
  businessDimensionId: string;
  businessDimensionLabel: string;
  allocatedFrom: string;
  amount: number;
};

type BudgetRow = {
  id: string;
  businessDimensionId: string;
  businessDimensionLabel: string;
  budgetPlan: string;
  budgetAmount: number;
  canAllocate: boolean;
  allocations: AllocationRow[];
};

type BudgetForm = {
  businessDimensionId: string;
  budgetPlan: string;
  budgetAmount: string;
  canAllocate: boolean;
};

type AllocationForm = {
  businessDimensionId: string;
  amount: string;
};

type DimensionOption = {
  id: string;
  label: string;
};

function formatCurrency(amount: number): string {
  return amount.toLocaleString('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 });
}

function emptyBudgetForm(): BudgetForm {
  return { businessDimensionId: '', budgetPlan: '', budgetAmount: '', canAllocate: false };
}

function emptyAllocationForm(): AllocationForm {
  return { businessDimensionId: '', amount: '' };
}

export function BudgetsPage() {
  const [budgets, setBudgets] = useState<BudgetRow[]>([]);
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());
  const [budgetModalId, setBudgetModalId] = useState<string | null>(null);
  const [budgetModalMode, setBudgetModalMode] = useState<'create' | 'edit' | null>(null);
  const [allocationModalBudgetId, setAllocationModalBudgetId] = useState<string | null>(null);
  const [allocationModalId, setAllocationModalId] = useState<string | null>(null);
  const [allocationModalMode, setAllocationModalMode] = useState<'create' | 'edit' | null>(null);
  const [budgetForm, setBudgetForm] = useState<BudgetForm>(emptyBudgetForm());
  const [allocationForm, setAllocationForm] = useState<AllocationForm>(emptyAllocationForm());
  const [dimensionOptions, setDimensionOptions] = useState<DimensionOption[]>([]);
  const [dimensionError, setDimensionError] = useState<ApiErrorDetails | null>(null);
  const [dimensionLoading, setDimensionLoading] = useState(true);

  useEffect(() => {
    void loadDimensions();
  }, []);

  const dimensionLabelById = useMemo(() => new Map(dimensionOptions.map((option) => [option.id, option.label])), [dimensionOptions]);

  const selectedBudgetForAllocation = allocationModalBudgetId
    ? budgets.find((budget) => budget.id === allocationModalBudgetId) ?? null
    : null;

  const selectedAllocation = useMemo(() => {
    if (!allocationModalBudgetId || !allocationModalId) return null;
    const budget = budgets.find((item) => item.id === allocationModalBudgetId);
    return budget?.allocations.find((allocation) => allocation.id === allocationModalId) ?? null;
  }, [allocationModalBudgetId, allocationModalId, budgets]);

  const allocationDimensionSelectDisabled = dimensionLoading || dimensionOptions.length === 0;

  async function loadDimensions() {
    try {
      setDimensionLoading(true);
      setDimensionError(null);
      const businessDimensions = await fetchBusinessObjects();
      const activeDimensions = businessDimensions
        .filter((dimension) => dimension.status.trim().toLowerCase() === 'active')
        .sort((left, right) => `${left.code} ${left.name}`.localeCompare(`${right.code} ${right.name}`));
      setDimensionOptions(activeDimensions.map((dimension) => ({ id: dimension.id, label: toDimensionLabel(dimension) })));
    } catch (error) {
      setDimensionError(getErrorDetails(error));
    } finally {
      setDimensionLoading(false);
    }
  }

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
      businessDimensionId: budget.businessDimensionId,
      budgetPlan: budget.budgetPlan,
      budgetAmount: String(budget.budgetAmount),
      canAllocate: budget.canAllocate,
    });
  };

  const closeBudgetModal = () => {
    setBudgetModalMode(null);
    setBudgetModalId(null);
  };

  const openCreateAllocation = (budget: BudgetRow) => {
    setAllocationModalMode('create');
    setAllocationModalBudgetId(budget.id);
    setAllocationModalId(null);
    setAllocationForm(emptyAllocationForm());
  };

  const openEditAllocation = (budget: BudgetRow, allocation: AllocationRow) => {
    setAllocationModalMode('edit');
    setAllocationModalBudgetId(budget.id);
    setAllocationModalId(allocation.id);
    setAllocationForm({ businessDimensionId: allocation.businessDimensionId, amount: String(allocation.amount) });
  };

  const closeAllocationModal = () => {
    setAllocationModalMode(null);
    setAllocationModalBudgetId(null);
    setAllocationModalId(null);
  };

  const deleteBudget = (budgetId: string) => {
    setBudgets((current) => current.filter((budget) => budget.id !== budgetId));
    setExpandedIds((current) => {
      const next = new Set(current);
      next.delete(budgetId);
      return next;
    });
  };

  const deleteAllocation = (budgetId: string, allocationId: string) => {
    setBudgets((current) => current.map((budget) => {
      if (budget.id !== budgetId) return budget;
      return {
        ...budget,
        allocations: budget.allocations.filter((allocation) => allocation.id !== allocationId),
      };
    }));
  };

  const saveBudget = () => {
    const parsedAmount = Number(budgetForm.budgetAmount.replace(/,/g, ''));
    const selectedDimensionLabel = dimensionLabelById.get(budgetForm.businessDimensionId);

    if (!budgetForm.businessDimensionId || !selectedDimensionLabel || !budgetForm.budgetPlan.trim() || !Number.isFinite(parsedAmount) || parsedAmount < 0) {
      return;
    }

    if (budgetModalMode === 'create') {
      const createdBudget: BudgetRow = {
        id: `b-${Date.now()}`,
        businessDimensionId: budgetForm.businessDimensionId,
        businessDimensionLabel: selectedDimensionLabel,
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
        businessDimensionId: budgetForm.businessDimensionId,
        businessDimensionLabel: selectedDimensionLabel,
        budgetPlan: budgetForm.budgetPlan.trim(),
        budgetAmount: parsedAmount,
        canAllocate: budgetForm.canAllocate,
      }
      : budget)));
    closeBudgetModal();
  };

  const saveAllocation = () => {
    if (!allocationModalBudgetId || !selectedBudgetForAllocation || !allocationModalMode) return;
    const parsedAmount = Number(allocationForm.amount.replace(/,/g, ''));
    const selectedDimensionLabel = dimensionLabelById.get(allocationForm.businessDimensionId);
    if (!allocationForm.businessDimensionId || !selectedDimensionLabel || !Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      return;
    }

    setBudgets((current) => current.map((budget) => {
      if (budget.id !== allocationModalBudgetId) return budget;

      if (allocationModalMode === 'edit' && allocationModalId) {
        return {
          ...budget,
          allocations: budget.allocations.map((allocation) => (allocation.id === allocationModalId
            ? {
              ...allocation,
              businessDimensionId: allocationForm.businessDimensionId,
              businessDimensionLabel: selectedDimensionLabel,
              amount: parsedAmount,
            }
            : allocation)),
        };
      }

      const nextAllocation: AllocationRow = {
        id: `a-${Date.now()}`,
        businessDimensionId: allocationForm.businessDimensionId,
        businessDimensionLabel: selectedDimensionLabel,
        allocatedFrom: selectedBudgetForAllocation.businessDimensionLabel,
        amount: parsedAmount,
      };
      return { ...budget, allocations: [...budget.allocations, nextAllocation] };
    }));

    setExpandedIds((current) => new Set([...current, allocationModalBudgetId]));
    closeAllocationModal();
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

      {dimensionError && <div className="message error"><strong>Unable to load business dimensions.</strong><div>{dimensionError.message}</div></div>}

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
                      <td className="strong">{budget.businessDimensionLabel}</td>
                      <td>{budget.budgetPlan}</td>
                      <td className="amount strong">{formatCurrency(budget.budgetAmount)}</td>
                      <td>
                        <div className="inline-actions">
                          <button type="button" className="btn btn-secondary" onClick={() => openEditBudget(budget)}>Edit Budget</button>
                          <button type="button" className="btn btn-secondary" onClick={() => deleteBudget(budget.id)}>Delete Budget</button>
                          <button type="button" className="btn btn-primary" disabled={!budget.canAllocate} onClick={() => openCreateAllocation(budget)}>Add Allocation</button>
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
                                  <th>Actions</th>
                                </tr>
                              </thead>
                              <tbody>
                                {budget.allocations.length === 0 && (
                                  <tr>
                                    <td colSpan={4} className="subtle">No allocations yet for this budget.</td>
                                  </tr>
                                )}
                                {budget.allocations.map((allocation) => (
                                  <tr key={allocation.id}>
                                    <td>{allocation.businessDimensionLabel}</td>
                                    <td>{allocation.allocatedFrom}</td>
                                    <td className="amount strong">{formatCurrency(allocation.amount)}</td>
                                    <td>
                                      <div className="inline-actions">
                                        <button type="button" className="btn btn-secondary" onClick={() => openEditAllocation(budget, allocation)}>Edit Allocation</button>
                                        <button type="button" className="btn btn-secondary" onClick={() => deleteAllocation(budget.id, allocation.id)}>Delete Allocation</button>
                                      </div>
                                    </td>
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
                <select
                  value={budgetForm.businessDimensionId}
                  onChange={(event) => setBudgetForm((current) => ({ ...current, businessDimensionId: event.target.value }))}
                  disabled={allocationDimensionSelectDisabled}
                >
                  <option value="">Select business dimension</option>
                  {dimensionOptions.map((option) => (
                    <option key={option.id} value={option.id}>{option.label}</option>
                  ))}
                </select>
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

      {selectedBudgetForAllocation && allocationModalMode && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="allocation-modal-title">
            <div className="modal-header">
              <h2 id="allocation-modal-title">{allocationModalMode === 'create' ? 'Add Allocation' : 'Edit Allocation'}</h2>
              <p>
                {allocationModalMode === 'create'
                  ? <>Allocate budget from <strong>{selectedBudgetForAllocation.businessDimensionLabel}</strong>.</>
                  : <>Update allocation from <strong>{selectedBudgetForAllocation.businessDimensionLabel}</strong>.</>}
              </p>
            </div>
            <div className="modal-form">
              <label>
                Business Dimension
                <select
                  value={allocationForm.businessDimensionId}
                  onChange={(event) => setAllocationForm((current) => ({ ...current, businessDimensionId: event.target.value }))}
                  disabled={allocationDimensionSelectDisabled}
                >
                  <option value="">Select business dimension</option>
                  {dimensionOptions.map((option) => (
                    <option key={option.id} value={option.id}>{option.label}</option>
                  ))}
                </select>
              </label>
              <label>
                Allocated Amount
                <input value={allocationForm.amount} onChange={(event) => setAllocationForm((current) => ({ ...current, amount: event.target.value }))} placeholder="100000" />
              </label>
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={closeAllocationModal}>Cancel</button>
              <button type="button" className="btn btn-primary" onClick={saveAllocation}>{allocationModalMode === 'create' ? 'Save Allocation' : 'Update Allocation'}</button>
            </div>
          </div>
        </div>
      )}

      {allocationModalMode === 'edit' && !selectedAllocation && (
        <div className="message error">The selected allocation could not be found.</div>
      )}
    </section>
  );
}

function toDimensionLabel(dimension: BusinessObjectInstance): string {
  return `${dimension.code} — ${dimension.name}`;
}
