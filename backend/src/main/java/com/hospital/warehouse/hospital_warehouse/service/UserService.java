package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.UserDTO;
import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.entity.Role;
import com.hospital.warehouse.hospital_warehouse.repository.KhoaPhongRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import com.hospital.warehouse.hospital_warehouse.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private KhoaPhongRepository khoaPhongRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String tenDangNhap) throws UsernameNotFoundException {
        User user = userRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập: " + tenDangNhap));

        String vaiTro = user.getRole().getTenVaiTro();
        return org.springframework.security.core.userdetails.User.withUsername(user.getTenDangNhap())
                .password(user.getMatKhau())
                .roles(vaiTro)
                .build();
    }


    public User findByUsername(String tenDangNhap) {
        return userRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập: " + tenDangNhap));
    }

    public User timKiemTheoTenDangNhap(String tenDangNhap) {
        return findByUsername(tenDangNhap);
    }

    @Transactional
    public User saveEmployee(User user) {
        // Kiểm tra username và email như cũ
        if (userRepository.findByTenDangNhap(user.getTenDangNhap()).isPresent()) {
            throw new IllegalStateException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email đã tồn tại");
        }

        // ===== XỬ LÝ MÃ USER =====
        if (user.getMaUser() != null && !user.getMaUser().trim().isEmpty()) {
            // Nếu người dùng nhập mã User -> kiểm tra trùng
            if (userRepository.existsByMaUser(user.getMaUser().trim())) {
                throw new IllegalStateException("Mã User '" + user.getMaUser() + "' đã tồn tại");
            }
            user.setMaUser(user.getMaUser().trim().toUpperCase()); // Chuẩn hóa
        }
        // Nếu để trống -> giữ nguyên null, không tự động tạo

        // Xử lý role như cũ
        if (user.getRole() == null || user.getRole().getTenVaiTro() == null) {
            Role defaultRole = roleRepository.findByTenVaiTro("NHAN_VIEN_KHO")
                    .orElseThrow(() -> new IllegalStateException("Vai trò mặc định không tồn tại"));
            user.setRole(defaultRole);
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> searchUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByTenDangNhapContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(String roleName) {
        log.info("Lấy danh sách user theo role: {}", roleName);

        return userRepository.findByRoleTenVaiTroAndTrangThai(roleName, User.TrangThaiUser.HOAT_DONG)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .maUser(user.getMaUser()) // Thêm field này
                .tenDangNhap(user.getTenDangNhap())
                .email(user.getEmail())
                .hoTen(user.getHoTen())
                .soDienThoai(user.getSoDienThoai())
                .roleId(user.getRole().getId())
                .roleName(user.getRole().getTenVaiTro())
                .khoaPhongId(user.getKhoaPhong().getId())
                .tenKhoaPhong(user.getKhoaPhong().getTenKhoaPhong())
                .trangThai(user.getTrangThai())
                .createdAt(user.getThoiGianTao())
                .build();
    }
}