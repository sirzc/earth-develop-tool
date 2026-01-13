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
import com.google.zxing.common.BitMatrix;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.transfer.FileServer;
import com.myth.earth.develop.ui.intellij.MyDarculaComboBoxUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * 文件传输视图
 *
 * @author zhouchao
 * @date 2025-10-08 上午11:02
 */
@Tool(category = ToolCategory.NETWORK, name = "文件传输", description = "同一网络环境下文件传输")
public class FileTransferViewImpl extends AbstractToolView {

    private final ComboBox<String>          ipComboBox;
    private final JBLabel                   qrLabel;
    private final ExtendableTextField       downloadTextField;
    private final TextFieldWithBrowseButton formFilePathField;
    private       FileServer                fileServer;

    public FileTransferViewImpl(@NotNull Project project) {
        super(project);
        // 网卡切换
        ipComboBox = new ComboBox<>();
        ipComboBox.setUI(new MyDarculaComboBoxUI());
        ipComboBox.setBorder(BorderFactory.createEmptyBorder());
        ipComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedIp = (String) ipComboBox.getSelectedItem();
                if (!"--多网卡，请选择手机同网段IP--".equals(selectedIp)) {
                    refresh(selectedIp);
                }
            }
        });

        // 下载地址
        downloadTextField = new ExtendableTextField();
        downloadTextField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineCopyHover, AllIcons.General.InlineCopy, "Copy", () -> {
            String text = downloadTextField.getText();
            ClipboardKit.copy(text);
        }));

        // 扫码地址
        qrLabel = new JBLabel();

        formFilePathField = new TextFieldWithBrowseButton();
        formFilePathField.getTextField().setBorder(JBUI.Borders.empty());
        // 自定义文件选择后的处理逻辑
        formFilePathField.addActionListener(e -> {
            reset();
            FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, true, false, false, false);
            fileChooserDescriptor.withTitle("选择文件");
            VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, project, null);
            if (file != null) {
                String filePath = file.getPath();
                // 处理压缩包路径后缀带.zip!/的情况
                if (filePath.endsWith("!/")) {
                    filePath = filePath.substring(0, filePath.length() - 2);
                }
                formFilePathField.getTextField().setText(filePath);
                refreshIpComboBox();
            }
        });

        JPanel ipComboBoxPanel = new JPanel(new BorderLayout());
        // ipComboBoxPanel.setBackground(COMBOBOX_COLOR);
        ipComboBoxPanel.add(ipComboBox, BorderLayout.CENTER);

        String tagName = "<html><body>扫码下载 <b style='color:orange;'>「刷新、重新选择文件、切换网卡均会终止当前传输」</b></body></html>";
        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(5)
                                        .addComponent(createLineLabelPanel(80, "文件路径", formFilePathField))
                                        .addComponent(createLineLabelPanel(80, "网卡切换", ipComboBoxPanel))
                                        .addComponent(createLineLabelPanel(80, "下载地址", downloadTextField))
                                        .addComponentFillVertically(createBoxLabelPanel(tagName, qrLabel), 5)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void manualRefresh() {
        reset();
    }

    private void reset() {
        if (fileServer != null) {
            fileServer.close();
            fileServer = null;
        }
        formFilePathField.setText(null);
        ipComboBox.removeAllItems();
        downloadTextField.setText(null);
        qrLabel.setIcon(null);
    }

    private void refreshIpComboBox() {
        ipComboBox.removeAllItems();
        List<String> ipList = getIpList();
        if (ipList.size() > 1) {
            ipComboBox.addItem("--多网卡，请选择手机同网段IP--");
            for (String ip : ipList) {
                ipComboBox.addItem(ip);
            }
        } else if (ipList.size() == 1) {
            ipComboBox.addItem(ipList.get(0));
            refresh(ipList.get(0));
        }
    }

    @NotNull
    private List<String> getIpList() {
        List<String> ipList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isLoopbackAddress()) {
                        //回路地址，如127.0.0.1
                    } else if (inetAddress.isLinkLocalAddress()) {
                        //169.254.x.x
                    } else {
                        //非链接和回路真实ip
                        ipList.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ipList;
    }

    public void refresh(String ip) {
        // 启动服务
        if (fileServer != null) {
            fileServer.close();
        }

        // 读取空闲的可用端口
        final int port = getPort();

        // 获取filePath
        String filePath = formFilePathField.getText();
        if (StringUtil.isEmpty(filePath)) {
            return;
        }

        // 开启一个web服务，用来下载
        new Thread(() -> {
            try {
                fileServer = new FileServer();
                fileServer.run(port, filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        // 更新二维码
        String qrContent = String.format("http://%s:%s/", ip, port);
        qrLabel.setIcon(generateQrCode(qrContent));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        downloadTextField.setText(qrContent);
    }

    private static int getPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            final int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            throw new RuntimeException("无法获取空闲端口！");
        }
    }

    private Icon generateQrCode(String qrContent) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 300, 300, hints);
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
            BufferedImage croppedImage = image.getSubimage(left, top, right - left, bottom - top);
            return new ImageIcon(croppedImage);
        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败！");
        }
    }
}
