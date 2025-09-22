package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @Size(max = 20, message = "Mã user không được vượt quá 20 ký tự")
    private String maUser;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 255, message = "Tên đăng nhập không được vượt quá 255 ký tự")
    private String tenDangNhap;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String hoTen;

    @Pattern(regexp = "^[0-9+\\-\\s]*$", message = "Số điện thoại không hợp lệ")
    @Size(max = 255, message = "Số điện thoại không được vượt quá 255 ký tự")
    private String soDienThoai;

    private String diaChi;
    private String ngaySinh;
    private String gioiTinh;
    private String chucDanh;

    @NotNull(message = "Role không được để trống")
    private Long roleId;
    private String roleName;

    @NotNull(message = "Khoa phòng không được để trống")
    private Long khoaPhongId;
    private String tenKhoaPhong;

    private User.TrangThaiUser trangThai;
    private String avatarUrl;
    private Date createdAt; // Đổi từ LocalDateTime thành Date để match với entity

    // Helper methods
    public boolean isActive() {
        return trangThai == User.TrangThaiUser.HOAT_DONG;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(roleName);
    }

    public boolean isQuanLyKho() {
        return "QUAN_LY_KHO".equals(roleName);
    }
}