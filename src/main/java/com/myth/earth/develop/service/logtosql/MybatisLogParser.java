package com.myth.earth.develop.service.logtosql;

import com.intellij.openapi.diagnostic.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志解析器
 *
 * @author Inger
 * @since 2025/7/15
 */
public class MybatisLogParser {

    private static final Logger logger = Logger.getInstance(MybatisLogParser.class);

    // 判断是否为 MyBatis 日志
    private static boolean isMyBatisLog(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.contains("==> Preparing:") || text.contains("==> Parameters:");
    }

    // 解析日志生成可执行 SQL
    public static String parse(String consoleLog) {
        if (!isMyBatisLog(consoleLog)) {
            return "";
        }
        try {
            // 匹配 "Preparing: SELECT ..." 和 "Parameters: ..."
            // 允许前面有任意内容（.*），只要包含 "Preparing:" 和 SQL
            Pattern preparingPattern = Pattern.compile(".*==>\\s+Preparing:\\s+(.+)");
            Pattern parametersPattern = Pattern.compile(".*==>\\s+Parameters:\\s+(.+)");

            Matcher preparingMatcher = preparingPattern.matcher(consoleLog);
            Matcher parametersMatcher = parametersPattern.matcher(consoleLog);

            return buildSqlString(preparingMatcher, parametersMatcher);
        } catch (Exception e) {
            logger.warn("MyBatis Log Parser Error:", e);
        }
        return "";
    }

    private static String buildSqlString(Matcher preparingMatcher, Matcher parametersMatcher) {
        if (!preparingMatcher.find() || !parametersMatcher.find()) {
            logger.warn("No sql or parameters from the log, Please check the selected text.");
            return "";
        }
        String sqlTemplate = preparingMatcher.group(1);
        String params = parametersMatcher.group(1);

        // 处理参数（示例：将 "1(Integer)" 转换为 "1"）
        String[] paramList = params.split(",\\s*");
        for (String param : paramList) {
            String value = param.replaceAll("\\(.*?\\)", "").trim();
            if (param.toLowerCase().contains("string") || param.contains("'")) {
                // 处理字符串转义
                value = "'" + value.replace("'", "''") + "'";
            }
            sqlTemplate = sqlTemplate.replaceFirst("\\?", value);
        }
        return "-- Generated SQL:\n" + sqlTemplate + ";";
    }

}
