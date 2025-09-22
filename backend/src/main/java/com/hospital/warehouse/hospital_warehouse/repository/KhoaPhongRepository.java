package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhoaPhongRepository extends JpaRepository<KhoaPhong, Long> {

    Optional<KhoaPhong> findByMaKhoaPhong(String maKhoaPhong);

    List<KhoaPhong> findByTrangThai(KhoaPhong.TrangThaiKhoaPhong trangThai);

    boolean existsByMaKhoaPhong(String maKhoaPhong);

    boolean existsByTenKhoaPhong(String tenKhoaPhong);
}