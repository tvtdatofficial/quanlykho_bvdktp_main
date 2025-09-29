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
@Table(name = "phieu_xuat_kho")
public class PhieuXuatKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu_xuat", unique = true, nullable = false, length = 30)
    private String maPhieuXuat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id", nullable = false)
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khoa_phong_yeu_cau_id")
    private KhoaPhong khoaPhongYeuCau;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_xuat", nullable = false)
    private LoaiXuat loaiXuat;

    @Column(name = "so_phieu_yeu_cau", length = 50)
    private String soPhieuYeuCau;

    @Column(name = "ngay_yeu_cau")
    private LocalDate ngayYeuCau;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "ngay_xuat", nullable = false)
    private LocalDateTime ngayXuat;

    @Column(name = "tong_gia_tri", precision = 15, scale = 2)
    private BigDecimal tongGiaTri = BigDecimal.ZERO;

    @Column(name = "nguoi_yeu_cau", length = 100)
    private String nguoiYeuCau;

    @Column(name = "sdt_nguoi_yeu_cau", length = 20)
    private String sdtNguoiYeuCau;

    @Column(name = "nguoi_nhan", length = 100)
    private String nguoiNhan;

    @Column(name = "sdt_nguoi_nhan", length = 20)
    private String sdtNguoiNhan;

    @Column(name = "dia_chi_giao", columnDefinition = "TEXT")
    private String diaChiGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xuat_id", nullable = false)
    private User nguoiXuat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_kiem_tra_id")
    private User nguoiKiemTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private User nguoiDuyet;

    @Column(name = "ly_do_xuat", columnDefinition = "TEXT")
    private String lyDoXuat;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiPhieuXuat trangThai = TrangThaiPhieuXuat.XUAT;

    @Column(name = "ly_do_huy", columnDefinition = "TEXT")
    private String lyDoHuy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "ngay_giao")
    private LocalDateTime ngayGiao;

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
        if (this.trangThai == null) this.trangThai = TrangThaiPhieuXuat.XUAT;
        if (this.ngayXuat == null) this.ngayXuat = LocalDateTime.now();
        if (this.tongGiaTri == null) this.tongGiaTri = BigDecimal.ZERO;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LoaiXuat {
        XUAT_SU_DUNG("Xuất sử dụng"),
        XUAT_TRA("Xuất trả"),
        XUAT_CHUYEN_KHO("Xuất chuyển kho"),
        XUAT_HUY("Xuất hủy"),
        XUAT_BAN("Xuất bán"),
        XUAT_SUA_CHUA("Xuất sửa chữa");

        private final String displayName;

        LoaiXuat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TrangThaiPhieuXuat {
        XUAT("Mới tạo"),
        CHO_DUYET("Chờ duyệt"),
        DA_DUYET("Đã duyệt"),
        DA_GIAO("Đã giao"),
        HUY("Đã hủy");

        private final String displayName;

        TrangThaiPhieuXuat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}