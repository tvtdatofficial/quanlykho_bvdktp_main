package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.PhieuXuatKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuXuatKhoRepository extends JpaRepository<PhieuXuatKho, Long>,
        JpaSpecificationExecutor<PhieuXuatKho> {

    Optional<PhieuXuatKho> findByMaPhieuXuat(String maPhieuXuat);

    List<PhieuXuatKho> findByTrangThai(PhieuXuatKho.TrangThaiPhieuXuat trangThai);

    long countByTrangThai(PhieuXuatKho.TrangThaiPhieuXuat trangThai);

    long countByMaPhieuXuatStartingWith(String prefix);

    List<PhieuXuatKho> findByKhoId(Long khoId);

    List<PhieuXuatKho> findByKhoIdAndNgayXuatBetween(
            Long khoId, LocalDateTime start, LocalDateTime end);

    List<PhieuXuatKho> findByKhoaPhongYeuCauId(Long khoaPhongId);
}