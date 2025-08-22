package com.hospital.warehouse.hospital_warehouse.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangNhapRequest {
    private String tenDangNhap;
    private String matKhau;
}