package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "khoa_phong", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ten_khoa_phong"}),
        @UniqueConstraint(columnNames = {"ma_khoa_phong"})
})
public class KhoaPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_khoa_phong", unique = true, nullable = false)
    private String maKhoaPhong;

    @Column(name = "ten_khoa_phong", nullable = false)
    private String tenKhoaPhong;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "dia_chi")
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date thoiGianTao;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Constructor tương thích với code cũ
    public KhoaPhong(String tenKhoaPhong, String maKhoaPhong, String moTa) {
        this.tenKhoaPhong = tenKhoaPhong;
        this.maKhoaPhong = maKhoaPhong;
        this.moTa = moTa;
    }

    @PrePersist
    public void prePersist() {
        if (this.thoiGianTao == null) this.thoiGianTao = new Date();
        if (this.updatedAt == null) this.updatedAt = new Date();
        if (this.trangThai == null) this.trangThai = TrangThaiKhoaPhong.HOAT_DONG;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
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