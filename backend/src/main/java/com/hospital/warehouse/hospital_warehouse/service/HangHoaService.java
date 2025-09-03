package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.HangHoaDTO;
import com.hospital.warehouse.hospital_warehouse.dto.PageResponse;
import com.hospital.warehouse.hospital_warehouse.entity.HangHoa;
import com.hospital.warehouse.hospital_warehouse.entity.DanhMuc;
import com.hospital.warehouse.hospital_warehouse.entity.DonViTinh;
import com.hospital.warehouse.hospital_warehouse.entity.NhaCungCap;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.repository.HangHoaRepository;
import com.hospital.warehouse.hospital_warehouse.repository.DanhMucRepository;
import com.hospital.warehouse.hospital_warehouse.repository.DonViTinhRepository;
import com.hospital.warehouse.hospital_warehouse.repository.NhaCungCapRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HangHoaService {

    private final HangHoaRepository hangHoaRepository;
    private final DanhMucRepository danhMucRepository;
    private final DonViTinhRepository donViTinhRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<HangHoaDTO> getAllHangHoa(String search, Long danhMucId, HangHoa.TrangThaiHangHoa trangThai, Pageable pageable) {
        log.info("Tìm kiếm hàng hóa với từ khóa: {}, danh mục: {}, trạng thái: {}", search, danhMucId, trangThai);

        Page<HangHoa> page = hangHoaRepository.findByTrangThaiAndDanhMucAndSearch(trangThai, danhMucId, search, pageable);

        List<HangHoaDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<HangHoaDTO>builder()
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
    public Optional<HangHoaDTO> getHangHoaById(Long id) {
        log.info("Tìm hàng hóa theo ID: {}", id);
        return hangHoaRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<HangHoaDTO> getHangHoaByMa(String maHangHoa) {
        log.info("Tìm hàng hóa theo mã: {}", maHangHoa);
        return hangHoaRepository.findByMaHangHoa(maHangHoa)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<HangHoaDTO> getHangHoaTonKhoThap() {
        log.info("Lấy danh sách hàng hóa tồn kho thấp");
        return hangHoaRepository.findHangHoaTonKhoThap()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HangHoaDTO> getHangHoaHetHang() {
        log.info("Lấy danh sách hàng hóa hết hàng");
        return hangHoaRepository.findHangHoaHetHang()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HangHoaDTO createHangHoa(HangHoaDTO dto) {
        log.info("Tạo hàng hóa mới: {}", dto.getTenHangHoa());

        // Validate unique constraints
        if (hangHoaRepository.existsByMaHangHoa(dto.getMaHangHoa())) {
            throw new IllegalStateException("Mã hàng hóa đã tồn tại: " + dto.getMaHangHoa());
        }

        if (dto.getMaBarcode() != null && !dto.getMaBarcode().isEmpty() &&
                hangHoaRepository.existsByMaBarcode(dto.getMaBarcode())) {
            throw new IllegalStateException("Mã barcode đã tồn tại: " + dto.getMaBarcode());
        }

        HangHoa entity = convertToEntity(dto);

        // Set relationships
        setEntityRelationships(entity, dto);

        // Set audit fields
        User currentUser = getCurrentUser();
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        HangHoa saved = hangHoaRepository.save(entity);
        log.info("Đã tạo hàng hóa với ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public HangHoaDTO updateHangHoa(Long id, HangHoaDTO dto) {
        log.info("Cập nhật hàng hóa ID: {}", id);

        HangHoa existing = hangHoaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa với ID: " + id));

        // Validate unique constraints (exclude current record)
        hangHoaRepository.findByMaHangHoa(dto.getMaHangHoa())
                .ifPresent(hh -> {
                    if (!hh.getId().equals(id)) {
                        throw new IllegalStateException("Mã hàng hóa đã tồn tại: " + dto.getMaHangHoa());
                    }
                });

        if (dto.getMaBarcode() != null && !dto.getMaBarcode().isEmpty()) {
            hangHoaRepository.findByMaBarcode(dto.getMaBarcode())
                    .ifPresent(hh -> {
                        if (!hh.getId().equals(id)) {
                            throw new IllegalStateException("Mã barcode đã tồn tại: " + dto.getMaBarcode());
                        }
                    });
        }

        // Update basic fields
        updateEntityFromDTO(existing, dto);

        // Update relationships
        setEntityRelationships(existing, dto);

        // Update audit fields
        User currentUser = getCurrentUser();
        existing.setUpdatedBy(currentUser);
        existing.setUpdatedAt(LocalDateTime.now());

        HangHoa updated = hangHoaRepository.save(existing);
        log.info("Đã cập nhật hàng hóa ID: {}", updated.getId());

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteHangHoa(Long id) {
        log.info("Xóa hàng hóa ID: {}", id);

        HangHoa existing = hangHoaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hàng hóa với ID: " + id));

        // Check if product is being used in transactions (add validation logic here)
        // For now, just soft delete by changing status
        existing.setTrangThai(HangHoa.TrangThaiHangHoa.NGUNG_KINH_DOANH);
        existing.setUpdatedBy(getCurrentUser());
        existing.setUpdatedAt(LocalDateTime.now());
        hangHoaRepository.save(existing);

        log.info("Đã ngừng kinh doanh hàng hóa ID: {}", id);
    }

    private void setEntityRelationships(HangHoa entity, HangHoaDTO dto) {
        // Set DanhMuc
        DanhMuc danhMuc = danhMucRepository.findById(dto.getDanhMucId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + dto.getDanhMucId()));
        entity.setDanhMuc(danhMuc);

        // Set DonViTinh
        DonViTinh donViTinh = donViTinhRepository.findById(dto.getDonViTinhId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị tính với ID: " + dto.getDonViTinhId()));
        entity.setDonViTinh(donViTinh);

        // Set NhaCungCap (optional)
        if (dto.getNhaCungCapId() != null) {
            NhaCungCap nhaCungCap = nhaCungCapRepository.findById(dto.getNhaCungCapId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + dto.getNhaCungCapId()));
            entity.setNhaCungCap(nhaCungCap);
        } else {
            entity.setNhaCungCap(null);
        }
    }

    private void updateEntityFromDTO(HangHoa entity, HangHoaDTO dto) {
        entity.setMaHangHoa(dto.getMaHangHoa());
        entity.setTenHangHoa(dto.getTenHangHoa());
        entity.setTenKhoaHoc(dto.getTenKhoaHoc());
        entity.setMaBarcode(dto.getMaBarcode());
        entity.setMaQrCode(dto.getMaQrCode());
        entity.setMoTa(dto.getMoTa());
        entity.setThanhPhan(dto.getThanhPhan());
        entity.setCongDung(dto.getCongDung());
        entity.setCachSuDung(dto.getCachSuDung());
        entity.setLieuLuong(dto.getLieuLuong());
        entity.setDongGoi(dto.getDongGoi());
        entity.setXuatXu(dto.getXuatXu());
        entity.setHangSanXuat(dto.getHangSanXuat());
        entity.setSoDangKy(dto.getSoDangKy());
        entity.setSoLuongToiThieu(dto.getSoLuongToiThieu());
        entity.setSoLuongToiDa(dto.getSoLuongToiDa());
        entity.setTrongLuong(dto.getTrongLuong());
        entity.setKichThuoc(dto.getKichThuoc());
        entity.setMauSac(dto.getMauSac());
        entity.setHinhAnhUrl(dto.getHinhAnhUrl());
        entity.setTaiLieuDinhKem(dto.getTaiLieuDinhKem());
        entity.setYeuCauBaoQuan(dto.getYeuCauBaoQuan());
        entity.setNhietDoBaoQuanMin(dto.getNhietDoBaoQuanMin());
        entity.setNhietDoBaoQuanMax(dto.getNhietDoBaoQuanMax());
        entity.setDoAmBaoQuan(dto.getDoAmBaoQuan());
        entity.setHanSuDungMacDinh(dto.getHanSuDungMacDinh());
        entity.setCanhBaoHetHan(dto.getCanhBaoHetHan());
        entity.setCoQuanLyLo(dto.getCoQuanLyLo());
        entity.setCoHanSuDung(dto.getCoHanSuDung());
        entity.setCoKiemSoatChatLuong(dto.getCoKiemSoatChatLuong());
        entity.setLaThuocDoc(dto.getLaThuocDoc());
        entity.setLaThuocHuongThan(dto.getLaThuocHuongThan());
        entity.setGhiChu(dto.getGhiChu());
        entity.setTrangThai(dto.getTrangThai());
    }

    private HangHoaDTO convertToDTO(HangHoa entity) {
        return HangHoaDTO.builder()
                .id(entity.getId())
                .maHangHoa(entity.getMaHangHoa())
                .tenHangHoa(entity.getTenHangHoa())
                .tenKhoaHoc(entity.getTenKhoaHoc())
                .maBarcode(entity.getMaBarcode())
                .maQrCode(entity.getMaQrCode())
                .danhMucId(entity.getDanhMuc().getId())
                .tenDanhMuc(entity.getDanhMuc().getTenDanhMuc())
                .donViTinhId(entity.getDonViTinh().getId())
                .tenDonViTinh(entity.getDonViTinh().getTenDvt())
                .nhaCungCapId(entity.getNhaCungCap() != null ? entity.getNhaCungCap().getId() : null)
                .tenNhaCungCap(entity.getNhaCungCap() != null ? entity.getNhaCungCap().getTenNcc() : null)
                .moTa(entity.getMoTa())
                .thanhPhan(entity.getThanhPhan())
                .congDung(entity.getCongDung())
                .cachSuDung(entity.getCachSuDung())
                .lieuLuong(entity.getLieuLuong())
                .dongGoi(entity.getDongGoi())
                .xuatXu(entity.getXuatXu())
                .hangSanXuat(entity.getHangSanXuat())
                .soDangKy(entity.getSoDangKy())
                .giaNhapTrungBinh(entity.getGiaNhapTrungBinh())
                .giaXuatTrungBinh(entity.getGiaXuatTrungBinh())
                .tongSoLuong(entity.getTongSoLuong())
                .soLuongCoTheXuat(entity.getSoLuongCoTheXuat())
                .soLuongDaDat(entity.getSoLuongDaDat())
                .soLuongToiThieu(entity.getSoLuongToiThieu())
                .soLuongToiDa(entity.getSoLuongToiDa())
                .trongLuong(entity.getTrongLuong())
                .kichThuoc(entity.getKichThuoc())
                .mauSac(entity.getMauSac())
                .hinhAnhUrl(entity.getHinhAnhUrl())
                .taiLieuDinhKem(entity.getTaiLieuDinhKem())
                .yeuCauBaoQuan(entity.getYeuCauBaoQuan())
                .nhietDoBaoQuanMin(entity.getNhietDoBaoQuanMin())
                .nhietDoBaoQuanMax(entity.getNhietDoBaoQuanMax())
                .doAmBaoQuan(entity.getDoAmBaoQuan())
                .hanSuDungMacDinh(entity.getHanSuDungMacDinh())
                .canhBaoHetHan(entity.getCanhBaoHetHan())
                .coQuanLyLo(entity.getCoQuanLyLo())
                .coHanSuDung(entity.getCoHanSuDung())
                .coKiemSoatChatLuong(entity.getCoKiemSoatChatLuong())
                .laThuocDoc(entity.getLaThuocDoc())
                .laThuocHuongThan(entity.getLaThuocHuongThan())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByName(entity.getCreatedBy() != null ? entity.getCreatedBy().getHoTen() : null)
                .updatedById(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getId() : null)
                .updatedByName(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getHoTen() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .trangThaiTonKho(entity.getTrangThaiTonKho())
                .isTonKhoThap(entity.isTonKhoThap())
                .isDangHetHang(entity.isDangHetHang())
                .build();
    }

    private HangHoa convertToEntity(HangHoaDTO dto) {
        return HangHoa.builder()
                .maHangHoa(dto.getMaHangHoa())
                .tenHangHoa(dto.getTenHangHoa())
                .tenKhoaHoc(dto.getTenKhoaHoc())
                .maBarcode(dto.getMaBarcode())
                .maQrCode(dto.getMaQrCode())
                .moTa(dto.getMoTa())
                .thanhPhan(dto.getThanhPhan())
                .congDung(dto.getCongDung())
                .cachSuDung(dto.getCachSuDung())
                .lieuLuong(dto.getLieuLuong())
                .dongGoi(dto.getDongGoi())
                .xuatXu(dto.getXuatXu())
                .hangSanXuat(dto.getHangSanXuat())
                .soDangKy(dto.getSoDangKy())
                .soLuongToiThieu(dto.getSoLuongToiThieu())
                .soLuongToiDa(dto.getSoLuongToiDa())
                .trongLuong(dto.getTrongLuong())
                .kichThuoc(dto.getKichThuoc())
                .mauSac(dto.getMauSac())
                .hinhAnhUrl(dto.getHinhAnhUrl())
                .taiLieuDinhKem(dto.getTaiLieuDinhKem())
                .yeuCauBaoQuan(dto.getYeuCauBaoQuan())
                .nhietDoBaoQuanMin(dto.getNhietDoBaoQuanMin())
                .nhietDoBaoQuanMax(dto.getNhietDoBaoQuanMax())
                .doAmBaoQuan(dto.getDoAmBaoQuan())
                .hanSuDungMacDinh(dto.getHanSuDungMacDinh())
                .canhBaoHetHan(dto.getCanhBaoHetHan())
                .coQuanLyLo(dto.getCoQuanLyLo())
                .coHanSuDung(dto.getCoHanSuDung())
                .coKiemSoatChatLuong(dto.getCoKiemSoatChatLuong())
                .laThuocDoc(dto.getLaThuocDoc())
                .laThuocHuongThan(dto.getLaThuocHuongThan())
                .ghiChu(dto.getGhiChu())
                .trangThai(dto.getTrangThai())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByTenDangNhap(authentication.getName())
                    .orElse(null);
        }
        return null;
    }
}