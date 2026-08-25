package com.aiparse.cli.tools;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generate a Chinese sample PDF resume and a Chinese JD under samples/.
 * Run via the Makefile target "samples" or:
 *   mvn -q test-compile
 *   java -cp "target/test-classes;target/classes;<deps>" com.aiparse.cli.tools.SampleResumeGen
 */
public class SampleResumeGen {
    public static void main(String[] args) throws IOException {
        Path target = Path.of("samples", "resume.pdf");
        Files.createDirectories(target.getParent());

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDFont font = loadCjkFont(doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.setFont(font, 14);
                int y = 760;
                for (String line : RESUME_LINES) {
                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    cs.showText(line);
                    cs.endText();
                    y -= 22;
                }
            }
            doc.save(target.toFile());
        }
        System.out.println("Wrote " + target.toAbsolutePath());

        Path jd = Path.of("samples", "jd.txt");
        Files.writeString(jd, SAMPLE_JD);
        System.out.println("Wrote " + jd.toAbsolutePath());
    }

    /**
     * Try to load a CJK-capable font from common system locations. Falls
     * back to Helvetica (which will render boxes for CJK chars) if nothing
     * is found, so the demo still works but visually degraded.
     */
    private static PDFont loadCjkFont(PDDocument doc) throws IOException {
        List<String> candidates = List.of(
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simfang.ttf",
                "/System/Library/Fonts/PingFang.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"
        );
        for (String path : candidates) {
            File f = new File(path);
            if (!f.exists() || !f.isFile()) continue;
            try {
                return PDType0Font.load(doc, f);
            } catch (IOException ignored) {
                // try the next candidate
            }
        }
        System.err.println("[WARN] No CJK font found, falling back to Helvetica. " +
                "Chinese characters may not render correctly in the sample PDF.");
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private static final String[] RESUME_LINES = new String[]{
            "姓名：张三",
            "电话：13812345678",
            "邮箱：zhangsan@example.com",
            "城市：北京",
            "",
            "教育背景：",
            "  清华大学 - 计算机科学与技术 - 本科 - 2020-06",
            "",
            "技能：",
            "  Java、Spring Boot、MySQL、Redis、",
            "  Docker、Kubernetes、Qwen、LLM、RAG、OpenAI",
            "",
            "工作经历：",
            "  某科技公司 高级工程师（2020 - 至今）",
            "  - 负责基于 Spring Boot、MySQL、Redis 的后端服务设计与实现",
            "  - 使用 Docker、Kubernetes 在 AWS 上部署与运维微服务",
            "  - 集成 Qwen / OpenAI 大模型 API，构建 RAG 智能问答系统"
    };

    private static final String SAMPLE_JD = """
            岗位名称：高级全栈工程师

            岗位职责：
            1. 负责基于 Java / Spring Boot 的后端服务设计与开发；
            2. 使用 React 或 Vue 构建响应式前端页面；
            3. 将大模型 API（Qwen、OpenAI 等）集成到产品中；
            4. 在 AWS / Kubernetes 环境下进行服务部署与运维。

            任职要求：
            - 3 年以上 Java 后端开发经验；
            - 熟悉 MySQL、Redis、Kafka 等常用中间件；
            - 具备 Docker、Kubernetes 实际使用经验；
            - 有调用 LLM / OpenAI / Qwen API 的实际经验者优先。
            """;
}
