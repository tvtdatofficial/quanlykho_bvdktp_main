package com.hospital.warehouse.hospital_warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenModel {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String role;
    private Long id;
    private Long khoaPhongId;
    private String hoTen;
    private String tenKhoaPhong;

    // Fields cho AuthController
    private Long userId;
    private String tenDangNhap;
    private String email;
    private String vaiTro;

    // Constructor cũ để tương thích
    public TokenModel(String accessToken, String refreshToken, Long expiresIn, String role, Long id, Long khoaPhongId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.role = role;
        this.id = id;
        this.khoaPhongId = khoaPhongId;
    }
}