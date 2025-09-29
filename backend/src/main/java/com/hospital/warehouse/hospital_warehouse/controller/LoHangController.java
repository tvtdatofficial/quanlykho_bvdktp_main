package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.LoHangDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.LoHang;
import com.hospital.warehouse.hospital_warehouse.service.LoHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lo-hang")
@RequiredArgsConstructor
public class LoHangController {

    private final LoHangService loHangService;

    /**
     * Lấy danh sách lô hàng có phân trang và lọc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<PageResponse<LoHangDTO>> getAllLoHang(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long hangHoaId,
            @RequestParam(required = false) Long nhaCungCapId,
            @RequestParam(required = false) LoHang.TrangThaiLoHang trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Boolean sapHetHan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "hanSuDung") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<LoHangDTO> response = loHangService.getAllLoHang(
                search, hangHoaId, nhaCungCapId, trangThai, tuNgay, denNgay, sapHetHan, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách lô hàng theo hàng hóa
     */
    @GetMapping("/hang-hoa/{hangHoaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<List<LoHangDTO>> getLoHangByHangHoa(@PathVariable Long hangHoaId) {
        List<LoHangDTO> list = loHangService.getLoHangByHangHoa(hangHoaId);
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy danh sách lô sắp hết hạn
     */
    @GetMapping("/sap-het-han")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<List<LoHangDTO>> getLoHangSapHetHan(
            @RequestParam(defaultValue = "30") int soNgay) {
        List<LoHangDTO> list = loHangService.getLoHangSapHetHan(soNgay);
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy danh sách lô đã hết hàng
     */
    @GetMapping("/het-hang")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<List<LoHangDTO>> getLoHangHetHang() {
        List<LoHangDTO> list = loHangService.getLoHangHetHang();
        return ResponseEntity.ok(list);
    }

    /**
     * Lấy chi tiết lô hàng
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<LoHangDTO> getLoHangById(@PathVariable Long id) {
        return loHangService.getLoHangById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Tạo lô hàng mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO', 'NHAN_VIEN_KHO')")
    public ResponseEntity<LoHangDTO> createLoHang(@Valid @RequestBody LoHangDTO dto) {
        LoHangDTO created = loHangService.createLoHang(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật lô hàng
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<LoHangDTO> updateLoHang(
            @PathVariable Long id,
            @Valid @RequestBody LoHangDTO dto) {
        LoHangDTO updated = loHangService.updateLoHang(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa lô hàng
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLoHang(@PathVariable Long id) {
        loHangService.deleteLoHang(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cập nhật trạng thái tất cả lô hàng (chạy định kỳ hoặc thủ công)
     */
    @PostMapping("/update-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateAllLoHangStatus() {
        loHangService.updateAllLoHangStatus();
        return ResponseEntity.ok().build();
    }
}