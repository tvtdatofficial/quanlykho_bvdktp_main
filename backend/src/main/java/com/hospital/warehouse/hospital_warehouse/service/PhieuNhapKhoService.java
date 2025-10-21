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
import org.springframework.transaction.annotation.Propagation;
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
    private final HangHoaViTriRepository hangHoaViTriRepository;
    private final ViTriKhoRepository viTriKhoRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;

    private final LoHangService loHangService;  // ✅ THÊM DÒNG NÀY
    private final HangHoaService hangHoaService;

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

    @Transactional(rollbackFor = Exception.class)
    public PhieuNhapKhoDTO duyetPhieuNhap(Long id) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        // Validate trạng thái
        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Phiếu nhập đã được duyệt");
        }
        if (phieuNhap.getTrangThai() == PhieuNhapKho.TrangThaiPhieuNhap.HUY) {
            throw new IllegalStateException("Không thể duyệt phiếu nhập đã hủy");
        }

        User currentUser = getCurrentUser();
        List<ChiTietPhieuNhap> chiTietList = chiTietPhieuNhapRepository.findByPhieuNhapId(id);

        if (chiTietList.isEmpty()) {
            throw new IllegalStateException("Phiếu nhập không có chi tiết");
        }

        try {
            // Xử lý từng chi tiết
            for (ChiTietPhieuNhap chiTiet : chiTietList) {
                processChiTietNhapKho(chiTiet, phieuNhap);
            }

            // Cập nhật trạng thái phiếu nhập
            phieuNhap.setNguoiDuyet(currentUser);
            phieuNhap.setNgayDuyet(LocalDateTime.now());
            phieuNhap.setTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET);
            phieuNhapKhoRepository.save(phieuNhap);

            log.info("✅ Successfully approved phieu nhap ID: {} by user: {}",
                    id, currentUser.getTenDangNhap());

            return convertToDTOWithDetails(phieuNhap);

        } catch (Exception e) {
            log.error("❌ Error approving phieu nhap ID: {}", id, e);
            throw new RuntimeException("Lỗi khi duyệt phiếu nhập: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý một chi tiết phiếu nhập
     */
    private void processChiTietNhapKho(ChiTietPhieuNhap chiTiet, PhieuNhapKho phieuNhap) {
        HangHoa hangHoa = chiTiet.getHangHoa();

        // Lưu số lượng trước khi nhập (để ghi lịch sử)
        Integer soLuongTruocNhap = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        // 1. Kiểm tra sức chứa vị trí kho
        validateViTriKhoCapacity(chiTiet);

        // 2. Tạo/Cập nhật lô hàng (nếu có quản lý lô)
        LoHang loHang = null;
        if (hangHoa.getCoQuanLyLo() != null && hangHoa.getCoQuanLyLo() &&
                chiTiet.getSoLo() != null && !chiTiet.getSoLo().trim().isEmpty()) {

            // ✅ GỌI TỪ LoHangService thay vì method trong class này
            loHang = loHangService.findOrCreateLoHang(chiTiet, phieuNhap);
            chiTiet.setLoHang(loHang);
            log.info("📦 Assigned lo_hang_id={} to chi_tiet_id={}",
                    loHang.getId(), chiTiet.getId());
        }

        // 3. Cập nhật hàng hóa vị trí
        if (chiTiet.getViTriKho() != null) {
            updateHangHoaViTri(chiTiet, loHang);
        }

        // 4. Cập nhật tồn kho
        updateInventoryFromNhap(chiTiet);

        // 5. Ghi lịch sử tồn kho ✅ THÊM MỚI
        Integer soLuongSauNhap = hangHoa.getSoLuongCoTheXuat();
        ghiLichSuTonKho(chiTiet, phieuNhap, soLuongTruocNhap, soLuongSauNhap);

        // 6. Cập nhật trạng thái chi tiết
        chiTiet.setTrangThai(ChiTietPhieuNhap.TrangThaiChiTiet.DA_NHAP);
        chiTietPhieuNhapRepository.save(chiTiet);
    }


    /**
     * Kiểm tra sức chứa vị trí kho
     */
    private void validateViTriKhoCapacity(ChiTietPhieuNhap chiTiet) {
        ViTriKho viTriKho = chiTiet.getViTriKho();

        if (viTriKho == null) {
            throw new IllegalStateException(
                    "Chi tiết phiếu nhập ID " + chiTiet.getId() + " chưa có vị trí kho");
        }

        if (viTriKho.getSucChuaToiDa() != null && viTriKho.getSucChuaToiDa() > 0) {
            Integer soLuongHienTai = hangHoaViTriRepository
                    .sumSoLuongByViTriKhoId(viTriKho.getId())
                    .orElse(0);

            Integer soLuongSauNhap = soLuongHienTai + chiTiet.getSoLuong();

            if (soLuongSauNhap > viTriKho.getSucChuaToiDa()) {
                // ✅ MESSAGE RÕ RÀNG CHO NGƯỜI DÙNG
                String errorMessage = String.format(
                        "Không thể nhập vào vị trí '%s': " +
                                "Sức chứa tối đa %d, hiện tại đã có %d, " +
                                "không thể nhập thêm %d (tổng sẽ là %d). " +
                                "Vui lòng chọn vị trí khác hoặc tăng sức chứa.",
                        viTriKho.getTenViTri(),
                        viTriKho.getSucChuaToiDa(),
                        soLuongHienTai,
                        chiTiet.getSoLuong(),
                        soLuongSauNhap
                );

                log.error("❌ Validation failed: {}", errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
    }



    /**
     * Ghi lịch sử biến động tồn kho
     */
    private void ghiLichSuTonKho(ChiTietPhieuNhap chiTiet,
                                 PhieuNhapKho phieuNhap,
                                 Integer soLuongTruoc,
                                 Integer soLuongSau) {
        try {
            LichSuTonKho lichSu = LichSuTonKho.builder()
                    .hangHoa(chiTiet.getHangHoa())
                    .loHang(chiTiet.getLoHang())
                    .viTriKho(chiTiet.getViTriKho())
                    .loaiBienDong(LichSuTonKho.LoaiBienDong.NHAP_KHO)
                    .soLuongTruoc(soLuongTruoc)
                    .soLuongBienDong(chiTiet.getSoLuong())
                    .soLuongSau(soLuongSau)
                    .donGia(chiTiet.getDonGia())
                    .giaTriBienDong(chiTiet.getThanhTien())
                    .maChungTu(phieuNhap.getMaPhieuNhap())
                    .loaiChungTu(LichSuTonKho.LoaiChungTu.PHIEU_NHAP)
                    .lyDo("Nhập kho từ phiếu nhập " + phieuNhap.getMaPhieuNhap())
                    .nguoiThucHien(getCurrentUser())
                    .build();

            lichSuTonKhoRepository.save(lichSu);

            log.info("📝 Saved lich su ton kho: HangHoa={}, Before={}, After={}, Delta=+{}",
                    chiTiet.getHangHoa().getTenHangHoa(),
                    soLuongTruoc,
                    soLuongSau,
                    chiTiet.getSoLuong());
        } catch (Exception e) {
            log.error("❌ Error saving lich su ton kho", e);
            throw new RuntimeException("Lỗi ghi lịch sử tồn kho: " + e.getMessage());
        }
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
     * ✅ BỔ SUNG: Hủy duyệt phiếu nhập (chỉ ADMIN)
     * Hoàn nguyên tồn kho, xóa/giảm lô hàng đã tạo
     */
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhapKhoDTO huyDuyetPhieuNhap(Long id, String lyDoHuyDuyet) {
        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập"));

        // Chỉ cho phép hủy duyệt nếu đã duyệt
        if (phieuNhap.getTrangThai() != PhieuNhapKho.TrangThaiPhieuNhap.DA_DUYET) {
            throw new IllegalStateException("Chỉ có thể hủy duyệt phiếu đã duyệt");
        }

        if (lyDoHuyDuyet == null || lyDoHuyDuyet.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hủy duyệt không được để trống");
        }

        try {
            List<ChiTietPhieuNhap> chiTietList = chiTietPhieuNhapRepository.findByPhieuNhapId(id);

            // Hoàn nguyên từng chi tiết
            for (ChiTietPhieuNhap chiTiet : chiTietList) {
                rollbackChiTietNhapKho(chiTiet, phieuNhap);
            }

            // Cập nhật trạng thái phiếu nhập
            phieuNhap.setTrangThai(PhieuNhapKho.TrangThaiPhieuNhap.CHO_DUYET);
            phieuNhap.setNguoiDuyet(null);
            phieuNhap.setNgayDuyet(null);
            phieuNhap.setGhiChu(
                    (phieuNhap.getGhiChu() != null ? phieuNhap.getGhiChu() + "\n\n" : "") +
                            "⚠️ ĐÃ HỦY DUYỆT\n" +
                            "Lý do: " + lyDoHuyDuyet + "\n" +
                            "Người thực hiện: " + getCurrentUser().getHoTen() + "\n" +
                            "Thời gian: " + LocalDateTime.now()
            );
            phieuNhapKhoRepository.save(phieuNhap);

            log.info("✅ Successfully rolled back phieu nhap ID: {}", id);
            return convertToDTOWithDetails(phieuNhap);

        } catch (Exception e) {
            log.error("❌ Error rolling back phieu nhap ID: {}", id, e);
            throw new RuntimeException("Lỗi khi hủy duyệt phiếu nhập: " + e.getMessage(), e);
        }
    }

    /**
     * Hoàn nguyên một chi tiết phiếu nhập
     */
    private void rollbackChiTietNhapKho(ChiTietPhieuNhap chiTiet, PhieuNhapKho phieuNhap) {
        HangHoa hangHoa = chiTiet.getHangHoa();
        Integer soLuongNhap = chiTiet.getSoLuong();

        log.info("🔄 Rolling back chi tiet: HangHoa={}, SoLuong={}",
                hangHoa.getTenHangHoa(), soLuongNhap);

        // 1. Lấy tồn kho hiện tại
        Integer tonKhoHienTai = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        if (tonKhoHienTai < soLuongNhap) {
            throw new IllegalStateException(
                    "Không thể hủy duyệt: Hàng hóa '" + hangHoa.getTenHangHoa() +
                            "' đã xuất " + (soLuongNhap - tonKhoHienTai) + ". " +
                            "Vui lòng nhập lại hoặc hủy các phiếu xuất liên quan."
            );
        }

        // 2. Giảm tồn kho
        hangHoaService.capNhatTonKhoSauXuat(hangHoa.getId(), soLuongNhap);

        // 3. Xử lý lô hàng (nếu có)
        if (chiTiet.getLoHang() != null) {
            LoHang loHang = chiTiet.getLoHang();

            // Trừ số lượng từ lô
            loHang.setSoLuongNhap(loHang.getSoLuongNhap() - soLuongNhap);
            loHang.setSoLuongHienTai(loHang.getSoLuongHienTai() - soLuongNhap);

            // Nếu lô về 0 → Xóa lô
            if (loHang.getSoLuongNhap() <= 0) {
                loHangRepository.delete(loHang);
                log.info("🗑️ Deleted lo_hang ID={}", loHang.getId());
            } else {
                loHangRepository.save(loHang);
            }
        }

        // 4. Xóa/Giảm hang_hoa_vi_tri
        if (chiTiet.getViTriKho() != null) {
            Long hangHoaId = hangHoa.getId();
            Long viTriKhoId = chiTiet.getViTriKho().getId();
            Long loHangId = chiTiet.getLoHang() != null ? chiTiet.getLoHang().getId() : null;

            Optional<HangHoaViTri> viTriOpt = hangHoaViTriRepository
                    .findByHangHoaIdAndViTriKhoIdAndLoHangId(hangHoaId, viTriKhoId, loHangId);

            if (viTriOpt.isPresent()) {
                HangHoaViTri viTri = viTriOpt.get();
                viTri.setSoLuong(viTri.getSoLuong() - soLuongNhap);

                if (viTri.getSoLuong() <= 0) {
                    hangHoaViTriRepository.delete(viTri);
                } else {
                    hangHoaViTriRepository.save(viTri);
                }

                // Cập nhật trạng thái vị trí kho
                updateViTriKhoStatus(chiTiet.getViTriKho());
            }
        }

        // 5. Ghi lịch sử
        LichSuTonKho lichSu = LichSuTonKho.builder()
                .hangHoa(hangHoa)
                .loHang(chiTiet.getLoHang())
                .viTriKho(chiTiet.getViTriKho())
                .loaiBienDong(LichSuTonKho.LoaiBienDong.HUY_DUYET_NHAP)
                .soLuongTruoc(tonKhoHienTai)
                .soLuongBienDong(soLuongNhap)
                .soLuongSau(tonKhoHienTai - soLuongNhap)
                .donGia(chiTiet.getDonGia())
                .giaTriBienDong(chiTiet.getThanhTien())
                .maChungTu(phieuNhap.getMaPhieuNhap())
                .loaiChungTu(LichSuTonKho.LoaiChungTu.HUY_DUYET_NHAP)
                .lyDo("Hủy duyệt phiếu nhập " + phieuNhap.getMaPhieuNhap())
                .nguoiThucHien(getCurrentUser())
                .build();

        lichSuTonKhoRepository.save(lichSu);

        // 6. Cập nhật trạng thái chi tiết
        chiTiet.setTrangThai(ChiTietPhieuNhap.TrangThaiChiTiet.CHO_NHAP);
        chiTietPhieuNhapRepository.save(chiTiet);
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

            // ✅ THÊM VALIDATION VỊ TRÍ KHO
            if (chiTiet.getViTriKhoId() == null) {
                throw new IllegalArgumentException("Vị trí kho không được để trống");
            }

            // Validate hạn sử dụng
            if (chiTiet.getHanSuDung() != null && chiTiet.getNgaySanXuat() != null) {
                if (chiTiet.getHanSuDung().isBefore(chiTiet.getNgaySanXuat())) {
                    throw new IllegalArgumentException("Hạn sử dụng phải sau ngày sản xuất");
                }
            }

            // ✅ THÊM VALIDATION SỐ LÔ
            HangHoa hangHoa = hangHoaRepository.findById(chiTiet.getHangHoaId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa"));

            if (hangHoa.getCoQuanLyLo() != null && hangHoa.getCoQuanLyLo()) {
                if (chiTiet.getSoLo() == null || chiTiet.getSoLo().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Số lô không được để trống cho hàng hóa: " + hangHoa.getTenHangHoa());
                }
                if (chiTiet.getHanSuDung() == null) {
                    throw new IllegalArgumentException(
                            "Hạn sử dụng không được để trống cho hàng hóa: " + hangHoa.getTenHangHoa());
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

        // ✅ SET VỊ TRÍ KHO
        if (chiTietDTO.getViTriKhoId() != null) {
            ViTriKho viTriKho = viTriKhoRepository.findById(chiTietDTO.getViTriKhoId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí kho"));
            chiTiet.setViTriKho(viTriKho);
        }

        chiTietPhieuNhapRepository.save(chiTiet);
    }

    /**
     * ✅ ĐÃ SỬA: Cập nhật tồn kho khi duyệt phiếu nhập
     * Delegate logic sang HangHoaService
     */
    private void updateInventoryFromNhap(ChiTietPhieuNhap chiTiet) {
        log.info("🔄 Updating inventory for HangHoa ID: {} via HangHoaService",
                chiTiet.getHangHoa().getId());

        // ✅ GỌI METHOD TỪ HangHoaService (thay vì tự xử lý)
        hangHoaService.capNhatTonKhoSauNhap(
                chiTiet.getHangHoa().getId(),    // ID hàng hóa
                chiTiet.getSoLuong(),            // Số lượng nhập
                chiTiet.getDonGia()              // Đơn giá nhập
        );

        log.info("✅ Inventory updated successfully for HangHoa ID: {}",
                chiTiet.getHangHoa().getId());
    }

    /**
     * Sinh mã phiếu nhập tự động - Thread-safe
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized String generateMaPhieuNhap() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("PN-%d%02d%02d-",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        try {
            Long maxNumber = phieuNhapKhoRepository
                    .findMaxNumberByPrefix(prefix + "%", prefix.length());

            return String.format("%s%04d", prefix, (maxNumber != null ? maxNumber : 0) + 1);
        } catch (Exception e) {
            log.error("Error generating ma phieu nhap", e);
            // Fallback: sử dụng timestamp
            return String.format("%s%s", prefix,
                    String.valueOf(System.currentTimeMillis()).substring(8));
        }
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

        // ✅ SỬ DỤNG QUERY TỐI ƯU
        List<ChiTietPhieuNhap> chiTietList =
                chiTietPhieuNhapRepository.findByPhieuNhapIdWithDetails(entity.getId());

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
                        hangHoa.getDonViTinh().getTenDvt() : null)
                .soLuong(entity.getSoLuong())
                .donGia(entity.getDonGia())
                .thanhTien(entity.getThanhTien())
                .tienThue(entity.getTienThue())
                .tyLeThue(entity.getTyLeThue())
                .ngaySanXuat(entity.getNgaySanXuat())
                .hanSuDung(entity.getHanSuDung())
                .soLo(entity.getSoLo())
                .viTriKhoId(entity.getViTriKho() != null ? entity.getViTriKho().getId() : null)
                .tenViTriKho(entity.getViTriKho() != null ? entity.getViTriKho().getTenViTri() : null)
                .loHangId(entity.getLoHang() != null ? entity.getLoHang().getId() : null)

                // ✅ THÊM DÒNG NÀY
                .hinhAnhUrl(hangHoa.getHinhAnhUrl())

                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .build();
    }


//    /**
//     * Tạo hoặc cập nhật lô hàng từ chi tiết phiếu nhập
//     */
//    private LoHang createOrUpdateLoHang(ChiTietPhieuNhap chiTiet) {
//        HangHoa hangHoa = chiTiet.getHangHoa();
//        PhieuNhapKho phieuNhap = chiTiet.getPhieuNhap();
//
//        // ✅ SỬA: Tìm lô hàng theo hangHoaId + soLo + hanSuDung
//        Optional<LoHang> existingLoHang = loHangRepository
//                .findByHangHoaIdAndSoLoAndHanSuDung(
//                        hangHoa.getId(),
//                        chiTiet.getSoLo(),
//                        chiTiet.getHanSuDung()  // ✅ THÊM THAM SỐ NÀY
//                );
//
//        LoHang loHang;
//        if (existingLoHang.isPresent()) {
//            // Cập nhật lô hàng đã tồn tại
//            loHang = existingLoHang.get();
//            int soLuongCu = loHang.getSoLuongNhap();
//            int soLuongMoi = chiTiet.getSoLuong();
//
//            loHang.setSoLuongNhap(soLuongCu + soLuongMoi);
//            loHang.setSoLuongHienTai(loHang.getSoLuongHienTai() + soLuongMoi);
//
//            // Cập nhật giá nhập trung bình (Weighted Average)
//            BigDecimal giaCu = loHang.getGiaNhap();
//            BigDecimal tongGiaTriCu = giaCu.multiply(new BigDecimal(soLuongCu));
//            BigDecimal giaTriMoi = chiTiet.getDonGia().multiply(new BigDecimal(soLuongMoi));
//            BigDecimal giaTrungBinh = tongGiaTriCu.add(giaTriMoi)
//                    .divide(new BigDecimal(loHang.getSoLuongNhap()), 2, RoundingMode.HALF_UP);
//
//            loHang.setGiaNhap(giaTrungBinh);
//
//            log.info("Updated lo hang {} (HSD: {}) - Old qty: {}, New qty: {}, Avg price: {}",
//                    loHang.getSoLo(), loHang.getHanSuDung(), soLuongCu,
//                    loHang.getSoLuongNhap(), giaTrungBinh);
//        } else {
//            // Tạo lô hàng mới
//            loHang = LoHang.builder()
//                    .hangHoa(hangHoa)
//                    .soLo(chiTiet.getSoLo())
//                    .ngaySanXuat(chiTiet.getNgaySanXuat())
//                    .hanSuDung(chiTiet.getHanSuDung())
//                    .soLuongNhap(chiTiet.getSoLuong())
//                    .soLuongHienTai(chiTiet.getSoLuong())
//                    .giaNhap(chiTiet.getDonGia())
//                    .nhaCungCap(phieuNhap.getNhaCungCap())
//                    .soChungTuNhap(phieuNhap.getMaPhieuNhap())
//                    .trangThai(LoHang.TrangThaiLoHang.MOI)
//                    .build();
//
//            log.info("Created new lo hang {} (HSD: {}) for hang hoa ID: {}",
//                    loHang.getSoLo(), loHang.getHanSuDung(), hangHoa.getId());
//        }
//
//        // Xác định và cập nhật trạng thái lô hàng
//        loHang.setTrangThai(determineLoHangStatus(loHang));
//
//        return loHangRepository.save(loHang);
//    }

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
            if (loHang.getHanSuDung().isBefore(now.plusDays(30))) {
                return LoHang.TrangThaiLoHang.GAN_HET_HAN;
            }
        }

        // Đang sử dụng (đã xuất một phần)
        if (loHang.getSoLuongHienTai() < loHang.getSoLuongNhap()) {
            return LoHang.TrangThaiLoHang.DANG_SU_DUNG;
        }

        // Mới (chưa xuất)
        return LoHang.TrangThaiLoHang.MOI;
    }

    /**
     * Cập nhật hoặc tạo mới bản ghi hang_hoa_vi_tri
     */
    private void updateHangHoaViTri(ChiTietPhieuNhap chiTiet, LoHang loHang) {
        Long hangHoaId = chiTiet.getHangHoa().getId();
        Long viTriKhoId = chiTiet.getViTriKho().getId();
        Long loHangId = loHang != null ? loHang.getId() : null;

        // Tìm bản ghi hiện có
        Optional<HangHoaViTri> existingOpt = hangHoaViTriRepository
                .findByHangHoaIdAndViTriKhoIdAndLoHangId(hangHoaId, viTriKhoId, loHangId);

        HangHoaViTri hangHoaViTri;

        if (existingOpt.isPresent()) {
            // Cập nhật số lượng
            hangHoaViTri = existingOpt.get();
            hangHoaViTri.setSoLuong(hangHoaViTri.getSoLuong() + chiTiet.getSoLuong());
            log.info("Updated hang_hoa_vi_tri: HangHoa={}, ViTri={}, LoHang={}, OldQty={}, NewQty={}",
                    hangHoaId, viTriKhoId, loHangId,
                    hangHoaViTri.getSoLuong() - chiTiet.getSoLuong(),
                    hangHoaViTri.getSoLuong());
        } else {
            // Tạo mới
            hangHoaViTri = HangHoaViTri.builder()
                    .hangHoa(chiTiet.getHangHoa())
                    .viTriKho(chiTiet.getViTriKho())
                    .loHang(loHang)
                    .soLuong(chiTiet.getSoLuong())
                    .build();
            log.info("Created new hang_hoa_vi_tri: HangHoa={}, ViTri={}, LoHang={}, Qty={}",
                    hangHoaId, viTriKhoId, loHangId, chiTiet.getSoLuong());
        }

        hangHoaViTriRepository.save(hangHoaViTri);

        // Cập nhật trạng thái vị trí kho
        updateViTriKhoStatus(chiTiet.getViTriKho());
    }

    /**
     * Cập nhật trạng thái vị trí kho dựa trên số lượng hàng
     */
    private void updateViTriKhoStatus(ViTriKho viTriKho) {
        Long soLuongHangHoa = hangHoaViTriRepository.countByViTriKhoId(viTriKho.getId());

        ViTriKho.TrangThaiViTri trangThaiMoi;

        if (soLuongHangHoa == 0) {
            trangThaiMoi = ViTriKho.TrangThaiViTri.TRONG;
        } else if (viTriKho.getSucChuaToiDa() != null &&
                viTriKho.getSucChuaToiDa() > 0 &&
                soLuongHangHoa >= viTriKho.getSucChuaToiDa() * 0.9) {
            trangThaiMoi = ViTriKho.TrangThaiViTri.DAY;
        } else {
            trangThaiMoi = ViTriKho.TrangThaiViTri.CO_HANG;
        }

        // ✅ THÊM SAVE VÀ CHECK
        if (viTriKho.getTrangThai() != trangThaiMoi) {
            viTriKho.setTrangThai(trangThaiMoi);
            viTriKhoRepository.save(viTriKho);
            log.info("Updated vi tri kho ID: {} status to {}",
                    viTriKho.getId(), trangThaiMoi);
        }
    }


}