package com.kewe.core.funding;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "allocations")
public class AllocationRecord {
    @Id
    private String id;
    private String budgetPlanId;
    private String allocatedFromDimensionId;
    private String allocatedToDimensionId;
    private double amount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBudgetPlanId() { return budgetPlanId; }
    public void setBudgetPlanId(String budgetPlanId) { this.budgetPlanId = budgetPlanId; }
    public String getAllocatedFromDimensionId() { return allocatedFromDimensionId; }
    public void setAllocatedFromDimensionId(String allocatedFromDimensionId) { this.allocatedFromDimensionId = allocatedFromDimensionId; }
    public String getAllocatedToDimensionId() { return allocatedToDimensionId; }
    public void setAllocatedToDimensionId(String allocatedToDimensionId) { this.allocatedToDimensionId = allocatedToDimensionId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
