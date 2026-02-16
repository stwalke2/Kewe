package com.kewe.core.dimensions.dto;

public class MoveNodeRequest {
    private String nodeId;
    private String newParentId;

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNewParentId() { return newParentId; }
    public void setNewParentId(String newParentId) { this.newParentId = newParentId; }
}
