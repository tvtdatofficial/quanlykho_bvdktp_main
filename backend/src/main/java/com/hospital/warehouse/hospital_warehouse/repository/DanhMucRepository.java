package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.DanhMuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {

    Optional<DanhMuc> findByMaDanhMuc(String maDanhMuc);

    boolean existsByMaDanhMuc(String maDanhMuc);

    List<DanhMuc> findByDanhMucChaIsNull(); // Root categories

    List<DanhMuc> findByDanhMucCha(DanhMuc danhMucCha); // Child categories

    List<DanhMuc> findByDanhMucChaId(Long danhMucChaId);

    List<DanhMuc> findByLoaiDanhMuc(DanhMuc.LoaiDanhMuc loaiDanhMuc);

    List<DanhMuc> findByTrangThai(DanhMuc.TrangThaiDanhMuc trangThai);

    List<DanhMuc> findByDanhMucChaIsNullAndTrangThai(DanhMuc.TrangThaiDanhMuc trangThai);

    @Query("SELECT dm FROM DanhMuc dm WHERE " +
            "dm.trangThai = :trangThai AND " +
            "(:loaiDanhMuc IS NULL OR dm.loaiDanhMuc = :loaiDanhMuc) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(dm.tenDanhMuc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dm.maDanhMuc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DanhMuc> findByTrangThaiAndLoaiDanhMucAndSearch(
            @Param("trangThai") DanhMuc.TrangThaiDanhMuc trangThai,
            @Param("loaiDanhMuc") DanhMuc.LoaiDanhMuc loaiDanhMuc,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT dm FROM DanhMuc dm WHERE dm.danhMucCha IS NULL ORDER BY dm.thuTuSapXep, dm.tenDanhMuc")
    List<DanhMuc> findRootCategoriesOrdered();

    @Query("SELECT COUNT(dm) FROM DanhMuc dm WHERE dm.danhMucCha.id = :danhMucChaId")
    long countByDanhMucChaId(@Param("danhMucChaId") Long danhMucChaId);
}