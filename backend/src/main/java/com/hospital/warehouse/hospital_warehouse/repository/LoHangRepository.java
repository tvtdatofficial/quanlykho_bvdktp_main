package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.LoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý lô hàng
 * Hỗ trợ: FIFO, FEFO, quản lý hạn sử dụng, tồn kho theo lô
 */
@Repository
public interface LoHangRepository extends JpaRepository<LoHang, Long>,
        JpaSpecificationExecutor<LoHang> {

    // ==================== TÌM KIẾM CƠ BẢN ====================

    /**
     * ✅ QUAN TRỌNG: Tìm lô hàng theo hàng hóa + số lô + hạn sử dụng
     * Dùng để kiểm tra trùng lặp khi tạo lô mới
     * Tránh tạo nhiều lô có cùng số lô nhưng khác HSD
     */
    Optional<LoHang> findByHangHoaIdAndSoLoAndHanSuDung(
            Long hangHoaId,
            String soLo,
            LocalDate hanSuDung
    );

    /**
     * Lấy tất cả lô hàng của một hàng hóa
     */
    List<LoHang> findByHangHoaId(Long hangHoaId);

    /**
     * ✅ BỔ SUNG: Tìm lô theo hàng hóa và số lượng còn > X
     * Dùng cho logic chọn lô có hàng
     */
    List<LoHang> findByHangHoaIdAndSoLuongHienTaiGreaterThan(
            Long hangHoaId,
            int soLuong
    );

    /**
     * Lấy lô hàng theo trạng thái
     */
    List<LoHang> findByTrangThai(LoHang.TrangThaiLoHang trangThai);

    // ==================== XUẤT KHO - FIFO/FEFO ====================

    /**
     * ✅ QUAN TRỌNG: Lấy danh sách lô hàng khả dụng để XUẤT KHO theo FIFO
     * Sắp xếp ưu tiên:
     * 1. Lô có HSD → Lô không có HSD (NULL cuối cùng)
     * 2. HSD sớm nhất → HSD muộn
     * 3. Ngày sản xuất sớm → muộn
     * 4. ID nhỏ (nhập trước) → ID lớn
     *
     * Chỉ lấy lô:
     * - Còn hàng (soLuongHienTai > 0)
     * - Trạng thái: MOI, DANG_SU_DUNG, GAN_HET_HAN
     * - Loại trừ: HET_HAN, HET_HANG
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.hangHoa.id = :hangHoaId 
          AND l.soLuongHienTai > 0
          AND l.trangThai IN ('MOI', 'DANG_SU_DUNG', 'GAN_HET_HAN')
        ORDER BY 
          CASE WHEN l.hanSuDung IS NULL THEN 1 ELSE 0 END,
          l.hanSuDung ASC,
          l.ngaySanXuat ASC,
          l.id ASC
    """)
    List<LoHang> findAvailableLoHangForXuat(@Param("hangHoaId") Long hangHoaId);

    // ==================== CẢNH BÁO & BÁO CÁO ====================

    /**
     * ✅ SỬA LẠI: Tìm lô sắp hết hạn trong khoảng thời gian
     * Dùng cho cảnh báo hết hạn (thường là 30 ngày)
     *
     * @param startDate Ngày bắt đầu (thường là hôm nay)
     * @param endDate   Ngày kết thúc (thường là +30 ngày)
     * @return Danh sách lô sắp hết hạn, sắp xếp theo HSD tăng dần
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.hanSuDung BETWEEN :startDate AND :endDate 
          AND l.soLuongHienTai > 0
          AND l.trangThai != 'HET_HAN'
        ORDER BY l.hanSuDung ASC
    """)
    List<LoHang> findExpiringSoon(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * ✅ BỔ SUNG: Tìm lô đã hết hạn nhưng còn hàng
     * Dùng để cảnh báo cần xử lý lô hết hạn
     *
     * @param date      Ngày kiểm tra (thường là hôm nay)
     * @param trangThai Trạng thái lô (thường là DANG_SU_DUNG hoặc GAN_HET_HAN)
     * @return Danh sách lô đã hết hạn nhưng còn hàng
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.hanSuDung <= :date 
          AND l.trangThai = :trangThai 
          AND l.soLuongHienTai > 0
        ORDER BY l.hanSuDung ASC
    """)
    List<LoHang> findByHanSuDungBeforeAndTrangThai(
            @Param("date") LocalDate date,
            @Param("trangThai") LoHang.TrangThaiLoHang trangThai
    );

    /**
     * Tìm lô đã hết hàng (số lượng = 0)
     * Dùng để dọn dẹp hoặc báo cáo
     */
    @Query("SELECT l FROM LoHang l WHERE l.soLuongHienTai <= 0")
    List<LoHang> findEmptyLoHang();

    // ==================== THỐNG KÊ & PHÂN TÍCH ====================

    /**
     * ✅ BỔ SUNG: Đếm số lô hàng của một hàng hóa
     */
    @Query("SELECT COUNT(l) FROM LoHang l WHERE l.hangHoa.id = :hangHoaId")
    Long countByHangHoaId(@Param("hangHoaId") Long hangHoaId);

    /**
     * ✅ BỔ SUNG: Tính tổng số lượng tồn kho của một hàng hóa
     * (Tổng từ tất cả các lô)
     */
    @Query("""
        SELECT COALESCE(SUM(l.soLuongHienTai), 0) 
        FROM LoHang l 
        WHERE l.hangHoa.id = :hangHoaId
    """)
    Integer sumSoLuongHienTaiByHangHoaId(@Param("hangHoaId") Long hangHoaId);

    /**
     * ✅ BỔ SUNG: Lấy giá nhập trung bình của hàng hóa từ các lô còn hàng
     * Dùng công thức WAVG (Weighted Average)
     */
    @Query("""
        SELECT COALESCE(
            SUM(l.giaNhap * l.soLuongHienTai) / 
            NULLIF(SUM(l.soLuongHienTai), 0), 
            0
        )
        FROM LoHang l 
        WHERE l.hangHoa.id = :hangHoaId 
          AND l.soLuongHienTai > 0
    """)
    Double calculateAverageGiaNhapByHangHoaId(@Param("hangHoaId") Long hangHoaId);

    /**
     * ✅ BỔ SUNG: Lấy lô có HSD gần nhất (còn hàng)
     * Dùng để hiển thị cảnh báo HSD sớm nhất của hàng hóa
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.hangHoa.id = :hangHoaId 
          AND l.soLuongHienTai > 0
          AND l.hanSuDung IS NOT NULL
        ORDER BY l.hanSuDung ASC
        LIMIT 1
    """)
    Optional<LoHang> findEarliestExpiringLoHang(@Param("hangHoaId") Long hangHoaId);

    // ==================== BÁO CÁO NÂNG CAO ====================

    /**
     * ✅ BỔ SUNG: Thống kê lô hàng theo trạng thái
     * Dùng cho dashboard
     *
     * @return List<Object[]> với format: [TrangThai, Count, TotalQty]
     */
    @Query("""
        SELECT l.trangThai, 
               COUNT(l), 
               COALESCE(SUM(l.soLuongHienTai), 0)
        FROM LoHang l 
        GROUP BY l.trangThai
        ORDER BY l.trangThai
    """)
    List<Object[]> statisticsByTrangThai();

    /**
     * ✅ BỔ SUNG: Tìm lô hàng theo nhà cung cấp
     * Dùng để tra cứu lô từ nhà cung cấp cụ thể
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.nhaCungCap.id = :nhaCungCapId 
          AND l.soLuongHienTai > 0
        ORDER BY l.hanSuDung ASC
    """)
    List<LoHang> findByNhaCungCapId(@Param("nhaCungCapId") Long nhaCungCapId);

    /**
     * ✅ BỔ SUNG: Tìm lô hàng nhập trong khoảng thời gian
     * Dùng cho báo cáo nhập kho theo thời gian
     */
    @Query("""
        SELECT l FROM LoHang l 
        WHERE l.createdAt BETWEEN :startDate AND :endDate
        ORDER BY l.createdAt DESC
    """)
    List<LoHang> findByCreatedAtBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * ✅ BỔ SUNG: Kiểm tra tồn tại lô hàng
     * Dùng để validate trước khi tạo mới
     */
    boolean existsByHangHoaIdAndSoLoAndHanSuDung(
            Long hangHoaId,
            String soLo,
            LocalDate hanSuDung
    );
}