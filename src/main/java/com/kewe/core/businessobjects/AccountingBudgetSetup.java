package com.kewe.core.businessobjects;

import java.util.Map;
import java.util.Set;

public class AccountingBudgetSetup {

    private ConfiguredField<Boolean> allowExpensePosting = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowRevenuePosting = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowAssetPosting = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowLiabilityPosting = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowBalanceSheetPosting = new ConfiguredField<>();
    private ConfiguredField<String> restrictionType = new ConfiguredField<>();
    private ConfiguredField<String> netAssetClassMapping = new ConfiguredField<>();

    private ConfiguredField<Boolean> budgetRequired = new ConfiguredField<>();
    private ConfiguredField<String> budgetControlLevel = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowBudgetOverride = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowCarryforward = new ConfiguredField<>();
    private ConfiguredField<String> budgetYearType = new ConfiguredField<>();

    private ConfiguredField<String> defaultCompanyId = new ConfiguredField<>();
    private ConfiguredField<String> defaultFunctionId = new ConfiguredField<>();
    private ConfiguredField<String> defaultLedgerAccountId = new ConfiguredField<>();
    private ConfiguredField<String> defaultRestrictionType = new ConfiguredField<>();

    private ConfiguredField<Boolean> enableEncumbrance = new ConfiguredField<>();
    private ConfiguredField<Boolean> enablePreEncumbrance = new ConfiguredField<>();
    private ConfiguredField<String> encumbranceReleaseRule = new ConfiguredField<>();

    private ConfiguredField<Boolean> idcEligible = new ConfiguredField<>();
    private ConfiguredField<Boolean> sponsorApprovalRequired = new ConfiguredField<>();
    private ConfiguredField<Boolean> allowCostTransfer = new ConfiguredField<>();

    private ConfiguredField<Boolean> cashManaged = new ConfiguredField<>();
    private ConfiguredField<Boolean> investmentManaged = new ConfiguredField<>();
    private ConfiguredField<Boolean> unitized = new ConfiguredField<>();

    private ConfiguredField<Boolean> capitalizable = new ConfiguredField<>();
    private ConfiguredField<String> defaultDepreciationProfile = new ConfiguredField<>();

    public static final Set<String> SUPPORTED_FIELDS = Set.of(
            "allowExpensePosting", "allowRevenuePosting", "allowAssetPosting", "allowLiabilityPosting", "allowBalanceSheetPosting",
            "restrictionType", "netAssetClassMapping", "budgetRequired", "budgetControlLevel", "allowBudgetOverride", "allowCarryforward",
            "budgetYearType", "defaultCompanyId", "defaultFunctionId", "defaultLedgerAccountId", "defaultRestrictionType", "enableEncumbrance",
            "enablePreEncumbrance", "encumbranceReleaseRule", "idcEligible", "sponsorApprovalRequired", "allowCostTransfer", "cashManaged",
            "investmentManaged", "unitized", "capitalizable", "defaultDepreciationProfile");

    public ConfiguredField<?> getFieldConfig(String fieldName) {
        return asMap().get(fieldName);
    }

    public Map<String, ConfiguredField<?>> asMap() {
        return Map.ofEntries(
                Map.entry("allowExpensePosting", allowExpensePosting),
                Map.entry("allowRevenuePosting", allowRevenuePosting),
                Map.entry("allowAssetPosting", allowAssetPosting),
                Map.entry("allowLiabilityPosting", allowLiabilityPosting),
                Map.entry("allowBalanceSheetPosting", allowBalanceSheetPosting),
                Map.entry("restrictionType", restrictionType),
                Map.entry("netAssetClassMapping", netAssetClassMapping),
                Map.entry("budgetRequired", budgetRequired),
                Map.entry("budgetControlLevel", budgetControlLevel),
                Map.entry("allowBudgetOverride", allowBudgetOverride),
                Map.entry("allowCarryforward", allowCarryforward),
                Map.entry("budgetYearType", budgetYearType),
                Map.entry("defaultCompanyId", defaultCompanyId),
                Map.entry("defaultFunctionId", defaultFunctionId),
                Map.entry("defaultLedgerAccountId", defaultLedgerAccountId),
                Map.entry("defaultRestrictionType", defaultRestrictionType),
                Map.entry("enableEncumbrance", enableEncumbrance),
                Map.entry("enablePreEncumbrance", enablePreEncumbrance),
                Map.entry("encumbranceReleaseRule", encumbranceReleaseRule),
                Map.entry("idcEligible", idcEligible),
                Map.entry("sponsorApprovalRequired", sponsorApprovalRequired),
                Map.entry("allowCostTransfer", allowCostTransfer),
                Map.entry("cashManaged", cashManaged),
                Map.entry("investmentManaged", investmentManaged),
                Map.entry("unitized", unitized),
                Map.entry("capitalizable", capitalizable),
                Map.entry("defaultDepreciationProfile", defaultDepreciationProfile)
        );
    }

    public ConfiguredField<Boolean> getAllowExpensePosting() { return allowExpensePosting; }
    public void setAllowExpensePosting(ConfiguredField<Boolean> allowExpensePosting) { this.allowExpensePosting = allowExpensePosting; }
    public ConfiguredField<Boolean> getAllowRevenuePosting() { return allowRevenuePosting; }
    public void setAllowRevenuePosting(ConfiguredField<Boolean> allowRevenuePosting) { this.allowRevenuePosting = allowRevenuePosting; }
    public ConfiguredField<Boolean> getAllowAssetPosting() { return allowAssetPosting; }
    public void setAllowAssetPosting(ConfiguredField<Boolean> allowAssetPosting) { this.allowAssetPosting = allowAssetPosting; }
    public ConfiguredField<Boolean> getAllowLiabilityPosting() { return allowLiabilityPosting; }
    public void setAllowLiabilityPosting(ConfiguredField<Boolean> allowLiabilityPosting) { this.allowLiabilityPosting = allowLiabilityPosting; }
    public ConfiguredField<Boolean> getAllowBalanceSheetPosting() { return allowBalanceSheetPosting; }
    public void setAllowBalanceSheetPosting(ConfiguredField<Boolean> allowBalanceSheetPosting) { this.allowBalanceSheetPosting = allowBalanceSheetPosting; }
    public ConfiguredField<String> getRestrictionType() { return restrictionType; }
    public void setRestrictionType(ConfiguredField<String> restrictionType) { this.restrictionType = restrictionType; }
    public ConfiguredField<String> getNetAssetClassMapping() { return netAssetClassMapping; }
    public void setNetAssetClassMapping(ConfiguredField<String> netAssetClassMapping) { this.netAssetClassMapping = netAssetClassMapping; }
    public ConfiguredField<Boolean> getBudgetRequired() { return budgetRequired; }
    public void setBudgetRequired(ConfiguredField<Boolean> budgetRequired) { this.budgetRequired = budgetRequired; }
    public ConfiguredField<String> getBudgetControlLevel() { return budgetControlLevel; }
    public void setBudgetControlLevel(ConfiguredField<String> budgetControlLevel) { this.budgetControlLevel = budgetControlLevel; }
    public ConfiguredField<Boolean> getAllowBudgetOverride() { return allowBudgetOverride; }
    public void setAllowBudgetOverride(ConfiguredField<Boolean> allowBudgetOverride) { this.allowBudgetOverride = allowBudgetOverride; }
    public ConfiguredField<Boolean> getAllowCarryforward() { return allowCarryforward; }
    public void setAllowCarryforward(ConfiguredField<Boolean> allowCarryforward) { this.allowCarryforward = allowCarryforward; }
    public ConfiguredField<String> getBudgetYearType() { return budgetYearType; }
    public void setBudgetYearType(ConfiguredField<String> budgetYearType) { this.budgetYearType = budgetYearType; }
    public ConfiguredField<String> getDefaultCompanyId() { return defaultCompanyId; }
    public void setDefaultCompanyId(ConfiguredField<String> defaultCompanyId) { this.defaultCompanyId = defaultCompanyId; }
    public ConfiguredField<String> getDefaultFunctionId() { return defaultFunctionId; }
    public void setDefaultFunctionId(ConfiguredField<String> defaultFunctionId) { this.defaultFunctionId = defaultFunctionId; }
    public ConfiguredField<String> getDefaultLedgerAccountId() { return defaultLedgerAccountId; }
    public void setDefaultLedgerAccountId(ConfiguredField<String> defaultLedgerAccountId) { this.defaultLedgerAccountId = defaultLedgerAccountId; }
    public ConfiguredField<String> getDefaultRestrictionType() { return defaultRestrictionType; }
    public void setDefaultRestrictionType(ConfiguredField<String> defaultRestrictionType) { this.defaultRestrictionType = defaultRestrictionType; }
    public ConfiguredField<Boolean> getEnableEncumbrance() { return enableEncumbrance; }
    public void setEnableEncumbrance(ConfiguredField<Boolean> enableEncumbrance) { this.enableEncumbrance = enableEncumbrance; }
    public ConfiguredField<Boolean> getEnablePreEncumbrance() { return enablePreEncumbrance; }
    public void setEnablePreEncumbrance(ConfiguredField<Boolean> enablePreEncumbrance) { this.enablePreEncumbrance = enablePreEncumbrance; }
    public ConfiguredField<String> getEncumbranceReleaseRule() { return encumbranceReleaseRule; }
    public void setEncumbranceReleaseRule(ConfiguredField<String> encumbranceReleaseRule) { this.encumbranceReleaseRule = encumbranceReleaseRule; }
    public ConfiguredField<Boolean> getIdcEligible() { return idcEligible; }
    public void setIdcEligible(ConfiguredField<Boolean> idcEligible) { this.idcEligible = idcEligible; }
    public ConfiguredField<Boolean> getSponsorApprovalRequired() { return sponsorApprovalRequired; }
    public void setSponsorApprovalRequired(ConfiguredField<Boolean> sponsorApprovalRequired) { this.sponsorApprovalRequired = sponsorApprovalRequired; }
    public ConfiguredField<Boolean> getAllowCostTransfer() { return allowCostTransfer; }
    public void setAllowCostTransfer(ConfiguredField<Boolean> allowCostTransfer) { this.allowCostTransfer = allowCostTransfer; }
    public ConfiguredField<Boolean> getCashManaged() { return cashManaged; }
    public void setCashManaged(ConfiguredField<Boolean> cashManaged) { this.cashManaged = cashManaged; }
    public ConfiguredField<Boolean> getInvestmentManaged() { return investmentManaged; }
    public void setInvestmentManaged(ConfiguredField<Boolean> investmentManaged) { this.investmentManaged = investmentManaged; }
    public ConfiguredField<Boolean> getUnitized() { return unitized; }
    public void setUnitized(ConfiguredField<Boolean> unitized) { this.unitized = unitized; }
    public ConfiguredField<Boolean> getCapitalizable() { return capitalizable; }
    public void setCapitalizable(ConfiguredField<Boolean> capitalizable) { this.capitalizable = capitalizable; }
    public ConfiguredField<String> getDefaultDepreciationProfile() { return defaultDepreciationProfile; }
    public void setDefaultDepreciationProfile(ConfiguredField<String> defaultDepreciationProfile) { this.defaultDepreciationProfile = defaultDepreciationProfile; }
}
