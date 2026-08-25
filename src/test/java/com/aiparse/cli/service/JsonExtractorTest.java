package com.aiparse.cli.service;

import com.aiparse.cli.exception.CliException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonExtractorTest {

    @Test
    void parsesRawJson() {
        JsonNode n = JsonExtractor.extract("{\"a\":1,\"b\":[2,3]}");
        assertEquals(1, n.get("a").asInt());
        assertEquals(2, n.get("b").size());
    }

    @Test
    void stripsCodeFence() {
        String wrapped = "```json\n{\"x\":true}\n```";
        JsonNode n = JsonExtractor.extract(wrapped);
        assertTrue(n.get("x").asBoolean());
    }

    @Test
    void repairsTrailingComma() {
        String raw = "Some prose.\n{\"name\":\"Tom\",}";
        JsonNode n = JsonExtractor.extract(raw);
        assertEquals("Tom", n.get("name").asText());
    }

    @Test
    void validatesResumeFields() {
        JsonNode good = JsonExtractor.extract("{\"name\":\"a\",\"phone\":\"b\",\"email\":\"c\",\"city\":\"d\",\"skills\":[]}");
        assertDoesNotThrow(() -> JsonExtractor.validateResume(good));

        JsonNode bad = JsonExtractor.extract("{\"name\":\"a\"}");
        assertThrows(CliException.class, () -> JsonExtractor.validateResume(bad));
    }

    @Test
    void validatesScoreRange() {
        JsonNode good = JsonExtractor.extract("{\"overall_score\":50}");
        assertDoesNotThrow(() -> JsonExtractor.validateScore(good));
        JsonNode bad = JsonExtractor.extract("{\"overall_score\":150}");
        assertThrows(CliException.class, () -> JsonExtractor.validateScore(bad));
    }
}
