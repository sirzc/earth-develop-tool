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

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.GotItTooltip;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

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
        Date currentDate = new Date();
        String currentTimeMillis = String.valueOf(currentDate.getTime());
        // 带图标的按钮
        extendableTextField = new ExtendableTextField();
        extendableTextField.setPreferredSize(JBUI.size(120, 35));
        extendableTextField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineCopyHover, AllIcons.General.InlineCopy, "复制", () -> {
            ClipboardKit.copy(extendableTextField.getText());
        }));

        ComboBox<String> timeUnitBox = new ComboBox<>();
        timeUnitBox.setPreferredSize(JBUI.size(80,35));
        timeUnitBox.addItem("毫秒（ms）");
        timeUnitBox.addItem("秒（s）");

        ComboBox<String> timeZoneBox = new ComboBox<>();
        timeZoneBox.setPreferredSize(JBUI.size(120,35));
        timeZoneBox.addItem("Asia/Shanghai");
        timeZoneBox.addItem("UTC");
        timeZoneBox.addItem("GMT");
        timeZoneBox.addItem("Asia/Tokyo");
        timeZoneBox.addItem("America/New_York");
        timeZoneBox.addItem("Europe/London");

        JBTextField fromTimeField = new JBTextField(currentTimeMillis);
        fromTimeField.setPreferredSize(JBUI.size(150, 35));

        JBTextField toDateField = new JBTextField(DateUtil.format(currentDate, DatePattern.NORM_DATETIME_PATTERN));
        toDateField.setPreferredSize(JBUI.size(150, 35));

        JButton toDateButton = new JButton("转换");
        toDateButton.setPreferredSize(JBUI.size(50, 35));

        JPanel toDatePanel = new JPanel(new HorizontalLayout());
        toDatePanel.add(fromTimeField);
        toDatePanel.add(timeUnitBox);
        toDatePanel.add(toDateButton);
        toDatePanel.add(toDateField);
        toDatePanel.add(timeZoneBox);

        @SuppressWarnings("all")
        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .addComponent(new JBLabel("当前时间戳"))
                                        .addComponent(extendableTextField)
                                        .addComponent(new JBLabel("时间戳转日期时间"), 20)
                                        .addComponent(toDatePanel)
                                        .addComponent(new JBLabel("日期时间转时间戳"),20)
                                        .addComponent(new JPanel())
                                        .getPanel();

        add(centerPanel, BorderLayout.NORTH);
    }

    @Override
    public void refreshToolData() {
        extendableTextField.setText(String.valueOf(System.currentTimeMillis()));
    }
}
