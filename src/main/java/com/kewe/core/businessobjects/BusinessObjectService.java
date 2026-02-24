package com.kewe.core.businessobjects;

import com.kewe.core.businessobjects.dto.AccountingBudgetDefaultsRequest;
import com.kewe.core.businessobjects.dto.BusinessObjectRequest;
import com.kewe.core.businessobjects.dto.BusinessObjectTypeRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class BusinessObjectService {

    private static final String SYSTEM_USER = "system";

    private final BusinessObjectTypeRepository typeRepository;
    private final BusinessObjectRepository objectRepository;

    public BusinessObjectService(BusinessObjectTypeRepository typeRepository, BusinessObjectRepository objectRepository) {
        this.typeRepository = typeRepository;
        this.objectRepository = objectRepository;
    }

    public BusinessObjectType createType(BusinessObjectTypeRequest request) {
        BusinessObjectType type = new BusinessObjectType();
        type.setType("BusinessObjectType");
        type.setStatus("Active");
        type.setCode(normalizeCode(request.getCode()));
        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());
        type.setObjectKind(request.getObjectKind().trim());
        type.setAllowInstanceAccountingBudgetOverride(Boolean.TRUE.equals(request.getAllowInstanceAccountingBudgetOverride()));
        if (request.getAccountingBudgetDefaults() != null) {
            type.setAccountingBudgetDefaults(request.getAccountingBudgetDefaults());
        }
        touchCreate(type);
        try {
            return typeRepository.save(type);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business object type code already exists");
        }
    }

    public List<BusinessObjectType> getTypes() { return typeRepository.findAll(); }

    public BusinessObjectType getTypeByCode(String code) { return getType(code); }

    public BusinessObjectType updateType(String code, BusinessObjectTypeRequest request) {
        BusinessObjectType type = getType(code);
        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());
        type.setObjectKind(request.getObjectKind().trim());
        type.setAllowInstanceAccountingBudgetOverride(Boolean.TRUE.equals(request.getAllowInstanceAccountingBudgetOverride()));
        type.setAccountingBudgetDefaults(request.getAccountingBudgetDefaults());
        touchUpdate(type);
        return typeRepository.save(type);
    }

    public BusinessObjectType updateTypeDefaults(String code, AccountingBudgetDefaultsRequest request) {
        BusinessObjectType type = getType(code);
        type.setAccountingBudgetDefaults(request.getAccountingBudgetDefaults());
        touchUpdate(type);
        return typeRepository.save(type);
    }

    public List<BusinessObjectInstance> getObjects(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return objectRepository.findAll();
        }
        return objectRepository.findByTypeCode(normalizeCode(typeCode));
    }

    public BusinessObjectInstance getObjectById(String id) {
        return objectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business object not found"));
    }

    public BusinessObjectInstance createObject(BusinessObjectRequest request) {
        BusinessObjectType type = getType(request.getTypeCode());
        BusinessObjectInstance object = new BusinessObjectInstance();
        object.setType("BusinessObject");
        object.setStatus("Active");
        object.setObjectKind(type.getObjectKind());
        object.setTypeCode(type.getCode());
        object.setCode(normalizeCode(request.getCode()));
        object.setName(request.getName().trim());
        object.setDescription(request.getDescription());
        object.setEffectiveDate(request.getEffectiveDate());
        object.setVisibility(request.getVisibility());
        object.setHierarchies(request.getHierarchies());
        object.setRoles(request.getRoles());

        validateAndApplyOverrides(type, object, request.getAccountingBudgetOverride());

        touchCreate(object);
        try {
            return objectRepository.save(object);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business object code already exists for this type");
        }
    }

    public BusinessObjectInstance overrideInstanceAccountingBudget(String id, Map<String, BusinessObjectFieldOverride> overrides) {
        BusinessObjectInstance object = getObjectById(id);
        BusinessObjectType type = getType(object.getTypeCode());
        validateAndApplyOverrides(type, object, overrides);
        touchUpdate(object);
        return objectRepository.save(object);
    }

    private void validateAndApplyOverrides(BusinessObjectType type,
                                           BusinessObjectInstance object,
                                           Map<String, BusinessObjectFieldOverride> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            object.setAccountingBudgetOverrides(Map.of());
            return;
        }
        if (!type.isAllowInstanceAccountingBudgetOverride()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This type does not allow accounting/budget overrides");
        }

        overrides.forEach((field, value) -> {
            ConfiguredField<?> config = type.getAccountingBudgetDefaults().getFieldConfig(field);
            if (config == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown accounting/budget field: " + field);
            }
            if (!config.isAllowOverride()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Override is not allowed for field: " + field);
            }
            if (config.isOverrideReasonRequired() && (value == null || !StringUtils.hasText(value.getOverrideReason()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Override reason is required for field: " + field);
            }
        });
        object.setAccountingBudgetOverrides(overrides);
    }

    private BusinessObjectType getType(String code) {
        return typeRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business object type not found"));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private void touchCreate(com.kewe.core.common.CanonicalObject value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy(SYSTEM_USER);
        value.setUpdatedBy(SYSTEM_USER);
    }

    private void touchUpdate(com.kewe.core.common.CanonicalObject value) {
        value.setUpdatedAt(Instant.now());
        value.setUpdatedBy(SYSTEM_USER);
    }
}
