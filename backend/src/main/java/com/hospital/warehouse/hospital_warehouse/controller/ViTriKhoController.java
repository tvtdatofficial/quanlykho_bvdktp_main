package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.ViTriKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.ViTriKho;
import com.hospital.warehouse.hospital_warehouse.service.ViTriKhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vi-tri-kho")
@RequiredArgsConstructor
public class ViTriKhoController {

    private final ViTriKhoService viTriKhoService;

    /**
     * Lấy danh sách vị trí kho có phân trang và lọc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<ViTriKhoDTO>>> getAllViTriKho(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long khoId,
            @RequestParam(required = false) ViTriKho.LoaiViTri loaiViTri,
            @RequestParam(required = false) ViTriKho.TrangThaiViTri trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<ViTriKhoDTO> response = viTriKhoService.getAllViTriKho(
                search, khoId, loaiViTri, trangThai, pageable);

        // ✅ THÊM LOG ĐỂ DEBUG
        log.info("📦 Returning {} vi tri kho for kho ID: {}",
                response.getContent().size(), khoId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy cây vị trí kho theo kho
     */
    @GetMapping("/tree/{khoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<ViTriKhoDTO>>> getViTriKhoTree(
            @PathVariable Long khoId) {
        List<ViTriKhoDTO> tree = viTriKhoService.getViTriKhoTree(khoId);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * Lấy danh sách vị trí trống
     * QUAN TRỌNG: Endpoint cụ thể /trong phải đặt TRƯỚC /{id}
     */
    @GetMapping("/trong")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<ViTriKhoDTO>>> getViTriTrong(
            @RequestParam Long khoId) {
        List<ViTriKhoDTO> list = viTriKhoService.getViTriTrongByKhoId(khoId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Lấy chi tiết vị trí kho
     * QUAN TRỌNG: Endpoint động /{id} phải đặt SAU các endpoint cụ thể
     */
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<ViTriKhoDTO>> getViTriKhoById(
            @PathVariable Long id) {
        return viTriKhoService.getViTriKhoById(id)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo vị trí kho mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<ViTriKhoDTO>> createViTriKho(
            @Valid @RequestBody ViTriKhoDTO dto) {
        try {
            ViTriKhoDTO created = viTriKhoService.createViTriKho(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo vị trí kho thành công", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật vị trí kho
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<ViTriKhoDTO>> updateViTriKho(
            @PathVariable Long id,
            @Valid @RequestBody ViTriKhoDTO dto) {
        try {
            ViTriKhoDTO updated = viTriKhoService.updateViTriKho(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật vị trí kho thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa vị trí kho
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteViTriKho(@PathVariable Long id) {
        try {
            viTriKhoService.deleteViTriKho(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa vị trí kho thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}