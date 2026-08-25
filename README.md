# resume-cli

> AI 简历解析 CLI Demo：解析 PDF 简历、调用 Qwen 提取结构化信息、并按 JD 给出匹配评分。

## 项目简介

`resume-cli` 是一个轻量的命令行工具，面向招聘初筛场景：

1. `parse` — 从本地 PDF 简历中提取纯文本；
2. `extract` — 调用 Qwen（大模型）抽取姓名/电话/邮箱/教育/技能等结构化字段；
3. `score` — 将简历与岗位描述（JD）一起交给 Qwen，得到 0–100 的匹配评分与面试问题。

在缺少 API Key 的情况下，工具支持 `--mock` 离线模式，使用基于关键字的启发式逻辑给出示意性结果，方便演示。

## 技术选型

| 关注点 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 21（兼容 JDK 17+） |
| 构建 | Maven + `maven-shade-plugin` 打 fat-jar |
| CLI 框架 | [picocli](https://picocli.info/) |
| PDF 解析 | Apache PDFBox 3.x |
| JSON | Jackson |
| HTTP | `java.net.http.HttpClient`（JDK 内置） |
| 日志 | SLF4J + Logback |
| AI 模型 | 阿里云 DashScope 提供的 **Qwen**（OpenAI 兼容接口） |

## 环境变量配置

| 变量 | 说明 | 必填 |
| --- | --- | --- |
| `QWEN_API_KEY` | DashScope / Qwen 的 API Key，用于调用真实模型 | `extract`/`score` 真实调用时必填；`--mock` 或未设置时自动降级为本地 mock |
| `QWEN_BASE_URL` | 自定义兼容网关地址，默认 `https://dashscope.aliyuncs.com/compatible-mode/v1` | 否 |
| `QWEN_MODEL` | 模型名，默认 `qwen-plus` | 否 |

**优先级（高 → 低）**

1. CLI 参数：`--api-key / --base-url / --model`
2. 进程环境变量：`QWEN_API_KEY / QWEN_BASE_URL / QWEN_MODEL`
3. 项目根目录的 `.env` 文件
4. 内置默认值

### 方式一：CLI 参数

```bash
resume-cli extract --api-key sk-xxxx samples/resume.pdf
```

### 方式二：系统环境变量

```powershell
# PowerShell 当前窗口
$env:QWEN_API_KEY = "sk-xxxx"
resume-cli extract samples\resume.pdf

# 永久（用户级）
[Environment]::SetEnvironmentVariable("QWEN_API_KEY", "sk-xxxx", "User")
```

```bash
# Linux / macOS
export QWEN_API_KEY="sk-xxxx"
```

### 方式三：项目根目录的 `.env` 文件

适合不想暴露到 shell history、又希望换项目自动切换 Key 的场景。

1. 复制模板：
   ```bash
   cp .env.example .env        # Linux / macOS
   copy .env.example .env      # Windows
   ```
2. 编辑 `.env`，填入真实 Key：
   ```dotenv
   QWEN_API_KEY=sk-xxxx
   # QWEN_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
   # QWEN_MODEL=qwen-plus
   ```
3. 直接调用，不需要再传 `--api-key`：
   ```bash
   resume-cli extract samples\resume.pdf
   ```

`.env` 文件**已加入 `.gitignore`**，不会随仓库泄露。`.env.example` 会被提交，作为模板。
支持常见语法：注释行（`#`）、`export KEY=value` 前缀、双/单引号包裹值、行内注释（`KEY=value # comment`）。

## 安装方式

需要本地安装 Maven 3.8+ 与 JDK 17+（建议 21）。

```bash
# 编译 + 打 fat-jar
mvn -DskipTests package

# 生成可执行 jar
ls target/resume-cli.jar
```

如果已经构建成功，可直接把 `target/resume-cli.jar` 拷走使用，无需 Maven。

项目根目录自带启动脚本 `resume-cli`（POSIX）和 `resume-cli.cmd`（Windows），构建后可直接调用：

```bash
# Linux / macOS / Git Bash
./resume-cli parse samples/resume.pdf

# Windows PowerShell / cmd
.\resume-cli.cmd parse samples\resume.pdf
```

#### 启动脚本说明

两个脚本做的事情是一样的：定位 `target/resume-cli.jar`、找一个 JDK 17+、然后 `java -jar` 跑起来。

| 脚本 | 适用平台 | JDK 查找顺序 |
| --- | --- | --- |
| `resume-cli` | Linux / macOS / Git Bash | 1) `$JAVA_HOME` → 2) `/usr/lib/jvm`、`/opt/java` 等常见目录 → 3) PATH 上的 `java` |
| `resume-cli.cmd` | Windows PowerShell / cmd | 1) `%JAVA_HOME%` → 2) `C:\Program Files\Java`、`Eclipse Adoptium` 等 → 3) PATH 上的 `java` |

Windows 脚本实际上是两层：`resume-cli.cmd` → `resume-cli.ps1`，目的是在 PowerShell 没在 PATH 时也能用。

常见用法：

```powershell
# 提取 + 写文件
.\resume-cli.cmd extract samples\resume.pdf -o result.json

# 用 --mock 离线演示
.\resume-cli.cmd score --mock samples\resume.pdf --jd samples\jd.txt

# 显式指定 API Key
.\resume-cli.cmd extract --api-key sk-xxxx samples\resume.pdf

# 切换到备用模型
.\resume-cli.cmd score --model qwen-turbo samples\resume.pdf --jd samples\jd.txt

# 打开 DEBUG 日志
.\resume-cli.cmd extract -v samples\resume.pdf
```

需要全局使用 `resume-cli` 命令时，把 `resume-cli` / `resume-cli.cmd` 与 `target/resume-cli.jar` 一起放到 PATH 上的目录即可。

Windows 用户也可以只把项目根目录加入 PATH（仅当前会话）：

```powershell
$env:Path = "$PWD;$env:Path"
resume-cli parse samples\resume.pdf
```

> Windows PowerShell 下若默认 Java 是 8，可临时切换：
> `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21" ; $env:Path = "$env:JAVA_HOME\bin;$env:Path"`

## CLI 命令说明

```
resume-cli [-hvV] [COMMAND]

Commands:
  parse    读取本地 PDF 简历并输出提取到的纯文本。
  extract  读取 PDF 简历并调用 AI 提取结构化信息。
  score    读取 PDF 简历与 JD 文本，调用 AI 输出匹配评分。
```

### 通用选项

| 选项 | 说明 |
| --- | --- |
| `-h, --help` | 查看帮助 |
| `-V, --version` | 打印版本 |
| `-v, --verbose` | 打开 DEBUG 日志（写到 stderr） |
| `--api-key <key>` | 覆盖 `QWEN_API_KEY` |
| `--base-url <url>` | 覆盖 base URL |
| `--model <name>` | 覆盖模型名 |
| `--mock` | 强制使用本地 mock AI（不联网） |
| `-o, --output <file>` | 将 JSON 结果额外写入指定文件 |

### `parse`

```bash
java -jar target/resume-cli.jar parse samples/resume.pdf
# 或
./resume-cli parse samples/resume.pdf             # Linux / macOS / Git Bash
.\resume-cli.cmd parse samples\resume.pdf         # Windows PowerShell / cmd
```

输出形如：

```json
{
  "path": "samples/resume.pdf",
  "length": 339,
  "text": "姓名：张三\n电话：13812345678\n..."
}
```

错误处理：

- 文件不存在 → `Error: File not found: <path>`（exit 2）
- 文件不是 PDF（magic number 校验）→ `Error: File is not a PDF: <path>`（exit 2）
- PDF 加密 / 解析失败 → `Error: Failed to read PDF: <msg>`（exit 3）
- PDF 文本为空 → `Error: PDF text is empty: <path>`（exit 3）

### `extract`

```bash
# 真实调用 Qwen
java -jar target/resume-cli.jar extract samples/resume.pdf

# 或使用启动脚本（推荐，Windows 上会按需切换 JDK 17+）
./resume-cli extract samples/resume.pdf                # Linux / macOS / Git Bash
.\resume-cli.cmd extract samples\resume.pdf            # Windows PowerShell / cmd

# 离线演示
java -jar target/resume-cli.jar extract --mock samples/resume.pdf
.\resume-cli.cmd extract --mock samples\resume.pdf     # Windows 同理
```

输出 JSON Schema：

```json
{
  "name": "张三",
  "phone": "13812345678",
  "email": "zhangsan@example.com",
  "city": "北京",
  "education": [
    {
      "school": "清华大学",
      "major": "计算机科学与技术",
      "degree": "本科",
      "graduation_time": "2020-06"
    }
  ],
  "skills": ["Java", "Spring Boot", "Qwen", "LLM"]
}
```

如果模型输出被 Markdown 代码块包裹、含有尾逗号、夹杂中文引言等，工具会尝试自动修复并重新解析；若仍失败，输出清晰的错误信息。

### `score`

```bash
java -jar target/resume-cli.jar score samples/resume.pdf --jd samples/jd.txt

# Windows 推荐用启动脚本（自动切换 JDK 17+，无需手动设置 JAVA_HOME）
.\resume-cli.cmd score samples\resume.pdf --jd samples\jd.txt

# 离线演示
.\resume-cli.cmd score --mock samples\resume.pdf --jd samples\jd.txt
```

输出示例：

```json
{
  "overall_score": 82,
  "skill_score": 96,
  "experience_score": 66,
  "education_score": 69,
  "comment": "Mock scoring: 11 skill keywords matched out of 12. Result is illustrative only.",
  "interview_questions": [
    "请介绍一个你最熟悉的全栈项目？",
    "你在项目中如何调用大模型 API？",
    "请说明一次你解决复杂性能问题的经历。"
  ]
}
```

错误处理：

- JD 文件不存在 / 不是普通文件 → 退出码 2
- JD 文件为空 → `Error: JD file is empty: <path>`（退出码 3）

## 示例输入与输出

仓库 `samples/` 目录自带中文示例：

```
samples/
├── resume.pdf    # 用 PDFBox + SimHei 生成的中文样例简历
└── jd.txt        # 高级全栈工程师 JD（中文）
```

```bash
# 1. 解析 PDF
java -jar target/resume-cli.jar parse samples/resume.pdf

# 2. 提取结构化信息（mock 模式）
java -jar target/resume-cli.jar extract --mock samples/resume.pdf -o result.json

# 3. 与 JD 评分（mock 模式）
java -jar target/resume-cli.jar score --mock samples/resume.pdf \
     --jd samples/jd.txt -o score.json

# Windows 上用启动脚本更省心（无需先 cd 到项目根目录、也无需 mvn）
.\resume-cli.cmd parse  samples\resume.pdf
.\resume-cli.cmd extract --mock samples\resume.pdf -o result.json
.\resume-cli.cmd score   --mock samples\resume.pdf --jd samples\jd.txt -o score.json
```

## 已实现功能

- `parse / extract / score` 三条主命令
- picocli 自动生成 `--help`、参数校验、错误信息
- PDF 解析（PDFBox）+ 错误分类（不存在 / 非 PDF / 不可读 / 空）
- 真实 Qwen（DashScope OpenAI 兼容接口）调用
- AI 返回 JSON 的自动修复（剥离 code fence、删尾逗号、定位首个 JSON 块）
- Resume / Score 字段 schema 校验
- `--mock` 离线模式（无 API Key 也能演示）
- `-o / --output` 将结果写入 JSON 文件
- SLF4J + Logback 日志输出到 stderr
- Dockerfile + Makefile
- JUnit 5 单元测试（12 用例，覆盖 PDF / JSON 修复 / Mock 评分 / CLI 帮助）

## 开发与测试

```bash
mvn test                  # 全部 12 个测试
mvn -DskipTests package   # 打 fat-jar
mvn exec:java -Dexec.mainClass=com.aiparse.cli.tools.SampleResumeGen
                       # 重新生成 samples/ 下的样例
```

## Makefile 常用目标

```bash
make build      # mvn -DskipTests package
make test       # mvn test
make run-parse  # 以 mock 模式跑一遍示例
make run-score  # 以 mock 模式对示例做评分
make clean      # mvn clean
```

## Docker

```bash
docker build -t resume-cli:latest .
docker run --rm -v $(pwd)/samples:/samples resume-cli:latest \
    score --mock /samples/resume.pdf --jd /samples/jd.txt
```

## 已知问题 / 未完成

- 当前 Mock 的 `education.major / degree` 在英文简历场景下只会硬编码中文默认值，仅用于演示；
- 没有实现 PDF 表格/图片/扫描件 OCR（依赖 PDFBox 文本提取，扫描件需先自行 OCR）；
- `interview_questions` 来自 prompt 中的硬编码要求模型返回 2-3 条，模型版本不同时条数可能不同；
- 没有实现重试 / 限流，调用真实 Qwen 时若遇 429 请自行重试；
- 日志目前全部走 stderr，stdout 严格保留为 JSON 输出，便于管道 `| jq`。

## 目录结构

```
AIParseCliDemo/
├── pom.xml
├── README.md
├── Dockerfile
├── Makefile
├── samples/                          # 示例简历与 JD
└── src/
    ├── main/
    │   ├── java/com/aiparse/cli/
    │   │   ├── Main.java
    │   │   ├── command/              # parse / extract / score
    │   │   ├── exception/
    │   │   ├── model/                # Resume / Education / ScoreResult
    │   │   ├── service/              # Pdf / Ai / JsonExtractor / Mock / Prompts
    │   │   └── util/                 # OutputWriter
    │   └── resources/logback.xml
    └── test/java/com/aiparse/cli/
        ├── MainCliTest.java
        ├── service/{JsonExtractor,MockAiService,PdfService}Test.java
        └── tools/SampleResumeGen.java
```
