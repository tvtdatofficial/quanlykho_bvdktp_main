package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.ViTriKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.ViTriKho;
import com.hospital.warehouse.hospital_warehouse.service.ViTriKhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PageResponse<ViTriKhoDTO>> getAllViTriKho(
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

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy cây vị trí kho theo kho
     */
    @GetMapping("/tree/{khoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<List<ViTriKhoDTO>> getViTriKhoTree(@PathVariable Long khoId) {
        List<ViTriKhoDTO> tree = viTriKhoService.getViTriKhoTree(khoId);
        return ResponseEntity.ok(tree);
    }

    /**
     * Lấy danh sách vị trí trống
     */
    @GetMapping("/trong")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<List<ViTriKhoDTO>> getViTriTrong(
            @RequestParam Long khoId) {
        List<ViTriKhoDTO> list = viTriKhoService.getViTriTrong(khoId);
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy chi tiết vị trí kho
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ViTriKhoDTO> getViTriKhoById(@PathVariable Long id) {
        return viTriKhoService.getViTriKhoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo vị trí kho mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ViTriKhoDTO> createViTriKho(
            @Valid @RequestBody ViTriKhoDTO dto) {
        ViTriKhoDTO created = viTriKhoService.createViTriKho(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật vị trí kho
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ViTriKhoDTO> updateViTriKho(
            @PathVariable Long id,
            @Valid @RequestBody ViTriKhoDTO dto) {
        ViTriKhoDTO updated = viTriKhoService.updateViTriKho(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa vị trí kho
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteViTriKho(@PathVariable Long id) {
        viTriKhoService.deleteViTriKho(id);
        return ResponseEntity.noContent().build();
    }
}