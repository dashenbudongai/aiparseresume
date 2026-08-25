package com.aiparse.cli.service;

import com.aiparse.cli.model.Resume;
import com.aiparse.cli.model.ScoreResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockAiServiceTest {

    @Test
    void extractResumeFindsPhoneAndEmail() {
        String text = "张三\n电话: 13812345678\n邮箱: zhangsan@example.com\n北京\n清华大学 计算机 本科 2020-06\n技能: Java, Spring Boot, Redis";
        Resume r = MockAiService.extractResume(text);
        assertEquals("13812345678", r.getPhone());
        assertEquals("zhangsan@example.com", r.getEmail());
        assertEquals("北京", r.getCity());
        assertNotNull(r.getEducation());
        assertFalse(r.getEducation().isEmpty());
        assertTrue(r.getSkills().contains("Java"));
    }

    @Test
    void scoreIsBoundedAndHasQuestions() {
        ScoreResult s = MockAiService.score("Java 后端, Redis, MySQL", "招聘 Java 工程师, 熟悉 Redis, MySQL");
        assertNotNull(s.getOverallScore());
        assertTrue(s.getOverallScore() >= 0 && s.getOverallScore() <= 100);
        assertNotNull(s.getInterviewQuestions());
        assertFalse(s.getInterviewQuestions().isEmpty());
    }
}
