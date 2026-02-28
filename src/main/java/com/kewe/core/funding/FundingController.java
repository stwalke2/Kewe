package com.kewe.core.funding;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FundingController {
    private final FundingService fundingService;
    private final BudgetRecordRepository budgetRepository;
    private final AllocationRecordRepository allocationRepository;

    public FundingController(FundingService fundingService,
                             BudgetRecordRepository budgetRepository,
                             AllocationRecordRepository allocationRepository) {
        this.fundingService = fundingService;
        this.budgetRepository = budgetRepository;
        this.allocationRepository = allocationRepository;
    }

    @GetMapping("/charging-locations")
    public List<FundingService.ChargingLocationDto> chargingLocations(@RequestParam(required = false) String budgetPlanId) {
        return fundingService.findChargingLocations(budgetPlanId);
    }

    @GetMapping("/funding-snapshot")
    public FundingService.FundingSnapshotResponse fundingSnapshot(@RequestParam String chargingDimensionId,
                                                                  @RequestParam(required = false) String budgetPlan) {
        return fundingService.fundingSnapshot(chargingDimensionId, budgetPlan);
    }

    @GetMapping("/budgets")
    public BudgetResponse budgets(@RequestParam String businessDimensionId,
                                  @RequestParam(required = false) String budgetPlanId) {
        BudgetRecord budget = (budgetPlanId == null || budgetPlanId.isBlank())
                ? budgetRepository.findByBusinessDimensionId(businessDimensionId).stream().findFirst().orElse(null)
                : budgetRepository.findFirstByBusinessDimensionIdAndBudgetPlanId(businessDimensionId, budgetPlanId).orElse(null);
        List<AllocationRecord> allocations = (budgetPlanId == null || budgetPlanId.isBlank())
                ? allocationRepository.findByAllocatedFromDimensionId(businessDimensionId)
                : allocationRepository.findByAllocatedFromDimensionIdAndBudgetPlanId(businessDimensionId, budgetPlanId);

        return new BudgetResponse(budget, allocations);
    }

    public record BudgetResponse(BudgetRecord budget, List<AllocationRecord> allocations) {}
}
