package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.DanhMucDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.DanhMuc;
import com.hospital.warehouse.hospital_warehouse.repository.DanhMucRepository;
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
public class DanhMucService {

    private final DanhMucRepository danhMucRepository;

    @Transactional(readOnly = true)
    public PageResponse<DanhMucDTO> getAllDanhMuc(String search, DanhMuc.LoaiDanhMuc loaiDanhMuc, Pageable pageable) {
        log.info("Tìm kiếm danh mục với từ khóa: {}, loại: {}", search, loaiDanhMuc);

        Page<DanhMuc> page = danhMucRepository.findByTrangThaiAndLoaiDanhMucAndSearch(
                DanhMuc.TrangThaiDanhMuc.HOAT_DONG, loaiDanhMuc, search, pageable);

        List<DanhMucDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<DanhMucDTO>builder()
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
    public List<DanhMucDTO.DanhMucTreeDTO> getDanhMucTree() {
        log.info("Lấy cây danh mục");
        List<DanhMuc> rootCategories = danhMucRepository.findRootCategoriesOrdered();
        return rootCategories.stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DanhMucDTO> getRootCategories() {
        log.info("Lấy danh mục gốc");
        return danhMucRepository.findByDanhMucChaIsNullAndTrangThai(DanhMuc.TrangThaiDanhMuc.HOAT_DONG)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DanhMucDTO> getChildCategories(Long danhMucChaId) {
        log.info("Lấy danh mục con của: {}", danhMucChaId);
        return danhMucRepository.findByDanhMucChaId(danhMucChaId)
                .stream()
                .filter(dm -> dm.getTrangThai() == DanhMuc.TrangThaiDanhMuc.HOAT_DONG)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DanhMucDTO> getDanhMucById(Long id) {
        log.info("Tìm danh mục theo ID: {}", id);
        return danhMucRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public DanhMucDTO createDanhMuc(DanhMucDTO dto) {
        log.info("Tạo danh mục mới: {}", dto.getTenDanhMuc());

        // Validate unique constraint
        if (danhMucRepository.existsByMaDanhMuc(dto.getMaDanhMuc())) {
            throw new IllegalStateException("Mã danh mục đã tồn tại: " + dto.getMaDanhMuc());
        }

        DanhMuc entity = convertToEntity(dto);

        // Set parent category if provided
        if (dto.getDanhMucChaId() != null) {
            DanhMuc parent = danhMucRepository.findById(dto.getDanhMucChaId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với ID: " + dto.getDanhMucChaId()));
            entity.setDanhMucCha(parent);
        }

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        DanhMuc saved = danhMucRepository.save(entity);
        log.info("Đã tạo danh mục với ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public DanhMucDTO updateDanhMuc(Long id, DanhMucDTO dto) {
        log.info("Cập nhật danh mục ID: {}", id);

        DanhMuc existing = danhMucRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        // Validate unique constraint (exclude current record)
        danhMucRepository.findByMaDanhMuc(dto.getMaDanhMuc())
                .ifPresent(dm -> {
                    if (!dm.getId().equals(id)) {
                        throw new IllegalStateException("Mã danh mục đã tồn tại: " + dto.getMaDanhMuc());
                    }
                });

        // Prevent circular reference
        if (dto.getDanhMucChaId() != null) {
            if (dto.getDanhMucChaId().equals(id)) {
                throw new IllegalStateException("Danh mục không thể là cha của chính nó");
            }
            // Additional check for deeper circular references would go here
        }

        // Update fields
        existing.setMaDanhMuc(dto.getMaDanhMuc());
        existing.setTenDanhMuc(dto.getTenDanhMuc());
        existing.setMoTa(dto.getMoTa());
        existing.setLoaiDanhMuc(dto.getLoaiDanhMuc());
        existing.setThuTuSapXep(dto.getThuTuSapXep());
        existing.setTrangThai(dto.getTrangThai());
        existing.setUpdatedAt(LocalDateTime.now());

        // Update parent category
        if (dto.getDanhMucChaId() != null) {
            DanhMuc parent = danhMucRepository.findById(dto.getDanhMucChaId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với ID: " + dto.getDanhMucChaId()));
            existing.setDanhMucCha(parent);
        } else {
            existing.setDanhMucCha(null);
        }

        DanhMuc updated = danhMucRepository.save(existing);
        log.info("Đã cập nhật danh mục ID: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDanhMuc(Long id) {
        log.info("Xóa danh mục ID: {}", id);

        DanhMuc existing = danhMucRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));

        // Check if category has children
        long childCount = danhMucRepository.countByDanhMucChaId(id);
        if (childCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục có danh mục con");
        }

        // Check if category is being used by products (add validation logic here)
        // For now, just soft delete by changing status
        existing.setTrangThai(DanhMuc.TrangThaiDanhMuc.NGUNG_HOAT_DONG);
        existing.setUpdatedAt(LocalDateTime.now());
        danhMucRepository.save(existing);

        log.info("Đã ngừng hoạt động danh mục ID: {}", id);
    }

    private DanhMucDTO convertToDTO(DanhMuc entity) {
        return DanhMucDTO.builder()
                .id(entity.getId())
                .maDanhMuc(entity.getMaDanhMuc())
                .tenDanhMuc(entity.getTenDanhMuc())
                .danhMucChaId(entity.getDanhMucCha() != null ? entity.getDanhMucCha().getId() : null)
                .tenDanhMucCha(entity.getDanhMucCha() != null ? entity.getDanhMucCha().getTenDanhMuc() : null)
                .moTa(entity.getMoTa())
                .loaiDanhMuc(entity.getLoaiDanhMuc())
                .thuTuSapXep(entity.getThuTuSapXep())
                .trangThai(entity.getTrangThai())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .hasChildren(entity.hasChildren())
                .isRootCategory(entity.isRootCategory())
                .build();
    }

    private DanhMuc convertToEntity(DanhMucDTO dto) {
        return DanhMuc.builder()
                .maDanhMuc(dto.getMaDanhMuc())
                .tenDanhMuc(dto.getTenDanhMuc())
                .moTa(dto.getMoTa())
                .loaiDanhMuc(dto.getLoaiDanhMuc())
                .thuTuSapXep(dto.getThuTuSapXep())
                .trangThai(dto.getTrangThai())
                .build();
    }

    private DanhMucDTO.DanhMucTreeDTO convertToTreeDTO(DanhMuc entity) {
        DanhMucDTO.DanhMucTreeDTO dto = new DanhMucDTO.DanhMucTreeDTO();
        dto.setId(entity.getId());
        dto.setMaDanhMuc(entity.getMaDanhMuc());
        dto.setTenDanhMuc(entity.getTenDanhMuc());
        dto.setDanhMucChaId(entity.getDanhMucCha() != null ? entity.getDanhMucCha().getId() : null);
        dto.setLevel(calculateLevel(entity));
        dto.setHasChildren(entity.hasChildren());

        if (entity.hasChildren()) {
            dto.setChildren(entity.getDanhMucCon().stream()
                    .filter(child -> child.getTrangThai() == DanhMuc.TrangThaiDanhMuc.HOAT_DONG)
                    .map(this::convertToTreeDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private int calculateLevel(DanhMuc entity) {
        int level = 0;
        DanhMuc parent = entity.getDanhMucCha();
        while (parent != null) {
            level++;
            parent = parent.getDanhMucCha();
        }
        return level;
    }
}
