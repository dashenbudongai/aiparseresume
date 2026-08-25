package com.aiparse.cli.service;

/**
 * Centralized prompt templates. Kept short and explicit so the model is
 * forced to return a single, parseable JSON object.
 */
public final class Prompts {
    private Prompts() {}

    public static final String RESUME_SYSTEM = """
            你是一个严谨的简历信息抽取助手。
            仅根据用户提供的简历文本提取信息。
            严格输出一个 JSON 对象，不要包含任何解释、Markdown 代码块或多余文本。
            如果某字段无法从简历中识别，填空字符串或空数组。
            """;

    public static final String RESUME_USER_TEMPLATE = """
            请从以下简历文本中提取关键信息，并按下面的 JSON Schema 输出（字段名、嵌套结构必须完全一致）：

            {
              "name": "姓名",
              "phone": "电话",
              "email": "邮箱",
              "city": "所在城市",
              "education": [
                {
                  "school": "学校",
                  "major": "专业",
                  "degree": "学历",
                  "graduation_time": "毕业时间，格式 YYYY-MM"
                }
              ],
              "skills": ["技能1", "技能2"]
            }

            要求：
            1) 只输出上述 JSON 对象本身。
            2) 字段值若在简历中无法确定，使用空字符串或空数组。
            3) skills 数组去重，去掉无意义的标签。

            简历文本：
            <<<
            %s
            >>>
            """;

    public static final String SCORE_SYSTEM = """
            你是一个资深的招聘评估专家。
            严格根据用户提供的简历与岗位描述（JD）进行匹配评分。
            仅输出一个 JSON 对象，不要包含任何解释、Markdown 代码块或额外文本。
            所有 score 字段必须是 0 到 100 的整数。
            """;

    public static final String SCORE_USER_TEMPLATE = """
            请对以下简历与岗位描述进行匹配评分，并按下面的 JSON Schema 输出：

            {
              "overall_score": 0,
              "skill_score": 0,
              "experience_score": 0,
              "education_score": 0,
              "comment": "简要中文评价（<=120 字）",
              "interview_questions": ["面试问题1", "面试问题2"]
            }

            评分说明：
            - overall_score 综合得分，0-100 整数。
            - skill_score 技能匹配度。
            - experience_score 经验/项目相关度。
            - education_score 学历/专业相关度。
            - interview_questions 给出 2-3 个针对性面试问题。

            简历：
            <<<
            %s
            >>>

            岗位描述（JD）：
            <<<
            %s
            >>>
            """;
}
