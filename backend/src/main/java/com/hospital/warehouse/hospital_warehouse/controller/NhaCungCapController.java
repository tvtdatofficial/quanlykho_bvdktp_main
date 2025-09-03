package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.NhaCungCapDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.NhaCungCap;
import com.hospital.warehouse.hospital_warehouse.service.NhaCungCapService;
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
@RequestMapping("/api/nha-cung-cap")
@RequiredArgsConstructor
public class NhaCungCapController {

    private final NhaCungCapService nhaCungCapService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<NhaCungCapDTO>>> getAllNhaCungCap(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) NhaCungCap.TrangThaiNcc trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "tenNcc") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<NhaCungCapDTO> response = nhaCungCapService.getAllNhaCungCap(search, trangThai, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<NhaCungCapDTO>>> getNhaCungCapActive() {
        List<NhaCungCapDTO> response = nhaCungCapService.getNhaCungCapActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<NhaCungCapDTO>> getNhaCungCapById(@PathVariable Long id) {
        return nhaCungCapService.getNhaCungCapById(id)
                .map(ncc -> ResponseEntity.ok(ApiResponse.success(ncc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<NhaCungCapDTO>> createNhaCungCap(@Valid @RequestBody NhaCungCapDTO dto) {
        try {
            NhaCungCapDTO created = nhaCungCapService.createNhaCungCap(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo nhà cung cấp thành công", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<NhaCungCapDTO>> updateNhaCungCap(
            @PathVariable Long id, @Valid @RequestBody NhaCungCapDTO dto) {
        try {
            NhaCungCapDTO updated = nhaCungCapService.updateNhaCungCap(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà cung cấp thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNhaCungCap(@PathVariable Long id) {
        try {
            nhaCungCapService.deleteNhaCungCap(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa nhà cung cấp thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
