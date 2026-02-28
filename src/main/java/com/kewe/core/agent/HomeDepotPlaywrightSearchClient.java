package com.kewe.core.agent;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HomeDepotPlaywrightSearchClient extends PlaywrightSupplierSearchClient {
    public HomeDepotPlaywrightSearchClient(PlaywrightBrowserManager browserManager, AgentSearchProperties properties) {
        super(browserManager, properties);
    }

    @Override
    public String supplierKey() { return "homedepot"; }

    @Override
    public String supplierName() { return "Home Depot"; }

    @Override
    public String searchLink(String query) {
        return "https://www.homedepot.com/s/" + query.trim().replace(" ", "%20");
    }

    @Override
    protected void waitForResultSelector(Page page) {
        page.waitForSelector("a[href*='/p/']", new Page.WaitForSelectorOptions().setTimeout(5000));
    }

    @Override
    protected boolean isBlocked(String pageContentLower) {
        return pageContentLower.contains("access denied") || pageContentLower.contains("403") || pageContentLower.contains("denied");
    }

    @Override
    protected String blockedWarning() {
        return "Home Depot blocked automated fetch (access denied).";
    }

    @Override
    protected List<SupplierSearchResult> extract(Page page, int limit) {
        List<SupplierSearchResult> results = new ArrayList<>();
        for (ElementHandle link : page.querySelectorAll("a[href*='/p/']").stream().limit(limit).toList()) {
            String title = link.innerText().trim();
            String href = absolutize(link.getAttribute("href"), "www.homedepot.com");
            if (title.isBlank() || href.isBlank()) continue;
            String text = link.evaluate("el => el.closest('article,div,li')?.innerText || el.innerText").toString();
            results.add(new SupplierSearchResult(supplierName(), title, href, extractPriceFromText(text), AgentSearchService.extractSku(text), null));
        }
        return results;
    }
}
