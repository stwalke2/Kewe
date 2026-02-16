package com.kewe.core.dimensions.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemToLedgerMappingRequest {
    @NotBlank
    private String itemTypeCode;
    @NotBlank
    private String itemNodeId;
    @NotBlank
    private String ledgerAccountNodeId;

    public String getItemTypeCode() { return itemTypeCode; }
    public void setItemTypeCode(String itemTypeCode) { this.itemTypeCode = itemTypeCode; }
    public String getItemNodeId() { return itemNodeId; }
    public void setItemNodeId(String itemNodeId) { this.itemNodeId = itemNodeId; }
    public String getLedgerAccountNodeId() { return ledgerAccountNodeId; }
    public void setLedgerAccountNodeId(String ledgerAccountNodeId) { this.ledgerAccountNodeId = ledgerAccountNodeId; }
}
