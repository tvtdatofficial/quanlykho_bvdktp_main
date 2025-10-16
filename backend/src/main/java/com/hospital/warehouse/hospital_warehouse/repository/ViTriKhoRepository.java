package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.ViTriKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViTriKhoRepository extends JpaRepository<ViTriKho, Long>,
        JpaSpecificationExecutor<ViTriKho> {

    Optional<ViTriKho> findByKhoIdAndMaViTri(Long khoId, String maViTri);

    List<ViTriKho> findByKhoId(Long khoId);

    List<ViTriKho> findByViTriChaId(Long viTriChaId);

    @Query("SELECT v FROM ViTriKho v WHERE v.viTriCha IS NULL AND v.kho.id = :khoId")
    List<ViTriKho> findRootViTriByKhoId(@Param("khoId") Long khoId);

    List<ViTriKho> findByTrangThai(ViTriKho.TrangThaiViTri trangThai);

    @Query("SELECT v FROM ViTriKho v WHERE v.kho.id = :khoId " +
            "AND v.trangThai = :trangThai")
    List<ViTriKho> findByKhoIdAndTrangThai(
            @Param("khoId") Long khoId,
            @Param("trangThai") ViTriKho.TrangThaiViTri trangThai);


    List<ViTriKho> findByKhoIdAndTrangThaiIn(
            Long khoId,
            List<ViTriKho.TrangThaiViTri> trangThaiList
    );
}

