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
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

/**
 * UUID 生成器
 *
 * @author zhouchao
 * @date 2025-09-11 下午9:10
 */
@Tool(category = ToolCategory.DEVELOP, name = "UUID生成器", description = "批量生成UUID、带-、不带-")
public class UUIDToolViewImpl extends AbstractToolView {

    public UUIDToolViewImpl(@NotNull Project project) {
        super(project);
        JBTextField generateNumField = new JBTextField("1");
        JBCheckBox selectBox = new JBCheckBox("不带-");
        JBTextArea resultArea = createTextArea();
        JButton generateButton = createButton("生成", e -> {
            try {
                int count = Integer.parseInt(generateNumField.getText());
                boolean withoutHyphen = selectBox.isSelected();
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    String uuid = UUID.randomUUID().toString();
                    if (withoutHyphen) {
                        uuid = uuid.replace("-", "");
                    }
                    result.append(uuid).append("\n");
                }
                resultArea.setText(result.toString());
            } catch (NumberFormatException ex) {
                resultArea.setText("请输入有效的数量");
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        topPanel.add(new JBLabel("数量："));
        topPanel.add(generateNumField);
        topPanel.add(selectBox);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(generateButton);

        JBScrollPane jbScrollPane = new JBScrollPane(resultArea);
        jbScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jbScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(topPanel, BorderLayout.NORTH);
        add(jbScrollPane, BorderLayout.CENTER);
    }
}
