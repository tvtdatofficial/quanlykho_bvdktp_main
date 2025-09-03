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
@Table(name = "nha_cung_cap", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_ncc"})
})
public class NhaCungCap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_ncc", unique = true, nullable = false, length = 20)
    private String maNcc;

    @Column(name = "ten_ncc", nullable = false, length = 150)
    private String tenNcc;

    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "ma_so_thue", length = 20)
    private String maSoThue;

    @Column(name = "nguoi_lien_he", length = 100)
    private String nguoiLienHe;

    @Column(name = "sdt_lien_he", length = 20)
    private String sdtLienHe;

    @Column(name = "email_lien_he", length = 100)
    private String emailLienHe;

    @Column(name = "diem_danh_gia", precision = 3, scale = 2, columnDefinition = "DECIMAL(3,2) DEFAULT 0.00")
    private BigDecimal diemDanhGia = BigDecimal.ZERO;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','TAM_DUNG','NGUNG_HOP_TAC') DEFAULT 'HOAT_DONG'")
    private TrangThaiNcc trangThai = TrangThaiNcc.HOAT_DONG;

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
            this.trangThai = TrangThaiNcc.HOAT_DONG;
        }
        if (this.diemDanhGia == null) {
            this.diemDanhGia = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TrangThaiNcc {
        HOAT_DONG("Hoạt động"),
        TAM_DUNG("Tạm dừng"),
        NGUNG_HOP_TAC("Ngừng hợp tác");

        private final String displayName;

        TrangThaiNcc(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}