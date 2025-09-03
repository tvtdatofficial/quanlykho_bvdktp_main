package com.hospital.warehouse.hospital_warehouse.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CodeGenerator {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * Generate mã nhà cung cấp: NCC + YYYYMMDD + sequence
     */
    public String generateMaNCC() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = sequence.getAndIncrement();
        return String.format("NCC%s%03d", dateStr, seq);
    }

    /**
     * Generate mã danh mục: DM + sequence
     */
    public String generateMaDanhMuc(String prefix) {
        int seq = sequence.getAndIncrement();
        return String.format("%s%03d", prefix.toUpperCase(), seq);
    }

    /**
     * Generate mã đơn vị tính: DVT + sequence
     */
    public String generateMaDVT() {
        int seq = sequence.getAndIncrement();
        return String.format("DVT%03d", seq);
    }

    /**
     * Generate mã hàng hóa: HH + YYYYMMDD + sequence
     */
    public String generateMaHangHoa() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = sequence.getAndIncrement();
        return String.format("HH%s%04d", dateStr, seq);
    }

    /**
     * Generate custom code with prefix
     */
    public String generateCode(String prefix, int length) {
        int seq = sequence.getAndIncrement();
        String format = String.format("%%s%%0%dd", length);
        return String.format(format, prefix, seq);
    }
}