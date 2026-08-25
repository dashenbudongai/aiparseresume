package com.aiparse.cli.command;

import com.aiparse.cli.config.EnvConfig;
import com.aiparse.cli.service.AiService;
import com.aiparse.cli.service.PdfService;
import com.aiparse.cli.service.ResumeExtractor;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * Shared option constants and helpers for the subcommands. Each
 * subcommand declares the same global options (--api-key, --mock, etc.)
 * so users can pass them after the subcommand name.
 *
 * <p>Resolution order for all env-style values:
 * <ol>
 *   <li>Explicit CLI flag (e.g. {@code --api-key}).</li>
 *   <li>System environment variable (e.g. {@code QWEN_API_KEY}).</li>
 *   <li>Project-local {@code .env} file (key=value per line).</li>
 *   <li>Built-in default.</li>
 * </ol>
 */
public abstract class BaseCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"--api-key"},
            description = "Qwen / DashScope API key. Falls back to env QWEN_API_KEY, then .env.")
    String apiKeyOpt;

    @CommandLine.Option(names = {"--base-url"},
            description = "Compatible base URL. Falls back to env QWEN_BASE_URL, then .env, then default.",
            defaultValue = "")
    String baseUrlOpt;

    @CommandLine.Option(names = {"--model"},
            description = "Model name. Falls back to env QWEN_MODEL, then .env, then default.",
            defaultValue = "")
    String modelOpt;

    @CommandLine.Option(names = {"--mock"},
            description = "Force offline mock AI (no network).")
    boolean mock;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Write JSON result to this file in addition to stdout.")
    String output;

    private static final EnvConfig ENV_FILE = EnvConfig.load();

    protected String resolveApiKey() {
        if (notBlank(apiKeyOpt)) return apiKeyOpt;
        String v = System.getenv("QWEN_API_KEY");
        if (notBlank(v)) return v;
        return ENV_FILE.get("QWEN_API_KEY");
    }

    protected String resolveBaseUrl() {
        if (notBlank(baseUrlOpt)) return baseUrlOpt;
        String v = System.getenv("QWEN_BASE_URL");
        if (notBlank(v)) return v;
        v = ENV_FILE.get("QWEN_BASE_URL");
        if (notBlank(v)) return v;
        return AiService.DEFAULT_BASE_URL;
    }

    protected String resolveModel() {
        if (notBlank(modelOpt)) return modelOpt;
        String v = System.getenv("QWEN_MODEL");
        if (notBlank(v)) return v;
        v = ENV_FILE.get("QWEN_MODEL");
        if (notBlank(v)) return v;
        return AiService.DEFAULT_MODEL;
    }

    protected AiService newAiService() {
        return new AiService(resolveApiKey(), resolveBaseUrl(), resolveModel(), mock);
    }

    protected ResumeExtractor newExtractor(AiService ai) {
        return new ResumeExtractor(ai);
    }

    protected PdfService newPdfService() {
        return new PdfService();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
