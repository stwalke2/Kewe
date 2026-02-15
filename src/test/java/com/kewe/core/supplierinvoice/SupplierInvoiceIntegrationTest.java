package com.kewe.core.supplierinvoice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SupplierInvoiceIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRunSupplierInvoiceLifecycleDraftToPosted() throws Exception {
        String draftPayload = validDraftPayload();

        String createResponse = mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Draft"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        String id = created.get("id").asText();
        assertThat(id).isNotBlank();

        mockMvc.perform(put("/supplier-invoices/{id}/submit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Submitted"));

        mockMvc.perform(put("/supplier-invoices/{id}/approve", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Approved"));

        mockMvc.perform(put("/supplier-invoices/{id}/post", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Posted"));
    }

    @Test
    void shouldAllowDraftCreationWithMinimalFieldsAndListInvoices() throws Exception {
        String draftPayload = """
                {
                  "supplierId": "SUP-200",
                  "invoiceNumber": "INV-2026-100"
                }
                """;

        mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Draft"));

        mockMvc.perform(get("/supplier-invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void shouldRejectSubmitWhenRequiredFieldsAreMissing() throws Exception {
        String createResponse = mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": "SUP-300",
                                  "invoiceNumber": "INV-2026-300"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/supplier-invoices/{id}/submit", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invoiceDate is required before submit"));
    }

    @Test
    void shouldRejectSubmitWhenLineTotalsDoNotMatchInvoiceAmount() throws Exception {
        String draftPayload = """
                {
                  "supplierId": "SUP-400",
                  "invoiceNumber": "INV-2026-400",
                  "invoiceDate": "2026-02-01",
                  "accountingDate": "2026-02-02",
                  "currency": "USD",
                  "invoiceAmount": 900.00,
                  "lines": [
                    {"description": "Item A", "amount": 500.00},
                    {"description": "Item B", "amount": 600.00}
                  ]
                }
                """;

        String createResponse = mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/supplier-invoices/{id}/submit", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invoiceAmount must equal the sum of all line amounts"));
    }

    @Test
    void shouldAllowUpdatesOnlyForDraftInvoices() throws Exception {
        String createResponse = mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDraftPayload()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(put("/supplier-invoices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": "SUP-100",
                                  "invoiceNumber": "INV-2026-UPDATED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-2026-UPDATED"));

        mockMvc.perform(put("/supplier-invoices/{id}/submit", id))
                .andExpect(status().isOk());

        mockMvc.perform(put("/supplier-invoices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": "SUP-100",
                                  "invoiceNumber": "INV-2026-FINAL"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only Draft invoices can be edited"));
    }

    @Test
    void shouldReturnFieldErrorsWhenBeanValidationFails() throws Exception {
        mockMvc.perform(post("/supplier-invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.path").value("/supplier-invoices"));
    }

    private String validDraftPayload() {
        return """
                {
                  "supplierId": "SUP-100",
                  "invoiceNumber": "INV-2026-001",
                  "invoiceDate": "2026-02-01",
                  "accountingDate": "2026-02-02",
                  "currency": "USD",
                  "invoiceAmount": 1500.25,
                  "lines": [
                    {"description": "Consulting services", "amount": 1000.00},
                    {"description": "Travel reimbursement", "amount": 500.25}
                  ],
                  "memo": "MVP workflow test",
                  "attachmentsMetadata": [
                    {"fileName": "invoice.pdf", "contentType": "application/pdf", "size": 51234}
                  ]
                }
                """;
    }
}
