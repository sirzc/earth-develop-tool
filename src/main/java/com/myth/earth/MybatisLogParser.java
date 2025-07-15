package com.myth.earth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志解析器
 *
 * @author Inger
 * @since 2025/7/15
 */
public class MybatisLogParser {


    // 判断是否为 MyBatis 日志
    private static boolean isMyBatisLog(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("==> Preparing:") || text.contains("==> Parameters:");
    }

    // 解析日志生成可执行 SQL
    public static String parse(String log) {
        if (!isMyBatisLog(log)) {
            return "";
        }
        try {
            // 匹配 "Preparing: SELECT ..." 和 "Parameters: ..."
            // 允许前面有任意内容（.*），只要包含 "Preparing:" 和 SQL
            Pattern preparingPattern = Pattern.compile(".*==>\\s+Preparing:\\s+(.+)");
            Pattern parametersPattern = Pattern.compile(".*==>\\s+Parameters:\\s+(.+)");

            Matcher preparingMatcher = preparingPattern.matcher(log);
            Matcher parametersMatcher = parametersPattern.matcher(log);

            if (preparingMatcher.find() && parametersMatcher.find()) {
                String sqlTemplate = preparingMatcher.group(1);
                String params = parametersMatcher.group(1);

                // 处理参数（示例：将 "1(Integer)" 转换为 "1"）
                String[] paramList = params.split(",\\s*");
                for (String param : paramList) {
                    String value = param.replaceAll("\\(.*?\\)", "").trim();
                    if (param.toLowerCase().contains("string") || param.contains("'")) {
                        value = "'" + value.replace("'", "''") + "'"; // 处理字符串转义
                    }
                    sqlTemplate = sqlTemplate.replaceFirst("\\?", value);
                }
                return "-- Generated SQL:\n" + sqlTemplate + ";";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
