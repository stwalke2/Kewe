package com.kewe.core.agent;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PromptParser {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\b(\\d+)\\b");
    private static final Pattern FOR_PATTERN = Pattern.compile("\\bfor\\s+([a-zA-Z0-9\\s-]+)$", Pattern.CASE_INSENSITIVE);
    private static final Set<String> STOPWORDS = new HashSet<>(List.of("i", "need", "to", "purchase", "buy", "please", "get", "a", "an", "the", "for", "we", "want", "require"));

    public ParsedPrompt parse(String prompt) {
        String safePrompt = prompt == null ? "" : prompt.trim();
        int quantity = 1;
        Matcher integerMatcher = INTEGER_PATTERN.matcher(safePrompt);
        if (integerMatcher.find()) {
            quantity = Integer.parseInt(integerMatcher.group(1));
        }

        String orgHint = null;
        Matcher forMatcher = FOR_PATTERN.matcher(safePrompt);
        if (forMatcher.find()) {
            orgHint = forMatcher.group(1).trim();
        }

        String cleaned = safePrompt.toLowerCase();
        cleaned = cleaned.replaceAll("\\b\\d+\\b", " ");
        cleaned = cleaned.replaceAll("[^a-z0-9\\s]", " ");
        List<String> keywords = Arrays.stream(cleaned.split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOPWORDS.contains(token))
                .distinct()
                .toList();

        String item = String.join(" ", keywords);
        if (item.isBlank()) {
            item = safePrompt;
        }

        return new ParsedPrompt(quantity, item.trim(), keywords, orgHint, item.trim());
    }

    public record ParsedPrompt(int quantity, String item, List<String> keywords, String orgHint, String normalizedQuery) {}
}
