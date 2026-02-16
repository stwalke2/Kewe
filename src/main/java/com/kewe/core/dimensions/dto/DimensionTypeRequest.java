package com.kewe.core.dimensions.dto;

import com.kewe.core.dimensions.EntryBehavior;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DimensionTypeRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Boolean hierarchical;
    @NotNull
    @Positive
    private Integer maxDepth;
    @NotNull
    private EntryBehavior entryBehavior;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getHierarchical() { return hierarchical; }
    public void setHierarchical(Boolean hierarchical) { this.hierarchical = hierarchical; }
    public Integer getMaxDepth() { return maxDepth; }
    public void setMaxDepth(Integer maxDepth) { this.maxDepth = maxDepth; }
    public EntryBehavior getEntryBehavior() { return entryBehavior; }
    public void setEntryBehavior(EntryBehavior entryBehavior) { this.entryBehavior = entryBehavior; }
}
