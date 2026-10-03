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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.component.MyLabelButton;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * base64 转图片
 *
 * @author zhouchao
 * @date 2026/9/22 下午10:27
 **/
@Tool(category = ToolCategory.IMAGE, name = "Base64转图片", description = "base64转图片")
public class Base64ToImageViewImpl extends AbstractToolView {

    private final JBLabel    imageLabel;
    private final JBTextArea textField;
    private final JPanel     resultPanel;

    public Base64ToImageViewImpl(@NotNull Project project) {
        super(project);
        imageLabel = new JBLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textField = createTextArea();
        textField.setEditable(true);

        resultPanel = createBoxButtonPanel("解析结果", imageLabel, createDownload());

        JBScrollPane textScrollPane = createScrollPane(textField);
        textScrollPane.setPreferredSize(JBUI.size(-1, 150));

        JPanel inputBase64Panel = createBoxButtonPanel("图片Base64编码", textScrollPane, createExecute());

        JPanel centerPanel = FormBuilder.createFormBuilder().setVerticalGap(5)
                                        // base64录入区域
                                        .addComponent(inputBase64Panel)
                                        // 图片显示区域
                                        .addComponentFillVertically(resultPanel, 5).getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void manualRefresh() {
        reset();
    }

    private void reset() {
        textField.setText(null);
        imageLabel.setText(null);
        imageLabel.setIcon(null);
    }

    /**
     * 将Base64字符串转换为BufferedImage
     */
    public BufferedImage base64ToBufferedImage(@NotNull String base64String) throws IOException {
        // 1. 检查并移除数据URI前缀
        String cleanBase64 = base64String;
        if (base64String.contains(",")) {
            cleanBase64 = base64String.substring(base64String.lastIndexOf(",") + 1);
        }
        // 2. 移除空白字符
        cleanBase64 = cleanBase64.replaceAll("\\s", "");
        // 3. 补齐Base64长度
        while (cleanBase64.length() % 4 != 0) {
            cleanBase64 += "=";
        }
        // 4. 解码为byte[]
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
        // 5. 从byte[]创建BufferedImage
        try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("无法解码图片数据，可能是不支持的图片格式");
            }
            return image;
        }
    }

    /**
     * 等比缩放图片到指定大小（填充模式）
     *
     * @param source       原图
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片
     */
    public static BufferedImage scaleToFit(BufferedImage source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // 计算缩放比例，取较小的比例以保证完全显示
        double scaleX = (double) targetWidth / sourceWidth;
        double scaleY = (double) targetHeight / sourceHeight;
        double scale = Math.min(scaleX, scaleY);

        // 计算缩放后的实际尺寸
        int scaledWidth = (int) (sourceWidth * scale);
        int scaledHeight = (int) (sourceHeight * scale);

        // 创建目标图像
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // 设置高质量渲染参数
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 计算居中位置
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        // 绘制缩放后的图片（居中显示）
        g2d.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return scaledImage;
    }

    @NotNull
    private MyLabelButton createDownload() {
        return new MyLabelButton(AllIcons.Actions.Download, e -> {
            String base64String = textField.getText();
            if (StringUtil.isEmpty(base64String)) {
                return;
            }

            try {
                BufferedImage image = base64ToBufferedImage(base64String);
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
                if (virtualFile != null) {
                    String path = virtualFile.getPath();
                    // 替换 FileServer.saveImage 调用
                    try {
                        ImageIO.write(image, "PNG", new File(path + "/base64.png"));
                        Desktop.getDesktop().open(new File(path));
                    } catch (IOException ex) {
                        imageLabel.setText("原始图片保存失败，失败原因：" + ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                imageLabel.setText("Base64解析失败，失败原因：" + ex.getMessage());
            }
        });
    }

    @NotNull
    private MyLabelButton createExecute() {
        return new MyLabelButton(AllIcons.Actions.Execute, e -> {
            String base64String = textField.getText();
            if (StringUtil.isNotEmpty(base64String)) {
                try {
                    int width = resultPanel.getWidth();
                    int height = resultPanel.getHeight();
                    BufferedImage bufferedImage = base64ToBufferedImage(base64String);
                    BufferedImage targetImage = scaleToFit(bufferedImage, width - 50, height - 50);
                    imageLabel.setIcon(new ImageIcon(targetImage));
                } catch (Exception ex) {
                    imageLabel.setText("Base64解析失败，失败原因：" + ex.getMessage());
                }
            }
        });
    }
}
