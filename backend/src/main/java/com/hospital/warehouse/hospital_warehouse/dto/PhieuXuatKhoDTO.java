package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.PhieuXuatKho;
import com.hospital.warehouse.hospital_warehouse.entity.ChiTietPhieuXuat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhieuXuatKhoDTO {

    private Long id;

    private String maPhieuXuat;

    @NotNull(message = "Vui lòng chọn kho")
    private Long khoId;

    private String tenKho;

    private Long khoaPhongYeuCauId;

    private String tenKhoaPhong;

    @NotNull(message = "Vui lòng chọn loại xuất")
    private PhieuXuatKho.LoaiXuat loaiXuat;

    @Size(max = 50, message = "Số phiếu yêu cầu không được quá 50 ký tự")
    private String soPhieuYeuCau;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayYeuCau;

    @NotNull(message = "Vui lòng chọn ngày xuất")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayXuat;

    private BigDecimal tongGiaTri;

    @Size(max = 100, message = "Tên người yêu cầu không được quá 100 ký tự")
    private String nguoiYeuCau;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String sdtNguoiYeuCau;

    @Size(max = 100, message = "Tên người nhận không được quá 100 ký tự")
    private String nguoiNhan;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String sdtNguoiNhan;

    @Size(max = 255, message = "Địa chỉ giao không được quá 255 ký tự")
    private String diaChiGiao;

    private Long nguoiXuatId;

    private String tenNguoiXuat;

    private Long nguoiKiemTraId;

    private String tenNguoiKiemTra;

    private Long nguoiDuyetId;

    private String tenNguoiDuyet;

    @NotBlank(message = "Vui lòng nhập lý do xuất")
    @Size(max = 500, message = "Lý do xuất không được quá 500 ký tự")
    private String lyDoXuat;

    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String ghiChu;

    private PhieuXuatKho.TrangThaiPhieuXuat trangThai;

    private String lyDoHuy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayDuyet;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayGiao;

    @Valid
    @NotEmpty(message = "Vui lòng thêm ít nhất một hàng hóa")
    private List<ChiTietPhieuXuatDTO> chiTiet = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ===== NESTED DTO: Chi tiết phiếu xuất =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietPhieuXuatDTO {

        private Long id;

        @NotNull(message = "Vui lòng chọn hàng hóa")
        private Long hangHoaId;

        private String maHangHoa;

        private String tenHangHoa;

        private String tenDonViTinh;

        private Long loHangId;

        private String soLo;

        private Long viTriKhoId;

        private String tenViTriKho;

        @NotNull(message = "Vui lòng nhập số lượng yêu cầu")
        @Min(value = 1, message = "Số lượng yêu cầu phải lớn hơn 0")
        private Integer soLuongYeuCau;

        @NotNull(message = "Vui lòng nhập số lượng xuất")
        @Min(value = 1, message = "Số lượng xuất phải lớn hơn 0")
        private Integer soLuongXuat;

        private Integer tonKhoHienTai;

        @NotNull(message = "Vui lòng nhập đơn giá")
        @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
        private BigDecimal donGia;

        @NotNull(message = "Thành tiền không hợp lệ")
        @DecimalMin(value = "0.0", message = "Thành tiền phải >= 0")
        private BigDecimal thanhTien;

        @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
        private String ghiChu;

        private ChiTietPhieuXuat.TrangThaiChiTiet trangThai;
    }

    // ===== NESTED DTO: Thống kê phiếu xuất =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThongKePhieuXuat {

        private Long tongSoPhieu;

        private Long soPhieuXuat;

        private Long soPhieuChoDuyet;

        private Long soPhieuDaDuyet;

        private Long soPhieuDaGiao;

        private Long soPhieuHuy;

        private BigDecimal tongGiaTri;

        private Integer tongSoMatHang;

        private Integer tongSoLuong;
    }
}