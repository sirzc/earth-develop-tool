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

package com.myth.earth.develop.utils;

/**
 * 构建路径key
 *
 * @author zhouchao
 * @date 2025-09-30 下午5:32
 */
public class KeyPathBuilder {
    private final String currentPath;
    private static final String SEPARATOR = ".";

    public KeyPathBuilder() {
        this.currentPath = "";
    }

    private KeyPathBuilder(String path) {
        this.currentPath = path;
    }

    public KeyPathBuilder append(String key) {
        if (currentPath.isEmpty()) {
            return new KeyPathBuilder(key);
        } else {
            return new KeyPathBuilder(currentPath + SEPARATOR + key);
        }
    }

    @Override
    public String toString() {
        return currentPath;
    }
}
