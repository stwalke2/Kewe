package com.kewe.core.agent;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BingWebSearchClient implements SearchProviderClient {
    private final AgentSearchProperties properties;
    private final WebClient client;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public BingWebSearchClient(AgentSearchProperties properties) {
        this.properties = properties;
        this.client = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public List<WebSearchResult> searchWeb(String query, int limit) {
        if (!properties.hasSearchKey()) {
            return List.of();
        }

        String cacheKey = String.join("|", query, String.valueOf(limit), properties.getBingMkt(), properties.getBingSafeSearch());
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.results();
        }

        String uri = UriComponentsBuilder.fromUriString(properties.getBingEndpoint())
                .queryParam("q", query)
                .queryParam("count", limit)
                .queryParam("mkt", properties.getBingMkt())
                .queryParam("safeSearch", properties.getBingSafeSearch())
                .toUriString();

        BingResponse bingResponse = client.get()
                .uri(uri)
                .header("Ocp-Apim-Subscription-Key", properties.getBingKey())
                .retrieve()
                .bodyToMono(BingResponse.class)
                .timeout(Duration.ofMillis(properties.getBingTimeoutMs()))
                .onErrorReturn(new BingResponse())
                .block();

        List<WebSearchResult> results = bingResponse == null || bingResponse.webPages == null || bingResponse.webPages.value == null
                ? List.of()
                : bingResponse.webPages.value.stream()
                .limit(limit)
                .map(item -> new WebSearchResult(item.name, item.url, item.snippet, item.displayUrl))
                .filter(item -> item.title() != null && !item.title().isBlank() && item.url() != null && !item.url().isBlank())
                .toList();

        cache.put(cacheKey, new CacheEntry(results, Instant.now().plusSeconds(properties.getSearchCacheTtlSeconds())));
        return results;
    }

    private record CacheEntry(List<WebSearchResult> results, Instant expiresAt) {}

    private static class BingResponse {
        public WebPages webPages;
    }

    private static class WebPages {
        public List<WebPageItem> value;
    }

    private static class WebPageItem {
        public String name;
        public String url;
        public String snippet;
        public String displayUrl;
    }
}
