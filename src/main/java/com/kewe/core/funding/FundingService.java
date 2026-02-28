package com.kewe.core.funding;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                .sorted(Comparator.comparing(ChargingLocationDto::typeName)
                        .thenComparing(ChargingLocationDto::code)
                        .thenComparing(ChargingLocationDto::name))
                .toList();
    }

    public FundingSnapshotDto fundingSnapshot(String chargingDimensionId, String budgetPlanId, double requisitionTotal) {
        BusinessObjectInstance charging = businessObjectRepository.findById(chargingDimensionId).orElse(null);
        if (charging == null) {
            return new FundingSnapshotDto(null, null, List.of(), List.of(),
                    new FundingTotalsDto(null, 0, 0, requisitionTotal, null));
        }

        Optional<BudgetRecord> budget = budgetPlanId == null || budgetPlanId.isBlank()
                ? budgetRepository.findByBusinessDimensionId(chargingDimensionId).stream().findFirst()
                : budgetRepository.findFirstByBusinessDimensionIdAndBudgetPlanId(chargingDimensionId, budgetPlanId);

        List<AllocationRecord> fromAllocations = budgetPlanId == null || budgetPlanId.isBlank()
                ? allocationRepository.findByAllocatedFromDimensionId(chargingDimensionId)
                : allocationRepository.findByAllocatedFromDimensionIdAndBudgetPlanId(chargingDimensionId, budgetPlanId);

        List<AllocationRecord> toAllocations = budgetPlanId == null || budgetPlanId.isBlank()
                ? allocationRepository.findByAllocatedToDimensionId(chargingDimensionId)
                : allocationRepository.findByAllocatedToDimensionIdAndBudgetPlanId(chargingDimensionId, budgetPlanId);

        List<AllocationViewDto> from = toAllocationViews(fromAllocations, true);
        List<AllocationViewDto> to = toAllocationViews(toAllocations, false);

        Double budgetTotal = budget.map(BudgetRecord::getAmount).orElse(null);
        double allocatedFromTotal = from.stream().mapToDouble(AllocationViewDto::amount).sum();
        double allocatedToTotal = to.stream().mapToDouble(AllocationViewDto::amount).sum();
        Double remaining = budgetTotal == null ? null : budgetTotal - allocatedFromTotal - requisitionTotal;

        return new FundingSnapshotDto(
                toDimensionDto(charging),
                budget.map(value -> new BudgetViewDto(value.getId(), value.getBudgetPlanId(), value.getBudgetPlanName(), value.getAmount())).orElse(null),
                from,
                to,
                new FundingTotalsDto(budgetTotal, allocatedFromTotal, allocatedToTotal, requisitionTotal, remaining)
        );
    }

    public boolean hasBudget(String dimensionId, String budgetPlanId) {
        if (budgetPlanId == null || budgetPlanId.isBlank()) {
            return !budgetRepository.findByBusinessDimensionId(dimensionId).isEmpty();
        }
        return budgetRepository.findFirstByBusinessDimensionIdAndBudgetPlanId(dimensionId, budgetPlanId).isPresent();
    }

    private List<AllocationViewDto> toAllocationViews(List<AllocationRecord> allocations, boolean from) {
        Set<String> relatedIds = new HashSet<>();
        allocations.forEach(allocation -> relatedIds.add(from ? allocation.getAllocatedToDimensionId() : allocation.getAllocatedFromDimensionId()));
        Map<String, BusinessObjectInstance> dimensionById = new HashMap<>();
        businessObjectRepository.findAllById(relatedIds).forEach(item -> dimensionById.put(item.getId(), item));

        return allocations.stream()
                .map(allocation -> {
                    String relatedId = from ? allocation.getAllocatedToDimensionId() : allocation.getAllocatedFromDimensionId();
                    return new AllocationViewDto(allocation.getId(), toDimensionDto(dimensionById.get(relatedId)), allocation.getAmount());
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

    public record ChargingLocationDto(String id, String code, String name, String typeName, String status) {}
    public record BudgetViewDto(String id, String budgetPlanId, String budgetPlanName, double amount) {}
    public record AllocationViewDto(String id, ChargingLocationDto allocatedDimension, double amount) {}
    public record FundingTotalsDto(Double budgetTotal, double allocatedFromTotal, double allocatedToTotal, double requisitionTotal,
                                   Double remainingIfBudget) {}
    public record FundingSnapshotDto(ChargingLocationDto chargingDimension, BudgetViewDto budget,
                                     List<AllocationViewDto> allocationsFrom, List<AllocationViewDto> allocationsTo,
                                     FundingTotalsDto totals) {}
}
