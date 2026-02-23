package com.kewe.core.businessobjects;

import com.kewe.core.businessobjects.dto.AccountingBudgetDefaultsRequest;
import com.kewe.core.businessobjects.dto.BusinessObjectRequest;
import com.kewe.core.businessobjects.dto.BusinessObjectTypeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business-object-types")
public class BusinessObjectController {

    private final BusinessObjectService service;

    public BusinessObjectController(BusinessObjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<BusinessObjectType> getTypes() {
        return service.getTypes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessObjectType createType(@Valid @RequestBody BusinessObjectTypeRequest request) {
        return service.createType(request);
    }

    @PutMapping("/{code}/accounting-budget-defaults")
    public BusinessObjectType updateDefaults(@PathVariable String code,
                                             @Valid @RequestBody AccountingBudgetDefaultsRequest request) {
        return service.updateTypeDefaults(code, request);
    }

    @PostMapping("/objects")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessObjectInstance createObject(@Valid @RequestBody BusinessObjectRequest request) {
        return service.createObject(request);
    }

    @PutMapping("/objects/{id}/accounting-budget-override")
    public BusinessObjectInstance overrideAccountingBudget(@PathVariable String id,
                                                           @RequestParam(required = false) String reason,
                                                           @Valid @RequestBody AccountingBudgetDefaultsRequest request) {
        return service.overrideInstanceAccountingBudget(id, request, reason);
    }
}
