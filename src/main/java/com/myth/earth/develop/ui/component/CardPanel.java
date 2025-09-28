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

package com.myth.earth.develop.ui.component;

import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolView;

import javax.swing.*;
import java.awt.*;

/**
 * 工具卡片信息
 *
 * @author zhouchao
 * @date 2025-09-12 下午3:28
 */
public class CardPanel extends JBPanel<CardPanel> {

    public CardPanel(Class<? extends ToolView> clz) {
        super(new BorderLayout());
        Tool tool = clz.getAnnotation(Tool.class);
        JBLabel toolLabel = new JBLabel(tool.name());
        toolLabel.setBorder(JBUI.Borders.emptyLeft(10));
        if (StringUtil.isNotEmpty(tool.iconPath())) {
            toolLabel.setIcon(IconLoader.getIcon(tool.iconPath(), CardPanel.class));
        }

        // 需求摘要信息
        JBTextArea summeryTextArea = new JBTextArea();
        // 判断IDEA是否dark主题
        if (UIUtil.isUnderDarcula()) {
            summeryTextArea.setBackground(new JBColor(new Color(0x3F4649), new Color(0x3F4649)));
        } else {
            summeryTextArea.setBackground(new JBColor(new Color(0xF0F0F0), new Color(0xF0F0F0)));
        }
        summeryTextArea.setMinimumSize(new Dimension(-1, 60));
        summeryTextArea.setBorder(JBUI.Borders.empty(10));
        summeryTextArea.setEditable(false);
        summeryTextArea.setLineWrap(true);
        summeryTextArea.setWrapStyleWord(true);
        summeryTextArea.setText(tool.description());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
        topPanel.setPreferredSize(JBUI.size(0, 35));
        topPanel.add(toolLabel, BorderLayout.WEST);

        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(IdeBorderFactory.createRoundedBorder(3));
        cardPanel.add(topPanel, BorderLayout.NORTH);
        cardPanel.add(summeryTextArea, BorderLayout.CENTER);

        JPanel rootPanel = new JBPanel<>(new BorderLayout());
        rootPanel.setBorder(JBUI.Borders.emptyBottom(10));
        rootPanel.add(cardPanel, BorderLayout.CENTER);
        add(rootPanel, BorderLayout.CENTER);
    }
}
