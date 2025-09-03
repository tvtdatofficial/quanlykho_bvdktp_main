package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.DanhMuc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucDTO {

    private Long id;

    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 20, message = "Mã danh mục không được vượt quá 20 ký tự")
    private String maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String tenDanhMuc;

    private Long danhMucChaId;
    private String tenDanhMucCha;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String moTa;

    private DanhMuc.LoaiDanhMuc loaiDanhMuc;

    @Min(value = 0, message = "Thứ tự sắp xếp phải >= 0")
    private Integer thuTuSapXep;

    private DanhMuc.TrangThaiDanhMuc trangThai;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For tree structure
    private List<DanhMucDTO> danhMucCon;
    private boolean hasChildren;
    private boolean isRootCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DanhMucTreeDTO {
        private Long id;
        private String maDanhMuc;
        private String tenDanhMuc;
        private Long danhMucChaId;
        private Integer level;
        private boolean hasChildren;
        private List<DanhMucTreeDTO> children;
    }
}