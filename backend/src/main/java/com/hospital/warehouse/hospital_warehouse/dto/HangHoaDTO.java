package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.HangHoa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HangHoaDTO {

    private Long id;

    @NotBlank(message = "Mã hàng hóa không được để trống")
    @Size(max = 30, message = "Mã hàng hóa không được vượt quá 30 ký tự")
    private String maHangHoa;

    @NotBlank(message = "Tên hàng hóa không được để trống")
    @Size(max = 200, message = "Tên hàng hóa không được vượt quá 200 ký tự")
    private String tenHangHoa;

    @Size(max = 200, message = "Tên khoa học không được vượt quá 200 ký tự")
    private String tenKhoaHoc;

    @Size(max = 50, message = "Mã barcode không được vượt quá 50 ký tự")
    private String maBarcode;

    @Size(max = 100, message = "Mã QR code không được vượt quá 100 ký tự")
    private String maQrCode;

    @NotNull(message = "Danh mục không được để trống")
    private Long danhMucId;
    private String tenDanhMuc;

    @NotNull(message = "Đơn vị tính không được để trống")
    private Long donViTinhId;
    private String tenDonViTinh;

    private Long nhaCungCapId;
    private String tenNhaCungCap;

    private String moTa;
    private String thanhPhan;
    private String congDung;
    private String cachSuDung;
    private String lieuLuong;
    private String dongGoi;
    private String xuatXu;
    private String hangSanXuat;
    private String soDangKy;

    @DecimalMin(value = "0.0", message = "Giá nhập trung bình phải >= 0")
    private BigDecimal giaNhapTrungBinh;

    @DecimalMin(value = "0.0", message = "Giá xuất trung bình phải >= 0")
    private BigDecimal giaXuatTrungBinh;

    @Min(value = 0, message = "Tổng số lượng phải >= 0")
    private Integer tongSoLuong;

    @Min(value = 0, message = "Số lượng có thể xuất phải >= 0")
    private Integer soLuongCoTheXuat;

    @Min(value = 0, message = "Số lượng đã đặt phải >= 0")
    private Integer soLuongDaDat;

    @Min(value = 0, message = "Số lượng tối thiểu phải >= 0")
    private Integer soLuongToiThieu;

    @Min(value = 0, message = "Số lượng tối đa phải >= 0")
    private Integer soLuongToiDa;

    @DecimalMin(value = "0.0", message = "Trọng lượng phải >= 0")
    private BigDecimal trongLuong;

    private String kichThuoc;
    private String mauSac;

    private String hinhAnhUrl;
    private String taiLieuDinhKem;

    private String yeuCauBaoQuan;
    private BigDecimal nhietDoBaoQuanMin;
    private BigDecimal nhietDoBaoQuanMax;
    private String doAmBaoQuan;

    @Min(value = 0, message = "Hạn sử dụng mặc định phải >= 0")
    private Integer hanSuDungMacDinh;

    @Min(value = 1, message = "Cảnh báo hết hạn phải >= 1")
    @Max(value = 365, message = "Cảnh báo hết hạn phải <= 365")
    private Integer canhBaoHetHan;

    private Boolean coQuanLyLo;
    private Boolean coHanSuDung;
    private Boolean coKiemSoatChatLuong;
    private Boolean laThuocDoc;
    private Boolean laThuocHuongThan;

    private String ghiChu;
    private HangHoa.TrangThaiHangHoa trangThai;

    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields for display
    private String trangThaiTonKho;
    private boolean isTonKhoThap;
    private boolean isDangHetHang;
}