package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PhieuNhapKhoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.*;
import com.hospital.warehouse.hospital_warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý phiếu nhập kho
 * Xử lý các nghiệp vụ: tạo, cập nhật, duyệt, hủy phiếu nhập
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhieuNhapKhoService {

    private final PhieuNhapKhoRepository phieuNhapKhoRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
    private final KhoRepository khoRepository;
    private final HangHoaRepository hangHoaRepository;
    private final LoHangRepository loHangRepository;
    private final UserRepository userRepository;
    private final NhaCungCapRepository nhaCungCapRepository;

    /**
     * Lấy danh sách phiếu nhập có phân trang và lọc
     */
    @Transactional(readOnly = true)
    public PageResponse<PhieuNhapKhoDTO> getAllPhieuNhap(
            String search,
            Long khoId,
            Long nhaCungCapId,
            PhieuNhapKho.TrangThaiPhieuNhap trangThai,
            LocalDate tuNgay,
            LocalDate denNgay,
            Pageable pageable) {

        Specification<PhieuNhapKho> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo mã phiếu nhập hoặc số hóa đơn
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("maPhieuNhap")), searchPattern),
                        cb.like(cb.lower(root.get("soHoaDon")), searchPattern)
                ));
            }

            // Lọc theo kho
            if (khoId != null) {
                predicates.add(cb.equal(root.get("kho").get("id"), khoId));
            }

            // Lọc theo nhà cung cấp
            if (nhaCungCapId != null) {
                predicates.add(cb.equal(root.get("nhaCungCap").get("id"), nhaCungCapId));
            }

            // Lọc theo trạng thái
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }

            // Lọc theo khoảng thời gian
            if (tuNgay != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("ngayNhap").as(LocalDate.class), tuNgay));
            }

            if (denNgay != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("ngayNhap").as(LocalDate.class), denNgay));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PhieuNhapKho> page = phieuNhapKhoRepository.findAll(spec, pageable);

        List<PhieuNhapKhoDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<PhieuNhapKhoDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Lấy chi tiết phiếu nhập theo ID
     */
    @Transactional(readOnly = true)
    public Optional<PhieuNhapKhoDTO> getPhieuNhapById(Long id) {
        return phieuNhapKhoRepository.findById(id)
                .map(this::convertToDTOWithDetails);
    }

    /**
     * Lấy chi tiết phiếu nhập theo mã phiếu
     */
    @Transactional(readOnly = true)
    public Optional<PhieuNhapKhoDTO> getPhieuNhapByMa(String maPhieuNhap) {
        return phieuNhapKhoRepository.findByMaPhieuNhap(maPhieuNhap)
                .map(this::convertToDTOWithDetails);
    }

    /**
     * Tạo phiếu nhập mới
     */
    @Transactional
    public PhieuNhapKhoDTO createPhieuNhap(PhieuNhapKhoDTO dto) {
        log.info("Creating phieu nhap with data: {}", dto);

        // Validate dữ liệu đầu vào
        validatePhieuNhapData(dto);

        User currentUser = getCurrentUser();
        Kho kho = khoRepository.findById(dto.getKhoId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho"));

        // Tạo entity phiếu nhập
        PhieuNhapKho phieuNhap = new PhieuNhapKho();
        phieuNhap.setMaPhieuNhap(generateMaPhieuNhap());
        phieuNhap.setKho(kho);

        // Set nhà cung cấp nếu có
        if (dto.getNhaCungCapId() != null) {
            NhaCungCap ncc = nhaCungCapRepository.findById(dto.getNhaCungCapId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
            phieuNhap.setNhaCungCap(ncc);
        }

        // Set thông tin cơ bản
        phieuNhap.setLoaiNhap(dto.getLoaiNhap());
        phieuNhap.setSoHoaDon(dto.getSoHoaDon());
        phieuNhap.setNgayHoaDon(dto.getNgayHoaDon());
        phieuNhap.setNgayNhap(dto.getNgayNhap() != null ? dto.getNgayNhap() : LocalDateTime.now());
        phieuNhap.setNguoiGiao(dto.getNguoiGiao());
        phieuNhap.setSdtNguoiGiao(dto.getSdtNguoiGiao());
        phieuNhap.setNguoiNhan(currentUser);
        phieuNhap.setGhiChu(dto.getGhiChu());
        phieuNhap.setTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.NHAP);
        phieuNhap.setCreatedBy(currentUser);

        // Tính toán các tổng tiền
        calculateTotals(phieuNhap, dto);

        // Lưu phiếu nhập
        phieuNhap = phieuNhapKhoRepository.save(phieuNhap);

        // Lưu chi tiết phiếu nhập
        if (dto.getChiTiet() != null && !dto.getChiTiet().isEmpty()) {
            for (PhieuNhapKhoDTO.ChiTietPhieuNhapDTO chiTietDTO : dto.getChiTiet()) {
                saveChiTietPhieuNhap(phieuNhap, chiTietDTO);
            }
        }

        log.info("Created phieu nhap successfully with ID: {}", phieuNhap.getId());
        return convertToDTOWithDetails(phieuNhap);
    }

    /**
     * Cập nhật thông tin phiếu nhập
     */
    @Transactional
    public PhieuNhapKhoDTO updatePhieuNhap(Long id, PhieuNhapKhoDTO dto) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        // Kiểm tra trạng thái
        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Không thể sửa phiếu nhập đã duyệt");
        }

        // Cập nhật thông tin
        phieuNhap.setSoHoaDon(dto.getSoHoaDon());
        phieuNhap.setNgayHoaDon(dto.getNgayHoaDon());
        phieuNhap.setNguoiGiao(dto.getNguoiGiao());
        phieuNhap.setSdtNguoiGiao(dto.getSdtNguoiGiao());
        phieuNhap.setGhiChu(dto.getGhiChu());
        phieuNhap.setUpdatedBy(getCurrentUser());

        // Cập nhật chi phí nếu có
        if (dto.getChiPhiVanChuyen() != null) {
            phieuNhap.setChiPhiVanChuyen(dto.getChiPhiVanChuyen());
        }
        if (dto.getChiPhiKhac() != null) {
            phieuNhap.setChiPhiKhac(dto.getChiPhiKhac());
        }
        if (dto.getGiamGia() != null) {
            phieuNhap.setGiamGia(dto.getGiamGia());
        }

        // Tính lại tổng thanh toán
        recalculateTongThanhToan(phieuNhap);

        return convertToDTOWithDetails(phieuNhapKhoRepository.save(phieuNhap));
    }

    /**
     * Duyệt phiếu nhập và cập nhật tồn kho
     */
    @Transactional
    public PhieuNhapKhoDTO duyetPhieuNhap(Long id) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        // Kiểm tra trạng thái
        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Phiếu nhập đã được duyệt");
        }

        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.HUY) {
            throw new IllegalStateException("Không thể duyệt phiếu nhập đã hủy");
        }

        User currentUser = getCurrentUser();
        phieuNhap.setNguoiDuyet(currentUser);
        phieuNhap.setNgayDuyet(LocalDateTime.now());
        phieuNhap.setTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET);

        // Cập nhật tồn kho cho từng chi tiết
        List<ChiTietPhieuNhap> chiTietList = chiTietPhieuNhapRepository.findByPhieuNhapId(id);

        for (ChiTietPhieuNhap chiTiet : chiTietList) {
            updateInventoryFromNhap(chiTiet);
            chiTiet.setTrangThai(ChiTietPhieuNhap.TrangThaiChiTiet.DA_NHAP);
            chiTietPhieuNhapRepository.save(chiTiet);
        }

        log.info("Approved phieu nhap ID: {} by user: {}", id, currentUser.getTenDangNhap());
        return convertToDTOWithDetails(phieuNhapKhoRepository.save(phieuNhap));
    }

    /**
     * Hủy phiếu nhập
     */
    @Transactional
    public PhieuNhapKhoDTO huyPhieuNhap(Long id, String lyDoHuy) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Không thể hủy phiếu nhập đã duyệt");
        }

        if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hủy không được để trống");
        }

        phieuNhap.setTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.HUY);
        phieuNhap.setLyDoHuy(lyDoHuy);
        phieuNhap.setUpdatedBy(getCurrentUser());

        log.info("Cancelled phieu nhap ID: {} with reason: {}", id, lyDoHuy);
        return convertToDTOWithDetails(phieuNhapKhoRepository.save(phieuNhap));
    }

    /**
     * Xóa phiếu nhập
     */
    @Transactional
    public void deletePhieuNhap(Long id) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Không thể xóa phiếu nhập đã duyệt");
        }

        // Xóa chi tiết trước
        chiTietPhieuNhapRepository.deleteByPhieuNhapId(id);

        // Xóa phiếu nhập
        phieuNhapKhoRepository.deleteById(id);

        log.info("Deleted phieu nhap ID: {}", id);
    }

    /**
     * Lấy thống kê phiếu nhập
     */
    @Transactional(readOnly = true)
    public PhieuNhapKhoDTO.ThongKePhieuNhap getThongKePhieuNhap(
            Long khoId, LocalDate tuNgay, LocalDate denNgay) {

        Specification<PhieuNhapKho> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (khoId != null) {
                predicates.add(cb.equal(root.get("kho").get("id"), khoId));
            }

            if (tuNgay != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("ngayNhap").as(LocalDate.class), tuNgay));
            }

            if (denNgay != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("ngayNhap").as(LocalDate.class), denNgay));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<PhieuNhapKho> phieuNhapList = phieuNhapKhoRepository.findAll(spec);

        long tongSoPhieu = phieuNhapList.size();
        long soPhieuChoDuyet = phieuNhapList.stream()
                .filter(p -> p.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.CHO_DUYET)
                .count();
        long soPhieuDaDuyet = phieuNhapList.stream()
                .filter(p -> p.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET)
                .count();

        BigDecimal tongGiaTri = phieuNhapList.stream()
                .filter(p -> p.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET)
                .map(PhieuNhapKho::getTongThanhToan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PhieuNhapKhoDTO.ThongKePhieuNhap.builder()
                .tongSoPhieu(tongSoPhieu)
                .soPhieuChoDuyet(soPhieuChoDuyet)
                .soPhieuDaDuyet(soPhieuDaDuyet)
                .tongGiaTri(tongGiaTri)
                .build();
    }

    /**
     * Lấy danh sách phiếu nhập chờ duyệt
     */
    @Transactional(readOnly = true)
    public List<PhieuNhapKhoDTO> getPhieuNhapChoDuyet() {
        return phieuNhapKhoRepository
                .findByTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.CHO_DUYET)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách phiếu nhập theo kho và khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<PhieuNhapKhoDTO> getPhieuNhapByKho(
            Long khoId, LocalDate tuNgay, LocalDate denNgay) {

        List<PhieuNhapKho> list;
        if (tuNgay != null && denNgay != null) {
            list = phieuNhapKhoRepository.findByKhoIdAndNgayNhapBetween(
                    khoId, tuNgay.atStartOfDay(), denNgay.atTime(23, 59, 59));
        } else {
            list = phieuNhapKhoRepository.findByKhoId(khoId);
        }

        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate dữ liệu phiếu nhập
     */
    private void validatePhieuNhapData(PhieuNhapKhoDTO dto) {
        if (dto.getKhoId() == null) {
            throw new IllegalArgumentException("Kho không được để trống");
        }

        if (dto.getChiTiet() == null || dto.getChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Chi tiết phiếu nhập không được để trống");
        }

        for (PhieuNhapKhoDTO.ChiTietPhieuNhapDTO chiTiet : dto.getChiTiet()) {
            if (chiTiet.getHangHoaId() == null) {
                throw new IllegalArgumentException("Hàng hóa không được để trống");
            }
            if (chiTiet.getSoLuong() == null || chiTiet.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }
            if (chiTiet.getDonGia() == null || chiTiet.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Đơn giá phải lớn hơn 0");
            }

            // Validate hạn sử dụng
            if (chiTiet.getHanSuDung() != null && chiTiet.getNgaySanXuat() != null) {
                if (chiTiet.getHanSuDung().isBefore(chiTiet.getNgaySanXuat())) {
                    throw new IllegalArgumentException("Hạn sử dụng phải sau ngày sản xuất");
                }
            }
        }
    }

    /**
     * Tính toán các tổng tiền cho phiếu nhập
     */
    private void calculateTotals(PhieuNhapKho phieuNhap, PhieuNhapKhoDTO dto) {
        BigDecimal tongTienTruocThue = BigDecimal.ZERO;
        BigDecimal tienThue = BigDecimal.ZERO;

        if (dto.getChiTiet() != null && !dto.getChiTiet().isEmpty()) {
            for (PhieuNhapKhoDTO.ChiTietPhieuNhapDTO chiTietDTO : dto.getChiTiet()) {
                tongTienTruocThue = tongTienTruocThue.add(chiTietDTO.getThanhTien());
                tienThue = tienThue.add(chiTietDTO.getTienThue() != null ?
                        chiTietDTO.getTienThue() : BigDecimal.ZERO);
            }
        }

        phieuNhap.setTongTienTruocThue(tongTienTruocThue);
        phieuNhap.setTienThue(tienThue);
        phieuNhap.setTongTienSauThue(tongTienTruocThue.add(tienThue));
        phieuNhap.setChiPhiVanChuyen(dto.getChiPhiVanChuyen() != null ?
                dto.getChiPhiVanChuyen() : BigDecimal.ZERO);
        phieuNhap.setChiPhiKhac(dto.getChiPhiKhac() != null ?
                dto.getChiPhiKhac() : BigDecimal.ZERO);
        phieuNhap.setGiamGia(dto.getGiamGia() != null ?
                dto.getGiamGia() : BigDecimal.ZERO);

        BigDecimal tongThanhToan = phieuNhap.getTongTienSauThue()
                .add(phieuNhap.getChiPhiVanChuyen())
                .add(phieuNhap.getChiPhiKhac())
                .subtract(phieuNhap.getGiamGia());
        phieuNhap.setTongThanhToan(tongThanhToan);
    }

    /**
     * Tính lại tổng thanh toán
     */
    private void recalculateTongThanhToan(PhieuNhapKho phieuNhap) {
        BigDecimal tongThanhToan = phieuNhap.getTongTienSauThue()
                .add(phieuNhap.getChiPhiVanChuyen())
                .add(phieuNhap.getChiPhiKhac())
                .subtract(phieuNhap.getGiamGia());
        phieuNhap.setTongThanhToan(tongThanhToan);
    }

    /**
     * Lưu chi tiết phiếu nhập
     */
    private void saveChiTietPhieuNhap(PhieuNhapKho phieuNhap,
                                      PhieuNhapKhoDTO.ChiTietPhieuNhapDTO chiTietDTO) {

        HangHoa hangHoa = hangHoaRepository.findById(chiTietDTO.getHangHoaId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa"));

        ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
        chiTiet.setPhieuNhap(phieuNhap);
        chiTiet.setHangHoa(hangHoa);
        chiTiet.setSoLuong(chiTietDTO.getSoLuong());
        chiTiet.setDonGia(chiTietDTO.getDonGia());
        chiTiet.setThanhTien(chiTietDTO.getThanhTien());
        chiTiet.setTienThue(chiTietDTO.getTienThue());
        chiTiet.setTyLeThue(chiTietDTO.getTyLeThue());
        chiTiet.setNgaySanXuat(chiTietDTO.getNgaySanXuat());
        chiTiet.setHanSuDung(chiTietDTO.getHanSuDung());
        chiTiet.setSoLo(chiTietDTO.getSoLo());
        chiTiet.setGhiChu(chiTietDTO.getGhiChu());
        chiTiet.setTrangThai(ChiTietPhieuNhap.TrangThaiChiTiet.CHO_NHAP);

        chiTietPhieuNhapRepository.save(chiTiet);

        // Tạo hoặc cập nhật lô hàng nếu hàng hóa có quản lý lô
        if (hangHoa.getCoQuanLyLo() && chiTietDTO.getSoLo() != null) {
            createOrUpdateLoHang(hangHoa, chiTietDTO, phieuNhap);
        }
    }

    /**
     * Tạo hoặc cập nhật lô hàng
     */
    private void createOrUpdateLoHang(HangHoa hangHoa,
                                      PhieuNhapKhoDTO.ChiTietPhieuNhapDTO chiTietDTO,
                                      PhieuNhapKho phieuNhap) {

        Optional<LoHang> existingLoHang = loHangRepository
                .findByHangHoaIdAndSoLo(hangHoa.getId(), chiTietDTO.getSoLo());

        LoHang loHang;
        if (existingLoHang.isPresent()) {
            // Cập nhật lô hàng đã có
            loHang = existingLoHang.get();
            loHang.setSoLuongNhap(loHang.getSoLuongNhap() + chiTietDTO.getSoLuong());
            loHang.setSoLuongHienTai(loHang.getSoLuongHienTai() + chiTietDTO.getSoLuong());
        } else {
            // Tạo lô hàng mới
            loHang = new LoHang();
            loHang.setHangHoa(hangHoa);
            loHang.setSoLo(chiTietDTO.getSoLo());
            loHang.setNgaySanXuat(chiTietDTO.getNgaySanXuat());
            loHang.setHanSuDung(chiTietDTO.getHanSuDung());
            loHang.setSoLuongNhap(chiTietDTO.getSoLuong());
            loHang.setSoLuongHienTai(chiTietDTO.getSoLuong());
            loHang.setGiaNhap(chiTietDTO.getDonGia());
            loHang.setNhaCungCap(phieuNhap.getNhaCungCap());
            loHang.setSoChungTuNhap(phieuNhap.getMaPhieuNhap());
            loHang.setTrangThai(LoHang.TrangThaiLoHang.MOI);
        }

        loHangRepository.save(loHang);
    }

    /**
     * Cập nhật tồn kho khi duyệt phiếu nhập
     */
    private void updateInventoryFromNhap(ChiTietPhieuNhap chiTiet) {
        HangHoa hangHoa = chiTiet.getHangHoa();

        // Tính tổng số lượng cũ
        Integer oldTongSoLuong = hangHoa.getTongSoLuong() != null ? hangHoa.getTongSoLuong() : 0;
        Integer oldSoLuongCoTheXuat = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        // Cập nhật số lượng tồn kho
        hangHoa.setTongSoLuong(oldTongSoLuong + chiTiet.getSoLuong());
        hangHoa.setSoLuongCoTheXuat(oldSoLuongCoTheXuat + chiTiet.getSoLuong());

        // Cập nhật giá nhập trung bình theo công thức WAVG (Weighted Average)
        BigDecimal giaNhapTrungBinhCu = hangHoa.getGiaNhapTrungBinh() != null ?
                hangHoa.getGiaNhapTrungBinh() : BigDecimal.ZERO;

        BigDecimal tongGiaTriCu = giaNhapTrungBinhCu.multiply(new BigDecimal(oldTongSoLuong));
        BigDecimal giaTriNhapMoi = chiTiet.getDonGia().multiply(new BigDecimal(chiTiet.getSoLuong()));
        BigDecimal tongGiaTriMoi = tongGiaTriCu.add(giaTriNhapMoi);

        BigDecimal giaNhapTrungBinhMoi = tongGiaTriMoi.divide(
                new BigDecimal(hangHoa.getTongSoLuong()),
                2,
                RoundingMode.HALF_UP
        );
        hangHoa.setGiaNhapTrungBinh(giaNhapTrungBinhMoi);

        // Cập nhật ngày nhập gần nhất
        hangHoa.setNgayNhapGanNhat(LocalDateTime.now());

        hangHoaRepository.save(hangHoa);

        log.info("Updated inventory for hang hoa ID: {} - Old quantity: {}, New quantity: {}, Average price: {}",
                hangHoa.getId(), oldTongSoLuong, hangHoa.getTongSoLuong(), giaNhapTrungBinhMoi);
    }

    /**
     * Sinh mã phiếu nhập tự động theo format: PN-YYYYMMDD-XXXX
     */
    private String generateMaPhieuNhap() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("PN-%d%02d%02d-",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        long count = phieuNhapKhoRepository.countByMaPhieuNhapStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin người dùng"));
    }

    /**
     * Convert entity sang DTO (không có chi tiết)
     */
    private PhieuNhapKhoDTO convertToDTO(PhieuNhapKho entity) {
        return PhieuNhapKhoDTO.builder()
                .id(entity.getId())
                .maPhieuNhap(entity.getMaPhieuNhap())
                .khoId(entity.getKho().getId())
                .tenKho(entity.getKho().getTenKho())
                .nhaCungCapId(entity.getNhaCungCap() != null ?
                        entity.getNhaCungCap().getId() : null)
                .tenNhaCungCap(entity.getNhaCungCap() != null ?
                        entity.getNhaCungCap().getTenNcc() : null)
                .loaiNhap(entity.getLoaiNhap())
                .soHoaDon(entity.getSoHoaDon())
                .ngayHoaDon(entity.getNgayHoaDon())
                .ngayNhap(entity.getNgayNhap())
                .tongTienTruocThue(entity.getTongTienTruocThue())
                .tienThue(entity.getTienThue())
                .tongTienSauThue(entity.getTongTienSauThue())
                .chiPhiVanChuyen(entity.getChiPhiVanChuyen())
                .chiPhiKhac(entity.getChiPhiKhac())
                .giamGia(entity.getGiamGia())
                .tongThanhToan(entity.getTongThanhToan())
                .nguoiGiao(entity.getNguoiGiao())
                .sdtNguoiGiao(entity.getSdtNguoiGiao())
                .nguoiNhanId(entity.getNguoiNhan() != null ?
                        entity.getNguoiNhan().getId() : null)
                .tenNguoiNhan(entity.getNguoiNhan() != null ?
                        entity.getNguoiNhan().getHoTen() : null)
                .nguoiDuyetId(entity.getNguoiDuyet() != null ?
                        entity.getNguoiDuyet().getId() : null)
                .tenNguoiDuyet(entity.getNguoiDuyet() != null ?
                        entity.getNguoiDuyet().getHoTen() : null)
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .lyDoHuy(entity.getLyDoHuy())
                .ngayDuyet(entity.getNgayDuyet())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity sang DTO (có chi tiết)
     */
    private PhieuNhapKhoDTO convertToDTOWithDetails(PhieuNhapKho entity) {
        PhieuNhapKhoDTO dto = convertToDTO(entity);

        // Lấy danh sách chi tiết
        List<ChiTietPhieuNhap> chiTietList =
                chiTietPhieuNhapRepository.findByPhieuNhapId(entity.getId());

        List<PhieuNhapKhoDTO.ChiTietPhieuNhapDTO> chiTietDTOs = chiTietList.stream()
                .map(this::convertChiTietToDTO)
                .collect(Collectors.toList());

        dto.setChiTiet(chiTietDTOs);
        return dto;
    }

    /**
     * Convert chi tiết entity sang DTO
     */
    private PhieuNhapKhoDTO.ChiTietPhieuNhapDTO convertChiTietToDTO(ChiTietPhieuNhap entity) {
        HangHoa hangHoa = entity.getHangHoa();

        return PhieuNhapKhoDTO.ChiTietPhieuNhapDTO.builder()
                .id(entity.getId())
                .hangHoaId(hangHoa.getId())
                .maHangHoa(hangHoa.getMaHangHoa())
                .tenHangHoa(hangHoa.getTenHangHoa())
                .tenDonViTinh(hangHoa.getDonViTinh() != null ?
                        hangHoa.getDonViTinh().getTenDvt() : null)  // ← Sửa dòng này
                .soLuong(entity.getSoLuong())
                .donGia(entity.getDonGia())
                .thanhTien(entity.getThanhTien())
                .tienThue(entity.getTienThue())
                .tyLeThue(entity.getTyLeThue())
                .ngaySanXuat(entity.getNgaySanXuat())
                .hanSuDung(entity.getHanSuDung())
                .soLo(entity.getSoLo())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .build();
    }
}