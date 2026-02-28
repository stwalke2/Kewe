package com.kewe.core.agent;

import java.math.BigDecimal;

public record SupplierSearchResult(String supplierName,
                                   String title,
                                   String url,
                                   BigDecimal price,
                                   String sku,
                                   String snippet) {
}
