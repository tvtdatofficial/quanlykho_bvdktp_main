package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.HangHoa;
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
public interface HangHoaRepository extends JpaRepository<HangHoa, Long> {

    Optional<HangHoa> findByMaHangHoa(String maHangHoa);

    Optional<HangHoa> findByMaBarcode(String maBarcode);  // <- THÊM DÒNG NÀY

    boolean existsByMaHangHoa(String maHangHoa);

    boolean existsByMaBarcode(String maBarcode);

    List<HangHoa> findByDanhMuc(DanhMuc danhMuc);

    List<HangHoa> findByDanhMucId(Long danhMucId);

    List<HangHoa> findByTrangThai(HangHoa.TrangThaiHangHoa trangThai);

    @Query("SELECT hh FROM HangHoa hh WHERE hh.soLuongCoTheXuat < hh.soLuongToiThieu AND hh.soLuongToiThieu > 0")
    List<HangHoa> findHangHoaTonKhoThap();

    @Query("SELECT hh FROM HangHoa hh WHERE hh.soLuongCoTheXuat <= 0")
    List<HangHoa> findHangHoaHetHang();

    @Query("SELECT hh FROM HangHoa hh WHERE " +
            "hh.trangThai = :trangThai AND " +
            "(:danhMucId IS NULL OR hh.danhMuc.id = :danhMucId) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(hh.tenHangHoa) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(hh.maHangHoa) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(hh.tenKhoaHoc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<HangHoa> findByTrangThaiAndDanhMucAndSearch(
            @Param("trangThai") HangHoa.TrangThaiHangHoa trangThai,
            @Param("danhMucId") Long danhMucId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(hh) FROM HangHoa hh WHERE hh.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") HangHoa.TrangThaiHangHoa trangThai);

    @Query("SELECT COUNT(hh) FROM HangHoa hh WHERE hh.danhMuc.id = :danhMucId")
    long countByDanhMucId(@Param("danhMucId") Long danhMucId);
}