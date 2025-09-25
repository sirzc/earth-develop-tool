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

package com.myth.earth.develop.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.common.CommonConst;
import com.myth.earth.develop.kit.ClipboardKit;

import javax.swing.*;
import java.awt.*;

/**
 * JBLabel + JBTextField
 *
 * @author zhouchao
 * @date 2025-09-24 下午4:34
 */
public class LabelTextField extends JPanel {

    private final ExpandableTextField textField;
    private final JBLabel             label;

    public LabelTextField(String tag, boolean openCopy) {
        this(tag, null, openCopy);
    }

    public LabelTextField(String tag, Icon icon, boolean openCopy) {
        super(new BorderLayout());
        setPreferredSize(JBUI.size(0, 35));
        setBorder(IdeBorderFactory.createBorder());

        label = new JBLabel(tag);
        label.setBorder(JBUI.Borders.empty(0, 10));
        if (icon != null) {
            label.setIcon(icon);
        }

        textField = new ExpandableTextField();
        // 添加右框或修改背景色
        // textField.setBorder(new CustomLineBorder(JBUI.insetsLeft(1)));
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setBackground(CommonConst.BACKGROUND_COLOR);
        if (openCopy) {
            textField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineCopyHover, AllIcons.General.InlineCopy, "Copy", () -> {
                String text = textField.getText();
                ClipboardKit.copy(text);
            }));
        }

        add(label, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
    }

    public void setLabelSize(int width, int height) {
        label.setPreferredSize(JBUI.size(width, height));
    }

    public JBTextField getTextField() {
        return textField;
    }

    public JBLabel getLabel() {
        return label;
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }
}