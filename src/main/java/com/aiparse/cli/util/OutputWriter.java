package com.aiparse.cli.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OutputWriter {
    private static final ObjectMapper PRETTY = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private OutputWriter() {}

    public static String toJson(Object value) {
        try {
            return PRETTY.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    public static void writeIfRequested(String output, Object value) {
        if (output == null || output.isBlank()) return;
        Path p = Path.of(output);
        try {
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.writeString(p, toJson(value));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output file: " + e.getMessage(), e);
        }
    }
}
