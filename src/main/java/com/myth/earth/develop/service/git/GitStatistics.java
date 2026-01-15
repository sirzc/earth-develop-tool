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
 * Git 代码统计结果模型
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitStatistics {

    /** 作者名称 */
    private String author;

    /** 提交次数 */
    private int commitCount;

    /** 新增行数 */
    private int linesAdded;

    /** 删除行数 */
    private int linesRemoved;

    /** 修改文件数 */
    private int filesModified;

    public GitStatistics() {
    }

    public GitStatistics(String author) {
        this.author = author;
    }

    public GitStatistics(String author, int commitCount, int linesAdded, int linesRemoved, int filesModified) {
        this.author = author;
        this.commitCount = commitCount;
        this.linesAdded = linesAdded;
        this.linesRemoved = linesRemoved;
        this.filesModified = filesModified;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public void setLinesRemoved(int linesRemoved) {
        this.linesRemoved = linesRemoved;
    }

    public int getFilesModified() {
        return filesModified;
    }

    public void setFilesModified(int filesModified) {
        this.filesModified = filesModified;
    }

}
