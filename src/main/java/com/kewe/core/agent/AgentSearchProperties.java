package com.kewe.core.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kewe")
public class AgentSearchProperties {
    private String searchProvider = "none";
    private String serpapiKey;
    private String bingKey;
    private String bingEndpoint = "https://api.bing.microsoft.com/v7.0/search";
    private boolean playwrightEnabled = true;

    public String getSearchProvider() { return searchProvider == null ? "none" : searchProvider; }
    public void setSearchProvider(String searchProvider) { this.searchProvider = searchProvider; }
    public String getSerpapiKey() { return serpapiKey; }
    public void setSerpapiKey(String serpapiKey) { this.serpapiKey = serpapiKey; }
    public String getBingKey() { return bingKey; }
    public void setBingKey(String bingKey) { this.bingKey = bingKey; }
    public String getBingEndpoint() { return bingEndpoint; }
    public void setBingEndpoint(String bingEndpoint) { this.bingEndpoint = bingEndpoint; }
    public boolean isPlaywrightEnabled() { return playwrightEnabled; }
    public void setPlaywrightEnabled(boolean playwrightEnabled) { this.playwrightEnabled = playwrightEnabled; }

    public boolean hasSearchKey() {
        return switch (getSearchProvider().toLowerCase()) {
            case "serpapi" -> serpapiKey != null && !serpapiKey.isBlank();
            case "bing" -> bingKey != null && !bingKey.isBlank();
            default -> false;
        };
    }
}
