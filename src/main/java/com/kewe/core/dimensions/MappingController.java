package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.AwardDriverToFundMappingRequest;
import com.kewe.core.dimensions.dto.CostCenterToOrgMappingRequest;
import com.kewe.core.dimensions.dto.DefaultFunctionMappingRequest;
import com.kewe.core.dimensions.dto.ItemToLedgerMappingRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dimensions/mappings")
public class MappingController {

    private final MappingService service;

    public MappingController(MappingService service) {
        this.service = service;
    }

    @GetMapping("/item-to-ledger")
    public List<DimensionMapping> getItemToLedger() {
        return service.getItemToLedgerMappings();
    }

    @PostMapping("/item-to-ledger")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionMapping setItemToLedger(@Valid @RequestBody ItemToLedgerMappingRequest request) {
        return service.setItemToLedger(request);
    }

    @DeleteMapping("/item-to-ledger")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItemToLedger(@RequestParam String itemTypeCode, @RequestParam String itemNodeId) {
        service.deleteItemToLedger(itemTypeCode, itemNodeId);
    }

    @GetMapping("/costcenter-to-org")
    public List<DimensionMapping> getCostCenterToOrg() {
        return service.getCostCenterToOrgMappings();
    }

    @PostMapping("/costcenter-to-org")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionMapping setCostCenterToOrg(@Valid @RequestBody CostCenterToOrgMappingRequest request) {
        return service.setCostCenterToOrg(request);
    }

    @DeleteMapping("/costcenter-to-org")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCostCenterToOrg(@RequestParam String costCenterNodeId) {
        service.deleteCostCenterToOrg(costCenterNodeId);
    }

    @GetMapping("/awarddriver-to-fund")
    public List<DimensionMapping> getAwardDriverToFund() {
        return service.getAwardDriverToFundMappings();
    }

    @PostMapping("/awarddriver-to-fund")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionMapping setAwardDriverToFund(@Valid @RequestBody AwardDriverToFundMappingRequest request) {
        return service.setAwardDriverToFund(request);
    }

    @DeleteMapping("/awarddriver-to-fund")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAwardDriverToFund(@RequestParam String driverTypeCode,
                                        @RequestParam(required = false) String driverNodeId) {
        service.deleteAwardDriverToFund(driverTypeCode, driverNodeId);
    }

    @GetMapping("/default-function")
    public List<DimensionMapping> getDefaultFunction() {
        return service.getDefaultFunctionMappings();
    }

    @PostMapping("/default-function")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionMapping setDefaultFunction(@Valid @RequestBody DefaultFunctionMappingRequest request) {
        return service.setDefaultFunction(request);
    }

    @DeleteMapping("/default-function")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDefaultFunction(@RequestParam String sourceTypeCode,
                                      @RequestParam String sourceNodeId) {
        service.deleteDefaultFunction(sourceTypeCode, sourceNodeId);
    }
}
