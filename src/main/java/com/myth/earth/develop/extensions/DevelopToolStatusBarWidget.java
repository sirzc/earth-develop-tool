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

package com.myth.earth.develop.extensions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IconLikeCustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.components.JBLabel;
import com.myth.earth.develop.kit.IconKit;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 状态栏小部件具体实现
 *
 * @author zhouchao
 * @date 2025-07-15 下午7:27
 */
public class DevelopToolStatusBarWidget implements IconLikeCustomStatusBarWidget {
    private static final Logger  LOG = Logger.getInstance(DevelopToolStatusBarWidget.class);
    private static final String  ID  = "EarthDevelopTool.StatusBarWidgetImpl";
    private final        Project project;

    public DevelopToolStatusBarWidget(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public JComponent getComponent() {
        return new JBLabel(IconKit.TOOLKIT_16X16);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        // 展示时调用，暂时无用
    }

    @Override
    public void dispose() {
        // 隐藏时调用，暂时无用
    }
}
