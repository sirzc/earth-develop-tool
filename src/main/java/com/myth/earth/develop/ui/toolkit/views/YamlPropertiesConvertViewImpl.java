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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.utils.PropertiesCompareUtil;
import com.myth.earth.develop.utils.PropertiesYamlUtil;
import com.myth.earth.develop.utils.YamlCompareUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * properties yaml互转
 *
 * @author zhouchao
 * @date 2025/12/6 下午3:52
 **/
@Tool(category = ToolCategory.DEVELOP, name = "Properties<=>Yaml", description = "properties转yaml、yaml转properties")
public class YamlPropertiesConvertViewImpl extends AbstractToolView {

    private final JBTextArea                inputTextArea;
    private final JBTextArea                outputTextArea;
    private final TextFieldWithBrowseButton inputTextField;

    public YamlPropertiesConvertViewImpl(@NotNull Project project) {
        super(project);

        inputTextField = buildFileSelectFieldButton(project);
        inputTextArea = createTextArea();
        inputTextArea.getEmptyText().setText("文件路径存在时，将优先读取文件到输入内容中，作为转换对象...");
        inputTextArea.setEditable(true);
        outputTextArea = createTextArea();

        JButton changeButton = createButton(80, "转换", e -> {
            String filePath = inputTextField.getText();
            if (!filePath.isEmpty()) {
                try {
                    String fileContent = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
                    inputTextArea.setText(fileContent);
                } catch (Exception ex) {
                    outputTextArea.setText("读取文件内容失败: " + ex.getMessage());
                    return;
                }
            }

            String inputText = inputTextArea.getText();
            if (inputText == null || inputText.isEmpty()) {
                return;
            }

            try {
                boolean isYaml = isYaml(inputText);
                String result = isYaml ? PropertiesYamlUtil.convertYamlToProperties(inputText) : PropertiesYamlUtil.convertPropertiesToYaml(inputText);
                outputTextArea.setText(result);
            } catch (Exception ex) {
                outputTextArea.setText("转换失败: " + ex.getMessage());
            }
        });

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(createLineLabelPanel(80, "选择文件", inputTextField), BorderLayout.CENTER);
        sourcePanel.add(changeButton, BorderLayout.EAST);

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(10)
                                        .addComponent(sourcePanel)
                                        .addComponentFillVertically(createBoxLabelPanel("输入内容", createScrollPane(inputTextArea)), 10)
                                        .addComponentFillVertically(createBoxLabelPanel("输出内容", createScrollPane(outputTextArea)), 10)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    private boolean isYaml(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return false;
        }
        try {
            PropertiesYamlUtil.convertYamlToProperties(inputText);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    private TextFieldWithBrowseButton buildFileSelectFieldButton(@NotNull Project project) {
        TextFieldWithBrowseButton formFilePathField = new TextFieldWithBrowseButton();
        formFilePathField.getTextField().setBorder(JBUI.Borders.empty());
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        fileChooserDescriptor.withFileFilter(
                virtualFile -> YamlCompareUtil.FILE_TYPE.equals(virtualFile.getExtension()) || YamlCompareUtil.SHORT_FILE_TYPE.equals(
                        virtualFile.getExtension()) || PropertiesCompareUtil.FILE_TYPE.equalsIgnoreCase(virtualFile.getExtension()));
        // 自定义文件选择后的处理逻辑
        formFilePathField.addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, project, null);
            if (file != null) {
                formFilePathField.getTextField().setText(file.getPath());
            }
        });
        return formFilePathField;
    }

    @Override
    public void manualRefresh() {
        inputTextField.setText(null);
        inputTextArea.setText(null);
        outputTextArea.setText(null);
    }

}
