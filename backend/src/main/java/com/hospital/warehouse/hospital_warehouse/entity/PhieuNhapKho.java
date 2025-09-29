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
@Table(name = "phieu_nhap_kho")
public class PhieuNhapKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu_nhap", unique = true, nullable = false, length = 30)
    private String maPhieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nha_cung_cap_id")
    private NhaCungCap nhaCungCap;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_nhap", nullable = false)
    private LoaiNhap loaiNhap;

    @Column(name = "so_hoa_don", length = 50)
    private String soHoaDon;

    @Column(name = "ngay_hoa_don")
    private LocalDate ngayHoaDon;

    @Column(name = "so_chung_tu", length = 50)
    private String soChungTu;

    @Column(name = "ngay_chung_tu")
    private LocalDate ngayChungTu;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "ngay_nhap", nullable = false)
    private LocalDateTime ngayNhap;

    @Column(name = "tong_tien_truoc_thue", precision = 15, scale = 2)
    private BigDecimal tongTienTruocThue = BigDecimal.ZERO;

    @Column(name = "tien_thue", precision = 15, scale = 2)
    private BigDecimal tienThue = BigDecimal.ZERO;

    @Column(name = "tong_tien_sau_thue", precision = 15, scale = 2)
    private BigDecimal tongTienSauThue = BigDecimal.ZERO;

    @Column(name = "ty_le_thue", precision = 5, scale = 2)
    private BigDecimal tyLeThue = BigDecimal.ZERO;

    @Column(name = "chi_phi_van_chuyen", precision = 15, scale = 2)
    private BigDecimal chiPhiVanChuyen = BigDecimal.ZERO;

    @Column(name = "chi_phi_khac", precision = 15, scale = 2)
    private BigDecimal chiPhiKhac = BigDecimal.ZERO;

    @Column(name = "giam_gia", precision = 15, scale = 2)
    private BigDecimal giamGia = BigDecimal.ZERO;

    @Column(name = "tong_thanh_toan", precision = 15, scale = 2)
    private BigDecimal tongThanhToan = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan")
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.CHUA_THANH_TOAN;

    @Column(name = "nguoi_giao", length = 100)
    private String nguoiGiao;

    @Column(name = "sdt_nguoi_giao", length = 20)
    private String sdtNguoiGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhan_id", nullable = false)
    private User nguoiNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_kiem_tra_id")
    private User nguoiKiemTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private User nguoiDuyet;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiPhieuNhap trangThai = TrangThaiPhieuNhap.NHAP;

    @Column(name = "ly_do_huy", columnDefinition = "TEXT")
    private String lyDoHuy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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
        if (this.trangThai == null) this.trangThai = TrangThaiPhieuNhap.NHAP;
        if (this.ngayNhap == null) this.ngayNhap = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LoaiNhap {
        NHAP_MOI, NHAP_TRA, NHAP_CHUYEN_KHO, NHAP_DIEU_CHINH, NHAP_BAO_HANH
    }

    public enum TrangThaiPhieuNhap {
        NHAP, CHO_DUYET, DA_DUYET, HUY
    }

    public enum TrangThaiThanhToan {
        CHUA_THANH_TOAN, THANH_TOAN_1_PHAN, DA_THANH_TOAN
    }
}