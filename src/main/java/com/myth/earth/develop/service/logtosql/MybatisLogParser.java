package com.myth.earth.develop.service.logtosql;

import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.List;
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

        // 使用括号深度感知分割，正确处理含逗号/括号的参数值（如 JSON 数组）
        List<String> paramList = splitParamsByDepth(params);
        for (String param : paramList) {
            String trimmed = param.trim();
            // 处理 null 值（MyBatis 对 null 不输出类型后缀）
            if ("null".equals(trimmed)) {
                sqlTemplate = sqlTemplate.replaceFirst("\\?", "NULL");
                continue;
            }
            // 从后向前查找最后一个 '('，分离值和类型，避免值内含括号时误匹配
            int lastParen = trimmed.lastIndexOf('(');
            String value;
            if (lastParen >= 0) {
                value = trimmed.substring(0, lastParen).trim();
                String typeStr = trimmed.substring(lastParen).toLowerCase();
                if (typeStr.contains("string")) {
                    value = "'" + value.replace("'", "''") + "'";
                } else if (typeStr.contains("timestamp")) {
                    value = "'" + value + "'";
                }
            } else {
                value = trimmed;
            }
            sqlTemplate = sqlTemplate.replaceFirst("\\?", value);
        }
        return "-- Generated SQL:\n" + sqlTemplate + ";";
    }

    /**
     * 括号深度感知参数分割。
     * 追踪 ()、[]、{} 三种括号深度，仅当三个深度均为 0 且逗号后跟空格时才作为参数分隔符。
     * MyBatis 参数分隔符为 ", "（逗号+空格），值内部逗号后紧跟 "(" 无空格，
     * 如 "WORKAREA01-V,(String), NEXT(Integer)" 中仅 ")," 后的 ", " 是分隔符。
     */
    private static List<String> splitParamsByDepth(String params) {
        List<String> result = new ArrayList<>();
        int parenDepth = 0, bracketDepth = 0, braceDepth = 0;
        int start = 0;
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            switch (c) {
                case '(': parenDepth++; break;
                case ')': parenDepth--; break;
                case '[': bracketDepth++; break;
                case ']': bracketDepth--; break;
                case '{': braceDepth++; break;
                case '}': braceDepth--; break;
                case ',':
                    // 仅当括号全部闭合且逗号后跟空格时，才视为参数分隔符
                    if (parenDepth == 0 && bracketDepth == 0 && braceDepth == 0
                            && i + 1 < params.length() && params.charAt(i + 1) == ' ') {
                        result.add(params.substring(start, i));
                        start = i + 1;
                    }
                    break;
            }
        }
        result.add(params.substring(start));
        return result;
    }

}
