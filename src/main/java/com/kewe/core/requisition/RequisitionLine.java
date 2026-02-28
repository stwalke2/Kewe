package com.kewe.core.requisition;

public class RequisitionLine {
    private int lineNumber;
    private String description;
    private double quantity;
    private String uom;
    private Double unitPrice;
    private double amount;
    private String supplierName;
    private String supplierUrl;
    private String supplierSku;

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierUrl() { return supplierUrl; }
    public void setSupplierUrl(String supplierUrl) { this.supplierUrl = supplierUrl; }
    public String getSupplierSku() { return supplierSku; }
    public void setSupplierSku(String supplierSku) { this.supplierSku = supplierSku; }
}
