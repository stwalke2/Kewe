package com.kewe.core.dimensions;

import com.kewe.core.common.CanonicalObject;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dimension_types")
@CompoundIndexes({
        @CompoundIndex(name = "uk_dimension_type_code", def = "{'code': 1}", unique = true),
        @CompoundIndex(name = "idx_dimension_type_status", def = "{'status': 1}")
})
public class DimensionType extends CanonicalObject {

    private String code;
    private String name;
    private String description;
    private boolean hierarchical;
    private Integer maxDepth;
    private EntryBehavior entryBehavior;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isHierarchical() { return hierarchical; }
    public void setHierarchical(boolean hierarchical) { this.hierarchical = hierarchical; }
    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }
    public EntryBehavior getEntryBehavior() { return entryBehavior; }
    public void setEntryBehavior(EntryBehavior entryBehavior) { this.entryBehavior = entryBehavior; }
}
