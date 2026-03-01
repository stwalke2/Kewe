package com.kewe.core.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kewe")
public class AgentSearchProperties {
    private String websearchProvider = "none";
    private String serpapiKey = "";
    private String serpapiEngine = "google";
    private int websearchCount = 10;
    private int websearchTimeoutMs = 7000;
    private int websearchCacheTtlSeconds = 3600;

    public String getWebsearchProvider() {
        return websearchProvider == null || websearchProvider.isBlank() ? "none" : websearchProvider;
    }

    public void setWebsearchProvider(String websearchProvider) {
        this.websearchProvider = websearchProvider;
    }

    public String getSerpapiKey() {
        return serpapiKey;
    }

    public void setSerpapiKey(String serpapiKey) {
        this.serpapiKey = serpapiKey;
    }

    public String getSerpapiEngine() {
        return serpapiEngine == null || serpapiEngine.isBlank() ? "google" : serpapiEngine;
    }

    public void setSerpapiEngine(String serpapiEngine) {
        this.serpapiEngine = serpapiEngine;
    }

    public int getWebsearchCount() {
        return websearchCount;
    }

    public void setWebsearchCount(int websearchCount) {
        this.websearchCount = websearchCount;
    }

    public int getWebsearchTimeoutMs() {
        return websearchTimeoutMs;
    }

    public void setWebsearchTimeoutMs(int websearchTimeoutMs) {
        this.websearchTimeoutMs = websearchTimeoutMs;
    }

    public int getWebsearchCacheTtlSeconds() {
        return websearchCacheTtlSeconds;
    }

    public void setWebsearchCacheTtlSeconds(int websearchCacheTtlSeconds) {
        this.websearchCacheTtlSeconds = websearchCacheTtlSeconds;
    }

    public boolean hasSearchKey() {
        return "serpapi".equalsIgnoreCase(getWebsearchProvider()) && serpapiKey != null && !serpapiKey.isBlank();
    }
}
