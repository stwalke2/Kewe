package com.kewe.core.dimensions.dto;

import jakarta.validation.constraints.NotBlank;

public class AwardDriverToFundMappingRequest {
    @NotBlank
    private String driverTypeCode;
    private String driverNodeId;
    @NotBlank
    private String fundNodeId;

    public String getDriverTypeCode() { return driverTypeCode; }
    public void setDriverTypeCode(String driverTypeCode) { this.driverTypeCode = driverTypeCode; }
    public String getDriverNodeId() { return driverNodeId; }
    public void setDriverNodeId(String driverNodeId) { this.driverNodeId = driverNodeId; }
    public String getFundNodeId() { return fundNodeId; }
    public void setFundNodeId(String fundNodeId) { this.fundNodeId = fundNodeId; }
}
