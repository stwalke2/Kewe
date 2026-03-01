package com.kewe.core.agent;

import com.kewe.core.funding.FundingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AgentSearchService {
    private final PromptParser parser;
    private final FundingService fundingService;
    private final WebSearchProvider webSearchProvider;
    private final AgentSearchProperties properties;

    public AgentSearchService(PromptParser parser,
                              FundingService fundingService,
                              WebSearchProvider webSearchProvider,
                              AgentSearchProperties properties) {
        this.parser = parser;
        this.fundingService = fundingService;
        this.webSearchProvider = webSearchProvider;
        this.properties = properties;
    }

    public CapabilitiesResponse capabilities() {
        return new CapabilitiesResponse(
                properties.getWebsearchProvider().toLowerCase(Locale.ROOT),
                properties.hasSearchKey(),
                properties.getSerpapiEngine(),
                properties.getWebsearchCount());
    }

    public AgentDraftResponse createDraft(String prompt) {
        PromptParser.ParsedPrompt parsed = parser.parse(prompt);
        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> searchLinks = buildSearchLinks(parsed.item());

        WebSearchResponse response = webSearchProvider.search(new WebSearchRequest(parsed.normalizedQuery(), properties.getWebsearchCount()));
        List<ProductSuggestion> top = prioritize(response.results(), parsed.keywords()).stream().limit(5).toList();

        return new AgentDraftResponse(parsed, searchLinks, top, response.warnings(), suggested);
    }

    static List<ProductSuggestion> prioritize(List<WebSearchResult> results, List<String> keywords) {
        return results.stream()
                .map(result -> new ScoredSuggestion(toSuggestion(result), scoreResult(result, keywords)))
                .sorted(Comparator.comparingInt(ScoredSuggestion::score).reversed())
                .map(ScoredSuggestion::suggestion)
                .toList();
    }

    static int scoreResult(WebSearchResult result, List<String> keywords) {
        String url = Optional.ofNullable(result.url()).orElse("").toLowerCase(Locale.ROOT);
        String title = Optional.ofNullable(result.title()).orElse("").toLowerCase(Locale.ROOT);
        String snippet = Optional.ofNullable(result.snippet()).orElse("").toLowerCase(Locale.ROOT);
        int score = 0;
        if (url.contains("amazon.com") || url.contains("fishersci.com") || url.contains("homedepot.com")) {
            score += 100;
        }
        if (keywords.stream().anyMatch(token -> title.contains(token.toLowerCase(Locale.ROOT)))) {
            score += 20;
        }
        if (snippet.contains("500 ml") || snippet.contains("500ml")) {
            score += 10;
        }
        if (url.contains("amazon.com") && !url.contains("/dp/")) {
            score -= 50;
        }
        if (url.contains("homedepot.com") && !url.contains("/p/")) {
            score -= 50;
        }
        if (url.contains("fishersci.com") && !(url.contains("/product/") || url.contains("/catalog/"))) {
            score -= 50;
        }
        return score;
    }

    private static ProductSuggestion toSuggestion(WebSearchResult result) {
        return new ProductSuggestion(
                result.title(),
                result.url(),
                result.snippet(),
                result.imageUrl(),
                result.price(),
                result.currency(),
                supplierFromUrl(result.url())
        );
    }

    private static String supplierFromUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) return "Other";
            host = host.toLowerCase(Locale.ROOT);
            if (host.contains("amazon.com")) return "Amazon";
            if (host.contains("fishersci.com")) return "Fisher Scientific";
            if (host.contains("homedepot.com")) return "Home Depot";
            return "Other";
        } catch (Exception e) {
            return "Other";
        }
    }

    private Map<String, String> buildSearchLinks(String keywords) {
        String encoded = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
        return Map.of(
                "amazon", "https://www.amazon.com/s?k=" + encoded,
                "fisher", "https://www.fishersci.com/us/en/catalog/search/products?keyword=" + encoded,
                "homedepot", "https://www.homedepot.com/s/" + encoded.replace("+", "%20")
        );
    }

    private FundingService.ChargingLocationDto suggestCharging(PromptParser.ParsedPrompt parsed, List<FundingService.ChargingLocationDto> eligible) {
        String orgHint = parsed.orgHint() == null ? "" : parsed.orgHint().trim().toLowerCase(Locale.ROOT);
        if (orgHint.isBlank()) {
            return null;
        }

        return eligible.stream().min((left, right) -> Integer.compare(matchScore(left, orgHint), matchScore(right, orgHint)))
                .filter(item -> matchScore(item, orgHint) < 10)
                .orElse(null);
    }

    private int matchScore(FundingService.ChargingLocationDto dto, String hint) {
        if (dto.code() != null && dto.code().equalsIgnoreCase(hint)) return 0;
        if (dto.name() != null && dto.name().equalsIgnoreCase(hint)) return 1;
        if (dto.name() != null && dto.name().toLowerCase(Locale.ROOT).contains(hint)) return 2;
        return 10;
    }

    private record ScoredSuggestion(ProductSuggestion suggestion, int score) {}

    public static BigDecimal extractPrice(String text) {
        if (text == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\$(\\d+[\\d,]*(?:\\.\\d{2})?)").matcher(text);
        if (!matcher.find()) return null;
        return BigDecimal.valueOf(Double.parseDouble(matcher.group(1).replace(",", "")));
    }

    public static String extractSku(String text) {
        if (text == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?:sku|model|part)[:#\\s]*([A-Za-z0-9-]+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public record ProductSuggestion(String title,
                                    String url,
                                    String snippet,
                                    String imageUrl,
                                    BigDecimal price,
                                    String currency,
                                    String supplier) {
    }

    public record CapabilitiesResponse(String provider, boolean hasKey, String engine, int count) {}

    public record AgentDraftResponse(PromptParser.ParsedPrompt parsed,
                                     Map<String, String> searchLinks,
                                     List<ProductSuggestion> results,
                                     List<String> warnings,
                                     FundingService.ChargingLocationDto suggestedChargingLocation) {}
}
