package com.aiparse.cli;

import com.aiparse.cli.command.ExtractCommand;
import com.aiparse.cli.command.ParseCommand;
import com.aiparse.cli.command.ScoreCommand;
import com.aiparse.cli.exception.CliException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "resume-cli",
        mixinStandardHelpOptions = true,
        version = "resume-cli 0.1.0",
        description = "AI 简历解析 CLI：解析 PDF、提取结构化信息、与 JD 匹配评分。",
        subcommands = {
                ParseCommand.class,
                ExtractCommand.class,
                ScoreCommand.class
        }
)
public class Main implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "Enable DEBUG logging.")
    boolean verbose;

    public static void main(String[] args) {
        int exit = run(args);
        System.exit(exit);
    }

    public static int run(String[] args) {
        Main main = new Main();
        CommandLine cmd = new CommandLine(main);
        cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
            if (ex instanceof CliException cli) {
                commandLine.getErr().println("Error: " + cli.getMessage());
                return cli.getExitCode();
            }
            commandLine.getErr().println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace(commandLine.getErr());
            return 99;
        });
        cmd.setParameterExceptionHandler((ex, args1) -> {
            ex.getCommandLine().getErr().println("Error: " + ex.getMessage());
            ex.getCommandLine().getErr().println();
            ex.getCommandLine().usage(ex.getCommandLine().getErr());
            return ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
        });
        int exit = cmd.execute(args);
        return exit;
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public boolean isVerbose() {
        return verbose;
    }
}
