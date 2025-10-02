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

import cn.hutool.core.io.FileUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 图片转Base64
 *
 * @author zhouchao
 * @date 2025-09-12 下午8:41
 */
@Tool(category = ToolCategory.IMAGE, name = "图片转Base64", description = "图片转Base64，CSS，html")
public class Base64ToolViewImpl extends AbstractToolView {

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp");
    private final TextFieldWithBrowseButton imageTextField;
    private final JBTextArea dataUriTextArea;
    private final JBTextArea cssTextArea;
    private final JBTextArea htmlTextArea;

    public Base64ToolViewImpl(@NotNull Project project) {
        super(project);
        imageTextField = buildFileSelectFieldButton(project);
        imageTextField.getTextField().setBorder(JBUI.Borders.empty());
        dataUriTextArea = createTextArea();
        cssTextArea = createTextArea();
        htmlTextArea = createTextArea();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createLineLabelPanel(80, "选择图片", imageTextField), BorderLayout.CENTER);
        topPanel.add(createButton(50, "转换", e -> {
            String filePath = imageTextField.getText();
            if (filePath.isEmpty()) {
                return;
            }

            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    return;
                }

                byte[] fileContent = FileUtil.readBytes(file);
                String base64String = Base64.getEncoder()
                                            .encodeToString(fileContent);
                String mimeType = getMimeType(filePath);
                String dataUri = "data:" + mimeType + ";base64," + base64String;
                // 获取图片尺寸
                ImageIcon imageIcon = new ImageIcon(fileContent);
                int width = imageIcon.getIconWidth();
                int height = imageIcon.getIconHeight();
                // CSS 补充 div.image 和宽高
                String cssText = String.format( "div.image {\n" +
                                                        "    width: %dpx;\n" +
                                                        "    height: %dpx;\n" +
                                                        "    background-image: url(%s);\n" +
                                                        "    background-size: contain;\n" +
                                                        "    background-repeat: no-repeat;\n" +
                                                        "}", width, height, dataUri);
                String htmlText = String.format("<img src=\"%s\" alt=\"base64 image\" width=\"%d\" height=\"%d\" />", dataUri, width, height);
                dataUriTextArea.setText(dataUri);
                cssTextArea.setText(cssText);
                htmlTextArea.setText(htmlText);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "转换失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }), BorderLayout.EAST);

        JPanel centerPanel =  new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(createBoxLabelPanel("data uri", createScrollPane(dataUriTextArea)));
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(createBoxLabelPanel("css", createScrollPane(cssTextArea)));
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(createBoxLabelPanel("html", createScrollPane(htmlTextArea)));


        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    @NotNull
    private TextFieldWithBrowseButton buildFileSelectFieldButton(@NotNull Project project) {
        TextFieldWithBrowseButton formFilePathField = new TextFieldWithBrowseButton();
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        fileChooserDescriptor.withTitle("选择图片");
        fileChooserDescriptor.withFileFilter(virtualFile -> {
            String extension = virtualFile.getExtension();
            if (extension == null)
                return false;
            return SUPPORTED_IMAGE_TYPES.contains(extension.toLowerCase());
        });

        // 自定义文件选择后的处理逻辑
        formFilePathField.addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, project, null);
            if (file != null) {
                formFilePathField.getTextField()
                                 .setText(file.getPath());
            }
        });
        return formFilePathField;
    }

    private String getMimeType(String filePath) {
        String extension = FileUtil.extName(filePath)
                                   .toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            default:
                return "image/" + extension;
        }
    }

    @Override
    public void manualRefresh() {
        imageTextField.setText(null);
        dataUriTextArea.setText(null);
        cssTextArea.setText(null);
        htmlTextArea.setText(null);
    }
}
