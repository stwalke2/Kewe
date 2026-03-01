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
class ChargingLocationsIntegrationTest {
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

    private String eligibleFromAllocation;

    @BeforeEach
    void setup() {
        allocationRepository.deleteAll();
        budgetRepository.deleteAll();
        businessObjectRepository.deleteAll();
        typeRepository.deleteAll();

        BusinessObjectType cc = new BusinessObjectType();
        cc.setCode("COST_CENTER"); cc.setName("Cost Center"); cc.setStatus("Active"); cc.setObjectKind("Business Dimension");
        typeRepository.save(cc);

        BusinessObjectInstance withBudget = new BusinessObjectInstance();
        withBudget.setTypeCode("COST_CENTER"); withBudget.setObjectKind("Business Dimension"); withBudget.setCode("CC0001"); withBudget.setName("Biology"); withBudget.setStatus("Active");
        withBudget = businessObjectRepository.save(withBudget);

        BusinessObjectInstance withAllocation = new BusinessObjectInstance();
        withAllocation.setTypeCode("COST_CENTER"); withAllocation.setObjectKind("Business Dimension"); withAllocation.setCode("CC0002"); withAllocation.setName("Chemistry"); withAllocation.setStatus("Active");
        withAllocation = businessObjectRepository.save(withAllocation);
        eligibleFromAllocation = withAllocation.getId();

        BusinessObjectInstance ineligible = new BusinessObjectInstance();
        ineligible.setTypeCode("COST_CENTER"); ineligible.setObjectKind("Business Dimension"); ineligible.setCode("CC0003"); ineligible.setName("Unfunded"); ineligible.setStatus("Active");
        businessObjectRepository.save(ineligible);

        BudgetRecord budget = new BudgetRecord();
        budget.setBusinessDimensionId(withBudget.getId()); budget.setBudgetPlanId("FY26-OPERATING"); budget.setBudgetPlanName("FY26 Operating"); budget.setAmount(10000);
        budgetRepository.save(budget);

        AllocationRecord allocation = new AllocationRecord();
        allocation.setAllocatedFromDimensionId(withBudget.getId()); allocation.setAllocatedToDimensionId(withAllocation.getId()); allocation.setBudgetPlanId("FY26-OPERATING"); allocation.setAmount(2500);
        allocationRepository.save(allocation);
    }

    @Test
    void shouldReturnOnlyEligibleChargingLocations() throws Exception {
        mockMvc.perform(get("/api/charging-locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'CC0001')]").exists())
                .andExpect(jsonPath("$[?(@.id == '%s')]", eligibleFromAllocation).exists())
                .andExpect(jsonPath("$[?(@.code == 'CC0003')]").doesNotExist());
    }
}
