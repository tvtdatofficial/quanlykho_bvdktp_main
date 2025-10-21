package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong;
import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong.LoaiCanhBao;
import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong.MucDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CanhBaoRepository extends JpaRepository<CanhBaoHeThong, Long> {

    // Tìm tất cả cảnh báo chưa xử lý
    Page<CanhBaoHeThong> findByDaXuLyFalseOrderByCreatedAtDesc(Pageable pageable);

    // Tìm theo loại cảnh báo
    Page<CanhBaoHeThong> findByLoaiCanhBaoAndDaXuLyFalse(
            LoaiCanhBao loaiCanhBao,
            Pageable pageable
    );

    // Tìm theo mức độ
    Page<CanhBaoHeThong> findByMucDoAndDaXuLyFalse(
            MucDo mucDo,
            Pageable pageable
    );

    // Tìm cảnh báo chưa đọc
    Page<CanhBaoHeThong> findByDaDocFalseOrderByCreatedAtDesc(Pageable pageable);

    // Đếm cảnh báo chưa xử lý
    long countByDaXuLyFalse();

    // Đếm cảnh báo chưa đọc
    long countByDaDocFalse();

    // Đếm theo mức độ chưa xử lý
    long countByMucDoAndDaXuLyFalse(MucDo mucDo);

    // Tìm theo đối tượng liên quan
    List<CanhBaoHeThong> findByDoiTuongLienQuanIdAndLoaiDoiTuong(
            Long doiTuongId,
            CanhBaoHeThong.LoaiDoiTuong loaiDoiTuong
    );

    // Tìm cảnh báo trong khoảng thời gian
    @Query("SELECT c FROM CanhBaoHeThong c WHERE c.createdAt BETWEEN :tuNgay AND :denNgay ORDER BY c.createdAt DESC")
    Page<CanhBaoHeThong> findByCreatedAtBetween(
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );

    // Thống kê theo loại
    @Query("SELECT c.loaiCanhBao, COUNT(c) FROM CanhBaoHeThong c WHERE c.daXuLy = false GROUP BY c.loaiCanhBao")
    List<Object[]> thongKeTheoLoai();

    // Thống kê theo mức độ
    @Query("SELECT c.mucDo, COUNT(c) FROM CanhBaoHeThong c WHERE c.daXuLy = false GROUP BY c.mucDo")
    List<Object[]> thongKeTheoMucDo();
}