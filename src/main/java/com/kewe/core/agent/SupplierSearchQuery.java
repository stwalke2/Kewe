package com.kewe.core.agent;

import java.util.Map;

public record SupplierSearchQuery(String keywords, int limit, Map<String, String> meta) {
    public SupplierSearchQuery {
        limit = limit <= 0 ? 5 : limit;
        meta = meta == null ? Map.of() : Map.copyOf(meta);
        keywords = keywords == null ? "" : keywords.trim();
    }

    public static SupplierSearchQuery of(String keywords) {
        return new SupplierSearchQuery(keywords, 5, Map.of());
    }
}
