package com.kewe.core.funding;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FundingController {
    private final FundingService fundingService;
    private final FundingModelDebugService fundingModelDebugService;
    private final BudgetRecordRepository budgetRepository;
    private final AllocationRecordRepository allocationRepository;

    public FundingController(FundingService fundingService,
                             FundingModelDebugService fundingModelDebugService,
                             BudgetRecordRepository budgetRepository,
                             AllocationRecordRepository allocationRepository) {
        this.fundingService = fundingService;
        this.fundingModelDebugService = fundingModelDebugService;
        this.budgetRepository = budgetRepository;
        this.allocationRepository = allocationRepository;
    }

    @GetMapping("/charging-locations")
    public List<FundingService.ChargingLocationDto> chargingLocations(@RequestParam(required = false) String budgetPlanId) {
        return fundingService.findChargingLocations(budgetPlanId);
    }

    @GetMapping("/debug/charging-locations")
    public FundingService.ChargingLocationDebugDto debugChargingLocations(@RequestParam(required = false) String budgetPlanId) {
        return fundingService.collectChargingLocationDebug(budgetPlanId);
    }

    @GetMapping("/debug/funding-model")
    public FundingModelDebugService.FundingModelDebugResponse fundingModelExplorer() {
        return fundingModelDebugService.loadFundingModel();
    }

    @GetMapping("/funding-snapshot")
    public FundingService.FundingSnapshotResponse fundingSnapshot(@RequestParam String chargingDimensionId,
                                                                  @RequestParam(required = false) String budgetPlan,
                                                                  @RequestParam(required = false) Double proposedAmount) {
        return fundingService.fundingSnapshot(chargingDimensionId, budgetPlan, proposedAmount);
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

    @GetMapping("/budgets/all")
    public FundingDataResponse allFundingData() {
        return new FundingDataResponse(budgetRepository.findAll(), allocationRepository.findAll());
    }

    @PostMapping("/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetRecord createBudget(@RequestBody UpsertBudgetRequest request) {
        BudgetRecord budget = new BudgetRecord();
        budget.setBusinessDimensionId(request.businessDimensionId());
        budget.setBudgetPlanId(request.budgetPlanId());
        budget.setBudgetPlanName(request.budgetPlanName());
        budget.setAmount(request.amount());
        return budgetRepository.save(budget);
    }

    @PutMapping("/budgets/{budgetId}")
    public BudgetRecord updateBudget(@PathVariable String budgetId, @RequestBody UpsertBudgetRequest request) {
        BudgetRecord budget = budgetRepository.findById(budgetId).orElseThrow();
        budget.setBusinessDimensionId(request.businessDimensionId());
        budget.setBudgetPlanId(request.budgetPlanId());
        budget.setBudgetPlanName(request.budgetPlanName());
        budget.setAmount(request.amount());
        return budgetRepository.save(budget);
    }

    @DeleteMapping("/budgets/{budgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable String budgetId) {
        budgetRepository.deleteById(budgetId);
    }

    @PostMapping("/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public AllocationRecord createAllocation(@RequestBody UpsertAllocationRequest request) {
        AllocationRecord allocation = new AllocationRecord();
        allocation.setBudgetPlanId(request.budgetPlanId());
        allocation.setAllocatedFromDimensionId(request.allocatedFromDimensionId());
        allocation.setAllocatedToDimensionId(request.allocatedToDimensionId());
        allocation.setAmount(request.amount());
        return allocationRepository.save(allocation);
    }

    @PutMapping("/allocations/{allocationId}")
    public AllocationRecord updateAllocation(@PathVariable String allocationId, @RequestBody UpsertAllocationRequest request) {
        AllocationRecord allocation = allocationRepository.findById(allocationId).orElseThrow();
        allocation.setBudgetPlanId(request.budgetPlanId());
        allocation.setAllocatedFromDimensionId(request.allocatedFromDimensionId());
        allocation.setAllocatedToDimensionId(request.allocatedToDimensionId());
        allocation.setAmount(request.amount());
        return allocationRepository.save(allocation);
    }

    @DeleteMapping("/allocations/{allocationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllocation(@PathVariable String allocationId) {
        allocationRepository.deleteById(allocationId);
    }

    public record BudgetResponse(BudgetRecord budget, List<AllocationRecord> allocations) {}
    public record FundingDataResponse(List<BudgetRecord> budgets, List<AllocationRecord> allocations) {}
    public record UpsertBudgetRequest(String businessDimensionId, String budgetPlanId, String budgetPlanName, double amount) {}
    public record UpsertAllocationRequest(String budgetPlanId, String allocatedFromDimensionId, String allocatedToDimensionId, double amount) {}
}
