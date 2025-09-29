package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chi_tiet_phieu_nhap")
public class ChiTietPhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_nhap_id", nullable = false)
    private PhieuNhapKho phieuNhap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hang_hoa_id", nullable = false)
    private HangHoa hangHoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_hang_id")
    private LoHang loHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vi_tri_kho_id")
    private ViTriKho viTriKho;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", precision = 15, scale = 2, nullable = false)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", precision = 15, scale = 2, nullable = false)
    private BigDecimal thanhTien;

    @Column(name = "tien_thue", precision = 15, scale = 2)
    private BigDecimal tienThue = BigDecimal.ZERO;

    @Column(name = "ty_le_thue", precision = 5, scale = 2)
    private BigDecimal tyLeThue = BigDecimal.ZERO;

    @Column(name = "ngay_san_xuat")
    private LocalDate ngaySanXuat;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    @Column(name = "so_lo", length = 50)
    private String soLo;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiChiTiet trangThai = TrangThaiChiTiet.CHO_NHAP;

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
        if (this.trangThai == null) this.trangThai = TrangThaiChiTiet.CHO_NHAP;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TrangThaiChiTiet {
        CHO_NHAP, DA_NHAP, LOI
    }
}