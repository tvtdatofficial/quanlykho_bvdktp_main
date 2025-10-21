package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.dto.CanhBaoDTO;
import com.hospital.warehouse.hospital_warehouse.dto.ThongKeCanhBaoDTO;
import com.hospital.warehouse.hospital_warehouse.entity.CanhBaoHeThong;
import com.hospital.warehouse.hospital_warehouse.entity.User;
import com.hospital.warehouse.hospital_warehouse.repository.CanhBaoRepository;
import com.hospital.warehouse.hospital_warehouse.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CanhBaoService {

    @Autowired
    private CanhBaoRepository canhBaoRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy tất cả cảnh báo có phân trang
     */
    public Page<CanhBaoDTO> getAllCanhBao(Pageable pageable) {
        Page<CanhBaoHeThong> canhBaoPage = canhBaoRepository.findAll(pageable);
        return canhBaoPage.map(this::convertToDTO);
    }

    /**
     * Lấy cảnh báo chưa xử lý
     */
    public Page<CanhBaoDTO> getCanhBaoChuaXuLy(Pageable pageable) {
        Page<CanhBaoHeThong> canhBaoPage = canhBaoRepository.findByDaXuLyFalseOrderByCreatedAtDesc(pageable);
        return canhBaoPage.map(this::convertToDTO);
    }

    /**
     * Lấy cảnh báo chưa đọc
     */
    public Page<CanhBaoDTO> getCanhBaoChuaDoc(Pageable pageable) {
        Page<CanhBaoHeThong> canhBaoPage = canhBaoRepository.findByDaDocFalseOrderByCreatedAtDesc(pageable);
        return canhBaoPage.map(this::convertToDTO);
    }

    /**
     * Lấy cảnh báo theo loại
     */
    public Page<CanhBaoDTO> getCanhBaoTheoLoai(
            CanhBaoHeThong.LoaiCanhBao loaiCanhBao,
            Pageable pageable
    ) {
        Page<CanhBaoHeThong> canhBaoPage = canhBaoRepository.findByLoaiCanhBaoAndDaXuLyFalse(
                loaiCanhBao,
                pageable
        );
        return canhBaoPage.map(this::convertToDTO);
    }

    /**
     * Lấy cảnh báo theo mức độ
     */
    public Page<CanhBaoDTO> getCanhBaoTheoMucDo(
            CanhBaoHeThong.MucDo mucDo,
            Pageable pageable
    ) {
        Page<CanhBaoHeThong> canhBaoPage = canhBaoRepository.findByMucDoAndDaXuLyFalse(
                mucDo,
                pageable
        );
        return canhBaoPage.map(this::convertToDTO);
    }

    /**
     * Lấy chi tiết cảnh báo
     */
    public CanhBaoDTO getCanhBaoById(Long id) {
        CanhBaoHeThong canhBao = canhBaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh báo với ID: " + id));
        return convertToDTO(canhBao);
    }

    /**
     * Đánh dấu đã đọc
     */
    @Transactional
    public CanhBaoDTO danhDauDaDoc(Long id) {
        CanhBaoHeThong canhBao = canhBaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh báo với ID: " + id));

        canhBao.setDaDoc(true);
        CanhBaoHeThong saved = canhBaoRepository.save(canhBao);

        log.info("✅ Đã đánh dấu đọc cảnh báo ID: {}", id);
        return convertToDTO(saved);
    }

    /**
     * Đánh dấu tất cả là đã đọc
     */
    @Transactional
    public void danhDauTatCaDaDoc() {
        List<CanhBaoHeThong> canhBaoChuaDoc = canhBaoRepository.findByDaDocFalseOrderByCreatedAtDesc(
                Pageable.unpaged()
        ).getContent();

        canhBaoChuaDoc.forEach(cb -> cb.setDaDoc(true));
        canhBaoRepository.saveAll(canhBaoChuaDoc);

        log.info("✅ Đã đánh dấu đọc {} cảnh báo", canhBaoChuaDoc.size());
    }

    /**
     * Xử lý cảnh báo
     */
    @Transactional
    public CanhBaoDTO xuLyCanhBao(Long id, Long nguoiXuLyId, String ghiChu) {
        CanhBaoHeThong canhBao = canhBaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cảnh báo với ID: " + id));

        canhBao.setDaXuLy(true);
        canhBao.setDaDoc(true);
        canhBao.setNguoiXuLyId(nguoiXuLyId);
        canhBao.setThoiGianXuLy(LocalDateTime.now());
        canhBao.setGhiChuXuLy(ghiChu);

        CanhBaoHeThong saved = canhBaoRepository.save(canhBao);

        log.info("✅ Đã xử lý cảnh báo ID: {} bởi user ID: {}", id, nguoiXuLyId);
        return convertToDTO(saved);
    }

    /**
     * Xóa cảnh báo
     */
    @Transactional
    public void xoaCanhBao(Long id) {
        if (!canhBaoRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy cảnh báo với ID: " + id);
        }
        canhBaoRepository.deleteById(id);
        log.info("🗑️ Đã xóa cảnh báo ID: {}", id);
    }

    /**
     * Thống kê cảnh báo
     */
    public ThongKeCanhBaoDTO thongKeCanhBao() {
        long tongCanhBao = canhBaoRepository.count();
        long chuaXuLy = canhBaoRepository.countByDaXuLyFalse();
        long chuaDoc = canhBaoRepository.countByDaDocFalse();
        long khanCap = canhBaoRepository.countByMucDoAndDaXuLyFalse(CanhBaoHeThong.MucDo.KHAN_CAP);
        long nghiemTrong = canhBaoRepository.countByMucDoAndDaXuLyFalse(CanhBaoHeThong.MucDo.NGHIEM_TRONG);
        long canhBao = canhBaoRepository.countByMucDoAndDaXuLyFalse(CanhBaoHeThong.MucDo.CANH_BAO);
        long thongTin = canhBaoRepository.countByMucDoAndDaXuLyFalse(CanhBaoHeThong.MucDo.THONG_TIN);

        return new ThongKeCanhBaoDTO(
                tongCanhBao, chuaXuLy, chuaDoc,
                khanCap, nghiemTrong, canhBao, thongTin
        );
    }

    /**
     * Tạo cảnh báo mới (dùng cho trigger/scheduler)
     */
    @Transactional
    public CanhBaoHeThong taoCanhBao(
            CanhBaoHeThong.LoaiCanhBao loaiCanhBao,
            CanhBaoHeThong.MucDo mucDo,
            String tieuDe,
            String noiDung,
            Long doiTuongId,
            CanhBaoHeThong.LoaiDoiTuong loaiDoiTuong
    ) {
        CanhBaoHeThong canhBao = new CanhBaoHeThong();
        canhBao.setLoaiCanhBao(loaiCanhBao);
        canhBao.setMucDo(mucDo);
        canhBao.setTieuDe(tieuDe);
        canhBao.setNoiDung(noiDung);
        canhBao.setDoiTuongLienQuanId(doiTuongId);
        canhBao.setLoaiDoiTuong(loaiDoiTuong);
        canhBao.setDaDoc(false);
        canhBao.setDaXuLy(false);

        CanhBaoHeThong saved = canhBaoRepository.save(canhBao);
        log.info("🚨 Đã tạo cảnh báo mới: {} - {}", loaiCanhBao, tieuDe);

        return saved;
    }

    /**
     * Convert Entity to DTO
     */
    private CanhBaoDTO convertToDTO(CanhBaoHeThong entity) {
        CanhBaoDTO dto = CanhBaoDTO.fromEntity(entity);

        // Lấy tên người xử lý nếu có
        if (entity.getNguoiXuLyId() != null) {
            userRepository.findById(entity.getNguoiXuLyId())
                    .ifPresent(user -> dto.setTenNguoiXuLy(user.getHoTen()));
        }

        return dto;
    }
}