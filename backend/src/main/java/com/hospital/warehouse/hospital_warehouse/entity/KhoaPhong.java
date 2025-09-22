package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "khoa_phong", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_khoa_phong"}),
        @UniqueConstraint(columnNames = {"ten_khoa_phong"})
})
public class KhoaPhong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_khoa_phong", unique = true, nullable = false, length = 255)
    private String maKhoaPhong;

    @Column(name = "ten_khoa_phong", unique = true, nullable = false, length = 255)
    private String tenKhoaPhong;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truong_khoa_id")
    private User truongKhoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','TAM_DUNG','DONG_CUA') DEFAULT 'HOAT_DONG'")
    private TrangThaiKhoaPhong trangThai = TrangThaiKhoaPhong.HOAT_DONG;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.trangThai == null) {
            this.trangThai = TrangThaiKhoaPhong.HOAT_DONG;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TrangThaiKhoaPhong {
        HOAT_DONG("Hoạt động"),
        TAM_DUNG("Tạm dừng"),
        DONG_CUA("Đóng cửa");

        private final String displayName;

        TrangThaiKhoaPhong(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}