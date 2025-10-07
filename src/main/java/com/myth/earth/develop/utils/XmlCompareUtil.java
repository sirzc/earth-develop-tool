/*
 * Copyright (c) 2025 周潮. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.myth.earth.develop.utils;

import com.myth.earth.develop.model.CompareResult;
import com.myth.earth.develop.model.DifferenceResult;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.*;

public class XmlCompareUtil {

    private static final DocumentBuilderFactory factory   = DocumentBuilderFactory.newInstance();
    public static final  String                 FILE_TYPE = "xml";

    public static CompareResult parseAndCompare(@NotNull String sourcePath, @NotNull String targetPath) throws Exception {
        return compareXmlFiles(new File(sourcePath), new File(targetPath));
    }

    /**
     * 比较两个XML文件
     *
     * @param sourceFile 源XML文件
     * @param targetFile 目标XML文件
     * @return CompareResult 比较结果
     * @throws Exception XML解析异常
     */
    public static CompareResult compareXmlFiles(File sourceFile, File targetFile) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document sourceDoc = builder.parse(sourceFile);
        Document targetDoc = builder.parse(targetFile);

        Map<String, String> sourceFlatMap = new HashMap<>();
        Map<String, String> targetFlatMap = new HashMap<>();

        flattenXml(sourceDoc.getDocumentElement(), "", sourceFlatMap);
        flattenXml(targetDoc.getDocumentElement(), "", targetFlatMap);

        return compareFlattenedMaps(sourceFlatMap, targetFlatMap);
    }

    /**
     * 比较两个XML字符串
     *
     * @param sourceXml 源XML字符串
     * @param targetXml 目标XML字符串
     * @return CompareResult 比较结果
     * @throws Exception XML解析异常
     */
    public static CompareResult compareXmlStrings(String sourceXml, String targetXml) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document sourceDoc = builder.parse(new InputSource(new StringReader(sourceXml)));
        Document targetDoc = builder.parse(new InputSource(new StringReader(targetXml)));

        Map<String, String> sourceFlatMap = new HashMap<>();
        Map<String, String> targetFlatMap = new HashMap<>();

        flattenXml(sourceDoc.getDocumentElement(), "", sourceFlatMap);
        flattenXml(targetDoc.getDocumentElement(), "", targetFlatMap);

        return compareFlattenedMaps(sourceFlatMap, targetFlatMap);
    }

    /**
     * 将XML扁平化为key-value映射，保持层级关系
     *
     * @param node      XML节点
     * @param prefix    键前缀
     * @param resultMap 结果映射
     */
    private static void flattenXml(Node node, String prefix, Map<String, String> resultMap) {
        String nodeName = node.getNodeName();
        String currentPath = prefix.isEmpty() ? nodeName : prefix;

        // 处理属性
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String attrKey = currentPath + "[@" + attr.getNodeName() + "]";
                resultMap.put(attrKey, attr.getNodeValue());
            }
        }

        // 收集所有子元素节点
        NodeList childNodes = node.getChildNodes();
        Map<String, List<Node>> childElementGroups = new LinkedHashMap<>();

        // 按节点名称分组
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String childName = child.getNodeName();
                childElementGroups.computeIfAbsent(childName, k -> new ArrayList<>()).add(child);
            }
        }

        // 处理每个分组
        for (Map.Entry<String, List<Node>> entry : childElementGroups.entrySet()) {
            String childName = entry.getKey();
            List<Node> children = entry.getValue();

            if (children.size() == 1) {
                // 单个元素，不添加索引
                String childPath = currentPath + "." + childName;
                flattenXml(children.get(0), childPath, resultMap);
            } else {
                // 多个同名元素，添加索引（从1开始）
                for (int i = 0; i < children.size(); i++) {
                    String childPath = currentPath + "." + childName + "[" + (i + 1) + "]";
                    flattenXml(children.get(i), childPath, resultMap);
                }
            }
        }

        // 处理文本内容
        StringBuilder textContent = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty()) {
                    textContent.append(text);
                }
            }
        }

        if (textContent.length() > 0) {
            resultMap.put(currentPath, textContent.toString());
        }
    }

    /**
     * 比较两个扁平化的Map
     *
     * @param sourceMap 源Map
     * @param targetMap 目标Map
     * @return CompareResult 比较结果
     */
    private static CompareResult compareFlattenedMaps(Map<String, String> sourceMap, Map<String, String> targetMap) {
        CompareResult result = new CompareResult();

        Set<String> sourceKeys = new HashSet<>(sourceMap.keySet());
        Set<String> targetKeys = new HashSet<>(targetMap.keySet());

        // 找出只存在于source的key
        Set<String> onlyInSource = new HashSet<>(sourceKeys);
        onlyInSource.removeAll(targetKeys);

        // 找出只存在于target的key
        Set<String> onlyInTarget = new HashSet<>(targetKeys);
        onlyInTarget.removeAll(sourceKeys);

        // 找出共同的key
        Set<String> commonKeys = new HashSet<>(sourceKeys);
        commonKeys.retainAll(targetKeys);

        // 找出相同key但值不同的项
        List<DifferenceResult> differenceResults = new ArrayList<>();
        for (String key : commonKeys) {
            String sourceValue = sourceMap.get(key);
            String targetValue = targetMap.get(key);

            if (!Objects.equals(sourceValue, targetValue)) {
                differenceResults.add(new DifferenceResult(key, sourceValue, targetValue));
            }
        }

        result.setSourceKeys(onlyInSource);
        result.setTargetKeys(onlyInTarget);
        result.setDifferenceResults(differenceResults);

        return result;
    }
}
