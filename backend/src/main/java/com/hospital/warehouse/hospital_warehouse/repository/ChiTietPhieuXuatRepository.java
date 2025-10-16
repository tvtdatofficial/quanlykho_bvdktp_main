package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.ChiTietPhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChiTietPhieuXuatRepository extends JpaRepository<ChiTietPhieuXuat, Long> {

    List<ChiTietPhieuXuat> findByPhieuXuatId(Long phieuXuatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChiTietPhieuXuat c WHERE c.phieuXuat.id = :phieuXuatId")
    void deleteByPhieuXuatId(@Param("phieuXuatId") Long phieuXuatId);

    List<ChiTietPhieuXuat> findByHangHoaId(Long hangHoaId);


    // ✅ THÊM METHOD NÀY
    @Query("SELECT c FROM ChiTietPhieuXuat c " +
            "JOIN FETCH c.hangHoa h " +
            "LEFT JOIN FETCH h.donViTinh " +
            "LEFT JOIN FETCH c.loHang " +
            "LEFT JOIN FETCH c.viTriKho " +
            "WHERE c.phieuXuat.id = :phieuXuatId")
    List<ChiTietPhieuXuat> findByPhieuXuatIdWithDetails(@Param("phieuXuatId") Long phieuXuatId);

}