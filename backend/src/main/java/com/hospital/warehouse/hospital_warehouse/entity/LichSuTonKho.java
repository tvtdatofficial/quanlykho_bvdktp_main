package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_ton_kho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuTonKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_hoa_id", nullable = false)
    private HangHoa hangHoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_hang_id")
    private LoHang loHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_kho_id")
    private ViTriKho viTriKho;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_bien_dong", nullable = false)
    private LoaiBienDong loaiBienDong;

    @Column(name = "so_luong_truoc", nullable = false)
    private Integer soLuongTruoc;

    @Column(name = "so_luong_bien_dong", nullable = false)
    private Integer soLuongBienDong;

    @Column(name = "so_luong_sau", nullable = false)
    private Integer soLuongSau;

    @Column(name = "don_gia", precision = 15, scale = 2)
    private BigDecimal donGia;

    @Column(name = "gia_tri_bien_dong", precision = 15, scale = 2)
    private BigDecimal giaTriBienDong;

    @Column(name = "ma_chung_tu", length = 30)
    private String maChungTu;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_chung_tu")
    private LoaiChungTu loaiChungTu;

    @Column(name = "ly_do", columnDefinition = "TEXT")
    private String lyDo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private User nguoiThucHien;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LoaiBienDong {
        NHAP_KHO,
        XUAT_KHO,
        DIEU_CHINH,
        KIEM_KE,
        HUY_HANG,
        CHUYEN_KHO,
        HUY_DUYET_NHAP,    // ✅ THÊM
        HUY_DUYET_XUAT     // ✅ THÊM
    }

    public enum LoaiChungTu {
        PHIEU_NHAP,
        PHIEU_XUAT,
        PHIEU_DIEU_CHINH,
        PHIEU_KIEM_KE,
        PHIEU_HUY,
        HUY_DUYET_NHAP,    // ✅ THÊM
        HUY_DUYET_XUAT     // ✅ THÊM
    }
}