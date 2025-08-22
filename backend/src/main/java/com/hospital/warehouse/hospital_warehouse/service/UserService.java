package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.entity.Role;
import com.hospital.warehouse.hospital_warehouse.repository.KhoaPhongRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import com.hospital.warehouse.hospital_warehouse.repository.RoleRepository;
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
        Optional<User> existingByUsername = userRepository.findByTenDangNhap(user.getTenDangNhap());
        if (existingByUsername.isPresent()) {
            throw new IllegalStateException("Tên đăng nhập '" + user.getTenDangNhap() + "' đã tồn tại");
        }
        Optional<User> existingByEmail = userRepository.findByEmail(user.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalStateException("Email '" + user.getEmail() + "' đã tồn tại");
        }
        if (user.getRole() == null || user.getRole().getTenVaiTro() == null) {
            Role defaultRole = roleRepository.findByTenVaiTro("NHAN_VIEN_KHO")
                    .orElseThrow(() -> new IllegalStateException("Vai trò mặc định NHAN_VIEN_KHO không tồn tại"));
            user.setRole(defaultRole);
        } else {
            Role role = roleRepository.findByTenVaiTro(user.getRole().getTenVaiTro())
                    .orElseThrow(() -> new IllegalStateException("Vai trò '" + user.getRole().getTenVaiTro() + "' không tồn tại"));
            user.setRole(role);
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
}