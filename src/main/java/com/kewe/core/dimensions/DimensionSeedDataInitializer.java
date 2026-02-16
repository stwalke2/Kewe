package com.kewe.core.dimensions;

import com.kewe.core.dimensions.dto.AwardDriverToFundMappingRequest;
import com.kewe.core.dimensions.dto.CostCenterToOrgMappingRequest;
import com.kewe.core.dimensions.dto.DefaultFunctionMappingRequest;
import com.kewe.core.dimensions.dto.DimensionNodeRequest;
import com.kewe.core.dimensions.dto.DimensionTypeRequest;
import com.kewe.core.dimensions.dto.ItemToLedgerMappingRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DimensionSeedDataInitializer {

    @Bean
    CommandLineRunner seedDimensionData(DimensionTypeService typeService,
                                        DimensionNodeService nodeService,
                                        MappingService mappingService,
                                        DimensionTypeRepository typeRepository) {
        return args -> {
            if (typeRepository.count() > 0) {
                return;
            }

            List<String> types = List.of(
                    createType(typeService, "LEDGER_ACCOUNT", "Ledger Account", "Natural account structure", true, 6, EntryBehavior.REQUIRED),
                    createType(typeService, "SPEND_ITEM", "Spend Item", "Spend item catalog", true, 6, EntryBehavior.REQUIRED),
                    createType(typeService, "REVENUE_ITEM", "Revenue Item", "Revenue item catalog", true, 6, EntryBehavior.REQUIRED),
                    createType(typeService, "ORGANIZATION", "Organization", "Structural organization chart", true, 6, EntryBehavior.REQUIRED),
                    createType(typeService, "COST_CENTER", "Cost Center", "Budgetary responsibility centers", true, 6, EntryBehavior.REQUIRED),
                    createType(typeService, "PROGRAM", "Program", "Internal program grouping", true, 6, EntryBehavior.OPTIONAL),
                    createType(typeService, "FUNCTION", "Function", "NACUBO functional expense category", true, 6, EntryBehavior.DERIVED),
                    createType(typeService, "FUND", "Fund", "Funding source dimension", true, 6, EntryBehavior.DERIVED),
                    createType(typeService, "GIFT", "Gift", "Gift driver", true, 6, EntryBehavior.OPTIONAL),
                    createType(typeService, "GRANT", "Grant", "Grant driver", true, 6, EntryBehavior.OPTIONAL),
                    createType(typeService, "PROJECT", "Project", "Project driver", true, 6, EntryBehavior.OPTIONAL),
                    createType(typeService, "APPROPRIATION", "Appropriation", "Appropriation driver", true, 6, EntryBehavior.OPTIONAL)
            );

            if (types.isEmpty()) {
                return;
            }

            DimensionNode orgAcademic = createNode(nodeService, "ORGANIZATION", "ORG-ACA", "Academic Affairs", null);
            DimensionNode costSci = createNode(nodeService, "COST_CENTER", "CC-SCI", "College of Science", null);
            DimensionNode fundGeneral = createNode(nodeService, "FUND", "FUND-GEN", "General Operating", null);
            DimensionNode ledgerTravel = createNode(nodeService, "LEDGER_ACCOUNT", "6100", "Travel Expense", null);
            DimensionNode spendTravel = createNode(nodeService, "SPEND_ITEM", "SP-TRAVEL", "Travel Booking", null);
            DimensionNode revenueTuition = createNode(nodeService, "REVENUE_ITEM", "RV-TUITION", "Tuition Revenue", null);
            DimensionNode ledgerTuition = createNode(nodeService, "LEDGER_ACCOUNT", "4100", "Tuition Revenue", null);
            DimensionNode programStem = createNode(nodeService, "PROGRAM", "PG-STEM", "STEM Excellence", null);
            DimensionNode functionInstruction = createNode(nodeService, "FUNCTION", "FN-INST", "Instruction", null);
            DimensionNode giftNode = createNode(nodeService, "GIFT", "GFT-100", "Alumni Gift", null);

            ItemToLedgerMappingRequest spendToLedger = new ItemToLedgerMappingRequest();
            spendToLedger.setItemTypeCode("SPEND_ITEM");
            spendToLedger.setItemNodeId(spendTravel.getId());
            spendToLedger.setLedgerAccountNodeId(ledgerTravel.getId());
            mappingService.setItemToLedger(spendToLedger);

            ItemToLedgerMappingRequest revenueToLedger = new ItemToLedgerMappingRequest();
            revenueToLedger.setItemTypeCode("REVENUE_ITEM");
            revenueToLedger.setItemNodeId(revenueTuition.getId());
            revenueToLedger.setLedgerAccountNodeId(ledgerTuition.getId());
            mappingService.setItemToLedger(revenueToLedger);

            CostCenterToOrgMappingRequest costCenterToOrg = new CostCenterToOrgMappingRequest();
            costCenterToOrg.setCostCenterNodeId(costSci.getId());
            costCenterToOrg.setOrganizationNodeId(orgAcademic.getId());
            mappingService.setCostCenterToOrg(costCenterToOrg);

            AwardDriverToFundMappingRequest giftToFund = new AwardDriverToFundMappingRequest();
            giftToFund.setDriverTypeCode("GIFT");
            giftToFund.setDriverNodeId(giftNode.getId());
            giftToFund.setFundNodeId(fundGeneral.getId());
            mappingService.setAwardDriverToFund(giftToFund);

            AwardDriverToFundMappingRequest noneToFund = new AwardDriverToFundMappingRequest();
            noneToFund.setDriverTypeCode("NONE");
            noneToFund.setFundNodeId(fundGeneral.getId());
            mappingService.setAwardDriverToFund(noneToFund);

            DefaultFunctionMappingRequest defaultFunction = new DefaultFunctionMappingRequest();
            defaultFunction.setSourceTypeCode("PROGRAM");
            defaultFunction.setSourceNodeId(programStem.getId());
            defaultFunction.setFunctionNodeId(functionInstruction.getId());
            mappingService.setDefaultFunction(defaultFunction);
        };
    }

    private String createType(DimensionTypeService service, String code, String name, String description, boolean hierarchical,
                              int maxDepth, EntryBehavior entryBehavior) {
        DimensionTypeRequest request = new DimensionTypeRequest();
        request.setCode(code);
        request.setName(name);
        request.setDescription(description);
        request.setHierarchical(hierarchical);
        request.setMaxDepth(maxDepth);
        request.setEntryBehavior(entryBehavior);
        return service.create(request).getCode();
    }

    private DimensionNode createNode(DimensionNodeService service, String typeCode, String code, String name, String parentId) {
        DimensionNodeRequest request = new DimensionNodeRequest();
        request.setCode(code);
        request.setName(name);
        request.setParentId(parentId);
        return service.createNode(typeCode, request);
    }
}
