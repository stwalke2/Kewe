package com.kewe.core.agent;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AmazonPlaywrightSearchClient extends PlaywrightSupplierSearchClient {
    public AmazonPlaywrightSearchClient(PlaywrightBrowserManager browserManager, AgentSearchProperties properties) {
        super(browserManager, properties);
    }

    @Override
    public String supplierKey() { return "amazon"; }

    @Override
    public String supplierName() { return "Amazon"; }

    @Override
    public String searchLink(String query) {
        return "https://www.amazon.com/s?k=" + encoded(query);
    }

    @Override
    protected void waitForResultSelector(Page page) {
        page.waitForSelector("div.s-result-item h2 a", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    @Override
    protected boolean isBlocked(String pageContentLower) {
        return pageContentLower.contains("robot check") || pageContentLower.contains("captcha") || pageContentLower.contains("enter the characters");
    }

    @Override
    protected String blockedWarning() {
        return "Amazon blocked automated fetch (captcha/robot check).";
    }

    @Override
    protected List<SupplierSearchResult> extract(Page page, int limit) {
        List<SupplierSearchResult> results = new ArrayList<>();
        for (ElementHandle card : page.querySelectorAll("div.s-result-item[data-component-type='s-search-result']").stream().limit(limit).toList()) {
            ElementHandle link = card.querySelector("h2 a");
            if (link == null) continue;
            String title = link.innerText().trim();
            String href = absolutize(link.getAttribute("href"), "www.amazon.com");
            if (title.isBlank() || href.isBlank()) continue;
            String text = card.innerText();
            results.add(new SupplierSearchResult(supplierName(), title, href, extractPriceFromText(text), AgentSearchService.extractSku(text), null));
        }
        return results;
    }
}
