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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;

import javax.swing.*;
import java.awt.*;

/**
 * 示例
 *
 * @author zhouchao
 * @date 2025-09-07 下午10:23
 */
@Tool(category = ToolCategory.TIME, name = "时间戳转换", description = "获取时间戳、时间戳转日期时间、日期时间转时间戳")
public class TimestampToolViewImpl extends AbstractToolView {

    private final ExtendableTextField extendableTextField;

    public TimestampToolViewImpl() {
        // 带图标的按钮
        extendableTextField = new ExtendableTextField();
        extendableTextField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineCopyHover, AllIcons.General.InlineCopy, "复制", () -> {
            ClipboardKit.copy(extendableTextField.getText());
        }));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JBLabel("当前时间戳："), BorderLayout.WEST);
        topPanel.add(extendableTextField, BorderLayout.CENTER);
        // topPanel.add(Box.createHorizontalStrut(200), BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    @Override
    public void refreshToolData() {
        extendableTextField.setText(String.valueOf(System.currentTimeMillis()));
    }
}
