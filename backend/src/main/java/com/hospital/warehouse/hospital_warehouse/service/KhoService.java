package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.KhoDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.Kho;
import com.hospital.warehouse.hospital_warehouse.entity.KhoaPhong;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.repository.KhoRepository;
import com.hospital.warehouse.hospital_warehouse.repository.KhoaPhongRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhoService {

    private final KhoRepository khoRepository;
    private final KhoaPhongRepository khoaPhongRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<KhoDTO> getAllKho(String search, Kho.LoaiKho loaiKho, Pageable pageable) {
        log.info("Tìm kiếm kho với từ khóa: {}, loại: {}", search, loaiKho);

        Page<Kho> page = khoRepository.findByLoaiKhoAndSearch(loaiKho, search, pageable);

        List<KhoDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<KhoDTO>builder()
                .content(dtoList)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Transactional(readOnly = true)
    public List<KhoDTO> getKhoActive() {
        log.info("Lấy danh sách kho đang hoạt động");
        return khoRepository.findByTrangThai(Kho.TrangThaiKho.HOAT_DONG)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KhoDTO> getKhoByLoaiKho(Kho.LoaiKho loaiKho) {
        log.info("Lấy danh sách kho theo loại: {}", loaiKho);
        return khoRepository.findActiveByLoaiKho(loaiKho)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KhoDTO> getKhoByKhoaPhong(Long khoaPhongId) {
        log.info("Lấy danh sách kho theo khoa phòng: {}", khoaPhongId);
        return khoRepository.findByKhoaPhongId(khoaPhongId)
                .stream()
                .filter(kho -> kho.getTrangThai() == Kho.TrangThaiKho.HOAT_DONG)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<KhoDTO> getKhoById(Long id) {
        log.info("Tìm kho theo ID: {}", id);
        return khoRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<KhoDTO> getKhoByMa(String maKho) {
        log.info("Tìm kho theo mã: {}", maKho);
        return khoRepository.findByMaKho(maKho)
                .map(this::convertToDTO);
    }

    @Transactional
    public KhoDTO createKho(KhoDTO dto) {
        log.info("Tạo kho mới: {}", dto.getTenKho());

        // Validate unique constraints
        if (khoRepository.existsByMaKho(dto.getMaKho())) {
            throw new IllegalStateException("Mã kho đã tồn tại: " + dto.getMaKho());
        }

        Kho entity = convertToEntity(dto);
        setEntityRelationships(entity, dto);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        Kho saved = khoRepository.save(entity);
        log.info("Đã tạo kho với ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public KhoDTO updateKho(Long id, KhoDTO dto) {
        log.info("Cập nhật kho ID: {}", id);

        Kho existing = khoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho với ID: " + id));

        // Validate unique constraint (exclude current record)
        khoRepository.findByMaKho(dto.getMaKho())
                .ifPresent(k -> {
                    if (!k.getId().equals(id)) {
                        throw new IllegalStateException("Mã kho đã tồn tại: " + dto.getMaKho());
                    }
                });

        updateEntityFromDTO(existing, dto);
        setEntityRelationships(existing, dto);
        existing.setUpdatedAt(LocalDateTime.now());

        Kho updated = khoRepository.save(existing);
        log.info("Đã cập nhật kho ID: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteKho(Long id) {
        log.info("Xóa kho ID: {}", id);

        Kho existing = khoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho với ID: " + id));

        // Check if warehouse is being used (add validation logic here)
        // For now, just soft delete by changing status
        existing.setTrangThai(Kho.TrangThaiKho.DONG_CUA);
        existing.setUpdatedAt(LocalDateTime.now());
        khoRepository.save(existing);

        log.info("Đã đóng cửa kho ID: {}", id);
    }

    @Transactional
    public void activateKho(Long id) {
        log.info("Kích hoạt kho ID: {}", id);

        Kho existing = khoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho với ID: " + id));

        existing.setTrangThai(Kho.TrangThaiKho.HOAT_DONG);
        existing.setUpdatedAt(LocalDateTime.now());
        khoRepository.save(existing);

        log.info("Đã kích hoạt kho ID: {}", id);
    }

    @Transactional(readOnly = true)
    public long countKhoByTrangThai(Kho.TrangThaiKho trangThai) {
        return khoRepository.countByTrangThai(trangThai);
    }

    private void setEntityRelationships(Kho entity, KhoDTO dto) {
        // Set KhoaPhong (required)
        KhoaPhong khoaPhong = khoaPhongRepository.findById(dto.getKhoaPhongId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoa phòng với ID: " + dto.getKhoaPhongId()));
        entity.setKhoaPhong(khoaPhong);

        // Set QuanLyKho (optional)
        if (dto.getQuanLyKhoId() != null) {
            User quanLyKho = userRepository.findById(dto.getQuanLyKhoId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quản lý kho với ID: " + dto.getQuanLyKhoId()));
            entity.setQuanLyKho(quanLyKho);
        } else {
            entity.setQuanLyKho(null);
        }
    }

    private void updateEntityFromDTO(Kho entity, KhoDTO dto) {
        entity.setMaKho(dto.getMaKho());
        entity.setTenKho(dto.getTenKho());
        entity.setLoaiKho(dto.getLoaiKho());
        entity.setMoTa(dto.getMoTa());
        entity.setDiaChi(dto.getDiaChi());
        entity.setDienTich(dto.getDienTich());
        entity.setNhietDoMin(dto.getNhietDoMin());
        entity.setNhietDoMax(dto.getNhietDoMax());
        entity.setDoAmMin(dto.getDoAmMin());
        entity.setDoAmMax(dto.getDoAmMax());
        if (dto.getTrangThai() != null) {
            entity.setTrangThai(dto.getTrangThai());
        }
    }

    private KhoDTO convertToDTO(Kho entity) {
        return KhoDTO.builder()
                .id(entity.getId())
                .maKho(entity.getMaKho())
                .tenKho(entity.getTenKho())
                .loaiKho(entity.getLoaiKho())
                .moTa(entity.getMoTa())
                .diaChi(entity.getDiaChi())
                .dienTich(entity.getDienTich())
                .nhietDoMin(entity.getNhietDoMin())
                .nhietDoMax(entity.getNhietDoMax())
                .doAmMin(entity.getDoAmMin())
                .doAmMax(entity.getDoAmMax())
                .khoaPhongId(entity.getKhoaPhong().getId())
                .tenKhoaPhong(entity.getKhoaPhong().getTenKhoaPhong())
                .quanLyKhoId(entity.getQuanLyKho() != null ? entity.getQuanLyKho().getId() : null)
                .tenQuanLyKho(entity.getQuanLyKho() != null ? entity.getQuanLyKho().getHoTen() : null)
                .trangThai(entity.getTrangThai())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Kho convertToEntity(KhoDTO dto) {
        return Kho.builder()
                .maKho(dto.getMaKho())
                .tenKho(dto.getTenKho())
                .loaiKho(dto.getLoaiKho())
                .moTa(dto.getMoTa())
                .diaChi(dto.getDiaChi())
                .dienTich(dto.getDienTich())
                .nhietDoMin(dto.getNhietDoMin())
                .nhietDoMax(dto.getNhietDoMax())
                .doAmMin(dto.getDoAmMin())
                .doAmMax(dto.getDoAmMax())
                .trangThai(dto.getTrangThai() != null ? dto.getTrangThai() : Kho.TrangThaiKho.HOAT_DONG)
                .build();
    }
}