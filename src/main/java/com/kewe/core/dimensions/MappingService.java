package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.AwardDriverToFundMappingRequest;
import com.kewe.core.dimensions.dto.CostCenterToOrgMappingRequest;
import com.kewe.core.dimensions.dto.DefaultFunctionMappingRequest;
import com.kewe.core.dimensions.dto.ItemToLedgerMappingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MappingService {

    private static final String TYPE_DIMENSION_MAPPING = "DimensionMapping";
    private static final String STATUS_ACTIVE = "Active";
    private static final String SYSTEM_USER = "system";

    private static final Set<String> ITEM_TYPES = Set.of("SPEND_ITEM", "REVENUE_ITEM");
    private static final Set<String> AWARD_DRIVER_TYPES = Set.of("GIFT", "GRANT", "PROJECT", "APPROPRIATION", "NONE");

    private final DimensionMappingRepository mappingRepository;
    private final DimensionNodeService nodeService;

    public MappingService(DimensionMappingRepository mappingRepository, DimensionNodeService nodeService) {
        this.mappingRepository = mappingRepository;
        this.nodeService = nodeService;
    }

    public List<DimensionMapping> getItemToLedgerMappings() { return mappingRepository.findByMappingType(MappingType.ITEM_TO_LEDGER); }
    public List<DimensionMapping> getCostCenterToOrgMappings() { return mappingRepository.findByMappingType(MappingType.COSTCENTER_TO_ORG); }
    public List<DimensionMapping> getAwardDriverToFundMappings() { return mappingRepository.findByMappingType(MappingType.AWARDDRIVER_TO_FUND); }
    public List<DimensionMapping> getDefaultFunctionMappings() { return mappingRepository.findByMappingType(MappingType.DEFAULT_FUNCTION); }

    public DimensionMapping setItemToLedger(ItemToLedgerMappingRequest request) {
        String itemType = normalize(request.getItemTypeCode());
        if (!ITEM_TYPES.contains(itemType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "itemTypeCode must be SPEND_ITEM or REVENUE_ITEM");
        }
        nodeService.getNode(itemType, request.getItemNodeId());
        nodeService.getNode("LEDGER_ACCOUNT", request.getLedgerAccountNodeId());
        DimensionMapping mapping = upsert(MappingType.ITEM_TO_LEDGER, itemType, request.getItemNodeId(), null,
                "LEDGER_ACCOUNT", request.getLedgerAccountNodeId(), Map.of());
        return mappingRepository.save(mapping);
    }

    public void deleteItemToLedger(String itemTypeCode, String itemNodeId) {
        mappingRepository.deleteByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(MappingType.ITEM_TO_LEDGER,
                normalize(itemTypeCode), itemNodeId, null);
    }

    public DimensionMapping setCostCenterToOrg(CostCenterToOrgMappingRequest request) {
        nodeService.getNode("COST_CENTER", request.getCostCenterNodeId());
        nodeService.getNode("ORGANIZATION", request.getOrganizationNodeId());
        DimensionMapping mapping = upsert(MappingType.COSTCENTER_TO_ORG, "COST_CENTER", request.getCostCenterNodeId(), null,
                "ORGANIZATION", request.getOrganizationNodeId(), Map.of());
        return mappingRepository.save(mapping);
    }

    public void deleteCostCenterToOrg(String costCenterNodeId) {
        mappingRepository.deleteByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(MappingType.COSTCENTER_TO_ORG,
                "COST_CENTER", costCenterNodeId, null);
    }

    public DimensionMapping setAwardDriverToFund(AwardDriverToFundMappingRequest request) {
        String driverType = normalize(request.getDriverTypeCode());
        if (!AWARD_DRIVER_TYPES.contains(driverType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "driverTypeCode must be GIFT/GRANT/PROJECT/APPROPRIATION/NONE");
        }
        String driverNodeId = request.getDriverNodeId();
        if (!"NONE".equals(driverType)) {
            if (!StringUtils.hasText(driverNodeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "driverNodeId is required unless driverTypeCode is NONE");
            }
            nodeService.getNode(driverType, driverNodeId);
        } else {
            driverNodeId = null;
        }
        nodeService.getNode("FUND", request.getFundNodeId());
        String sourceKey = "NONE".equals(driverType) ? "NONE" : null;
        DimensionMapping mapping = upsert(MappingType.AWARDDRIVER_TO_FUND, driverType, driverNodeId, sourceKey,
                "FUND", request.getFundNodeId(), Map.of());
        return mappingRepository.save(mapping);
    }

    public void deleteAwardDriverToFund(String driverTypeCode, String driverNodeId) {
        String type = normalize(driverTypeCode);
        String sourceKey = "NONE".equals(type) ? "NONE" : null;
        mappingRepository.deleteByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(MappingType.AWARDDRIVER_TO_FUND,
                type, "NONE".equals(type) ? null : driverNodeId, sourceKey);
    }

    public DimensionMapping setDefaultFunction(DefaultFunctionMappingRequest request) {
        String sourceType = normalize(request.getSourceTypeCode());
        if (!Set.of("PROGRAM", "ORGANIZATION", "LEDGER_ACCOUNT").contains(sourceType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceTypeCode must be PROGRAM/ORGANIZATION/LEDGER_ACCOUNT");
        }
        nodeService.getNode(sourceType, request.getSourceNodeId());
        nodeService.getNode("FUNCTION", request.getFunctionNodeId());
        DimensionMapping mapping = upsert(MappingType.DEFAULT_FUNCTION, sourceType, request.getSourceNodeId(), null,
                "FUNCTION", request.getFunctionNodeId(), Map.of());
        return mappingRepository.save(mapping);
    }

    public void deleteDefaultFunction(String sourceTypeCode, String sourceNodeId) {
        mappingRepository.deleteByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(MappingType.DEFAULT_FUNCTION,
                normalize(sourceTypeCode), sourceNodeId, null);
    }

    public Optional<String> deriveLedgerAccountFromItem(String itemTypeCode, String itemNodeId) {
        return mappingRepository.findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
                MappingType.ITEM_TO_LEDGER, normalize(itemTypeCode), itemNodeId, null
        ).map(DimensionMapping::getTargetNodeId);
    }

    public Optional<String> deriveOrganizationFromCostCenter(String costCenterNodeId) {
        return mappingRepository.findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
                MappingType.COSTCENTER_TO_ORG, "COST_CENTER", costCenterNodeId, null
        ).map(DimensionMapping::getTargetNodeId);
    }

    public Optional<String> deriveFundFromAwardDriver(String driverTypeCode, String driverId) {
        String type = normalize(driverTypeCode);
        String sourceKey = "NONE".equals(type) ? "NONE" : null;
        String sourceNodeId = "NONE".equals(type) ? null : driverId;
        return mappingRepository.findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
                MappingType.AWARDDRIVER_TO_FUND, type, sourceNodeId, sourceKey
        ).map(DimensionMapping::getTargetNodeId);
    }

    public Optional<String> deriveFunctionDefault(String programNodeId, String orgNodeId, String ledgerAccountNodeId) {
        if (StringUtils.hasText(programNodeId)) {
            Optional<String> mapped = findDefaultFunction("PROGRAM", programNodeId);
            if (mapped.isPresent()) return mapped;
        }
        if (StringUtils.hasText(orgNodeId)) {
            Optional<String> mapped = findDefaultFunction("ORGANIZATION", orgNodeId);
            if (mapped.isPresent()) return mapped;
        }
        if (StringUtils.hasText(ledgerAccountNodeId)) {
            return findDefaultFunction("LEDGER_ACCOUNT", ledgerAccountNodeId);
        }
        return Optional.empty();
    }

    private Optional<String> findDefaultFunction(String sourceTypeCode, String sourceNodeId) {
        return mappingRepository.findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
                MappingType.DEFAULT_FUNCTION, sourceTypeCode, sourceNodeId, null
        ).map(DimensionMapping::getTargetNodeId);
    }

    private DimensionMapping upsert(MappingType mappingType,
                                    String sourceTypeCode,
                                    String sourceNodeId,
                                    String sourceKey,
                                    String targetTypeCode,
                                    String targetNodeId,
                                    Map<String, Object> context) {
        DimensionMapping mapping = mappingRepository.findByMappingTypeAndSourceTypeCodeAndSourceNodeIdAndSourceKey(
                mappingType, sourceTypeCode, sourceNodeId, sourceKey
        ).orElseGet(DimensionMapping::new);

        if (mapping.getId() == null) {
            touchCreate(mapping);
            mapping.setType(TYPE_DIMENSION_MAPPING);
            mapping.setStatus(STATUS_ACTIVE);
        }
        mapping.setMappingType(mappingType);
        mapping.setSourceTypeCode(sourceTypeCode);
        mapping.setSourceNodeId(sourceNodeId);
        mapping.setSourceKey(sourceKey);
        mapping.setTargetTypeCode(targetTypeCode);
        mapping.setTargetNodeId(targetNodeId);
        mapping.setContext(new LinkedHashMap<>(context));
        touchUpdate(mapping);
        return mapping;
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private void touchCreate(DimensionMapping value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
    }

    private void touchUpdate(DimensionMapping value) {
        value.setUpdatedAt(Instant.now());
        value.setUpdatedBy(SYSTEM_USER);
    }
}
