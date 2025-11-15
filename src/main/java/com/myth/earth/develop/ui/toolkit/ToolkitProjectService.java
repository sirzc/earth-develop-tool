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

package com.myth.earth.develop.ui.toolkit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.JBInsets;
import com.myth.earth.develop.model.ToolKitInfo;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.ui.toolkit.core.ToolView;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工具管理服务
 *
 * @author zhouchao
 * @date 2025-09-08 上午11:31
 */
public class ToolkitProjectService {
    private final Project            project;
    private final ToolkitLoader      toolkitLoader;
    private final ToolMainPopupPanel toolMainPopupPanel;

    public ToolkitProjectService(@NotNull Project project) {
        this.project = project;
        this.toolkitLoader = new ToolkitLoader(getClass().getPackageName() + ".views");
        this.toolMainPopupPanel = new ToolMainPopupPanel(project, toolkitLoader.getAllCategorizedTools());
    }

    public static ToolkitProjectService getInstance(@NotNull Project project) {
        return project.getService(ToolkitProjectService.class);
    }

    public ToolView get(Class<? extends ToolView> clz) {
        return toolkitLoader.getInstance(project, clz);
    }


    public void showDialog() {
        JBPopup popup = JBPopupFactory.getInstance()
                                                      // 弹出内容 + 首选获取焦点的组件
                                                      .createComponentPopupBuilder(toolMainPopupPanel, toolMainPopupPanel.getSearchField())
                                                      // .setTitle("class search")
                                                      .setProject(project).setModalContext(false).setCancelOnClickOutside(false).setRequestFocus(true)
                                                      .setCancelKeyEnabled(true)
                                                      .setCancelOnWindowDeactivation(false)
                                                      // .setCancelCallback(() -> false)
                                                      .setCancelOnMouseOutCallback(toolMainPopupPanel)// 鼠标外移回调，仅在mac 全屏下才启作用
                                                      .addUserData("SIMPLE_WINDOW").setResizable(true).setMovable(true)
                                                      // .setDimensionServiceKey(project,KEY.getName(), true)
                                                      .setLocateWithinScreenBounds(false)
                                                      .createPopup();
        toolMainPopupPanel.refreshPopupAndToolView(popup);
        // 注册为父子关系
        Disposer.register(popup, toolMainPopupPanel);
        // 设置popup当前显示最小大小
        Dimension size = toolMainPopupPanel.getMinimumSize();
        JBInsets.addTo(size, popup.getContent().getInsets());
        popup.setMinimumSize(size);
        popup.showCenteredInCurrentWindow(project);
        //listPopup.showInBestPositionFor(dataContext);
    }

    public List<ToolKitInfo> assembleToolKitInfos() {
        List<ToolKitInfo> results = new ArrayList<>(16);
        ToolkitGlobalState toolkitGlobalState = ToolkitGlobalState.getInstance();
        List<String> hideToolKits = toolkitGlobalState.getHideToolKits();
        Map<ToolCategory, List<Class<? extends ToolView>>> tools = toolkitLoader.getAllCategorizedTools();
        for (Map.Entry<ToolCategory, List<Class<? extends ToolView>>> entry : tools.entrySet()) {
            ToolCategory toolCategory = entry.getKey();
            for (Class<? extends ToolView> toolView : entry.getValue()) {
                Tool tool = toolView.getAnnotation(Tool.class);
                ToolKitInfo toolKitInfo = new ToolKitInfo();
                toolKitInfo.setEnable(!hideToolKits.contains(toolCategory.getName() + "#" + tool.name()));
                toolKitInfo.setCategory(toolCategory.getName());
                toolKitInfo.setName(tool.name());
                results.add(toolKitInfo);
            }
        }
        return results;
    }
}
