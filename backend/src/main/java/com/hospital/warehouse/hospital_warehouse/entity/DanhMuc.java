package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "danh_muc", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_danh_muc"})
})
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_danh_muc", unique = true, nullable = false, length = 20)
    private String maDanhMuc;

    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String tenDanhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_muc_cha_id")
    @JsonIgnore
    private DanhMuc danhMucCha;

    @OneToMany(mappedBy = "danhMucCha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DanhMuc> danhMucCon;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_danh_muc")
    private LoaiDanhMuc loaiDanhMuc;

    @Column(name = "thu_tu_sap_xep", columnDefinition = "INT DEFAULT 0")
    private Integer thuTuSapXep = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','NGUNG_HOAT_DONG') DEFAULT 'HOAT_DONG'")
    private TrangThaiDanhMuc trangThai = TrangThaiDanhMuc.HOAT_DONG;

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
            this.trangThai = TrangThaiDanhMuc.HOAT_DONG;
        }
        if (this.thuTuSapXep == null) {
            this.thuTuSapXep = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LoaiDanhMuc {
        THUOC("Thuốc"),
        VAT_TU_Y_TE("Vật tư y tế"),
        THIET_BI_Y_TE("Thiết bị y tế"),
        HOA_CHAT("Hóa chất"),
        TINH_THAT("Trang thiết bị"),
        KHAC("Khác");

        private final String displayName;

        LoaiDanhMuc(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TrangThaiDanhMuc {
        HOAT_DONG("Hoạt động"),
        NGUNG_HOAT_DONG("Ngừng hoạt động");

        private final String displayName;

        TrangThaiDanhMuc(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean hasChildren() {
        return danhMucCon != null && !danhMucCon.isEmpty();
    }

    public boolean isRootCategory() {
        return danhMucCha == null;
    }
}