package com.kewe.core.funding;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class FundingService {
    private final BudgetRecordRepository budgetRepository;
    private final AllocationRecordRepository allocationRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final BusinessObjectTypeRepository businessObjectTypeRepository;

    public FundingService(BudgetRecordRepository budgetRepository,
                          AllocationRecordRepository allocationRepository,
                          BusinessObjectRepository businessObjectRepository,
                          BusinessObjectTypeRepository businessObjectTypeRepository) {
        this.budgetRepository = budgetRepository;
        this.allocationRepository = allocationRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.businessObjectTypeRepository = businessObjectTypeRepository;
    }

    public List<ChargingLocationDto> findChargingLocations(String budgetPlanId) {
        List<BudgetRecord> budgets = budgetPlanId == null || budgetPlanId.isBlank()
                ? budgetRepository.findAll()
                : budgetRepository.findByBudgetPlanId(budgetPlanId);
        List<AllocationRecord> allocations = budgetPlanId == null || budgetPlanId.isBlank()
                ? allocationRepository.findAll()
                : allocationRepository.findByBudgetPlanId(budgetPlanId);

        Set<String> ids = new HashSet<>();
        budgets.stream().map(BudgetRecord::getBusinessDimensionId).filter(Objects::nonNull).forEach(ids::add);
        allocations.stream().map(AllocationRecord::getAllocatedFromDimensionId).filter(Objects::nonNull).forEach(ids::add);
        allocations.stream().map(AllocationRecord::getAllocatedToDimensionId).filter(Objects::nonNull).forEach(ids::add);

        if (ids.isEmpty()) {
            return List.of();
        }

        return toDimensionDtos(businessObjectRepository.findAllById(ids), ids).stream()
                .sorted(Comparator.comparing(ChargingLocationDto::type)
                        .thenComparing(ChargingLocationDto::code)
                        .thenComparing(ChargingLocationDto::name))
                .toList();
    }

    public FundingSnapshotResponse fundingSnapshot(String chargingDimensionId, String budgetPlan) {
        BusinessObjectInstance charging = businessObjectRepository.findById(chargingDimensionId).orElse(null);
        if (charging == null) {
            return new FundingSnapshotResponse(null, null, null, List.of(),
                    new FundingTotalsDto(null, 0, null));
        }

        List<BudgetRecord> budgetsForDimension = budgetRepository.findByBusinessDimensionId(chargingDimensionId);
        Optional<BudgetRecord> matchedBudget = resolveBudgetByPlan(budgetsForDimension, budgetPlan);
        String resolvedPlanId = matchedBudget.map(BudgetRecord::getBudgetPlanId)
                .or(() -> resolvePlanIdFromAllocations(chargingDimensionId, budgetPlan))
                .orElse(budgetPlan);

        List<AllocationRecord> fromAllocations = isBlank(resolvedPlanId)
                ? allocationRepository.findByAllocatedFromDimensionId(chargingDimensionId)
                : allocationRepository.findByAllocatedFromDimensionIdAndBudgetPlanId(chargingDimensionId, resolvedPlanId);

        List<AllocationSnapshotDto> from = toAllocationViews(fromAllocations, true);

        Double budgetTotal = matchedBudget.map(BudgetRecord::getAmount).orElse(null);
        double allocatedFromTotal = from.stream().mapToDouble(AllocationSnapshotDto::amount).sum();
        Double remaining = budgetTotal == null ? null : budgetTotal - allocatedFromTotal;

        BudgetPlanDto budgetPlanDto = matchedBudget
                .map(value -> new BudgetPlanDto(value.getBudgetPlanId(), value.getBudgetPlanName()))
                .orElseGet(() -> isBlank(resolvedPlanId) ? null : new BudgetPlanDto(resolvedPlanId, resolvedPlanId));

        return new FundingSnapshotResponse(
                toDimensionDto(charging),
                budgetPlanDto,
                matchedBudget.map(value -> new BudgetAmountDto(value.getId(), value.getAmount())).orElse(null),
                from,
                new FundingTotalsDto(budgetTotal, allocatedFromTotal, remaining)
        );
    }

    private Optional<String> resolvePlanIdFromAllocations(String chargingDimensionId, String budgetPlan) {
        if (isBlank(budgetPlan)) {
            return Optional.empty();
        }
        String normalized = normalizePlanKey(budgetPlan);
        return allocationRepository.findByAllocatedFromDimensionId(chargingDimensionId).stream()
                .map(AllocationRecord::getBudgetPlanId)
                .filter(planId -> normalizePlanKey(planId).equals(normalized))
                .findFirst();
    }

    private Optional<BudgetRecord> resolveBudgetByPlan(List<BudgetRecord> budgets, String budgetPlan) {
        if (budgets.isEmpty()) {
            return Optional.empty();
        }
        if (isBlank(budgetPlan)) {
            return budgets.stream().findFirst();
        }
        String normalized = normalizePlanKey(budgetPlan);
        return budgets.stream()
                .filter(budget -> normalizePlanKey(budget.getBudgetPlanId()).equals(normalized)
                        || normalizePlanKey(budget.getBudgetPlanName()).equals(normalized))
                .findFirst();
    }

    private List<AllocationSnapshotDto> toAllocationViews(List<AllocationRecord> allocations, boolean from) {
        Set<String> relatedIds = new HashSet<>();
        allocations.forEach(allocation -> relatedIds.add(from ? allocation.getAllocatedToDimensionId() : allocation.getAllocatedFromDimensionId()));
        Map<String, BusinessObjectInstance> dimensionById = new HashMap<>();
        businessObjectRepository.findAllById(relatedIds).forEach(item -> dimensionById.put(item.getId(), item));

        return allocations.stream()
                .map(allocation -> {
                    String relatedId = from ? allocation.getAllocatedToDimensionId() : allocation.getAllocatedFromDimensionId();
                    return new AllocationSnapshotDto(allocation.getId(), toDimensionDto(dimensionById.get(relatedId)), allocation.getAmount());
                })
                .toList();
    }

    private List<ChargingLocationDto> toDimensionDtos(Collection<BusinessObjectInstance> dimensions, Set<String> selectedIds) {
        Map<String, String> typeNames = loadTypeNames();
        return dimensions.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .map(item -> new ChargingLocationDto(item.getId(), item.getCode(), item.getName(), typeNames.getOrDefault(item.getTypeCode(), item.getTypeCode()), item.getStatus()))
                .toList();
    }

    private ChargingLocationDto toDimensionDto(BusinessObjectInstance item) {
        if (item == null) {
            return null;
        }
        Map<String, String> typeNames = loadTypeNames();
        return new ChargingLocationDto(item.getId(), item.getCode(), item.getName(), typeNames.getOrDefault(item.getTypeCode(), item.getTypeCode()), item.getStatus());
    }

    private Map<String, String> loadTypeNames() {
        Map<String, String> result = new HashMap<>();
        for (BusinessObjectType type : businessObjectTypeRepository.findAll()) {
            result.put(type.getCode(), type.getName());
        }
        return result;
    }

    private String normalizePlanKey(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[\\s_-]+", "").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ChargingLocationDto(String id, String code, String name, String type, String status) {}
    public record BudgetPlanDto(String id, String name) {}
    public record BudgetAmountDto(String id, double amount) {}
    public record AllocationSnapshotDto(String id, ChargingLocationDto allocatedTo, double amount) {}
    public record FundingTotalsDto(Double budgetTotal, double allocatedFromTotal, Double remainingBeforeReq) {}
    public record FundingSnapshotResponse(ChargingLocationDto chargingDimension,
                                          BudgetPlanDto budgetPlan,
                                          BudgetAmountDto budget,
                                          List<AllocationSnapshotDto> allocationsFrom,
                                          FundingTotalsDto totals) {}
}
