package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.ViTriKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.*;
import com.hospital.warehouse.hospital_warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViTriKhoService {

    private final ViTriKhoRepository viTriKhoRepository;
    private final KhoRepository khoRepository;
    private final HangHoaViTriRepository hangHoaViTriRepository;

    /**
     * Lấy danh sách vị trí kho có phân trang và lọc
     */
    @Transactional(readOnly = true)
    public PageResponse<ViTriKhoDTO> getAllViTriKho(
            String search,
            Long khoId,
            ViTriKho.LoaiViTri loaiViTri,
            ViTriKho.TrangThaiViTri trangThai,
            Pageable pageable) {

        Specification<ViTriKho> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo mã hoặc tên vị trí
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("maViTri")), searchPattern),
                        cb.like(cb.lower(root.get("tenViTri")), searchPattern)
                ));
            }

            // Lọc theo kho
            if (khoId != null) {
                predicates.add(cb.equal(root.get("kho").get("id"), khoId));
            }

            // Lọc theo loại vị trí
            if (loaiViTri != null) {
                predicates.add(cb.equal(root.get("loaiViTri"), loaiViTri));
            }

            // Lọc theo trạng thái
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ViTriKho> page = viTriKhoRepository.findAll(spec, pageable);

        List<ViTriKhoDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<ViTriKhoDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Lấy cây vị trí kho theo kho
     */
    @Transactional(readOnly = true)
    public List<ViTriKhoDTO> getViTriKhoTree(Long khoId) {
        List<ViTriKho> rootViTris = viTriKhoRepository.findRootViTriByKhoId(khoId);
        return rootViTris.stream()
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết vị trí kho
     */
    @Transactional(readOnly = true)
    public Optional<ViTriKhoDTO> getViTriKhoById(Long id) {
        return viTriKhoRepository.findById(id)
                .map(this::convertToDTOWithChildren);
    }

    /**
     * Tạo vị trí kho mới
     */
    @Transactional
    public ViTriKhoDTO createViTriKho(ViTriKhoDTO dto) {
        log.info("Creating vi tri kho: {}", dto);

        // Validate
        validateViTriKhoData(dto);

        Kho kho = khoRepository.findById(dto.getKhoId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho"));

        // Kiểm tra trùng mã vị trí trong cùng kho
        Optional<ViTriKho> existing = viTriKhoRepository.findByKhoIdAndMaViTri(
                dto.getKhoId(), dto.getMaViTri());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Mã vị trí đã tồn tại trong kho này");
        }

        ViTriKho viTriKho = new ViTriKho();
        viTriKho.setKho(kho);
        viTriKho.setMaViTri(dto.getMaViTri());
        viTriKho.setTenViTri(dto.getTenViTri());
        viTriKho.setLoaiViTri(dto.getLoaiViTri());
        viTriKho.setMoTa(dto.getMoTa());
        viTriKho.setSucChuaToiDa(dto.getSucChuaToiDa());
        viTriKho.setTrongLuongToiDa(dto.getTrongLuongToiDa());
        viTriKho.setNhietDoYeuCau(dto.getNhietDoYeuCau());
        viTriKho.setTrangThai(dto.getTrangThai() != null ?
                dto.getTrangThai() : ViTriKho.TrangThaiViTri.TRONG);

        // Set vị trí cha nếu có
        if (dto.getViTriChaId() != null) {
            ViTriKho viTriCha = viTriKhoRepository.findById(dto.getViTriChaId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí cha"));
            viTriKho.setViTriCha(viTriCha);
        }

        viTriKho = viTriKhoRepository.save(viTriKho);
        log.info("Created vi tri kho successfully with ID: {}", viTriKho.getId());

        return convertToDTO(viTriKho);
    }

    /**
     * Cập nhật vị trí kho
     */
    @Transactional
    public ViTriKhoDTO updateViTriKho(Long id, ViTriKhoDTO dto) {
        ViTriKho viTriKho = viTriKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí kho"));

        // Kiểm tra trùng mã nếu thay đổi
        if (!viTriKho.getMaViTri().equals(dto.getMaViTri())) {
            Optional<ViTriKho> existing = viTriKhoRepository.findByKhoIdAndMaViTri(
                    viTriKho.getKho().getId(), dto.getMaViTri());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Mã vị trí đã tồn tại trong kho này");
            }
            viTriKho.setMaViTri(dto.getMaViTri());
        }

        viTriKho.setTenViTri(dto.getTenViTri());
        viTriKho.setLoaiViTri(dto.getLoaiViTri());
        viTriKho.setMoTa(dto.getMoTa());
        viTriKho.setSucChuaToiDa(dto.getSucChuaToiDa());
        viTriKho.setTrongLuongToiDa(dto.getTrongLuongToiDa());
        viTriKho.setNhietDoYeuCau(dto.getNhietDoYeuCau());

        if (dto.getTrangThai() != null) {
            viTriKho.setTrangThai(dto.getTrangThai());
        }

        // Cập nhật vị trí cha nếu có thay đổi
        if (dto.getViTriChaId() != null &&
                !dto.getViTriChaId().equals(id)) { // Tránh vòng lặp
            ViTriKho viTriCha = viTriKhoRepository.findById(dto.getViTriChaId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí cha"));
            viTriKho.setViTriCha(viTriCha);
        }

        return convertToDTO(viTriKhoRepository.save(viTriKho));
    }

    /**
     * Xóa vị trí kho
     */
    @Transactional
    public void deleteViTriKho(Long id) {
        ViTriKho viTriKho = viTriKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí kho"));

        // Kiểm tra có vị trí con không
        List<ViTriKho> viTriCon = viTriKhoRepository.findByViTriChaId(id);
        if (!viTriCon.isEmpty()) {
            throw new IllegalStateException("Không thể xóa vị trí có vị trí con");
        }

        // Kiểm tra có hàng hóa không
        long soLuongHangHoa = hangHoaViTriRepository.countByViTriKhoId(id);
        if (soLuongHangHoa > 0) {
            throw new IllegalStateException("Không thể xóa vị trí đang chứa hàng hóa");
        }

        viTriKhoRepository.deleteById(id);
        log.info("Deleted vi tri kho ID: {}", id);
    }

    /**
     * Lấy danh sách vị trí trống
     */
    @Transactional(readOnly = true)
    public List<ViTriKhoDTO> getViTriTrong(Long khoId) {
        List<ViTriKho> list = viTriKhoRepository.findByKhoIdAndTrangThai(
                khoId, ViTriKho.TrangThaiViTri.TRONG);
        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void validateViTriKhoData(ViTriKhoDTO dto) {
        if (dto.getKhoId() == null) {
            throw new IllegalArgumentException("Kho không được để trống");
        }
        if (dto.getMaViTri() == null || dto.getMaViTri().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vị trí không được để trống");
        }
    }

    private ViTriKhoDTO convertToDTO(ViTriKho entity) {
        // Tính toán thông tin sử dụng
        Integer soLuongHienTai = hangHoaViTriRepository.countByViTriKhoId(entity.getId()).intValue();
        Integer phanTramSuDung = 0;
        Boolean dangDay = false;

        if (entity.getSucChuaToiDa() != null && entity.getSucChuaToiDa() > 0) {
            phanTramSuDung = (soLuongHienTai * 100) / entity.getSucChuaToiDa();
            dangDay = phanTramSuDung >= 90;
        }

        return ViTriKhoDTO.builder()
                .id(entity.getId())
                .khoId(entity.getKho().getId())
                .tenKho(entity.getKho().getTenKho())
                .maViTri(entity.getMaViTri())
                .tenViTri(entity.getTenViTri())
                .loaiViTri(entity.getLoaiViTri())
                .viTriChaId(entity.getViTriCha() != null ? entity.getViTriCha().getId() : null)
                .tenViTriCha(entity.getViTriCha() != null ? entity.getViTriCha().getTenViTri() : null)
                .moTa(entity.getMoTa())
                .sucChuaToiDa(entity.getSucChuaToiDa())
                .trongLuongToiDa(entity.getTrongLuongToiDa())
                .nhietDoYeuCau(entity.getNhietDoYeuCau())
                .trangThai(entity.getTrangThai())
                .soLuongHienTai(soLuongHienTai)
                .phanTramSuDung(phanTramSuDung)
                .dangDay(dangDay)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ViTriKhoDTO convertToDTOWithChildren(ViTriKho entity) {
        ViTriKhoDTO dto = convertToDTO(entity);

        // Lấy danh sách vị trí con
        if (entity.getViTriCon() != null && !entity.getViTriCon().isEmpty()) {
            List<ViTriKhoDTO> viTriConDTOs = entity.getViTriCon().stream()
                    .map(this::convertToDTOWithChildren)
                    .collect(Collectors.toList());
            dto.setViTriCon(viTriConDTOs);
        }

        return dto;
    }
}