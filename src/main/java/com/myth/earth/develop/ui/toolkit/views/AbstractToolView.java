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

package com.myth.earth.develop.ui.toolkit.views;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.ToolView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 封装统一的工具面板内容
 *
 * @author zhouchao
 * @date 2025-09-08 下午3:54
 */
public abstract class AbstractToolView extends JPanel implements ToolView {

    private final JPanel  rootPanel;
    protected     Project project;

    public AbstractToolView() {
        super(new BorderLayout());
        setBorder(JBUI.Borders.empty(5));
        // Tool tool = this.getClass().getAnnotation(Tool.class);
        // String title = String.format("%s>%s(%s)", tool.category().getName(), tool.name(), tool.description());
        // add(new JBLabel(title), BorderLayout.NORTH);
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(this, BorderLayout.CENTER);
    }

    public abstract void refreshToolData();

    @Override
    public @NotNull JComponent refreshView(@NotNull Project project) {
        this.project = project;
        refreshToolData();
        return rootPanel;
    }

    @NotNull
    protected static JButton createButton(String name, ActionListener listener) {
        JButton button = new JButton(name);
        // new JBColor(new Color(0, 120, 215), new Color(0, 120, 215))
        button.setPreferredSize(JBUI.size(50, 35));
        button.addActionListener(listener);
        return button;
    }
}
