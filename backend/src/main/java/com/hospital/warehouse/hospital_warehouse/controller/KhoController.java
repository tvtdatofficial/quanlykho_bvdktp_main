package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.KhoDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.Kho;
import com.hospital.warehouse.hospital_warehouse.service.KhoService;
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
@RequestMapping("/api/kho")
@RequiredArgsConstructor
public class KhoController {

    private final KhoService khoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<KhoDTO>>> getAllKho(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Kho.LoaiKho loaiKho,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "tenKho") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<KhoDTO> response = khoService.getAllKho(search, loaiKho, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<KhoDTO>>> getKhoActive() {
        List<KhoDTO> response = khoService.getKhoActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<KhoDTO>> getKhoById(@PathVariable Long id) {
        return khoService.getKhoById(id)
                .map(kho -> ResponseEntity.ok(ApiResponse.success(kho)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<KhoDTO>> createKho(@Valid @RequestBody KhoDTO dto) {
        try {
            KhoDTO created = khoService.createKho(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo kho thành công", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<KhoDTO>> updateKho(
            @PathVariable Long id, @Valid @RequestBody KhoDTO dto) {
        try {
            KhoDTO updated = khoService.updateKho(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật kho thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteKho(@PathVariable Long id) {
        try {
            khoService.deleteKho(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa kho thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/loai/{loaiKho}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<KhoDTO>>> getKhoByLoaiKho(@PathVariable Kho.LoaiKho loaiKho) {
        List<KhoDTO> response = khoService.getKhoByLoaiKho(loaiKho);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/khoa-phong/{khoaPhongId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<KhoDTO>>> getKhoByKhoaPhong(@PathVariable Long khoaPhongId) {
        List<KhoDTO> response = khoService.getKhoByKhoaPhong(khoaPhongId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ma/{maKho}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<KhoDTO>> getKhoByMa(@PathVariable String maKho) {
        return khoService.getKhoByMa(maKho)
                .map(kho -> ResponseEntity.ok(ApiResponse.success(kho)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<Void>> activateKho(@PathVariable Long id) {
        try {
            khoService.activateKho(id);
            return ResponseEntity.ok(ApiResponse.success("Kích hoạt kho thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<Long>> countKhoHoatDong() {
        long count = khoService.countKhoByTrangThai(Kho.TrangThaiKho.HOAT_DONG);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}