package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.DonViTinhDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.DonViTinh;
import com.hospital.warehouse.hospital_warehouse.repository.DonViTinhRepository;
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
public class DonViTinhService {

    private final DonViTinhRepository donViTinhRepository;

    @Transactional(readOnly = true)
    public PageResponse<DonViTinhDTO> getAllDonViTinh(String search, Pageable pageable) {
        log.info("Tìm kiếm đơn vị tính với từ khóa: {}", search);

        Page<DonViTinh> page = donViTinhRepository.searchDonViTinh(search, pageable);

        List<DonViTinhDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<DonViTinhDTO>builder()
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
    public List<DonViTinhDTO> getAllDonViTinh() {
        log.info("Lấy tất cả đơn vị tính");
        return donViTinhRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DonViTinhDTO> getDonViTinhById(Long id) {
        log.info("Tìm đơn vị tính theo ID: {}", id);
        return donViTinhRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public DonViTinhDTO createDonViTinh(DonViTinhDTO dto) {
        log.info("Tạo đơn vị tính mới: {}", dto.getTenDvt());

        // Validate unique constraints
        if (donViTinhRepository.existsByMaDvt(dto.getMaDvt())) {
            throw new IllegalStateException("Mã đơn vị tính đã tồn tại: " + dto.getMaDvt());
        }

        if (donViTinhRepository.existsByTenDvt(dto.getTenDvt())) {
            throw new IllegalStateException("Tên đơn vị tính đã tồn tại: " + dto.getTenDvt());
        }

        DonViTinh entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());

        DonViTinh saved = donViTinhRepository.save(entity);
        log.info("Đã tạo đơn vị tính với ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public DonViTinhDTO updateDonViTinh(Long id, DonViTinhDTO dto) {
        log.info("Cập nhật đơn vị tính ID: {}", id);

        DonViTinh existing = donViTinhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị tính với ID: " + id));

        // Validate unique constraints (exclude current record)
        donViTinhRepository.findByMaDvt(dto.getMaDvt())
                .ifPresent(dvt -> {
                    if (!dvt.getId().equals(id)) {
                        throw new IllegalStateException("Mã đơn vị tính đã tồn tại: " + dto.getMaDvt());
                    }
                });

        donViTinhRepository.findByTenDvt(dto.getTenDvt())
                .ifPresent(dvt -> {
                    if (!dvt.getId().equals(id)) {
                        throw new IllegalStateException("Tên đơn vị tính đã tồn tại: " + dto.getTenDvt());
                    }
                });

        // Update fields
        existing.setMaDvt(dto.getMaDvt());
        existing.setTenDvt(dto.getTenDvt());
        existing.setMoTa(dto.getMoTa());

        DonViTinh updated = donViTinhRepository.save(existing);
        log.info("Đã cập nhật đơn vị tính ID: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDonViTinh(Long id) {
        log.info("Xóa đơn vị tính ID: {}", id);

        DonViTinh existing = donViTinhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị tính với ID: " + id));

        // Check if DVT is being used by products (add validation logic here)
        // For now, allow deletion
        donViTinhRepository.delete(existing);

        log.info("Đã xóa đơn vị tính ID: {}", id);
    }

    private DonViTinhDTO convertToDTO(DonViTinh entity) {
        return DonViTinhDTO.builder()
                .id(entity.getId())
                .maDvt(entity.getMaDvt())
                .tenDvt(entity.getTenDvt())
                .moTa(entity.getMoTa())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private DonViTinh convertToEntity(DonViTinhDTO dto) {
        return DonViTinh.builder()
                .maDvt(dto.getMaDvt())
                .tenDvt(dto.getTenDvt())
                .moTa(dto.getMoTa())
                .build();
    }
}