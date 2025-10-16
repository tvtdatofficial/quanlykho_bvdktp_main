package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.LichSuTonKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichSuTonKhoRepository extends JpaRepository<LichSuTonKho, Long> {

    List<LichSuTonKho> findByHangHoaIdOrderByCreatedAtDesc(Long hangHoaId);

    @Query("SELECT ls FROM LichSuTonKho ls WHERE ls.hangHoa.id = :hangHoaId " +
            "AND ls.createdAt BETWEEN :tuNgay AND :denNgay " +
            "ORDER BY ls.createdAt DESC")
    List<LichSuTonKho> findByHangHoaAndDateRange(
            @Param("hangHoaId") Long hangHoaId,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay
    );

    @Query("SELECT ls FROM LichSuTonKho ls WHERE ls.maChungTu = :maChungTu")
    List<LichSuTonKho> findByMaChungTu(@Param("maChungTu") String maChungTu);
}