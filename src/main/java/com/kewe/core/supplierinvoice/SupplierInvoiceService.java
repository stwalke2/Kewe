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

    private static final String STATUS_DRAFT = "Draft";
    private static final String STATUS_SUBMITTED = "Submitted";
    private static final String STATUS_APPROVED = "Approved";
    private static final String STATUS_POSTED = "Posted";

    private final SupplierInvoiceRepository repository;

    public SupplierInvoiceService(SupplierInvoiceRepository repository) {
        this.repository = repository;
    }

    public SupplierInvoice createDraft(SupplierInvoiceDTO dto) {
        validateDraftFields(dto);
        SupplierInvoice invoice = mapToEntity(dto);
        invoice.setType("SupplierInvoice");
        invoice.setStatus(STATUS_DRAFT);
        invoice.setCreatedBy("system");
        invoice.setUpdatedAt(Instant.now());
        invoice.setUpdatedBy("system");
        return repository.save(invoice);
    }

    public SupplierInvoice getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Supplier invoice not found: " + id));
    }

    public List<SupplierInvoiceDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    public SupplierInvoice updateDraft(String id, SupplierInvoiceDTO dto) {
        SupplierInvoice invoice = getById(id);
        if (!STATUS_DRAFT.equals(invoice.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only Draft invoices can be edited");
        }
        validateDraftFields(dto);

        invoice.setSupplierId(dto.getSupplierId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setAccountingDate(dto.getAccountingDate());
        invoice.setCurrency(dto.getCurrency());
        invoice.setInvoiceAmount(dto.getInvoiceAmount());
        invoice.setMemo(dto.getMemo());
        invoice.setLines(mapLines(dto.getLines()));
        invoice.setAttachmentsMetadata(mapAttachments(dto.getAttachmentsMetadata()));
        invoice.setUpdatedAt(Instant.now());
        invoice.setUpdatedBy("system");
        return repository.save(invoice);
    }

    public SupplierInvoice submit(String id) {
        SupplierInvoice invoice = getById(id);
        if (!STATUS_DRAFT.equals(invoice.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status transition: " + invoice.getStatus() + " -> " + STATUS_SUBMITTED);
        }
        validateSubmitRules(invoice);
        invoice.setStatus(STATUS_SUBMITTED);
        invoice.setUpdatedAt(Instant.now());
        invoice.setUpdatedBy("system");
        return repository.save(invoice);
    }

    public SupplierInvoice approve(String id) {
        return transition(id, STATUS_SUBMITTED, STATUS_APPROVED);
    }

    public SupplierInvoice post(String id) {
        return transition(id, STATUS_APPROVED, STATUS_POSTED);
    }

    private SupplierInvoice transition(String id, String fromStatus, String toStatus) {
        SupplierInvoice invoice = getById(id);
        if (!fromStatus.equals(invoice.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status transition: " + invoice.getStatus() + " -> " + toStatus);
        }
        invoice.setStatus(toStatus);
        invoice.setUpdatedAt(Instant.now());
        invoice.setUpdatedBy("system");
        return repository.save(invoice);
    }

    private void validateDraftFields(SupplierInvoiceDTO dto) {
        if (isBlank(dto.getSupplierId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supplierId is required for draft creation");
        }
        if (isBlank(dto.getInvoiceNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoiceNumber is required for draft creation");
        }
    }

    private void validateSubmitRules(SupplierInvoice invoice) {
        if (invoice.getInvoiceDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoiceDate is required before submit");
        }
        if (invoice.getAccountingDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "accountingDate is required before submit");
        }
        if (isBlank(invoice.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currency is required before submit");
        }
        if (invoice.getInvoiceAmount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoiceAmount is required before submit");
        }
        if (invoice.getLines() == null || invoice.getLines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one line is required before submit");
        }

        BigDecimal lineTotal = BigDecimal.ZERO;
        for (int i = 0; i < invoice.getLines().size(); i++) {
            SupplierInvoice.Line line = invoice.getLines().get(i);
            if (isBlank(line.getDescription())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "line " + (i + 1) + " description is required before submit");
            }
            if (line.getAmount() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "line " + (i + 1) + " amount is required before submit");
            }
            lineTotal = lineTotal.add(line.getAmount());
        }

        if (invoice.getInvoiceAmount().compareTo(lineTotal) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "invoiceAmount must equal the sum of all line amounts");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SupplierInvoice mapToEntity(SupplierInvoiceDTO dto) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setSupplierId(dto.getSupplierId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setAccountingDate(dto.getAccountingDate());
        invoice.setCurrency(dto.getCurrency());
        invoice.setInvoiceAmount(dto.getInvoiceAmount());
        invoice.setMemo(dto.getMemo());
        invoice.setLines(mapLines(dto.getLines()));
        invoice.setAttachmentsMetadata(mapAttachments(dto.getAttachmentsMetadata()));
        return invoice;
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
        return safeList(attachmentDTOs).stream().map(attachmentDTO -> {
            SupplierInvoice.AttachmentMetadata metadata = new SupplierInvoice.AttachmentMetadata();
            metadata.setFileName(attachmentDTO.getFileName());
            metadata.setContentType(attachmentDTO.getContentType());
            metadata.setSize(attachmentDTO.getSize());
            return metadata;
        }).toList();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

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

}
