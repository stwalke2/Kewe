package com.kewe.core.requisition;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;

@Service
public class RequisitionDraftService {
    private final RequisitionDraftRepository repository;
    private final BusinessObjectRepository businessObjectRepository;

    public RequisitionDraftService(RequisitionDraftRepository repository, BusinessObjectRepository businessObjectRepository) {
        this.repository = repository;
        this.businessObjectRepository = businessObjectRepository;
    }

    public RequisitionDraft createDraft() {
        RequisitionDraft draft = new RequisitionDraft();
        draft.setType("RequisitionDraft");
        draft.setStatus("DRAFT");
        draft.setTitle("New Requisition");
        draft.setRequesterName("Demo User");
        draft.setCurrency("USD");
        draft.setTotals(new RequisitionTotals());
        touchCreate(draft);
        return repository.save(draft);
    }

    public RequisitionDraft getDraft(String id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisition draft not found"));
    }

    public RequisitionDraft updateDraft(String id, RequisitionDraft payload) {
        RequisitionDraft current = getDraft(id);
        if (!"DRAFT".equals(current.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT requisitions can be edited");
        }

        current.setTitle(payload.getTitle());
        current.setMemo(payload.getMemo());
        current.setRequesterName(payload.getRequesterName());
        current.setCurrency(payload.getCurrency());
        current.setNeedByDate(payload.getNeedByDate());
        current.setBudgetPlanId(payload.getBudgetPlanId());
        current.setChargingBusinessDimensionId(payload.getChargingBusinessDimensionId());
        BusinessObjectInstance dimension = payload.getChargingBusinessDimensionId() == null ? null
                : businessObjectRepository.findById(payload.getChargingBusinessDimensionId()).orElse(null);
        current.setChargingBusinessDimensionCode(dimension == null ? payload.getChargingBusinessDimensionCode() : dimension.getCode());
        current.setChargingBusinessDimensionName(dimension == null ? payload.getChargingBusinessDimensionName() : dimension.getName());

        current.setLines(payload.getLines() == null ? java.util.List.of() : payload.getLines().stream()
                .sorted(Comparator.comparingInt(RequisitionLine::getLineNumber))
                .toList());
        RequisitionTotals totals = new RequisitionTotals();
        totals.setSubtotal(current.getLines().stream().mapToDouble(RequisitionLine::getAmount).sum());
        current.setTotals(totals);
        touchUpdate(current);
        return repository.save(current);
    }

    public RequisitionDraft submit(String id) {
        RequisitionDraft current = getDraft(id);
        current.setStatus("SUBMITTED");
        touchUpdate(current);
        return repository.save(current);
    }

    private void touchCreate(RequisitionDraft value) {
        Instant now = Instant.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setCreatedBy("system");
        value.setUpdatedBy("system");
    }

    private void touchUpdate(RequisitionDraft value) {
        value.setUpdatedAt(Instant.now());
        value.setUpdatedBy("system");
    }
}
