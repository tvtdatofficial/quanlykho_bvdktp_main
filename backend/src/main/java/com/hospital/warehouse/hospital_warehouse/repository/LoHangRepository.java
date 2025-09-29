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

@Repository
public interface LoHangRepository extends JpaRepository<LoHang, Long>,
        JpaSpecificationExecutor<LoHang> {

    Optional<LoHang> findByHangHoaIdAndSoLo(Long hangHoaId, String soLo);

    List<LoHang> findByHangHoaId(Long hangHoaId);

    List<LoHang> findByTrangThai(LoHang.TrangThaiLoHang trangThai);

    @Query("SELECT l FROM LoHang l WHERE l.hanSuDung <= :date " +
            "AND l.trangThai = :trangThai AND l.soLuongHienTai > 0")
    List<LoHang> findByHanSuDungBeforeAndTrangThai(
            @Param("date") LocalDate date,
            @Param("trangThai") LoHang.TrangThaiLoHang trangThai);

    @Query("SELECT l FROM LoHang l WHERE l.hanSuDung BETWEEN :startDate AND :endDate " +
            "AND l.soLuongHienTai > 0 ORDER BY l.hanSuDung ASC")
    List<LoHang> findExpiringSoon(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM LoHang l WHERE l.soLuongHienTai <= 0")
    List<LoHang> findEmptyLoHang();
}