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
public class AmazonSearchClient implements SupplierSearchClient {
    private final HtmlFetcher fetcher;

    public AmazonSearchClient(HtmlFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String supplierName() { return "Amazon"; }

    @Override
    public String searchLink(String query) {
        return "https://www.amazon.com/s?k=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    @Override
    public List<SupplierResult> search(String query) {
        String html = fetcher.fetch(searchLink(query));
        Document doc = Jsoup.parse(html);
        List<SupplierResult> results = new ArrayList<>();
        for (Element element : doc.select("div.s-result-item h2 a").stream().limit(5).toList()) {
            String title = element.text();
            String href = element.absUrl("href");
            String text = element.parent() == null ? "" : element.parent().text();
            results.add(new SupplierResult(supplierName(), title, AgentSearchService.extractPrice(text), null, href, null));
        }
        return results;
    }
}
