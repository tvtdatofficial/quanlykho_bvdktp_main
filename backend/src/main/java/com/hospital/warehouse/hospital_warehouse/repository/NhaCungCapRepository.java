package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.NhaCungCap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhaCungCapRepository extends JpaRepository<NhaCungCap, Long> {

    Optional<NhaCungCap> findByMaNcc(String maNcc);

    Optional<NhaCungCap> findByEmail(String email);  // <- THÊM DÒNG NÀY

    boolean existsByMaNcc(String maNcc);

    boolean existsByEmail(String email);

    List<NhaCungCap> findByTrangThai(NhaCungCap.TrangThaiNcc trangThai);

    @Query("SELECT ncc FROM NhaCungCap ncc WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(ncc.tenNcc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ncc.maNcc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ncc.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ncc.soDienThoai) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<NhaCungCap> searchNhaCungCap(@Param("search") String search, Pageable pageable);

    @Query("SELECT ncc FROM NhaCungCap ncc WHERE " +
            "(:trangThai IS NULL OR ncc.trangThai = :trangThai) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(ncc.tenNcc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ncc.maNcc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<NhaCungCap> findByTrangThaiAndSearch(@Param("trangThai") NhaCungCap.TrangThaiNcc trangThai,
                                              @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(ncc) FROM NhaCungCap ncc WHERE ncc.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") NhaCungCap.TrangThaiNcc trangThai);
}