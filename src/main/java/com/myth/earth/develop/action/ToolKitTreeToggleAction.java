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

package com.myth.earth.develop.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.myth.earth.develop.ui.toolkit.ToolkitGlobalState;
import com.myth.earth.develop.ui.toolkit.ToolkitProjectService;
import org.jetbrains.annotations.NotNull;

/**
 * 工具树菜单显示或隐藏
 *
 * @author zhouchao
 * @date 2025-11-15 下午9:31
 */
public class ToolKitTreeToggleAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        boolean hideToolTree = !hideToolTree();
        ToolkitGlobalState.getInstance().setHideToolTree(hideToolTree);
        ToolkitProjectService.getInstance(project).refreshToolKitTree();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        if (hideToolTree()) {
            presentation.setIcon(AllIcons.General.TreeHovered);
            presentation.setText("显示工具树");
        } else {
            presentation.setIcon(AllIcons.General.TreeSelected);
            presentation.setText("隐藏工具树");
        }
    }

    private boolean hideToolTree() {
        return ToolkitGlobalState.getInstance().getHideToolTree();
    }
}
