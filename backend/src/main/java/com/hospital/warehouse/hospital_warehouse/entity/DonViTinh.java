package com.hospital.warehouse.hospital_warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "don_vi_tinh", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ma_dvt"})
})
public class DonViTinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_dvt", unique = true, nullable = false, length = 10)
    private String maDvt;

    @Column(name = "ten_dvt", unique = true, nullable = false, length = 50)
    private String tenDvt;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}