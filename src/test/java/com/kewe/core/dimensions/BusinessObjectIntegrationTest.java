package com.kewe.core.dimensions;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BusinessObjectIntegrationTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.14");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTypeAndSupportInstanceOverrideGuardrails() throws Exception {
        String typePayload = """
                {
                  "code": "FUND",
                  "name": "Fund",
                  "objectKind": "FundingSource",
                  "allowInstanceAccountingBudgetOverride": true,
                  "accountingBudgetDefaults": {
                    "budgetRequired": {"defaultValue": true, "allowOverride": false, "overrideReasonRequired": false},
                    "allowExpensePosting": {"defaultValue": true, "allowOverride": true, "overrideReasonRequired": true},
                    "budgetControlLevel": {"defaultValue": "HARD", "allowOverride": true, "overrideReasonRequired": false}
                  }
                }
                """;

        mockMvc.perform(post("/api/business-object-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(typePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountingBudgetDefaults.budgetControlLevel.defaultValue").value("HARD"));

        String created = mockMvc.perform(post("/api/business-object-types/objects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "typeCode": "FUND",
                                  "code": "F100",
                                  "name": "General Fund"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountingBudgetOverrides").isMap())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(created);
        String objectId = node.get("id").asText();

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "budgetRequired": {"value": false}
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Override is not allowed for field: budgetRequired"));

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "allowExpensePosting": {"value": false}
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Override reason is required for field: allowExpensePosting"));

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "allowExpensePosting": {"value": false, "overrideReason": "Policy exception"}
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountingBudgetOverrides.allowExpensePosting.value").value(false));

        mockMvc.perform(get("/api/business-object-types/FUND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountingBudgetDefaults.allowExpensePosting.defaultValue").value(true));
    }

    @Test
    void shouldSupportChargeObjectDefaultsInheritanceAndOverrideRules() throws Exception {
        String typePayload = """
                {
                  "code": "PROJECT",
                  "name": "Project",
                  "objectKind": "Grant",
                  "allowInstanceAccountingBudgetOverride": true,
                  "accountingBudgetDefaults": {
                    "chargeObjectEnabled": {"defaultValue": true, "allowOverride": true, "overrideReasonRequired": false},
                    "budgetCheckPoint": {"defaultValue": "INVOICE", "allowOverride": false, "overrideReasonRequired": false},
                    "liquiditySourceMode": {"defaultValue": "SELF", "allowOverride": true, "overrideReasonRequired": true}
                  }
                }
                """;

        mockMvc.perform(post("/api/business-object-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(typePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountingBudgetDefaults.chargeObjectEnabled.defaultValue").value(true))
                .andExpect(jsonPath("$.accountingBudgetDefaults.budgetCheckPoint.defaultValue").value("INVOICE"));

        String created = mockMvc.perform(post("/api/business-object-types/objects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "typeCode": "PROJECT",
                                  "code": "P100",
                                  "name": "Project 100"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountingBudgetOverrides").isMap())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(created);
        String objectId = node.get("id").asText();

        mockMvc.perform(get("/api/business-object-types/PROJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountingBudgetDefaults.chargeObjectEnabled.defaultValue").value(true));

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "budgetCheckPoint": {"value": "PO"}
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Override is not allowed for field: budgetCheckPoint"));

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "liquiditySourceMode": {"value": "BRIDGE"}
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Override reason is required for field: liquiditySourceMode"));

        mockMvc.perform(put("/api/business-object-types/objects/" + objectId + "/accounting-budget-override")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "overrides": {
                                    "liquiditySourceMode": {"value": "BRIDGE", "overrideReason": "Temporary bridge funding"}
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountingBudgetOverrides.liquiditySourceMode.value").value("BRIDGE"));
    }
}
