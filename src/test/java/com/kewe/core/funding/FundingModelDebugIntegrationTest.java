package com.kewe.core.funding;

import com.kewe.core.businessobjects.BusinessObjectInstance;
import com.kewe.core.businessobjects.BusinessObjectRepository;
import com.kewe.core.businessobjects.BusinessObjectType;
import com.kewe.core.businessobjects.BusinessObjectTypeRepository;
import com.kewe.core.requisition.RequisitionDraft;
import com.kewe.core.requisition.RequisitionDraftRepository;
import com.kewe.core.requisition.RequisitionLine;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FundingModelDebugIntegrationTest {
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
    @Autowired private RequisitionDraftRepository requisitionDraftRepository;

    private String eligibleDimensionId;

    @BeforeEach
    void setup() {
        requisitionDraftRepository.deleteAll();
        allocationRepository.deleteAll();
        budgetRepository.deleteAll();
        businessObjectRepository.deleteAll();
        typeRepository.deleteAll();

        BusinessObjectType cc = new BusinessObjectType();
        cc.setCode("COST_CENTER");
        cc.setName("Cost Center");
        cc.setStatus("Active");
        cc.setObjectKind("Business Dimension");
        typeRepository.save(cc);

        BusinessObjectInstance fundedDimension = new BusinessObjectInstance();
        fundedDimension.setTypeCode("COST_CENTER");
        fundedDimension.setObjectKind("Business Dimension");
        fundedDimension.setCode("CC1000");
        fundedDimension.setName("Biology");
        fundedDimension.setStatus("Active");
        fundedDimension = businessObjectRepository.save(fundedDimension);

        BusinessObjectInstance allocationDestination = new BusinessObjectInstance();
        allocationDestination.setTypeCode("COST_CENTER");
        allocationDestination.setObjectKind("Business Dimension");
        allocationDestination.setCode("CC2000");
        allocationDestination.setName("Chemistry");
        allocationDestination.setStatus("Active");
        allocationDestination = businessObjectRepository.save(allocationDestination);
        eligibleDimensionId = allocationDestination.getId();

        BudgetRecord budget = new BudgetRecord();
        budget.setBusinessDimensionId(fundedDimension.getId());
        budget.setBudgetPlanId("FY26-OPERATING");
        budget.setBudgetPlanName("FY26 Operating");
        budget.setAmount(25000);
        budgetRepository.save(budget);

        AllocationRecord validAllocation = new AllocationRecord();
        validAllocation.setBudgetPlanId("FY26-OPERATING");
        validAllocation.setAllocatedFromDimensionId(fundedDimension.getId());
        validAllocation.setAllocatedToDimensionId(allocationDestination.getId());
        validAllocation.setAmount(5000);
        allocationRepository.save(validAllocation);

        AllocationRecord orphanAllocation = new AllocationRecord();
        orphanAllocation.setBudgetPlanId("FY26-OPERATING");
        orphanAllocation.setAllocatedFromDimensionId("missing-dimension");
        orphanAllocation.setAllocatedToDimensionId("missing-destination");
        orphanAllocation.setAmount(750);
        allocationRepository.save(orphanAllocation);

        RequisitionLine line = new RequisitionLine();
        line.setLineNumber(1);
        line.setDescription("Lab equipment");
        line.setQuantity(2);
        line.setUnitPrice(1250.0);
        line.setSupplierName("Fisher Scientific");
        line.setSupplierUrl("https://example.com/item");
        line.setChargingBusinessDimensionId(allocationDestination.getId());

        RequisitionDraft requisitionDraft = new RequisitionDraft();
        requisitionDraft.setId("req-1");
        requisitionDraft.setStatus("DRAFT");
        requisitionDraft.setTitle("Science supplies");
        requisitionDraft.setRequesterName("Ada Lovelace");
        requisitionDraft.setMemo("For spring term");
        requisitionDraft.setLines(List.of(line));
        requisitionDraftRepository.save(requisitionDraft);
    }

    @Test
    void debugFundingModelEndpointShouldExposePersistedAssociationsAndWarnings() throws Exception {
        mockMvc.perform(get("/api/debug/funding-model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.businessDimensionsCount").value(2))
                .andExpect(jsonPath("$.counts.budgetsCount").value(1))
                .andExpect(jsonPath("$.counts.allocationsCount").value(2))
                .andExpect(jsonPath("$.counts.requisitionsCount").value(1))
                .andExpect(jsonPath("$.counts.requisitionLinesCount").value(1))
                .andExpect(jsonPath("$.counts.eligibleChargingLocationsCount").value(2))
                .andExpect(jsonPath("$.budgets[0].businessDimensionCode").value("CC1000"))
                .andExpect(jsonPath("$.requisitionLines[0].chargingLocationId").value(eligibleDimensionId))
                .andExpect(jsonPath("$.eligibleChargingLocations.eligibleFromBudgets.length()").value(1))
                .andExpect(jsonPath("$.eligibleChargingLocations.eligibleFromAllocationDestinations.length()").value(1))
                .andExpect(jsonPath("$.eligibleChargingLocations.finalUnionSet.length()").value(2))
                .andExpect(jsonPath("$.integrityWarnings[?(@ =~ /.*missing fromBusinessDimensionId.*/)]").exists())
                .andExpect(jsonPath("$.integrityWarnings[?(@ =~ /.*missing toBusinessDimensionId.*/)]").exists());
    }
}
