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
    void shouldCreateTypeAndObjectAndAllowOverride() throws Exception {
        mockMvc.perform(post("/api/business-object-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "FUND",
                                  "name": "Fund",
                                  "objectKind": "FundingSource",
                                  "allowInstanceAccountingBudgetOverride": true,
                                  "accountingBudgetDefaults": {
                                    "budgetRequired": {"value": true, "allowOverride": true, "overrideRequiresReason": true}
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("FUND"));

        String created = mockMvc.perform(post("/api/business-object-types/objects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "typeCode": "FUND",
                                  "code": "F100",
                                  "name": "General Fund",
                                  "roles": [{"roleCode": "Manager", "assigneeId": "user-1"}],
                                  "hierarchies": [{"hierarchyCode": "PRIMARY"}],
                                  "accountingBudgetOverride": {
                                    "budgetRequired": {"value": false, "allowOverride": true, "overrideRequiresReason": false}
                                  },
                                  "overrideReason": "Initial setup"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0].roleCode").value("Manager"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(created);
        mockMvc.perform(put("/api/business-object-types/objects/" + node.get("id").asText() + "/accounting-budget-override?reason=Policy%20change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountingBudgetDefaults": {
                                    "allowExpense": {"value": true, "allowOverride": true, "overrideRequiresReason": false}
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountingBudgetSetup.allowExpense.value").value(true));
    }
}
