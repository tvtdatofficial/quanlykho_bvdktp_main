package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PhieuXuatKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.PhieuXuatKho;
import com.hospital.warehouse.hospital_warehouse.service.PhieuXuatKhoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/phieu-xuat")
@RequiredArgsConstructor
public class PhieuXuatKhoController {

    private final PhieuXuatKhoService phieuXuatKhoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<PhieuXuatKhoDTO>>> getAllPhieuXuat(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long khoId,
            @RequestParam(required = false) Long khoaPhongId,
            @RequestParam(required = false) PhieuXuatKho.TrangThaiPhieuXuat trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            // ✅ SỬA: Đổi mặc định từ 'ngayXuat' → 'createdAt'
            @RequestParam(defaultValue = "createdAt") String sortBy,    // ← ĐỔI DÒNG NÀY
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<PhieuXuatKhoDTO> response = phieuXuatKhoService.getAllPhieuXuat(
                search, khoId, khoaPhongId, trangThai, tuNgay, denNgay, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> getPhieuXuatById(@PathVariable Long id) {
        return phieuXuatKhoService.getPhieuXuatById(id)
                .map(phieu -> ResponseEntity.ok(ApiResponse.success(phieu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma/{maPhieuXuat}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> getPhieuXuatByMa(@PathVariable String maPhieuXuat) {
        return phieuXuatKhoService.getPhieuXuatByMa(maPhieuXuat)
                .map(phieu -> ResponseEntity.ok(ApiResponse.success(phieu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> createPhieuXuat(
            @Valid @RequestBody PhieuXuatKhoDTO dto) {
        try {
            PhieuXuatKhoDTO created = phieuXuatKhoService.createPhieuXuat(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo phiếu xuất thành công", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> updatePhieuXuat(
            @PathVariable Long id,
            @Valid @RequestBody PhieuXuatKhoDTO dto) {
        try {
            PhieuXuatKhoDTO updated = phieuXuatKhoService.updatePhieuXuat(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu xuất thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/duyet")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> duyetPhieuXuat(@PathVariable Long id) {
        try {
            PhieuXuatKhoDTO approved = phieuXuatKhoService.duyetPhieuXuat(id);
            return ResponseEntity.ok(ApiResponse.success("Duyệt phiếu xuất thành công", approved));
        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy phiếu xuất ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy phiếu xuất"));
        } catch (IllegalStateException e) {
            log.error("Lỗi nghiệp vụ khi duyệt phiếu xuất ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Lỗi không xác định khi duyệt phiếu xuất ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/huy")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> huyPhieuXuat(
            @PathVariable Long id,
            @RequestParam String lyDoHuy) {
        try {
            PhieuXuatKhoDTO cancelled = phieuXuatKhoService.huyPhieuXuat(id, lyDoHuy);
            return ResponseEntity.ok(ApiResponse.success("Hủy phiếu xuất thành công", cancelled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    /**
     * ✅ BỔ SUNG: Hủy duyệt phiếu xuất (chỉ ADMIN)
     */
    @PatchMapping("/{id}/huy-duyet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO>> huyDuyetPhieuXuat(
            @PathVariable Long id,
            @RequestParam String lyDoHuyDuyet) {
        try {
            PhieuXuatKhoDTO result = phieuXuatKhoService.huyDuyetPhieuXuat(id, lyDoHuyDuyet);
            return ResponseEntity.ok(ApiResponse.success(
                    "Hủy duyệt phiếu xuất thành công. Tồn kho đã được hoàn nguyên.",
                    result
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error rolling back phieu xuat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePhieuXuat(@PathVariable Long id) {
        try {
            phieuXuatKhoService.deletePhieuXuat(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa phiếu xuất thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/thong-ke")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<PhieuXuatKhoDTO.ThongKePhieuXuat>> getThongKePhieuXuat(
            @RequestParam(required = false) Long khoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        PhieuXuatKhoDTO.ThongKePhieuXuat thongKe =
                phieuXuatKhoService.getThongKePhieuXuat(khoId, tuNgay, denNgay);
        return ResponseEntity.ok(ApiResponse.success(thongKe));
    }

    @GetMapping("/cho-duyet")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<PhieuXuatKhoDTO>>> getPhieuXuatChoDuyet() {
        List<PhieuXuatKhoDTO> list = phieuXuatKhoService.getPhieuXuatChoDuyet();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}