package com.kewe.core.supplierinvoice;

import com.kewe.core.supplierinvoice.dto.StatusTransitionResponse;
import com.kewe.core.supplierinvoice.dto.SupplierInvoiceDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supplier-invoices")
public class SupplierInvoiceController {

    private final SupplierInvoiceService service;

    public SupplierInvoiceController(SupplierInvoiceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierInvoiceDTO createDraft(@Valid @RequestBody SupplierInvoiceDTO dto) {
        SupplierInvoice created = service.createDraft(dto);
        return service.mapToDTO(created);
    }

    @GetMapping("/{id}")
    public SupplierInvoiceDTO getById(@PathVariable String id) {
        return service.mapToDTO(service.getById(id));
    }

    @PutMapping("/{id}/submit")
    public StatusTransitionResponse submit(@PathVariable String id) {
        SupplierInvoice invoice = service.submit(id);
        return new StatusTransitionResponse(invoice.getId(), invoice.getStatus());
    }

    @PutMapping("/{id}/approve")
    public StatusTransitionResponse approve(@PathVariable String id) {
        SupplierInvoice invoice = service.approve(id);
        return new StatusTransitionResponse(invoice.getId(), invoice.getStatus());
    }

    @PutMapping("/{id}/post")
    public StatusTransitionResponse post(@PathVariable String id) {
        SupplierInvoice invoice = service.post(id);
        return new StatusTransitionResponse(invoice.getId(), invoice.getStatus());
    }
}
