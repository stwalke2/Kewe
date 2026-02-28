package com.kewe.core.agent;

import java.util.List;

public record SupplierSearchOutcome(List<SupplierSearchResult> results,
                                    List<String> warnings,
                                    boolean blockedOrFailed,
                                    long elapsedMs) {
    public static SupplierSearchOutcome empty(long elapsedMs, boolean blockedOrFailed, String warning) {
        return new SupplierSearchOutcome(List.of(), warning == null ? List.of() : List.of(warning), blockedOrFailed, elapsedMs);
    }
}
