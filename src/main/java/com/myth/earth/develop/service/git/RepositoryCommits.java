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
 * Git 仓库的提交信息聚合
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class RepositoryCommits {

    /**
     * 仓库信息
     */
    private GitRepository repository;

    /**
     * 该仓库的提交列表
     */
    private List<CommitLog> commits;

    public RepositoryCommits() {
        this.commits = new ArrayList<>();
    }

    public RepositoryCommits(GitRepository repository) {
        this.repository = repository;
        this.commits = new ArrayList<>();
    }

    public GitRepository getRepository() {
        return repository;
    }

    public void setRepository(GitRepository repository) {
        this.repository = repository;
    }

    public List<CommitLog> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitLog> commits) {
        this.commits = commits;
    }

    public void addCommit(CommitLog commit) {
        this.commits.add(commit);
    }

    public int getCommitCount() {
        return commits.size();
    }

    public int getTotalAdditions() {
        return commits.stream().mapToInt(CommitLog::getAdditions).sum();
    }

    public int getTotalDeletions() {
        return commits.stream().mapToInt(CommitLog::getDeletions).sum();
    }

    public int getTotalFilesChanged() {
        return commits.stream().mapToInt(CommitLog::getFilesChanged).sum();
    }
}
