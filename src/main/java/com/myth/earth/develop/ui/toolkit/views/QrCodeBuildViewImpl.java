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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.intellij.MyDarculaComboBoxUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * 生成二维码
 *
 * @author zhouchao
 * @date 2025/10/20 下午10:28
 **/
@Tool(category = ToolCategory.NETWORK, name = "二维码生成", description = "支持二维码生成")
public class QrCodeBuildViewImpl extends AbstractToolView {

    private final JBLabel             qrLabel;
    private final ExpandableTextField textField;
    private final ComboBox<Integer>   optionBox;
    private final List<Integer>       SIZE_OPTIONS = Arrays.asList(150, 200, 250, 300, 350, 400);

    public QrCodeBuildViewImpl(@NotNull Project project) {
        super(project);
        qrLabel = new JBLabel();
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textField = new ExpandableTextField();
        optionBox = new ComboBox<>();
        optionBox.setUI(new MyDarculaComboBoxUI());
        optionBox.setBorder(BorderFactory.createEmptyBorder());
        // optionBox.setBackground(COMBOBOX_COLOR);
        SIZE_OPTIONS.forEach(optionBox::addItem);
        optionBox.setSelectedItem(300);

        JPanel optionBoxPanel = new JPanel(new BorderLayout());
        // optionBoxPanel.setBackground(COMBOBOX_COLOR);
        optionBoxPanel.add(optionBox, BorderLayout.CENTER);

        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.X_AXIS));
        optPanel.setBorder(JBUI.Borders.empty());
        optPanel.add(createLineLabelPanel(80, "尺寸大小", optionBoxPanel));
        optPanel.add(Box.createHorizontalStrut(5));
        optPanel.add(createButton(60, "生成", e -> {
            String text = textField.getText();
            Integer selectedItem = (Integer) optionBox.getSelectedItem();
            if (StringUtil.isNotEmpty(text) && selectedItem != null) {
                qrLabel.setIcon(generateQrCode(text, selectedItem));
            }
        }));
        optPanel.add(Box.createHorizontalStrut(5));
        optPanel.add(createButton(60, "保存", e -> {
            Icon icon = qrLabel.getIcon();
            if (icon instanceof ImageIcon) {
                BufferedImage image = (BufferedImage) ((ImageIcon) icon).getImage();
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
                if (virtualFile != null) {
                    String path = virtualFile.getPath();
                    // 替换 FileServer.saveImage 调用
                    try {
                        ImageIO.write(image, "PNG", new File(path + "/qrcode.png"));
                        Desktop.getDesktop().open(new File(path));
                    } catch (IOException ex) {
                        qrLabel.setText("二维码保存失败，失败原因：" + ex.getMessage());
                    }
                }
            }
        }));

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(5)
                                        .addComponent(createLineLabelPanel(80, "输入内容", textField))
                                        .addComponent(optPanel)
                                        .addComponentFillVertically(createBoxLabelPanel("二维码", qrLabel), 5)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void manualRefresh() {
        reset();
    }

    private void reset() {
        textField.setText(null);
        qrLabel.setIcon(null);
        optionBox.setSelectedItem(300);
    }

    private Icon generateQrCode(String qrContent, int size) {
        try {
            BufferedImage croppedImage = getBufferedImage(qrContent, size);
            return new ImageIcon(croppedImage);
        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败！");
        }
    }

    private static BufferedImage getBufferedImage(String qrContent, int size) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, size, size, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        // 创建一个新的图像，只包含二维码实际大小，去除多余的黑色背景
        int left = width;
        int top = height;
        int right = 0;
        int bottom = 0;

        // 查找二维码的实际边界
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }

        // 添加margin
        int margin = 10;
        left = Math.max(0, left - margin);
        top = Math.max(0, top - margin);
        right = Math.min(width, right + margin);
        bottom = Math.min(height, bottom + margin);

        // 裁剪图像
        return image.getSubimage(left, top, right - left, bottom - top);
    }
}
