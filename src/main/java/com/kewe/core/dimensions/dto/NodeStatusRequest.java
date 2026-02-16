package com.kewe.core.dimensions.dto;

import jakarta.validation.constraints.NotBlank;

public class NodeStatusRequest {
    @NotBlank
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
