package com.kewe.core.agent;

import java.util.List;

public interface SupplierSearchClient {
    String supplierName();
    String searchLink(String query);
    List<SupplierResult> search(String query);
}
