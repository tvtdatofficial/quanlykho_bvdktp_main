package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "khoa_phong", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ten_khoa_phong"}),
        @UniqueConstraint(columnNames = {"ma_khoa_phong"})
})
public class KhoaPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_khoa_phong", nullable = false)
    private String tenKhoaPhong;

    @Column(name = "ma_khoa_phong", unique = true, nullable = false)
    private String maKhoaPhong;

    @Column(name = "mo_ta")
    private String moTa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "thoi_gian_tao", nullable = false, updatable = false)
    private Date thoiGianTao;

    public KhoaPhong(String tenKhoaPhong, String maKhoaPhong, String moTa) {
        this.tenKhoaPhong = tenKhoaPhong;
        this.maKhoaPhong = maKhoaPhong;
        this.moTa = moTa;
    }

    @PrePersist
    public void prePersist() {
        if (this.thoiGianTao == null) this.thoiGianTao = new Date();
    }
}