package com.kewe.core.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfiguredSearchProviderClient implements SearchProviderClient {
    private final AgentSearchProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();

    public ConfiguredSearchProviderClient(AgentSearchProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SearchResult> searchWeb(String query, int limit) {
        if (!properties.hasSearchKey()) {
            return List.of();
        }
        return switch (properties.getSearchProvider().toLowerCase()) {
            case "serpapi" -> searchSerpApi(query, limit);
            case "bing" -> searchBing(query, limit);
            default -> List.of();
        };
    }

    private List<SearchResult> searchSerpApi(String query, int limit) {
        String url = UriComponentsBuilder.fromHttpUrl("https://serpapi.com/search.json")
                .queryParam("engine", "google")
                .queryParam("q", query)
                .queryParam("api_key", properties.getSerpapiKey())
                .queryParam("num", limit)
                .toUriString();
        return parseResults(httpGet(url, null), "organic_results", "title", "link", "snippet", limit);
    }

    private List<SearchResult> searchBing(String query, int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(properties.getBingEndpoint())
                .queryParam("q", query)
                .queryParam("count", limit)
                .toUriString();
        return parseResults(httpGet(url, properties.getBingKey()), "webPages.value", "name", "url", "snippet", limit);
    }

    private String httpGet(String url, String key) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(6)).GET();
            if (key != null && !key.isBlank()) {
                builder.header("Ocp-Apim-Subscription-Key", key);
            }
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Search provider HTTP " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Search provider failed", e);
        }
    }

    private List<SearchResult> parseResults(String json, String path, String titleField, String urlField, String snippetField, int limit) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root;
            for (String token : path.split("\\.")) {
                items = items.path(token);
            }
            if (!items.isArray()) {
                return List.of();
            }
            List<SearchResult> results = new ArrayList<>();
            for (JsonNode item : items) {
                String title = item.path(titleField).asText("");
                String url = item.path(urlField).asText("");
                if (title.isBlank() || url.isBlank()) continue;
                results.add(new SearchResult(title, url, item.path(snippetField).asText(null)));
                if (results.size() >= limit) break;
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse provider response", e);
        }
    }
}
