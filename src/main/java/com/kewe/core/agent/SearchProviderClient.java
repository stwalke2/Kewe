package com.kewe.core.agent;

import java.util.List;

public interface SearchProviderClient {
    List<WebSearchResult> searchWeb(String query, int limit);
}
