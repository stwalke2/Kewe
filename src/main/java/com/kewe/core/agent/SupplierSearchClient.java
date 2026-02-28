package com.kewe.core.agent;

public interface SupplierSearchClient {
    String supplierKey();
    String supplierName();
    String searchLink(String query);
    SupplierSearchOutcome search(SupplierSearchQuery query);
}
