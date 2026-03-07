package com.kewe.core.funding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import com.kewe.core.requisition.RequisitionDraft;
import com.kewe.core.requisition.RequisitionDraftRepository;
import com.kewe.core.requisition.RequisitionLine;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class FundingModelDebugService {
    private final BusinessObjectRepository businessObjectRepository;
    private final BusinessObjectTypeRepository businessObjectTypeRepository;
    private final BudgetRecordRepository budgetRecordRepository;
    private final AllocationRecordRepository allocationRecordRepository;
    private final RequisitionDraftRepository requisitionDraftRepository;
    private final ObjectMapper objectMapper;

    public FundingModelDebugService(BusinessObjectRepository businessObjectRepository,
                                    BusinessObjectTypeRepository businessObjectTypeRepository,
                                    BudgetRecordRepository budgetRecordRepository,
                                    AllocationRecordRepository allocationRecordRepository,
                                    RequisitionDraftRepository requisitionDraftRepository,
                                    ObjectMapper objectMapper) {
        this.businessObjectRepository = businessObjectRepository;
        this.businessObjectTypeRepository = businessObjectTypeRepository;
        this.budgetRecordRepository = budgetRecordRepository;
        this.allocationRecordRepository = allocationRecordRepository;
        this.requisitionDraftRepository = requisitionDraftRepository;
        this.objectMapper = objectMapper;
    }

    public FundingModelDebugResponse loadFundingModel() {
        List<BusinessObjectInstance> dimensions = businessObjectRepository.findAll();
        List<BudgetRecord> budgets = budgetRecordRepository.findAll();
        List<AllocationRecord> allocations = allocationRecordRepository.findAll();
        List<RequisitionDraft> requisitions = requisitionDraftRepository.findAll();

        Map<String, BusinessObjectInstance> dimensionById = new HashMap<>();
        Map<String, String> dimensionIdByCode = new HashMap<>();
        for (BusinessObjectInstance dimension : dimensions) {
            dimensionById.put(dimension.getId(), dimension);
            if (!isBlank(dimension.getCode())) {
                dimensionIdByCode.put(normalize(dimension.getCode()), dimension.getId());
            }
        }

        Map<String, String> typeNames = new HashMap<>();
        for (BusinessObjectType type : businessObjectTypeRepository.findAll()) {
            typeNames.put(type.getCode(), type.getName());
        }

        List<String> warnings = new ArrayList<>();
        warnings.addAll(findDuplicateDimensionCodes(dimensions));

        List<BusinessDimensionDebugRow> businessDimensionRows = dimensions.stream()
                .sorted(Comparator.comparing((BusinessObjectInstance item) -> safe(item.getCode()))
                        .thenComparing(item -> safe(item.getName()))
                        .thenComparing(BusinessObjectInstance::getId))
                .map(dimension -> new BusinessDimensionDebugRow(
                        dimension.getId(),
                        dimension.getCode(),
                        dimension.getName(),
                        typeNames.getOrDefault(dimension.getTypeCode(), dimension.getTypeCode()),
                        json(dimension)
                ))
                .toList();

        Map<String, Double> outgoingAllocationTotalsByDimensionAndPlan = new HashMap<>();
        for (AllocationRecord allocation : allocations) {
            Optional<String> sourceId = resolveDimensionId(allocation.getAllocatedFromDimensionId(), dimensionIdByCode);
            sourceId.ifPresent(id -> {
                String key = buildDimensionPlanKey(id, allocation.getBudgetPlanId());
                outgoingAllocationTotalsByDimensionAndPlan.merge(key, allocation.getAmount(), Double::sum);
            });
        }

        List<BudgetDebugRow> budgetRows = budgets.stream()
                .sorted(Comparator.comparing((BudgetRecord item) -> safe(item.getBusinessDimensionId()))
                        .thenComparing(item -> safe(item.getBudgetPlanId()))
                        .thenComparing(BudgetRecord::getId))
                .map(budget -> {
                    Optional<String> dimensionId = resolveDimensionId(budget.getBusinessDimensionId(), dimensionIdByCode);
                    BusinessObjectInstance dimension = dimensionId.map(dimensionById::get).orElse(null);
                    if (dimension == null) {
                        warnings.add("Budget " + safeId(budget.getId()) + " references nonexistent Business Dimension: " + safeId(budget.getBusinessDimensionId()));
                    }
                    double amountUsed = dimensionId
                            .map(id -> outgoingAllocationTotalsByDimensionAndPlan.getOrDefault(
                                    buildDimensionPlanKey(id, budget.getBudgetPlanId()),
                                    0.0
                            ))
                            .orElse(0.0);
                    return new BudgetDebugRow(
                            budget.getId(),
                            budget.getBusinessDimensionId(),
                            dimension == null ? null : dimension.getCode(),
                            dimension == null ? null : dimension.getName(),
                            budget.getBudgetPlanId(),
                            budget.getBudgetPlanName(),
                            budget.getAmount(),
                            amountUsed,
                            null,
                            json(budget)
                    );
                })
                .toList();

        List<AllocationDebugRow> allocationRows = allocations.stream()
                .sorted(Comparator.comparing((AllocationRecord item) -> safe(item.getBudgetPlanId()))
                        .thenComparing(item -> safe(item.getAllocatedFromDimensionId()))
                        .thenComparing(item -> safe(item.getAllocatedToDimensionId()))
                        .thenComparing(AllocationRecord::getId))
                .map(allocation -> {
                    Optional<String> fromId = resolveDimensionId(allocation.getAllocatedFromDimensionId(), dimensionIdByCode);
                    Optional<String> toId = resolveDimensionId(allocation.getAllocatedToDimensionId(), dimensionIdByCode);
                    BusinessObjectInstance fromDimension = fromId.map(dimensionById::get).orElse(null);
                    BusinessObjectInstance toDimension = toId.map(dimensionById::get).orElse(null);

                    if (fromDimension == null) {
                        warnings.add("Allocation " + safeId(allocation.getId()) + " has missing fromBusinessDimensionId: " + safeId(allocation.getAllocatedFromDimensionId()));
                    }
                    if (toDimension == null) {
                        warnings.add("Allocation " + safeId(allocation.getId()) + " has missing toBusinessDimensionId: " + safeId(allocation.getAllocatedToDimensionId()));
                    }

                    return new AllocationDebugRow(
                            allocation.getId(),
                            allocation.getAllocatedFromDimensionId(),
                            fromDimension == null ? null : fromDimension.getCode(),
                            fromDimension == null ? null : fromDimension.getName(),
                            allocation.getAllocatedToDimensionId(),
                            toDimension == null ? null : toDimension.getCode(),
                            toDimension == null ? null : toDimension.getName(),
                            allocation.getBudgetPlanId(),
                            allocation.getAmount(),
                            null,
                            null,
                            json(allocation)
                    );
                })
                .toList();

        Set<String> eligibleFromBudgetIds = new LinkedHashSet<>();
        for (BudgetRecord budget : budgets) {
            resolveDimensionId(budget.getBusinessDimensionId(), dimensionIdByCode)
                    .filter(dimensionById::containsKey)
                    .ifPresent(eligibleFromBudgetIds::add);
        }

        Set<String> eligibleFromAllocationDestinationIds = new LinkedHashSet<>();
        for (AllocationRecord allocation : allocations) {
            resolveDimensionId(allocation.getAllocatedToDimensionId(), dimensionIdByCode)
                    .filter(dimensionById::containsKey)
                    .ifPresent(eligibleFromAllocationDestinationIds::add);
        }

        Set<String> eligibleFinalIds = new LinkedHashSet<>(eligibleFromBudgetIds);
        eligibleFinalIds.addAll(eligibleFromAllocationDestinationIds);

        if (eligibleFinalIds.isEmpty()) {
            warnings.add("Eligible charging locations count = 0");
        }

        List<EligibleChargingLocationRow> eligibleFromBudget = eligibleFromBudgetIds.stream()
                .map(dimensionById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((BusinessObjectInstance item) -> safe(item.getCode()))
                        .thenComparing(item -> safe(item.getName())))
                .map(item -> new EligibleChargingLocationRow(item.getId(), item.getCode(), item.getName(), "Budget"))
                .toList();

        List<EligibleChargingLocationRow> eligibleFromAllocationDestination = eligibleFromAllocationDestinationIds.stream()
                .map(dimensionById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((BusinessObjectInstance item) -> safe(item.getCode()))
                        .thenComparing(item -> safe(item.getName())))
                .map(item -> new EligibleChargingLocationRow(item.getId(), item.getCode(), item.getName(), "Allocation Destination"))
                .toList();

        List<EligibleChargingLocationRow> eligibleFinal = eligibleFinalIds.stream()
                .map(dimensionById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((BusinessObjectInstance item) -> safe(item.getCode()))
                        .thenComparing(item -> safe(item.getName())))
                .map(item -> new EligibleChargingLocationRow(
                        item.getId(),
                        item.getCode(),
                        item.getName(),
                        eligibleFromBudgetIds.contains(item.getId()) && eligibleFromAllocationDestinationIds.contains(item.getId())
                                ? "Both"
                                : (eligibleFromBudgetIds.contains(item.getId()) ? "Budget" : "Allocation Destination")
                ))
                .toList();

        List<RequisitionDebugRow> requisitionRows = requisitions.stream()
                .sorted(Comparator.comparing((RequisitionDraft item) -> Optional.ofNullable(item.getCreatedAt()).orElse(Instant.EPOCH)).reversed()
                        .thenComparing(item -> safe(item.getId())))
                .map(requisition -> new RequisitionDebugRow(
                        requisition.getId(),
                        requisition.getTitle(),
                        requisition.getRequesterName(),
                        requisition.getMemo(),
                        requisition.getCreatedAt(),
                        json(requisition)
                ))
                .toList();

        List<RequisitionLineDebugRow> requisitionLineRows = new ArrayList<>();
        for (RequisitionDraft requisition : requisitions) {
            if (requisition.getLines() == null) {
                continue;
            }
            for (RequisitionLine line : requisition.getLines()) {
                if (isBlank(requisition.getId())) {
                    warnings.add("Requisition Line references nonexistent Requisition");
                }
                String chargingLocationId = line.getChargingBusinessDimensionId();
                BusinessObjectInstance chargingLocation = isBlank(chargingLocationId) ? null : dimensionById.get(chargingLocationId);
                if (!isBlank(chargingLocationId) && chargingLocation == null) {
                    warnings.add("Requisition Line " + deriveLineId(requisition.getId(), line) + " references nonexistent Charging Location: " + chargingLocationId);
                }

                double lineAmount = line.getAmount() > 0 ? line.getAmount() : computeLineAmount(line);
                requisitionLineRows.add(new RequisitionLineDebugRow(
                        deriveLineId(requisition.getId(), line),
                        requisition.getId(),
                        requisition.getTitle(),
                        line.getDescription(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        lineAmount,
                        line.getSupplierName(),
                        chargingLocationId,
                        chargingLocation == null ? line.getChargingBusinessDimensionCode() : chargingLocation.getCode(),
                        chargingLocation == null ? line.getChargingBusinessDimensionName() : chargingLocation.getName(),
                        line.getSupplierUrl(),
                        json(line)
                ));
            }
        }
        requisitionLineRows.sort(Comparator.comparing((RequisitionLineDebugRow item) -> safe(item.requisitionId()))
                .thenComparing(item -> safe(item.id())));

        FundingModelCounts counts = new FundingModelCounts(
                businessDimensionRows.size(),
                budgetRows.size(),
                allocationRows.size(),
                requisitionRows.size(),
                requisitionLineRows.size(),
                eligibleFinal.size()
        );

        EligibleChargingLocationsDebug eligibleChargingLocations = new EligibleChargingLocationsDebug(
                eligibleFromBudget,
                eligibleFromAllocationDestination,
                eligibleFinal,
                eligibleFinal.size()
        );

        return new FundingModelDebugResponse(
                counts,
                businessDimensionRows,
                budgetRows,
                allocationRows,
                requisitionRows,
                requisitionLineRows,
                eligibleChargingLocations,
                dedupeWarnings(warnings)
        );
    }

    private List<String> findDuplicateDimensionCodes(Collection<BusinessObjectInstance> dimensions) {
        Map<String, Set<String>> idsByCode = new HashMap<>();
        for (BusinessObjectInstance dimension : dimensions) {
            if (isBlank(dimension.getCode())) {
                continue;
            }
            idsByCode.computeIfAbsent(normalize(dimension.getCode()), ignored -> new LinkedHashSet<>()).add(dimension.getId());
        }
        List<String> warnings = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : idsByCode.entrySet()) {
            if (entry.getValue().size() > 1) {
                warnings.add("Duplicate Business Dimension code detected: " + entry.getKey() + " (" + String.join(", ", entry.getValue()) + ")");
            }
        }
        return warnings;
    }

    private List<String> dedupeWarnings(List<String> warnings) {
        return warnings.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String deriveLineId(String requisitionId, RequisitionLine line) {
        return safeId(requisitionId) + "#line-" + line.getLineNumber();
    }

    private double computeLineAmount(RequisitionLine line) {
        if (line.getUnitPrice() == null) {
            return 0;
        }
        return line.getQuantity() * line.getUnitPrice();
    }

    private Optional<String> resolveDimensionId(String rawIdOrCode, Map<String, String> dimensionIdByCode) {
        if (isBlank(rawIdOrCode)) {
            return Optional.empty();
        }
        String normalized = normalize(rawIdOrCode);
        if (dimensionIdByCode.containsKey(normalized)) {
            return Optional.ofNullable(dimensionIdByCode.get(normalized));
        }
        return Optional.of(rawIdOrCode);
    }

    private String buildDimensionPlanKey(String dimensionId, String budgetPlanId) {
        return dimensionId + "::" + normalizePlanKey(budgetPlanId);
    }

    private String normalizePlanKey(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[\\s_-]+", "").trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeId(String value) {
        return isBlank(value) ? "<blank>" : value;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"error\":\"Unable to serialize record\"}";
        }
    }

    public record FundingModelDebugResponse(FundingModelCounts counts,
                                            List<BusinessDimensionDebugRow> businessDimensions,
                                            List<BudgetDebugRow> budgets,
                                            List<AllocationDebugRow> allocations,
                                            List<RequisitionDebugRow> requisitions,
                                            List<RequisitionLineDebugRow> requisitionLines,
                                            EligibleChargingLocationsDebug eligibleChargingLocations,
                                            List<String> integrityWarnings) {
    }

    public record FundingModelCounts(int businessDimensionsCount,
                                     int budgetsCount,
                                     int allocationsCount,
                                     int requisitionsCount,
                                     int requisitionLinesCount,
                                     int eligibleChargingLocationsCount) {
    }

    public record BusinessDimensionDebugRow(String id,
                                            String code,
                                            String name,
                                            String type,
                                            String rawJson) {
    }

    public record BudgetDebugRow(String id,
                                 String businessDimensionId,
                                 String businessDimensionCode,
                                 String businessDimensionName,
                                 String budgetPlanId,
                                 String budgetPlanName,
                                 double totalBudget,
                                 Double amountUsed,
                                 String status,
                                 String rawJson) {
    }

    public record AllocationDebugRow(String id,
                                     String fromBusinessDimensionId,
                                     String fromBusinessDimensionCode,
                                     String fromBusinessDimensionName,
                                     String toBusinessDimensionId,
                                     String toBusinessDimensionCode,
                                     String toBusinessDimensionName,
                                     String budgetPlanId,
                                     double allocatedAmount,
                                     Double amountUsed,
                                     String status,
                                     String rawJson) {
    }

    public record RequisitionDebugRow(String id,
                                      String title,
                                      String requesterName,
                                      String memo,
                                      Instant createdAt,
                                      String rawJson) {
    }

    public record RequisitionLineDebugRow(String id,
                                          String requisitionId,
                                          String requisitionTitle,
                                          String description,
                                          double qty,
                                          Double unitPrice,
                                          double amount,
                                          String supplier,
                                          String chargingLocationId,
                                          String chargingLocationCode,
                                          String chargingLocationName,
                                          String link,
                                          String rawJson) {
    }

    public record EligibleChargingLocationsDebug(List<EligibleChargingLocationRow> eligibleFromBudgets,
                                                 List<EligibleChargingLocationRow> eligibleFromAllocationDestinations,
                                                 List<EligibleChargingLocationRow> finalUnionSet,
                                                 int finalEligibleCount) {
    }

    public record EligibleChargingLocationRow(String id,
                                              String code,
                                              String name,
                                              String eligibilityReason) {
    }
}
