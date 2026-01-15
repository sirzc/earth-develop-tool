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

/**
 * Git 提交日志数据模型 - 表示单条提交信息
 *
 * @author IngerChao
 * @date 2025-01-15
 */
public class CommitLog {

    /**
     * 提交哈希（短哈希，如 abc1234）
     */
    private String hash;

    /**
     * 提交作者
     */
    private String author;

    /**
     * 提交日期
     */
    private LocalDate date;

    /**
     * 提交消息（第一行）
     */
    private String message;

    /**
     * 修改的文件数（可选）
     */
    private int filesChanged;

    /**
     * 增加的代码行数（可选）
     */
    private int additions;

    /**
     * 删除的代码行数（可选）
     */
    private int deletions;

    public CommitLog() {
    }

    public CommitLog(String hash, String author, LocalDate date, String message) {
        this.hash = hash;
        this.author = author;
        this.date = date;
        this.message = message;
    }

    public CommitLog(String hash, String author, LocalDate date, String message,
                     int filesChanged, int additions, int deletions) {
        this.hash = hash;
        this.author = author;
        this.date = date;
        this.message = message;
        this.filesChanged = filesChanged;
        this.additions = additions;
        this.deletions = deletions;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFilesChanged() {
        return filesChanged;
    }

    public void setFilesChanged(int filesChanged) {
        this.filesChanged = filesChanged;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    @Override
    public String toString() {
        return "CommitLog{" +
                "hash='" + hash + '\'' +
                ", author='" + author + '\'' +
                ", date=" + date +
                ", message='" + message + '\'' +
                ", filesChanged=" + filesChanged +
                ", additions=" + additions +
                ", deletions=" + deletions +
                '}';
    }
}
