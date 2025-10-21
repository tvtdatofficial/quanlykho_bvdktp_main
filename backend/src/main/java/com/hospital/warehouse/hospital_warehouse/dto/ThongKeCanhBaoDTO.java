package com.hospital.warehouse.hospital_warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeCanhBaoDTO {
    private long tongCanhBao;
    private long chuaXuLy;
    private long chuaDoc;
    private long khanCap;
    private long nghiemTrong;
    private long canhBao;
    private long thongTin;
}