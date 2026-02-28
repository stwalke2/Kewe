package com.kewe.core.agent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightBrowserManager {
    private Playwright playwright;
    private Browser browser;

    public synchronized Browser browser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
        return browser;
    }

    @PreDestroy
    public synchronized void close() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        browser = null;
        playwright = null;
    }
}
