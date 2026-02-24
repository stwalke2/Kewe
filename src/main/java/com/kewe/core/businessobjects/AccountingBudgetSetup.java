package com.kewe.core.businessobjects;

import java.util.Map;
import java.util.Set;
import java.util.List;

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

    private ConfiguredField<Boolean> chargeObjectEnabled = new ConfiguredField<>();
    private ConfiguredField<Boolean> directPostAllowed = new ConfiguredField<>();
    private ConfiguredField<Boolean> requiresSpendAuthority = new ConfiguredField<>();
    private ConfiguredField<String> spendAuthorityRoleKey = new ConfiguredField<>();
    private ConfiguredField<Boolean> liquidityRequired = new ConfiguredField<>();
    private ConfiguredField<String> liquiditySourceMode = new ConfiguredField<>();
    private ConfiguredField<Boolean> bridgingAllowed = new ConfiguredField<>();
    private ConfiguredField<Boolean> bridgingRequired = new ConfiguredField<>();
    private ConfiguredField<List<String>> bridgingObjectTypeCodes = new ConfiguredField<>();
    private ConfiguredField<String> defaultBridgingObjectId = new ConfiguredField<>();
    private ConfiguredField<Boolean> fundingSplitAllowed = new ConfiguredField<>();
    private ConfiguredField<String> fundingSplitMode = new ConfiguredField<>();
    private ConfiguredField<String> budgetCheckPoint = new ConfiguredField<>();
    private ConfiguredField<String> allowedSpendCategoriesMode = new ConfiguredField<>();
    private ConfiguredField<List<String>> allowedSpendCategoryIds = new ConfiguredField<>();
    private ConfiguredField<List<String>> deniedSpendCategoryIds = new ConfiguredField<>();

    public static final Set<String> SUPPORTED_FIELDS = Set.of(
            "allowExpensePosting", "allowRevenuePosting", "allowAssetPosting", "allowLiabilityPosting", "allowBalanceSheetPosting",
            "restrictionType", "netAssetClassMapping", "budgetRequired", "budgetControlLevel", "allowBudgetOverride", "allowCarryforward",
            "budgetYearType", "defaultCompanyId", "defaultFunctionId", "defaultLedgerAccountId", "defaultRestrictionType", "enableEncumbrance",
            "enablePreEncumbrance", "encumbranceReleaseRule", "idcEligible", "sponsorApprovalRequired", "allowCostTransfer", "cashManaged",
            "investmentManaged", "unitized", "capitalizable", "defaultDepreciationProfile", "chargeObjectEnabled", "directPostAllowed",
            "requiresSpendAuthority", "spendAuthorityRoleKey", "liquidityRequired", "liquiditySourceMode", "bridgingAllowed",
            "bridgingRequired", "bridgingObjectTypeCodes", "defaultBridgingObjectId", "fundingSplitAllowed", "fundingSplitMode",
            "budgetCheckPoint", "allowedSpendCategoriesMode", "allowedSpendCategoryIds", "deniedSpendCategoryIds");

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
                Map.entry("defaultDepreciationProfile", defaultDepreciationProfile),
                Map.entry("chargeObjectEnabled", chargeObjectEnabled),
                Map.entry("directPostAllowed", directPostAllowed),
                Map.entry("requiresSpendAuthority", requiresSpendAuthority),
                Map.entry("spendAuthorityRoleKey", spendAuthorityRoleKey),
                Map.entry("liquidityRequired", liquidityRequired),
                Map.entry("liquiditySourceMode", liquiditySourceMode),
                Map.entry("bridgingAllowed", bridgingAllowed),
                Map.entry("bridgingRequired", bridgingRequired),
                Map.entry("bridgingObjectTypeCodes", bridgingObjectTypeCodes),
                Map.entry("defaultBridgingObjectId", defaultBridgingObjectId),
                Map.entry("fundingSplitAllowed", fundingSplitAllowed),
                Map.entry("fundingSplitMode", fundingSplitMode),
                Map.entry("budgetCheckPoint", budgetCheckPoint),
                Map.entry("allowedSpendCategoriesMode", allowedSpendCategoriesMode),
                Map.entry("allowedSpendCategoryIds", allowedSpendCategoryIds),
                Map.entry("deniedSpendCategoryIds", deniedSpendCategoryIds)
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
    public ConfiguredField<Boolean> getChargeObjectEnabled() { return chargeObjectEnabled; }
    public void setChargeObjectEnabled(ConfiguredField<Boolean> chargeObjectEnabled) { this.chargeObjectEnabled = chargeObjectEnabled; }
    public ConfiguredField<Boolean> getDirectPostAllowed() { return directPostAllowed; }
    public void setDirectPostAllowed(ConfiguredField<Boolean> directPostAllowed) { this.directPostAllowed = directPostAllowed; }
    public ConfiguredField<Boolean> getRequiresSpendAuthority() { return requiresSpendAuthority; }
    public void setRequiresSpendAuthority(ConfiguredField<Boolean> requiresSpendAuthority) { this.requiresSpendAuthority = requiresSpendAuthority; }
    public ConfiguredField<String> getSpendAuthorityRoleKey() { return spendAuthorityRoleKey; }
    public void setSpendAuthorityRoleKey(ConfiguredField<String> spendAuthorityRoleKey) { this.spendAuthorityRoleKey = spendAuthorityRoleKey; }
    public ConfiguredField<Boolean> getLiquidityRequired() { return liquidityRequired; }
    public void setLiquidityRequired(ConfiguredField<Boolean> liquidityRequired) { this.liquidityRequired = liquidityRequired; }
    public ConfiguredField<String> getLiquiditySourceMode() { return liquiditySourceMode; }
    public void setLiquiditySourceMode(ConfiguredField<String> liquiditySourceMode) { this.liquiditySourceMode = liquiditySourceMode; }
    public ConfiguredField<Boolean> getBridgingAllowed() { return bridgingAllowed; }
    public void setBridgingAllowed(ConfiguredField<Boolean> bridgingAllowed) { this.bridgingAllowed = bridgingAllowed; }
    public ConfiguredField<Boolean> getBridgingRequired() { return bridgingRequired; }
    public void setBridgingRequired(ConfiguredField<Boolean> bridgingRequired) { this.bridgingRequired = bridgingRequired; }
    public ConfiguredField<List<String>> getBridgingObjectTypeCodes() { return bridgingObjectTypeCodes; }
    public void setBridgingObjectTypeCodes(ConfiguredField<List<String>> bridgingObjectTypeCodes) { this.bridgingObjectTypeCodes = bridgingObjectTypeCodes; }
    public ConfiguredField<String> getDefaultBridgingObjectId() { return defaultBridgingObjectId; }
    public void setDefaultBridgingObjectId(ConfiguredField<String> defaultBridgingObjectId) { this.defaultBridgingObjectId = defaultBridgingObjectId; }
    public ConfiguredField<Boolean> getFundingSplitAllowed() { return fundingSplitAllowed; }
    public void setFundingSplitAllowed(ConfiguredField<Boolean> fundingSplitAllowed) { this.fundingSplitAllowed = fundingSplitAllowed; }
    public ConfiguredField<String> getFundingSplitMode() { return fundingSplitMode; }
    public void setFundingSplitMode(ConfiguredField<String> fundingSplitMode) { this.fundingSplitMode = fundingSplitMode; }
    public ConfiguredField<String> getBudgetCheckPoint() { return budgetCheckPoint; }
    public void setBudgetCheckPoint(ConfiguredField<String> budgetCheckPoint) { this.budgetCheckPoint = budgetCheckPoint; }
    public ConfiguredField<String> getAllowedSpendCategoriesMode() { return allowedSpendCategoriesMode; }
    public void setAllowedSpendCategoriesMode(ConfiguredField<String> allowedSpendCategoriesMode) { this.allowedSpendCategoriesMode = allowedSpendCategoriesMode; }
    public ConfiguredField<List<String>> getAllowedSpendCategoryIds() { return allowedSpendCategoryIds; }
    public void setAllowedSpendCategoryIds(ConfiguredField<List<String>> allowedSpendCategoryIds) { this.allowedSpendCategoryIds = allowedSpendCategoryIds; }
    public ConfiguredField<List<String>> getDeniedSpendCategoryIds() { return deniedSpendCategoryIds; }
    public void setDeniedSpendCategoryIds(ConfiguredField<List<String>> deniedSpendCategoryIds) { this.deniedSpendCategoryIds = deniedSpendCategoryIds; }
}
