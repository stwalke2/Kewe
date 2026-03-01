package com.kewe.core.agent;

import java.util.List;

public record WebSearchResponse(List<WebSearchResult> results, List<String> warnings) {
}
