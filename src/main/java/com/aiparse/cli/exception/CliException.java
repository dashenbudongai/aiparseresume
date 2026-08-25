package com.aiparse.cli.exception;

/**
 * Base exception thrown by the CLI tool. Carries an exit code so the
 * top-level handler can translate errors into meaningful shell return values.
 */
public class CliException extends RuntimeException {
    private final int exitCode;

    public CliException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public CliException(int exitCode, String message, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
