package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.LoHang;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoHangDTO {

    private Long id;

    @NotNull(message = "Vui lòng chọn hàng hóa")
    private Long hangHoaId;

    private String maHangHoa;

    private String tenHangHoa;

    private String tenDonViTinh;

    @NotBlank(message = "Vui lòng nhập số lô")
    @Size(max = 50, message = "Số lô không được quá 50 ký tự")
    private String soLo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySanXuat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hanSuDung;

    @NotNull(message = "Vui lòng nhập số lượng nhập")
    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
    private Integer soLuongNhap;

    private Integer soLuongHienTai;

    @NotNull(message = "Vui lòng nhập giá nhập")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá nhập phải lớn hơn 0")
    private BigDecimal giaNhap;

    private Long nhaCungCapId;

    private String tenNhaCungCap;

    @Size(max = 50, message = "Số chứng từ nhập không được quá 50 ký tự")
    private String soChungTuNhap;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String ghiChu;

    private LoHang.TrangThaiLoHang trangThai;

    private Integer soNgayConLai;

    private Boolean sapHetHan;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}