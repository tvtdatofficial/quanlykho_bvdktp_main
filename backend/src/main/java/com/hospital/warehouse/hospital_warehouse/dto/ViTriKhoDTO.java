package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.ViTriKho;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViTriKhoDTO {

    private Long id;

    @NotNull(message = "Vui lòng chọn kho")
    private Long khoId;

    private String tenKho;

    @NotBlank(message = "Vui lòng nhập mã vị trí")
    @Size(max = 30, message = "Mã vị trí không được quá 30 ký tự")
    private String maViTri;

    @Size(max = 100, message = "Tên vị trí không được quá 100 ký tự")
    private String tenViTri;

    private ViTriKho.LoaiViTri loaiViTri;

    private Long viTriChaId;

    private String tenViTriCha;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String moTa;

    @Min(value = 0, message = "Sức chứa tối đa phải >= 0")
    private Integer sucChuaToiDa;

    @DecimalMin(value = "0.0", message = "Trọng lượng tối đa phải >= 0")
    private BigDecimal trongLuongToiDa;

    private BigDecimal nhietDoYeuCau;

    private ViTriKho.TrangThaiViTri trangThai;

    private Integer soLuongHienTai;

    private Integer phanTramSuDung;

    private Boolean dangDay;

    private List<ViTriKhoDTO> viTriCon = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}