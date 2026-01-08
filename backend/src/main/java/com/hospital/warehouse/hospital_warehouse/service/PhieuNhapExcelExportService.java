package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.entity.*;
import com.hospital.warehouse.hospital_warehouse.repository.PhieuNhapKhoRepository;
import com.hospital.warehouse.hospital_warehouse.repository.ChiTietPhieuNhapRepository;
import com.hospital.warehouse.hospital_warehouse.util.NumberToVietnameseWords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhieuNhapExcelExportService {

    private final PhieuNhapKhoRepository phieuNhapKhoRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;

    /**
     * Export Phiếu Nhập theo mẫu C30-HD
     */
    public byte[] exportPhieuNhap(Long phieuNhapId) throws IOException {

        PhieuNhapKho phieuNhap = phieuNhapKhoRepository.findById(phieuNhapId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập ID: " + phieuNhapId));

        List<ChiTietPhieuNhap> chiTietList = chiTietPhieuNhapRepository
                .findByPhieuNhapIdWithDetails(phieuNhapId);

        if (chiTietList.isEmpty()) {
            throw new IllegalStateException("Phiếu nhập không có chi tiết");
        }

        // Nhóm chi tiết theo nhà cung cấp
        Map<String, List<ChiTietPhieuNhap>> chiTietByNCC = groupByNhaCungCap(chiTietList, phieuNhap);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Phiếu Nhập");

        int currentRow = 0;

        // ==================== DÒNG 1: HEADER ====================
        Row row1 = sheet.createRow(currentRow++);
        row1.setHeightInPoints(18.75f);

        // A1:B1 - Tên bệnh viện
        Cell cellA1 = row1.createCell(0);
        cellA1.setCellValue("Bệnh viện Đa khoa Tân Phú");
        cellA1.setCellStyle(createStyleHeader12BoldLeft(workbook));

        Cell cellB1 = row1.createCell(1);
        cellB1.setCellStyle(createStyleHeader12BoldLeft(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // C1:G1 - Tiêu đề chính
        Cell cellC1 = row1.createCell(2);
        cellC1.setCellValue("       PHIẾU  NHẬP  KHO");
        cellC1.setCellStyle(createStyleHeader14BoldCenter(workbook));

        for (int col = 3; col <= 6; col++) {
            row1.createCell(col).setCellStyle(createStyleHeader14BoldCenter(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 6));

        // H1:I1 - Mẫu số
        Cell cellH1 = row1.createCell(7);
        cellH1.setCellValue("Mẫu số : C30 - HD");
        cellH1.setCellStyle(createStyleHeader9BoldRight(workbook));

        Cell cellI1 = row1.createCell(8);
        cellI1.setCellStyle(createStyleHeader9BoldRight(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 8));

        // ==================== DÒNG 2: PHÒNG + NGÀY ====================
        Row row2 = sheet.createRow(currentRow++);
        row2.setHeightInPoints(18f);

        // A2:B2 - Phòng
        Cell cellA2 = row2.createCell(0);
        cellA2.setCellValue("Phòng TC-HCQT");
        cellA2.setCellStyle(createStyleHeader12BoldLeft(workbook));

        Cell cellB2 = row2.createCell(1);
        cellB2.setCellStyle(createStyleHeader12BoldLeft(workbook));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

        // C2:G2 - Ngày tháng năm
        Cell cellC2 = row2.createCell(2);
        cellC2.setCellValue(formatNgayThangNam(phieuNhap.getNgayNhap()));
        cellC2.setCellStyle(createStyleHeader12Center(workbook));

        for (int col = 3; col <= 6; col++) {
            row2.createCell(col).setCellStyle(createStyleHeader12Center(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 6));

        // H2:I4 - Ô bảng tính (merge dọc từ dòng 2-4)
        Cell cellH2 = row2.createCell(7);
        cellH2.setCellValue("");
        cellH2.setCellStyle(createStyleTableBorder(workbook));

        Cell cellI2 = row2.createCell(8);
        cellI2.setCellValue("");
        cellI2.setCellStyle(createStyleTableBorder(workbook));

        sheet.addMergedRegion(new CellRangeAddress(1, 3, 7, 8));

        // ==================== DÒNG 3: SỐ PHIẾU ====================
        Row row3 = sheet.createRow(currentRow++);
        row3.setHeightInPoints(19.5f);

        // A3:B3 - Trống
        Cell cellA3 = row3.createCell(0);
        cellA3.setCellStyle(createStyleNormal(workbook));
        Cell cellB3 = row3.createCell(1);
        cellB3.setCellStyle(createStyleNormal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 1));

        // C3:G3 - Số phiếu
        Cell cellC3 = row3.createCell(2);
        cellC3.setCellValue("   Số phiếu: " + phieuNhap.getMaPhieuNhap());
        cellC3.setCellStyle(createStyleHeader12Center(workbook));

        for (int col = 3; col <= 6; col++) {
            row3.createCell(col).setCellStyle(createStyleHeader12Center(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 6));

        // Cell H3, I3 cho merge H2:I4
        Cell cellH3 = row3.createCell(7);
        cellH3.setCellStyle(createStyleTableBorder(workbook));

        Cell cellI3 = row3.createCell(8);
        cellI3.setCellStyle(createStyleTableBorder(workbook));

        // ==================== DÒNG 4: HEADER BẢNG TÍNH THUẾ ====================
        Row row4 = sheet.createRow(currentRow++);
        row4.setHeightInPoints(12f);

        // Cell H4, I4 cho merge H2:I4
        Cell cellH4 = row4.createCell(7);
        cellH4.setCellStyle(createStyleTableBorder(workbook));

        Cell cellI4 = row4.createCell(8);
        cellI4.setCellStyle(createStyleTableBorder(workbook));

        // M4:R4 - Header bảng tính thuế
        Cell cellM4 = row4.createCell(12);
        cellM4.setCellValue("Tiền có thuế");
        cellM4.setCellStyle(createStyleTableHeaderBold(workbook));

        Cell cellN4 = row4.createCell(13);
        cellN4.setCellValue("Thuế");
        cellN4.setCellStyle(createStyleTableHeaderBold(workbook));

        Cell cellO4 = row4.createCell(14);
        cellO4.setCellValue("Tiền thuế");
        cellO4.setCellStyle(createStyleTableHeaderBold(workbook));

        Cell cellP4 = row4.createCell(15);
        cellP4.setCellValue("Tổng có thuế");
        cellP4.setCellStyle(createStyleTableHeaderBold(workbook));

        Cell cellQ4 = row4.createCell(16);
        cellQ4.setCellValue("Số lượng");
        cellQ4.setCellStyle(createStyleTableHeaderBold(workbook));

        Cell cellR4 = row4.createCell(17);
        cellR4.setCellValue("Thành tiền");
        cellR4.setCellStyle(createStyleTableHeaderBold(workbook));

        // ==================== DÒNG 5-10: THÔNG TIN NCC + BẢNG TÍNH THUẾ ====================
        int nccRowStart = currentRow;
        int totalNCCRows = 0;

        for (Map.Entry<String, List<ChiTietPhieuNhap>> entry : chiTietByNCC.entrySet()) {
            String tenNCC = entry.getKey();
            List<ChiTietPhieuNhap> itemsNCC = entry.getValue();

            // ===== DÒNG NGƯỜI GIAO =====
            Row rowNguoiGiao = sheet.createRow(currentRow++);
            rowNguoiGiao.setHeightInPoints(18.75f);

            // A:B - Label "Họ và tên người giao :"
            Cell cellLabelNguoiGiao = rowNguoiGiao.createCell(0);
            cellLabelNguoiGiao.setCellValue("Họ và tên người  giao :");
            cellLabelNguoiGiao.setCellStyle(createStyleAlignRight(workbook));

            Cell cellB_NG = rowNguoiGiao.createCell(1);
            cellB_NG.setCellStyle(createStyleAlignRight(workbook));
            sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1));

            // C:I - Tên người giao
            Cell cellNguoiGiao = rowNguoiGiao.createCell(2);
            cellNguoiGiao.setCellValue(tenNCC);
            cellNguoiGiao.setCellStyle(createStyleValueNormalBorder(workbook));

            for (int col = 3; col <= 8; col++) {
                Cell cell = rowNguoiGiao.createCell(col);
                cell.setCellStyle(createStyleValueNormalBorder(workbook));
            }
            sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 2, 8));

            // L - Tên sản phẩm đầu tiên
            if (!itemsNCC.isEmpty()) {
                Cell cellTenSP = rowNguoiGiao.createCell(11);
                cellTenSP.setCellValue(itemsNCC.get(0).getHangHoa().getTenHangHoa());
                cellTenSP.setCellStyle(createStyleNormalWrap(workbook));
            }

            // ===== BẢNG TÍNH THUẾ - DÒNG NCC =====
            // ✅ ĐƠN GIÁ TRONG DB ĐÃ BAO GỒM THUẾ
            // ✅ CẦN TÍNH NGƯỢC LẠI ĐƠN GIÁ KHÔNG THUẾ
            BigDecimal donGiaCoThueNCC = !itemsNCC.isEmpty() ?
                    itemsNCC.get(0).getDonGia() : BigDecimal.ZERO;

            // Tính đơn giá không thuế: donGiaCoThue / 1.08
            // ✅ Dùng 2 chữ số thập phân để chính xác hơn
            BigDecimal donGiaKhongThueNCC = donGiaCoThueNCC.divide(
                    new BigDecimal("1.08"), 2, BigDecimal.ROUND_HALF_UP);

            // ✅ SỬA: Chỉ lấy số lượng sản phẩm đầu tiên, không phải tổng
            int soLuongSP1 = !itemsNCC.isEmpty() ? itemsNCC.get(0).getSoLuong() : 0;

            // M - Đơn giá KHÔNG thuế
            Cell cellM = rowNguoiGiao.createCell(12);
            cellM.setCellValue(donGiaKhongThueNCC.doubleValue());
            cellM.setCellStyle(createStyleNumberBorder(workbook));

            // N - Thuế suất 0.08 (8%)
            Cell cellN = rowNguoiGiao.createCell(13);
            cellN.setCellValue(0.08);
            cellN.setCellStyle(createStylePercentBorder(workbook));

            // O - Công thức tiền thuế: =+N*M
            Cell cellO = rowNguoiGiao.createCell(14);
            cellO.setCellFormula(String.format("+N%d*M%d", currentRow, currentRow));
            cellO.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

            // P - Công thức đơn giá có thuế: =+O+M
            Cell cellP = rowNguoiGiao.createCell(15);
            cellP.setCellFormula(String.format("+O%d+M%d", currentRow, currentRow));
            cellP.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

            // Q - Số lượng sản phẩm đầu tiên
            Cell cellQ = rowNguoiGiao.createCell(16);
            cellQ.setCellValue(soLuongSP1);
            cellQ.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

            // R - Thành tiền tổng = Q × P
            Cell cellR = rowNguoiGiao.createCell(17);
            cellR.setCellFormula(String.format("+Q%d*P%d", currentRow, currentRow));
            cellR.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

            totalNCCRows++;

            // ===== DÒNG HÓA ĐƠN (và các dòng tiếp theo nếu có nhiều sản phẩm) =====
            int spIndex = 1;
            for (int i = 1; i < itemsNCC.size() || (i == 1 && spIndex == 1); i++, spIndex++) {
                Row rowHoaDon = sheet.createRow(currentRow++);
                rowHoaDon.setHeightInPoints(18.75f);

                // A:B - Label "Theo hóa đơn số :"
                Cell cellLabelHD = rowHoaDon.createCell(0);
                cellLabelHD.setCellValue("   Theo hóa đơn số :");
                cellLabelHD.setCellStyle(createStyleAlignCenter(workbook));

                Cell cellB_HD = rowHoaDon.createCell(1);
                cellB_HD.setCellStyle(createStyleAlignCenter(workbook));
                sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1));

                // C:I - Số hóa đơn (chỉ dòng đầu tiên)
                Cell cellHoaDon = rowHoaDon.createCell(2);
                if (i == 1) {
                    String hoaDonInfo = formatHoaDonInfo(phieuNhap);
                    cellHoaDon.setCellValue(hoaDonInfo);
                }
                cellHoaDon.setCellStyle(createStyleValueNormalBorder(workbook));

                for (int col = 3; col <= 8; col++) {
                    Cell cell = rowHoaDon.createCell(col);
                    cell.setCellStyle(createStyleValueNormalBorder(workbook));
                }
                sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 2, 8));

                // L - Tên sản phẩm tiếp theo
                if (spIndex < itemsNCC.size()) {
                    Cell cellTenSP = rowHoaDon.createCell(11);
                    cellTenSP.setCellValue(itemsNCC.get(spIndex).getHangHoa().getTenHangHoa());
                    cellTenSP.setCellStyle(createStyleNormalWrap(workbook));
                }

                // M:R - Bảng tính thuế cho sản phẩm này
                if (spIndex < itemsNCC.size()) {
                    ChiTietPhieuNhap item = itemsNCC.get(spIndex);
                    BigDecimal donGiaCoThueSP = item.getDonGia();

                    // ✅ Tính đơn giá không thuế: donGiaCoThue / 1.08
                    // ✅ Dùng 2 chữ số thập phân để chính xác hơn
                    BigDecimal donGiaKhongThueSP = donGiaCoThueSP.divide(
                            new BigDecimal("1.08"), 2, BigDecimal.ROUND_HALF_UP);

                    int soLuong = item.getSoLuong();

                    // M - Đơn giá không thuế
                    Cell cellM_SP = rowHoaDon.createCell(12);
                    cellM_SP.setCellValue(donGiaKhongThueSP.doubleValue());
                    cellM_SP.setCellStyle(createStyleNumberBorder(workbook));

                    // N - Thuế suất
                    Cell cellN_SP = rowHoaDon.createCell(13);
                    cellN_SP.setCellValue(0.08);
                    cellN_SP.setCellStyle(createStylePercentBorder(workbook));

                    // O - Tiền thuế
                    Cell cellO_SP = rowHoaDon.createCell(14);
                    cellO_SP.setCellFormula(String.format("+N%d*M%d", currentRow, currentRow));
                    cellO_SP.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

                    // P - Đơn giá có thuế
                    Cell cellP_SP = rowHoaDon.createCell(15);
                    cellP_SP.setCellFormula(String.format("+O%d+M%d", currentRow, currentRow));
                    cellP_SP.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

                    // Q - Số lượng
                    Cell cellQ_SP = rowHoaDon.createCell(16);
                    cellQ_SP.setCellValue(soLuong);
                    cellQ_SP.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

                    // R - Thành tiền
                    Cell cellR_SP = rowHoaDon.createCell(17);
                    cellR_SP.setCellFormula(String.format("+Q%d*P%d", currentRow, currentRow));
                    cellR_SP.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên
                }

                totalNCCRows++;
            }
        }

        // ===== DÒNG 10: NHẬP TẠI KHO =====
        Row row10 = sheet.createRow(currentRow++);
        row10.setHeightInPoints(18.75f);

        // A:B - Label
        Cell cellLabelKho = row10.createCell(0);
        cellLabelKho.setCellValue("           Nhập tại kho :");
        cellLabelKho.setCellStyle(createStyleAlignRight(workbook));

        Cell cellB_Kho = row10.createCell(1);
        cellB_Kho.setCellStyle(createStyleAlignRight(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1));

        // C - Tên kho
        Cell cellTenKho = row10.createCell(2);
        String tenKho = phieuNhap.getKho() != null ? phieuNhap.getKho().getTenKho() : "Hành chính";
        cellTenKho.setCellValue(tenKho);
        cellTenKho.setCellStyle(createStyleNormal(workbook));

        // D:E - Label "Địa Điểm:"
        Cell cellLabelDiaDiem = row10.createCell(3);
        cellLabelDiaDiem.setCellValue("Địa Điểm:");
        cellLabelDiaDiem.setCellStyle(createStyleNormal(workbook));

        Cell cellE_DD = row10.createCell(4);
        cellE_DD.setCellStyle(createStyleNormal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 3, 4));

        // F:K - Địa chỉ
        Cell cellDiaDiem = row10.createCell(5);
        cellDiaDiem.setCellValue("Bệnh viện Đa khoa Tân Phú");
        cellDiaDiem.setCellStyle(createStyleNormal(workbook));

        for (int col = 6; col <= 10; col++) {
            Cell cell = row10.createCell(col);
            cell.setCellStyle(createStyleNormal(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 5, 10));

        // R10 - Công thức tổng tiền
        Cell cellR10 = row10.createCell(17);
        String sumFormula = String.format("SUM(R%d:R%d)", nccRowStart + 1, currentRow - 1);
        cellR10.setCellFormula(sumFormula);
        cellR10.setCellStyle(createStyleNumberBorderInteger(workbook));  // ✅ Số nguyên

        // ==================== DÒNG 11: EMPTY ====================
        sheet.createRow(currentRow++).setHeightInPoints(16.5f);

        // ==================== DÒNG 12-14: HEADER BẢNG CHI TIẾT ====================
        int headerRow = currentRow;

        // DÒNG 12 - Header chính
        Row row12 = sheet.createRow(currentRow++);
        row12.setHeightInPoints(41.25f);

        // A12 - Số TT
        Cell cellA12 = row12.createCell(0);
        cellA12.setCellValue("Số \nTT");
        cellA12.setCellStyle(createStyleTableHeader(workbook));

        // B12 - Tên hàng hóa
        Cell cellB12 = row12.createCell(1);
        cellB12.setCellValue("Tên nhãn hiệu,qui cách,\nphẩm chất vật tư, dụng cụ\n (sản phẩm,hàng hoá)");
        cellB12.setCellStyle(createStyleTableHeader(workbook));

        Cell cellC12 = row12.createCell(2);
        cellC12.setCellStyle(createStyleTableHeader(workbook));

        // D12 - Mã số
        Cell cellD12 = row12.createCell(3);
        cellD12.setCellValue("Mã số");
        cellD12.setCellStyle(createStyleTableHeader(workbook));

        // E12 - ĐVT
        Cell cellE12 = row12.createCell(4);
        cellE12.setCellValue("Đơn vị  \ntính");
        cellE12.setCellStyle(createStyleTableHeader(workbook));

        // F12 - Số lượng
        Cell cellF12 = row12.createCell(5);
        cellF12.setCellValue("Số lượng  ");
        cellF12.setCellStyle(createStyleTableHeader(workbook));

        Cell cellG12 = row12.createCell(6);
        cellG12.setCellStyle(createStyleTableHeader(workbook));

        // H12 - Đơn giá
        Cell cellH12 = row12.createCell(7);
        cellH12.setCellValue("Đơn giá");
        cellH12.setCellStyle(createStyleTableHeader(workbook));

        // I12 - Thành tiền
        Cell cellI12 = row12.createCell(8);
        cellI12.setCellValue("Thành tiền");
        cellI12.setCellStyle(createStyleTableHeader(workbook));

        // Merge các ô header
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 0, 0)); // A12:A13
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 1, 2)); // B12:C13
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 3, 3)); // D12:D13
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 4, 4)); // E12:E13
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow, 5, 6));     // F12:G12
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 7, 7)); // H12:H13
        sheet.addMergedRegion(new CellRangeAddress(headerRow, headerRow + 1, 8, 8)); // I12:I13

        // DÒNG 13 - Sub header "Số lượng"
        Row row13 = sheet.createRow(currentRow++);
        row13.setHeightInPoints(42.75f);

        for (int col = 0; col <= 4; col++) {
            if (col != 5 && col != 6) {
                Cell cell = row13.createCell(col);
                cell.setCellStyle(createStyleTableHeader(workbook));
            }
        }

        Cell cellF13 = row13.createCell(5);
        cellF13.setCellValue("Chứng từ");
        cellF13.setCellStyle(createStyleTableHeader(workbook));

        Cell cellG13 = row13.createCell(6);
        cellG13.setCellValue("Thực nhập");
        cellG13.setCellStyle(createStyleTableHeader(workbook));

        Cell cellH13 = row13.createCell(7);
        cellH13.setCellStyle(createStyleTableHeader(workbook));

        Cell cellI13 = row13.createCell(8);
        cellI13.setCellStyle(createStyleTableHeader(workbook));

        // DÒNG 14 - Labels (A, B, C, D, 1, 2, 3, 4)
        Row row14 = sheet.createRow(currentRow++);

        String[] labels = {"A", "B", "", "C", "D", "1", "2", "3", "4"};
        for (int i = 0; i < labels.length; i++) {
            Cell cell = row14.createCell(i);
            cell.setCellValue(labels[i]);
            cell.setCellStyle(createStyleTableHeaderLabel(workbook));
        }

        // Merge B14:C14
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, 2));

        // ==================== CHI TIẾT SẢN PHẨM ====================
        int dataStartRow = currentRow;
        int stt = 1;

        for (ChiTietPhieuNhap chiTiet : chiTietList) {
            Row dataRow = sheet.createRow(currentRow);

            String tenHangHoa = chiTiet.getHangHoa().getTenHangHoa();
            float rowHeight = tenHangHoa.length() > 50 ? 27.75f : (tenHangHoa.length() > 30 ? 26.25f : 22.5f);
            dataRow.setHeightInPoints(rowHeight);

            // A - STT
            Cell cellA = dataRow.createCell(0);
            cellA.setCellValue(stt++);
            cellA.setCellStyle(createStyleDataCenter(workbook));

            // B:C - Tên hàng hóa (merge)
            Cell cellB = dataRow.createCell(1);
            cellB.setCellValue(tenHangHoa);
            cellB.setCellStyle(createStyleDataLeft(workbook));

            Cell cellC = dataRow.createCell(2);
            cellC.setCellStyle(createStyleDataLeft(workbook));
            sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 1, 2));

            // D - Mã số (trống)
            Cell cellD = dataRow.createCell(3);
            cellD.setCellValue("");
            cellD.setCellStyle(createStyleDataCenter(workbook));

            // E - ĐVT
            Cell cellE = dataRow.createCell(4);
            String dvt = chiTiet.getHangHoa().getDonViTinh() != null ?
                    chiTiet.getHangHoa().getDonViTinh().getTenDvt() : "Cái";
            cellE.setCellValue(dvt);
            cellE.setCellStyle(createStyleDataCenter(workbook));

            // F - SL Chứng từ
            Cell cellF = dataRow.createCell(5);
            cellF.setCellValue(chiTiet.getSoLuong());
            cellF.setCellStyle(createStyleDataCenter(workbook));

            // G - SL Thực nhập
            Cell cellG = dataRow.createCell(6);
            cellG.setCellValue(chiTiet.getSoLuong());
            cellG.setCellStyle(createStyleDataCenter(workbook));

            // H - Đơn giá (công thức liên kết từ bảng thuế)
            Cell cellH = dataRow.createCell(7);
            int nccRowNumber = findNCCRowForChiTiet(chiTiet, chiTietByNCC, nccRowStart);
            if (nccRowNumber > 0) {
                cellH.setCellFormula(String.format("+P%d", nccRowNumber));
            } else {
                cellH.setCellValue(chiTiet.getDonGia().doubleValue());
            }
            cellH.setCellStyle(createStyleDataNumber(workbook));

            // I - Thành tiền (công thức =H*G)
            Cell cellI = dataRow.createCell(8);
            cellI.setCellFormula(String.format("H%d*G%d", currentRow + 1, currentRow + 1));
            cellI.setCellStyle(createStyleDataNumber(workbook));

            // J - Trạng thái
            Cell cellJ = dataRow.createCell(9);
            String trangThai = "Chưa xuất";
            if (chiTiet.getTrangThai() == ChiTietPhieuNhap.TrangThaiChiTiet.DA_NHAP) {
                trangThai = "Đã nhập";
            }
            cellJ.setCellValue(trangThai);
            cellJ.setCellStyle(createStyleDataCenter(workbook));

            // K - Ghi chú
            Cell cellK = dataRow.createCell(10);
            String ghiChu = buildGhiChu(chiTiet);
            cellK.setCellValue(ghiChu);
            cellK.setCellStyle(createStyleDataLeft(workbook));

            currentRow++;
        }

        int dataEndRow = currentRow - 1;

        // ==================== DÒNG 20: TỔNG CỘNG ====================
        Row row20 = sheet.createRow(currentRow++);
        row20.setHeightInPoints(23.25f);

        // A - Trống
        Cell cellA20 = row20.createCell(0);
        cellA20.setCellValue("");
        cellA20.setCellStyle(createStyleTongCong(workbook));

        // B:C - "Cộng: khoản"
        Cell cellB20 = row20.createCell(1);
        cellB20.setCellValue(String.format("Cộng:     %d khoản", chiTietList.size()));
        cellB20.setCellStyle(createStyleTongCong(workbook));

        Cell cellC20 = row20.createCell(2);
        cellC20.setCellStyle(createStyleTongCong(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, 2));

        // D - x
        Cell cellD20 = row20.createCell(3);
        cellD20.setCellValue("x");
        cellD20.setCellStyle(createStyleTongCongX(workbook));

        // E - x
        Cell cellE20 = row20.createCell(4);
        cellE20.setCellValue("x");
        cellE20.setCellStyle(createStyleTongCongX(workbook));

        // F - Số khoản
        Cell cellF20 = row20.createCell(5);
        cellF20.setCellValue(chiTietList.size());
        cellF20.setCellStyle(createStyleTongCongBold(workbook));

        // G - Tổng số lượng (công thức)
        Cell cellG20 = row20.createCell(6);
        cellG20.setCellFormula(String.format("SUM(G%d:G%d)", dataStartRow + 1, dataEndRow + 1));
        cellG20.setCellStyle(createStyleTongCongBold(workbook));

        // H - x
        Cell cellH20 = row20.createCell(7);
        cellH20.setCellValue("x");
        cellH20.setCellStyle(createStyleTongCongX(workbook));

        // I - Tổng thành tiền (công thức)
        Cell cellI20 = row20.createCell(8);
        cellI20.setCellFormula(String.format("SUM(I%d:I%d)", dataStartRow + 1, dataEndRow + 1));
        cellI20.setCellStyle(createStyleTongCongNumber(workbook));

        // J - Chênh lệch (công thức =+I20-R10)
        Cell cellJ20 = row20.createCell(9);
        int r10Row = nccRowStart + totalNCCRows + 1;
        cellJ20.setCellFormula(String.format("+I%d-R%d", currentRow, r10Row));
        cellJ20.setCellStyle(createStyleDataNumber(workbook));

        // ==================== DÒNG 21: TỔNG SỐ TIỀN BẰNG CHỮ ====================
        Row row21 = sheet.createRow(currentRow++);
        row21.setHeightInPoints(23.25f);

        // B:C - Label
        Cell cellB21 = row21.createCell(1);
        cellB21.setCellValue("Tổng số tiền bằng chữ:");
        cellB21.setCellStyle(createStyleNormal(workbook));

        Cell cellC21 = row21.createCell(2);
        cellC21.setCellStyle(createStyleNormal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, 2));

        // D:I - Tiền bằng chữ
        Cell cellD21 = row21.createCell(3);
        BigDecimal tongTien = phieuNhap.getTongThanhToan();
        String tienBangChu = NumberToVietnameseWords.convert(tongTien);
        cellD21.setCellValue(tienBangChu);
        cellD21.setCellStyle(createStyleValueDotted(workbook));

        for (int col = 4; col <= 8; col++) {
            Cell cell = row21.createCell(col);
            cell.setCellStyle(createStyleValueDotted(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 3, 8));

        // ==================== DÒNG 22: SỐ CHỨNG TỪ KÈM THEO ====================
        Row row22 = sheet.createRow(currentRow++);
        row22.setHeightInPoints(21f);

        // B:C - Label
        Cell cellB22 = row22.createCell(1);
        cellB22.setCellValue("Số chứng từ kèm theo:");
        cellB22.setCellStyle(createStyleNormal(workbook));

        Cell cellC22 = row22.createCell(2);
        cellC22.setCellStyle(createStyleNormal(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, 2));

        // E:I - Số hóa đơn
        Cell cellE22 = row22.createCell(4);
        int soHoaDon = (phieuNhap.getSoHoaDon() != null && !phieuNhap.getSoHoaDon().isEmpty()) ? 1 : 0;
        String chungTuText = soHoaDon > 0 ? String.format("%02d Hóa Đơn GTGT", soHoaDon) : "";
        cellE22.setCellValue(chungTuText);
        cellE22.setCellStyle(createStyleNormal(workbook));

        for (int col = 5; col <= 8; col++) {
            Cell cell = row22.createCell(col);
            cell.setCellStyle(createStyleNormal(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 4, 8));

        // ==================== DÒNG 23: EMPTY ====================
        sheet.createRow(currentRow++).setHeightInPoints(15f);

        // ==================== DÒNG 24-29: CHỮ KÝ ====================
        Row row24 = sheet.createRow(currentRow++);
        row24.setHeightInPoints(22f);

        // A24:B24 - NGƯỜI LẬP PHIẾU
        Cell cellA24 = row24.createCell(0);
        cellA24.setCellValue("NGƯỜI LẬP PHIẾU");
        cellA24.setCellStyle(createStyleChuKyBold(workbook));

        Cell cellB24 = row24.createCell(1);
        cellB24.setCellStyle(createStyleChuKyBold(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1));

        // C24:D24 - TRƯỞNG PHÒNG
        Cell cellC24 = row24.createCell(2);
        cellC24.setCellValue("TRƯỞNG PHÒNG");
        cellC24.setCellStyle(createStyleChuKyBold(workbook));

        Cell cellD24 = row24.createCell(3);
        cellD24.setCellStyle(createStyleChuKyBold(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 2, 3));

        // E24:G24 - PHÂN KẾ TOÁN
        Cell cellE24 = row24.createCell(4);
        cellE24.setCellValue("BỘ PHẬN KẾ TOÁN");
        cellE24.setCellStyle(createStyleChuKyBold(workbook));

        for (int col = 5; col <= 6; col++) {
            Cell cell = row24.createCell(col);
            cell.setCellStyle(createStyleChuKyBold(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 4, 6));

        // H24:I24 - TOÁN TRƯỞNG
        Cell cellH24 = row24.createCell(7);
        cellH24.setCellValue("TOÁN TRƯỞNG");
        cellH24.setCellStyle(createStyleChuKyBold(workbook));

        Cell cellI24 = row24.createCell(8);
        cellI24.setCellStyle(createStyleChuKyBold(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 7, 8));

        // Khoảng trống cho chữ ký
        for (int i = 0; i < 4; i++) {
            sheet.createRow(currentRow++).setHeightInPoints(18f);
        }

        // Dòng tên người ký
        Row row29 = sheet.createRow(currentRow++);
        row29.setHeightInPoints(20f);

        // A29:B29 - Tên người lập
        Cell cellA29 = row29.createCell(0);
        String tenNguoiLap = phieuNhap.getNguoiNhan() != null ?
                phieuNhap.getNguoiNhan().getHoTen() : "";
        cellA29.setCellValue(tenNguoiLap);
        cellA29.setCellStyle(createStyleChuKyName(workbook));

        Cell cellB29 = row29.createCell(1);
        cellB29.setCellStyle(createStyleChuKyName(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1));

        // C29:D29 - Trưởng phòng
        Cell cellC29 = row29.createCell(2);
        cellC29.setCellValue("Phạm Xuân Ánh Đào");
        cellC29.setCellStyle(createStyleChuKyName(workbook));

        Cell cellD29 = row29.createCell(3);
        cellD29.setCellStyle(createStyleChuKyName(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 2, 3));

        // E29:G29 - Kế toán
        Cell cellE29 = row29.createCell(4);
        cellE29.setCellValue("Đặng Thị Kim Chi");
        cellE29.setCellStyle(createStyleChuKyName(workbook));

        for (int col = 5; col <= 6; col++) {
            Cell cell = row29.createCell(col);
            cell.setCellStyle(createStyleChuKyName(workbook));
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 4, 6));

        // H29:I29 - Kế toán trưởng
        Cell cellH29 = row29.createCell(7);
        cellH29.setCellValue("Nguyễn Thị Mỹ Lệ");
        cellH29.setCellStyle(createStyleChuKyName(workbook));

        Cell cellI29 = row29.createCell(8);
        cellI29.setCellStyle(createStyleChuKyName(workbook));
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 7, 8));

        // ==================== ĐỘ RỘNG CỘT ====================
        sheet.setColumnWidth(0, (int)(5.21875 * 256));    // A
        sheet.setColumnWidth(1, (int)(17.88671875 * 256)); // B
        sheet.setColumnWidth(2, (int)(9.6640625 * 256));   // C
        sheet.setColumnWidth(3, (int)(7.109375 * 256));    // D
        sheet.setColumnWidth(4, (int)(8 * 256));           // E - ĐVT ✅ TĂNG từ 4.88 → 8
        sheet.setColumnWidth(5, (int)(9 * 256));           // F - Chứng từ ✅ TĂNG từ 6.44 → 9
        sheet.setColumnWidth(6, (int)(9 * 256));           // G - Thực nhập ✅ TĂNG từ 7.1 → 9
        sheet.setColumnWidth(7, (int)(12 * 256));          // H - Đơn giá ✅ TĂNG
        sheet.setColumnWidth(8, (int)(15 * 256));          // I - Thành tiền ✅ TĂNG
        sheet.setColumnWidth(9, (int)(11.77734375 * 256)); // J
        sheet.setColumnWidth(10, (int)(11.0 * 256));       // K
        sheet.setColumnWidth(11, (int)(29.33203125 * 256)); // L
        sheet.setColumnWidth(12, (int)(12 * 256));         // M - Tiền có thuế ✅ TĂNG
        sheet.setColumnWidth(13, (int)(8 * 256));          // N - Thuế suất
        sheet.setColumnWidth(14, (int)(12 * 256));         // O - Tiền thuế
        sheet.setColumnWidth(15, (int)(13 * 256));         // P
        sheet.setColumnWidth(16, (int)(9 * 256));          // Q
        sheet.setColumnWidth(17, (int)(15 * 256));         // R - Thành tiền ✅ TĂNG

        // ==================== PRINT SETUP ====================
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.setAutobreaks(true);

        sheet.setMargin(Sheet.TopMargin, 0.5);
        sheet.setMargin(Sheet.BottomMargin, 0.5);
        sheet.setMargin(Sheet.LeftMargin, 0.5);
        sheet.setMargin(Sheet.RightMargin, 0.5);

        // ==================== FORCE FORMULA CALCULATION ====================
        workbook.setForceFormulaRecalculation(true);

        // ==================== WRITE TO BYTE ARRAY ====================
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        log.info("✅ Successfully exported phieu nhap ID: {} to Excel", phieuNhapId);
        return outputStream.toByteArray();
    }

    // ==================== HELPER METHODS ====================

    private Map<String, List<ChiTietPhieuNhap>> groupByNhaCungCap(
            List<ChiTietPhieuNhap> chiTietList, PhieuNhapKho phieuNhap) {

        Map<String, List<ChiTietPhieuNhap>> grouped = new LinkedHashMap<>();

        if (phieuNhap.getNhaCungCap() != null) {
            String tenNCC = phieuNhap.getNhaCungCap().getTenNcc();
            grouped.put(tenNCC, new ArrayList<>(chiTietList));
        } else if (phieuNhap.getNguoiGiao() != null && !phieuNhap.getNguoiGiao().isEmpty()) {
            grouped.put(phieuNhap.getNguoiGiao(), new ArrayList<>(chiTietList));
        } else {
            grouped.put("Nhà cung cấp", new ArrayList<>(chiTietList));
        }

        return grouped;
    }

    private int findNCCRowForChiTiet(ChiTietPhieuNhap chiTiet,
                                     Map<String, List<ChiTietPhieuNhap>> chiTietByNCC,
                                     int nccRowStart) {
        int rowNumber = nccRowStart + 1;
        int chiTietIndex = 0;

        for (Map.Entry<String, List<ChiTietPhieuNhap>> entry : chiTietByNCC.entrySet()) {
            List<ChiTietPhieuNhap> items = entry.getValue();

            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(chiTiet)) {
                    if (i == 0) {
                        return rowNumber;
                    } else {
                        return rowNumber + i;
                    }
                }
            }

            rowNumber += Math.max(2, items.size() + 1);
        }

        return 0;
    }

    private String buildGhiChu(ChiTietPhieuNhap chiTiet) {
        StringBuilder sb = new StringBuilder();

        if (chiTiet.getViTriKho() != null) {
            sb.append("01 ").append(chiTiet.getViTriKho().getTenViTri());
        }

        if (chiTiet.getSoLo() != null && !chiTiet.getSoLo().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Lô: ").append(chiTiet.getSoLo());
        }

        if (chiTiet.getHanSuDung() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("HSD: ").append(formatDateShort(chiTiet.getHanSuDung()));
        }

        return sb.toString();
    }

    private String formatHoaDonInfo(PhieuNhapKho phieuNhap) {
        StringBuilder sb = new StringBuilder();

        if (phieuNhap.getSoHoaDon() != null && !phieuNhap.getSoHoaDon().isEmpty()) {
            sb.append(phieuNhap.getSoHoaDon());

            if (phieuNhap.getNgayHoaDon() != null) {
                sb.append(", ngày ").append(formatDateLong(phieuNhap.getNgayHoaDon()));
            }
        }

        return sb.toString();
    }

    private String formatNgayThangNam(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return String.format("Ngày %02d tháng %02d năm %d",
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue(),
                dateTime.getYear());
    }

    private String formatDateLong(LocalDate date) {
        if (date == null) return "";
        return String.format("%02d tháng %02d năm %d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear());
    }

    private String formatDateShort(LocalDate date) {
        if (date == null) return "";
        return String.format("%02d/%02d/%d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear());
    }

    // ==================== CELL STYLE CREATORS ====================

    private CellStyle createStyleHeader12BoldLeft(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleHeader14BoldCenter(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleHeader9BoldRight(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleHeader12Center(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleTableBorder(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleTableHeaderBold(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleAlignRight(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleAlignCenter(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleValueNormalBorder(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(false);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleNormal(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleNormalWrap(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createStyleNumberBorder(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        // ✅ Hiển thị 2 chữ số thập phân: 9,259,259.26
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createStyleNumberBorderInteger(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        // ✅ Chỉ hiển thị số nguyên: 740,741
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle createStylePercentBorder(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        return style;
    }

    private CellStyle createStyleTableHeader(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.DOUBLE);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleTableHeaderLabel(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleDataCenter(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleDataLeft(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleDataNumber(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle createStyleTongCong(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleTongCongX(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleTongCongBold(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStyleTongCongNumber(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private CellStyle createStyleValueDotted(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.DOTTED);
        return style;
    }

    private CellStyle createStyleChuKyBold(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createStyleChuKyName(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}