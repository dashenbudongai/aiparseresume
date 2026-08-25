package com.aiparse.cli.service;

import com.aiparse.cli.model.Resume;
import com.aiparse.cli.model.ScoreResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level coordinator: prompts the AI, extracts JSON, validates the
 * contract, and maps into Java model classes. Handles mock fallback.
 */
public class ResumeExtractor {
    private static final Logger log = LoggerFactory.getLogger(ResumeExtractor.class);

    private final AiService ai;

    public ResumeExtractor(AiService ai) {
        this.ai = ai;
    }

    public Resume extract(String resumeText) {
        if (ai.isMock()) {
            log.info("Using MOCK mode for extract");
            return MockAiService.extractResume(resumeText);
        }
        String prompt = String.format(Prompts.RESUME_USER_TEMPLATE, resumeText);
        String raw = ai.chat(Prompts.RESUME_SYSTEM, prompt);
        JsonNode node = JsonExtractor.extract(raw);
        JsonExtractor.validateResume(node);
        return JsonExtractor.mapper().convertValue(node, Resume.class);
    }

    public ScoreResult score(String resumeText, String jdText) {
        if (ai.isMock()) {
            log.info("Using MOCK mode for score");
            return MockAiService.score(resumeText, jdText);
        }
        String prompt = String.format(Prompts.SCORE_USER_TEMPLATE, resumeText, jdText);
        String raw = ai.chat(Prompts.SCORE_SYSTEM, prompt);
        JsonNode node = JsonExtractor.extract(raw);
        JsonExtractor.validateScore(node);
        return JsonExtractor.mapper().convertValue(node, ScoreResult.class);
    }
}
