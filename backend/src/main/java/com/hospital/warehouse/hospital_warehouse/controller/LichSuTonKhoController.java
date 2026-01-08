package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.LichSuTonKhoDTO;
import com.hospital.warehouse.hospital_warehouse.service.LichSuTonKhoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lich-su-ton-kho")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LichSuTonKhoController {

    private final LichSuTonKhoService lichSuTonKhoService;

    // ✅ Lấy danh sách lịch sử với filter
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<Page<LichSuTonKhoDTO>>> getLichSuTonKho(
            @RequestParam(required = false) Long hangHoaId,
            @RequestParam(required = false) Long viTriKhoId,
            @RequestParam(required = false) String loaiBienDong,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime denNgay,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<LichSuTonKhoDTO> result = lichSuTonKhoService.getLichSuTonKho(
                    hangHoaId, viTriKhoId, loaiBienDong, tuNgay, denNgay, keyword, page, size
            );
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tồn kho thành công", result));
        } catch (Exception e) {
            log.error("Error getting lich su ton kho", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ✅ Lấy lịch sử theo hàng hóa
    @GetMapping("/hang-hoa/{hangHoaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<Page<LichSuTonKhoDTO>>> getLichSuByHangHoa(
            @PathVariable Long hangHoaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<LichSuTonKhoDTO> result = lichSuTonKhoService.getLichSuByHangHoa(hangHoaId, page, size);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử hàng hóa thành công", result));
        } catch (Exception e) {
            log.error("Error getting lich su by hang hoa", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ✅ Lấy lịch sử theo mã chứng từ
    @GetMapping("/chung-tu/{maChungTu}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<LichSuTonKhoDTO>>> getLichSuByMaChungTu(
            @PathVariable String maChungTu
    ) {
        try {
            List<LichSuTonKhoDTO> result = lichSuTonKhoService.getLichSuByMaChungTu(maChungTu);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử chứng từ thành công", result));
        } catch (Exception e) {
            log.error("Error getting lich su by chung tu", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    // ✅ Lấy dữ liệu cho chart
    @GetMapping("/chart/{hangHoaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<ApiResponse<List<LichSuTonKhoDTO>>> getChartData(
            @PathVariable Long hangHoaId,
            @RequestParam(defaultValue = "30") int days
    ) {
        try {
            List<LichSuTonKhoDTO> result = lichSuTonKhoService.getChartData(hangHoaId, days);
            return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu biểu đồ thành công", result));
        } catch (Exception e) {
            log.error("Error getting chart data", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}