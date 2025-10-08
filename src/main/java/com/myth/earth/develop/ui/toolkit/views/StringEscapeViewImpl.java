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
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.apache.commons.text.StringEscapeUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串转义工具
 *
 * @author zhouchao
 * @date 2025/10/7 下午10:00
 **/
@Tool(category = ToolCategory.ENCODE, name = "内容转义", description = "支持Java、JavaScript、JSON、HTML、XML、CSV、XSI")
public class StringEscapeViewImpl extends AbstractToolView {
    /**
     * 支持签名
     */
    private static final List<String>     ESCAPE_OPTIONS = Arrays.asList("Java", "JavaScript", "JSON", "HTML3", "HTML4", "XML10", "XML11", "CSV", "XSI");
    private final        JBTextArea       inputTextArea;
    private final        JBTextArea       outputTextArea;
    private final        ComboBox<String> optionBox;

    public StringEscapeViewImpl(@NotNull Project project) {
        super(project);

        inputTextArea = createTextArea();
        inputTextArea.setEditable(true);

        outputTextArea = createTextArea();

        optionBox = new ComboBox<>();
        optionBox.setBorder(BorderFactory.createEmptyBorder());
        optionBox.setBackground(COMBOBOX_COLOR);

        ESCAPE_OPTIONS.forEach(optionBox::addItem);

        JPanel optionBoxPanel = new JPanel(new BorderLayout());
        optionBoxPanel.setBackground(COMBOBOX_COLOR);
        optionBoxPanel.add(optionBox, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new HorizontalLayout());
        topPanel.setBorder(JBUI.Borders.empty());
        topPanel.add(createLineLabelPanel(80, "文本类型", optionBoxPanel));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(createButton(50, "交换", e -> {
            String inputText = inputTextArea.getText();
            String outputText = outputTextArea.getText();
            inputTextArea.setText(outputText);
            outputTextArea.setText(inputText);
        }));
        topPanel.add(createButton(50, "转义", e -> escape()));
        topPanel.add(createButton(70, "去转义", e -> unescape()));
        topPanel.add(createButton(110, "复制到剪贴板", e -> {
            String text = outputTextArea.getText();
            ClipboardKit.copy(text);
        }));

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(5)
                                        .addComponent(topPanel)
                                        .addComponentFillVertically(createBoxLabelPanel("输入", createScrollPane(inputTextArea)), 5)
                                        .addComponentFillVertically(createBoxLabelPanel("输出", createScrollPane(outputTextArea)), 5)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void manualRefresh() {
        inputTextArea.setText(null);
        outputTextArea.setText(null);
    }

    private void escape() {
        String input = inputTextArea.getText();
        String selectedOption = (String) optionBox.getSelectedItem();
        if (input == null || input.isEmpty() || selectedOption == null) {
            outputTextArea.setText("");
            return;
        }

        String result;
        switch (selectedOption) {
            case "Java":
                result = StringEscapeUtils.escapeJava(input);
                break;
            case "Java Script":
                result = StringEscapeUtils.escapeEcmaScript(input);
                break;
            case "JSON":
                result = StringEscapeUtils.escapeJson(input);
                break;
            case "HTML3":
                result = StringEscapeUtils.escapeHtml3(input);
                break;
            case "HTML4":
                result = StringEscapeUtils.escapeHtml4(input);
                break;
            case "XML10":
                result = StringEscapeUtils.escapeXml10(input);
                break;
            case "XML11":
                result = StringEscapeUtils.escapeXml11(input);
                break;
            case "CSV":
                result = StringEscapeUtils.escapeCsv(input);
                break;
            case "XSI":
                result = StringEscapeUtils.escapeXSI(input);
                break;
            default:
                result = input;
                break;
        }
        outputTextArea.setText(result);
    }

    private void unescape() {
        String input = inputTextArea.getText();
        String selectedOption = (String) optionBox.getSelectedItem();
        if (input == null || input.isEmpty() || selectedOption == null) {
            outputTextArea.setText("");
            return;
        }

        String result;
        switch (selectedOption) {
            case "Java":
                result = StringEscapeUtils.unescapeJava(input);
                break;
            case "Java Script":
                result = StringEscapeUtils.unescapeEcmaScript(input);
                break;
            case "JSON":
                result = StringEscapeUtils.unescapeJson(input);
                break;
            case "HTML3":
                result = StringEscapeUtils.unescapeHtml3(input);
                break;
            case "HTML4":
                result = StringEscapeUtils.unescapeHtml4(input);
                break;
            case "XML10":
            case "XML11":
                result = StringEscapeUtils.unescapeXml(input);
                break;
            case "CSV":
                result = StringEscapeUtils.unescapeCsv(input);
                break;
            case "XSI":
                result = StringEscapeUtils.unescapeXSI(input);
                break;
            default:
                result = input;
                break;
        }
        outputTextArea.setText(result);
    }
}
