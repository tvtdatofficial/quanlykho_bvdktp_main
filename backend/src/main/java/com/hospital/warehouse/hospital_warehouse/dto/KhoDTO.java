package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.Kho;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhoDTO {

    private Long id;

    @NotBlank(message = "Mã kho không được để trống")
    @Size(max = 20, message = "Mã kho không được vượt quá 20 ký tự")
    private String maKho;

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 100, message = "Tên kho không được vượt quá 100 ký tự")
    private String tenKho;

    @NotNull(message = "Loại kho không được để trống")
    private Kho.LoaiKho loaiKho;

    private String moTa;
    private String diaChi;
    private BigDecimal dienTich;
    private BigDecimal nhietDoMin;
    private BigDecimal nhietDoMax;
    private BigDecimal doAmMin;
    private BigDecimal doAmMax;

    @NotNull(message = "Khoa phòng không được để trống")
    private Long khoaPhongId;
    private String tenKhoaPhong;

    private Long quanLyKhoId;
    private String tenQuanLyKho;

    private Kho.TrangThaiKho trangThai;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}