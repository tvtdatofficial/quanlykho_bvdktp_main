package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.LichSuTonKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichSuTonKhoRepository extends JpaRepository<LichSuTonKho, Long> {

    // ✅ Tìm theo hàng hóa (có phân trang)
    Page<LichSuTonKho> findByHangHoaIdOrderByCreatedAtDesc(Long hangHoaId, Pageable pageable);

    // ✅ Tìm theo hàng hóa (không phân trang)
    List<LichSuTonKho> findByHangHoaIdOrderByCreatedAtDesc(Long hangHoaId);

    // ✅ Tìm theo hàng hóa và khoảng thời gian
    @Query("SELECT ls FROM LichSuTonKho ls WHERE ls.hangHoa.id = :hangHoaId " +
            "AND ls.createdAt BETWEEN :tuNgay AND :denNgay " +
            "ORDER BY ls.createdAt DESC")
    List<LichSuTonKho> findByHangHoaAndDateRange(
            @Param("hangHoaId") Long hangHoaId,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay
    );

    // ✅ Tìm theo mã chứng từ
    @Query("SELECT ls FROM LichSuTonKho ls WHERE ls.maChungTu = :maChungTu ORDER BY ls.createdAt DESC")
    List<LichSuTonKho> findByMaChungTu(@Param("maChungTu") String maChungTu);

    // ✅ Tìm theo vị trí kho
    Page<LichSuTonKho> findByViTriKhoIdOrderByCreatedAtDesc(Long viTriKhoId, Pageable pageable);

    // ✅ Tìm theo loại biến động
    Page<LichSuTonKho> findByLoaiBienDongOrderByCreatedAtDesc(
            LichSuTonKho.LoaiBienDong loaiBienDong,
            Pageable pageable
    );

    // ✅ Query tổng hợp với filter
    @Query("SELECT ls FROM LichSuTonKho ls " +
            "WHERE (:hangHoaId IS NULL OR ls.hangHoa.id = :hangHoaId) " +
            "AND (:viTriKhoId IS NULL OR ls.viTriKho.id = :viTriKhoId) " +
            "AND (:loaiBienDong IS NULL OR ls.loaiBienDong = :loaiBienDong) " +
            "AND (:tuNgay IS NULL OR ls.createdAt >= :tuNgay) " +
            "AND (:denNgay IS NULL OR ls.createdAt <= :denNgay) " +
            "AND (:keyword IS NULL OR " +
            "     LOWER(ls.hangHoa.tenHangHoa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(ls.hangHoa.maHangHoa) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(ls.maChungTu) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY ls.createdAt DESC")
    Page<LichSuTonKho> findWithFilters(
            @Param("hangHoaId") Long hangHoaId,
            @Param("viTriKhoId") Long viTriKhoId,
            @Param("loaiBienDong") LichSuTonKho.LoaiBienDong loaiBienDong,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ✅ Lấy dữ liệu cho chart (30 ngày gần nhất)
    @Query("SELECT ls FROM LichSuTonKho ls " +
            "WHERE ls.hangHoa.id = :hangHoaId " +
            "AND ls.createdAt >= :fromDate " +
            "ORDER BY ls.createdAt ASC")
    List<LichSuTonKho> findForChart(
            @Param("hangHoaId") Long hangHoaId,
            @Param("fromDate") LocalDateTime fromDate
    );
}