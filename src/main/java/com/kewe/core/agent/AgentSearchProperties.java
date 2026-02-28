package com.kewe.core.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kewe")
public class AgentSearchProperties {
    private String searchProvider = "bing";
    private String bingKey;
    private String bingEndpoint = "https://api.bing.microsoft.com/v7.0/search";
    private String bingMkt = "en-US";
    private String bingSafeSearch = "Moderate";
    private int bingResultCount = 5;
    private int bingTimeoutMs = 6000;
    private int searchCacheTtlSeconds = 3600;

    public String getSearchProvider() { return searchProvider == null ? "bing" : searchProvider; }
    public void setSearchProvider(String searchProvider) { this.searchProvider = searchProvider; }
    public String getBingKey() { return bingKey; }
    public void setBingKey(String bingKey) { this.bingKey = bingKey; }
    public String getBingEndpoint() { return bingEndpoint; }
    public void setBingEndpoint(String bingEndpoint) { this.bingEndpoint = bingEndpoint; }
    public String getBingMkt() { return bingMkt; }
    public void setBingMkt(String bingMkt) { this.bingMkt = bingMkt; }
    public String getBingSafeSearch() { return bingSafeSearch; }
    public void setBingSafeSearch(String bingSafeSearch) { this.bingSafeSearch = bingSafeSearch; }
    public int getBingResultCount() { return bingResultCount; }
    public void setBingResultCount(int bingResultCount) { this.bingResultCount = bingResultCount; }
    public int getBingTimeoutMs() { return bingTimeoutMs; }
    public void setBingTimeoutMs(int bingTimeoutMs) { this.bingTimeoutMs = bingTimeoutMs; }
    public int getSearchCacheTtlSeconds() { return searchCacheTtlSeconds; }
    public void setSearchCacheTtlSeconds(int searchCacheTtlSeconds) { this.searchCacheTtlSeconds = searchCacheTtlSeconds; }

    public boolean hasSearchKey() {
        return "bing".equalsIgnoreCase(getSearchProvider()) && bingKey != null && !bingKey.isBlank();
    }
}
