package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.KhoaPhongDTO;
import com.hospital.warehouse.hospital_warehouse.service.KhoaPhongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/khoa-phong")
@RequiredArgsConstructor
public class KhoaPhongController {

    private final KhoaPhongService khoaPhongService;

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<KhoaPhongDTO>>> getKhoaPhongActive() {
        List<KhoaPhongDTO> response = khoaPhongService.getKhoaPhongActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<KhoaPhongDTO>>> getAllKhoaPhong() {
        List<KhoaPhongDTO> response = khoaPhongService.getAllKhoaPhong();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}