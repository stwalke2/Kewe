package com.kewe.core.agent;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentSearchServiceTest {

    @Test
    void prioritizesPreferredVendorsAndProductUrls() {
        List<WebSearchResult> input = List.of(
                new WebSearchResult("Glass beaker product", "https://www.amazon.com/dp/B0001", "500 ml beaker", "serpapi", "Amazon", null, BigDecimal.ONE, "USD"),
                new WebSearchResult("Blog result", "https://example.com/blog/beaker", "500 ml", "serpapi", "example.com", null, null, null),
                new WebSearchResult("Fisher category", "https://www.fishersci.com/us/en/catalog/search/products", "beaker", "serpapi", "Fisher Scientific", null, null, null)
        );

        List<AgentSearchService.ProductSuggestion> ranked = AgentSearchService.prioritize(input, List.of("beaker", "500ml"));

        assertThat(ranked.getFirst().supplier()).isEqualTo("Amazon");
        assertThat(ranked.getLast().supplier()).isEqualTo("Other");
    }
}
