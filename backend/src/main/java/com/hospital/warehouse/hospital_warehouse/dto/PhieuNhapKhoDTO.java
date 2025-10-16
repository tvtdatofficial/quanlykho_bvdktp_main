package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.PhieuNhapKho;
import com.hospital.warehouse.hospital_warehouse.entity.ChiTietPhieuNhap;
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
public class PhieuNhapKhoDTO {

    private Long id;

    private String maPhieuNhap;

    @NotNull(message = "Vui lòng chọn kho")
    private Long khoId;

    private String tenKho;

    private Long nhaCungCapId;

    private String tenNhaCungCap;

    @NotNull(message = "Vui lòng chọn loại nhập")
    private PhieuNhapKho.LoaiNhap loaiNhap;

    @Size(max = 50, message = "Số hóa đơn không được quá 50 ký tự")
    private String soHoaDon;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayHoaDon;

    @Size(max = 50, message = "Số chứng từ không được quá 50 ký tự")
    private String soChungTu;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayChungTu;

    @NotNull(message = "Vui lòng chọn ngày nhập")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayNhap;

    private BigDecimal tongTienTruocThue;

    private BigDecimal tienThue;

    private BigDecimal tongTienSauThue;

    @DecimalMin(value = "0.0", message = "Tỷ lệ thuế phải >= 0")
    @DecimalMax(value = "100.0", message = "Tỷ lệ thuế phải <= 100")
    private BigDecimal tyLeThue;

    @DecimalMin(value = "0.0", message = "Chi phí vận chuyển phải >= 0")
    private BigDecimal chiPhiVanChuyen;

    @DecimalMin(value = "0.0", message = "Chi phí khác phải >= 0")
    private BigDecimal chiPhiKhac;

    @DecimalMin(value = "0.0", message = "Giảm giá phải >= 0")
    private BigDecimal giamGia;

    private BigDecimal tongThanhToan;

    private PhieuNhapKho.TrangThaiThanhToan trangThaiThanhToan;

    @Size(max = 100, message = "Tên người giao không được quá 100 ký tự")
    private String nguoiGiao;

    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String sdtNguoiGiao;

    private Long nguoiNhanId;

    private String tenNguoiNhan;

    private Long nguoiKiemTraId;

    private String tenNguoiKiemTra;

    private Long nguoiDuyetId;

    private String tenNguoiDuyet;

    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String ghiChu;

    private PhieuNhapKho.TrangThaiPhieuNhap trangThai;

    private String lyDoHuy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayDuyet;

    @Valid
    @NotEmpty(message = "Vui lòng thêm ít nhất một hàng hóa")
    private List<ChiTietPhieuNhapDTO> chiTiet = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ===== NESTED DTO: Chi tiết phiếu nhập =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietPhieuNhapDTO {

        private Long id;

        @NotNull(message = "Vui lòng chọn hàng hóa")
        private Long hangHoaId;

        private String maHangHoa;

        private String tenHangHoa;

        private String tenDonViTinh;

        // ✅ THÊM DÒNG NÀY
        private String hinhAnhUrl;

        private Long loHangId;

        private Long viTriKhoId;

        private String tenViTriKho;

        @NotNull(message = "Vui lòng nhập số lượng")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;

        @NotNull(message = "Vui lòng nhập đơn giá")
        @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
        private BigDecimal donGia;

        @NotNull(message = "Thành tiền không hợp lệ")
        @DecimalMin(value = "0.0", message = "Thành tiền phải >= 0")
        private BigDecimal thanhTien;

        @DecimalMin(value = "0.0", message = "Tiền thuế phải >= 0")
        private BigDecimal tienThue;

        @DecimalMin(value = "0.0", message = "Tỷ lệ thuế phải >= 0")
        @DecimalMax(value = "100.0", message = "Tỷ lệ thuế phải <= 100")
        private BigDecimal tyLeThue;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate ngaySanXuat;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate hanSuDung;

        @Size(max = 50, message = "Số lô không được quá 50 ký tự")
        private String soLo;

        @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
        private String ghiChu;

        private ChiTietPhieuNhap.TrangThaiChiTiet trangThai;
    }

    // ===== NESTED DTO: Thống kê phiếu nhập =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThongKePhieuNhap {

        private Long tongSoPhieu;

        private Long soPhieuNhap;

        private Long soPhieuChoDuyet;

        private Long soPhieuDaDuyet;

        private Long soPhieuHuy;

        private BigDecimal tongGiaTri;

        private BigDecimal giaTriChoDuyet;

        private BigDecimal giaTriDaDuyet;

        private Integer tongSoMatHang;

        private Integer tongSoLuong;
    }
}