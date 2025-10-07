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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myth.earth.develop.model.CompareResult;
import com.myth.earth.develop.model.DifferenceResult;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Json比较工具
 *
 * @author zhouchao
 * @date 2025/10/7 下午4:41
 **/
public class JsonCompareUtil {

    public static final String FILE_TYPE           = "json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CompareResult parseAndCompare(@NotNull String sourcePath, @NotNull String targetPath) throws IOException {
        return compareJsonFiles(new File(sourcePath), new File(targetPath));
    }

    /**
     * 比较两个JSON文件
     *
     * @param sourceFile 源JSON文件
     * @param targetFile 目标JSON文件
     * @return CompareResult 比较结果
     * @throws IOException 文件读取异常
     */
    public static CompareResult compareJsonFiles(File sourceFile, File targetFile) throws IOException {
        JsonNode sourceRoot = objectMapper.readTree(sourceFile);
        JsonNode targetRoot = objectMapper.readTree(targetFile);

        Map<String, String> sourceFlatMap = new HashMap<>();
        Map<String, String> targetFlatMap = new HashMap<>();

        flattenJson(sourceRoot, "", sourceFlatMap);
        flattenJson(targetRoot, "", targetFlatMap);

        return compareFlattenedMaps(sourceFlatMap, targetFlatMap);
    }

    /**
     * 比较两个JSON字符串
     *
     * @param sourceJson 源JSON字符串
     * @param targetJson 目标JSON字符串
     * @return CompareResult 比较结果
     * @throws IOException JSON解析异常
     */
    public static CompareResult compareJsonStrings(String sourceJson, String targetJson) throws IOException {
        JsonNode sourceRoot = objectMapper.readTree(sourceJson);
        JsonNode targetRoot = objectMapper.readTree(targetJson);

        Map<String, String> sourceFlatMap = new HashMap<>();
        Map<String, String> targetFlatMap = new HashMap<>();

        flattenJson(sourceRoot, "", sourceFlatMap);
        flattenJson(targetRoot, "", targetFlatMap);

        return compareFlattenedMaps(sourceFlatMap, targetFlatMap);
    }

    /**
     * 将嵌套JSON扁平化为key-value映射，保持层级关系
     *
     * @param node      JSON节点
     * @param prefix    键前缀
     * @param resultMap 结果映射
     */
    private static void flattenJson(JsonNode node, String prefix, Map<String, String> resultMap) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flattenJson(entry.getValue(), key, resultMap);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String key = prefix + "[" + i + "]";
                flattenJson(node.get(i), key, resultMap);
            }
        } else {
            // 叶子节点，存储值
            resultMap.put(prefix, node.asText());
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
