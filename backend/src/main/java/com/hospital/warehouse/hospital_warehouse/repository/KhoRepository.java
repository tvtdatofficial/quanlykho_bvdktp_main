package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.Kho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhoRepository extends JpaRepository<Kho, Long> {

    Optional<Kho> findByMaKho(String maKho);

    boolean existsByMaKho(String maKho);

    List<Kho> findByTrangThai(Kho.TrangThaiKho trangThai);

    List<Kho> findByLoaiKho(Kho.LoaiKho loaiKho);

    List<Kho> findByKhoaPhongId(Long khoaPhongId);

    @Query("SELECT k FROM Kho k WHERE " +
            "(:loaiKho IS NULL OR k.loaiKho = :loaiKho) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(k.tenKho) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(k.maKho) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Kho> findByLoaiKhoAndSearch(@Param("loaiKho") Kho.LoaiKho loaiKho,
                                     @Param("search") String search, Pageable pageable);

    @Query("SELECT k FROM Kho k WHERE " +
            "k.trangThai = 'HOAT_DONG' AND " +
            "(:loaiKho IS NULL OR k.loaiKho = :loaiKho)")
    List<Kho> findActiveByLoaiKho(@Param("loaiKho") Kho.LoaiKho loaiKho);

    @Query("SELECT COUNT(k) FROM Kho k WHERE k.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") Kho.TrangThaiKho trangThai);

    @Query("SELECT k FROM Kho k WHERE k.quanLyKho.id = :quanLyKhoId")
    List<Kho> findByQuanLyKhoId(@Param("quanLyKhoId") Long quanLyKhoId);
}