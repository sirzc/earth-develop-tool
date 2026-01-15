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

/**
 * 提交周报的统计信息
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class ReportStatistics {

    /**
     * 扫描的总仓库数
     */
    private int totalRepositories;

    /**
     * 有提交的仓库数
     */
    private int repositoriesWithCommits;

    /**
     * 总提交数
     */
    private int totalCommits;

    /**
     * 总修改文件数
     */
    private int totalFilesChanged;

    /**
     * 总增加行数
     */
    private int totalAdditions;

    /**
     * 总删除行数
     */
    private int totalDeletions;

    public ReportStatistics() {
    }

    public ReportStatistics(int totalRepositories, int repositoriesWithCommits,
                           int totalCommits, int totalFilesChanged,
                           int totalAdditions, int totalDeletions) {
        this.totalRepositories = totalRepositories;
        this.repositoriesWithCommits = repositoriesWithCommits;
        this.totalCommits = totalCommits;
        this.totalFilesChanged = totalFilesChanged;
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
    }

    public int getTotalRepositories() {
        return totalRepositories;
    }

    public void setTotalRepositories(int totalRepositories) {
        this.totalRepositories = totalRepositories;
    }

    public int getRepositoriesWithCommits() {
        return repositoriesWithCommits;
    }

    public void setRepositoriesWithCommits(int repositoriesWithCommits) {
        this.repositoriesWithCommits = repositoriesWithCommits;
    }

    public int getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(int totalCommits) {
        this.totalCommits = totalCommits;
    }

    public int getTotalFilesChanged() {
        return totalFilesChanged;
    }

    public void setTotalFilesChanged(int totalFilesChanged) {
        this.totalFilesChanged = totalFilesChanged;
    }

    public int getTotalAdditions() {
        return totalAdditions;
    }

    public void setTotalAdditions(int totalAdditions) {
        this.totalAdditions = totalAdditions;
    }

    public int getTotalDeletions() {
        return totalDeletions;
    }

    public void setTotalDeletions(int totalDeletions) {
        this.totalDeletions = totalDeletions;
    }
}
