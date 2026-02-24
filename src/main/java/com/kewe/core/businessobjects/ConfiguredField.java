package com.kewe.core.businessobjects;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ConfiguredField<T> {

    private T defaultValue;
    private boolean allowOverride;
    private boolean overrideReasonRequired;

    public T getDefaultValue() { return defaultValue; }
    public void setDefaultValue(T defaultValue) { this.defaultValue = defaultValue; }

    @JsonAlias("value")
    public void setValue(T value) { this.defaultValue = value; }

    public T getValue() { return defaultValue; }
    public boolean isAllowOverride() { return allowOverride; }
    public void setAllowOverride(boolean allowOverride) { this.allowOverride = allowOverride; }
    public boolean isOverrideReasonRequired() { return overrideReasonRequired; }

    @JsonAlias("overrideRequiresReason")
    public void setOverrideRequiresReason(boolean overrideRequiresReason) { this.overrideReasonRequired = overrideRequiresReason; }

    public boolean isOverrideRequiresReason() { return overrideReasonRequired; }
    public void setOverrideReasonRequired(boolean overrideReasonRequired) { this.overrideReasonRequired = overrideReasonRequired; }
}
