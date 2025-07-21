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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.myth.earth.develop.helper.CommandHelper;
import com.myth.earth.develop.kit.PluginNotifyKit;
import org.jetbrains.annotations.NotNull;

/**
 * 解除端口号占用问题
 *
 * @author zhouchao
 * @date 2025-07-21 下午2:33
 */
public class ReleasePortAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        String port = "";
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            port = editor.getSelectionModel().getSelectedText();
        }

        if (port == null || port.isBlank()) {
            port = Messages.showInputDialog(project, "输入需要解除占用的端口号", "请输入端口号", null);
        }

        if (port == null || !port.matches("\\d+")) {
            return;
        }

        try {
            int pid = CommandHelper.getPid(Integer.parseInt(port));
            if (pid == -1 || !CommandHelper.killProcess(pid)) {
                PluginNotifyKit.info(project, "未检测到端口号占用，端口号：" + port);
                return;
            }
            PluginNotifyKit.info(project, "端口号占用解除完成，端口号：" + port);
        } catch (Exception ex) {
            PluginNotifyKit.warn(project, "端口号解除占用失败，端口号：" + port);
        }
    }
}
