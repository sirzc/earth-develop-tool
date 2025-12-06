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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Properties yaml互转
 *
 * @author zhouchao
 * @date 2025-12-06 下午3:30
 */
public class PropertiesYamlUtil {

    /**
     * 将Properties格式字符串转换为YAML格式
     *
     * @param propertiesContent Properties文件内容
     * @return YAML格式字符串
     */
    public static String convertPropertiesToYaml(String propertiesContent) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(propertiesContent));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
        return convertPropertiesToYaml(properties);
    }

    /**
     * 将Properties对象转换为YAML格式
     *
     * @param properties Properties对象
     * @return YAML格式字符串
     */
    public static String convertPropertiesToYaml(Properties properties) {
        Map<String, Object> yamlMap = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // 将属性键值对转换为嵌套Map结构
            setNestedValue(yamlMap, key, value);
        }

        // 配置YAML输出格式
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        return yaml.dump(yamlMap);
    }

    /**
     * 将YAML格式字符串转换为Properties格式
     *
     * @param yamlContent YAML文件内容
     * @return Properties格式字符串
     */
    public static String convertYamlToProperties(String yamlContent) {
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(yamlContent);

        Properties properties = new Properties();
        flattenMapToProperties(properties, yamlMap, "");

        return properties.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("\n"));
    }

    private String standardOutput(Properties properties) {
        // 将Properties转换为字符串（标准输出方式）
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to properties", e);
        }

        // 移除时间戳注释行并返回结果
        String result = writer.toString();
        return result.substring(result.indexOf('\n') + 1); // 跳过第一行的时间戳注释
    }

    /**
     * 将YAML Map转换为扁平的Properties
     */
    @SuppressWarnings("unchecked")
    private static void flattenMapToProperties(Properties properties, Map<String, Object> map, String prefix) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                // 递归处理嵌套Map
                flattenMapToProperties(properties, (Map<String, Object>) value, fullKey);
            } else if (value instanceof List) {
                // 处理列表，转换为[value1,value2,...]格式
                List<?> list = (List<?>) value;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(list.get(i).toString());
                }
                sb.append("]");
                properties.setProperty(fullKey, sb.toString());
            } else {
                // 普通值
                properties.setProperty(fullKey, value != null ? value.toString() : "");
            }
        }
    }

    /**
     * 将扁平的key转换为嵌套的Map结构
     * 例如: "server.port" -> {"server": {"port": value}}
     */
    @SuppressWarnings("unchecked")
    private static void setNestedValue(Map<String, Object> map, String key, String value) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!currentMap.containsKey(part)) {
                currentMap.put(part, new LinkedHashMap<String, Object>());
            }
            currentMap = (Map<String, Object>) currentMap.get(part);
        }

        // 处理值的类型转换
        currentMap.put(parts[parts.length - 1], convertValueType(value));
    }

    /**
     * 转换值的类型，尝试解析为适当的Java类型
     */
    private static Object convertValueType(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();

        // 布尔值
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        // 数字
        if (value.matches("-?\\d+")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // 继续尝试其他类型
            }
        }

        if (value.matches("-?\\d+\\.\\d+")) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // 继续尝试其他类型
            }
        }

        // 数组形式 [val1,val2,val3]
        if (value.startsWith("[") && value.endsWith("]")) {
            String arrayContent = value.substring(1, value.length() - 1);
            if (arrayContent.isEmpty()) {
                return new ArrayList<>();
            }
            String[] items = arrayContent.split(",");
            List<Object> list = new ArrayList<>();
            for (String item : items) {
                list.add(convertValueType(item.trim()));
            }
            return list;
        }

        return value;
    }

    /**
     * 将Properties文件转换为YAML文件
     */
    public static void convertPropertiesFileToYamlFile(String propertiesFilePath, String yamlFilePath) throws IOException {
        // 读取properties文件
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
        }

        // 转换为YAML
        String yamlContent = convertPropertiesToYaml(properties);

        // 写入YAML文件
        try (FileWriter writer = new FileWriter(yamlFilePath)) {
            writer.write(yamlContent);
        }
    }

    /**
     * 将YAML文件转换为Properties文件
     */
    public static void convertYamlFileToPropertiesFile(String yamlFilePath, String propertiesFilePath) throws IOException {
        // 读取YAML文件
        String yamlContent;
        try (BufferedReader reader = new BufferedReader(new FileReader(yamlFilePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            yamlContent = sb.toString();
        }

        // 转换为Properties
        String propertiesContent = convertYamlToProperties(yamlContent);

        // 写入Properties文件
        try (FileWriter writer = new FileWriter(propertiesFilePath)) {
            writer.write(propertiesContent);
        }
    }
}
