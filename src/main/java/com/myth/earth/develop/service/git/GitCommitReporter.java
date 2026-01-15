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

import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Git 提交周报生成器 - 用于生成和导出提交周报
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitCommitReporter {

    /**
     * 生成提交周报
     *
     * @param repositories 仓库列表
     * @param author       作者名
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param executor     Git 命令执行器
     * @return 生成的报告
     * @throws GitException 如果执行失败
     */
    public CommitReport generateReport(List<GitRepository> repositories, String author,
                                       LocalDate startDate, LocalDate endDate,
                                       GitCommandExecutor executor) throws GitException {
        CommitReport report = new CommitReport(author, startDate, endDate);

        if (repositories == null || repositories.isEmpty()) {
            return report;
        }

        // 遍历所有仓库收集提交信息
        for (GitRepository repo : repositories) {
            try {
                // 切换到该仓库
                executor.setWorkingDirectory(repo.getPath());

                // 获取 HEAD 分支（如果无法获取则使用 "master" 作为备选）
                String branch = "HEAD";
                try {
                    String currentBranch = executor.getCurrentBranch();
                    if (currentBranch != null) {
                        branch = currentBranch;
                    }
                } catch (GitException e) {
                    // 使用默认分支
                }

                // 获取该仓库的提交日志
                List<CommitLog> logs = executor.getCommitLogs(branch, author, startDate, endDate);

                if (!logs.isEmpty()) {
                    RepositoryCommits repoCommits = new RepositoryCommits(repo);
                    repoCommits.setCommits(logs);
                    report.addRepositoryCommits(repoCommits);
                }
            } catch (GitException e) {
                // 记录错误但继续处理其他仓库
                System.err.println("警告：无法处理仓库 " + repo.getName() + ": " + e.getMessage());
            }
        }

        // 重新计算统计信息
        report.recalculateStatistics(repositories.size());

        return report;
    }

    /**
     * 将报告导出为 Markdown 格式
     *
     * @param report 报告对象
     * @return Markdown 格式的字符串
     */
    public String exportAsMarkdown(CommitReport report) {
        StringBuilder sb = new StringBuilder();

        // 标题
        sb.append("# Git 提交周报\n\n");

        // 报告信息
        sb.append("**作者:** ").append(report.getAuthor()).append("\n");
        if (report.getStartDate() != null && report.getEndDate() != null) {
            sb.append("**时间范围:** ").append(report.getStartDate()).append(" ~ ")
                    .append(report.getEndDate()).append("\n");
        } else if (report.getEndDate() != null) {
            sb.append("**截止日期:** ").append(report.getEndDate()).append("\n");
        }
        sb.append("\n");

        // 按仓库展示提交
        List<RepositoryCommits> repositories = report.getRepositories();
        if (repositories.isEmpty()) {
            sb.append("在选定时间范围内无提交记录。\n");
        } else {
            for (RepositoryCommits repoCommits : repositories) {
                GitRepository repo = repoCommits.getRepository();
                List<CommitLog> commits = repoCommits.getCommits();

                sb.append("## 仓库: ").append(repo.getName()).append("\n");
                if (commits.isEmpty()) {
                    sb.append("该仓库在此时间范围内无提交。\n");
                } else {
                    for (CommitLog log : commits) {
                        sb.append("- `").append(log.getHash()).append("` - ")
                                .append(log.getDate()).append(" : ")
                                .append(log.getMessage()).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        // 统计总结
        ReportStatistics stats = report.getStatistics();
        sb.append("---\n\n");
        sb.append("**统计总结:**\n");
        sb.append("- 涉及仓库: ").append(stats.getRepositoriesWithCommits())
                .append(" / ").append(stats.getTotalRepositories()).append("\n");
        sb.append("- 总提交数: ").append(stats.getTotalCommits()).append("\n");
        if (stats.getTotalFilesChanged() > 0) {
            sb.append("- 修改文件数: ").append(stats.getTotalFilesChanged()).append("\n");
        }
        if (stats.getTotalAdditions() > 0 || stats.getTotalDeletions() > 0) {
            sb.append("- 代码增加: +").append(stats.getTotalAdditions())
                    .append(" 删除: -").append(stats.getTotalDeletions()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 将报告导出为纯文本格式
     *
     * @param report 报告对象
     * @return 纯文本格式的字符串
     */
    public String exportAsPlainText(CommitReport report) {
        StringBuilder sb = new StringBuilder();

        // 标题
        sb.append("Git 提交周报\n");
        sb.append(StrUtil.repeat("=", 50)).append("\n\n");

        // 报告信息
        sb.append("作者: ").append(report.getAuthor()).append("\n");
        if (report.getStartDate() != null && report.getEndDate() != null) {
            sb.append("时间范围: ").append(report.getStartDate()).append(" ~ ")
                    .append(report.getEndDate()).append("\n");
        } else if (report.getEndDate() != null) {
            sb.append("截止日期: ").append(report.getEndDate()).append("\n");
        }
        sb.append("\n");

        // 按仓库展示提交
        List<RepositoryCommits> repositories = report.getRepositories();
        if (repositories.isEmpty()) {
            sb.append("在选定时间范围内无提交记录。\n");
        } else {
            for (RepositoryCommits repoCommits : repositories) {
                GitRepository repo = repoCommits.getRepository();
                List<CommitLog> commits = repoCommits.getCommits();

                sb.append("仓库: ").append(repo.getName()).append("\n");
                sb.append(StrUtil.repeat("-", 50)).append("\n");
                if (commits.isEmpty()) {
                    sb.append("该仓库在此时间范围内无提交。\n");
                } else {
                    for (CommitLog log : commits) {
                        sb.append(log.getDate()).append(" ")
                                .append(log.getHash()).append(" ")
                                .append(log.getMessage()).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        // 统计总结
        ReportStatistics stats = report.getStatistics();
        sb.append(StrUtil.repeat("=", 50)).append("\n");
        sb.append("统计总结:\n");
        sb.append("  涉及仓库: ").append(stats.getRepositoriesWithCommits())
                .append(" / ").append(stats.getTotalRepositories()).append("\n");
        sb.append("  总提交数: ").append(stats.getTotalCommits()).append("\n");
        if (stats.getTotalFilesChanged() > 0) {
            sb.append("  修改文件数: ").append(stats.getTotalFilesChanged()).append("\n");
        }
        if (stats.getTotalAdditions() > 0 || stats.getTotalDeletions() > 0) {
            sb.append("  代码增加: +").append(stats.getTotalAdditions())
                    .append(" 删除: -").append(stats.getTotalDeletions()).append("\n");
        }

        return sb.toString();
    }
}
