package com.aiparse.cli.command;

import com.aiparse.cli.model.Resume;
import com.aiparse.cli.service.AiService;
import com.aiparse.cli.service.PdfService;
import com.aiparse.cli.service.ResumeExtractor;
import com.aiparse.cli.util.OutputWriter;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "extract",
        mixinStandardHelpOptions = true,
        description = "读取 PDF 简历并调用 AI 提取结构化信息（姓名/电话/邮箱/教育/技能等）。"
)
public class ExtractCommand extends BaseCommand {

    @CommandLine.Parameters(index = "0", paramLabel = "PDF_PATH", description = "简历 PDF 路径。")
    Path pdfPath;

    @Override
    public Integer call() {
        PdfService pdf = newPdfService();
        String text = pdf.extractText(pdfPath);
        AiService ai = newAiService();
        if (ai.isMock()) {
            System.err.println("[WARN] No QWEN_API_KEY set, running in MOCK mode. " +
                    "Use --api-key or set the env var for real AI results.");
        }
        Resume resume = newExtractor(ai).extract(text);
        String json = OutputWriter.toJson(resume);
        System.out.println(json);
        OutputWriter.writeIfRequested(output, resume);
        return 0;
    }
}
