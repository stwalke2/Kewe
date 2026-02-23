package com.kewe.core.businessobjects;

public class AccountingBudgetSetup {

    private ConfiguredField<Boolean> allowExpense = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowRevenue = new ConfiguredField<>();
    private ConfiguredField<Boolean> budgetRequired = new ConfiguredField<>();
    private ConfiguredField<String> budgetControlLevel = new ConfiguredField<>();
    private ConfiguredField<String> defaultLedgerAccount = new ConfiguredField<>();
    private ConfiguredField<String> defaultCompany = new ConfiguredField<>();
    private ConfiguredField<String> defaultFunction = new ConfiguredField<>();
    private ConfiguredField<Boolean> enableEncumbrance = new ConfiguredField<>();
    private ConfiguredField<Boolean> idcEligible = new ConfiguredField<>();
    private ConfiguredField<Boolean> cashManaged = new ConfiguredField<>();
    private ConfiguredField<Boolean> capitalizable = new ConfiguredField<>();

    public ConfiguredField<Boolean> getAllowExpense() { return allowExpense; }
    public void setAllowExpense(ConfiguredField<Boolean> allowExpense) { this.allowExpense = allowExpense; }
    public ConfiguredField<Boolean> getAllowRevenue() { return allowRevenue; }
    public void setAllowRevenue(ConfiguredField<Boolean> allowRevenue) { this.allowRevenue = allowRevenue; }
    public ConfiguredField<Boolean> getBudgetRequired() { return budgetRequired; }
    public void setBudgetRequired(ConfiguredField<Boolean> budgetRequired) { this.budgetRequired = budgetRequired; }
    public ConfiguredField<String> getBudgetControlLevel() { return budgetControlLevel; }
    public void setBudgetControlLevel(ConfiguredField<String> budgetControlLevel) { this.budgetControlLevel = budgetControlLevel; }
    public ConfiguredField<String> getDefaultLedgerAccount() { return defaultLedgerAccount; }
    public void setDefaultLedgerAccount(ConfiguredField<String> defaultLedgerAccount) { this.defaultLedgerAccount = defaultLedgerAccount; }
    public ConfiguredField<String> getDefaultCompany() { return defaultCompany; }
    public void setDefaultCompany(ConfiguredField<String> defaultCompany) { this.defaultCompany = defaultCompany; }
    public ConfiguredField<String> getDefaultFunction() { return defaultFunction; }
    public void setDefaultFunction(ConfiguredField<String> defaultFunction) { this.defaultFunction = defaultFunction; }
    public ConfiguredField<Boolean> getEnableEncumbrance() { return enableEncumbrance; }
    public void setEnableEncumbrance(ConfiguredField<Boolean> enableEncumbrance) { this.enableEncumbrance = enableEncumbrance; }
    public ConfiguredField<Boolean> getIdcEligible() { return idcEligible; }
    public void setIdcEligible(ConfiguredField<Boolean> idcEligible) { this.idcEligible = idcEligible; }
    public ConfiguredField<Boolean> getCashManaged() { return cashManaged; }
    public void setCashManaged(ConfiguredField<Boolean> cashManaged) { this.cashManaged = cashManaged; }
    public ConfiguredField<Boolean> getCapitalizable() { return capitalizable; }
    public void setCapitalizable(ConfiguredField<Boolean> capitalizable) { this.capitalizable = capitalizable; }
}
