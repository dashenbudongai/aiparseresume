package com.aiparse.cli.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EnvConfigTest {

    @Test
    void parsesSimpleKeyValue() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, "FOO=bar\nBAZ=qux\n");
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("bar", env.get("FOO"));
            assertEquals("qux", env.get("BAZ"));
            assertNull(env.get("MISSING"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void ignoresBlankAndCommentLines() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, """
                # this is a comment
                FOO=bar

                # another comment
                BAZ=qux
                """);
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("bar", env.get("FOO"));
            assertEquals("qux", env.get("BAZ"));
            assertFalse(env.isEmpty());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void stripsInlineComments() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, "FOO=bar # trailing comment\nBAZ=qux #another\n");
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("bar", env.get("FOO"));
            assertEquals("qux", env.get("BAZ"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void stripsExportPrefix() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, "export FOO=bar\n");
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("bar", env.get("FOO"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void unquotesValues() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, "DOUBLE=\"hello world\"\nSINGLE='single quoted'\nPLAIN=plain\n");
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("hello world", env.get("DOUBLE"));
            assertEquals("single quoted", env.get("SINGLE"));
            assertEquals("plain", env.get("PLAIN"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void missingFileIsEmpty() {
        Path missing = Path.of("does-not-exist-" + System.nanoTime() + ".env");
        EnvConfig env = EnvConfig.load(missing);
        assertTrue(env.isEmpty());
        assertNull(env.get("ANYTHING"));
    }

    @Test
    void getOrDefault() throws IOException {
        Path tmp = Files.createTempFile("env", ".env");
        Files.writeString(tmp, "FOO=bar\n");
        try {
            EnvConfig env = EnvConfig.load(tmp);
            assertEquals("bar", env.getOrDefault("FOO", "fallback"));
            assertEquals("fallback", env.getOrDefault("MISSING", "fallback"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
