package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhoaPhongDTO {

    private Long id;

    @NotBlank(message = "Mã khoa phòng không được để trống")
    @Size(max = 255, message = "Mã khoa phòng không được vượt quá 255 ký tự")
    private String maKhoaPhong;

    @NotBlank(message = "Tên khoa phòng không được để trống")
    @Size(max = 255, message = "Tên khoa phòng không được vượt quá 255 ký tự")
    private String tenKhoaPhong;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String moTa;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String diaChi;

    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String soDienThoai;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    private Long truongKhoaId;
    private String tenTruongKhoa;

    private KhoaPhong.TrangThaiKhoaPhong trangThai;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isActive() {
        return trangThai == KhoaPhong.TrangThaiKhoaPhong.HOAT_DONG;
    }
}