package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.ChiTietPhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, Long> {

    List<ChiTietPhieuNhap> findByPhieuNhapId(Long phieuNhapId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChiTietPhieuNhap c WHERE c.phieuNhap.id = :phieuNhapId")
    void deleteByPhieuNhapId(@Param("phieuNhapId") Long phieuNhapId);

    List<ChiTietPhieuNhap> findByHangHoaId(Long hangHoaId);

    List<ChiTietPhieuNhap> findByLoHangId(Long loHangId);

    // ✅ SỬA BẰNG FETCH JOIN
    @Query("SELECT ct FROM ChiTietPhieuNhap ct " +
            "JOIN FETCH ct.hangHoa h " +
            "JOIN FETCH h.donViTinh " +
            "LEFT JOIN FETCH ct.viTriKho " +
            "LEFT JOIN FETCH ct.loHang " +
            "WHERE ct.phieuNhap.id = :phieuNhapId")
    List<ChiTietPhieuNhap> findByPhieuNhapIdWithDetails(@Param("phieuNhapId") Long phieuNhapId);
}