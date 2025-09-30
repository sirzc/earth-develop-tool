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
import com.myth.earth.develop.model.CompareResult;
import com.myth.earth.develop.model.DifferenceResult;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.utils.YamlCompareUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * yaml文件比较工具
 *
 * @author zhouchao
 * @date 2025/9/30 下午5:42
 **/
@Tool(category = ToolCategory.DIFF, name = "Yaml差异", description = "比较yaml、yml文件key、value差异")
public class YamlDiffViewImpl extends AbstractToolView {

    public YamlDiffViewImpl(@NotNull Project project) {
        super(project);

        JBTextArea result1 = createTextArea();
        result1.getEmptyText().setText("仅展示Yaml1中存在的内容");

        JBTextArea result2 = createTextArea();
        result2.getEmptyText().setText("仅展示Yaml2中存在的内容");

        TextFieldWithBrowseButton sourceTextField = buildFileSelectFieldButton(project);
        TextFieldWithBrowseButton targetTextField = buildFileSelectFieldButton(project);

        JButton diffKeyButton = createButton(80, "差异键", e -> {
            try {
                CompareResult compareResult = YamlCompareUtil.parseAndCompare(sourceTextField.getText(), targetTextField.getText());
                Collection<String> sourceKeys = compareResult.getSourceKeys();
                Collection<String> targetKeys = compareResult.getTargetKeys();
                result1.setText(String.join("\n", sourceKeys));
                result2.setText(String.join("\n", targetKeys));
            } catch (Exception ex) {
                result1.setText("比较执行异常: " + ex.getMessage());
                result2.setText("比较执行异常: " + ex.getMessage());
            }
        });

        JButton diffValueButton = createButton(80, "差异值", e -> {
            try {
                CompareResult compareResult = YamlCompareUtil.parseAndCompare(sourceTextField.getText(), targetTextField.getText());
                Collection<DifferenceResult> differenceResults = compareResult.getDifferenceResults();
                List<String> sources = differenceResults.stream().map(dr -> dr.getKey() + "=" + dr.getSource()).collect(Collectors.toList());
                List<String> targets = differenceResults.stream().map(dr -> dr.getKey() + "=" + dr.getTarget()).collect(Collectors.toList());
                result1.setText(String.join("\n", sources));
                result2.setText(String.join("\n", targets));
            } catch (Exception ex) {
                result1.setText("比较执行异常: " + ex.getMessage());
                result2.setText("比较执行异常: " + ex.getMessage());
            }
        });

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(createLineLabelPanel(60, "Yaml1", sourceTextField), BorderLayout.CENTER);
        sourcePanel.add(diffKeyButton, BorderLayout.EAST);

        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.add(createLineLabelPanel(60, "Yaml2", targetTextField), BorderLayout.CENTER);
        targetPanel.add(diffValueButton, BorderLayout.EAST);

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(10)
                                        .addComponent(sourcePanel)
                                        .addComponent(targetPanel)
                                        .addComponentFillVertically(createBoxLabelPanel("在Yaml1中存在", createScrollPane(result1)), 10)
                                        .addComponentFillVertically(createBoxLabelPanel("在Yaml2中存在", createScrollPane(result2)), 10)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @NotNull
    private TextFieldWithBrowseButton buildFileSelectFieldButton(@NotNull Project project) {
        TextFieldWithBrowseButton formFilePathField = new TextFieldWithBrowseButton();
        formFilePathField.getTextField().setBorder(JBUI.Borders.empty());
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        fileChooserDescriptor.withFileFilter(virtualFile -> YamlCompareUtil.FILE_TYPE.equals(virtualFile.getExtension()) || YamlCompareUtil.SHORT_FILE_TYPE.equals(virtualFile.getExtension()));
        // 自定义文件选择后的处理逻辑
        formFilePathField.addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, project, null);
            if (file != null) {
                formFilePathField.getTextField().setText(file.getPath());
            }
        });
        return formFilePathField;
    }
}
