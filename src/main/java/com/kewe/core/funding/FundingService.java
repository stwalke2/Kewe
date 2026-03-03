package com.kewe.core.funding;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    private static final Logger log = LoggerFactory.getLogger(FundingService.class);
    private final BudgetRecordRepository budgetRepository;
    private final AllocationRecordRepository allocationRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final BusinessObjectTypeRepository businessObjectTypeRepository;
    private final MongoTemplate mongoTemplate;

    public FundingService(BudgetRecordRepository budgetRepository,
                          AllocationRecordRepository allocationRepository,
                          BusinessObjectRepository businessObjectRepository,
                          BusinessObjectTypeRepository businessObjectTypeRepository,
                          MongoTemplate mongoTemplate) {
        this.budgetRepository = budgetRepository;
        this.allocationRepository = allocationRepository;
        this.businessObjectRepository = businessObjectRepository;
        this.businessObjectTypeRepository = businessObjectTypeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<ChargingLocationDto> findChargingLocations(String budgetPlanId) {
        ChargingLocationDebugDto debug = collectChargingLocationDebug(budgetPlanId);
        Set<String> ids = new HashSet<>();
        ids.addAll(debug.eligibleFromBudgetIds());
        ids.addAll(debug.eligibleFromAllocDestIds());

        log.info("charging-locations: budgetPlanId={}, dimensionsCount={}, budgetsCount={}, allocationsCount={}, budgetJoinKeys={}, allocationJoinKeys={}, eligibleIds={}",
                budgetPlanId,
                debug.dimensionsCount(),
                debug.budgetsCount(),
                debug.allocationsCount(),
                debug.eligibleFromBudgetIds(),
                debug.eligibleFromAllocDestIds(),
                ids);

        if (ids.isEmpty()) {
            log.warn("No eligible charging locations found. budgetPlanId={}", budgetPlanId);
            return List.of();
        }

        return toDimensionDtos(businessObjectRepository.findAllById(ids), ids).stream()
                .sorted(Comparator.comparing(ChargingLocationDto::type)
                        .thenComparing(ChargingLocationDto::code)
                        .thenComparing(ChargingLocationDto::name))
                .toList();
    }

    public ChargingLocationDebugDto collectChargingLocationDebug(String budgetPlanId) {
        List<BusinessObjectInstance> dimensions = businessObjectRepository.findAll();
        Map<String, BusinessObjectInstance> dimensionById = new HashMap<>();
        Map<String, String> dimensionIdByCode = new HashMap<>();
        for (BusinessObjectInstance dimension : dimensions) {
            dimensionById.put(dimension.getId(), dimension);
            if (dimension.getCode() != null) {
                dimensionIdByCode.put(dimension.getCode().trim().toLowerCase(), dimension.getId());
            }
        }

        List<Document> budgets = readCollection("budgets");
        List<Document> allocations = readCollection("allocations");
        List<Document> filteredBudgets = filterByBudgetPlan(budgets, budgetPlanId);
        List<Document> filteredAllocations = filterByBudgetPlan(allocations, budgetPlanId);

        Set<String> eligibleFromBudgetIds = new HashSet<>();
        for (Document budget : filteredBudgets) {
            resolveDimensionId(budget, dimensionIdByCode, "businessDimensionId", "dimensionId", "destinationDimensionId", "toBusinessDimensionId", "code")
                    .ifPresent(eligibleFromBudgetIds::add);
        }

        Set<String> eligibleFromAllocDestIds = new HashSet<>();
        for (Document allocation : filteredAllocations) {
            resolveDimensionId(allocation, dimensionIdByCode, "allocatedToDimensionId", "toBusinessDimensionId", "businessDimensionId", "destinationDimensionId", "dimensionId", "code")
                    .ifPresent(eligibleFromAllocDestIds::add);
        }

        Set<String> eligibleIds = new HashSet<>(eligibleFromBudgetIds);
        eligibleIds.addAll(eligibleFromAllocDestIds);
        List<ChargingLocationDto> eligibleFinal = toDimensionDtos(dimensionById.values(), eligibleIds).stream()
                .sorted(Comparator.comparing(ChargingLocationDto::type)
                        .thenComparing(ChargingLocationDto::code)
                        .thenComparing(ChargingLocationDto::name))
                .toList();

        return new ChargingLocationDebugDto(
                dimensions.size(),
                filteredBudgets.size(),
                filteredAllocations.size(),
                filteredBudgets.stream().limit(3).map(Document::toJson).toList(),
                filteredAllocations.stream().limit(3).map(Document::toJson).toList(),
                eligibleFromBudgetIds.stream().sorted().toList(),
                eligibleFromAllocDestIds.stream().sorted().toList(),
                eligibleFinal
        );
    }

    public FundingSnapshotResponse fundingSnapshot(String chargingDimensionId, String budgetPlan, Double proposedAmount) {
        BusinessObjectInstance charging = businessObjectRepository.findById(chargingDimensionId).orElse(null);
        if (charging == null) {
            return new FundingSnapshotResponse(null, null, null, List.of(), List.of(),
                    List.of(), new FundingTotalsDto(null, 0, 0, null, null));
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

        List<AllocationRecord> toAllocations = isBlank(resolvedPlanId)
                ? allocationRepository.findByAllocatedToDimensionId(chargingDimensionId)
                : allocationRepository.findByAllocatedToDimensionIdAndBudgetPlanId(chargingDimensionId, resolvedPlanId);
        List<AllocationSnapshotDto> to = toAllocationViews(toAllocations, false);

        Double budgetTotal = matchedBudget.map(BudgetRecord::getAmount).orElse(null);
        double allocatedFromTotal = from.stream().mapToDouble(AllocationSnapshotDto::amount).sum();
        double allocatedToTotal = to.stream().mapToDouble(AllocationSnapshotDto::amount).sum();
        Double remaining = budgetTotal == null ? null : budgetTotal - allocatedFromTotal;
        Double chargingRemainingBeforeReq = remaining != null ? remaining : allocatedToTotal - allocatedFromTotal;
        double requestAmount = proposedAmount == null ? 0 : proposedAmount;

        BudgetPlanDto budgetPlanDto = matchedBudget
                .map(value -> new BudgetPlanDto(value.getBudgetPlanId(), value.getBudgetPlanName()))
                .orElseGet(() -> isBlank(resolvedPlanId) ? null : new BudgetPlanDto(resolvedPlanId, resolvedPlanId));

        List<FundingSourceSnapshotDto> fundingSources = toAllocations.stream()
                .map(allocation -> {
                    BusinessObjectInstance sourceDimension = businessObjectRepository.findById(allocation.getAllocatedFromDimensionId()).orElse(null);
                    Double sourceRemainingBeforeReq = budgetRemainingBeforeReq(allocation.getAllocatedFromDimensionId(), resolvedPlanId);
                    return new FundingSourceSnapshotDto(
                            toDimensionDto(sourceDimension),
                            toDimensionDto(charging),
                            requestAmount,
                            chargingRemainingBeforeReq == null ? null : chargingRemainingBeforeReq - requestAmount,
                            sourceRemainingBeforeReq == null ? null : sourceRemainingBeforeReq - requestAmount
                    );
                })
                .toList();

        return new FundingSnapshotResponse(
                toDimensionDto(charging),
                budgetPlanDto,
                matchedBudget.map(value -> new BudgetAmountDto(value.getId(), value.getAmount())).orElse(null),
                from,
                to,
                fundingSources,
                new FundingTotalsDto(budgetTotal, allocatedFromTotal, allocatedToTotal, remaining, chargingRemainingBeforeReq)
        );
    }



    private Double budgetRemainingBeforeReq(String businessDimensionId, String budgetPlanId) {
        Optional<BudgetRecord> budget = resolveBudgetByPlan(
                budgetRepository.findByBusinessDimensionId(businessDimensionId),
                budgetPlanId
        );
        if (budget.isEmpty()) {
            return null;
        }
        List<AllocationRecord> outgoing = isBlank(budgetPlanId)
                ? allocationRepository.findByAllocatedFromDimensionId(businessDimensionId)
                : allocationRepository.findByAllocatedFromDimensionIdAndBudgetPlanId(businessDimensionId, budgetPlanId);
        double allocated = outgoing.stream().mapToDouble(AllocationRecord::getAmount).sum();
        return budget.get().getAmount() - allocated;
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
                .map(item -> new ChargingLocationDto(item.getId(), item.getCode(), item.getName(), typeNames.getOrDefault(item.getTypeCode(), item.getTypeCode())))
                .toList();
    }

    private ChargingLocationDto toDimensionDto(BusinessObjectInstance item) {
        if (item == null) {
            return null;
        }
        Map<String, String> typeNames = loadTypeNames();
        return new ChargingLocationDto(item.getId(), item.getCode(), item.getName(), typeNames.getOrDefault(item.getTypeCode(), item.getTypeCode()));
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

    private List<Document> readCollection(String collectionName) {
        return mongoTemplate.getCollection(collectionName).find().into(new java.util.ArrayList<>());
    }

    private List<Document> filterByBudgetPlan(List<Document> records, String budgetPlanId) {
        if (isBlank(budgetPlanId)) {
            return records;
        }
        String normalized = normalizePlanKey(budgetPlanId);
        return records.stream()
                .filter(doc -> normalizePlanKey(doc.getString("budgetPlanId")).equals(normalized)
                        || normalizePlanKey(doc.getString("budgetPlanName")).equals(normalized)
                        || normalizePlanKey(doc.getString("budgetPlan")).equals(normalized))
                .toList();
    }

    private Optional<String> resolveDimensionId(Document source, Map<String, String> dimensionIdByCode, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (!(value instanceof String stringValue) || stringValue.isBlank()) {
                continue;
            }
            if (dimensionIdByCode.containsKey(stringValue.trim().toLowerCase())) {
                return Optional.ofNullable(dimensionIdByCode.get(stringValue.trim().toLowerCase()));
            }
            return Optional.of(stringValue);
        }
        return Optional.empty();
    }

    public record ChargingLocationDto(String id, String code, String name, String type) {}
    public record BudgetPlanDto(String id, String name) {}
    public record BudgetAmountDto(String id, double amount) {}
    public record AllocationSnapshotDto(String id, ChargingLocationDto allocatedTo, double amount) {}
    public record FundingSourceSnapshotDto(ChargingLocationDto fundingLocation,
                                           ChargingLocationDto chargingLocation,
                                           double proposedChargeAmount,
                                           Double projectedChargingAvailable,
                                           Double projectedFundingAvailable) {}
    public record FundingTotalsDto(Double budgetTotal,
                                   double allocatedFromTotal,
                                   double allocatedToTotal,
                                   Double remainingBeforeReq,
                                   Double chargingRemainingBeforeReq) {}
    public record FundingSnapshotResponse(ChargingLocationDto chargingDimension,
                                          BudgetPlanDto budgetPlan,
                                          BudgetAmountDto budget,
                                          List<AllocationSnapshotDto> allocationsFrom,
                                          List<AllocationSnapshotDto> allocationsTo,
                                          List<FundingSourceSnapshotDto> fundingSources,
                                          FundingTotalsDto totals) {}
    public record ChargingLocationDebugDto(int dimensionsCount,
                                           int budgetsCount,
                                           int allocationsCount,
                                           List<String> budgetSample,
                                           List<String> allocationSample,
                                           List<String> eligibleFromBudgetIds,
                                           List<String> eligibleFromAllocDestIds,
                                           List<ChargingLocationDto> eligibleFinal) {}
}
