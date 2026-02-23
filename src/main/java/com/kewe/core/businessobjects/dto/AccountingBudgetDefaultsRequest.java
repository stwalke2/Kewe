package com.kewe.core.businessobjects.dto;

import com.kewe.core.businessobjects.AccountingBudgetSetup;
import jakarta.validation.constraints.NotNull;

public class AccountingBudgetDefaultsRequest {

    @NotNull
    private AccountingBudgetSetup accountingBudgetDefaults;

    public AccountingBudgetSetup getAccountingBudgetDefaults() { return accountingBudgetDefaults; }
    public void setAccountingBudgetDefaults(AccountingBudgetSetup accountingBudgetDefaults) { this.accountingBudgetDefaults = accountingBudgetDefaults; }
}
