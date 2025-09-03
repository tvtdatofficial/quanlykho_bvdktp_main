package com.hospital.warehouse.hospital_warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonViTinhDTO {

    private Long id;

    @NotBlank(message = "Mã đơn vị tính không được để trống")
    @Size(max = 10, message = "Mã đơn vị tính không được vượt quá 10 ký tự")
    private String maDvt;

    @NotBlank(message = "Tên đơn vị tính không được để trống")
    @Size(max = 50, message = "Tên đơn vị tính không được vượt quá 50 ký tự")
    private String tenDvt;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String moTa;

    private LocalDateTime createdAt;
}