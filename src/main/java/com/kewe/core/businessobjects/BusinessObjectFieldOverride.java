package com.kewe.core.businessobjects;

public class BusinessObjectFieldOverride {
    private Object value;
    private String overrideReason;

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}
