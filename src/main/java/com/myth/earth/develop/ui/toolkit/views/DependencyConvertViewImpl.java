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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.common.DependencyStyle;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.utils.DependencyConverter;
import com.myth.earth.develop.utils.DependencyParser;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Jar依赖转换
 *
 * @author zhouchao
 * @date 2026/6/2 下午7:00
 **/
@Tool(category = ToolCategory.DEVELOP, name = "Jar依赖转换", description = "maven、groovy风格互转")
public class DependencyConvertViewImpl extends AbstractToolView {

    public static final String[] SCOPES = new String[] {"Compile", "Runtime", "Test", "Provided"};
    private final JBTextArea inputTextArea;
    private final ComboBox<DependencyStyle> styleComboBox;
    private final JComboBox<String> scopeComboBox;

    public DependencyConvertViewImpl(@NotNull Project project) {
        super(project);
        // 初始化风格下拉框
        styleComboBox = new ComboBox<>(DependencyStyle.values());
        styleComboBox.setPreferredSize(JBUI.size(120, -1));
        styleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DependencyStyle) {
                    setText(((DependencyStyle) value).getDesc());
                }
                return this;
            }
        });

        // 初始化依赖范围下拉框 (Scope)
        scopeComboBox = new ComboBox<>(SCOPES);
        scopeComboBox.setPreferredSize(JBUI.size(100, -1));

        inputTextArea = createTextArea();
        inputTextArea.getEmptyText().setText("自动识别依赖风格进行转换...");
        inputTextArea.setEditable(true);
        inputTextArea.setMargin(JBUI.insets(5));

        JPanel topPanel = new JPanel(new HorizontalLayout(5));
        topPanel.setPreferredSize(JBUI.size(-1, 35));
        topPanel.setBorder(JBUI.Borders.emptyLeft(5));
        topPanel.add(styleComboBox);
        topPanel.add(scopeComboBox);
        topPanel.add(createButton(50, "转换", e -> {
            String text = inputTextArea.getText();
            if (StringUtils.isBlank(text)) {
                return;
            }

            try {
                DependencyParser.ParsedDependency dependency = DependencyParser.parse(text);
                String scope = (String) scopeComboBox.getSelectedItem();
                DependencyStyle dependencyStyle = (DependencyStyle) styleComboBox.getSelectedItem();
                String convert = DependencyConverter.convert(dependency.groupId, dependency.artifactId, dependency.version, scope, dependencyStyle);
                inputTextArea.setText(convert);
            } catch (Exception exc) {
                inputTextArea.setText(exc.getMessage() + "\n" + text);
            }
        }));

        JBScrollPane scrollPane = createScrollPane(inputTextArea);
        scrollPane.setBorder(new CustomLineBorder(JBUI.insetsTop(1)));

        JPanel boxPanel = new JPanel(new BorderLayout());
        boxPanel.setBorder(IdeBorderFactory.createBorder());
        boxPanel.add(topPanel, BorderLayout.NORTH);
        boxPanel.add(scrollPane, BorderLayout.CENTER);

        // 添加 Scope 说明表格
        String scopeTableHtml = "<html><body style='font-size: 11px; font-family: monospace;'>" +
                "<div style='margin-bottom: 5px;'><b>scope&nbsp;&nbsp; </b>: 有效范围&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;依赖传递&nbsp;| 说明</div>" +
                "<div style='margin-bottom: 5px;'><b>compile&nbsp;</b>: 编译、运行、测试 | 是&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 默认范围，全周期有效</div>" +
                "<div style='margin-bottom: 5px;'><b>provided</b>: 编译、测试&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;| 否&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 容器提供，打包排除</div>" +
                "<div style='margin-bottom: 5px;'><b>runtime&nbsp;</b>: 运行、测试&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;| 是&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 接口编译，实现运行</div>" +
                "<div style='margin-bottom: 5px;'><b>test&nbsp;&nbsp;&nbsp;&nbsp;</b>: 测试&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;| 否&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 仅测试代码使用，不传递</div>" +
                "<div style='margin-bottom: 5px;'><b>system&nbsp;&nbsp;</b>: 编译、测试&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;| 是&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | 类似provided，需指定本地路径</div>" +
                "</body></html>";
        JBLabel scopeInfoLabel = new JBLabel(scopeTableHtml);
        scopeInfoLabel.setBorder(JBUI.Borders.empty(5));

        add(boxPanel, BorderLayout.CENTER);
        add(scopeInfoLabel, BorderLayout.SOUTH);
    }

    @Override
    public void manualRefresh() {
        // 默认选择 Maven 风格
        styleComboBox.setSelectedItem(DependencyStyle.MAVEN);
        // 默认选择 Compile 范围
        scopeComboBox.setSelectedItem("Compile");
        // 清空输入框
        inputTextArea.setText(null);
    }
}
