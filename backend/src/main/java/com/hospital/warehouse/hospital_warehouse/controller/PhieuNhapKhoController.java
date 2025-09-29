package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PhieuNhapKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.PhieuNhapKho;
import com.hospital.warehouse.hospital_warehouse.service.PhieuNhapKhoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/phieu-nhap")
@RequiredArgsConstructor
public class PhieuNhapKhoController {

    private final PhieuNhapKhoService phieuNhapKhoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<PhieuNhapKhoDTO>>> getAllPhieuNhap(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long khoId,
            @RequestParam(required = false) Long nhaCungCapId,
            @RequestParam(required = false) PhieuNhapKho.TrangThaiPhieuNhap trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngayNhap") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<PhieuNhapKhoDTO> response = phieuNhapKhoService.getAllPhieuNhap(
                search, khoId, nhaCungCapId, trangThai, tuNgay, denNgay, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> getPhieuNhapById(@PathVariable Long id) {
        return phieuNhapKhoService.getPhieuNhapById(id)
                .map(phieu -> ResponseEntity.ok(ApiResponse.success(phieu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma/{maPhieuNhap}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> getPhieuNhapByMa(@PathVariable String maPhieuNhap) {
        return phieuNhapKhoService.getPhieuNhapByMa(maPhieuNhap)
                .map(phieu -> ResponseEntity.ok(ApiResponse.success(phieu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> createPhieuNhap(
            @Valid @RequestBody PhieuNhapKhoDTO dto) {
        try {
            PhieuNhapKhoDTO created = phieuNhapKhoService.createPhieuNhap(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo phiếu nhập thành công", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> updatePhieuNhap(
            @PathVariable Long id,
            @Valid @RequestBody PhieuNhapKhoDTO dto) {
        try {
            PhieuNhapKhoDTO updated = phieuNhapKhoService.updatePhieuNhap(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu nhập thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> duyetPhieuNhap(@PathVariable Long id) {
        try {
            PhieuNhapKhoDTO approved = phieuNhapKhoService.duyetPhieuNhap(id);
            return ResponseEntity.ok(ApiResponse.success("Duyệt phiếu nhập thành công", approved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/huy")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO>> huyPhieuNhap(
            @PathVariable Long id,
            @RequestParam String lyDoHuy) {
        try {
            PhieuNhapKhoDTO cancelled = phieuNhapKhoService.huyPhieuNhap(id, lyDoHuy);
            return ResponseEntity.ok(ApiResponse.success("Hủy phiếu nhập thành công", cancelled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePhieuNhap(@PathVariable Long id) {
        try {
            phieuNhapKhoService.deletePhieuNhap(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa phiếu nhập thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/thong-ke")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuNhapKhoDTO.ThongKePhieuNhap>> getThongKePhieuNhap(
            @RequestParam(required = false) Long khoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        PhieuNhapKhoDTO.ThongKePhieuNhap thongKe =
                phieuNhapKhoService.getThongKePhieuNhap(khoId, tuNgay, denNgay);
        return ResponseEntity.ok(ApiResponse.success(thongKe));
    }

    @GetMapping("/cho-duyet")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<PhieuNhapKhoDTO>>> getPhieuNhapChoDuyet() {
        List<PhieuNhapKhoDTO> list = phieuNhapKhoService.getPhieuNhapChoDuyet();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/kho/{khoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<PhieuNhapKhoDTO>>> getPhieuNhapByKho(
            @PathVariable Long khoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        List<PhieuNhapKhoDTO> list = phieuNhapKhoService.getPhieuNhapByKho(khoId, tuNgay, denNgay);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}