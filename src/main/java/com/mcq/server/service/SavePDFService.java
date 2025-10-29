package com.mcq.server.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SavePDFService {

    private final String uploadDir = System.getProperty("user.home") + "/com.mcq.server/tests/pdf/";

    public String savePDF(MultipartFile file) throws IOException {
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

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        return uploadDir + fileName;
    }
}