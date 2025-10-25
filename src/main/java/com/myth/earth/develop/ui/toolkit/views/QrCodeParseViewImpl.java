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

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 二维码解析
 *
 * @author zhouchao
 * @date 2025/10/20 下午10:28
 **/
@Tool(category = ToolCategory.NETWORK, name = "二维码解析", description = "读取二位码内容")
public class QrCodeParseViewImpl extends AbstractToolView {

    private static final List<String>              SUPPORTED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp");
    private final        TextFieldWithBrowseButton formFilePathField;
    private final        JBTextArea                resultArea;

    public QrCodeParseViewImpl(@NotNull Project project) {
        super(project);
        formFilePathField = new TextFieldWithBrowseButton();
        formFilePathField.getTextField().setBorder(JBUI.Borders.empty());
        // 自定义文件选择后的处理逻辑
        formFilePathField.addActionListener(e -> {
            FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, false);
            fileChooserDescriptor.withTitle("选择文件");
            fileChooserDescriptor.withFileFilter(virtualFile -> {
                String extension = virtualFile.getExtension();
                return Objects.nonNull(extension) && SUPPORTED_IMAGE_TYPES.contains(extension.toLowerCase());
            });
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, project, null);
            if (file != null) {
                formFilePathField.getTextField().setText(file.getPath());
            }
        });

        resultArea = createTextArea();

        // 添加拖拽支持
        resultArea.setDropTarget(new DropTarget() {
            @Override
            @SuppressWarnings("all")
            public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
                try {
                    dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dropTargetDropEvent.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) {
                            File file = files.get(0);
                            String filePath = file.getAbsolutePath();
                            formFilePathField.getTextField().setText(filePath);
                            // 自动解析二维码
                            try {
                                String content = readQRCode(file);
                                resultArea.setText(content);
                            } catch (NotFoundException ex) {
                                resultArea.setText("错误：未识别到有效的二维码");
                            } catch (Exception ex) {
                                resultArea.setText("错误：解析失败 - " + ex.getMessage());
                            }
                        }
                    }
                    dropTargetDropEvent.dropComplete(true);
                } catch (Exception e) {
                    dropTargetDropEvent.dropComplete(false);
                }
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(createLineLabelPanel(80, "文件路径", formFilePathField));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(createButton(60, "解析", e -> {
            String filePath = formFilePathField.getText();
            if (StringUtil.isEmpty(filePath)) {
                return;
            }

            File imageFile = new File(filePath);
            if (!imageFile.exists()) {
                resultArea.setText("错误：文件不存在");
                return;
            }

            try {
                resultArea.setText(readQRCode(imageFile));
            } catch (NotFoundException ex) {
                resultArea.setText("错误：未识别到有效的二维码");
            } catch (IOException ex) {
                resultArea.setText("错误：读取文件失败 - " + ex.getMessage());
            } catch (Exception ex) {
                resultArea.setText("错误：解析失败 - " + ex.getMessage());
            }
        }));

        String tagName = "<html><body>解析结果 <b style='color:orange;'>「拖拽二维码到下方区域自动解析结果」</b></body></html>";
        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(5)
                                        .addComponent(topPanel)
                                        .addComponentFillVertically(createBoxLabelPanel(tagName, createScrollPane(resultArea)), 5)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void manualRefresh() {
        reset();
    }

    private void reset() {
        formFilePathField.setText(null);
        resultArea.setText(null);
    }

    public String readQRCode(File imageFile) throws IOException, NotFoundException {
        // 读取图像文件
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        // 创建亮度源
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        // 创建二值化位图
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        // 解码获取结果
        Result result = new MultiFormatReader().decode(binaryBitmap);
        // 返回二维码内容
        return result.getText();
    }
}
