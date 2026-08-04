package com.myth.earth.develop.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyBatis XML 解析工具类
 * <p>
 * 负责解析 MyBatis Mapper XML 文件，提供 statement 节点定位、sql 片段提取等功能。
 *
 * @author Claude
 * @since 2026/8/3
 */
public class MybatisXmlParser {

    private static final Set<String> STATEMENT_TAGS = new HashSet<>(
            Arrays.asList("select", "insert", "update", "delete")
    );

    /**
     * 解析 XML 内容，返回 Document 对象
     */
    public static Document parseXml(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 关闭 DTD 外部加载，避免无网络时解析失败
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 检测给定 XML 内容是否为 MyBatis Mapper XML（根节点为 mapper）
     */
    public static boolean isMybatisXml(String xmlContent) {
        try {
            Document doc = parseXml(xmlContent);
            return "mapper".equalsIgnoreCase(doc.getDocumentElement().getTagName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 mapper 根节点下所有 statement 节点（select/insert/update/delete）
     */
    public static List<Element> getAllStatements(Document doc) {
        List<Element> statements = new ArrayList<>();
        Element root = doc.getDocumentElement();
        if (root == null || !"mapper".equalsIgnoreCase(root.getTagName())) {
            return statements;
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element el = (Element) children.item(i);
                if (STATEMENT_TAGS.contains(el.getTagName().toLowerCase())) {
                    statements.add(el);
                }
            }
        }
        return statements;
    }

    /**
     * 按 statement id 查找目标节点
     *
     * @param doc         已解析的 XML Document
     * @param statementId 要查找的 statement id 属性值
     * @return 匹配的 Element，未找到返回 null
     */
    public static Element findStatementById(Document doc, String statementId) {
        if (doc == null || statementId == null) {
            return null;
        }
        for (Element stmt : getAllStatements(doc)) {
            if (statementId.equals(stmt.getAttribute("id"))) {
                return stmt;
            }
        }
        return null;
    }

    /**
     * 获取同文件内所有 sql 片段，key 为 sql id
     * <p>
     * 供 &lt;include refid="..."&gt; 内联替换使用。
     */
    public static Map<String, Element> getSqlFragments(Document doc) {
        Map<String, Element> fragments = new HashMap<>();
        Element root = doc.getDocumentElement();
        if (root == null) {
            return fragments;
        }
        NodeList sqlNodes = root.getElementsByTagName("sql");
        for (int i = 0; i < sqlNodes.getLength(); i++) {
            Element sqlEl = (Element) sqlNodes.item(i);
            String id = sqlEl.getAttribute("id");
            if (!id.isEmpty()) {
                fragments.put(id, sqlEl);
            }
        }
        return fragments;
    }

    /**
     * 检测编辑器光标偏移量是否位于 statement 标签的 id 属性值上。
     * <p>
     * 通过文本扫描实现，避免完整 XML 解析，适合在 Action update() 中高频调用。
     *
     * @param content 文件文本内容
     * @param offset  光标偏移量
     * @return true 表示光标位于 statement 的 id 属性值上
     */
    public static boolean isOffsetOnStatementId(String content, int offset) {
        return findStatementIdAtOffset(content, offset) != null;
    }

    /**
     * 获取光标偏移量所在位置对应的 statement id。
     * <p>
     * 从 offset 向前扫描找到最近的 {@code <}，判断是否为 statement 标签，
     * 并检查 offset 是否位于 id 属性值范围内。
     *
     * @param content 文件文本内容
     * @param offset  光标偏移量
     * @return statement id 值，若光标不在 statement id 上则返回 null
     */
    public static String findStatementIdAtOffset(String content, int offset) {
        if (content == null || offset < 0 || offset >= content.length()) {
            return null;
        }

        // 向前扫描找到最近的 '<'
        int tagStart = -1;
        for (int i = offset; i >= 0; i--) {
            char c = content.charAt(i);
            if (c == '<') {
                tagStart = i;
                break;
            }
            // 遇到 '>' 说明 offset 不在任何标签内
            if (c == '>') {
                return null;
            }
        }
        if (tagStart < 0) {
            return null;
        }

        // 检查是否是 statement 标签（select/insert/update/delete）
        String afterLt = content.substring(tagStart + 1, Math.min(tagStart + 8, content.length())).trim();
        boolean isStatementTag = false;
        for (String tag : STATEMENT_TAGS) {
            if (afterLt.startsWith(tag)) {
                isStatementTag = true;
                break;
            }
        }
        if (!isStatementTag) {
            return null;
        }

        // 找到标签的结束 '>'
        int tagEnd = content.indexOf('>', tagStart);
        if (tagEnd < 0) {
            return null;
        }

        String tagContent = content.substring(tagStart, tagEnd + 1);

        // 查找 id="..." 的位置
        Pattern idPattern = Pattern.compile("\\bid\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = idPattern.matcher(tagContent);
        if (matcher.find()) {
            // 计算 id 值在 tagContent 中的起止位置
            int idValueStart = matcher.start(1);
            int idValueEnd = matcher.end(1);
            // 将 tagContent 中的位置映射回 content 中的绝对位置
            int absIdStart = tagStart + idValueStart;
            int absIdEnd = tagStart + idValueEnd;
            if (offset >= absIdStart && offset <= absIdEnd) {
                return matcher.group(1);
            }
        }

        return null;
    }
}
