package com.hospital.warehouse.hospital_warehouse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadDir);
            log.info("Upload directory created at: {}", this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, String folder) {
        // Validate
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được rỗng");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Check file extension
        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh: jpg, jpeg, png, gif");
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "." + extension;

        try {
            // Create folder if not exists
            Path folderPath = this.uploadDir.resolve(folder);
            Files.createDirectories(folderPath);

            // Store file
            Path targetLocation = folderPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {}", targetLocation);

            // Return relative path
            return folder + "/" + filename;

        } catch (IOException e) {
            log.error("Could not store file {}", filename, e);
            throw new RuntimeException("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;

        try {
            Path path = this.uploadDir.resolve(filePath).normalize();
            Files.deleteIfExists(path);
            log.info("Deleted file: {}", path);
        } catch (IOException e) {
            log.error("Could not delete file {}", filePath, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private boolean isValidImageExtension(String extension) {
        return extension.matches("jpg|jpeg|png|gif|webp");
    }
}