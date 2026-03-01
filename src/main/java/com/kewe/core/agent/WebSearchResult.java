package com.kewe.core.agent;

import java.math.BigDecimal;

public record WebSearchResult(String title,
                              String url,
                              String snippet,
                              String source,
                              String domain,
                              String imageUrl,
                              BigDecimal price,
                              String currency) {
}
