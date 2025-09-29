package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.LoHangDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoHangService {

    private final LoHangRepository loHangRepository;
    private final HangHoaRepository hangHoaRepository;
    private final NhaCungCapRepository nhaCungCapRepository;

    /**
     * Lấy danh sách lô hàng có phân trang và lọc
     */
    @Transactional(readOnly = true)
    public PageResponse<LoHangDTO> getAllLoHang(
            String search,
            Long hangHoaId,
            Long nhaCungCapId,
            LoHang.TrangThaiLoHang trangThai,
            LocalDate tuNgay,
            LocalDate denNgay,
            Boolean sapHetHan,
            Pageable pageable) {

        Specification<LoHang> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo số lô hoặc tên hàng hóa
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("soLo")), searchPattern),
                        cb.like(cb.lower(root.get("hangHoa").get("tenHangHoa")), searchPattern)
                ));
            }

            // Lọc theo hàng hóa
            if (hangHoaId != null) {
                predicates.add(cb.equal(root.get("hangHoa").get("id"), hangHoaId));
            }

            // Lọc theo nhà cung cấp
            if (nhaCungCapId != null) {
                predicates.add(cb.equal(root.get("nhaCungCap").get("id"), nhaCungCapId));
            }

            // Lọc theo trạng thái
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }

            // Lọc theo hạn sử dụng
            if (tuNgay != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("hanSuDung"), tuNgay));
            }

            if (denNgay != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hanSuDung"), denNgay));
            }

            // Lọc lô sắp hết hạn (trong vòng 30 ngày)
            if (sapHetHan != null && sapHetHan) {
                LocalDate now = LocalDate.now();
                LocalDate future = now.plusDays(30);
                predicates.add(cb.between(root.get("hanSuDung"), now, future));
                predicates.add(cb.greaterThan(root.get("soLuongHienTai"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LoHang> page = loHangRepository.findAll(spec, pageable);

        List<LoHangDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<LoHangDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Lấy chi tiết lô hàng
     */
    @Transactional(readOnly = true)
    public Optional<LoHangDTO> getLoHangById(Long id) {
        return loHangRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Lấy danh sách lô hàng theo hàng hóa
     */
    @Transactional(readOnly = true)
    public List<LoHangDTO> getLoHangByHangHoa(Long hangHoaId) {
        return loHangRepository.findByHangHoaId(hangHoaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo lô hàng mới
     */
    @Transactional
    public LoHangDTO createLoHang(LoHangDTO dto) {
        log.info("Creating lo hang: {}", dto);

        // Validate
        validateLoHangData(dto);

        HangHoa hangHoa = hangHoaRepository.findById(dto.getHangHoaId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa"));

        // Kiểm tra trùng số lô
        Optional<LoHang> existing = loHangRepository.findByHangHoaIdAndSoLo(
                dto.getHangHoaId(), dto.getSoLo());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Số lô đã tồn tại cho hàng hóa này");
        }

        LoHang loHang = new LoHang();
        loHang.setHangHoa(hangHoa);
        loHang.setSoLo(dto.getSoLo());
        loHang.setNgaySanXuat(dto.getNgaySanXuat());
        loHang.setHanSuDung(dto.getHanSuDung());
        loHang.setSoLuongNhap(dto.getSoLuongNhap());
        loHang.setSoLuongHienTai(dto.getSoLuongNhap()); // Ban đầu bằng số lượng nhập
        loHang.setGiaNhap(dto.getGiaNhap());
        loHang.setSoChungTuNhap(dto.getSoChungTuNhap());
        loHang.setGhiChu(dto.getGhiChu());

        // Set nhà cung cấp nếu có
        if (dto.getNhaCungCapId() != null) {
            NhaCungCap ncc = nhaCungCapRepository.findById(dto.getNhaCungCapId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
            loHang.setNhaCungCap(ncc);
        }

        // Xác định trạng thái
        loHang.setTrangThai(determineLoHangStatus(loHang));

        loHang = loHangRepository.save(loHang);
        log.info("Created lo hang successfully with ID: {}", loHang.getId());

        return convertToDTO(loHang);
    }

    /**
     * Cập nhật lô hàng
     */
    @Transactional
    public LoHangDTO updateLoHang(Long id, LoHangDTO dto) {
        LoHang loHang = loHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô hàng"));

        // Chỉ cho phép sửa một số trường
        if (dto.getNgaySanXuat() != null) {
            loHang.setNgaySanXuat(dto.getNgaySanXuat());
        }
        if (dto.getHanSuDung() != null) {
            loHang.setHanSuDung(dto.getHanSuDung());
        }
        if (dto.getGhiChu() != null) {
            loHang.setGhiChu(dto.getGhiChu());
        }

        // Cập nhật trạng thái
        loHang.setTrangThai(determineLoHangStatus(loHang));

        return convertToDTO(loHangRepository.save(loHang));
    }

    /**
     * Xóa lô hàng (chỉ cho phép nếu chưa sử dụng)
     */
    @Transactional
    public void deleteLoHang(Long id) {
        LoHang loHang = loHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô hàng"));

        if (loHang.getSoLuongHienTai() < loHang.getSoLuongNhap()) {
            throw new IllegalStateException("Không thể xóa lô hàng đã xuất");
        }

        loHangRepository.deleteById(id);
        log.info("Deleted lo hang ID: {}", id);
    }

    /**
     * Lấy danh sách lô sắp hết hạn
     */
    @Transactional(readOnly = true)
    public List<LoHangDTO> getLoHangSapHetHan(int soNgay) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(soNgay);

        return loHangRepository.findExpiringSoon(now, future).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lô đã hết hàng
     */
    @Transactional(readOnly = true)
    public List<LoHangDTO> getLoHangHetHang() {
        return loHangRepository.findEmptyLoHang().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái tất cả các lô hàng
     */
    @Transactional
    public void updateAllLoHangStatus() {
        List<LoHang> allLoHang = loHangRepository.findAll();

        for (LoHang loHang : allLoHang) {
            LoHang.TrangThaiLoHang oldStatus = loHang.getTrangThai();
            LoHang.TrangThaiLoHang newStatus = determineLoHangStatus(loHang);

            if (oldStatus != newStatus) {
                loHang.setTrangThai(newStatus);
                loHangRepository.save(loHang);
                log.info("Updated status for lo hang ID: {} from {} to {}",
                        loHang.getId(), oldStatus, newStatus);
            }
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void validateLoHangData(LoHangDTO dto) {
        if (dto.getHangHoaId() == null) {
            throw new IllegalArgumentException("Hàng hóa không được để trống");
        }
        if (dto.getSoLo() == null || dto.getSoLo().trim().isEmpty()) {
            throw new IllegalArgumentException("Số lô không được để trống");
        }
        if (dto.getSoLuongNhap() == null || dto.getSoLuongNhap() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }
        if (dto.getGiaNhap() == null || dto.getGiaNhap().signum() <= 0) {
            throw new IllegalArgumentException("Giá nhập phải lớn hơn 0");
        }

        // Validate ngày sản xuất và hạn sử dụng
        if (dto.getNgaySanXuat() != null && dto.getHanSuDung() != null) {
            if (dto.getHanSuDung().isBefore(dto.getNgaySanXuat())) {
                throw new IllegalArgumentException("Hạn sử dụng phải sau ngày sản xuất");
            }
        }

        if (dto.getHanSuDung() != null && dto.getHanSuDung().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Hạn sử dụng không được trong quá khứ");
        }
    }

    /**
     * Xác định trạng thái lô hàng dựa trên hạn sử dụng và số lượng
     */
    private LoHang.TrangThaiLoHang determineLoHangStatus(LoHang loHang) {
        // Hết hàng
        if (loHang.getSoLuongHienTai() <= 0) {
            return LoHang.TrangThaiLoHang.HET_HANG;
        }

        // Kiểm tra hạn sử dụng
        if (loHang.getHanSuDung() != null) {
            LocalDate now = LocalDate.now();

            // Hết hạn
            if (loHang.getHanSuDung().isBefore(now)) {
                return LoHang.TrangThaiLoHang.HET_HAN;
            }

            // Gần hết hạn (30 ngày)
            LocalDate ganHetHan = now.plusDays(30);
            if (loHang.getHanSuDung().isBefore(ganHetHan)) {
                return LoHang.TrangThaiLoHang.GAN_HET_HAN;
            }
        }

        // Đang sử dụng
        if (loHang.getSoLuongHienTai() < loHang.getSoLuongNhap()) {
            return LoHang.TrangThaiLoHang.DANG_SU_DUNG;
        }

        // Mới
        return LoHang.TrangThaiLoHang.MOI;
    }

    private LoHangDTO convertToDTO(LoHang entity) {
        HangHoa hangHoa = entity.getHangHoa();

        // Tính số ngày còn lại
        Integer soNgayConLai = null;
        Boolean sapHetHan = false;

        if (entity.getHanSuDung() != null) {
            LocalDate now = LocalDate.now();
            soNgayConLai = (int) ChronoUnit.DAYS.between(now, entity.getHanSuDung());
            sapHetHan = soNgayConLai >= 0 && soNgayConLai <= 30;
        }

        return LoHangDTO.builder()
                .id(entity.getId())
                .hangHoaId(hangHoa.getId())
                .maHangHoa(hangHoa.getMaHangHoa())
                .tenHangHoa(hangHoa.getTenHangHoa())
                .tenDonViTinh(hangHoa.getDonViTinh() != null ?
                        hangHoa.getDonViTinh().getTenDvt() : null)
                .soLo(entity.getSoLo())
                .ngaySanXuat(entity.getNgaySanXuat())
                .hanSuDung(entity.getHanSuDung())
                .soLuongNhap(entity.getSoLuongNhap())
                .soLuongHienTai(entity.getSoLuongHienTai())
                .giaNhap(entity.getGiaNhap())
                .nhaCungCapId(entity.getNhaCungCap() != null ?
                        entity.getNhaCungCap().getId() : null)
                .tenNhaCungCap(entity.getNhaCungCap() != null ?
                        entity.getNhaCungCap().getTenNcc() : null)
                .soChungTuNhap(entity.getSoChungTuNhap())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .soNgayConLai(soNgayConLai)
                .sapHetHan(sapHetHan)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}