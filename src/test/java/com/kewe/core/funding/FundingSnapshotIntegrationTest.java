package com.kewe.core.funding;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FundingSnapshotIntegrationTest {
    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.14");

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private BusinessObjectRepository businessObjectRepository;
    @Autowired private BusinessObjectTypeRepository typeRepository;
    @Autowired private BudgetRecordRepository budgetRepository;
    @Autowired private AllocationRecordRepository allocationRepository;

    private String biologyId;

    @BeforeEach
    void setup() {
        allocationRepository.deleteAll();
        budgetRepository.deleteAll();
        businessObjectRepository.deleteAll();
        typeRepository.deleteAll();

        BusinessObjectType cc = new BusinessObjectType();
        cc.setCode("COST_CENTER"); cc.setName("Cost Center"); cc.setStatus("Active"); cc.setObjectKind("Business Dimension");
        typeRepository.save(cc);

        BusinessObjectInstance biology = new BusinessObjectInstance();
        biology.setTypeCode("COST_CENTER"); biology.setObjectKind("Business Dimension"); biology.setCode("CC0001"); biology.setName("Biology"); biology.setStatus("Active");
        biology = businessObjectRepository.save(biology);

        BusinessObjectInstance pd = new BusinessObjectInstance();
        pd.setTypeCode("COST_CENTER"); pd.setObjectKind("Business Dimension"); pd.setCode("AT0001"); pd.setName("PD Tom Jones"); pd.setStatus("Active");
        pd = businessObjectRepository.save(pd);

        biologyId = biology.getId();

        BudgetRecord budget = new BudgetRecord();
        budget.setBusinessDimensionId(biology.getId()); budget.setBudgetPlanId("FY26-OPERATING"); budget.setBudgetPlanName("FY26 Operating"); budget.setAmount(10000);
        budgetRepository.save(budget);

        AllocationRecord allocation = new AllocationRecord();
        allocation.setAllocatedFromDimensionId(biology.getId()); allocation.setAllocatedToDimensionId(pd.getId()); allocation.setBudgetPlanId("FY26-OPERATING"); allocation.setAmount(5500);
        allocationRepository.save(allocation);
    }

    @Test
    void shouldResolveBudgetPlanByNameAndReturnTotals() throws Exception {
        mockMvc.perform(get("/api/funding-snapshot")
                        .param("chargingDimensionId", biologyId)
                        .param("budgetPlan", "fy26 operating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargingDimension.code").value("CC0001"))
                .andExpect(jsonPath("$.budget.amount").value(10000.0))
                .andExpect(jsonPath("$.allocationsFrom[0].allocatedTo.code").value("AT0001"))
                .andExpect(jsonPath("$.totals.allocatedFromTotal").value(5500.0))
                .andExpect(jsonPath("$.totals.remainingBeforeReq").value(4500.0));
    }
}
