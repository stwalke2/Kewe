package com.kewe.core.businessobjects;

public class ConfiguredField<T> {

    private T value;
    private boolean allowOverride;
    private boolean overrideRequiresReason;

    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
    public boolean isAllowOverride() { return allowOverride; }
    public void setAllowOverride(boolean allowOverride) { this.allowOverride = allowOverride; }
    public boolean isOverrideRequiresReason() { return overrideRequiresReason; }
    public void setOverrideRequiresReason(boolean overrideRequiresReason) { this.overrideRequiresReason = overrideRequiresReason; }
}
