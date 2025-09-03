package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.DonViTinhDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.service.DonViTinhService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/don-vi-tinh")
@RequiredArgsConstructor
public class DonViTinhController {

    private final DonViTinhService donViTinhService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<DonViTinhDTO>>> getAllDonViTinh(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "tenDvt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DonViTinhDTO> response = donViTinhService.getAllDonViTinh(search, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<DonViTinhDTO>>> getAllDonViTinhList() {
        List<DonViTinhDTO> response = donViTinhService.getAllDonViTinh();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<DonViTinhDTO>> getDonViTinhById(@PathVariable Long id) {
        return donViTinhService.getDonViTinhById(id)
                .map(dvt -> ResponseEntity.ok(ApiResponse.success(dvt)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<DonViTinhDTO>> createDonViTinh(@Valid @RequestBody DonViTinhDTO dto) {
        try {
            DonViTinhDTO created = donViTinhService.createDonViTinh(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo đơn vị tính thành công", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<DonViTinhDTO>> updateDonViTinh(
            @PathVariable Long id, @Valid @RequestBody DonViTinhDTO dto) {
        try {
            DonViTinhDTO updated = donViTinhService.updateDonViTinh(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn vị tính thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDonViTinh(@PathVariable Long id) {
        try {
            donViTinhService.deleteDonViTinh(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa đơn vị tính thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}