package com.kewe.core.businessobjects.dto;

import com.kewe.core.businessobjects.BusinessObjectFieldOverride;
import com.kewe.core.businessobjects.HierarchyAssignment;
import com.kewe.core.businessobjects.RoleAssignment;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessObjectRequest {

    @NotBlank
    private String typeCode;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    private Instant effectiveDate;
    private String visibility;
    private List<HierarchyAssignment> hierarchies = new ArrayList<>();
    private List<RoleAssignment> roles = new ArrayList<>();
    private Map<String, BusinessObjectFieldOverride> accountingBudgetOverride = new HashMap<>();

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
    public Map<String, BusinessObjectFieldOverride> getAccountingBudgetOverride() { return accountingBudgetOverride; }
    public void setAccountingBudgetOverride(Map<String, BusinessObjectFieldOverride> accountingBudgetOverride) { this.accountingBudgetOverride = accountingBudgetOverride; }
}
