package com.myth.earth.develop.utils;

import com.google.common.collect.Maps;
import com.myth.earth.develop.model.CompareResult;
import com.myth.earth.develop.model.DifferenceResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * properties比较工具类
 *
 * @author zhouchao
 * @date 2024-01-02 15:59
 */
public class PropertiesCompareUtil {

    public static final String FILE_TYPE = "properties";

    public static final String IGNORE_FLAG = "#";

    public static final String SPLITTER = "=";

    /**
     * 解析每一行properties的内容
     *
     * @param properties 配置内容
     * @return 解析结果
     */
    @NotNull
    public static Map<String, String> parse(@Nullable List<String> properties) {
        if (properties == null || properties.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = Maps.newHashMapWithExpectedSize(properties.size());
        for (String property : properties) {
            if (StringUtils.isBlank(property) || property.startsWith(IGNORE_FLAG)) {
                continue;
            }

            int index = StringUtils.indexOf(property, SPLITTER);
            if (index < 0) {
                continue;
            }

            String key = StringUtils.substring(property, 0, index);
            String value = StringUtils.substring(property, index + 1);
            result.put(StringUtils.trim(key), StringUtils.trim(value));
        }
        return result;
    }

    /**
     * 比较两个map之间的差异
     *
     * @param source 源
     * @param target 目标
     * @return 差异信息
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static CompareResult compare(@NotNull Map<String, String> source, @NotNull Map<String, String> target) {
        CompareResult result = new CompareResult();
        Collection<String> sourceKeys = CollectionUtils.removeAll(source.keySet(), target.keySet());
        // 只有source中有的
        result.setSourceKeys(sourceKeys);
        // 只有target中有的
        Collection<String> targetKeys = CollectionUtils.removeAll(target.keySet(), source.keySet());
        result.setTargetKeys(targetKeys);
        // 差异的key
        Collection<String> intersection = CollectionUtils.intersection(source.keySet(), target.keySet());
        List<DifferenceResult> differenceResults = new ArrayList<>(intersection.size());
        for (String key : intersection) {
            if (StringUtils.equals(source.get(key), target.get(key))) {
                continue;
            }
            String s1 = UnicodeUtil.convertUnicodeToChinese(source.get(key));
            String s2 = UnicodeUtil.convertUnicodeToChinese(target.get(key));
            differenceResults.add(new DifferenceResult(key, s1, s2));
        }
        result.setDifferenceResults(differenceResults);
        return result;
    }

    @NotNull
    public static CompareResult parseAndCompare(@NotNull String sourcePath, @NotNull String targetPath) throws IOException {
        Map<String, String> sourceMap = parse(FileUtils.readLines(new File(sourcePath), StandardCharsets.UTF_8));
        Map<String, String> targetMap = parse(FileUtils.readLines(new File(targetPath), StandardCharsets.UTF_8));
        return compare(sourceMap, targetMap);
    }

    @NotNull
    public static CompareResult parseAndCompare(@NotNull List<String> source, @NotNull List<String> target){
        Map<String, String> sourceMap = parse(source);
        Map<String, String> targetMap = parse(target);
        return compare(sourceMap, targetMap);
    }
}
