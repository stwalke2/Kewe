package com.kewe.core.agent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class PlaywrightSupplierSearchClient implements SupplierSearchClient {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightSupplierSearchClient.class);
    private final PlaywrightBrowserManager browserManager;
    private final AgentSearchProperties properties;

    protected PlaywrightSupplierSearchClient(PlaywrightBrowserManager browserManager, AgentSearchProperties properties) {
        this.browserManager = browserManager;
        this.properties = properties;
    }

    @Override
    public SupplierSearchOutcome search(SupplierSearchQuery query) {
        long start = System.currentTimeMillis();
        if (!properties.isPlaywrightEnabled()) {
            return SupplierSearchOutcome.empty(System.currentTimeMillis() - start, true, "Playwright disabled by config.");
        }
        try {
            return CompletableFuture.supplyAsync(() -> scrape(query))
                    .orTimeout(6, TimeUnit.SECONDS)
                    .exceptionally(ex -> SupplierSearchOutcome.empty(System.currentTimeMillis() - start, true, supplierName() + " fetch failed: " + ex.getMessage()))
                    .join();
        } catch (Exception ex) {
            log.warn("Playwright search failed for {}", supplierName(), ex);
            return SupplierSearchOutcome.empty(System.currentTimeMillis() - start, true, supplierName() + " fetch failed.");
        }
    }

    private SupplierSearchOutcome scrape(SupplierSearchQuery query) {
        long start = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        try {
            Browser browser = browserManager.browser();
            try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .setLocale("en-US"))) {
                Page page = context.newPage();
                page.navigate(searchLink(query.keywords()), new Page.NavigateOptions().setTimeout(6000));
                page.waitForLoadState(Page.LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(6000));
                waitForResultSelector(page);
                String body = page.content().toLowerCase();
                if (isBlocked(body)) {
                    warnings.add(blockedWarning());
                    return new SupplierSearchOutcome(List.of(), warnings, true, System.currentTimeMillis() - start);
                }
                List<SupplierSearchResult> results = extract(page, query.limit());
                boolean blockedOrFailed = results.isEmpty();
                if (blockedOrFailed) {
                    warnings.add("No " + supplierName() + " results captured from Playwright.");
                }
                return new SupplierSearchOutcome(results, warnings, blockedOrFailed, System.currentTimeMillis() - start);
            }
        } catch (PlaywrightException ex) {
            warnings.add(supplierName() + " fetch failed: " + ex.getMessage());
            return new SupplierSearchOutcome(List.of(), warnings, true, System.currentTimeMillis() - start);
        }
    }

    protected abstract void waitForResultSelector(Page page);

    protected abstract boolean isBlocked(String pageContentLower);

    protected abstract String blockedWarning();

    protected abstract List<SupplierSearchResult> extract(Page page, int limit);

    protected BigDecimal extractPriceFromText(String text) {
        return AgentSearchService.extractPrice(text);
    }

    protected static String encoded(String query) {
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    protected static String absolutize(String href, String fallbackHost) {
        if (href == null || href.isBlank()) return "";
        if (href.startsWith("http")) return href;
        if (href.startsWith("//")) return "https:" + href;
        try {
            return new URI("https", fallbackHost, href.startsWith("/") ? href : "/" + href, null).toString();
        } catch (URISyntaxException ignored) {
            return href;
        }
    }
}
