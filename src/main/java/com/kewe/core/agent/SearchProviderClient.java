package com.kewe.core.agent;

import java.util.List;

public interface SearchProviderClient {
    List<SearchResult> searchWeb(String query, int limit);
}
