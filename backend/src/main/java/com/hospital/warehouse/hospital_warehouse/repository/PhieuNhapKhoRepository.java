package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.PhieuNhapKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuNhapKhoRepository extends JpaRepository<PhieuNhapKho, Long>,
        JpaSpecificationExecutor<PhieuNhapKho> {

    Optional<PhieuNhapKho> findByMaPhieuNhap(String maPhieuNhap);

    List<PhieuNhapKho> findByTrangThai(PhieuNhapKho.TrangThaiPhieuNhap trangThai);

    long countByTrangThai(PhieuNhapKho.TrangThaiPhieuNhap trangThai);

    long countByMaPhieuNhapStartingWith(String prefix);

    List<PhieuNhapKho> findByKhoId(Long khoId);

    List<PhieuNhapKho> findByKhoIdAndNgayNhapBetween(
            Long khoId, LocalDateTime start, LocalDateTime end);


    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.maPhieuNhap, :prefixLength + 1, 4) AS long)), 0) " +
            "FROM PhieuNhapKho p WHERE p.maPhieuNhap LIKE :prefix")
    Long findMaxNumberByPrefix(@Param("prefix") String prefix,
                               @Param("prefixLength") int prefixLength);
}