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

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.BuildNumber;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
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

    public static final JBColor NEW_COMBOBOX_COLOR = new JBColor(0xFFFFFFFF, 0xFF393B40);
    public static final JBColor OLD_COMBOBOX_COLOR = new JBColor(0xFFFFFFFF, 0xFF3B3F41);
    public static final JBColor COMBOBOX_COLOR;
    private final       JPanel  rootPanel;
    protected           Project project;

    static {
        BuildNumber buildNumber = ApplicationInfo.getInstance().getBuild();
        int baselineVersion = buildNumber.getBaselineVersion();
        if (baselineVersion > 223) {
            COMBOBOX_COLOR = NEW_COMBOBOX_COLOR;
        } else {
            COMBOBOX_COLOR = OLD_COMBOBOX_COLOR;
        }
    }

    public AbstractToolView(@NotNull Project project) {
        super(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        // Tool tool = this.getClass().getAnnotation(Tool.class);
        // String title = String.format("%s>%s(%s)", tool.category().getName(), tool.name(), tool.description());
        // add(new JBLabel(title), BorderLayout.NORTH);
        rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(this, BorderLayout.CENTER);
    }

    /**
     * 显示内容时刷新数据
     */
    public void refreshToolData() {
        // 需要点击菜单刷新数据的可重写此方法
    }

    @Override
    public @NotNull JComponent refreshView() {
        refreshToolData();
        return rootPanel;
    }

    @NotNull
    protected static JButton createButton(int weight, String name, ActionListener listener) {
        JButton button = new JButton(name);
        // new JBColor(new Color(0, 120, 215), new Color(0, 120, 215))
        button.setPreferredSize(JBUI.size(weight, 35));
        button.addActionListener(listener);
        return button;
    }

    /**
     * 创建一个不可编辑的面板
     *
     * @return 不可编辑的组件
     */
    @NotNull
    protected static JBTextArea createTextArea() {
        JBTextArea textArea = new JBTextArea();
        textArea.setMargin(JBUI.insets(5));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    /**
     * 创建一个无水平滚动条，需要时显示的垂直滚动条
     *
     * @param component 添加滚动条的内容
     * @return 带滚动条的组件
     */
    @NotNull
    protected static JBScrollPane createScrollPane(JComponent component) {
        JBScrollPane scrollPane = new JBScrollPane(component);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    /**
     * 创建一个label行panel
     *
     * @param weight    标签名称宽度
     * @param tag       标签名称
     * @param component 显示内容
     * @return 水平带label的panel
     */
    @NotNull
    protected static JPanel createLineLabelPanel(int weight, @NotNull String tag, @NotNull JComponent component) {
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBorder(IdeBorderFactory.createBorder());

        JBLabel label = new JBLabel(tag);
        label.setBorder(JBUI.Borders.empty(0, 10));
        label.setPreferredSize(JBUI.size(weight, 35));

        component.setBorder(new CustomLineBorder(JBUI.insetsLeft(1)));
        labelPanel.add(label, BorderLayout.WEST);
        labelPanel.add(component, BorderLayout.CENTER);
        return labelPanel;
    }

    /**
     * 创建一个label盒子panel
     *
     * @param tag       标签名称
     * @param component 显示内容
     * @return 上下结构带label的panel
     */
    protected static JPanel createBoxLabelPanel(@NotNull String tag, @NotNull JComponent component) {
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBorder(IdeBorderFactory.createBorder());

        JBLabel label = new JBLabel(tag);
        label.setPreferredSize(JBUI.size(-1, 35));
        label.setBorder(JBUI.Borders.emptyLeft(10));

        component.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));
        labelPanel.add(label, BorderLayout.NORTH);
        labelPanel.add(component, BorderLayout.CENTER);
        return labelPanel;
    }
}
