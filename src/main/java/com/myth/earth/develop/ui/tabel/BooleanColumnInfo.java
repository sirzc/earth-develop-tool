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

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Boolean 类型
 *
 * @author zhouchao
 * @date 2025-11-14 上午9:53
 */
public class BooleanColumnInfo<T> extends ColumnInfo<T, Boolean> {

    private final Function<T, Boolean>   function;
    private final BiConsumer<T, Boolean> biConsumer;

    public BooleanColumnInfo(String name, Function<T, Boolean> function, BiConsumer<T, Boolean> biConsumer) {
        super(name);
        this.function = function;
        this.biConsumer = biConsumer;
    }

    @Override
    public @Nullable TableCellEditor getEditor(T t) {
        return new DefaultCellEditor(new JCheckBox());
    }

    @Override
    public final Class<Boolean> getColumnClass() {
        return Boolean.class;
    }

    @Override
    public final boolean isCellEditable(T item) {
        return true;
    }

    @Override
    public final void setValue(final T o, final Boolean aValue) {
        biConsumer.accept(o, aValue);
    }

    @Override
    public int getWidth(JTable table) {
        return 40;
    }

    @Override
    public @Nullable Boolean valueOf(T t) {
        return function.apply(t);
    }
}
