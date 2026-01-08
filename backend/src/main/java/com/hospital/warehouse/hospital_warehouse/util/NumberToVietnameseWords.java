package com.hospital.warehouse.hospital_warehouse.util;

import java.math.BigDecimal;

/**
 * Utility class để chuyển đổi số thành chữ tiếng Việt
 * Dùng cho việc hiển thị tổng tiền bằng chữ trong phiếu nhập/xuất
 */
public class NumberToVietnameseWords {

    private static final String[] ONES = {
            "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private static final String[] TEENS = {
            "mười", "mười một", "mười hai", "mười ba", "mười bốn", "mười lăm",
            "mười sáu", "mười bảy", "mười tám", "mười chín"
    };

    /**
     * Chuyển đổi số thành chữ tiếng Việt
     *
     * @param amount Số tiền cần chuyển đổi
     * @return Chuỗi tiếng Việt biểu diễn số tiền
     */
    public static String convert(BigDecimal amount) {
        if (amount == null) {
            return "Không đồng";
        }

        long value = amount.longValue();

        if (value == 0) {
            return "Không đồng";
        }

        if (value < 0) {
            return "Âm " + convert(amount.abs()) + " đồng";
        }

        StringBuilder result = new StringBuilder();

        // Tỷ (billions)
        if (value >= 1000000000) {
            result.append(convertHundreds((int) (value / 1000000000)));
            result.append(" tỷ ");
            value %= 1000000000;
        }

        // Triệu (millions)
        if (value >= 1000000) {
            result.append(convertHundreds((int) (value / 1000000)));
            result.append(" triệu ");
            value %= 1000000;
        }

        // Nghìn (thousands)
        if (value >= 1000) {
            result.append(convertHundreds((int) (value / 1000)));
            result.append(" nghìn ");
            value %= 1000;
        }

        // Đơn vị (ones)
        if (value > 0) {
            result.append(convertHundreds((int) value));
        }

        result.append(" đồng");

        // Capitalize first letter
        String resultStr = result.toString().trim();
        return resultStr.substring(0, 1).toUpperCase() + resultStr.substring(1);
    }

    /**
     * Chuyển đổi số từ 0-999
     */
    private static String convertHundreds(int number) {
        if (number == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        // Hàng trăm
        int hundreds = number / 100;
        if (hundreds > 0) {
            result.append(ONES[hundreds]).append(" trăm");
            number %= 100;

            if (number > 0 && number < 10) {
                result.append(" lẻ");
            }
        }

        // Hàng chục và đơn vị
        if (number >= 20) {
            int tens = number / 10;
            result.append(" ").append(ONES[tens]).append(" mươi");
            number %= 10;

            if (number == 5) {
                result.append(" lăm");
            } else if (number > 0) {
                result.append(" ").append(ONES[number]);
            }
        } else if (number >= 10) {
            if (number == 15) {
                result.append(" mười lăm");
            } else {
                result.append(" ").append(TEENS[number - 10]);
            }
        } else if (number > 0) {
            result.append(" ").append(ONES[number]);
        }

        return result.toString().trim();
    }

    /**
     * Phương thức test
     */
    public static void main(String[] args) {
        // Test cases
        System.out.println(convert(new BigDecimal("0")));           // Không đồng
        System.out.println(convert(new BigDecimal("15")));          // Mười lăm đồng
        System.out.println(convert(new BigDecimal("25")));          // Hai mươi lăm đồng
        System.out.println(convert(new BigDecimal("100")));         // Một trăm đồng
        System.out.println(convert(new BigDecimal("105")));         // Một trăm lẻ năm đồng
        System.out.println(convert(new BigDecimal("1000")));        // Một nghìn đồng
        System.out.println(convert(new BigDecimal("1250000")));     // Một triệu hai trăm năm mươi nghìn đồng
        System.out.println(convert(new BigDecimal("10000000")));    // Mười triệu đồng
        System.out.println(convert(new BigDecimal("1000000000")));  // Một tỷ đồng
    }
}