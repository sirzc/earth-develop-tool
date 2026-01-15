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

import java.io.File;

/**
 * Git 仓库数据模型
 *
 * @author zhouchao
 * @date 2025-01-15
 */
public class GitRepository {

    /**
     * 仓库名称（目录名或相对路径）
     */
    private String name;

    /**
     * 仓库绝对路径
     */
    private File path;

    /**
     * 相对于项目根目录的路径
     */
    private String relativePath;

    /**
     * 当前 HEAD 分支名称
     */
    private String currentBranch;

    /**
     * 是否为项目主仓库（项目根目录的 .git）
     */
    private boolean isMainRepository;

    public GitRepository() {
    }

    public GitRepository(String name, File path, String relativePath) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
    }

    public GitRepository(String name, File path, String relativePath, boolean isMainRepository) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
        this.isMainRepository = isMainRepository;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(String currentBranch) {
        this.currentBranch = currentBranch;
    }

    public boolean isMainRepository() {
        return isMainRepository;
    }

    public void setMainRepository(boolean mainRepository) {
        isMainRepository = mainRepository;
    }

    @Override
    public String toString() {
        if (isMainRepository) {
            return name + " (主仓库)";
        }
        return name;
    }
}
