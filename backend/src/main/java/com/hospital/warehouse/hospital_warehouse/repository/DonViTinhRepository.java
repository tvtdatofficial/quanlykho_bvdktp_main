package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.DonViTinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonViTinhRepository extends JpaRepository<DonViTinh, Long> {

    Optional<DonViTinh> findByMaDvt(String maDvt);

    Optional<DonViTinh> findByTenDvt(String tenDvt);  // <- THÊM DÒNG NÀY

    boolean existsByMaDvt(String maDvt);

    boolean existsByTenDvt(String tenDvt);

    @Query("SELECT dvt FROM DonViTinh dvt WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(dvt.tenDvt) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dvt.maDvt) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DonViTinh> searchDonViTinh(@Param("search") String search, Pageable pageable);
}