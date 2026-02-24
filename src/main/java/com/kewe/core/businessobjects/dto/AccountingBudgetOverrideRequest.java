package com.kewe.core.businessobjects.dto;

import com.kewe.core.businessobjects.BusinessObjectFieldOverride;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AccountingBudgetOverrideRequest {

    @NotNull
    private Map<String, BusinessObjectFieldOverride> overrides = new HashMap<>();

    public Map<String, BusinessObjectFieldOverride> getOverrides() { return overrides; }
    public void setOverrides(Map<String, BusinessObjectFieldOverride> overrides) { this.overrides = overrides; }
}
