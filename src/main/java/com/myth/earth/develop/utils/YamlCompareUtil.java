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
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * yaml文件比较工具
 *
 * @author zhouchao
 * @date 2025-09-30 下午5:32
 */
public class YamlCompareUtil {

    public static final String FILE_TYPE       = "yaml";
    public static final String SHORT_FILE_TYPE = "yml";

    public static Map<String, Object> parseYaml(String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 遍历Map结构，生成扁平化的key-value映射表
    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Map<String, String> flatMap = new LinkedHashMap<>();
        buildFlatMap(map, new KeyPathBuilder(), flatMap);
        return flatMap;
    }

    @SuppressWarnings("unchecked")
    private static void buildFlatMap(Object obj, KeyPathBuilder parentPath, Map<String, String> result) {
        if (obj instanceof Map) {
            ((Map<String, Object>) obj).forEach((k, v) -> {
                KeyPathBuilder currentPath = parentPath.append(k);
                if (v instanceof Map || v instanceof List) {
                    buildFlatMap(v, currentPath, result);
                } else {
                    result.put(currentPath.toString(), Objects.toString(v));
                }
            });
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            for (int i = 0; i < list.size(); i++) {
                KeyPathBuilder indexPath = parentPath.append("[" + i + "]");
                buildFlatMap(list.get(i), indexPath, result);
            }
        } else {
            result.put(parentPath.toString(), Objects.toString(obj));
        }
    }

    // 主要比较逻辑
    public static CompareResult compareYaml(Map<String, String> sourceMap, Map<String, String> targetMap) {
        Set<String> sourceKeys = new HashSet<>(sourceMap.keySet());
        Set<String> targetKeys = new HashSet<>(targetMap.keySet());

        Collection<String> onlyInSource = new ArrayList<>();
        Collection<String> onlyInTarget = new ArrayList<>();
        Collection<DifferenceResult> differences = new ArrayList<>();

        for (String key : sourceKeys) {
            if (!targetKeys.contains(key)) {
                onlyInSource.add(key);
            } else {
                String sourceVal = sourceMap.get(key);
                String targetVal = targetMap.get(key);
                if (!Objects.equals(sourceVal, targetVal)) {
                    differences.add(new DifferenceResult(key, sourceVal, targetVal));
                }
            }
        }

        for (String key : targetKeys) {
            if (!sourceKeys.contains(key)) {
                onlyInTarget.add(key);
            }
        }

        CompareResult compareResult = new CompareResult();
        compareResult.setSourceKeys(onlyInSource);
        compareResult.setTargetKeys(onlyInTarget);
        compareResult.setDifferenceResults(differences);
        return compareResult;
    }

    public static CompareResult parseAndCompare(@NotNull String sourcePath, @NotNull String targetPath) {
        Map<String, Object> sourceMap = parseYaml(sourcePath);
        Map<String, Object> targetMap = parseYaml(targetPath);
        return compareYaml(flattenMap(sourceMap), flattenMap(targetMap));
    }
}
