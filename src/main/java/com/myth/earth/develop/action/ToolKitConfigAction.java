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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.myth.earth.develop.model.ToolKitInfo;
import com.myth.earth.develop.ui.dialog.ToolKitConfigDialog;
import com.myth.earth.develop.ui.toolkit.ToolkitProjectService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 工具箱设置action
 *
 * @author zhouchao
 * @date 2025-11-13 下午9:27
 */
public class ToolKitConfigAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        ToolkitProjectService toolkitProjectService = ToolkitProjectService.getInstance(project);
        List<ToolKitInfo> toolKitInfos = toolkitProjectService.assembleToolKitInfos();

        ToolKitConfigDialog toolKitConfigDialog = new ToolKitConfigDialog(project, toolKitInfos);
        toolKitConfigDialog.setVisible(true);
    }
}
