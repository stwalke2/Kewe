package com.kewe.core.dimensions;

import com.kewe.core.common.CanonicalObject;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "dimension_mappings")
@CompoundIndexes({
        @CompoundIndex(name = "uk_mapping_source", def = "{'mappingType':1,'sourceTypeCode':1,'sourceNodeId':1,'sourceKey':1}", unique = true),
        @CompoundIndex(name = "idx_mapping_lookup", def = "{'mappingType':1,'sourceTypeCode':1,'sourceNodeId':1}"),
        @CompoundIndex(name = "idx_mapping_target", def = "{'mappingType':1,'targetNodeId':1}")
})
public class DimensionMapping extends CanonicalObject {

    private MappingType mappingType;
    private String sourceTypeCode;
    private String sourceNodeId;
    private String sourceKey;
    private String targetTypeCode;
    private String targetNodeId;
    private Map<String, Object> context = new HashMap<>();

    public MappingType getMappingType() { return mappingType; }
    public void setMappingType(MappingType mappingType) { this.mappingType = mappingType; }
    public String getSourceTypeCode() { return sourceTypeCode; }
    public void setSourceTypeCode(String sourceTypeCode) { this.sourceTypeCode = sourceTypeCode; }
    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getTargetTypeCode() { return targetTypeCode; }
    public void setTargetTypeCode(String targetTypeCode) { this.targetTypeCode = targetTypeCode; }
    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
