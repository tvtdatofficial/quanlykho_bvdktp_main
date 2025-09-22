package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kho", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_kho"})
})
public class Kho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_kho", unique = true, nullable = false, length = 20)
    private String maKho;

    @Column(name = "ten_kho", nullable = false, length = 100)
    private String tenKho;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_kho", nullable = false)
    private LoaiKho loaiKho;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "dien_tich", precision = 10, scale = 2)
    private BigDecimal dienTich;

    @Column(name = "nhiet_do_min", precision = 5, scale = 2)
    private BigDecimal nhietDoMin;

    @Column(name = "nhiet_do_max", precision = 5, scale = 2)
    private BigDecimal nhietDoMax;

    @Column(name = "do_am_min", precision = 5, scale = 2)
    private BigDecimal doAmMin;

    @Column(name = "do_am_max", precision = 5, scale = 2)
    private BigDecimal doAmMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khoa_phong_id", nullable = false)
    private KhoaPhong khoaPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quan_ly_kho_id")
    private User quanLyKho;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','BAO_TRI','DONG_CUA') DEFAULT 'HOAT_DONG'")
    private TrangThaiKho trangThai = TrangThaiKho.HOAT_DONG;

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
            this.trangThai = TrangThaiKho.HOAT_DONG;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LoaiKho {
        KHO_CHINH("Kho chính"),
        KHO_DUOC("Kho dược"),
        KHO_VAT_TU("Kho vật tư"),
        KHO_THIET_BI("Kho thiết bị"),
        KHO_TINH_THAT("Kho trang thiết bị"),
        KHO_HOA_CHAT("Kho hóa chất");

        private final String displayName;

        LoaiKho(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TrangThaiKho {
        HOAT_DONG("Hoạt động"),
        BAO_TRI("Bảo trì"),
        DONG_CUA("Đóng cửa");

        private final String displayName;

        TrangThaiKho(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}