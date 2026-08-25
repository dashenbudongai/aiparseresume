package com.aiparse.cli.service;

import com.aiparse.cli.exception.CliException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for parsing JSON returned by the AI. Tries to repair the most
 * common malformations (code fences, trailing commas, leading prose)
 * before giving up.
 */
public class JsonExtractor {
    private static final Logger log = LoggerFactory.getLogger(JsonExtractor.class);
    private static final Pattern FENCE = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Extract and repair a JSON value from a free-form AI response. Returns
     * the first parseable JSON object/array found.
     */
    public static JsonNode extract(String text) {
        if (text == null || text.isBlank()) {
            throw new CliException("AI returned empty content", 4);
        }
        // 1) try the raw string
        JsonNode direct = tryParse(text.trim());
        if (direct != null) return direct;

        // 2) try stripping code fences
        Matcher m = FENCE.matcher(text);
        if (m.find()) {
            JsonNode fromFence = tryParse(m.group(1).trim());
            if (fromFence != null) return fromFence;
        }

        // 3) find the first balanced {...} or [...] block
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{' || c == '[') {
                String sub = text.substring(i);
                int end = findMatchingBracket(sub, c);
                if (end > 0) {
                    String candidate = sub.substring(0, end + 1);
                    JsonNode parsed = tryParse(candidate);
                    if (parsed != null) return parsed;
                    // try repairs
                    String repaired = repair(candidate);
                    if (repaired != null) {
                        JsonNode repairedNode = tryParse(repaired);
                        if (repairedNode != null) return repairedNode;
                    }
                }
            }
        }
        throw new CliException("AI did not return a recognizable JSON payload", 4);
    }

    /**
     * Validate that a parsed node contains the resume fields we need.
     * Throws CliException if the contract is violated.
     */
    public static void validateResume(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new CliException("Resume JSON must be an object", 4);
        }
        for (String f : new String[]{"name", "phone", "email", "city", "skills"}) {
            if (node.get(f) == null) {
                throw new CliException("Resume JSON missing required field: " + f, 4);
            }
        }
        if (node.get("skills") != null && !node.get("skills").isArray()) {
            throw new CliException("Resume JSON field 'skills' must be an array", 4);
        }
        if (node.get("education") != null && !node.get("education").isArray()) {
            throw new CliException("Resume JSON field 'education' must be an array", 4);
        }
    }

    public static void validateScore(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new CliException("Score JSON must be an object", 4);
        }
        JsonNode overall = node.get("overall_score");
        if (overall == null || !overall.isNumber()) {
            throw new CliException("Score JSON missing numeric 'overall_score'", 4);
        }
        int v = overall.asInt();
        if (v < 0 || v > 100) {
            throw new CliException("Score JSON 'overall_score' out of range 0-100: " + v, 4);
        }
    }

    private static JsonNode tryParse(String s) {
        try {
            return MAPPER.readTree(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Repair common malformations: trailing commas, single quotes,
     * unescaped newlines in strings, JS-style comments.
     */
    static String repair(String s) {
        String r = s;
        r = r.replaceAll(",\\s*([}\\]])", "$1");
        r = r.replaceAll("/\\*.*?\\*/", "");
        r = r.replaceAll("(?m)//.*$", "");
        // Replace single-quoted keys/values with double quotes (very rough)
        if (r.contains("'") && !r.contains("\"")) {
            r = r.replace('\'', '"');
        }
        return r;
    }

    private static int findMatchingBracket(String s, char open) {
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inStr = false;
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') { inStr = !inStr; continue; }
            if (inStr) continue;
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
