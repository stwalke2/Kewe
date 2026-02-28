package com.kewe.core.funding;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "budgets")
public class BudgetRecord {
    @Id
    private String id;
    private String businessDimensionId;
    private String budgetPlanId;
    private String budgetPlanName;
    private double amount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessDimensionId() { return businessDimensionId; }
    public void setBusinessDimensionId(String businessDimensionId) { this.businessDimensionId = businessDimensionId; }
    public String getBudgetPlanId() { return budgetPlanId; }
    public void setBudgetPlanId(String budgetPlanId) { this.budgetPlanId = budgetPlanId; }
    public String getBudgetPlanName() { return budgetPlanName; }
    public void setBudgetPlanName(String budgetPlanName) { this.budgetPlanName = budgetPlanName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
