package com.myth.earth.develop.utils;

/**
 * 进制转换结果格式化工具类
 * 根据目标进制对转换结果进行格式化显示（添加分隔符）
 *
 * @author Claude
 * @since 2025/01/13
 */
public class ResultFormatterHelper {

    /**
     * 格式化结果字符串
     *
     * @param result 转换结果字符串
     * @param radix 目标进制（2、8、10、16）
     * @return 格式化后的字符串
     */
    public static String formatResult(String result, int radix) {
        if (result == null || result.isEmpty()) {
            return result;
        }

        switch (radix) {
            case 2:
                return formatBinary(result);
            case 8:
                return formatOctal(result);
            case 10:
                return formatDecimal(result);
            case 16:
                return formatHexadecimal(result);
            default:
                return result;
        }
    }

    /**
     * 格式化二进制：4 位一组
     * 补零至 4 的倍数
     */
    private static String formatBinary(String binary) {
        if (binary.isEmpty()) {
            return binary;
        }

        // 补零至 4 的倍数
        int paddingNeeded = (4 - (binary.length() % 4)) % 4;
        String padded = repeat("0", paddingNeeded) + binary;

        // 分组
        return insertSpaces(padded, 4);
    }

    /**
     * 格式化八进制：3 位一组
     * 补零至 3 的倍数
     */
    private static String formatOctal(String octal) {
        if (octal.isEmpty()) {
            return octal;
        }

        // 补零至 3 的倍数
        int paddingNeeded = (3 - (octal.length() % 3)) % 3;
        String padded = repeat("0", paddingNeeded) + octal;

        // 分组
        return insertSpaces(padded, 3);
    }

    /**
     * 格式化十进制：3 位一组（从右至左）
     * 补零至 3 的倍数
     */
    private static String formatDecimal(String decimal) {
        if (decimal.isEmpty()) {
            return decimal;
        }

        // 补零至 3 的倍数
        int paddingNeeded = (3 - (decimal.length() % 3)) % 3;
        String padded = repeat("0", paddingNeeded) + decimal;

        // 分组（从右至左）
        return insertSpacesFromRight(padded, 3);
    }

    /**
     * 格式化十六进制：2 位一组
     * 补零至 2 的倍数
     */
    private static String formatHexadecimal(String hex) {
        if (hex.isEmpty()) {
            return hex;
        }

        // 补零至 2 的倍数
        int paddingNeeded = (2 - (hex.length() % 2)) % 2;
        String padded = repeat("0", paddingNeeded) + hex;

        // 分组
        return insertSpaces(padded, 2);
    }

    /**
     * 从左至右每 groupSize 位插入空格
     */
    private static String insertSpaces(String str, int groupSize) {
        if (str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (i > 0 && i % groupSize == 0) {
                result.append(" ");
            }
            result.append(str.charAt(i));
        }
        return result.toString();
    }

    /**
     * 从右至左每 groupSize 位插入空格
     */
    private static String insertSpacesFromRight(String str, int groupSize) {
        if (str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % groupSize == 0) {
                result.append(" ");
            }
            result.append(str.charAt(i));
        }
        return result.toString();
    }

    /**
     * Java 8 兼容的字符串重复方法
     */
    private static String repeat(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }
}
