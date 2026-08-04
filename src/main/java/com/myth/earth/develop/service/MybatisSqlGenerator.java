package com.myth.earth.develop.service;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis 动态 SQL 生成器
 * <p>
 * 对 MyBatis XML 中的动态 SQL 标签进行静态解析，生成可读的预执行 SQL。
 * 参数位置使用 {@code #{paramName}} 占位符表示。
 * <p>
 * 支持的动态标签：
 * <ul>
 *   <li>{@code <if>} — 默认条件为 true，包含内部 SQL</li>
 *   <li>{@code <where>} — 去除首部多余的 AND/OR</li>
 *   <li>{@code <set>} — 去除尾部多余的逗号</li>
 *   <li>{@code <choose>/<when>/<otherwise>} — 取第一个 when 分支</li>
 *   <li>{@code <foreach>} — 展开一次循环，用占位符替换 item 引用</li>
 *   <li>{@code <trim>} — 按 prefix/suffix/overrides 处理</li>
 *   <li>{@code <include>} — 内联同文件 sql 片段</li>
 *   <li>{@code <bind>} — 跳过（静态分析无法求值）</li>
 * </ul>
 *
 * @author Claude
 * @since 2026/8/3
 */
public class MybatisSqlGenerator {

    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * 递归处理 XML 节点，生成 SQL 字符串。
     *
     * @param element     当前 XML 元素（可以是 statement 节点或动态标签节点）
     * @param sqlFragments 同文件内的 sql 片段映射（id → Element），用于 include 解析
     * @return 生成的 SQL 片段
     */
    public String processNode(Element element, Map<String, Element> sqlFragments) {
        StringBuilder sb = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text != null && !text.trim().isEmpty()) {
                    // 将换行和多余空白压缩为单个空格
                    sb.append(MULTI_SPACE_PATTERN.matcher(text.trim()).replaceAll(" ")).append(" ");
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                String tagName = el.getTagName().toLowerCase();
                switch (tagName) {
                    case "if":
                        sb.append(processIf(el, sqlFragments));
                        break;
                    case "where":
                        sb.append(processWhere(el, sqlFragments));
                        break;
                    case "set":
                        sb.append(processSet(el, sqlFragments));
                        break;
                    case "choose":
                        sb.append(processChoose(el, sqlFragments));
                        break;
                    case "foreach":
                        sb.append(processForeach(el, sqlFragments));
                        break;
                    case "trim":
                        sb.append(processTrim(el, sqlFragments));
                        break;
                    case "include":
                        sb.append(processInclude(el, sqlFragments));
                        break;
                    case "bind":
                        // 静态分析无法求值 bind 表达式，跳过
                        break;
                    default:
                        // 未知标签，递归处理其子节点
                        sb.append(processNode(el, sqlFragments));
                        break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 入口方法：从 statement Element 生成完整 SQL。
     *
     * @param statementElement statement 节点（select/insert/update/delete）
     * @param sqlFragments     同文件内的 sql 片段映射
     * @return 格式化后的 SQL 字符串
     */
    public String generate(Element statementElement, Map<String, Element> sqlFragments) {
        String rawSql = processNode(statementElement, sqlFragments);
        return formatSql(rawSql);
    }

    // ======================== 动态标签处理 ========================

    /**
     * &lt;if test="..."&gt; — 默认条件为 true，包含内部 SQL
     */
    private String processIf(Element el, Map<String, Element> sqlFragments) {
        return processNode(el, sqlFragments);
    }

    /**
     * &lt;where&gt; — 递归处理子节点，去除首部多余的 AND/OR
     */
    private String processWhere(Element el, Map<String, Element> sqlFragments) {
        String inner = processNode(el, sqlFragments).trim();
        if (inner.isEmpty()) {
            return "";
        }
        // 去除首部多余的 AND / OR
        String upper = inner.toUpperCase();
        if (upper.startsWith("AND ")) {
            inner = inner.substring(4).trim();
        } else if (upper.startsWith("OR ")) {
            inner = inner.substring(3).trim();
        }
        return "WHERE " + inner + " ";
    }

    /**
     * &lt;set&gt; — 递归处理子节点，去除尾部多余逗号
     */
    private String processSet(Element el, Map<String, Element> sqlFragments) {
        String inner = processNode(el, sqlFragments).trim();
        if (inner.isEmpty()) {
            return "";
        }
        // 去除尾部逗号
        if (inner.endsWith(",")) {
            inner = inner.substring(0, inner.length() - 1).trim();
        }
        return "SET " + inner + " ";
    }

    /**
     * &lt;choose&gt;/&lt;when&gt;/&lt;otherwise&gt; — 取第一个 when 分支，否则取 otherwise
     */
    private String processChoose(Element el, Map<String, Element> sqlFragments) {
        NodeList children = el.getChildNodes();
        // 优先取第一个 when
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                if ("when".equalsIgnoreCase(child.getTagName())) {
                    return processNode(child, sqlFragments);
                }
            }
        }
        // 没有 when，取 otherwise
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                if ("otherwise".equalsIgnoreCase(child.getTagName())) {
                    return processNode(child, sqlFragments);
                }
            }
        }
        return "";
    }

    /**
     * &lt;foreach&gt; — 展开一次循环，用占位符替换 item 引用。
     * <p>
     * 例：{@code <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>}
     * 输出：{@code (#{ids[0]})}
     */
    private String processForeach(Element el, Map<String, Element> sqlFragments) {
        String item = el.getAttribute("item");
        String collection = el.getAttribute("collection");
        String open = el.getAttribute("open");
        String close = el.getAttribute("close");
        String separator = el.getAttribute("separator");

        if (open == null) open = "";
        if (close == null) close = "";
        if (separator == null) separator = "";

        // 处理循环体，将 #{item.xxx} 或 #{item} 替换为 #{collection[0].xxx} 或 #{collection[0]}
        // collection 可能含特殊字符（如 req.getApplAreas()），替换字符串需要转义
        String body = processNode(el, sqlFragments);
        if (item != null && !item.isEmpty()) {
            String collLiteral = Matcher.quoteReplacement(collection);
            // 替换 #{item.field} → #{collection[0].field}
            body = body.replaceAll("#\\{" + Pattern.quote(item) + "\\.(\\w+)}", "#{" + collLiteral + "[0].$1}");
            // 替换 #{item} → #{collection[0]}
            body = body.replaceAll("#\\{" + Pattern.quote(item) + "}", "#{" + collLiteral + "[0]}");
            // 替换 ${item.field} → ${collection[0].field}
            body = body.replaceAll("\\$\\{" + Pattern.quote(item) + "\\.(\\w+)}", "${" + collLiteral + "[0].$1}");
            body = body.replaceAll("\\$\\{" + Pattern.quote(item) + "}", "${" + collLiteral + "[0]}");
        }

        return open + body.trim() + close + " ";
    }

    /**
     * &lt;trim&gt; — 根据 prefix/suffix/prefixOverrides/suffixOverrides 属性处理
     */
    private String processTrim(Element el, Map<String, Element> sqlFragments) {
        String prefix = getAttr(el, "prefix");
        String suffix = getAttr(el, "suffix");
        String prefixOverrides = getAttr(el, "prefixOverrides");
        String suffixOverrides = getAttr(el, "suffixOverrides");

        String inner = processNode(el, sqlFragments).trim();
        if (inner.isEmpty()) {
            return "";
        }

        // 处理 prefixOverrides：去除首部匹配的前缀
        if (prefixOverrides != null && !prefixOverrides.isEmpty()) {
            for (String override : prefixOverrides.split("\\|")) {
                String trimmed = override.trim();
                if (!trimmed.isEmpty() && inner.toUpperCase().startsWith(trimmed.toUpperCase())) {
                    inner = inner.substring(trimmed.length()).trim();
                    break;
                }
            }
        }

        // 处理 suffixOverrides：去除尾部匹配的后缀
        if (suffixOverrides != null && !suffixOverrides.isEmpty()) {
            for (String override : suffixOverrides.split("\\|")) {
                String trimmed = override.trim();
                if (!trimmed.isEmpty() && inner.toUpperCase().endsWith(trimmed.toUpperCase())) {
                    inner = inner.substring(0, inner.length() - trimmed.length()).trim();
                    break;
                }
            }
        }

        StringBuilder result = new StringBuilder();
        if (!prefix.isEmpty()) {
            result.append(prefix).append(" ");
        }
        result.append(inner);
        if (!suffix.isEmpty()) {
            result.append(" ").append(suffix);
        }
        return result.toString() + " ";
    }

    /**
     * &lt;include refid="..."&gt; — 查找对应 sql 片段，递归解析并内联替换
     */
    private String processInclude(Element el, Map<String, Element> sqlFragments) {
        String refId = el.getAttribute("refid");
        if (refId == null || refId.isEmpty()) {
            return "";
        }
        // 如果 refId 包含 '.'，表示跨命名空间引用，静态分析无法处理，返回注释提示
        if (refId.contains(".")) {
            return "/* include " + refId + " */ ";
        }
        Element fragment = sqlFragments.get(refId);
        if (fragment != null) {
            return processNode(fragment, sqlFragments);
        }
        return "/* include " + refId + " not found */ ";
    }

    // ======================== 工具方法 ========================

    /**
     * 格式化最终 SQL：压缩多余空白、去除首尾空格
     */
    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        // 压缩连续空白为单个空格
        String result = MULTI_SPACE_PATTERN.matcher(sql.trim()).replaceAll(" ");
        return result.trim();
    }

    private String getAttr(Element el, String name) {
        String val = el.getAttribute(name);
        return val != null ? val : "";
    }
}
