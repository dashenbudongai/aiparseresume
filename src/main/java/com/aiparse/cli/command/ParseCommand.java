package com.aiparse.cli.command;

import com.aiparse.cli.service.PdfService;
import com.aiparse.cli.util.OutputWriter;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;

@CommandLine.Command(
        name = "parse",
        mixinStandardHelpOptions = true,
        description = "读取本地 PDF 简历并输出提取到的纯文本。"
)
public class ParseCommand extends BaseCommand {

    @CommandLine.Parameters(index = "0", paramLabel = "PDF_PATH", description = "简历 PDF 路径。")
    Path pdfPath;

    @Override
    public Integer call() {
        PdfService pdf = newPdfService();
        String text = pdf.extractText(pdfPath);
        // For "parse" we wrap the text in a JSON object so that --output
        // always produces valid JSON.
        var payload = Map.of(
                "path", pdfPath.toString(),
                "length", text.length(),
                "text", text
        );
        String json = OutputWriter.toJson(payload);
        System.out.println(json);
        OutputWriter.writeIfRequested(output, payload);
        return 0;
    }
}
