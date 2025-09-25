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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.common.CommonConst;
import com.myth.earth.develop.other.ConfigTools;
import com.myth.earth.develop.ui.component.LabelTextField;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 文本加解密
 *
 * @author zhouchao
 * @date 2025-09-12 下午10:03
 */
@Tool(category = ToolCategory.ENCODE, name = "Druid加解密", description = "支持Druid加密、解密")
public class DruidToolViewImpl extends AbstractToolView {

    public DruidToolViewImpl(@NotNull Project project) {
        super(project);

        // 加密面板
        LabelTextField privateKeyField = createLabelTextField("privateKey", true);
        LabelTextField publicKeyField = createLabelTextField("publicKey", true);
        LabelTextField encryptResultField = createLabelTextField("password", true);
        LabelTextField encryptPasswordField = createLabelTextField("Input", false);
        JButton encryptButton = new JButton("加密");
        JPanel encryptPanel = new JPanel(new BorderLayout());
        encryptPanel.add(encryptPasswordField, BorderLayout.CENTER);
        encryptPanel.add(encryptButton, BorderLayout.EAST);

        // 解密面板
        LabelTextField decryptPublicKeyField = createLabelTextField("publicKey", false);
        LabelTextField decryptPasswordField = createLabelTextField("password", false);
        LabelTextField decryptResultField = createLabelTextField("Output", true);
        JButton decryptButton = new JButton("解密");
        JPanel decryptPanel = new JPanel(new BorderLayout());
        decryptPanel.add(decryptResultField, BorderLayout.CENTER);
        decryptPanel.add(decryptButton, BorderLayout.EAST);

        JBTextArea propertiesTextArea = createTextArea();
        propertiesTextArea.setEditable(true);
        propertiesTextArea.setPreferredSize(JBUI.size(-1, 120));
        propertiesTextArea.setBackground(CommonConst.BACKGROUND_COLOR);

        JBScrollPane propertiesPanel = new JBScrollPane(propertiesTextArea);
        propertiesPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        propertiesPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel rootPanel = FormBuilder.createFormBuilder()
                                      .setVerticalGap(10)
                                      .addComponent(encryptPanel)
                                      .addComponent(privateKeyField)
                                      .addComponent(publicKeyField)
                                      .addComponent(encryptResultField)
                                      .addLabeledComponent("Properties example:", propertiesPanel, true)
                                      .addSeparator()
                                      .addComponent(decryptPublicKeyField)
                                      .addComponent(decryptPasswordField)
                                      .addComponent(decryptPanel)
                                      .getPanel();

        // 添加事件监听器
        encryptButton.addActionListener(e -> {
            String password = encryptPasswordField.getText();
            if (StringUtils.isNotBlank(password)) {
                try {
                    String[] keyPair = ConfigTools.genKeyPair(512);
                    String encryptedPassword = ConfigTools.encrypt(keyPair[0], password);
                    privateKeyField.setText(keyPair[0]);
                    publicKeyField.setText(keyPair[1]);
                    encryptResultField.setText(encryptedPassword);
                    propertiesTextArea.setText("prefix.publicKey=" + keyPair[1] + "\n" + "prefix.password=" + encryptedPassword);
                } catch (Exception ex) {
                    encryptResultField.setText("加密失败: " + ex.getMessage());
                }
            }
        });

        decryptButton.addActionListener(e -> {
            String publicKey = decryptPublicKeyField.getText();
            String password = decryptPasswordField.getText();
            if (StringUtils.isNotBlank(publicKey) && StringUtils.isNotBlank(password)) {
                try {
                    String decrypted = ConfigTools.decrypt(publicKey, password);
                    decryptResultField.setText(decrypted);
                } catch (Exception ex) {
                    decryptResultField.setText("解密失败: " + ex.getMessage());
                }
            }
        });

        add(rootPanel, BorderLayout.NORTH);
    }

    private LabelTextField createLabelTextField(String tag, boolean openCopy) {
        LabelTextField labelTextField = new LabelTextField(tag, openCopy);
        labelTextField.setLabelSize(85, -1);
        return labelTextField;
    }
}
