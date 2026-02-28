package com.kewe.core.agent;

import com.kewe.core.funding.FundingService;
import com.kewe.core.requisition.RequisitionLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentSearchService {
    private static final Logger log = LoggerFactory.getLogger(AgentSearchService.class);
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$(\\d+[\\d,]*(?:\\.\\d{2})?)");
    private static final Pattern SKU_PATTERN = Pattern.compile("(?:sku|model|part)[:#\\s]*([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    private final PromptParser parser;
    private final FundingService fundingService;
    private final ConfiguredSearchProviderClient searchProviderClient;
    private final AgentSearchProperties properties;
    private final List<SupplierSearchClient> supplierClients;

    public AgentSearchService(PromptParser parser,
                              FundingService fundingService,
                              ConfiguredSearchProviderClient searchProviderClient,
                              AgentSearchProperties properties,
                              List<SupplierSearchClient> supplierClients) {
        this.parser = parser;
        this.fundingService = fundingService;
        this.searchProviderClient = searchProviderClient;
        this.properties = properties;
        this.supplierClients = supplierClients;
    }

    public CapabilitiesResponse capabilities() {
        return new CapabilitiesResponse(
                properties.isPlaywrightEnabled(),
                properties.getSearchProvider().toLowerCase(Locale.ROOT),
                properties.hasSearchKey()
        );
    }

    public AgentDraftResponse createDraft(String prompt, boolean stubMode) {
        long started = System.currentTimeMillis();
        PromptParser.ParsedPrompt parsed = parser.parse(prompt);
        log.info("agent_request start stubMode={} prompt={}", stubMode, prompt);
        if (stubMode) {
            return stubDraft(parsed, prompt);
        }

        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> links = buildSearchLinks(parsed.item());

        Map<String, List<SupplierSearchResult>> results = new LinkedHashMap<>();
        Map<String, SupplierDebug> debug = new LinkedHashMap<>();
        ArrayList<String> warnings = new ArrayList<>();

        try {
            CompletableFuture.allOf(supplierClients.stream()
                    .map(client -> CompletableFuture.runAsync(() -> searchSupplier(client, parsed.item(), links, results, warnings, debug)))
                    .toArray(CompletableFuture[]::new))
                    .orTimeout(15, TimeUnit.SECONDS)
                    .join();
        } catch (Exception ex) {
            warnings.add("Supplier search timed out; showing links only where needed.");
            log.warn("agent_request supplier aggregation timed out", ex);
        }

        for (SupplierSearchClient client : supplierClients) {
            results.putIfAbsent(client.supplierKey(), List.of());
            debug.putIfAbsent(client.supplierKey(), new SupplierDebug(0, true, "linkOnly", "timed out"));
        }

        RequisitionLine prefilled = choosePrefillLine(parsed, results);
        DraftPayload draft = new DraftPayload(titleFrom(parsed), prompt, "USD", prefilled == null ? List.of() : List.of(prefilled));

        long elapsed = System.currentTimeMillis() - started;
        log.info("agent_request end elapsedMs={} warnings={} lines={}", elapsed, warnings.size(), draft.lines().size());
        return new AgentDraftResponse(parsed, suggested, links, results, warnings, debug, draft);
    }

    private synchronized void searchSupplier(SupplierSearchClient client,
                                             String keywords,
                                             Map<String, String> links,
                                             Map<String, List<SupplierSearchResult>> resultsSink,
                                             List<String> globalWarnings,
                                             Map<String, SupplierDebug> debugSink) {
        long start = System.currentTimeMillis();
        String reason = "";
        String source = "playwright";
        boolean blocked = false;
        List<SupplierSearchResult> results = List.of();
        ArrayList<String> warnings = new ArrayList<>();

        try {
            SupplierSearchOutcome outcome = client.search(SupplierSearchQuery.of(keywords));
            warnings.addAll(outcome.warnings());
            blocked = outcome.blockedOrFailed();
            results = outcome.results();

            if (outcome.blockedOrFailed() || outcome.results().isEmpty()) {
                List<SupplierSearchResult> fallback = fallbackSiteSearch(client, keywords);
                if (!fallback.isEmpty()) {
                    source = "searchProvider";
                    results = fallback;
                    warnings.add("Using search provider results (site search); prices may be unavailable.");
                    blocked = false;
                } else {
                    source = "linkOnly";
                    warnings.add("Automated fetch blocked; open search link.");
                    blocked = true;
                }
            }
        } catch (Exception ex) {
            source = "linkOnly";
            blocked = true;
            reason = ex.getMessage();
            warnings.add("Failed to fetch " + client.supplierName() + " results.");
            log.warn("agent_supplier_error supplier={} reason={}", client.supplierKey(), reason, ex);
        }

        long elapsed = System.currentTimeMillis() - start;
        synchronized (resultsSink) {
            resultsSink.put(client.supplierKey(), results);
        }
        synchronized (globalWarnings) {
            globalWarnings.addAll(warnings.stream().map(w -> client.supplierName() + ": " + w).toList());
        }
        synchronized (debugSink) {
            debugSink.put(client.supplierKey(), new SupplierDebug(elapsed, blocked, source, reason));
        }
        log.info("agent_supplier supplier={} source={} elapsedMs={} blocked={} warnings={}", client.supplierKey(), source, elapsed, blocked, warnings.size());
    }

    private List<SupplierSearchResult> fallbackSiteSearch(SupplierSearchClient client, String keywords) {
        if (!properties.hasSearchKey()) {
            return List.of();
        }
        String query = "site:" + switch (client.supplierKey()) {
            case "amazon" -> "amazon.com " + keywords + " beaker";
            case "fisher" -> "fishersci.com " + keywords;
            case "homedepot" -> "homedepot.com " + keywords;
            default -> keywords;
        };
        return searchProviderClient.searchWeb(query, 5).stream()
                .map(r -> new SupplierSearchResult(client.supplierName(), r.title(), r.url(), null, null, r.snippet()))
                .toList();
    }

    private AgentDraftResponse stubDraft(PromptParser.ParsedPrompt parsed, String prompt) {
        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> links = buildSearchLinks(parsed.item());

        Map<String, List<SupplierSearchResult>> results = Map.of(
                "amazon", List.of(stubResult("Amazon", parsed.item(), "https://www.amazon.com/s?k=beaker"), stubResult("Amazon", "Glass Beaker Set", "https://www.amazon.com/s?k=glass+beaker")),
                "fisher", List.of(stubResult("Fisher Scientific", parsed.item(), "https://www.fishersci.com/us/en/catalog/search/products?keyword=beaker"), stubResult("Fisher Scientific", "Class A Beaker", "https://www.fishersci.com/us/en/catalog/search/products?keyword=class+a+beaker")),
                "homedepot", List.of(stubResult("Home Depot", parsed.item(), "https://www.homedepot.com/s/beaker"), stubResult("Home Depot", "Lab Glassware", "https://www.homedepot.com/s/glassware"))
        );
        Map<String, SupplierDebug> debug = Map.of(
                "amazon", new SupplierDebug(10, false, "playwright", ""),
                "fisher", new SupplierDebug(12, false, "playwright", ""),
                "homedepot", new SupplierDebug(15, false, "playwright", "")
        );

        RequisitionLine line = choosePrefillLine(parsed, results);
        DraftPayload draft = new DraftPayload(titleFrom(parsed), prompt, "USD", line == null ? List.of() : List.of(line));
        return new AgentDraftResponse(parsed, suggested, links, results, List.of("Stub mode enabled: returning canned supplier results."), debug, draft);
    }

    private SupplierSearchResult stubResult(String supplier, String item, String url) {
        return new SupplierSearchResult(supplier, "Stub result: " + item, url, BigDecimal.valueOf(19.99), "STUB-001", "stub snippet");
    }

    private Map<String, String> buildSearchLinks(String keywords) {
        Map<String, String> links = new LinkedHashMap<>();
        supplierClients.forEach(client -> links.put(client.supplierKey(), client.searchLink(keywords)));
        return links;
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
    public record SupplierDebug(long elapsedMs, boolean blockedOrFailed, String source, String reason) {}
    public record CapabilitiesResponse(boolean playwrightEnabled, String searchProvider, boolean hasSearchKey) {}
    public record AgentDraftResponse(PromptParser.ParsedPrompt parsed,
                                     FundingService.ChargingLocationDto suggestedChargingDimension,
                                     Map<String, String> searchLinks,
                                     Map<String, List<SupplierSearchResult>> results,
                                     List<String> warnings,
                                     Map<String, SupplierDebug> debug,
                                     DraftPayload draft) {}
}
