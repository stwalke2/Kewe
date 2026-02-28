package com.kewe.core.requisition;

import com.kewe.core.common.CanonicalObject;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "requisition_drafts")
public class RequisitionDraft extends CanonicalObject {
    private String title;
    private String memo;
    private String requesterName;
    private String currency;
    private LocalDate needByDate;
    private String chargingBusinessDimensionId;
    private String chargingBusinessDimensionCode;
    private String chargingBusinessDimensionName;
    private String budgetPlanId;
    private List<RequisitionLine> lines = new ArrayList<>();
    private RequisitionTotals totals = new RequisitionTotals();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getNeedByDate() { return needByDate; }
    public void setNeedByDate(LocalDate needByDate) { this.needByDate = needByDate; }
    public String getChargingBusinessDimensionId() { return chargingBusinessDimensionId; }
    public void setChargingBusinessDimensionId(String chargingBusinessDimensionId) { this.chargingBusinessDimensionId = chargingBusinessDimensionId; }
    public String getChargingBusinessDimensionCode() { return chargingBusinessDimensionCode; }
    public void setChargingBusinessDimensionCode(String chargingBusinessDimensionCode) { this.chargingBusinessDimensionCode = chargingBusinessDimensionCode; }
    public String getChargingBusinessDimensionName() { return chargingBusinessDimensionName; }
    public void setChargingBusinessDimensionName(String chargingBusinessDimensionName) { this.chargingBusinessDimensionName = chargingBusinessDimensionName; }
    public String getBudgetPlanId() { return budgetPlanId; }
    public void setBudgetPlanId(String budgetPlanId) { this.budgetPlanId = budgetPlanId; }
    public List<RequisitionLine> getLines() { return lines; }
    public void setLines(List<RequisitionLine> lines) { this.lines = lines; }
    public RequisitionTotals getTotals() { return totals; }
    public void setTotals(RequisitionTotals totals) { this.totals = totals; }
}
