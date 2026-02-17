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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DimensionIntegrationTest {

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
    void shouldCreateTypeAndNodeAndPreventDuplicateCodes() throws Exception {
        mockMvc.perform(post("/api/dimension-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "TEST_DIM",
                                  "name": "Test Dimension",
                                  "description": "For integration tests",
                                  "hierarchical": true,
                                  "maxDepth": 3,
                                  "entryBehavior": "OPTIONAL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST_DIM"));

        String createNodeResponse = mockMvc.perform(post("/api/dimensions/TEST_DIM/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "N-001",
                                  "name": "Node 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("N-001"))
                .andReturn().getResponse().getContentAsString();

        String nodeId = objectMapper.readTree(createNodeResponse).get("id").asText();
        assertThat(nodeId).isNotBlank();

        mockMvc.perform(post("/api/dimensions/TEST_DIM/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "N-001",
                                  "name": "Node Duplicate"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldPreventCycleAndUpdatePathOnMove() throws Exception {
        mockMvc.perform(post("/api/dimension-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "TREE_DIM",
                                  "name": "Tree Dimension",
                                  "hierarchical": true,
                                  "maxDepth": 5,
                                  "entryBehavior": "OPTIONAL"
                                }
                                """))
                .andExpect(status().isCreated());

        String rootResp = mockMvc.perform(post("/api/dimensions/TREE_DIM/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"ROOT\"," +
                                "\"name\":\"Root\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(rootResp);

        String childResp = mockMvc.perform(post("/api/dimensions/TREE_DIM/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"CHILD\"," +
                                "\"name\":\"Child\"," +
                                "\"parentId\":\"" + root.get("id").asText() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode child = objectMapper.readTree(childResp);

        String grandResp = mockMvc.perform(post("/api/dimensions/TREE_DIM/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"GRAND\"," +
                                "\"name\":\"Grand\"," +
                                "\"parentId\":\"" + child.get("id").asText() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode grand = objectMapper.readTree(grandResp);

        mockMvc.perform(post("/api/dimensions/TREE_DIM/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"nodeId\":\"" + root.get("id").asText() + "\"," +
                                "\"newParentId\":\"" + grand.get("id").asText() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Move would create cycle"));

        mockMvc.perform(post("/api/dimensions/TREE_DIM/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"nodeId\":\"" + child.get("id").asText() + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dimensions/TREE_DIM/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code=='CHILD')].depth").value(org.hamcrest.Matchers.contains(0)))
                .andExpect(jsonPath("$[?(@.code=='GRAND')].path").exists());
    }


    @Test
    void shouldUpdateNodeParentWithPut() throws Exception {
        mockMvc.perform(post("/api/dimension-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "MOVE_WITH_UPDATE",
                                  "name": "Move With Update",
                                  "hierarchical": true,
                                  "maxDepth": 5,
                                  "entryBehavior": "OPTIONAL"
                                }
                                """))
                .andExpect(status().isCreated());

        String rootResp = mockMvc.perform(post("/api/dimensions/MOVE_WITH_UPDATE/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ROOT\",\"name\":\"Root\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(rootResp);

        String childResp = mockMvc.perform(post("/api/dimensions/MOVE_WITH_UPDATE/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CHILD\",\"name\":\"Child\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode child = objectMapper.readTree(childResp);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/dimensions/MOVE_WITH_UPDATE/nodes/" + child.get("id").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"code\":\"CHILD\"," +
                                "\"name\":\"Child\"," +
                                "\"parentId\":\"" + root.get("id").asText() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(root.get("id").asText()))
                .andExpect(jsonPath("$.depth").value(1));
    }

    @Test
    void shouldLookupMappings() throws Exception {
        String spendItems = mockMvc.perform(get("/api/dimensions/SPEND_ITEM/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String ledgerItems = mockMvc.perform(get("/api/dimensions/LEDGER_ACCOUNT/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String costCenters = mockMvc.perform(get("/api/dimensions/COST_CENTER/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String orgs = mockMvc.perform(get("/api/dimensions/ORGANIZATION/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String gifts = mockMvc.perform(get("/api/dimensions/GIFT/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String funds = mockMvc.perform(get("/api/dimensions/FUND/tree"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String spendId = objectMapper.readTree(spendItems).get(0).get("id").asText();
        String ledgerId = objectMapper.readTree(ledgerItems).get(0).get("id").asText();
        String costCenterId = objectMapper.readTree(costCenters).get(0).get("id").asText();
        String orgId = objectMapper.readTree(orgs).get(0).get("id").asText();
        String giftId = objectMapper.readTree(gifts).get(0).get("id").asText();
        String fundId = objectMapper.readTree(funds).get(0).get("id").asText();

        mockMvc.perform(post("/api/dimensions/mappings/item-to-ledger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"itemTypeCode\":\"SPEND_ITEM\"," +
                                "\"itemNodeId\":\"" + spendId + "\"," +
                                "\"ledgerAccountNodeId\":\"" + ledgerId + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/dimensions/mappings/costcenter-to-org")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"costCenterNodeId\":\"" + costCenterId + "\"," +
                                "\"organizationNodeId\":\"" + orgId + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/dimensions/mappings/awarddriver-to-fund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"driverTypeCode\":\"GIFT\"," +
                                "\"driverNodeId\":\"" + giftId + "\"," +
                                "\"fundNodeId\":\"" + fundId + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dimensions/mappings/item-to-ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetNodeId").value(ledgerId));

        mockMvc.perform(get("/api/dimensions/mappings/costcenter-to-org"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetNodeId").value(orgId));

        mockMvc.perform(get("/api/dimensions/mappings/awarddriver-to-fund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetNodeId").value(fundId));
    }
}
