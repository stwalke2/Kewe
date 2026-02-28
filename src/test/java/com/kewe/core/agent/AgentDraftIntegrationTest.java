package com.kewe.core.agent;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import com.kewe.core.funding.AllocationRecord;
import com.kewe.core.funding.AllocationRecordRepository;
import com.kewe.core.funding.BudgetRecord;
import com.kewe.core.funding.BudgetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AgentDraftIntegrationTest {
    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.14");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HtmlFetcher htmlFetcher;
    @Autowired
    private BusinessObjectRepository businessObjectRepository;
    @Autowired
    private BusinessObjectTypeRepository typeRepository;
    @Autowired
    private BudgetRecordRepository budgetRepository;
    @Autowired
    private AllocationRecordRepository allocationRepository;

    @BeforeEach
    void setup() throws Exception {
        allocationRepository.deleteAll();
        budgetRepository.deleteAll();
        businessObjectRepository.deleteAll();
        typeRepository.deleteAll();

        BusinessObjectType cc = new BusinessObjectType();
        cc.setCode("COST_CENTER"); cc.setName("Cost Center"); cc.setStatus("Active"); cc.setObjectKind("Business Dimension");
        typeRepository.save(cc);

        BusinessObjectInstance biology = new BusinessObjectInstance();
        biology.setTypeCode("COST_CENTER"); biology.setObjectKind("Business Dimension"); biology.setCode("CC0001"); biology.setName("Biology"); biology.setStatus("Active");
        businessObjectRepository.save(biology);

        BudgetRecord budget = new BudgetRecord();
        budget.setBusinessDimensionId(biology.getId()); budget.setBudgetPlanId("FY26-OPERATING"); budget.setBudgetPlanName("FY26 Operating"); budget.setAmount(10000);
        budgetRepository.save(budget);

        AllocationRecord allocation = new AllocationRecord();
        allocation.setAllocatedFromDimensionId(biology.getId()); allocation.setAllocatedToDimensionId(biology.getId()); allocation.setBudgetPlanId("FY26-OPERATING"); allocation.setAmount(1200);
        allocationRepository.save(allocation);

        when(htmlFetcher.fetch(contains("fishersci"))).thenReturn(Files.readString(Path.of("src/test/resources/fixtures/fisher-search.html")));
        when(htmlFetcher.fetch(contains("homedepot"))).thenReturn(Files.readString(Path.of("src/test/resources/fixtures/homedepot-search.html")));
        when(htmlFetcher.fetch(contains("amazon"))).thenReturn("<html><body>blocked</body></html>");
    }

    @Test
    void shouldBuildDraftFromPromptAndSupplierResults() throws Exception {
        mockMvc.perform(post("/api/agent/requisition-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"I need to purchase 6 5ml glass beakers for biology\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parsed.quantity").value(6))
                .andExpect(jsonPath("$.suggestedChargingDimension.code").value("CC0001"))
                .andExpect(jsonPath("$.results.fisher").isArray())
                .andExpect(jsonPath("$.results.homedepot").isArray())
                .andExpect(jsonPath("$.searchLinks.amazon").exists());
    }
}
