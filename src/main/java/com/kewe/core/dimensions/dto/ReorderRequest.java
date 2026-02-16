package com.kewe.core.dimensions.dto;

import java.util.ArrayList;
import java.util.List;

public class ReorderRequest {
    private String parentId;
    private List<String> nodeIds = new ArrayList<>();

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public List<String> getNodeIds() { return nodeIds; }
    public void setNodeIds(List<String> nodeIds) { this.nodeIds = nodeIds; }
}
