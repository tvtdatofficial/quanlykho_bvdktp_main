package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.LichSuTonKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.*;
import com.hospital.warehouse.hospital_warehouse.repository.LichSuTonKhoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LichSuTonKhoService {

    private final LichSuTonKhoRepository lichSuTonKhoRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ✅ Lấy danh sách lịch sử với filter
    public Page<LichSuTonKhoDTO> getLichSuTonKho(
            Long hangHoaId,
            Long viTriKhoId,
            String loaiBienDong,
            LocalDateTime tuNgay,
            LocalDateTime denNgay,
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        LichSuTonKho.LoaiBienDong enumLoaiBienDong = null;
        if (loaiBienDong != null && !loaiBienDong.isEmpty()) {
            try {
                enumLoaiBienDong = LichSuTonKho.LoaiBienDong.valueOf(loaiBienDong);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid loaiBienDong: {}", loaiBienDong);
            }
        }

        Page<LichSuTonKho> result = lichSuTonKhoRepository.findWithFilters(
                hangHoaId,
                viTriKhoId,
                enumLoaiBienDong,
                tuNgay,
                denNgay,
                keyword,
                pageable
        );

        return result.map(this::convertToDTO);
    }

    // ✅ Lấy lịch sử theo hàng hóa
    public Page<LichSuTonKhoDTO> getLichSuByHangHoa(Long hangHoaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LichSuTonKho> result = lichSuTonKhoRepository.findByHangHoaIdOrderByCreatedAtDesc(hangHoaId, pageable);
        return result.map(this::convertToDTO);
    }

    // ✅ Lấy lịch sử theo mã chứng từ
    public List<LichSuTonKhoDTO> getLichSuByMaChungTu(String maChungTu) {
        List<LichSuTonKho> list = lichSuTonKhoRepository.findByMaChungTu(maChungTu);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ✅ Lấy dữ liệu cho chart
    public List<LichSuTonKhoDTO> getChartData(Long hangHoaId, int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        List<LichSuTonKho> list = lichSuTonKhoRepository.findForChart(hangHoaId, fromDate);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ✅ GHI LOG LỊCH SỬ - Method chính
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ghiLichSu(
            HangHoa hangHoa,
            LoHang loHang,
            ViTriKho viTriKho,
            LichSuTonKho.LoaiBienDong loaiBienDong,
            Integer soLuongTruoc,
            Integer soLuongBienDong,
            Integer soLuongSau,
            BigDecimal donGia,
            String maChungTu,
            LichSuTonKho.LoaiChungTu loaiChungTu,
            String lyDo,
            User nguoiThucHien
    ) {
        try {
            // Tính giá trị biến động
            BigDecimal giaTriBienDong = BigDecimal.ZERO;
            if (donGia != null && soLuongBienDong != null) {
                giaTriBienDong = donGia.multiply(BigDecimal.valueOf(Math.abs(soLuongBienDong)));
            }

            // Tạo entity
            LichSuTonKho lichSu = LichSuTonKho.builder()
                    .hangHoa(hangHoa)
                    .loHang(loHang)
                    .viTriKho(viTriKho)
                    .loaiBienDong(loaiBienDong)
                    .soLuongTruoc(soLuongTruoc != null ? soLuongTruoc : 0)
                    .soLuongBienDong(soLuongBienDong != null ? soLuongBienDong : 0)
                    .soLuongSau(soLuongSau != null ? soLuongSau : 0)
                    .donGia(donGia)
                    .giaTriBienDong(giaTriBienDong)
                    .maChungTu(maChungTu)
                    .loaiChungTu(loaiChungTu)
                    .lyDo(lyDo)
                    .nguoiThucHien(nguoiThucHien)
                    .build();

            lichSuTonKhoRepository.save(lichSu);

            log.info("✅ Ghi lịch sử: {} - {} {} {} - Từ {} → {}",
                    loaiBienDong,
                    hangHoa.getTenHangHoa(),
                    soLuongBienDong > 0 ? "+" : "",
                    soLuongBienDong,
                    soLuongTruoc,
                    soLuongSau
            );
        } catch (Exception e) {
            log.error("❌ Lỗi ghi lịch sử tồn kho: {}", e.getMessage(), e);
            // Không throw để không làm fail transaction chính
        }
    }

    private LichSuTonKhoDTO convertToDTO(LichSuTonKho entity) {
        LichSuTonKhoDTO dto = new LichSuTonKhoDTO();

        // ID
        dto.setId(entity.getId());

        // ✅ Hàng hóa
        if (entity.getHangHoa() != null) {
            dto.setHangHoaId(entity.getHangHoa().getId());
            dto.setTenHangHoa(entity.getHangHoa().getTenHangHoa());
            dto.setMaHangHoa(entity.getHangHoa().getMaHangHoa());

            // ✅ SỬA: Lấy tên đơn vị tính từ Entity DonViTinh
            if (entity.getHangHoa().getDonViTinh() != null) {
                dto.setDonViTinh(entity.getHangHoa().getDonViTinh().getTenDvt());
            }
        }

        // Lô hàng
        if (entity.getLoHang() != null) {
            dto.setLoHangId(entity.getLoHang().getId());
            dto.setSoLo(entity.getLoHang().getSoLo());
            if (entity.getLoHang().getHanSuDung() != null) {
                dto.setHanSuDung(entity.getLoHang().getHanSuDung().toString());
            }
        }

        // Vị trí kho
        if (entity.getViTriKho() != null) {
            dto.setViTriKhoId(entity.getViTriKho().getId());
            dto.setTenViTriKho(entity.getViTriKho().getTenViTri());
            if (entity.getViTriKho().getKho() != null) {
                dto.setTenKho(entity.getViTriKho().getKho().getTenKho());
            }
        }

        // Biến động
        dto.setLoaiBienDong(entity.getLoaiBienDong().name());
        dto.setLoaiBienDongText(getLoaiBienDongText(entity.getLoaiBienDong()));
        dto.setSoLuongTruoc(entity.getSoLuongTruoc());
        dto.setSoLuongBienDong(entity.getSoLuongBienDong());
        dto.setSoLuongSau(entity.getSoLuongSau());

        // Giá trị
        dto.setDonGia(entity.getDonGia());
        dto.setGiaTriBienDong(entity.getGiaTriBienDong());

        // Chứng từ
        dto.setMaChungTu(entity.getMaChungTu());
        if (entity.getLoaiChungTu() != null) {
            dto.setLoaiChungTu(entity.getLoaiChungTu().name());
            dto.setLoaiChungTuText(getLoaiChungTuText(entity.getLoaiChungTu()));
        }

        // Lý do
        dto.setLyDo(entity.getLyDo());

        // Người thực hiện
        if (entity.getNguoiThucHien() != null) {
            dto.setNguoiThucHienId(entity.getNguoiThucHien().getId());
            dto.setTenNguoiThucHien(entity.getNguoiThucHien().getHoTen());
        }

        // Thời gian
        dto.setCreatedAt(entity.getCreatedAt());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAtFormatted(entity.getCreatedAt().format(FORMATTER));
        }

        return dto;
    }

    // ✅ Helper: Convert enum sang text tiếng Việt
    private String getLoaiBienDongText(LichSuTonKho.LoaiBienDong loaiBienDong) {
        if (loaiBienDong == null) return "";

        switch (loaiBienDong) {
            case NHAP_KHO: return "Nhập kho";
            case XUAT_KHO: return "Xuất kho";
            case DIEU_CHINH: return "Điều chỉnh";
            case KIEM_KE: return "Kiểm kê";
            case HUY_HANG: return "Hủy hàng";
            case CHUYEN_KHO: return "Chuyển kho";
            case HUY_DUYET_NHAP: return "Hủy duyệt nhập";
            case HUY_DUYET_XUAT: return "Hủy duyệt xuất";
            default: return loaiBienDong.name();
        }
    }

    private String getLoaiChungTuText(LichSuTonKho.LoaiChungTu loaiChungTu) {
        if (loaiChungTu == null) return "";

        switch (loaiChungTu) {
            case PHIEU_NHAP: return "Phiếu nhập";
            case PHIEU_XUAT: return "Phiếu xuất";
            case PHIEU_DIEU_CHINH: return "Phiếu điều chỉnh";
            case PHIEU_KIEM_KE: return "Phiếu kiểm kê";
            case PHIEU_HUY: return "Phiếu hủy";
            case HUY_DUYET_NHAP: return "Hủy duyệt nhập";
            case HUY_DUYET_XUAT: return "Hủy duyệt xuất";
            default: return loaiChungTu.name();
        }
    }
}