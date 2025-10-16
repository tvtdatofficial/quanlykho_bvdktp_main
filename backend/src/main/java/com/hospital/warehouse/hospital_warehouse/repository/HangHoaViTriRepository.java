package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.HangHoaViTri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HangHoaViTriRepository extends JpaRepository<HangHoaViTri, Long> {

    Optional<HangHoaViTri> findByHangHoaIdAndViTriKhoIdAndLoHangId(
            Long hangHoaId, Long viTriKhoId, Long loHangId);

    List<HangHoaViTri> findByHangHoaId(Long hangHoaId);

    List<HangHoaViTri> findByViTriKhoId(Long viTriKhoId);

    @Query("SELECT h FROM HangHoaViTri h WHERE h.hangHoa.id = :hangHoaId " +
            "AND h.soLuong > 0 ORDER BY h.loHang.hanSuDung ASC")
    List<HangHoaViTri> findAvailableByHangHoaId(@Param("hangHoaId") Long hangHoaId);


    Long countByViTriKhoId(Long viTriKhoId);

    void deleteByViTriKhoId(Long viTriKhoId);


    @Query("SELECT COALESCE(SUM(hhvt.soLuong), 0) " +
            "FROM HangHoaViTri hhvt WHERE hhvt.viTriKho.id = :viTriKhoId")
    Optional<Integer> sumSoLuongByViTriKhoId(@Param("viTriKhoId") Long viTriKhoId);




    List<HangHoaViTri> findByHangHoaIdAndLoHangId(Long hangHoaId, Long loHangId);

}