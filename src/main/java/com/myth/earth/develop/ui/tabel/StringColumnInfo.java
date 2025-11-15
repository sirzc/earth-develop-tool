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

package com.myth.earth.develop.ui.tabel;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * String 类型列
 *
 * @author zhouchao
 * @date 2025-11-14 上午9:29
 */
public class StringColumnInfo<T> extends ColumnInfo<T, String> {

    private final Function<T, String> function;

    public StringColumnInfo(String name, Function<T, String> function) {
        super(name);
        this.function = function;
    }

    @Override
    public @Nullable String valueOf(T o) {
        return function.apply(o);
    }
}
