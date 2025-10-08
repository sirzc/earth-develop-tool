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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 文本加解密
 *
 * @author zhouchao
 * @date 2025-09-12 下午10:03
 */
@Tool(category = ToolCategory.ENCODE, name = "文本加解密", description = "支持Base64、Url、MD5、AES、DES、SHA1、SHA256")
public class TextEnDecodeToolViewImpl extends AbstractToolView {

    /**
     * 支持签名
     */
    private static final List<String>     SUPPORT_SIGN   = Arrays.asList("AES", "DES", "SHA1", "SHA256");
    private static final List<String>     ENCODE_OPTIONS = Arrays.asList("BASE64", "URL(UTF-8)", "URL(GBK)", "MD5", "AES", "DES", "SHA1", "SHA256");
    private static final List<String>     DECODE_OPTIONS = Arrays.asList("BASE64", "URL(UTF-8)", "URL(GBK)", "AES", "DES");
    private final        JBTextArea       inputTextArea;
    private final        JBTextArea       outputTextArea;
    private final        JBTextField      signTextField;
    private final        ComboBox<String> optionBox;
    private final        JBRadioButton    encodeRadio;
    private final        JBRadioButton    decodeRadio;

    public TextEnDecodeToolViewImpl(@NotNull Project project) {
        super(project);

        inputTextArea = createTextArea();
        inputTextArea.setEditable(true);

        outputTextArea = createTextArea();

        signTextField = new JBTextField();
        signTextField.setPreferredSize(new Dimension(-1, 35));

        JPanel signPanel = createLineLabelPanel(80, "密钥", signTextField);
        signPanel.setVisible(false);

        optionBox = new ComboBox<>();
        optionBox.setBorder(BorderFactory.createEmptyBorder());
        optionBox.setBackground(COMBOBOX_COLOR);
        ENCODE_OPTIONS.forEach(optionBox::addItem);

        optionBox.addActionListener(e -> {
            String selected = (String) optionBox.getSelectedItem();
            signPanel.setVisible(SUPPORT_SIGN.contains(selected));
        });

        encodeRadio = new JBRadioButton("加密 ", true);
        decodeRadio = new JBRadioButton("解密 ");
        encodeRadio.addActionListener(e -> {
            String selected = (String) optionBox.getSelectedItem();
            optionBox.removeAllItems();
            ENCODE_OPTIONS.forEach(optionBox::addItem);
            if (selected != null && ENCODE_OPTIONS.contains(selected)) {
                optionBox.setSelectedItem(selected);
            }
            signPanel.setVisible(selected != null && SUPPORT_SIGN.contains(selected));
        });

        decodeRadio.addActionListener(e -> {
            String selected = (String) optionBox.getSelectedItem();
            optionBox.removeAllItems();
            DECODE_OPTIONS.forEach(optionBox::addItem);
            if (selected != null && DECODE_OPTIONS.contains(selected)) {
                optionBox.setSelectedItem(selected);
            }
            signPanel.setVisible(selected != null && SUPPORT_SIGN.contains(selected));
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(encodeRadio);
        buttonGroup.add(decodeRadio);

        JPanel optionBoxPanel = new JPanel(new BorderLayout());
        optionBoxPanel.setBackground(COMBOBOX_COLOR);
        optionBoxPanel.add(optionBox, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new HorizontalLayout());
        topPanel.setBorder(JBUI.Borders.empty());
        topPanel.add(createLineLabelPanel(80, "加密方式", optionBoxPanel));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(encodeRadio);
        topPanel.add(decodeRadio);
        topPanel.add(createButton(50, "转换", e -> change()));

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .setVerticalGap(5)
                                        .addComponent(topPanel)
                                        .addComponent(signPanel)
                                        .addComponentFillVertically(createBoxLabelPanel("输入", createScrollPane(inputTextArea)), 5)
                                        .addComponentFillVertically(createBoxLabelPanel("输出", createScrollPane(outputTextArea)), 5)
                                        .getPanel();

        add(centerPanel, BorderLayout.CENTER);
    }

    private void change() {
        String selected = (String) optionBox.getSelectedItem();
        String input = inputTextArea.getText();
        String output = "";
        String sign = signTextField.getText();

        if (encodeRadio.isSelected()) {
            // 加密逻辑
            switch (selected) {
                case "BASE64":
                    output = Base64.encode(input);
                    break;
                case "URL(UTF-8)":
                    output = URLEncodeUtil.encode(input, CharsetUtil.CHARSET_UTF_8);
                    break;
                case "URL(GBK)":
                    output = URLEncodeUtil.encode(input, CharsetUtil.CHARSET_GBK);
                    break;
                case "MD5":
                    output = DigestUtil.md5Hex(input);
                    break;
                case "AES":
                    if (!sign.isEmpty()) {
                        try {
                            byte[] keyBytes = expandKey(sign.getBytes(StandardCharsets.UTF_8), 16);
                            output = SecureUtil.aes(keyBytes).encryptHex(input);
                        } catch (Exception ex) {
                            output = "加密失败: " + ex.getMessage();
                        }
                    } else {
                        output = "请输入密钥";
                    }
                    break;
                case "DES":
                    if (!sign.isEmpty()) {
                        try {
                            byte[] keyBytes = expandKey(sign.getBytes(StandardCharsets.UTF_8), 8);
                            output = SecureUtil.des(keyBytes).encryptHex(input);
                        } catch (Exception ex) {
                            output = "加密失败: " + ex.getMessage();
                        }
                    } else {
                        output = "请输入密钥";
                    }
                    break;
                case "SHA1":
                    output = DigestUtil.sha1Hex(input);
                    break;
                case "SHA256":
                    output = DigestUtil.sha256Hex(input);
                    break;
                default:
                    output = "不支持的加密方式";
            }
        } else {
            // 解密逻辑
            switch (selected) {
                case "BASE64":
                    try {
                        output = Base64.decodeStr(input);
                    } catch (Exception ex) {
                        output = "解密失败: " + ex.getMessage();
                    }
                    break;
                case "URL(UTF-8)":
                    output = URLDecoder.decode(input, CharsetUtil.CHARSET_UTF_8);
                    break;
                case "URL(GBK)":
                    output = URLDecoder.decode(input, CharsetUtil.CHARSET_GBK);
                    break;
                case "AES":
                    if (!sign.isEmpty()) {
                        try {
                            byte[] keyBytes = expandKey(sign.getBytes(StandardCharsets.UTF_8), 16);
                            output = SecureUtil.aes(keyBytes).decryptStr(input);
                        } catch (Exception ex) {
                            output = "解密失败: " + ex.getMessage();
                        }
                    } else {
                        output = "请输入密钥";
                    }
                    break;
                case "DES":
                    if (!sign.isEmpty()) {
                        try {
                            byte[] keyBytes = expandKey(sign.getBytes(StandardCharsets.UTF_8), 8);
                            output = SecureUtil.des(keyBytes).decryptStr(input);
                        } catch (Exception ex) {
                            output = "解密失败: " + ex.getMessage();
                        }
                    } else {
                        output = "请输入密钥";
                    }
                    break;
                default:
                    output = "不支持的解密方式";
            }
        }

        outputTextArea.setText(output);
    }

    /**
     * 扩展密钥到指定长度
     *
     * @param keyBytes 原始密钥字节数组
     * @param length   目标长度
     * @return 扩展后的密钥字节数组
     */
    private byte[] expandKey(byte[] keyBytes, int length) {
        byte[] result = new byte[length];
        if (keyBytes.length > length) {
            System.arraycopy(keyBytes, 0, result, 0, length);
        } else {
            int index = 0;
            for (int i = 0; i < length; i++) {
                if (index >= keyBytes.length) {
                    index = 0;
                }
                result[i] = keyBytes[index++];
            }
        }
        return result;
    }

    @Override
    public void manualRefresh() {
        inputTextArea.setText(null);
        outputTextArea.setText(null);
    }
}
