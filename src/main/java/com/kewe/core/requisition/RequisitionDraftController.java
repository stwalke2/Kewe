package com.kewe.core.requisition;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requisitions/drafts")
public class RequisitionDraftController {
    private final RequisitionDraftService service;

    public RequisitionDraftController(RequisitionDraftService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequisitionDraft create() {
        return service.createDraft();
    }

    @GetMapping("/{id}")
    public RequisitionDraft get(@PathVariable String id) {
        return service.getDraft(id);
    }

    @PutMapping("/{id}")
    public RequisitionDraft update(@PathVariable String id, @RequestBody RequisitionDraft payload) {
        return service.updateDraft(id, payload);
    }

    @PostMapping("/{id}/submit")
    public RequisitionDraft submit(@PathVariable String id) {
        return service.submit(id);
    }
}
