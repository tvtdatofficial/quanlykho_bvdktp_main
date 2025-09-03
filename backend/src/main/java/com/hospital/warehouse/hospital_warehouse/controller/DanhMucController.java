package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.DanhMucDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.DanhMuc;
import com.hospital.warehouse.hospital_warehouse.service.DanhMucService;
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
@RequestMapping("/api/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<DanhMucDTO>>> getAllDanhMuc(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) DanhMuc.LoaiDanhMuc loaiDanhMuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "thuTuSapXep") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<DanhMucDTO> response = danhMucService.getAllDanhMuc(search, loaiDanhMuc, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<DanhMucDTO.DanhMucTreeDTO>>> getDanhMucTree() {
        List<DanhMucDTO.DanhMucTreeDTO> response = danhMucService.getDanhMucTree();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/root")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<DanhMucDTO>>> getRootCategories() {
        List<DanhMucDTO> response = danhMucService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{danhMucChaId}/children")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<DanhMucDTO>>> getChildCategories(@PathVariable Long danhMucChaId) {
        List<DanhMucDTO> response = danhMucService.getChildCategories(danhMucChaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<DanhMucDTO>> getDanhMucById(@PathVariable Long id) {
        return danhMucService.getDanhMucById(id)
                .map(dm -> ResponseEntity.ok(ApiResponse.success(dm)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<DanhMucDTO>> createDanhMuc(@Valid @RequestBody DanhMucDTO dto) {
        try {
            DanhMucDTO created = danhMucService.createDanhMuc(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo danh mục thành công", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<DanhMucDTO>> updateDanhMuc(
            @PathVariable Long id, @Valid @RequestBody DanhMucDTO dto) {
        try {
            DanhMucDTO updated = danhMucService.updateDanhMuc(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDanhMuc(@PathVariable Long id) {
        try {
            danhMucService.deleteDanhMuc(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}