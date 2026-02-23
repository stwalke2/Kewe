package com.kewe.core.businessobjects.dto;

import com.kewe.core.businessobjects.AccountingBudgetSetup;
import jakarta.validation.constraints.NotBlank;

public class BusinessObjectTypeRequest {

    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String objectKind;
    private Boolean allowInstanceAccountingBudgetOverride;
    private AccountingBudgetSetup accountingBudgetDefaults;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjectKind() { return objectKind; }
    public void setObjectKind(String objectKind) { this.objectKind = objectKind; }
    public Boolean getAllowInstanceAccountingBudgetOverride() { return allowInstanceAccountingBudgetOverride; }
    public void setAllowInstanceAccountingBudgetOverride(Boolean allowInstanceAccountingBudgetOverride) { this.allowInstanceAccountingBudgetOverride = allowInstanceAccountingBudgetOverride; }
    public AccountingBudgetSetup getAccountingBudgetDefaults() { return accountingBudgetDefaults; }
    public void setAccountingBudgetDefaults(AccountingBudgetSetup accountingBudgetDefaults) { this.accountingBudgetDefaults = accountingBudgetDefaults; }
}
