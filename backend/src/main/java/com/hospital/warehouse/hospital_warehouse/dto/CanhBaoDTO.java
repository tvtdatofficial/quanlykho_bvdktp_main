package com.hospital.warehouse.hospital_warehouse.dto;

import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanhBaoDTO {
    private Long id;
    private String loaiCanhBao;
    private String mucDo;
    private String tieuDe;
    private String noiDung;
    private Long doiTuongLienQuanId;
    private String loaiDoiTuong;
    private Boolean daDoc;
    private Boolean daXuLy;
    private Long nguoiXuLyId;
    private String tenNguoiXuLy;
    private LocalDateTime thoiGianXuLy;
    private String ghiChuXuLy;
    private LocalDate ngayHetHieuLuc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Convert từ Entity sang DTO
    public static CanhBaoDTO fromEntity(CanhBaoHeThong entity) {
        CanhBaoDTO dto = new CanhBaoDTO();
        dto.setId(entity.getId());
        dto.setLoaiCanhBao(entity.getLoaiCanhBao() != null ? entity.getLoaiCanhBao().name() : null);
        dto.setMucDo(entity.getMucDo() != null ? entity.getMucDo().name() : null);
        dto.setTieuDe(entity.getTieuDe());
        dto.setNoiDung(entity.getNoiDung());
        dto.setDoiTuongLienQuanId(entity.getDoiTuongLienQuanId());
        dto.setLoaiDoiTuong(entity.getLoaiDoiTuong() != null ? entity.getLoaiDoiTuong().name() : null);
        dto.setDaDoc(entity.getDaDoc());
        dto.setDaXuLy(entity.getDaXuLy());
        dto.setNguoiXuLyId(entity.getNguoiXuLyId());
        dto.setThoiGianXuLy(entity.getThoiGianXuLy());
        dto.setGhiChuXuLy(entity.getGhiChuXuLy());
        dto.setNgayHetHieuLuc(entity.getNgayHetHieuLuc());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}