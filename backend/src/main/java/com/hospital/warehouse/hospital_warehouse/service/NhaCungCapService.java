package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.NhaCungCapDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.NhaCungCap;
import com.hospital.warehouse.hospital_warehouse.repository.NhaCungCapRepository;
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
public class NhaCungCapService {

    private final NhaCungCapRepository nhaCungCapRepository;

    @Transactional(readOnly = true)
    public PageResponse<NhaCungCapDTO> getAllNhaCungCap(String search, NhaCungCap.TrangThaiNcc trangThai, Pageable pageable) {
        log.info("Tìm kiếm nhà cung cấp với từ khóa: {}, trạng thái: {}", search, trangThai);

        Page<NhaCungCap> page = nhaCungCapRepository.findByTrangThaiAndSearch(trangThai, search, pageable);

        List<NhaCungCapDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<NhaCungCapDTO>builder()
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
    public Optional<NhaCungCapDTO> getNhaCungCapById(Long id) {
        log.info("Tìm nhà cung cấp theo ID: {}", id);
        return nhaCungCapRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<NhaCungCapDTO> getNhaCungCapByMa(String maNcc) {
        log.info("Tìm nhà cung cấp theo mã: {}", maNcc);
        return nhaCungCapRepository.findByMaNcc(maNcc)
                .map(this::convertToDTO);
    }

    @Transactional
    public NhaCungCapDTO createNhaCungCap(NhaCungCapDTO dto) {
        log.info("Tạo nhà cung cấp mới: {}", dto.getTenNcc());

        // Validate unique constraints
        if (nhaCungCapRepository.existsByMaNcc(dto.getMaNcc())) {
            throw new IllegalStateException("Mã nhà cung cấp đã tồn tại: " + dto.getMaNcc());
        }

        if (dto.getEmail() != null && !dto.getEmail().isEmpty() &&
                nhaCungCapRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email nhà cung cấp đã tồn tại: " + dto.getEmail());
        }

        NhaCungCap entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        NhaCungCap saved = nhaCungCapRepository.save(entity);
        log.info("Đã tạo nhà cung cấp với ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public NhaCungCapDTO updateNhaCungCap(Long id, NhaCungCapDTO dto) {
        log.info("Cập nhật nhà cung cấp ID: {}", id);

        NhaCungCap existing = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + id));

        // Validate unique constraints (exclude current record)
        nhaCungCapRepository.findByMaNcc(dto.getMaNcc())
                .ifPresent(ncc -> {
                    if (!ncc.getId().equals(id)) {
                        throw new IllegalStateException("Mã nhà cung cấp đã tồn tại: " + dto.getMaNcc());
                    }
                });

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            nhaCungCapRepository.findByEmail(dto.getEmail())
                    .ifPresent(ncc -> {
                        if (!ncc.getId().equals(id)) {
                            throw new IllegalStateException("Email nhà cung cấp đã tồn tại: " + dto.getEmail());
                        }
                    });
        }

        // Update fields
        existing.setMaNcc(dto.getMaNcc());
        existing.setTenNcc(dto.getTenNcc());
        existing.setDiaChi(dto.getDiaChi());
        existing.setSoDienThoai(dto.getSoDienThoai());
        existing.setEmail(dto.getEmail());
        existing.setWebsite(dto.getWebsite());
        existing.setMaSoThue(dto.getMaSoThue());
        existing.setNguoiLienHe(dto.getNguoiLienHe());
        existing.setSdtLienHe(dto.getSdtLienHe());
        existing.setEmailLienHe(dto.getEmailLienHe());
        existing.setDiemDanhGia(dto.getDiemDanhGia());
        existing.setGhiChu(dto.getGhiChu());
        existing.setTrangThai(dto.getTrangThai());
        existing.setUpdatedAt(LocalDateTime.now());

        NhaCungCap updated = nhaCungCapRepository.save(existing);
        log.info("Đã cập nhật nhà cung cấp ID: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteNhaCungCap(Long id) {
        log.info("Xóa nhà cung cấp ID: {}", id);

        NhaCungCap existing = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + id));

        // Check if NCC is being used (add validation logic here)
        // For now, just soft delete by changing status
        existing.setTrangThai(NhaCungCap.TrangThaiNcc.NGUNG_HOP_TAC);
        existing.setUpdatedAt(LocalDateTime.now());
        nhaCungCapRepository.save(existing);

        log.info("Đã ngừng hợp tác với nhà cung cấp ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<NhaCungCapDTO> getNhaCungCapActive() {
        log.info("Lấy danh sách nhà cung cấp đang hoạt động");
        return nhaCungCapRepository.findByTrangThai(NhaCungCap.TrangThaiNcc.HOAT_DONG)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private NhaCungCapDTO convertToDTO(NhaCungCap entity) {
        return NhaCungCapDTO.builder()
                .id(entity.getId())
                .maNcc(entity.getMaNcc())
                .tenNcc(entity.getTenNcc())
                .diaChi(entity.getDiaChi())
                .soDienThoai(entity.getSoDienThoai())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .maSoThue(entity.getMaSoThue())
                .nguoiLienHe(entity.getNguoiLienHe())
                .sdtLienHe(entity.getSdtLienHe())
                .emailLienHe(entity.getEmailLienHe())
                .diemDanhGia(entity.getDiemDanhGia())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private NhaCungCap convertToEntity(NhaCungCapDTO dto) {
        return NhaCungCap.builder()
                .maNcc(dto.getMaNcc())
                .tenNcc(dto.getTenNcc())
                .diaChi(dto.getDiaChi())
                .soDienThoai(dto.getSoDienThoai())
                .email(dto.getEmail())
                .website(dto.getWebsite())
                .maSoThue(dto.getMaSoThue())
                .nguoiLienHe(dto.getNguoiLienHe())
                .sdtLienHe(dto.getSdtLienHe())
                .emailLienHe(dto.getEmailLienHe())
                .diemDanhGia(dto.getDiemDanhGia())
                .ghiChu(dto.getGhiChu())
                .trangThai(dto.getTrangThai())
                .build();
    }
}