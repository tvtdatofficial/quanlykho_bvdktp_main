package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.KhoaPhongDTO;
import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import com.hospital.warehouse.hospital_warehouse.repository.KhoaPhongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhoaPhongService {

    private final KhoaPhongRepository khoaPhongRepository;

    @Transactional(readOnly = true)
    public List<KhoaPhongDTO> getKhoaPhongActive() {
        return khoaPhongRepository.findByTrangThai(KhoaPhong.TrangThaiKhoaPhong.HOAT_DONG)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KhoaPhongDTO> getAllKhoaPhong() {
        return khoaPhongRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private KhoaPhongDTO convertToDTO(KhoaPhong entity) {
        return KhoaPhongDTO.builder()
                .id(entity.getId())
                .maKhoaPhong(entity.getMaKhoaPhong())
                .tenKhoaPhong(entity.getTenKhoaPhong())
                .moTa(entity.getMoTa())
                .diaChi(entity.getDiaChi())
                .soDienThoai(entity.getSoDienThoai())
                .email(entity.getEmail())
                .trangThai(entity.getTrangThai())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}