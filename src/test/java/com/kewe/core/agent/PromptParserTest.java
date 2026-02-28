package com.kewe.core.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptParserTest {
    private final PromptParser parser = new PromptParser();

    @Test
    void parsesQuantityItemAndOrgHint() {
        PromptParser.ParsedPrompt parsed = parser.parse("I need to purchase 6 5ml glass beakers for biology");

        assertThat(parsed.quantity()).isEqualTo(6);
        assertThat(parsed.item()).contains("5ml").contains("glass").contains("beakers");
        assertThat(parsed.orgHint()).isEqualTo("biology");
        assertThat(parsed.uom()).isEqualTo("ml");
    }

    @Test
    void defaultsQuantityToOneWhenMissing() {
        PromptParser.ParsedPrompt parsed = parser.parse("buy microscope slides for chemistry");

        assertThat(parsed.quantity()).isEqualTo(1);
        assertThat(parsed.orgHint()).isEqualTo("chemistry");
    }
}
