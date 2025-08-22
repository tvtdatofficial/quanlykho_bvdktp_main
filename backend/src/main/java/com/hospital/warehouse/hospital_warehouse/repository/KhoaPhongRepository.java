package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KhoaPhongRepository extends JpaRepository<KhoaPhong, Long> {
    Optional<KhoaPhong> findByMaKhoaPhong(String maKhoaPhong);
}