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

package com.myth.earth.develop.service.git;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Git 统计数据解析器
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitStatisticsParser {

    /**
     * 解析 git log --numstat 的输出，计算统计数据
     * 格式：
     * 作者名
     * 增加行数  删除行数  文件名
     * 增加行数  删除行数  文件名
     *
     * @param output       git log 输出
     * @param statistics   统计结果集合（会被填充）
     */
    public static void parseStatistics(String output, Map<String, GitStatistics> statistics) {
        String currentAuthor = null;
        Set<String> modifiedFiles = new HashSet<>();

        String[] lines = output.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // 检查是否是作者行（不包含制表符）
            if (!line.contains("\t")) {
                // 新作者行，保存上一个作者的文件统计
                if (currentAuthor != null && !modifiedFiles.isEmpty()) {
                    GitStatistics stats = statistics.get(currentAuthor);
                    if (stats != null) {
                        stats.setFilesModified(stats.getFilesModified() + modifiedFiles.size());
                    }
                    modifiedFiles.clear();
                }
                currentAuthor = line;
                continue;
            }

            // 这是一个 numstat 行
            if (currentAuthor == null) {
                continue;
            }

            String[] parts = line.split("\t");
            if (parts.length >= 3) {
                try {
                    int added = parseNumber(parts[0]);
                    int removed = parseNumber(parts[1]);
                    String fileName = parts[2];

                    GitStatistics stats = statistics.get(currentAuthor);
                    if (stats == null) {
                        stats = new GitStatistics(currentAuthor);
                        statistics.put(currentAuthor, stats);
                    }
                    stats.setLinesAdded(stats.getLinesAdded() + added);
                    stats.setLinesRemoved(stats.getLinesRemoved() + removed);
                    stats.setCommitCount(stats.getCommitCount() + 1);
                    modifiedFiles.add(fileName);
                } catch (NumberFormatException e) {
                    // 忽略无法解析的行
                }
            }
        }

        // 处理最后一个作者的文件统计
        if (currentAuthor != null && !modifiedFiles.isEmpty()) {
            GitStatistics stats = statistics.get(currentAuthor);
            if (stats != null) {
                stats.setFilesModified(stats.getFilesModified() + modifiedFiles.size());
            }
        }

        // 修正提交次数统计（因为上面是按行来计算的）
        recalculateCommitCount(output, statistics);
    }

    /**
     * 解析数字，处理 "-" 和其他无法解析的值
     *
     * @param str 待解析的字符串
     * @return 解析结果，无法解析返回 0
     */
    private static int parseNumber(String str) {
        if ("-".equals(str)) {
            return 0;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 重新计算每个作者的提交次数
     * 因为 --format=%an --numstat 会为每个文件输出一行，而不是每个提交输出一行
     * 我们需要通过另一种方式来计算真实的提交次数
     *
     * @param output       原始 git log 输出
     * @param statistics   统计结果（会被修改）
     */
    private static void recalculateCommitCount(String output, Map<String, GitStatistics> statistics) {
        // 因为 git log --format=%an --numstat 的输出格式是：
        // 对于每个提交，先输出作者名，然后输出所有修改的文件及其行数
        // 我们需要计算有多少行只包含作者名（不含制表符）
        String currentAuthor = null;
        String[] lines = output.split("\n");

        // 清空之前的提交次数
        for (GitStatistics stats : statistics.values()) {
            stats.setCommitCount(0);
        }

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (!line.contains("\t")) {
                // 这是作者行，说明新提交开始
                currentAuthor = line;
                if (statistics.containsKey(currentAuthor)) {
                    GitStatistics stats = statistics.get(currentAuthor);
                    stats.setCommitCount(stats.getCommitCount() + 1);
                }
            }
        }
    }

}
