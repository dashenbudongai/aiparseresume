package com.aiparse.cli.service;

import com.aiparse.cli.model.Education;
import com.aiparse.cli.model.Resume;
import com.aiparse.cli.model.ScoreResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic, offline stub of the AI model. Used when the user passes
 * --mock or no API key is configured. Produces plausible-looking JSON
 * derived from keyword matching on the resume / JD text.
 */
public class MockAiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Resume extractResume(String resumeText) {
        Resume r = new Resume();
        r.setName(guessName(resumeText));
        r.setPhone(RegexUtil.firstMatch(resumeText, "1[3-9]\\d{9}"));
        r.setEmail(RegexUtil.firstMatch(resumeText, "[\\w.+-]+@[\\w-]+\\.[\\w.-]+"));
        r.setCity(guessCity(resumeText));
        r.setEducation(guessEducation(resumeText));
        r.setSkills(guessSkills(resumeText));
        return r;
    }

    public static ScoreResult score(String resumeText, String jdText) {
        ScoreResult s = new ScoreResult();
        String jd = jdText == null ? "" : jdText.toLowerCase(Locale.ROOT);
        List<String> skills = guessSkills(resumeText);
        long hits = skills.stream()
                .filter(sk -> jd.contains(sk.toLowerCase(Locale.ROOT)))
                .count();
        int skillScore = skills.isEmpty() ? 50
                : (int) Math.round(50.0 + 50.0 * hits / Math.max(skills.size(), 1));
        skillScore = clamp(skillScore);

        int experienceScore = clamp(60 + (int) (Math.random() * 25));
        int educationScore = clamp(60 + (int) (Math.random() * 30));

        int overall = (int) Math.round(skillScore * 0.5 + experienceScore * 0.3 + educationScore * 0.2);
        overall = clamp(overall);

        s.setOverallScore(overall);
        s.setSkillScore(skillScore);
        s.setExperienceScore(experienceScore);
        s.setEducationScore(educationScore);
        s.setComment(String.format(
                "Mock scoring: %d skill keywords matched out of %d. Result is illustrative only.",
                hits, skills.size()));
        s.setInterviewQuestions(new ArrayList<>(Arrays.asList(
                "请介绍一个你最熟悉的全栈项目？",
                "你在项目中如何调用大模型 API？",
                "请说明一次你解决复杂性能问题的经历。")));
        return s;
    }

    public static String resumeAsJsonString(String resumeText) {
        try {
            return MAPPER.writeValueAsString(extractResume(resumeText));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String scoreAsJsonString(String resumeText, String jdText) {
        try {
            return MAPPER.writeValueAsString(score(resumeText, jdText));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode resumeAsJsonNode(String resumeText) {
        return MAPPER.valueToTree(extractResume(resumeText));
    }

    public static JsonNode scoreAsJsonNode(String resumeText, String jdText) {
        return MAPPER.valueToTree(score(resumeText, jdText));
    }

    public static ObjectNode resumeAsObjectNode(String resumeText) {
        return (ObjectNode) resumeAsJsonNode(resumeText);
    }

    private static int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    private static String guessName(String text) {
        if (text == null) return "未识别";
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            // Match "Name: Zhang San" / "Name：张三" / "姓名：张三" (ASCII or full-width colon)
            String lower = t.toLowerCase(Locale.ROOT);
            int colon = Math.max(t.indexOf(':'), t.indexOf('：'));
            boolean hasNamePrefix = lower.startsWith("name") || t.startsWith("姓名");
            if (hasNamePrefix && colon >= 0) {
                String after = t.substring(colon + 1).trim();
                if (!after.isEmpty()) return after;
            }
            if (t.length() <= 12 && t.length() >= 2
                    && t.matches("[\u4e00-\u9fa5]{2,4}")) {
                return t;
            }
        }
        return "未识别";
    }

    private static String guessCity(String text) {
        if (text == null) return null;
        for (String city : Set.of("北京", "上海", "深圳", "广州", "杭州", "成都", "南京", "武汉", "苏州", "西安",
                "Beijing", "Shanghai", "Shenzhen", "Guangzhou", "Hangzhou")) {
            if (text.contains(city)) return city;
        }
        return null;
    }

    private static List<Education> guessEducation(String text) {
        List<Education> list = new ArrayList<>();
        if (text == null) return list;
        String[] schools = {"清华大学", "北京大学", "浙江大学", "上海交通大学", "复旦大学",
                "南京大学", "武汉大学", "中山大学", "哈尔滨工业大学", "同济大学",
                "Tsinghua", "Peking", "MIT", "Stanford", "Harvard", "Berkeley"};
        for (String s : schools) {
            if (text.contains(s)) {
                Education e = new Education();
                e.setSchool(s);
                e.setMajor("计算机科学与技术");
                e.setDegree("本科");
                e.setGraduationTime("2020-06");
                list.add(e);
                break;
            }
        }
        return list;
    }

    private static List<String> guessSkills(String text) {
        if (text == null) return new ArrayList<>();
        String[] known = {"Java", "Python", "Go", "Spring", "Spring Boot", "MySQL", "Redis",
                "Kafka", "Docker", "Kubernetes", "React", "Vue", "TypeScript", "AWS",
                "大模型", "LLM", "RAG", "LangChain", "OpenAI", "Qwen", "Hadoop", "Spark"};
        List<String> out = new ArrayList<>();
        for (String s : known) {
            if (text.contains(s)) out.add(s);
        }
        if (out.isEmpty()) out.add("Java");
        return out;
    }
}
