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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Git 提交周报 - 聚合报告
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class CommitReport {

    /**
     * 报告对应的作者
     */
    private String author;

    /**
     * 报告的起始日期
     */
    private LocalDate startDate;

    /**
     * 报告的终止日期
     */
    private LocalDate endDate;

    /**
     * 各仓库的提交信息列表
     */
    private List<RepositoryCommits> repositories;

    /**
     * 统计信息
     */
    private ReportStatistics statistics;

    public CommitReport() {
        this.repositories = new ArrayList<>();
        this.statistics = new ReportStatistics();
    }

    public CommitReport(String author, LocalDate startDate, LocalDate endDate) {
        this.author = author;
        this.startDate = startDate;
        this.endDate = endDate;
        this.repositories = new ArrayList<>();
        this.statistics = new ReportStatistics();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<RepositoryCommits> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryCommits> repositories) {
        this.repositories = repositories;
    }

    public void addRepositoryCommits(RepositoryCommits repositoryCommits) {
        this.repositories.add(repositoryCommits);
    }

    public ReportStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(ReportStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 重新计算统计信息
     */
    public void recalculateStatistics(int totalRepositories) {
        int repositoriesWithCommits = (int) repositories.stream()
                .filter(rc -> rc.getCommitCount() > 0)
                .count();
        int totalCommits = (int) repositories.stream()
                .mapToInt(RepositoryCommits::getCommitCount)
                .sum();
        int totalFilesChanged = (int) repositories.stream()
                .mapToInt(RepositoryCommits::getTotalFilesChanged)
                .sum();
        int totalAdditions = (int) repositories.stream()
                .mapToInt(RepositoryCommits::getTotalAdditions)
                .sum();
        int totalDeletions = (int) repositories.stream()
                .mapToInt(RepositoryCommits::getTotalDeletions)
                .sum();

        statistics.setTotalRepositories(totalRepositories);
        statistics.setRepositoriesWithCommits(repositoriesWithCommits);
        statistics.setTotalCommits(totalCommits);
        statistics.setTotalFilesChanged(totalFilesChanged);
        statistics.setTotalAdditions(totalAdditions);
        statistics.setTotalDeletions(totalDeletions);
    }
}
