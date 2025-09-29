package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vi_tri_kho")
public class ViTriKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @Column(name = "ma_vi_tri", nullable = false, length = 30)
    private String maViTri;

    @Column(name = "ten_vi_tri", length = 100)
    private String tenViTri;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_vi_tri")
    private LoaiViTri loaiViTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_cha_id")
    private ViTriKho viTriCha;

    @OneToMany(mappedBy = "viTriCha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ViTriKho> viTriCon;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "suc_chua_toi_da", columnDefinition = "INT DEFAULT 0")
    private Integer sucChuaToiDa = 0;

    @Column(name = "trong_luong_toi_da", precision = 10, scale = 2)
    private BigDecimal trongLuongToiDa;

    @Column(name = "nhiet_do_yeu_cau", precision = 5, scale = 2)
    private BigDecimal nhietDoYeuCau;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('TRONG','CO_HANG','DAY','BAO_TRI') DEFAULT 'TRONG'")
    private TrangThaiViTri trangThai = TrangThaiViTri.TRONG;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.trangThai == null) this.trangThai = TrangThaiViTri.TRONG;
        if (this.sucChuaToiDa == null) this.sucChuaToiDa = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LoaiViTri {
        KE("Kệ"),
        NGAN("Ngăn"),
        O("Ô"),
        TU_LANH("Tủ lạnh"),
        TU_DONG("Tủ đông"),
        KHU_VUC("Khu vực");

        private final String displayName;

        LoaiViTri(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TrangThaiViTri {
        TRONG("Trống"),
        CO_HANG("Có hàng"),
        DAY("Đầy"),
        BAO_TRI("Bảo trì");

        private final String displayName;

        TrangThaiViTri(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}