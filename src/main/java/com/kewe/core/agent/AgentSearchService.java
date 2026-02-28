package com.kewe.core.agent;

import com.kewe.core.funding.FundingService;
import com.kewe.core.requisition.RequisitionLine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentSearchService {
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$(\\d+[\\d,]*(?:\\.\\d{2})?)");
    private static final Pattern SKU_PATTERN = Pattern.compile("(?:sku|model|part)[:#\\s]*([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);
    private final PromptParser parser;
    private final FundingService fundingService;
    private final SearchProviderClient searchProviderClient;
    private final AgentSearchProperties properties;

    public AgentSearchService(PromptParser parser,
                              FundingService fundingService,
                              SearchProviderClient searchProviderClient,
                              AgentSearchProperties properties) {
        this.parser = parser;
        this.fundingService = fundingService;
        this.searchProviderClient = searchProviderClient;
        this.properties = properties;
    }

    public CapabilitiesResponse capabilities() {
        return new CapabilitiesResponse(properties.getSearchProvider().toLowerCase(Locale.ROOT), properties.hasSearchKey());
    }

    public AgentDraftResponse createDraft(String prompt, boolean stubMode) {
        PromptParser.ParsedPrompt parsed = parser.parse(prompt);
        if (stubMode) {
            return stubDraft(parsed, prompt);
        }

        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> links = buildSearchLinks(parsed.item());

        Map<String, List<SupplierSearchResult>> results = new LinkedHashMap<>();
        List<String> warnings;
        if (!properties.hasSearchKey()) {
            warnings = List.of("Search provider not configured; showing search links only.");
            results.put("amazon", List.of());
            results.put("fisher", List.of());
            results.put("homedepot", List.of());
        } else {
            warnings = List.of();
            results.put("amazon", searchSupplier("Amazon", "site:amazon.com " + parsed.item()));
            results.put("fisher", searchSupplier("Fisher Scientific", "site:fishersci.com " + parsed.item()));
            results.put("homedepot", searchSupplier("Home Depot", "site:homedepot.com " + parsed.item()));
        }

        RequisitionLine prefilled = choosePrefillLine(parsed, results);
        DraftPayload draft = new DraftPayload(titleFrom(parsed), prompt, "USD", prefilled == null ? List.of() : List.of(prefilled));
        return new AgentDraftResponse(parsed, suggested, links, results, warnings, draft);
    }

    private List<SupplierSearchResult> searchSupplier(String supplierName, String query) {
        int limit = properties.getBingResultCount();
        return searchProviderClient.searchWeb(query, limit).stream()
                .map(r -> new SupplierSearchResult(supplierName, r.title(), r.url(), null, null, r.snippet()))
                .toList();
    }

    private AgentDraftResponse stubDraft(PromptParser.ParsedPrompt parsed, String prompt) {
        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> links = buildSearchLinks(parsed.item());

        Map<String, List<SupplierSearchResult>> results = Map.of(
                "amazon", List.of(stubResult("Amazon", parsed.item(), "https://www.amazon.com/s?k=beaker")),
                "fisher", List.of(stubResult("Fisher Scientific", parsed.item(), "https://www.fishersci.com/us/en/catalog/search/products?keyword=beaker")),
                "homedepot", List.of(stubResult("Home Depot", parsed.item(), "https://www.homedepot.com/s/beaker"))
        );

        RequisitionLine line = choosePrefillLine(parsed, results);
        DraftPayload draft = new DraftPayload(titleFrom(parsed), prompt, "USD", line == null ? List.of() : List.of(line));
        return new AgentDraftResponse(parsed, suggested, links, results, List.of("Stub mode enabled: returning canned supplier results."), draft);
    }

    private SupplierSearchResult stubResult(String supplier, String item, String url) {
        return new SupplierSearchResult(supplier, "Stub result: " + item, url, BigDecimal.valueOf(19.99), "STUB-001", "stub snippet");
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
        String hint = Optional.ofNullable(parsed.orgHint()).orElse(parsed.item()).toLowerCase(Locale.ROOT);
        return eligible.stream().min((left, right) -> score(left, hint) - score(right, hint)).orElse(null);
    }

    private int score(FundingService.ChargingLocationDto dto, String hint) {
        String name = dto.name().toLowerCase(Locale.ROOT);
        if (name.equals(hint)) return 0;
        if (name.startsWith(hint)) return 1;
        if (name.contains(hint)) return 2;
        if (dto.code().toLowerCase(Locale.ROOT).contains(hint)) return 3;
        return 10;
    }

    private RequisitionLine choosePrefillLine(PromptParser.ParsedPrompt parsed, Map<String, List<SupplierSearchResult>> results) {
        SupplierSearchResult chosen = results.getOrDefault("fisher", List.of()).stream().findFirst()
                .or(() -> results.getOrDefault("homedepot", List.of()).stream().findFirst())
                .or(() -> results.getOrDefault("amazon", List.of()).stream().findFirst())
                .orElse(null);
        if (chosen == null) return null;
        RequisitionLine line = new RequisitionLine();
        line.setLineNumber(1);
        line.setDescription(chosen.title());
        line.setQuantity(parsed.quantity());
        line.setUom(parsed.uom());
        line.setUnitPrice(chosen.price() == null ? null : chosen.price().doubleValue());
        line.setAmount((chosen.price() == null ? BigDecimal.ZERO : chosen.price()).doubleValue() * parsed.quantity());
        line.setSupplierName(chosen.supplierName());
        line.setSupplierUrl(chosen.url());
        line.setSupplierSku(chosen.sku());
        return line;
    }

    private String titleFrom(PromptParser.ParsedPrompt parsed) { return "Requisition - " + parsed.item(); }

    public static BigDecimal extractPrice(String text) {
        Matcher matcher = PRICE_PATTERN.matcher(text == null ? "" : text);
        if (!matcher.find()) return null;
        return BigDecimal.valueOf(Double.parseDouble(matcher.group(1).replace(",", "")));
    }

    public static String extractSku(String text) {
        Matcher matcher = SKU_PATTERN.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public record DraftPayload(String title, String memo, String currency, List<RequisitionLine> lines) {}
    public record CapabilitiesResponse(String provider, boolean hasKey) {}
    public record AgentDraftResponse(PromptParser.ParsedPrompt parsed,
                                     FundingService.ChargingLocationDto suggestedChargingDimension,
                                     Map<String, String> searchLinks,
                                     Map<String, List<SupplierSearchResult>> results,
                                     List<String> warnings,
                                     DraftPayload draft) {}
}
