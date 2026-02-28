package com.kewe.core.agent;

import com.kewe.core.funding.FundingService;
import com.kewe.core.requisition.RequisitionLine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentSearchService {
    private static final Logger log = LoggerFactory.getLogger(AgentSearchService.class);
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$(\\d+[\\d,]*(?:\\.\\d{2})?)");
    private static final Pattern SKU_PATTERN = Pattern.compile("(?:sku|model|part)[:#\\s]*([A-Za-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    private final PromptParser parser;
    private final FundingService fundingService;
    private final HtmlFetcher htmlFetcher;
    private final AmazonSearchClient amazon;
    private final FisherSearchClient fisher;
    private final HomeDepotSearchClient homeDepot;

    public AgentSearchService(PromptParser parser,
                              FundingService fundingService,
                              HtmlFetcher htmlFetcher,
                              AmazonSearchClient amazon,
                              FisherSearchClient fisher,
                              HomeDepotSearchClient homeDepot) {
        this.parser = parser;
        this.fundingService = fundingService;
        this.htmlFetcher = htmlFetcher;
        this.amazon = amazon;
        this.fisher = fisher;
        this.homeDepot = homeDepot;
    }

    public AgentDraftResponse createDraft(String prompt, boolean stubMode) {
        PromptParser.ParsedPrompt parsed = parser.parse(prompt);
        log.info("Agent draft request started (stubMode={}, prompt={})", stubMode, prompt);

        if (stubMode) {
            return stubDraft(parsed, prompt);
        }
        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);
        Map<String, String> links = Map.of(
                "amazon", amazon.searchLink(parsed.item()),
                "fisher", fisher.searchLink(parsed.item()),
                "homedepot", homeDepot.searchLink(parsed.item())
        );

        Map<String, List<SupplierResult>> results = new HashMap<>();
        java.util.ArrayList<String> warnings = new java.util.ArrayList<>();
        searchSupplier("amazon", amazon, parsed.item(), results, warnings);
        searchSupplier("fisher", fisher, parsed.item(), results, warnings);
        searchSupplier("homedepot", homeDepot, parsed.item(), results, warnings);

        RequisitionLine prefilled = choosePrefillLine(parsed, results);
        DraftPayload draft = new DraftPayload(
                titleFrom(parsed),
                prompt,
                "USD",
                prefilled == null ? List.of() : List.of(prefilled)
        );

        AgentDraftResponse response = new AgentDraftResponse(parsed, suggested, links, results, draft, warnings);
        log.info("Agent draft request finished (lines={}, warnings={})", response.draft().lines().size(), warnings.size());
        return response;
    }

    private AgentDraftResponse stubDraft(PromptParser.ParsedPrompt parsed, String prompt) {
        List<FundingService.ChargingLocationDto> chargingLocations = fundingService.findChargingLocations(null);
        FundingService.ChargingLocationDto suggested = suggestCharging(parsed, chargingLocations);

        Map<String, String> links = new HashMap<>();
        links.put("amazon", amazon.searchLink(parsed.item()));
        links.put("fisher", fisher.searchLink(parsed.item()));
        links.put("homedepot", homeDepot.searchLink(parsed.item()));

        SupplierResult sample = new SupplierResult(
                "Stub Supplier",
                parsed.item().isBlank() ? "Lab supply starter pack" : "Stub result: " + parsed.item(),
                19.99,
                "STUB-001",
                "https://example.com/stub-item",
                "STUB-001"
        );

        Map<String, List<SupplierResult>> results = new HashMap<>();
        results.put("amazon", List.of(sample));
        results.put("fisher", List.of(sample));
        results.put("homedepot", List.of(sample));

        RequisitionLine line = new RequisitionLine();
        line.setLineNumber(1);
        line.setDescription(sample.title());
        line.setQuantity(parsed.quantity());
        line.setUom(parsed.uom());
        line.setUnitPrice(sample.price());
        line.setAmount(sample.price() * parsed.quantity());
        line.setSupplierName(sample.supplier());
        line.setSupplierUrl(sample.url());
        line.setSupplierSku(sample.sku());

        DraftPayload draft = new DraftPayload(titleFrom(parsed), prompt, "USD", List.of(line));
        java.util.ArrayList<String> warnings = new java.util.ArrayList<>();
        warnings.add("Stub mode enabled: returning canned supplier results.");
        AgentDraftResponse response = new AgentDraftResponse(parsed, suggested, links, results, draft, warnings);
        log.info("Agent draft stub response generated (lines={})", response.draft().lines().size());
        return response;
    }

    private void searchSupplier(String key, SupplierSearchClient client, String query, Map<String, List<SupplierResult>> sink, List<String> warnings) {
        try {
            log.info("Searching supplier {} with query [{}]", client.supplierName(), query);
            List<SupplierResult> direct = client.search(query);
            if (!direct.isEmpty()) {
                sink.put(key, direct);
                log.info("Supplier {} returned {} direct results", client.supplierName(), direct.size());
                return;
            }

            List<SupplierResult> fallback = searchViaWeb(key, client.supplierName(), query);
            sink.put(key, fallback);
            if (fallback.isEmpty()) {
                warnings.add("Could not fetch " + client.supplierName() + " results; open search link.");
                log.warn("Supplier {} fallback search returned no results", client.supplierName());
            } else {
                warnings.add("Using fallback web search for " + client.supplierName() + " results.");
                log.info("Supplier {} fallback returned {} results", client.supplierName(), fallback.size());
            }
        } catch (Exception ex) {
            sink.put(key, List.of());
            warnings.add("Failed to fetch " + client.supplierName() + " results: " + ex.getMessage());
            log.warn("Supplier {} failed", client.supplierName(), ex);
        }
    }

    private List<SupplierResult> searchViaWeb(String supplierKey, String supplierName, String query) {
        String domain = switch (supplierKey) {
            case "amazon" -> "amazon.com";
            case "fisher" -> "fishersci.com";
            case "homedepot" -> "homedepot.com";
            default -> "";
        };
        if (domain.isBlank()) {
            return List.of();
        }

        String encoded = URLEncoder.encode("site:" + domain + " " + query, StandardCharsets.UTF_8);
        String html = htmlFetcher.fetch("https://duckduckgo.com/html/?q=" + encoded);
        Document doc = Jsoup.parse(html);

        java.util.ArrayList<SupplierResult> results = new java.util.ArrayList<>();
        for (Element item : doc.select(".result").stream().limit(5).toList()) {
            Element link = item.selectFirst("a.result__a");
            if (link == null) {
                continue;
            }
            String title = link.text();
            String href = link.absUrl("href");
            if (href.isBlank()) {
                href = link.attr("href");
            }
            String snippet = item.text();
            results.add(new SupplierResult(
                    supplierName,
                    title,
                    extractPrice(snippet),
                    null,
                    href,
                    extractSku(snippet)
            ));
        }
        return results;
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

    private RequisitionLine choosePrefillLine(PromptParser.ParsedPrompt parsed, Map<String, List<SupplierResult>> results) {
        SupplierResult chosen = results.getOrDefault("fisher", List.of()).stream().findFirst()
                .or(() -> results.getOrDefault("homedepot", List.of()).stream().findFirst())
                .or(() -> results.getOrDefault("amazon", List.of()).stream().findFirst())
                .orElse(null);
        if (chosen == null) {
            return null;
        }
        RequisitionLine line = new RequisitionLine();
        line.setLineNumber(1);
        line.setDescription(chosen.title());
        line.setQuantity(parsed.quantity());
        line.setUom(parsed.uom());
        line.setUnitPrice(chosen.price());
        line.setAmount(chosen.price() == null ? 0 : chosen.price() * parsed.quantity());
        line.setSupplierName(chosen.supplier());
        line.setSupplierUrl(chosen.url());
        line.setSupplierSku(chosen.sku());
        return line;
    }

    private String titleFrom(PromptParser.ParsedPrompt parsed) {
        return "Requisition - " + parsed.item();
    }

    public static Double extractPrice(String text) {
        Matcher matcher = PRICE_PATTERN.matcher(text == null ? "" : text);
        if (!matcher.find()) return null;
        return Double.parseDouble(matcher.group(1).replace(",", ""));
    }

    public static String extractSku(String text) {
        Matcher matcher = SKU_PATTERN.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public record DraftPayload(String title, String memo, String currency, List<RequisitionLine> lines) {}

    public record AgentDraftResponse(PromptParser.ParsedPrompt parsed,
                                     FundingService.ChargingLocationDto suggestedChargingDimension,
                                     Map<String, String> searchLinks,
                                     Map<String, List<SupplierResult>> results,
                                     DraftPayload draft,
                                     List<String> warnings) {}
}
