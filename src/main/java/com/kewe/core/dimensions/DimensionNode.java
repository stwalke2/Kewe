package com.kewe.core.dimensions;

import com.kewe.core.businessobjects.BusinessObject;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "dimension_nodes")
@CompoundIndexes({
        @CompoundIndex(name = "uk_type_code", def = "{'typeCode': 1, 'code': 1}", unique = true),
        @CompoundIndex(name = "idx_type_parent_sort", def = "{'typeCode': 1, 'parentId': 1, 'sortOrder': 1}"),
        @CompoundIndex(name = "idx_type_path", def = "{'typeCode': 1, 'path': 1}"),
        @CompoundIndex(name = "idx_type_name", def = "{'typeCode': 1, 'name': 1}"),
        @CompoundIndex(name = "idx_type_status", def = "{'typeCode': 1, 'status': 1}")
})
public class DimensionNode extends BusinessObject {

    private String parentId;
    private String path;
    private Integer depth;
    private Integer sortOrder;
    private Map<String, Object> attributes = new HashMap<>();
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getDepth() { return depth; }
    public void setDepth(Integer depth) { this.depth = depth; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
