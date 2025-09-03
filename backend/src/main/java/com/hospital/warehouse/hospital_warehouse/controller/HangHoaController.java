package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.HangHoaDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.HangHoa;
import com.hospital.warehouse.hospital_warehouse.service.HangHoaService;
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
@RequestMapping("/api/hang-hoa")
@RequiredArgsConstructor
public class HangHoaController {

    private final HangHoaService hangHoaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<PageResponse<HangHoaDTO>>> getAllHangHoa(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long danhMucId,
            @RequestParam(required = false) HangHoa.TrangThaiHangHoa trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "tenHangHoa") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        HangHoa.TrangThaiHangHoa status = trangThai != null ? trangThai : HangHoa.TrangThaiHangHoa.HOAT_DONG;

        PageResponse<HangHoaDTO> response = hangHoaService.getAllHangHoa(search, danhMucId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ton-kho-thap")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<HangHoaDTO>>> getHangHoaTonKhoThap() {
        List<HangHoaDTO> response = hangHoaService.getHangHoaTonKhoThap();
        return ResponseEntity.ok(ApiResponse.success("Danh sách hàng hóa tồn kho thấp", response));
    }

    @GetMapping("/het-hang")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<HangHoaDTO>>> getHangHoaHetHang() {
        List<HangHoaDTO> response = hangHoaService.getHangHoaHetHang();
        return ResponseEntity.ok(ApiResponse.success("Danh sách hàng hóa hết hàng", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<HangHoaDTO>> getHangHoaById(@PathVariable Long id) {
        return hangHoaService.getHangHoaById(id)
                .map(hh -> ResponseEntity.ok(ApiResponse.success(hh)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma/{maHangHoa}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<HangHoaDTO>> getHangHoaByMa(@PathVariable String maHangHoa) {
        return hangHoaService.getHangHoaByMa(maHangHoa)
                .map(hh -> ResponseEntity.ok(ApiResponse.success(hh)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<HangHoaDTO>> createHangHoa(@Valid @RequestBody HangHoaDTO dto) {
        try {
            HangHoaDTO created = hangHoaService.createHangHoa(dto);
            return ResponseEntity.ok(ApiResponse.success("Tạo hàng hóa thành công", created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<HangHoaDTO>> updateHangHoa(
            @PathVariable Long id, @Valid @RequestBody HangHoaDTO dto) {
        try {
            HangHoaDTO updated = hangHoaService.updateHangHoa(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật hàng hóa thành công", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHangHoa(@PathVariable Long id) {
        try {
            hangHoaService.deleteHangHoa(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa hàng hóa thành công", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}