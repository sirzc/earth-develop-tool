package com.myth.earth.develop.utils;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * unicode转换工具
 *
 * @author zhouchao
 * @date 2024-03-21 19:35
 */
public final class UnicodeUtil {

    /**
     * 匹配Unicode编码部分
     */
    private static final Pattern PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    private UnicodeUtil() {

    }

    /**
     * Unicode 中文编码内容
     * @param unicode Unicode编码内容
     * @return 中文内容
     */
    public static String unicodeToChinese(String unicode) {
        StringBuilder chinese = new StringBuilder();
        String[] arr = unicode.split("\\\\u");

        for (int i = 1; i < arr.length; i++) {
            int h = Integer.parseInt(arr[i].substring(0, 4), 16);
            chinese.append((char) h);
            if (arr[i].length() > 4) {
                chinese.append(arr[i].substring(4));
            }
        }

        return Normalizer.normalize(chinese.toString(), Normalizer.Form.NFKD);
    }

    /**
     * 将字符串（包含Unicode）转换为中文
     *
     * @param str 字符串
     * @return 处理后的内容
     */
    public static String convertUnicodeToChinese(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = PATTERN.matcher(str);
        int lastIndex = 0;
        while (matcher.find()) {
            // 添加非Unicode编码部分
            sb.append(str.substring(lastIndex, matcher.start()));
            // 获取Unicode编码
            String unicode = matcher.group(1);
            // 将Unicode编码转为十进制
            int codePoint = Integer.parseInt(unicode, 16);
            // 添加对应的中文字符
            sb.append((char) codePoint);
            lastIndex = matcher.end();
        }
        // 添加剩余的非Unicode编码部分
        sb.append(str.substring(lastIndex));
        return sb.toString();
    }
}
