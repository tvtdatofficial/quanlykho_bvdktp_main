package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "canh_bao_he_thong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanhBaoHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_canh_bao", nullable = false, length = 50)
    private LoaiCanhBao loaiCanhBao;

    @Enumerated(EnumType.STRING)
    @Column(name = "muc_do", length = 20)
    private MucDo mucDo = MucDo.THONG_TIN;

    @Column(name = "tieu_de", nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "doi_tuong_lien_quan_id")
    private Long doiTuongLienQuanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_doi_tuong", length = 30)
    private LoaiDoiTuong loaiDoiTuong;

    @Column(name = "da_doc")
    private Boolean daDoc = false;

    @Column(name = "da_xu_ly")
    private Boolean daXuLy = false;

    @Column(name = "nguoi_xu_ly_id")
    private Long nguoiXuLyId;

    @Column(name = "thoi_gian_xu_ly")
    private LocalDateTime thoiGianXuLy;

    @Column(name = "ghi_chu_xu_ly", columnDefinition = "TEXT")
    private String ghiChuXuLy;

    @Column(name = "ngay_het_hieu_luc")
    private LocalDate ngayHetHieuLuc;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LoaiCanhBao {
        TON_KHO_THAP,
        HET_HAN,
        GAN_HET_HAN,
        THIET_BI_CAN_BAO_TRI,
        THIET_BI_HU_HONG,
        THANH_TOAN_QUA_HAN,
        KIEM_KE,
        KHAC
    }

    public enum MucDo {
        THONG_TIN,
        CANH_BAO,
        NGHIEM_TRONG,
        KHAN_CAP
    }

    public enum LoaiDoiTuong {
        HANG_HOA,
        THIET_BI,
        HOA_DON,
        PHIEU_NHAP,
        PHIEU_XUAT,
        KHO,
        KHAC
    }
}