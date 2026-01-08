package com.hospital.warehouse.hospital_warehouse.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuTonKhoDTO {

    private Long id;

    // Thông tin hàng hóa
    private Long hangHoaId;
    private String tenHangHoa;
    private String maHangHoa;
    private String donViTinh;

    // Thông tin lô hàng
    private Long loHangId;
    private String soLo;
    private String hanSuDung;

    // Thông tin vị trí kho
    private Long viTriKhoId;
    private String tenViTriKho;
    private String tenKho;

    // Thông tin biến động
    private String loaiBienDong;
    private String loaiBienDongText;
    private Integer soLuongTruoc;
    private Integer soLuongBienDong;
    private Integer soLuongSau;

    // Thông tin giá trị
    private BigDecimal donGia;
    private BigDecimal giaTriBienDong;

    // Thông tin chứng từ
    private String maChungTu;
    private String loaiChungTu;
    private String loaiChungTuText;

    // Lý do
    private String lyDo;

    // Người thực hiện
    private Long nguoiThucHienId;
    private String tenNguoiThucHien;

    // Thời gian
    private LocalDateTime createdAt;
    private String createdAtFormatted;
}