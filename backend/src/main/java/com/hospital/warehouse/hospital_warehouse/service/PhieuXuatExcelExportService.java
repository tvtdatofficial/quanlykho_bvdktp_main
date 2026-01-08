package com.hospital.warehouse.hospital_warehouse.service;

import com.hospital.warehouse.hospital_warehouse.entity.*;
import com.hospital.warehouse.hospital_warehouse.repository.PhieuXuatKhoRepository;
import com.hospital.warehouse.hospital_warehouse.repository.ChiTietPhieuXuatRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service xuất phiếu xuất kho theo mẫu C31-HD
 * CẤU TRÚC ĐƠN GIẢN HƠN PHIẾU NHẬP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhieuXuatExcelExportService {

    private final PhieuXuatKhoRepository phieuXuatKhoRepository;
    private final ChiTietPhieuXuatRepository chiTietPhieuXuatRepository;

    /**
     * Xuất phiếu xuất kho ra file Excel theo mẫu C31-HD
     */
    public byte[] exportPhieuXuat(Long phieuXuatId) throws IOException {
        // Lấy thông tin phiếu xuất
        PhieuXuatKho phieuXuat = phieuXuatKhoRepository.findById(phieuXuatId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất ID: " + phieuXuatId));

        // Lấy chi tiết
        List<ChiTietPhieuXuat> chiTietList = chiTietPhieuXuatRepository.findByPhieuXuatId(phieuXuatId);

        // Tạo workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Phiếu Xuất");

        // Tạo các cell style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = createBoldStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle borderStyle = createBorderStyle(workbook);
        CellStyle moneyStyle = createMoneyStyle(workbook);

        int rowNum = 0;

        // ROW 0: Header
        Row row0 = sheet.createRow(rowNum++);
        row0.createCell(0).setCellValue("Bệnh viện quận Tân Phú");
        row0.getCell(0).setCellStyle(boldStyle);

        Cell cell0_2 = row0.createCell(2);
        cell0_2.setCellValue("PHIẾU XUẤT KHO");
        cell0_2.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 6));

        row0.createCell(7).setCellValue("Mẫu số : C31 - HD");
        row0.getCell(7).setCellStyle(centerStyle);

        // ROW 1: Phòng ban + Ngày
        Row row1 = sheet.createRow(rowNum++);
        row1.createCell(0).setCellValue("Phòng TC-HCQT");
        row1.getCell(0).setCellStyle(boldStyle);

        Cell cell1_2 = row1.createCell(2);
        String ngayThangNam = formatNgayThangNam(phieuXuat.getNgayXuat());
        cell1_2.setCellValue(ngayThangNam);
        cell1_2.setCellStyle(centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 6));

        // ROW 2: Số phiếu
        Row row2 = sheet.createRow(rowNum++);
        Cell cell2_2 = row2.createCell(2);
        cell2_2.setCellValue("Số phiếu: " + phieuXuat.getMaPhieuXuat());
        cell2_2.setCellStyle(centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 6));

        // Empty row
        sheet.createRow(rowNum++);

        // ROW 4: Người nhận
        Row row4 = sheet.createRow(rowNum++);
        row4.createCell(1).setCellValue("Họ và tên người nhận:");
        row4.getCell(1).setCellStyle(boldStyle);

        Cell cell4_2 = row4.createCell(2);
        String nguoiNhan = phieuXuat.getKhoaPhongYeuCau() != null ?
                phieuXuat.getKhoaPhongYeuCau().getTenKhoaPhong() :
                (phieuXuat.getNguoiNhan() != null ? phieuXuat.getNguoiNhan() : "");
        cell4_2.setCellValue(nguoiNhan);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 2, 6));

        // ROW 5: Theo hóa đơn/Số phiếu nhập
        Row row5 = sheet.createRow(rowNum++);
        row5.createCell(1).setCellValue("Theo hóa đơn số:");
        row5.getCell(1).setCellStyle(boldStyle);

        Cell cell5_2 = row5.createCell(2);
        String soPhieuYC = phieuXuat.getSoPhieuYeuCau() != null ? phieuXuat.getSoPhieuYeuCau() : "";
        cell5_2.setCellValue(soPhieuYC);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 6));

        // ROW 6: Xuất tại kho
        Row row6 = sheet.createRow(rowNum++);
        row6.createCell(1).setCellValue("Xuất tại kho:");
        row6.getCell(1).setCellStyle(boldStyle);

        Cell cell6_2 = row6.createCell(2);
        String tenKho = phieuXuat.getKho() != null ? phieuXuat.getKho().getTenKho() : "";
        cell6_2.setCellValue(tenKho);

        row6.createCell(4).setCellValue("Địa Điểm:");
        row6.getCell(4).setCellStyle(boldStyle);

        row6.createCell(5).setCellValue("Bệnh viện quận Tân Phú");

        // Empty row
        sheet.createRow(rowNum++);

        // ROW 8-10: Header bảng
        createTableHeaderXuat(sheet, rowNum, borderStyle, boldStyle, centerStyle);
        rowNum += 3;

        // Chi tiết hàng hóa
        int stt = 1;
        BigDecimal tongThanhTien = BigDecimal.ZERO;

        for (ChiTietPhieuXuat chiTiet : chiTietList) {
            Row detailRow = sheet.createRow(rowNum++);

            detailRow.createCell(0).setCellValue(stt++);
            detailRow.getCell(0).setCellStyle(centerStyle);

            Cell cellTen = detailRow.createCell(1);
            String tenHangHoa = chiTiet.getHangHoa() != null ?
                    chiTiet.getHangHoa().getTenHangHoa() : "";
            cellTen.setCellValue(tenHangHoa);
            cellTen.setCellStyle(borderStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 2));

            detailRow.createCell(3).setCellStyle(borderStyle); // Mã số (empty)

            Cell cellDVT = detailRow.createCell(4);
            String donViTinh = chiTiet.getHangHoa() != null &&
                    chiTiet.getHangHoa().getDonViTinh() != null ?
                    chiTiet.getHangHoa().getDonViTinh().getTenDonViTinh() : "";
            cellDVT.setCellValue(donViTinh);
            cellDVT.setCellStyle(centerStyle);

            Cell cellSLYC = detailRow.createCell(5);
            cellSLYC.setCellValue(chiTiet.getSoLuongYeuCau());
            cellSLYC.setCellStyle(centerStyle);

            Cell cellSLThucXuat = detailRow.createCell(6);
            cellSLThucXuat.setCellValue(chiTiet.getSoLuongXuat());
            cellSLThucXuat.setCellStyle(centerStyle);

            Cell cellDonGia = detailRow.createCell(7);
            cellDonGia.setCellValue(chiTiet.getDonGia().doubleValue());
            cellDonGia.setCellStyle(moneyStyle);

            Cell cellThanhTien = detailRow.createCell(8);
            BigDecimal thanhTien = chiTiet.getThanhTien();
            cellThanhTien.setCellValue(thanhTien.doubleValue());
            cellThanhTien.setCellStyle(moneyStyle);

            tongThanhTien = tongThanhTien.add(thanhTien);
        }

        // Empty row
        sheet.createRow(rowNum++);

        // ROW Tổng cộng
        Row rowTong = sheet.createRow(rowNum++);
        rowTong.createCell(1).setCellValue("Cộng");
        rowTong.getCell(1).setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 2));

        rowTong.createCell(3).setCellValue("x");
        rowTong.getCell(3).setCellStyle(centerStyle);

        rowTong.createCell(4).setCellValue("x");
        rowTong.getCell(4).setCellStyle(centerStyle);

        int tongSLYeuCau = chiTietList.stream()
                .mapToInt(ChiTietPhieuXuat::getSoLuongYeuCau)
                .sum();
        rowTong.createCell(5).setCellValue(tongSLYeuCau);
        rowTong.getCell(5).setCellStyle(centerStyle);

        int tongSLXuat = chiTietList.stream()
                .mapToInt(ChiTietPhieuXuat::getSoLuongXuat)
                .sum();
        rowTong.createCell(6).setCellValue(tongSLXuat);
        rowTong.getCell(6).setCellStyle(centerStyle);

        rowTong.createCell(7).setCellValue("x");
        rowTong.getCell(7).setCellStyle(centerStyle);

        Cell cellTongTT = rowTong.createCell(8);
        cellTongTT.setCellValue(tongThanhTien.doubleValue());
        cellTongTT.setCellStyle(moneyStyle);

        // ROW Tổng tiền bằng chữ
        Row rowBangChu = sheet.createRow(rowNum++);
        rowBangChu.createCell(1).setCellValue("Tổng số tiền bằng chữ:");
        rowBangChu.getCell(1).setCellStyle(boldStyle);

        Cell cellBangChu = rowBangChu.createCell(2);
        String tienBangChu = NumberToVietnameseWords.convert(tongThanhTien);
        cellBangChu.setCellValue(tienBangChu);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 2, 7));

        // ROW Số chứng từ
        Row rowChungTu = sheet.createRow(rowNum++);
        rowChungTu.createCell(1).setCellValue("Số chứng từ kèm theo:");
        rowChungTu.getCell(1).setCellStyle(boldStyle);

        // Empty row
        sheet.createRow(rowNum++);

        // ROW Chữ ký - Title
        Row rowChuKyTitle = sheet.createRow(rowNum++);
        rowChuKyTitle.createCell(0).setCellValue("NƠI GIAO");
        rowChuKyTitle.getCell(0).setCellStyle(boldStyle);

        rowChuKyTitle.createCell(2).setCellValue("NƠI NHẬN");
        rowChuKyTitle.getCell(2).setCellStyle(boldStyle);

        rowChuKyTitle.createCell(4).setCellValue("KẾ TOÁN");
        rowChuKyTitle.getCell(4).setCellStyle(boldStyle);

        rowChuKyTitle.createCell(7).setCellValue("KẾ TOÁN TRƯỞNG");
        rowChuKyTitle.getCell(7).setCellStyle(boldStyle);

        // Empty rows for signature
        for (int i = 0; i < 4; i++) {
            sheet.createRow(rowNum++);
        }

        // ROW Chữ ký - Names
        Row rowChuKyNames = sheet.createRow(rowNum++);

        String nguoiXuat = phieuXuat.getNguoiXuat() != null ?
                phieuXuat.getNguoiXuat().getHoTen() : "Võ Thành Nhân";
        rowChuKyNames.createCell(0).setCellValue(nguoiXuat);
        rowChuKyNames.getCell(0).setCellStyle(centerStyle);

        rowChuKyNames.createCell(4).setCellValue("Đặng Thị Kim Chi");
        rowChuKyNames.getCell(4).setCellStyle(centerStyle);

        rowChuKyNames.createCell(7).setCellValue("Nguyễn Thị Mỹ Lệ");
        rowChuKyNames.getCell(7).setCellStyle(centerStyle);

        // Set column widths
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 2500);
        sheet.setColumnWidth(4, 2500);
        sheet.setColumnWidth(5, 2500);
        sheet.setColumnWidth(6, 2500);
        sheet.setColumnWidth(7, 3500);
        sheet.setColumnWidth(8, 3500);

        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * Tạo header bảng chi tiết phiếu xuất
     */
    private void createTableHeaderXuat(XSSFSheet sheet, int startRow, CellStyle borderStyle,
                                       CellStyle boldStyle, CellStyle centerStyle) {
        Row headerRow1 = sheet.createRow(startRow);

        String[] headers1 = {"Số TT", "Tên nhãn hiệu,qui cách, phẩm chất\nvật tư, dụng cụ",
                "", "Mã\nsố", "Đơn vị tính", "Số lượng", "", "Đơn giá", "Thành tiền"};

        for (int i = 0; i < headers1.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(headers1[i]);
            cell.setCellStyle(centerStyle);
        }

        // Merge cells
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 0, 0)); // STT
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 1, 2)); // Tên
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 3, 3)); // Mã số
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 4, 4)); // ĐVT
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 5, 6)); // Số lượng
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 7, 7)); // Đơn giá
        sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 1, 8, 8)); // Thành tiền

        // Row 1 of table header
        Row headerRow2 = sheet.createRow(startRow + 1);
        Cell cell5 = headerRow2.createCell(5);
        cell5.setCellValue("Theo yêu cầu");
        cell5.setCellStyle(centerStyle);

        Cell cell6 = headerRow2.createCell(6);
        cell6.setCellValue("Thực xuất");
        cell6.setCellStyle(centerStyle);

        // Row 2 - Column labels
        Row headerRow3 = sheet.createRow(startRow + 2);
        String[] headers3 = {"A", "B", "", "C", "D", "1", "2", "3", "4"};

        for (int i = 0; i < headers3.length; i++) {
            Cell cell = headerRow3.createCell(i);
            cell.setCellValue(headers3[i]);
            cell.setCellStyle(centerStyle);
        }

        sheet.addMergedRegion(new CellRangeAddress(startRow + 2, startRow + 2, 1, 2));
    }

    // Helper methods (same as PhieuNhap)
    private String formatNgayThangNam(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return String.format("Ngày %02d tháng %02d năm %d",
                dateTime.getDayOfMonth(),
                dateTime.getMonthValue(),
                dateTime.getYear());
    }

    // Cell style creators (same as PhieuNhap)
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createBoldStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createCenterStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createBorderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createMoneyStyle(XSSFWorkbook workbook) {
        CellStyle style = createBorderStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}