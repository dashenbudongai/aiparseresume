package com.aiparse.cli.service;

import com.aiparse.cli.exception.CliException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads text content from local PDF files using Apache PDFBox.
 */
public class PdfService {
    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    private static final int MAX_TEXT_CHARS = 16_000;

    /**
     * Extract plain text from the given PDF file path. Performs basic
     * validation (file exists, is a PDF, can be parsed, has text).
     */
    public String extractText(Path pdfPath) {
        if (pdfPath == null) {
            throw new CliException("PDF path is null", 2);
        }

        File file = pdfPath.toFile();
        if (!file.exists()) {
            throw new CliException("File not found: " + pdfPath, 2);
        }
        if (!file.isFile()) {
            throw new CliException("Path is not a regular file: " + pdfPath, 2);
        }
        if (!file.canRead()) {
            throw new CliException("File is not readable: " + pdfPath, 2);
        }
        if (!isPdf(file)) {
            throw new CliException("File is not a PDF: " + pdfPath, 2);
        }

        log.info("Extracting text from {}", pdfPath);
        try (PDDocument document = Loader.loadPDF(file)) {
            if (document.isEncrypted()) {
                throw new CliException("PDF is encrypted and cannot be read: " + pdfPath, 3);
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new CliException("PDF text is empty: " + pdfPath, 3);
            }
            if (text.length() > MAX_TEXT_CHARS) {
                log.warn("PDF text length {} exceeds max {}, truncating.", text.length(), MAX_TEXT_CHARS);
                text = text.substring(0, MAX_TEXT_CHARS);
            }
            return text;
        } catch (IOException e) {
            log.error("Failed to read PDF {}", pdfPath, e);
            throw new CliException(3, "Failed to read PDF: " + e.getMessage(), e);
        }
    }

    private boolean isPdf(File file) {
        try {
            String header = new String(Files.readAllBytes(file.toPath()).length > 0
                    ? readHeader(file)
                    : new byte[0]);
            return header.startsWith("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }

    private byte[] readHeader(File file) throws IOException {
        try (var in = Files.newInputStream(file.toPath())) {
            byte[] buf = new byte[5];
            int read = 0;
            while (read < buf.length) {
                int n = in.read(buf, read, buf.length - read);
                if (n < 0) break;
                read += n;
            }
            if (read < buf.length) {
                byte[] out = new byte[read];
                System.arraycopy(buf, 0, out, 0, read);
                return out;
            }
            return buf;
        }
    }
}
