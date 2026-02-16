package com.kewe.core.supplierinvoice;

import com.kewe.core.supplierinvoice.dto.SupplierInvoiceDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class SupplierInvoiceService {

    private static final String TYPE_SUPPLIER_INVOICE = "SupplierInvoice";

    private static final String STATUS_DRAFT = "Draft";
    private static final String STATUS_SUBMITTED = "Submitted";
    private static final String STATUS_APPROVED = "Approved";
    private static final String STATUS_POSTED = "Posted";

    private static final String SYSTEM_USER = "system";

    private final SupplierInvoiceRepository repository;

    public SupplierInvoiceService(SupplierInvoiceRepository repository) {
        this.repository = repository;
    }

    // ---- Commands ----

    public SupplierInvoice createDraft(SupplierInvoiceDTO dto) {
        validateDraftFields(dto);

        SupplierInvoice invoice = mapToEntity(dto);
        invoice.setType(TYPE_SUPPLIER_INVOICE);
        invoice.setStatus(STATUS_DRAFT);
        touchCreate(invoice);

        return repository.save(invoice);
    }

    public SupplierInvoice updateDraft(String id, SupplierInvoiceDTO dto) {
        SupplierInvoice invoice = getById(id);

        requireStatus(invoice, STATUS_DRAFT, "Only Draft invoices can be edited");
        validateDraftFields(dto);

        applyUpdatableFields(invoice, dto);
        touchUpdate(invoice);

        return repository.save(invoice);
    }

    public SupplierInvoice submit(String id) {
        SupplierInvoice invoice = getById(id);

        requireStatusTransition(invoice, STATUS_DRAFT, STATUS_SUBMITTED);
        validateSubmitRules(invoice);

        invoice.setStatus(STATUS_SUBMITTED);
        touchUpdate(invoice);

        return repository.save(invoice);
    }

    public SupplierInvoice approve(String id) {
        return transition(id, STATUS_SUBMITTED, STATUS_APPROVED);
    }

    public SupplierInvoice post(String id) {
        return transition(id, STATUS_APPROVED, STATUS_POSTED);
    }

    // ---- Queries ----

    public SupplierInvoice getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> notFound("Supplier invoice not found: " + id));
    }

    /**
     * Canonical query method for "get all" (entities).
     * Controller can map to DTOs via mapToDTO.
     */
    public List<SupplierInvoice> getAll() {
        return repository.findAll();
    }

    // ---- Status transitions ----

    private SupplierInvoice transition(String id, String fromStatus, String toStatus) {
        SupplierInvoice invoice = getById(id);

        requireStatusTransition(invoice, fromStatus, toStatus);

        invoice.setStatus(toStatus);
        touchUpdate(invoice);

        return repository.save(invoice);
    }

    private void requireStatus(SupplierInvoice invoice, String expectedStatus, String messageIfWrong) {
        if (!expectedStatus.equals(invoice.getStatus())) {
            throw badRequest(messageIfWrong);
        }
    }

    private void requireStatusTransition(SupplierInvoice invoice, String fromStatus, String toStatus) {
        if (!fromStatus.equals(invoice.getStatus())) {
            throw badRequest("Invalid status transition: " + invoice.getStatus() + " -> " + toStatus);
        }
    }

    // ---- Validation ----

    private void validateDraftFields(SupplierInvoiceDTO dto) {
        if (isBlank(dto.getSupplierId())) {
            throw badRequest("supplierId is required for draft creation");
        }
        if (isBlank(dto.getInvoiceNumber())) {
            throw badRequest("invoiceNumber is required for draft creation");
        }
    }

    private void validateSubmitRules(SupplierInvoice invoice) {
        if (invoice.getInvoiceDate() == null) {
            throw badRequest("invoiceDate is required before submit");
        }
        if (invoice.getAccountingDate() == null) {
            throw badRequest("accountingDate is required before submit");
        }
        if (isBlank(invoice.getCurrency())) {
            throw badRequest("currency is required before submit");
        }
        if (invoice.getInvoiceAmount() == null) {
            throw badRequest("invoiceAmount is required before submit");
        }
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw badRequest("at least one line is required before submit");
        }

        BigDecimal lineTotal = BigDecimal.ZERO;
        for (int i = 0; i < invoice.getLines().size(); i++) {
            SupplierInvoice.Line line = invoice.getLines().get(i);
            if (isBlank(line.getDescription())) {
                throw badRequest("line " + (i + 1) + " description is required before submit");
            }
            if (line.getAmount() == null) {
                throw badRequest("line " + (i + 1) + " amount is required before submit");
            }
            lineTotal = lineTotal.add(line.getAmount());
        }

        if (invoice.getInvoiceAmount().compareTo(lineTotal) != 0) {
            throw badRequest("invoiceAmount must equal the sum of all line amounts");
        }
    }

    // ---- Mapping ----

    public SupplierInvoiceDTO mapToDTO(SupplierInvoice invoice) {
        SupplierInvoiceDTO dto = new SupplierInvoiceDTO();
        dto.setId(invoice.getId());
        dto.setStatus(invoice.getStatus());
        dto.setSupplierId(invoice.getSupplierId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setAccountingDate(invoice.getAccountingDate());
        dto.setCurrency(invoice.getCurrency());
        dto.setInvoiceAmount(invoice.getInvoiceAmount());
        dto.setMemo(invoice.getMemo());

        dto.setLines(safeList(invoice.getLines()).stream().map(line -> {
            SupplierInvoiceDTO.LineDTO lineDTO = new SupplierInvoiceDTO.LineDTO();
            lineDTO.setDescription(line.getDescription());
            lineDTO.setAmount(line.getAmount());
            return lineDTO;
        }).toList());

        dto.setAttachmentsMetadata(safeList(invoice.getAttachmentsMetadata()).stream().map(attachment -> {
            SupplierInvoiceDTO.AttachmentMetadataDTO attachmentDTO = new SupplierInvoiceDTO.AttachmentMetadataDTO();
            attachmentDTO.setFileName(attachment.getFileName());
            attachmentDTO.setContentType(attachment.getContentType());
            attachmentDTO.setSize(attachment.getSize());
            return attachmentDTO;
        }).toList());

        return dto;
    }


    private SupplierInvoice mapToEntity(SupplierInvoiceDTO dto) {
        SupplierInvoice invoice = new SupplierInvoice();
        applyUpdatableFields(invoice, dto);
        return invoice;
    }

    private void applyUpdatableFields(SupplierInvoice invoice, SupplierInvoiceDTO dto) {
        invoice.setSupplierId(dto.getSupplierId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setAccountingDate(dto.getAccountingDate());
        invoice.setCurrency(dto.getCurrency());
        invoice.setInvoiceAmount(dto.getInvoiceAmount());
        invoice.setMemo(dto.getMemo());
        invoice.setLines(mapLines(dto.getLines()));
        invoice.setAttachmentsMetadata(mapAttachments(dto.getAttachmentsMetadata()));
    }

    private List<SupplierInvoice.Line> mapLines(List<SupplierInvoiceDTO.LineDTO> lineDTOs) {
        return safeList(lineDTOs).stream().map(lineDTO -> {
            SupplierInvoice.Line line = new SupplierInvoice.Line();
            line.setDescription(lineDTO.getDescription());
            line.setAmount(lineDTO.getAmount());
            return line;
        }).toList();
    }

    private List<SupplierInvoice.AttachmentMetadata> mapAttachments(List<SupplierInvoiceDTO.AttachmentMetadataDTO> attachmentDTOs) {
        return safeList(attachmentDTOs).stream().map(attDTO -> {
            SupplierInvoice.AttachmentMetadata metadata = new SupplierInvoice.AttachmentMetadata();
            metadata.setFileName(attDTO.getFileName());
            metadata.setContentType(attDTO.getContentType());
            metadata.setSize(attDTO.getSize());
            return metadata;
        }).toList();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    // ---- Audit helpers ----

    private void touchCreate(SupplierInvoice invoice) {
        Instant now = Instant.now();
        invoice.setCreatedBy(SYSTEM_USER);
        invoice.setUpdatedBy(SYSTEM_USER);
        invoice.setUpdatedAt(now);
        // If you have createdAt on the entity, set it here too:
        // invoice.setCreatedAt(now);
    }

    private void touchUpdate(SupplierInvoice invoice) {
        invoice.setUpdatedAt(Instant.now());
        invoice.setUpdatedBy(SYSTEM_USER);
    }

    // ---- Error helpers ----

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

}

