package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    @PostMapping("/upload/hang-hoa")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadHangHoaImage(
            @RequestParam("file") MultipartFile file) {

        try {
            String filePath = fileStorageService.storeFile(file, "hang-hoa");
            String fileUrl = "/api/files/view/" + filePath;

            Map<String, String> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(ApiResponse.success("Upload thành công", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi upload file"));
        }
    }

    @GetMapping("/view/{folder}/{filename:.+}")
    public ResponseEntity<Resource> viewFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        try {
            Path filePath = uploadDir.resolve(folder).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error retrieving file", e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam String filePath) {

        try {
            fileStorageService.deleteFile(filePath);
            return ResponseEntity.ok(ApiResponse.success("Xóa file thành công", null));
        } catch (Exception e) {
            log.error("Error deleting file", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa file"));
        }
    }
}