package com.kewe.core.agent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class FisherSearchClient implements SupplierSearchClient {
    private final HtmlFetcher fetcher;

    public FisherSearchClient(HtmlFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String supplierName() { return "Fisher Scientific"; }

    @Override
    public String searchLink(String query) {
        return "https://www.fishersci.com/us/en/catalog/search/products?keyword=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    @Override
    public List<SupplierResult> search(String query) {
        String html = fetcher.fetch(searchLink(query));
        Document doc = Jsoup.parse(html);
        List<SupplierResult> results = new ArrayList<>();
        for (Element element : doc.select("a[data-testid*=product], .searchResultItem a").stream().limit(5).toList()) {
            String title = element.text();
            if (title.isBlank()) continue;
            String href = element.absUrl("href");
            if (href.isBlank()) href = element.attr("href");
            String cardText = element.parent() == null ? "" : element.parent().text();
            Double price = AgentSearchService.extractPrice(cardText);
            results.add(new SupplierResult(supplierName(), title, price, null, href, AgentSearchService.extractSku(cardText)));
        }
        return results;
    }
}
