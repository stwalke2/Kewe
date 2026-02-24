package com.kewe.core.businessobjects;

import com.kewe.core.common.CanonicalObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BusinessObject extends CanonicalObject {

    private String objectKind;
    private String typeCode;
    private String code;
    private String name;
    private String description;
    private Instant effectiveDate;
    private String visibility;
    private List<HierarchyAssignment> hierarchies = new ArrayList<>();
    private List<RoleAssignment> roles = new ArrayList<>();
    private Map<String, BusinessObjectFieldOverride> accountingBudgetOverrides = new HashMap<>();

    public String getObjectKind() { return objectKind; }
    public void setObjectKind(String objectKind) { this.objectKind = objectKind; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Instant effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public List<HierarchyAssignment> getHierarchies() { return hierarchies; }
    public void setHierarchies(List<HierarchyAssignment> hierarchies) { this.hierarchies = hierarchies; }
    public List<RoleAssignment> getRoles() { return roles; }
    public void setRoles(List<RoleAssignment> roles) { this.roles = roles; }
    public Map<String, BusinessObjectFieldOverride> getAccountingBudgetOverrides() { return accountingBudgetOverrides; }
    public void setAccountingBudgetOverrides(Map<String, BusinessObjectFieldOverride> accountingBudgetOverrides) { this.accountingBudgetOverrides = accountingBudgetOverrides; }
}
