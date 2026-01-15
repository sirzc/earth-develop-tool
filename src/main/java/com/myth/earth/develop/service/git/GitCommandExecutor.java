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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Git 命令执行器
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitCommandExecutor {

    private final File projectRoot;
    private static final int TIMEOUT_SECONDS = 30;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public GitCommandExecutor(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    /**
     * 获取分支列表（本地+远程）
     *
     * @return 分支列表
     * @throws GitException 如果执行失败
     */
    public List<String> getBranches() throws GitException {
        List<String> branches = new ArrayList<>();
        try {
            // 获取本地分支
            String output = executeGitCommand("git", "branch", "--list");
            for (String line : output.split("\n")) {
                String branch = line.trim();
                if (!branch.isEmpty()) {
                    // 移除 * 标记
                    branch = branch.replaceFirst("^\\*\\s+", "");
                    branches.add(branch);
                }
            }

            // 获取远程分支
            output = executeGitCommand("git", "branch", "-r");
            for (String line : output.split("\n")) {
                String branch = line.trim();
                if (!branch.isEmpty() && !branch.contains("->")) {
                    // 移除 origin/ 前缀，避免重复
                    if (!branch.startsWith("HEAD")) {
                        branches.add(branch);
                    }
                }
            }

            return branches;
        } catch (Exception e) {
            throw new GitException("无法获取分支列表: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定时间范围、分支的作者列表
     *
     * @param branch    分支名称
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 作者列表
     * @throws GitException 如果执行失败
     */
    public List<String> getAuthors(String branch, Date startDate, Date endDate) throws GitException {
        Set<String> authors = new HashSet<>();
        try {
            StringBuilder cmd = new StringBuilder("git log ");
            cmd.append(branch);
            if (startDate != null) {
                cmd.append(" --after=").append(DATE_FORMAT.format(startDate));
            }
            if (endDate != null) {
                cmd.append(" --before=").append(DATE_FORMAT.format(endDate));
            }
            cmd.append(" --format=%an");

            String output = executeGitCommand("bash", "-c", cmd.toString());
            for (String line : output.split("\n")) {
                String author = line.trim();
                if (!author.isEmpty()) {
                    authors.add(author);
                }
            }

            return new ArrayList<>(authors);
        } catch (Exception e) {
            throw new GitException("无法获取作者列表: " + e.getMessage(), e);
        }
    }

    /**
     * 获取提交统计数据
     *
     * @param branch    分支名称
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param authors   作者列表（null 表示所有作者）
     * @return 按作者分组的统计数据
     * @throws GitException 如果执行失败
     */
    public Map<String, GitStatistics> getStatistics(String branch, Date startDate, Date endDate, List<String> authors)
            throws GitException {
        Map<String, GitStatistics> statistics = new HashMap<>();
        try {
            // 使用 git log --numstat 获取行数统计
            // 正确的格式：git log <branch> --numstat --pretty=format:%an <date filters> <author filters>
            List<String> cmd = new ArrayList<>();
            cmd.add("git");
            cmd.add("log");
            cmd.add(branch);
            cmd.add("--numstat");
            cmd.add("--pretty=format:%an");

            if (startDate != null) {
                cmd.add("--after=" + DATE_FORMAT.format(startDate));
            }
            if (endDate != null) {
                cmd.add("--before=" + DATE_FORMAT.format(endDate));
            }

            if (authors != null && !authors.isEmpty()) {
                for (String author : authors) {
                    cmd.add("--author=" + author);
                }
            }

            String output = executeGitCommandWithList(cmd);
            GitStatisticsParser.parseStatistics(output, statistics);

            return statistics;
        } catch (Exception e) {
            throw new GitException("无法获取统计数据: " + e.getMessage(), e);
        }
    }

    /**
     * 执行 Git 命令并返回输出
     *
     * @param command 命令及参数
     * @return 命令输出
     * @throws Exception 如果执行失败
     */
    private String executeGitCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectRoot);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new GitException("Git 命令执行超时（超过 " + TIMEOUT_SECONDS + " 秒）");
        }

        int exitCode = process.exitValue();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        if (exitCode != 0) {
            throw new GitException("Git 命令执行失败，exit code: " + exitCode);
        }

        return output.toString();
    }

    /**
     * 执行 Git 命令（参数列表形式）并返回输出
     *
     * @param commandList 命令及参数列表
     * @return 命令输出
     * @throws Exception 如果执行失败
     */
    private String executeGitCommandWithList(List<String> commandList) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(projectRoot);

        Process process = pb.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new GitException("Git 命令执行超时（超过 " + TIMEOUT_SECONDS + " 秒）");
        }

        int exitCode = process.exitValue();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        if (exitCode != 0) {
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            throw new GitException("Git 命令执行失败，exit code: " + exitCode + ", 错误信息: " + errorOutput.toString());
        }

        return output.toString();
    }
}
