package com.aiparse.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainCliTest {

    @Test
    void helpExitsZero() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream orig = System.out;
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        try {
            int code = Main.run(new String[]{"--help"});
            assertEquals(0, code);
        } finally {
            System.setOut(orig);
        }
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("resume-cli"));
    }

    @Test
    void missingFileReturnsNonZero() {
        int code = Main.run(new String[]{"parse", "definitely-missing.pdf"});
        assertTrue(code != 0, "expected non-zero exit for missing file");
    }
}
