package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.CanhBaoDTO;
import com.hospital.warehouse.hospital_warehouse.dto.ThongKeCanhBaoDTO;
import com.hospital.warehouse.hospital_warehouse.dto.XuLyCanhBaoRequest;
import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong;
import com.hospital.warehouse.hospital_warehouse.service.CanhBaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/canh-bao")
@Slf4j
@CrossOrigin(origins = "*")
public class CanhBaoController {

    @Autowired
    private CanhBaoService canhBaoService;

    /**
     * GET /api/canh-bao - Lấy tất cả cảnh báo
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CanhBaoDTO>>> getAllCanhBao(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String loai,
            @RequestParam(required = false) String mucDo,
            @RequestParam(required = false) Boolean chuaXuLy,
            @RequestParam(required = false) Boolean chuaDoc
    ) {
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<CanhBaoDTO> canhBaoPage;

            // Filter logic
            if (chuaXuLy != null && chuaXuLy) {
                canhBaoPage = canhBaoService.getCanhBaoChuaXuLy(pageable);
            } else if (chuaDoc != null && chuaDoc) {
                canhBaoPage = canhBaoService.getCanhBaoChuaDoc(pageable);
            } else if (loai != null) {
                CanhBaoHeThong.LoaiCanhBao loaiCanhBao = CanhBaoHeThong.LoaiCanhBao.valueOf(loai);
                canhBaoPage = canhBaoService.getCanhBaoTheoLoai(loaiCanhBao, pageable);
            } else if (mucDo != null) {
                CanhBaoHeThong.MucDo mucDoCanhBao = CanhBaoHeThong.MucDo.valueOf(mucDo);
                canhBaoPage = canhBaoService.getCanhBaoTheoMucDo(mucDoCanhBao, pageable);
            } else {
                canhBaoPage = canhBaoService.getAllCanhBao(pageable);
            }

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cảnh báo thành công", canhBaoPage));

        } catch (Exception e) {
            log.error("❌ Error getting canh bao list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * GET /api/canh-bao/{id} - Lấy chi tiết cảnh báo
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CanhBaoDTO>> getCanhBaoById(@PathVariable Long id) {
        try {
            CanhBaoDTO canhBao = canhBaoService.getCanhBaoById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cảnh báo thành công", canhBao));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error getting canh bao detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/canh-bao/{id}/doc - Đánh dấu đã đọc
     */
    @PatchMapping("/{id}/doc")
    public ResponseEntity<ApiResponse<CanhBaoDTO>> danhDauDaDoc(@PathVariable Long id) {
        try {
            CanhBaoDTO canhBao = canhBaoService.danhDauDaDoc(id);
            return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc cảnh báo", canhBao));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error marking canh bao as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/canh-bao/doc-tat-ca - Đánh dấu tất cả đã đọc
     */
    @PatchMapping("/doc-tat-ca")
    public ResponseEntity<ApiResponse<String>> danhDauTatCaDaDoc() {
        try {
            canhBaoService.danhDauTatCaDaDoc();
            return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc tất cả cảnh báo", "SUCCESS"));
        } catch (Exception e) {
            log.error("❌ Error marking all canh bao as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/canh-bao/{id}/xu-ly - Xử lý cảnh báo
     */
    @PatchMapping("/{id}/xu-ly")
    public ResponseEntity<ApiResponse<CanhBaoDTO>> xuLyCanhBao(
            @PathVariable Long id,
            @RequestBody XuLyCanhBaoRequest request,
            Authentication authentication
    ) {
        try {
            // Get current user ID from authentication
            Long userId = 1L; // TODO: Get from authentication

            CanhBaoDTO canhBao = canhBaoService.xuLyCanhBao(id, userId, request.getGhiChu());
            return ResponseEntity.ok(ApiResponse.success("Đã xử lý cảnh báo thành công", canhBao));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error handling canh bao", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/canh-bao/{id} - Xóa cảnh báo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> xoaCanhBao(@PathVariable Long id) {
        try {
            canhBaoService.xoaCanhBao(id);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa cảnh báo", "SUCCESS"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error deleting canh bao", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * GET /api/canh-bao/thong-ke - Thống kê cảnh báo
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<ApiResponse<ThongKeCanhBaoDTO>> thongKeCanhBao() {
        try {
            ThongKeCanhBaoDTO thongKe = canhBaoService.thongKeCanhBao();
            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thành công", thongKe));
        } catch (Exception e) {
            log.error("❌ Error getting canh bao statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}