package com.hospital.warehouse.hospital_warehouse.controller;

import com.hospital.warehouse.hospital_warehouse.dto.ApiResponse;
import com.hospital.warehouse.hospital_warehouse.dto.UserDTO;
import com.hospital.warehouse.hospital_warehouse.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/quan-ly-kho")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getQuanLyKhoUsers() {
        try {
            List<UserDTO> users = userService.getUsersByRole("QUAN_LY_KHO");
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách quản lý kho: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách quản lý kho"));
        }
    }

    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable String roleName) {
        try {
            List<UserDTO> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người dùng theo role {}: {}", roleName, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách người dùng"));
        }
    }

    @GetMapping("/nhan-vien-kho")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getNhanVienKhoUsers() {
        try {
            List<UserDTO> users = userService.getUsersByRole("NHAN_VIEN_KHO");
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhân viên kho: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách nhân viên kho"));
        }
    }

    @GetMapping("/ky-thuat-vien")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUAN_LY_KHO')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getKyThuatVienUsers() {
        try {
            List<UserDTO> users = userService.getUsersByRole("KY_THUAT_VIEN");
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách kỹ thuật viên: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi lấy danh sách kỹ thuật viên"));
        }
    }
}