package com.aiparse.cli.service;

import com.aiparse.cli.exception.CliException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceTest {

    @Test
    void extractThrowsWhenFileMissing() {
        PdfService svc = new PdfService();
        CliException ex = assertThrows(CliException.class,
                () -> svc.extractText(Path.of("does-not-exist.pdf")));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void extractThrowsWhenNotPdf() throws Exception {
        Path tmp = Files.createTempFile("not-a-pdf", ".pdf");
        Files.writeString(tmp, "this is plain text, not a PDF");
        try {
            PdfService svc = new PdfService();
            CliException ex = assertThrows(CliException.class, () -> svc.extractText(tmp));
            assertTrue(ex.getMessage().contains("not a PDF"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void extractReturnsText() throws Exception {
        Path tmp = Files.createTempFile("good", ".pdf");
        writeSimplePdf(tmp, "Hello Resume World");
        try {
            PdfService svc = new PdfService();
            String text = svc.extractText(tmp);
            assertTrue(text.contains("Hello Resume World"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private void writeSimplePdf(Path target, String text) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(text);
                cs.endText();
            }
            doc.save(target.toFile());
        }
    }
}
