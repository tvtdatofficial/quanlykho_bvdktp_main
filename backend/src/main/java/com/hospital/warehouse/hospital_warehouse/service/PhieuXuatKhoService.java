package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.dto.PhieuXuatKhoDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhieuXuatKhoService {

    private final PhieuXuatKhoRepository phieuXuatKhoRepository;
    private final ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;
    private final KhoRepository khoRepository;
    private final HangHoaRepository hangHoaRepository;
    private final LoHangRepository loHangRepository;
    private final UserRepository userRepository;
    private final HangHoaViTriRepository hangHoaViTriRepository;
    private final ViTriKhoRepository viTriKhoRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;
    private final KhoaPhongRepository khoaPhongRepository;
    private final LoHangService loHangService;  // ✅ THÊM DÒNG NÀY

    private final HangHoaService hangHoaService;

    /**
     * Lấy danh sách phiếu xuất có phân trang và lọc
     */
    @Transactional(readOnly = true)
    public PageResponse<PhieuXuatKhoDTO> getAllPhieuXuat(
            String search,
            Long khoId,
            Long khoaPhongId,
            PhieuXuatKho.TrangThaiPhieuXuat trangThai,
            LocalDate tuNgay,
            LocalDate denNgay,
            Pageable pageable) {

        Specification<PhieuXuatKho> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo mã phiếu xuất
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("maPhieuXuat")), searchPattern));
            }

            // Lọc theo kho
            if (khoId != null) {
                predicates.add(cb.equal(root.get("kho").get("id"), khoId));
            }

            // Lọc theo khoa phòng yêu cầu
            if (khoaPhongId != null) {
                predicates.add(cb.equal(root.get("khoaPhongYeuCau").get("id"), khoaPhongId));
            }

            // Lọc theo trạng thái
            if (trangThai != null) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }

            // Lọc theo khoảng thời gian
            if (tuNgay != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("ngayXuat").as(LocalDate.class), tuNgay));
            }

            if (denNgay != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("ngayXuat").as(LocalDate.class), denNgay));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PhieuXuatKho> page = phieuXuatKhoRepository.findAll(spec, pageable);

        List<PhieuXuatKhoDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<PhieuXuatKhoDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Lấy chi tiết phiếu xuất theo ID
     */
    @Transactional(readOnly = true)
    public Optional<PhieuXuatKhoDTO> getPhieuXuatById(Long id) {
        return phieuXuatKhoRepository.findById(id)
                .map(this::convertToDTOWithDetails);
    }

    /**
     * Lấy chi tiết phiếu xuất theo mã phiếu
     */
    @Transactional(readOnly = true)
    public Optional<PhieuXuatKhoDTO> getPhieuXuatByMa(String maPhieuXuat) {
        return phieuXuatKhoRepository.findByMaPhieuXuat(maPhieuXuat)
                .map(this::convertToDTOWithDetails);
    }

    /**
     * Tạo phiếu xuất mới
     */
    @Transactional
    public PhieuXuatKhoDTO createPhieuXuat(PhieuXuatKhoDTO dto) {
        log.info("Creating phieu xuat with data: {}", dto);

        // Validate dữ liệu đầu vào
        validatePhieuXuatData(dto);

        User currentUser = getCurrentUser();
        Kho kho = khoRepository.findById(dto.getKhoId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho"));

        // Tạo entity phiếu xuất
        PhieuXuatKho phieuXuat = new PhieuXuatKho();
        phieuXuat.setMaPhieuXuat(generateMaPhieuXuat());
        phieuXuat.setKho(kho);

        // Set khoa phòng yêu cầu nếu có
        if (dto.getKhoaPhongYeuCauId() != null) {
            KhoaPhong khoaPhong = khoaPhongRepository.findById(dto.getKhoaPhongYeuCauId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoa phòng"));
            phieuXuat.setKhoaPhongYeuCau(khoaPhong);
        }

        // Set thông tin cơ bản
        phieuXuat.setLoaiXuat(dto.getLoaiXuat());
        phieuXuat.setSoPhieuYeuCau(dto.getSoPhieuYeuCau());
        phieuXuat.setNgayYeuCau(dto.getNgayYeuCau());
        phieuXuat.setNgayXuat(dto.getNgayXuat() != null ? dto.getNgayXuat() : LocalDateTime.now());
        phieuXuat.setNguoiYeuCau(dto.getNguoiYeuCau());
        phieuXuat.setSdtNguoiYeuCau(dto.getSdtNguoiYeuCau());
        phieuXuat.setNguoiNhan(dto.getNguoiNhan());
        phieuXuat.setSdtNguoiNhan(dto.getSdtNguoiNhan());
        phieuXuat.setDiaChiGiao(dto.getDiaChiGiao());
        phieuXuat.setNguoiXuat(currentUser);
        phieuXuat.setLyDoXuat(dto.getLyDoXuat());
        phieuXuat.setGhiChu(dto.getGhiChu());
        phieuXuat.setTrangThai(PhieuXuatKho.TrangThaiPhieuXuat.XUAT);
        phieuXuat.setCreatedBy(currentUser);

        // Tính tổng giá trị
        BigDecimal tongGiaTri = BigDecimal.ZERO;
        if (dto.getChiTiet() != null && !dto.getChiTiet().isEmpty()) {
            for (PhieuXuatKhoDTO.ChiTietPhieuXuatDTO chiTietDTO : dto.getChiTiet()) {
                tongGiaTri = tongGiaTri.add(chiTietDTO.getThanhTien());
            }
        }
        phieuXuat.setTongGiaTri(tongGiaTri);

        // Lưu phiếu xuất
        phieuXuat = phieuXuatKhoRepository.save(phieuXuat);

        // Lưu chi tiết phiếu xuất
        if (dto.getChiTiet() != null && !dto.getChiTiet().isEmpty()) {
            for (PhieuXuatKhoDTO.ChiTietPhieuXuatDTO chiTietDTO : dto.getChiTiet()) {
                saveChiTietPhieuXuat(phieuXuat, chiTietDTO);
            }
        }

        log.info("Created phieu xuat successfully with ID: {}", phieuXuat.getId());
        return convertToDTOWithDetails(phieuXuat);
    }

    /**
     * Cập nhật thông tin phiếu xuất
     */
    @Transactional
    public PhieuXuatKhoDTO updatePhieuXuat(Long id, PhieuXuatKhoDTO dto) {
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất"));

        // Kiểm tra trạng thái
        if (phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET ||
                phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_GIAO) {
            throw new IllegalStateException("Không thể sửa phiếu xuất đã duyệt hoặc đã giao");
        }

        // Cập nhật thông tin
        phieuXuat.setNguoiYeuCau(dto.getNguoiYeuCau());
        phieuXuat.setSdtNguoiYeuCau(dto.getSdtNguoiYeuCau());
        phieuXuat.setNguoiNhan(dto.getNguoiNhan());
        phieuXuat.setSdtNguoiNhan(dto.getSdtNguoiNhan());
        phieuXuat.setDiaChiGiao(dto.getDiaChiGiao());
        phieuXuat.setLyDoXuat(dto.getLyDoXuat());
        phieuXuat.setGhiChu(dto.getGhiChu());
        phieuXuat.setUpdatedBy(getCurrentUser());

        return convertToDTOWithDetails(phieuXuatKhoRepository.save(phieuXuat));
    }

    /**
     * Duyệt phiếu xuất - Logic quan trọng nhất
     */
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuatKhoDTO duyetPhieuXuat(Long id) {
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất"));

        // Validate trạng thái
        if (phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET) {
            throw new IllegalStateException("Phiếu xuất đã được duyệt");
        }
        if (phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.HUY) {
            throw new IllegalStateException("Không thể duyệt phiếu xuất đã hủy");
        }

        User currentUser = getCurrentUser();
        List<ChiTietPhieuXuat> chiTietList = chiTietPhieuXuatRepository.findByPhieuXuatId(id);

        if (chiTietList.isEmpty()) {
            throw new IllegalStateException("Phiếu xuất không có chi tiết");
        }

        try {
            // Xử lý từng chi tiết
            for (ChiTietPhieuXuat chiTiet : chiTietList) {
                processChiTietXuatKho(chiTiet, phieuXuat);
            }

            // Cập nhật trạng thái phiếu xuất
            phieuXuat.setNguoiDuyet(currentUser);
            phieuXuat.setNgayDuyet(LocalDateTime.now());
            phieuXuat.setTrangThai(PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET);
            phieuXuatKhoRepository.save(phieuXuat);

            log.info("✅ Successfully approved phieu xuat ID: {} by user: {}",
                    id, currentUser.getTenDangNhap());

            return convertToDTOWithDetails(phieuXuat);

        } catch (Exception e) {
            log.error("❌ Error approving phieu xuat ID: {}", id, e);
            throw new RuntimeException("Lỗi khi duyệt phiếu xuất: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý xuất kho theo FEFO (First Expired First Out)
     */
    private void processChiTietXuatKho(ChiTietPhieuXuat chiTiet, PhieuXuatKho phieuXuat) {
        HangHoa hangHoa = chiTiet.getHangHoa();
        Integer soLuongCanXuat = chiTiet.getSoLuongXuat();
        Long khoId = phieuXuat.getKho().getId();

        log.info("🔄 Processing xuat kho: HangHoa={}, Kho={}, SoLuong={}, QuanLyLo={}",
                hangHoa.getTenHangHoa(), khoId, soLuongCanXuat, hangHoa.getCoQuanLyLo());

        // 1. Kiểm tra tồn kho
        Integer tonKhoHienTai = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        if (tonKhoHienTai < soLuongCanXuat) {
            throw new IllegalStateException(String.format(
                    "❌ Không đủ hàng để xuất!\n\n" +
                            "Hàng hóa: %s\n" +
                            "Tồn kho: %d\n" +
                            "Yêu cầu xuất: %d\n" +
                            "Còn thiếu: %d\n\n" +
                            "💡 Vui lòng nhập thêm hàng hoặc giảm số lượng xuất.",
                    hangHoa.getTenHangHoa(), tonKhoHienTai, soLuongCanXuat,
                    soLuongCanXuat - tonKhoHienTai
            ));
        }

        Integer soLuongTruocXuat = tonKhoHienTai;

        // ✅ 2. PHÂN BIỆT 2 TRƯỜNG HỢP
        if (hangHoa.getCoQuanLyLo() != null && hangHoa.getCoQuanLyLo()) {
            // ========== TRƯỜNG HỢP 1: CÓ QUẢN LÝ LÔ ==========
            log.info("📦 Hàng có quản lý lô → Xuất theo FIFO");

            List<LoHang> danhSachLoHang = loHangRepository.findAvailableLoHangForXuat(
                    hangHoa.getId(),
                    khoId,
                    0
            );

            log.info("📦 Found {} available lots", danhSachLoHang.size());

            if (danhSachLoHang.isEmpty()) {
                // Kiểm tra xem có lô ở kho khác không
                List<LoHang> loHangKhoKhac = loHangRepository
                        .findByHangHoaIdAndSoLuongHienTaiGreaterThan(hangHoa.getId(), 0);

                String errorMsg;
                if (loHangKhoKhac.isEmpty()) {
                    errorMsg = String.format(
                            "❌ Không thể xuất '%s'!\n\n" +
                                    "📦 Hàng này CÓ QUẢN LÝ LÔ nhưng CHƯA CÓ LÔ NÀO trong hệ thống.\n\n" +
                                    "💡 Giải pháp:\n" +
                                    "1. Tạo PHIẾU NHẬP với thông tin lô (số lô, HSD)\n" +
                                    "2. Sau đó mới có thể xuất\n\n" +
                                    "Chi tiết:\n" +
                                    "- Kho: %s (ID: %d)\n" +
                                    "- Cần xuất: %d",
                            hangHoa.getTenHangHoa(),
                            phieuXuat.getKho().getTenKho(), khoId,
                            soLuongCanXuat
                    );
                } else {
                    StringBuilder khoInfo = new StringBuilder();
                    for (LoHang lo : loHangKhoKhac) {
                        if (lo.getKho() != null) {
                            khoInfo.append(String.format(
                                    "\n  • %s: %d (Lô: %s, HSD: %s)",
                                    lo.getKho().getTenKho(),
                                    lo.getSoLuongHienTai(),
                                    lo.getSoLo(),
                                    lo.getHanSuDung() != null ? lo.getHanSuDung().toString() : "N/A"
                            ));
                        }
                    }

                    errorMsg = String.format(
                            "❌ Không thể xuất '%s' từ '%s'!\n\n" +
                                    "⚠️ Hàng này KHÔNG CÓ LÔ trong kho bạn chọn.\n" +
                                    "📍 Hàng đang có ở:%s\n\n" +
                                    "💡 Giải pháp:\n" +
                                    "1. Nhập hàng vào '%s', hoặc\n" +
                                    "2. Chuyển kho, hoặc\n" +
                                    "3. Đổi kho xuất\n\n" +
                                    "Chi tiết: Cần xuất %d",
                            hangHoa.getTenHangHoa(),
                            phieuXuat.getKho().getTenKho(),
                            khoInfo.toString(),
                            phieuXuat.getKho().getTenKho(),
                            soLuongCanXuat
                    );
                }

                log.error("❌ {}", errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            // Xuất theo từng lô (FIFO)
            Integer soLuongDaXuat = 0;
            for (LoHang loHang : danhSachLoHang) {
                if (soLuongDaXuat >= soLuongCanXuat) break;

                Integer soLuongCoTheLay = Math.min(
                        loHang.getSoLuongHienTai(),
                        soLuongCanXuat - soLuongDaXuat
                );

                log.info("📤 XUAT from Lot: ID={}, SoLo={}, Before={}, XuatRa={}",
                        loHang.getId(), loHang.getSoLo(),
                        loHang.getSoLuongHienTai(), soLuongCoTheLay);

                // Trừ số lượng từ lô
                loHangService.truSoLuongLo(loHang.getId(), soLuongCoTheLay);

                // Cập nhật hang_hoa_vi_tri
                updateHangHoaViTriAfterXuat(hangHoa.getId(), loHang.getId(), soLuongCoTheLay);

                soLuongDaXuat += soLuongCoTheLay;
            }

            if (soLuongDaXuat < soLuongCanXuat) {
                throw new IllegalStateException(String.format(
                        "Không đủ hàng trong các lô. Cần: %d, Có: %d, Thiếu: %d",
                        soLuongCanXuat, soLuongDaXuat, soLuongCanXuat - soLuongDaXuat
                ));
            }

        } else {
            // ========== TRƯỜNG HỢP 2: KHÔNG QUẢN LÝ LÔ ==========
            log.info("📦 Hàng KHÔNG quản lý lô → Trừ trực tiếp từ tồn kho");

            // Không cần kiểm tra lô, chỉ cần tồn kho đủ (đã check ở bước 1)
            // Không cần làm gì thêm, chỉ cập nhật tồn kho ở bước sau
        }

        // ✅ 3. Cập nhật tồn kho hàng hóa (cho cả 2 trường hợp)
        log.info("🔄 Updating inventory via HangHoaService");
        hangHoaService.capNhatTonKhoSauXuat(hangHoa.getId(), soLuongCanXuat);

        // ✅ 4. Ghi lịch sử tồn kho
        ghiLichSuTonKho(chiTiet, phieuXuat, soLuongTruocXuat,
                hangHoa.getSoLuongCoTheXuat());

        // ✅ 5. Cập nhật trạng thái chi tiết
        chiTiet.setTrangThai(ChiTietPhieuXuat.TrangThaiChiTiet.DA_XUAT);
        chiTietPhieuXuatRepository.save(chiTiet);

        log.info("✅ Xuất kho thành công: {} x {}",
                hangHoa.getTenHangHoa(), soLuongCanXuat);
    }

    /**
     * Cập nhật hang_hoa_vi_tri sau khi xuất
     */
    private void updateHangHoaViTriAfterXuat(Long hangHoaId, Long loHangId, Integer soLuongXuat) {
        List<HangHoaViTri> viTriList = hangHoaViTriRepository
                .findByHangHoaIdAndLoHangId(hangHoaId, loHangId);

        Integer soLuongConLai = soLuongXuat;

        for (HangHoaViTri viTri : viTriList) {
            if (soLuongConLai <= 0) break;

            Integer soLuongTru = Math.min(viTri.getSoLuong(), soLuongConLai);
            viTri.setSoLuong(viTri.getSoLuong() - soLuongTru);

            if (viTri.getSoLuong() <= 0) {
                hangHoaViTriRepository.delete(viTri);
                log.info("🗑️ Deleted hang_hoa_vi_tri: ViTri={}", viTri.getViTriKho().getTenViTri());
            } else {
                hangHoaViTriRepository.save(viTri);
            }

            // Cập nhật trạng thái vị trí kho
            updateViTriKhoStatus(viTri.getViTriKho());

            soLuongConLai -= soLuongTru;
        }
    }

    /**
     * Ghi lịch sử tồn kho cho xuất kho
     */
    private void ghiLichSuTonKho(ChiTietPhieuXuat chiTiet,
                                 PhieuXuatKho phieuXuat,
                                 Integer soLuongTruoc,
                                 Integer soLuongSau) {
        try {
            LichSuTonKho lichSu = LichSuTonKho.builder()
                    .hangHoa(chiTiet.getHangHoa())
                    .loHang(chiTiet.getLoHang())
                    .viTriKho(chiTiet.getViTriKho())
                    .loaiBienDong(LichSuTonKho.LoaiBienDong.XUAT_KHO)
                    .soLuongTruoc(soLuongTruoc)
                    .soLuongBienDong(chiTiet.getSoLuongXuat())
                    .soLuongSau(soLuongSau)
                    .donGia(chiTiet.getDonGia())
                    .giaTriBienDong(chiTiet.getThanhTien())
                    .maChungTu(phieuXuat.getMaPhieuXuat())
                    .loaiChungTu(LichSuTonKho.LoaiChungTu.PHIEU_XUAT)
                    .lyDo("Xuất kho từ phiếu xuất " + phieuXuat.getMaPhieuXuat())
                    .nguoiThucHien(getCurrentUser())
                    .build();

            lichSuTonKhoRepository.save(lichSu);

            log.info("📝 Saved lich su ton kho: HangHoa={}, Before={}, After={}, Delta=-{}",
                    chiTiet.getHangHoa().getTenHangHoa(),
                    soLuongTruoc,
                    soLuongSau,
                    chiTiet.getSoLuongXuat());
        } catch (Exception e) {
            log.error("❌ Error saving lich su ton kho", e);
            throw new RuntimeException("Lỗi ghi lịch sử tồn kho: " + e.getMessage());
        }
    }

    /**
     * Hủy phiếu xuất
     */
    @Transactional
    public PhieuXuatKhoDTO huyPhieuXuat(Long id, String lyDoHuy) {
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất"));

        if (phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET ||
                phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_GIAO) {
            throw new IllegalStateException("Không thể hủy phiếu xuất đã duyệt hoặc đã giao");
        }

        if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hủy không được để trống");
        }

        phieuXuat.setTrangThai(PhieuXuatKho.TrangThaiPhieuXuat.HUY);
        phieuXuat.setLyDoHuy(lyDoHuy);
        phieuXuat.setUpdatedBy(getCurrentUser());

        log.info("Cancelled phieu xuat ID: {} with reason: {}", id, lyDoHuy);
        return convertToDTOWithDetails(phieuXuatKhoRepository.save(phieuXuat));
    }

    /**
     * ✅ BỔ SUNG: Hủy duyệt phiếu xuất (chỉ ADMIN)
     */
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuatKhoDTO huyDuyetPhieuXuat(Long id, String lyDoHuyDuyet) {
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất"));

        if (phieuXuat.getTrangThai() != PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET) {
            throw new IllegalStateException("Chỉ có thể hủy duyệt phiếu đã duyệt");
        }

        if (lyDoHuyDuyet == null || lyDoHuyDuyet.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do hủy duyệt không được để trống");
        }

        try {
            List<ChiTietPhieuXuat> chiTietList = chiTietPhieuXuatRepository.findByPhieuXuatId(id);

            // Hoàn nguyên từng chi tiết (CỘNG LẠI TỒN KHO)
            for (ChiTietPhieuXuat chiTiet : chiTietList) {
                rollbackChiTietXuatKho(chiTiet, phieuXuat);
            }

            // Cập nhật trạng thái
            phieuXuat.setTrangThai(PhieuXuatKho.TrangThaiPhieuXuat.CHO_DUYET);
            phieuXuat.setNguoiDuyet(null);
            phieuXuat.setNgayDuyet(null);
            phieuXuat.setGhiChu(
                    (phieuXuat.getGhiChu() != null ? phieuXuat.getGhiChu() + "\n\n" : "") +
                            "⚠️ ĐÃ HỦY DUYỆT\n" +
                            "Lý do: " + lyDoHuyDuyet + "\n" +
                            "Người thực hiện: " + getCurrentUser().getHoTen() + "\n" +
                            "Thời gian: " + LocalDateTime.now()
            );
            phieuXuatKhoRepository.save(phieuXuat);

            log.info("✅ Successfully rolled back phieu xuat ID: {}", id);
            return convertToDTOWithDetails(phieuXuat);

        } catch (Exception e) {
            log.error("❌ Error rolling back phieu xuat ID: {}", id, e);
            throw new RuntimeException("Lỗi khi hủy duyệt phiếu xuất: " + e.getMessage(), e);
        }
    }

    /**
     * Hoàn nguyên một chi tiết phiếu xuất (CỘNG LẠI TỒN KHO)
     */
    private void rollbackChiTietXuatKho(ChiTietPhieuXuat chiTiet, PhieuXuatKho phieuXuat) {
        HangHoa hangHoa = chiTiet.getHangHoa();
        Integer soLuongXuat = chiTiet.getSoLuongXuat();

        log.info("🔄 Rolling back xuat: HangHoa={}, SoLuong={}",
                hangHoa.getTenHangHoa(), soLuongXuat);

        Integer tonKhoHienTai = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        // 1. Cộng lại tồn kho
        hangHoaService.capNhatTonKhoSauNhap(
                hangHoa.getId(),
                soLuongXuat,
                chiTiet.getDonGia()
        );

        // 2. Cộng lại lô hàng (nếu có)
        if (chiTiet.getLoHang() != null) {
            LoHang loHang = chiTiet.getLoHang();
            loHang.setSoLuongHienTai(loHang.getSoLuongHienTai() + soLuongXuat);
            loHangRepository.save(loHang);
            log.info("✅ Restored lo_hang ID={}, new qty={}",
                    loHang.getId(), loHang.getSoLuongHienTai());
        }

        // 3. Ghi lịch sử
        LichSuTonKho lichSu = LichSuTonKho.builder()
                .hangHoa(hangHoa)
                .loHang(chiTiet.getLoHang())
                .viTriKho(chiTiet.getViTriKho())
                .loaiBienDong(LichSuTonKho.LoaiBienDong.HUY_DUYET_XUAT)
                .soLuongTruoc(tonKhoHienTai)
                .soLuongBienDong(soLuongXuat)
                .soLuongSau(tonKhoHienTai + soLuongXuat)
                .donGia(chiTiet.getDonGia())
                .giaTriBienDong(chiTiet.getThanhTien())
                .maChungTu(phieuXuat.getMaPhieuXuat())
                .loaiChungTu(LichSuTonKho.LoaiChungTu.HUY_DUYET_XUAT)
                .lyDo("Hủy duyệt phiếu xuất " + phieuXuat.getMaPhieuXuat())
                .nguoiThucHien(getCurrentUser())
                .build();

        lichSuTonKhoRepository.save(lichSu);

        // 4. Cập nhật trạng thái
        chiTiet.setTrangThai(ChiTietPhieuXuat.TrangThaiChiTiet.CHO_XUAT);
        chiTietPhieuXuatRepository.save(chiTiet);
    }

    /**
     * Xóa phiếu xuất
     */
    @Transactional
    public void deletePhieuXuat(Long id) {
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất"));

        if (phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET ||
                phieuXuat.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_GIAO) {
            throw new IllegalStateException("Không thể xóa phiếu xuất đã duyệt hoặc đã giao");
        }

        // Xóa chi tiết trước
        chiTietPhieuXuatRepository.deleteByPhieuXuatId(id);

        // Xóa phiếu xuất
        phieuXuatKhoRepository.deleteById(id);

        log.info("Deleted phieu xuat ID: {}", id);
    }

    /**
     * Lấy danh sách phiếu xuất chờ duyệt
     */
    @Transactional(readOnly = true)
    public List<PhieuXuatKhoDTO> getPhieuXuatChoDuyet() {
        return phieuXuatKhoRepository
                .findByTrangThai(PhieuXuatKho.TrangThaiPhieuXuat.CHO_DUYET)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate dữ liệu phiếu xuất
     */
    private void validatePhieuXuatData(PhieuXuatKhoDTO dto) {
        if (dto.getKhoId() == null) {
            throw new IllegalArgumentException("Kho không được để trống");
        }

        if (dto.getLyDoXuat() == null || dto.getLyDoXuat().trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do xuất không được để trống");
        }

        if (dto.getChiTiet() == null || dto.getChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Chi tiết phiếu xuất không được để trống");
        }

        for (PhieuXuatKhoDTO.ChiTietPhieuXuatDTO chiTiet : dto.getChiTiet()) {
            if (chiTiet.getHangHoaId() == null) {
                throw new IllegalArgumentException("Hàng hóa không được để trống");
            }
            if (chiTiet.getSoLuongXuat() == null || chiTiet.getSoLuongXuat() <= 0) {
                throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
            }
            if (chiTiet.getDonGia() == null || chiTiet.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Đơn giá phải lớn hơn 0");
            }
        }
    }

    /**
     * Lưu chi tiết phiếu xuất
     */
    private void saveChiTietPhieuXuat(PhieuXuatKho phieuXuat,
                                      PhieuXuatKhoDTO.ChiTietPhieuXuatDTO chiTietDTO) {

        HangHoa hangHoa = hangHoaRepository.findById(chiTietDTO.getHangHoaId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa"));

        // Lấy tồn kho hiện tại
        Integer tonKhoHienTai = hangHoa.getSoLuongCoTheXuat() != null ?
                hangHoa.getSoLuongCoTheXuat() : 0;

        ChiTietPhieuXuat chiTiet = new ChiTietPhieuXuat();
        chiTiet.setPhieuXuat(phieuXuat);
        chiTiet.setHangHoa(hangHoa);
        chiTiet.setSoLuongYeuCau(chiTietDTO.getSoLuongYeuCau());
        chiTiet.setSoLuongXuat(chiTietDTO.getSoLuongXuat());
        chiTiet.setTonKhoHienTai(tonKhoHienTai);
        chiTiet.setDonGia(chiTietDTO.getDonGia());
        chiTiet.setThanhTien(chiTietDTO.getThanhTien());
        chiTiet.setGhiChu(chiTietDTO.getGhiChu());
        chiTiet.setTrangThai(ChiTietPhieuXuat.TrangThaiChiTiet.CHO_XUAT);

        chiTietPhieuXuatRepository.save(chiTiet);
    }

//    /**
//     * Xác định trạng thái lô hàng
//     */
//    private LoHang.TrangThaiLoHang determineLoHangStatus(LoHang loHang) {
//        if (loHang.getSoLuongHienTai() <= 0) {
//            return LoHang.TrangThaiLoHang.HET_HANG;
//        }
//
//        if (loHang.getHanSuDung() != null) {
//            LocalDate now = LocalDate.now();
//            if (loHang.getHanSuDung().isBefore(now)) {
//                return LoHang.TrangThaiLoHang.HET_HAN;
//            }
//            if (loHang.getHanSuDung().isBefore(now.plusDays(30))) {
//                return LoHang.TrangThaiLoHang.GAN_HET_HAN;
//            }
//        }
//
//        if (loHang.getSoLuongHienTai() < loHang.getSoLuongNhap()) {
//            return LoHang.TrangThaiLoHang.DANG_SU_DUNG;
//        }
//
//        return LoHang.TrangThaiLoHang.MOI;
//    }

    /**
     * Cập nhật trạng thái vị trí kho
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

        if (viTriKho.getTrangThai() != trangThaiMoi) {
            viTriKho.setTrangThai(trangThaiMoi);
            viTriKhoRepository.save(viTriKho);
            log.info("Updated vi tri kho ID: {} status to {}",
                    viTriKho.getId(), trangThaiMoi);
        }
    }

    /**
     * Sinh mã phiếu xuất tự động - Thread-safe
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized String generateMaPhieuXuat() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("PX-%d%02d%02d-",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        long count = phieuXuatKhoRepository.countByMaPhieuXuatStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    /**
     * Lấy thông tin user hiện tại
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
    private PhieuXuatKhoDTO convertToDTO(PhieuXuatKho entity) {
        return PhieuXuatKhoDTO.builder()
                .id(entity.getId())
                .maPhieuXuat(entity.getMaPhieuXuat())
                .khoId(entity.getKho().getId())
                .tenKho(entity.getKho().getTenKho())
                .khoaPhongYeuCauId(entity.getKhoaPhongYeuCau() != null ?
                        entity.getKhoaPhongYeuCau().getId() : null)
                .tenKhoaPhong(entity.getKhoaPhongYeuCau() != null ?
                        entity.getKhoaPhongYeuCau().getTenKhoaPhong() : null)
                .loaiXuat(entity.getLoaiXuat())
                .soPhieuYeuCau(entity.getSoPhieuYeuCau())
                .ngayYeuCau(entity.getNgayYeuCau())
                .ngayXuat(entity.getNgayXuat())
                .tongGiaTri(entity.getTongGiaTri())
                .nguoiYeuCau(entity.getNguoiYeuCau())
                .sdtNguoiYeuCau(entity.getSdtNguoiYeuCau())
                .nguoiNhan(entity.getNguoiNhan())
                .sdtNguoiNhan(entity.getSdtNguoiNhan())
                .diaChiGiao(entity.getDiaChiGiao())
                .nguoiXuatId(entity.getNguoiXuat() != null ?
                        entity.getNguoiXuat().getId() : null)
                .tenNguoiXuat(entity.getNguoiXuat() != null ?
                        entity.getNguoiXuat().getHoTen() : null)
                .nguoiDuyetId(entity.getNguoiDuyet() != null ?
                        entity.getNguoiDuyet().getId() : null)
                .tenNguoiDuyet(entity.getNguoiDuyet() != null ?
                        entity.getNguoiDuyet().getHoTen() : null)
                .lyDoXuat(entity.getLyDoXuat())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .lyDoHuy(entity.getLyDoHuy())
                .ngayDuyet(entity.getNgayDuyet())
                .ngayGiao(entity.getNgayGiao())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity sang DTO (có chi tiết)
     */
    private PhieuXuatKhoDTO convertToDTOWithDetails(PhieuXuatKho entity) {
        PhieuXuatKhoDTO dto = convertToDTO(entity);

        List<ChiTietPhieuXuat> chiTietList =
                chiTietPhieuXuatRepository.findByPhieuXuatIdWithDetails(entity.getId());

        List<PhieuXuatKhoDTO.ChiTietPhieuXuatDTO> chiTietDTOs = chiTietList.stream()
                .map(this::convertChiTietToDTO)
                .collect(Collectors.toList());

        dto.setChiTiet(chiTietDTOs);
        return dto;
    }

    /**
     * Convert chi tiết entity sang DTO
     */
    private PhieuXuatKhoDTO.ChiTietPhieuXuatDTO convertChiTietToDTO(ChiTietPhieuXuat entity) {
        HangHoa hangHoa = entity.getHangHoa();

        return PhieuXuatKhoDTO.ChiTietPhieuXuatDTO.builder()
                .id(entity.getId())
                .hangHoaId(hangHoa.getId())
                .maHangHoa(hangHoa.getMaHangHoa())
                .tenHangHoa(hangHoa.getTenHangHoa())
                .tenDonViTinh(hangHoa.getDonViTinh() != null ?
                        hangHoa.getDonViTinh().getTenDvt() : null)
                .loHangId(entity.getLoHang() != null ? entity.getLoHang().getId() : null)
                .soLo(entity.getLoHang() != null ? entity.getLoHang().getSoLo() : null)
                .viTriKhoId(entity.getViTriKho() != null ? entity.getViTriKho().getId() : null)
                .tenViTriKho(entity.getViTriKho() != null ? entity.getViTriKho().getTenViTri() : null)
                .soLuongYeuCau(entity.getSoLuongYeuCau())
                .soLuongXuat(entity.getSoLuongXuat())
                .tonKhoHienTai(entity.getTonKhoHienTai())
                .donGia(entity.getDonGia())
                .thanhTien(entity.getThanhTien())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .build();
    }

    /**
     * Lấy thống kê phiếu xuất
     */
    @Transactional(readOnly = true)
    public PhieuXuatKhoDTO.ThongKePhieuXuat getThongKePhieuXuat(
            Long khoId, LocalDate tuNgay, LocalDate denNgay) {

        Specification<PhieuXuatKho> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (khoId != null) {
                predicates.add(cb.equal(root.get("kho").get("id"), khoId));
            }

            if (tuNgay != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("ngayXuat").as(LocalDate.class), tuNgay));
            }

            if (denNgay != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("ngayXuat").as(LocalDate.class), denNgay));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<PhieuXuatKho> phieuXuatList = phieuXuatKhoRepository.findAll(spec);

        long tongSoPhieu = phieuXuatList.size();
        long soPhieuChoDuyet = phieuXuatList.stream()
                .filter(p -> p.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.CHO_DUYET)
                .count();
        long soPhieuDaDuyet = phieuXuatList.stream()
                .filter(p -> p.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET)
                .count();

        BigDecimal tongGiaTri = phieuXuatList.stream()
                .filter(p -> p.getTrangThai() == PhieuXuatKho.TrangThaiPhieuXuat.DA_DUYET)
                .map(PhieuXuatKho::getTongGiaTri)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PhieuXuatKhoDTO.ThongKePhieuXuat.builder()
                .tongSoPhieu(tongSoPhieu)
                .soPhieuChoDuyet(soPhieuChoDuyet)
                .soPhieuDaDuyet(soPhieuDaDuyet)
                .tongGiaTri(tongGiaTri)
                .build();
    }
}