package com.kewe.core.dimensions.dto;

import jakarta.validation.constraints.NotBlank;

public class CostCenterToOrgMappingRequest {
    @NotBlank
    private String costCenterNodeId;
    @NotBlank
    private String organizationNodeId;

    public String getCostCenterNodeId() { return costCenterNodeId; }
    public void setCostCenterNodeId(String costCenterNodeId) { this.costCenterNodeId = costCenterNodeId; }
    public String getOrganizationNodeId() { return organizationNodeId; }
    public void setOrganizationNodeId(String organizationNodeId) { this.organizationNodeId = organizationNodeId; }
}
