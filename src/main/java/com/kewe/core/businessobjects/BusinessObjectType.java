package com.kewe.core.businessobjects;

import com.kewe.core.common.CanonicalObject;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "business_object_types")
@CompoundIndexes({
        @CompoundIndex(name = "uk_bo_type_code", def = "{'code': 1}", unique = true),
        @CompoundIndex(name = "idx_bo_type_status", def = "{'status': 1}")
})
public class BusinessObjectType extends CanonicalObject {

    private String code;
    private String name;
    private String description;
    private String objectKind;
    private boolean allowInstanceAccountingBudgetOverride;
    private AccountingBudgetSetup accountingBudgetDefaults = new AccountingBudgetSetup();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getObjectKind() { return objectKind; }
    public void setObjectKind(String objectKind) { this.objectKind = objectKind; }
    public boolean isAllowInstanceAccountingBudgetOverride() { return allowInstanceAccountingBudgetOverride; }
    public void setAllowInstanceAccountingBudgetOverride(boolean allowInstanceAccountingBudgetOverride) { this.allowInstanceAccountingBudgetOverride = allowInstanceAccountingBudgetOverride; }
    public AccountingBudgetSetup getAccountingBudgetDefaults() { return accountingBudgetDefaults; }
    public void setAccountingBudgetDefaults(AccountingBudgetSetup accountingBudgetDefaults) { this.accountingBudgetDefaults = accountingBudgetDefaults; }
}
