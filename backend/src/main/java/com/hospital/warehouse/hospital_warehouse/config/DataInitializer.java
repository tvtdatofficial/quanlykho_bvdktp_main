package com.hospital.warehouse.hospital_warehouse.config;

import com.hospital.warehouse.hospital_warehouse.entity.Role;
import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.repository.RoleRepository;
import com.hospital.warehouse.hospital_warehouse.repository.KhoaPhongRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final KhoaPhongRepository khoaPhongRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Bắt đầu khởi tạo dữ liệu...");

        khoiTaoVaiTro();
        khoiTaoKhoaPhong();
        khoiTaoNguoiDungAdmin();

        log.info("Hoàn thành khởi tạo dữ liệu.");
    }

    private void khoiTaoVaiTro() {
        String[] cacVaiTro = {
                "ADMIN",
                "QUAN_LY_KHO",
                "NHAN_VIEN_KHO"
        };

        for (String vaiTro : cacVaiTro) {
            if (roleRepository.findByTenVaiTro(vaiTro).isEmpty()) {
                Role role = new Role(vaiTro);
                roleRepository.save(role);
                log.info("Đã tạo vai trò: {}", vaiTro);
            }
        }
    }

    private void khoiTaoKhoaPhong() {
        String[][] cacKhoaPhong = {
                {"ADMIN", "Ban Giám Đốc", "Ban Giám đốc bệnh viện"},
                {"KHO_CHINH", "Kho Chính", "Kho tổng của bệnh viện"},
                {"KHO_DUOC", "Kho Dược", "Kho chứa thuốc và dược phẩm"},
                {"KHO_VAT_TU", "Kho Vật Tư Y Tế", "Kho chứa vật tư y tế"},
                {"KHO_THIET_BI", "Kho Thiết Bị", "Kho chứa thiết bị y tế"}
        };

        for (String[] khoaPhong : cacKhoaPhong) {
            if (khoaPhongRepository.findByMaKhoaPhong(khoaPhong[0]).isEmpty()) {
                KhoaPhong kp = new KhoaPhong(khoaPhong[1], khoaPhong[0], khoaPhong[2]);
                khoaPhongRepository.save(kp);
                log.info("Đã tạo khoa phòng: {}", khoaPhong[1]);
            }
        }
    }

    private void khoiTaoNguoiDungAdmin() {
        // Tạo tài khoản admin mặc định
        if (userRepository.findByTenDangNhap("admin").isEmpty()) {
            Role adminRole = roleRepository.findByTenVaiTro("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò ADMIN"));

            KhoaPhong adminDepartment = khoaPhongRepository.findByMaKhoaPhong("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa phòng ADMIN"));

            User admin = new User();
            admin.setTenDangNhap("admin");
            admin.setMatKhau(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@hospital.com");
            admin.setHoTen("Quản trị viên");
            admin.setSoDienThoai("0123456789");
            admin.setRole(adminRole);
            admin.setKhoaPhong(adminDepartment);

            userRepository.save(admin);
            log.info("Đã tạo tài khoản admin mặc định - Username: admin, Password: admin123");
        }

        // Tạo tài khoản quản lý kho mẫu
        if (userRepository.findByTenDangNhap("quanly_kho").isEmpty()) {
            Role quanLyRole = roleRepository.findByTenVaiTro("QUAN_LY_KHO")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò QUAN_LY_KHO"));

            KhoaPhong khoChinhDepartment = khoaPhongRepository.findByMaKhoaPhong("KHO_CHINH")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa phòng KHO_CHINH"));

            User quanLyKho = new User();
            quanLyKho.setTenDangNhap("quanly_kho");
            quanLyKho.setMatKhau(passwordEncoder.encode("123456"));
            quanLyKho.setEmail("quanlykho@hospital.com");
            quanLyKho.setHoTen("Nguyễn Văn Quản Lý");
            quanLyKho.setSoDienThoai("0987654321");
            quanLyKho.setRole(quanLyRole);
            quanLyKho.setKhoaPhong(khoChinhDepartment);

            userRepository.save(quanLyKho);
            log.info("Đã tạo tài khoản quản lý kho mẫu - Username: quanly_kho, Password: 123456");
        }

        // Tạo tài khoản nhân viên kho mẫu
        if (userRepository.findByTenDangNhap("nhanvien_kho").isEmpty()) {
            Role nhanVienRole = roleRepository.findByTenVaiTro("NHAN_VIEN_KHO")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò NHAN_VIEN_KHO"));

            KhoaPhong khoDuocDepartment = khoaPhongRepository.findByMaKhoaPhong("KHO_DUOC")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa phòng KHO_DUOC"));

            User nhanVienKho = new User();
            nhanVienKho.setTenDangNhap("nhanvien_kho");
            nhanVienKho.setMatKhau(passwordEncoder.encode("123456"));
            nhanVienKho.setEmail("nhanvienkho@hospital.com");
            nhanVienKho.setHoTen("Trần Thị Nhân Viên");
            nhanVienKho.setSoDienThoai("0912345678");
            nhanVienKho.setRole(nhanVienRole);
            nhanVienKho.setKhoaPhong(khoDuocDepartment);

            userRepository.save(nhanVienKho);
            log.info("Đã tạo tài khoản nhân viên kho mẫu - Username: nhanvien_kho, Password: 123456");
        }
    }
}