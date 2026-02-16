package com.kewe.core.dimensions.dto;

import jakarta.validation.constraints.NotBlank;

public class DefaultFunctionMappingRequest {
    @NotBlank
    private String sourceTypeCode;
    @NotBlank
    private String sourceNodeId;
    @NotBlank
    private String functionNodeId;

    public String getSourceTypeCode() { return sourceTypeCode; }
    public void setSourceTypeCode(String sourceTypeCode) { this.sourceTypeCode = sourceTypeCode; }
    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }
    public String getFunctionNodeId() { return functionNodeId; }
    public void setFunctionNodeId(String functionNodeId) { this.functionNodeId = functionNodeId; }
}
