package com.mcq.server.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SavePDFService {

    private final String uploadDir = System.getProperty("user.home") + "/com.mcq.server/tests/pdf/";

    // A simple record to return two values
    public record PdfInfo(String path, int pageCount) {}

    public PdfInfo savePDF(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // --- NEW: Load PDF to count pages ---
        int pageCount;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            pageCount = document.getNumberOfPages();
        } catch (IOException e) {
            throw new IOException("Failed to read PDF file to count pages.", e);
        }
        // --- END NEW ---

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        return new PdfInfo(uploadDir + fileName, pageCount);
    }
}