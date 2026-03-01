package com.kewe.core.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SerpApiWebSearchProvider implements WebSearchProvider {
    private static final String NOT_CONFIGURED_WARNING = "Search provider not configured; showing search links only.";

    private final AgentSearchProperties properties;
    private final WebClient client;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public SerpApiWebSearchProvider(AgentSearchProperties properties) {
        this.properties = properties;
        this.client = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public WebSearchResponse search(WebSearchRequest request) {
        if (!"serpapi".equalsIgnoreCase(properties.getWebsearchProvider()) || !properties.hasSearchKey()) {
            return new WebSearchResponse(List.of(), List.of(NOT_CONFIGURED_WARNING));
        }

        int count = request.count() <= 0 ? properties.getWebsearchCount() : request.count();
        String cacheKey = String.join("|", properties.getSerpapiEngine(), request.query(), String.valueOf(count));
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return new WebSearchResponse(cached.results(), List.of());
        }

        String uri = UriComponentsBuilder.fromUriString("https://serpapi.com/search.json")
                .queryParam("api_key", properties.getSerpapiKey())
                .queryParam("engine", properties.getSerpapiEngine())
                .queryParam("q", request.query())
                .queryParam("num", count)
                .toUriString();

        SerpApiResponse response = client.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(SerpApiResponse.class)
                .timeout(Duration.ofMillis(properties.getWebsearchTimeoutMs()))
                .onErrorReturn(new SerpApiResponse())
                .block();

        List<WebSearchResult> results = new ArrayList<>();
        if (response != null && response.organicResults != null) {
            for (OrganicResult item : response.organicResults) {
                if (item == null || isBlank(item.link) || isBlank(item.title)) {
                    continue;
                }
                String image = !isBlank(item.thumbnail) ? item.thumbnail
                        : item.inlineImages != null && !item.inlineImages.isEmpty() ? item.inlineImages.getFirst().thumbnail
                        : null;
                BigDecimal price = parsePrice(item);
                String currency = price == null ? null : "USD";
                String domain = deriveDomain(item.link);
                results.add(new WebSearchResult(item.title, item.link, item.snippet, "serpapi", domain, image, price, currency));
            }
        }

        cache.put(cacheKey, new CacheEntry(results, Instant.now().plusSeconds(properties.getWebsearchCacheTtlSeconds())));
        return new WebSearchResponse(results, List.of());
    }

    static String deriveDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) return "Other";
            host = host.toLowerCase(Locale.ROOT).replace("www.", "");
            if (host.contains("amazon.com")) return "Amazon";
            if (host.contains("fishersci.com")) return "Fisher Scientific";
            if (host.contains("homedepot.com")) return "Home Depot";
            return host;
        } catch (Exception ignored) {
            return "Other";
        }
    }

    private BigDecimal parsePrice(OrganicResult item) {
        if (item.richSnippet == null || item.richSnippet.top == null || item.richSnippet.top.detectedExtensions == null) {
            return null;
        }
        String value = item.richSnippet.top.detectedExtensions.price;
        if (isBlank(value)) return null;
        try {
            return new BigDecimal(value.replaceAll("[^\\d.]", ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record CacheEntry(List<WebSearchResult> results, Instant expiresAt) {}

    private static class SerpApiResponse {
        @JsonProperty("organic_results")
        public List<OrganicResult> organicResults;
    }

    private static class OrganicResult {
        public String title;
        public String link;
        public String snippet;
        public String thumbnail;
        @JsonProperty("inline_images")
        public List<InlineImage> inlineImages;
        @JsonProperty("rich_snippet")
        public RichSnippet richSnippet;
    }

    private static class InlineImage {
        public String thumbnail;
    }

    private static class RichSnippet {
        public RichSnippetTop top;
    }

    private static class RichSnippetTop {
        @JsonProperty("detected_extensions")
        public DetectedExtensions detectedExtensions;
    }

    private static class DetectedExtensions {
        public String price;
    }
}
