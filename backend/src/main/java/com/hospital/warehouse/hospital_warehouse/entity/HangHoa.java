package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hang_hoa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_hang_hoa"})
})
public class HangHoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hang_hoa", unique = true, nullable = false, length = 30)
    private String maHangHoa;

    @Column(name = "ten_hang_hoa", nullable = false, length = 200)
    private String tenHangHoa;

    @Column(name = "ten_khoa_hoc", length = 200)
    private String tenKhoaHoc;

    @Column(name = "ma_barcode", length = 50)
    private String maBarcode;

    @Column(name = "ma_qr_code", length = 100)
    private String maQrCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "danh_muc_id", nullable = false)
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "don_vi_tinh_id", nullable = false)
    private DonViTinh donViTinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nha_cung_cap_id")
    private NhaCungCap nhaCungCap;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "thanh_phan", columnDefinition = "TEXT")
    private String thanhPhan;

    @Column(name = "cong_dung", columnDefinition = "TEXT")
    private String congDung;

    @Column(name = "cach_su_dung", columnDefinition = "TEXT")
    private String cachSuDung;

    @Column(name = "lieu_luong", length = 100)
    private String lieuLuong;

    @Column(name = "dong_goi", length = 100)
    private String dongGoi;

    @Column(name = "xuat_xu", length = 100)
    private String xuatXu;

    @Column(name = "hang_san_xuat", length = 150)
    private String hangSanXuat;

    @Column(name = "so_dang_ky", length = 50)
    private String soDangKy;

    @Column(name = "gia_nhap_trung_binh", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal giaNhapTrungBinh = BigDecimal.ZERO;

    @Column(name = "gia_xuat_trung_binh", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal giaXuatTrungBinh = BigDecimal.ZERO;

    @Column(name = "tong_so_luong", columnDefinition = "INT DEFAULT 0")
    private Integer tongSoLuong = 0;

    @Column(name = "so_luong_co_the_xuat", columnDefinition = "INT DEFAULT 0")
    private Integer soLuongCoTheXuat = 0;

    @Column(name = "so_luong_da_dat", columnDefinition = "INT DEFAULT 0")
    private Integer soLuongDaDat = 0;

    @Column(name = "so_luong_toi_thieu", columnDefinition = "INT DEFAULT 0")
    private Integer soLuongToiThieu = 0;

    @Column(name = "so_luong_toi_da", columnDefinition = "INT DEFAULT 0")
    private Integer soLuongToiDa = 0;

    @Column(name = "trong_luong", precision = 8, scale = 3)
    private BigDecimal trongLuong;

    @Column(name = "kich_thuoc", length = 100)
    private String kichThuoc;

    @Column(name = "mau_sac", length = 50)
    private String mauSac;

    // Thay vì các annotation JSON phức tạp:
    @Lob
    @Column(name = "hinh_anh_url", columnDefinition = "longtext")
    private String hinhAnhUrl;

    @Lob
    @Column(name = "tai_lieu_dinh_kem", columnDefinition = "longtext")
    private String taiLieuDinhKem;

    @Column(name = "yeu_cau_bao_quan", columnDefinition = "TEXT")
    private String yeuCauBaoQuan;

    @Column(name = "nhiet_do_bao_quan_min", precision = 5, scale = 2)
    private BigDecimal nhietDoBaoQuanMin;

    @Column(name = "nhiet_do_bao_quan_max", precision = 5, scale = 2)
    private BigDecimal nhietDoBaoQuanMax;

    @Column(name = "do_am_bao_quan", length = 50)
    private String doAmBaoQuan;

    @Column(name = "han_su_dung_mac_dinh", columnDefinition = "INT")
    private Integer hanSuDungMacDinh;

    @Column(name = "canh_bao_het_han", columnDefinition = "INT DEFAULT 30")
    private Integer canhBaoHetHan = 30;

    @Column(name = "co_quan_ly_lo", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean coQuanLyLo = false;

    @Column(name = "co_han_su_dung", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean coHanSuDung = true;

    @Column(name = "co_kiem_soat_chat_luong", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean coKiemSoatChatLuong = false;

    @Column(name = "la_thuoc_doc", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean laThuocDoc = false;

    @Column(name = "la_thuoc_huong_than", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean laThuocHuongThan = false;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "ENUM('HOAT_DONG','TAM_DUNG','NGUNG_KINH_DOANH') DEFAULT 'HOAT_DONG'")
    private TrangThaiHangHoa trangThai = TrangThaiHangHoa.HOAT_DONG;

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
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.trangThai == null) {
            this.trangThai = TrangThaiHangHoa.HOAT_DONG;
        }
        // Set default values for numeric fields
        if (this.giaNhapTrungBinh == null) this.giaNhapTrungBinh = BigDecimal.ZERO;
        if (this.giaXuatTrungBinh == null) this.giaXuatTrungBinh = BigDecimal.ZERO;
        if (this.tongSoLuong == null) this.tongSoLuong = 0;
        if (this.soLuongCoTheXuat == null) this.soLuongCoTheXuat = 0;
        if (this.soLuongDaDat == null) this.soLuongDaDat = 0;
        if (this.soLuongToiThieu == null) this.soLuongToiThieu = 0;
        if (this.soLuongToiDa == null) this.soLuongToiDa = 0;
        if (this.canhBaoHetHan == null) this.canhBaoHetHan = 30;
        if (this.coQuanLyLo == null) this.coQuanLyLo = false;
        if (this.coHanSuDung == null) this.coHanSuDung = true;
        if (this.coKiemSoatChatLuong == null) this.coKiemSoatChatLuong = false;
        if (this.laThuocDoc == null) this.laThuocDoc = false;
        if (this.laThuocHuongThan == null) this.laThuocHuongThan = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TrangThaiHangHoa {
        HOAT_DONG("Hoạt động"),
        TAM_DUNG("Tạm dừng"),
        NGUNG_KINH_DOANH("Ngừng kinh doanh");

        private final String displayName;

        TrangThaiHangHoa(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean isTonKhoThap() {
        return soLuongCoTheXuat != null && soLuongToiThieu != null &&
                soLuongCoTheXuat < soLuongToiThieu;
    }

    public boolean isDangHetHang() {
        return soLuongCoTheXuat != null && soLuongCoTheXuat <= 0;
    }

    public String getTrangThaiTonKho() {
        if (isDangHetHang()) {
            return "HẾT HÀNG";
        } else if (isTonKhoThap()) {
            return "TỒN KHO THẤP";
        } else {
            return "BÌNH THƯỜNG";
        }
    }
}