package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.NhaCungCap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhaCungCapDTO {

    private Long id;

    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    @Size(max = 20, message = "Mã nhà cung cấp không được vượt quá 20 ký tự")
    private String maNcc;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 150, message = "Tên nhà cung cấp không được vượt quá 150 ký tự")
    private String tenNcc;

    @Size(max = 1000, message = "Địa chỉ không được vượt quá 1000 ký tự")
    private String diaChi;

    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String soDienThoai;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;

    @Size(max = 20, message = "Mã số thuế không được vượt quá 20 ký tự")
    private String maSoThue;

    @Size(max = 100, message = "Tên người liên hệ không được vượt quá 100 ký tự")
    private String nguoiLienHe;

    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Số điện thoại liên hệ không hợp lệ")
    @Size(max = 20, message = "Số điện thoại liên hệ không được vượt quá 20 ký tự")
    private String sdtLienHe;

    @Email(message = "Email liên hệ không hợp lệ")
    @Size(max = 100, message = "Email liên hệ không được vượt quá 100 ký tự")
    private String emailLienHe;

    @DecimalMin(value = "0.0", message = "Điểm đánh giá phải >= 0")
    @DecimalMax(value = "5.0", message = "Điểm đánh giá phải <= 5")
    private BigDecimal diemDanhGia;

    private String ghiChu;

    private NhaCungCap.TrangThaiNcc trangThai;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
