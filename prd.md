AI 简历解析 CLI Demo
背景
在招聘流程中，快速理解候选人简历并判断其与岗位的匹配程度，是一项常见但耗时的工作。
你的任务是实现一个简单的 命令行 CLI 工具，支持读取 PDF 简历，调用 AI 模型提取关键信息，并根据岗位描述进行匹配评分。
本题重点考察全栈工程师的基础工程能力、文件处理能力、AI API 调用能力和代码组织能力。

---
技术要求
- 编程语言：Java
- 输入文件：PDF简历
- AI 模型：Qwen

---
功能要求
1. 简历文本解析
实现 CLI 命令，读取本地 PDF 简历并提取文本内容。
示例：
resume-cli parse ./resume.pdf
要求：
- 支持读取本地 PDF 文件
- 能够提取 PDF 中的文本内容
- 对以下异常情况有基本错误提示：
  - 文件不存在
  - 文件不是 PDF
  - PDF 无法读取
  - PDF 文本为空

---
2. AI 结构化信息提取
实现 CLI 命令，调用 AI 模型从简历中提取结构化信息。
示例：
resume-cli extract ./resume.pdf
需要提取以下字段：
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
      "graduation_time": "毕业时间"
    }
  ],
  "skills": ["技能1", "技能2"]
}
要求：
- AI 返回结果必须是 JSON
- 需要对 AI 返回结果做基本校验
- 如果 AI 调用失败，需要有清晰错误提示

---
3. JD 匹配评分
实现 CLI 命令，输入一份简历和一份岗位描述文件，调用 AI 进行匹配评分。
示例：
resume-cli score ./resume.pdf --jd ./jd.txt
输出 JSON 格式评分结果：
{
  "overall_score": 82,
  "skill_score": 88,
  "experience_score": 80,
  "education_score": 75,
  "comment": "候选人具备较好的全栈开发基础，技能与岗位要求较匹配，但缺少明确的大模型应用经验。",
  "interview_questions": [
    "请介绍一个你主导过的全栈项目。",
    "你是否有调用大模型 API 的实际经验？"
  ]
}
要求：
- 支持读取 JD 文本文件
- 评分范围为 0-100
- 评分结果需要包含简要理由
- 对 JD 文件为空、文件不存在等情况有基本错误处理

---
CLI 命令要求
至少实现以下三个命令：
resume-cli parse <pdf_path>
resume-cli extract <pdf_path>
resume-cli score <pdf_path> --jd <jd_path>
要求：
- 命令参数清晰
- 支持 --help
- 输出结果适合在终端查看
- JSON 输出格式清晰

---
工程质量要求
项目需要包含：
- 清晰的项目结构
- README.md
- 示例命令
- 至少 1-2 个基础测试，或提供 mock AI 模式用于演示


加分项
以下功能不是必须，完成任意一项即可加分：
- 支持 --output result.json 保存结果
- 支持 --mock 模式，在没有 AI API Key 时也能演示
- 支持从 AI 返回结果中自动修复常见 JSON 格式错误
- 支持简单日志输出
- 有 Dockerfile 或 Makefile

README 必须包含
- 项目简介
- 技术选型
- 环境变量配置方式
- 安装方式
- CLI 命令说明
- 示例输入和输出
- 已实现功能
- 已知问题或未完成内容