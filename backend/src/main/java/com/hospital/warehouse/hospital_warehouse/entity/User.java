package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenDangNhap"}),
        @UniqueConstraint(columnNames = {"email"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_user", unique = true, nullable = true, length = 20)
    private String maUser;

    @Column(name = "ten_dang_nhap", unique = true, nullable = false)
    private String tenDangNhap;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date thoiGianTao;

    @ManyToOne
    @JoinColumn(name = "khoa_phong_id", nullable = false)
    private KhoaPhong khoaPhong;

    // Thêm enum TrangThaiUser
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','TAM_KHOA','NGHI_VIEC') DEFAULT 'HOAT_DONG'")
    private TrangThaiUser trangThai = TrangThaiUser.HOAT_DONG;

    @PrePersist
    public void prePersist() {
        if (this.thoiGianTao == null) this.thoiGianTao = new Date();
        if (this.trangThai == null) this.trangThai = TrangThaiUser.HOAT_DONG;
    }

    // Thêm enum vào cuối class
    public enum TrangThaiUser {
        HOAT_DONG("Hoạt động"),
        TAM_KHOA("Tạm khóa"),
        NGHI_VIEC("Nghỉ việc");

        private final String displayName;

        TrangThaiUser(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}