package com.aiparse.cli.command;

import com.aiparse.cli.exception.CliException;
import com.aiparse.cli.model.ScoreResult;
import com.aiparse.cli.service.AiService;
import com.aiparse.cli.service.PdfService;
import com.aiparse.cli.service.ResumeExtractor;
import com.aiparse.cli.util.OutputWriter;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
        name = "score",
        mixinStandardHelpOptions = true,
        description = "读取 PDF 简历与 JD 文本，调用 AI 输出匹配评分。"
)
public class ScoreCommand extends BaseCommand {

    @CommandLine.Parameters(index = "0", paramLabel = "PDF_PATH", description = "简历 PDF 路径。")
    Path pdfPath;

    @CommandLine.Option(names = {"--jd"}, required = true, paramLabel = "JD_PATH",
            description = "岗位描述文本文件路径。")
    Path jdPath;

    @Override
    public Integer call() {
        if (!Files.exists(jdPath)) {
            throw new CliException("JD file not found: " + jdPath, 2);
        }
        if (!Files.isRegularFile(jdPath)) {
            throw new CliException("JD path is not a regular file: " + jdPath, 2);
        }

        String jdText;
        try {
            jdText = Files.readString(jdPath);
        } catch (Exception e) {
            throw new CliException(3, "Failed to read JD file: " + e.getMessage(), e);
        }
        if (jdText.trim().isEmpty()) {
            throw new CliException("JD file is empty: " + jdPath, 3);
        }

        PdfService pdf = newPdfService();
        String resumeText = pdf.extractText(pdfPath);
        AiService ai = newAiService();
        if (ai.isMock()) {
            System.err.println("[WARN] No QWEN_API_KEY set, running in MOCK mode.");
        }
        ScoreResult result = newExtractor(ai).score(resumeText, jdText);
        String json = OutputWriter.toJson(result);
        System.out.println(json);
        OutputWriter.writeIfRequested(output, result);
        return 0;
    }
}
