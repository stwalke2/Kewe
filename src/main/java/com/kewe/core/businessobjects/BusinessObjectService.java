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

    public List<BusinessObjectType> getTypes() {
        return typeRepository.findAll();
    }

    public BusinessObjectType updateTypeDefaults(String code, AccountingBudgetDefaultsRequest request) {
        BusinessObjectType type = getType(code);
        type.setAccountingBudgetDefaults(request.getAccountingBudgetDefaults());
        touchUpdate(type);
        return typeRepository.save(type);
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

        if (request.getAccountingBudgetOverride() != null) {
            if (!type.isAllowInstanceAccountingBudgetOverride()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This type does not allow accounting/budget overrides");
            }
            if (requiresReason(request.getAccountingBudgetOverride()) && !StringUtils.hasText(request.getOverrideReason())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Override reason is required by configuration");
            }
            object.setAccountingBudgetSetup(request.getAccountingBudgetOverride());
        } else {
            object.setAccountingBudgetSetup(type.getAccountingBudgetDefaults());
        }

        touchCreate(object);
        try {
            return objectRepository.save(object);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business object code already exists for this type");
        }
    }

    public BusinessObjectInstance overrideInstanceAccountingBudget(String id, AccountingBudgetDefaultsRequest request, String reason) {
        BusinessObjectInstance object = objectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business object not found"));
        BusinessObjectType type = getType(object.getTypeCode());
        if (!type.isAllowInstanceAccountingBudgetOverride()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This type does not allow accounting/budget overrides");
        }
        if (requiresReason(request.getAccountingBudgetDefaults()) && !StringUtils.hasText(reason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Override reason is required by configuration");
        }
        object.setAccountingBudgetSetup(request.getAccountingBudgetDefaults());
        touchUpdate(object);
        return objectRepository.save(object);
    }

    private boolean requiresReason(AccountingBudgetSetup setup) {
        return setup.getAllowExpense().isOverrideRequiresReason()
                || setup.getAllowRevenue().isOverrideRequiresReason()
                || setup.getBudgetRequired().isOverrideRequiresReason()
                || setup.getBudgetControlLevel().isOverrideRequiresReason()
                || setup.getDefaultLedgerAccount().isOverrideRequiresReason()
                || setup.getDefaultCompany().isOverrideRequiresReason()
                || setup.getDefaultFunction().isOverrideRequiresReason()
                || setup.getEnableEncumbrance().isOverrideRequiresReason()
                || setup.getIdcEligible().isOverrideRequiresReason()
                || setup.getCashManaged().isOverrideRequiresReason()
                || setup.getCapitalizable().isOverrideRequiresReason();
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
