package com.kewe.core.agent;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FisherPlaywrightSearchClient extends PlaywrightSupplierSearchClient {
    public FisherPlaywrightSearchClient(PlaywrightBrowserManager browserManager, AgentSearchProperties properties) {
        super(browserManager, properties);
    }

    @Override
    public String supplierKey() { return "fisher"; }

    @Override
    public String supplierName() { return "Fisher Scientific"; }

    @Override
    public String searchLink(String query) {
        return "https://www.fishersci.com/us/en/catalog/search/products?keyword=" + encoded(query);
    }

    @Override
    protected void waitForResultSelector(Page page) {
        page.waitForSelector("a[href*='/shop/products/']", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    @Override
    protected boolean isBlocked(String pageContentLower) {
        return pageContentLower.contains("access denied") || pageContentLower.contains("forbidden");
    }

    @Override
    protected String blockedWarning() {
        return "Fisher Scientific blocked automated fetch.";
    }

    @Override
    protected List<SupplierSearchResult> extract(Page page, int limit) {
        List<SupplierSearchResult> results = new ArrayList<>();
        for (ElementHandle link : page.querySelectorAll("a[href*='/shop/products/']").stream().limit(limit).toList()) {
            String title = link.innerText().trim();
            String href = absolutize(link.getAttribute("href"), "www.fishersci.com");
            if (title.isBlank() || href.isBlank()) continue;
            String text = link.evaluate("el => el.closest('article,div,li')?.innerText || el.innerText").toString();
            results.add(new SupplierSearchResult(supplierName(), title, href, extractPriceFromText(text), AgentSearchService.extractSku(text), null));
        }
        return results;
    }
}
