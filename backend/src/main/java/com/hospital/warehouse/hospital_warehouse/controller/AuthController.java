package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.DangNhapRequest;
import com.hospital.warehouse.hospital_warehouse.dto.TokenModel;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.service.UserService;
import com.hospital.warehouse.hospital_warehouse.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/dang-nhap")
    public ResponseEntity<?> dangNhap(@RequestBody DangNhapRequest request) {
        log.info("Đang thử đăng nhập người dùng: {}", request.getTenDangNhap());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getTenDangNhap(), request.getMatKhau())
            );

            UserDetails userDetails = userService.loadUserByUsername(request.getTenDangNhap());
            User nguoiDung = userService.timKiemTheoTenDangNhap(request.getTenDangNhap());

            String accessToken = jwtService.taoAccessToken(
                    userDetails,
                    nguoiDung.getId(),
                    nguoiDung.getRole().getTenVaiTro(),
                    nguoiDung.getKhoaPhong().getId(),
                    nguoiDung.getHoTen()
            );

            String refreshToken = jwtService.taoRefreshToken(userDetails);

            TokenModel tokenResponse = TokenModel.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.layThoiGianHetHanAccessToken())
                    .tokenType("Bearer")
                    .userId(nguoiDung.getId())
                    .id(nguoiDung.getId())
                    .tenDangNhap(nguoiDung.getTenDangNhap())
                    .hoTen(nguoiDung.getHoTen())
                    .email(nguoiDung.getEmail())
                    .role(nguoiDung.getRole().getTenVaiTro())
                    .vaiTro(nguoiDung.getRole().getTenVaiTro())
                    .khoaPhongId(nguoiDung.getKhoaPhong().getId())
                    .tenKhoaPhong(nguoiDung.getKhoaPhong().getTenKhoaPhong())
                    .build();

            log.info("Đăng nhập thành công cho người dùng: {}", request.getTenDangNhap());
            return ResponseEntity.ok(tokenResponse);

        } catch (BadCredentialsException e) {
            log.warn("Sai thông tin đăng nhập: {}", request.getTenDangNhap());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Tên đăng nhập hoặc mật khẩu không đúng"));

        } catch (Exception e) {
            log.error("Lỗi trong quá trình đăng nhập: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi đăng nhập"));
        }
    }

    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(@RequestBody User user) {
        try {
            if (user.getKhoaPhong() == null || user.getKhoaPhong().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phải cung cấp thông tin khoa/phòng hợp lệ"));
            }

            // Mã hóa mật khẩu
            user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));

            User savedUser = userService.saveEmployee(user);

            return ResponseEntity.ok(Map.of("message", "Đăng ký người dùng thành công"));

        } catch (IllegalStateException e) {
            log.warn("Đăng ký thất bại: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Lỗi xảy ra khi đăng ký người dùng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống khi đăng ký"));
        }
    }

    @PostMapping("/dang-xuat")
    public ResponseEntity<?> dangXuat() {
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
}