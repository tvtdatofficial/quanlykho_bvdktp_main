package com.hospital.warehouse.hospital_warehouse.repository;

import com.hospital.warehouse.hospital_warehouse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTenDangNhap(String tenDangNhap);
    Optional<User> findByEmail(String email);

    Optional<User> findByMaUser(String maUser);
    boolean existsByMaUser(String maUser);


    Page<User> findByTenDangNhapContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String tenDangNhap, String email, Pageable pageable);
}