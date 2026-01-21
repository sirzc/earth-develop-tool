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

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Git 仓库查找器 - 扫描项目内的所有 Git 仓库
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitRepositoryFinder {

    private File currentWorkingDir;
    private final File projectRoot;


    private static final int DEFAULT_MAX_DEPTH = 1;
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * 默认排除的目录
     */
    private static final Set<String> EXCLUDE_DIRS = new HashSet<>(Arrays.asList(
            "node_modules", "bower_components",
            "target", "build", "dist", "out",
            ".gradle", ".maven", ".m2",
            "venv", ".venv", "env",
            ".idea", ".vscode", ".DS_Store", "src", "logs", "log", "tmp", "temp",
            "claude", ".claude", "openspec", ".openspec", "docs", "sql", "doc", "gradle",
            ".git"  // 避免进入 .git 目录本身
    ));


    public GitRepositoryFinder(File projectRoot) {
        this.projectRoot = projectRoot;
        this.currentWorkingDir = projectRoot;
    }

    /**
     * 扫描项目内所有 Git 仓库
     * @return Git 仓库列表
     */
    @NotNull
    public List<GitRepository> findRepositories() {
        return findRepositories(DEFAULT_MAX_DEPTH);
    }

    /**
     * 扫描项目内所有 Git 仓库
     *
     * @param maxDepth    最大扫描深度
     * @return Git 仓库列表
     */
    @NotNull
    public List<GitRepository> findRepositories(int maxDepth) {
        List<GitRepository> repositories = new ArrayList<>();

        if (!projectRoot.exists() || !projectRoot.isDirectory()) {
            return repositories;
        }

        // 首先检查项目根目录本身是否为 Git 仓库
        if (isGitRepository(projectRoot)) {
            GitRepository mainRepo = new GitRepository(
                    projectRoot.getName(),
                    projectRoot,
                    ".",
                    true
            );
            try {
                mainRepo.setCurrentBranch(getCurrentBranch(projectRoot));
            } catch (Exception e) {
                // 忽略异常，使用 null 作为当前分支
            }
            repositories.add(mainRepo);
        }

        // 递归扫描子目录
        scanDirectory(projectRoot, "", repositories, maxDepth, 0);

        return repositories;
    }

    /**
     * 递归扫描目录，查找 Git 仓库
     */
    private void scanDirectory(File currentDir, String relativePath,
                              List<GitRepository> repositories, int maxDepth, int currentDepth) {
        // 达到最大深度，停止扫描
        if (currentDepth >= maxDepth) {
            return;
        }

        File[] files = currentDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            String dirName = file.getName();

            // 跳过排除的目录
            if (EXCLUDE_DIRS.contains(dirName) || dirName.startsWith(".")) {
                continue;
            }

            String newRelativePath = relativePath.isEmpty() ? dirName : relativePath + "/" + dirName;

            // 检查是否为 Git 仓库
            if (isGitRepository(file)) {
                GitRepository repo = new GitRepository(
                        dirName,
                        file,
                        newRelativePath,
                        false
                );
                try {
                    repo.setCurrentBranch(getCurrentBranch(file));
                } catch (Exception e) {
                    // 忽略异常，使用 null 作为当前分支
                }
                repositories.add(repo);
            }

            // 继续递归扫描子目录
            scanDirectory(file, newRelativePath, repositories, maxDepth, currentDepth + 1);
        }
    }

    /**
     * 判断目录是否为 Git 仓库
     *
     * @param dir 目录
     * @return true 如果是 Git 仓库，否则 false
     */
    private boolean isGitRepository(File dir) {
        try {
            String output = executeGitCommand(dir, "git", "rev-parse", "--git-dir");
            return !output.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取仓库的当前 HEAD 分支名称
     *
     * @param repoDir 仓库目录
     * @return 分支名称，或 null 如果无法获取
     */
    public String getCurrentBranch(File repoDir) {
        try {
            // 首先尝试使用 symbolic-ref
            String output = executeGitCommand(repoDir, "git", "symbolic-ref", "--short", "HEAD");
            if (output != null && !output.trim().isEmpty()) {
                return output.trim();
            }
        } catch (Exception e) {
            // 继续尝试下一种方法
        }

        try {
            // 备选方案：使用 rev-parse
            String output = executeGitCommand(repoDir, "git", "rev-parse", "--abbrev-ref", "HEAD");
            if (output != null && !output.trim().isEmpty()) {
                return output.trim();
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return null;
    }

    /**
     * 执行 Git 命令并返回输出
     *
     * @param workDir  工作目录
     * @param command  命令及参数
     * @return 命令输出
     * @throws Exception 如果执行失败
     */
    private String executeGitCommand(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new GitException("Git 命令执行超时");
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

        return output.toString().trim();
    }
}
