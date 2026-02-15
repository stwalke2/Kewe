package com.kewe.core.supplierinvoice;

import com.kewe.core.common.CanonicalObject;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "supplier_invoices")
public class SupplierInvoice extends CanonicalObject {

    private String supplierId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate accountingDate;
    private String currency;
    private BigDecimal invoiceAmount;
    private List<Line> lines = new ArrayList<>();
    private String memo;
    private List<AttachmentMetadata> attachmentsMetadata = new ArrayList<>();

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getAccountingDate() {
        return accountingDate;
    }

    public void setAccountingDate(LocalDate accountingDate) {
        this.accountingDate = accountingDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public List<AttachmentMetadata> getAttachmentsMetadata() {
        return attachmentsMetadata;
    }

    public void setAttachmentsMetadata(List<AttachmentMetadata> attachmentsMetadata) {
        this.attachmentsMetadata = attachmentsMetadata;
    }

    public static class Line {
        private String description;
        private BigDecimal amount;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class AttachmentMetadata {
        private String fileName;
        private String contentType;
        private Long size;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }
    }
}
