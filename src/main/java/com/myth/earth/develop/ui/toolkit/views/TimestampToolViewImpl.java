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

import cn.hutool.core.convert.impl.TimeZoneConverter;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.ZoneUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

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

        ComboBox<String> timeUnitBox1 = createTimeUnitBox();
        ComboBox<String> timeZoneBox2 = createTimeZoneBox();
        JBTextField fromTimeField = createTextField(currentTimeMillis);
        JBTextField toDateField = createTextField(DateUtil.format(currentDate, DatePattern.NORM_DATETIME_PATTERN));
        JButton toDateButton = createButton();
        toDateButton.addActionListener(e -> {
            try {
                String timestampText = fromTimeField.getText().trim();
                if (timestampText.isEmpty()) {
                    return;
                }
                long timestamp = Long.parseLong(timestampText);
                // 根据单位调整时间戳
                if ("s".equals(timeUnitBox1.getSelectedItem())) {
                    timestamp *= 1000;
                }
                // 获取时区
                String zoneIdStr = (String) timeZoneBox2.getSelectedItem();
                ZoneId zoneId = ZoneId.of(zoneIdStr != null ? zoneIdStr : ZoneId.systemDefault().getId());
                // 转换为日期时间并显示
                Instant instant = Instant.ofEpochMilli(timestamp);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
                String formattedDate = formatter.format(instant.atZone(zoneId));
                toDateField.setText(formattedDate);
            } catch (NumberFormatException ex) {
                toDateField.setText("无效的时间戳");
            }
        });

        JPanel toDatePanel = new JPanel(new HorizontalLayout());
        toDatePanel.add(fromTimeField);
        toDatePanel.add(timeUnitBox1);
        toDatePanel.add(toDateButton);
        toDatePanel.add(toDateField);
        toDatePanel.add(timeZoneBox2);

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

    private static @NotNull JButton createButton() {
        JButton toDateButton = new JButton("转换");
        toDateButton.setPreferredSize(JBUI.size(50, 35));
        return toDateButton;
    }

    private static @NotNull JBTextField createTextField(String currentTimeMillis) {
        JBTextField fromTimeField = new JBTextField(currentTimeMillis);
        fromTimeField.setPreferredSize(JBUI.size(150, 35));
        return fromTimeField;
    }

    private static @NotNull ComboBox<String> createTimeZoneBox() {
        // 获取所有可用时区ID
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPreferredSize(JBUI.size(130,35));
        // 将常用的时区优先放在最上（如：Asia/Shanghai、GMT、UTC等），其他安装字母排序
        Set<String> availableZones = ZoneId.getAvailableZoneIds();
        // 定义常用时区列表，优先显示
        String[] commonZones = {"Asia/Shanghai", "GMT", "UTC"};
        // 先添加常用时区
        for (String zone : commonZones) {
            if (availableZones.contains(zone)) {
                comboBox.addItem(zone);
            }
        }
        // 添加剩余时区并按字母排序
        availableZones.stream()
                .filter(zone -> !Arrays.asList(commonZones).contains(zone))
                .sorted()
                .forEach(comboBox::addItem);
        comboBox.setSelectedItem(ZoneId.systemDefault().getId());
        return comboBox;
    }

    private static @NotNull ComboBox<String> createTimeUnitBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPreferredSize(JBUI.size(60,35));
        comboBox.addItem("ms");
        comboBox.addItem("s");
        return comboBox;
    }
}
