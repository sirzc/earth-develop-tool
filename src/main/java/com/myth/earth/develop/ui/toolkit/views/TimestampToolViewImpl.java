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
import com.intellij.openapi.project.Project;
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

/**
 * 时间戳转换工具
 *
 * @author zhouchao
 * @date 2025-09-07 下午10:23
 */
@Tool(category = ToolCategory.DEVELOP, name = "时间戳转换", description = "获取时间戳、时间戳转日期时间、日期时间转时间戳")
public class TimestampToolViewImpl extends AbstractToolView {

    private final ExtendableTextField extendableTextField;
    private       ComboBox<String>    timeUnitBox2;
    private       ComboBox<String>    timeZoneBox2;
    private       JBTextField         fromDateTimeField;
    private       JBTextField         toTimestampField;
    private       ComboBox<String>    timeUnitBox1;
    private       ComboBox<String>    timeZoneBox1;
    private       JBTextField         fromTimeField;
    private       JBTextField         toDateField;

    public TimestampToolViewImpl(@NotNull Project project) {
        super(project);
        Date date = new Date();
        String currentDate = DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);
        String currentTimeMillis = String.valueOf(date.getTime());
        // 带图标的按钮
        extendableTextField = new ExtendableTextField();
        extendableTextField.setPreferredSize(JBUI.size(120, 35));
        extendableTextField.addExtension(ExtendableTextComponent.Extension.create(AllIcons.General.InlineCopyHover, AllIcons.General.InlineCopy, "复制", () -> {
            ClipboardKit.copy(extendableTextField.getText());
        }));

        JPanel toDatePanel = createToDatePanel(currentDate, currentTimeMillis);
        JPanel toTimestampPanel = createTimestampPanel(currentDate, currentTimeMillis);

        @SuppressWarnings("all") JPanel centerPanel = FormBuilder.createFormBuilder()
                                                                 .addComponent(new JBLabel("当前时间戳"))
                                                                 .addComponent(extendableTextField)
                                                                 .addComponent(new JBLabel("时间戳转日期时间"), 20)
                                                                 .addComponent(toDatePanel)
                                                                 .addComponent(new JBLabel("日期时间转时间戳"), 20)
                                                                 .addComponent(toTimestampPanel)
                                                                 .getPanel();

        add(centerPanel, BorderLayout.NORTH);
    }

    private @NotNull JPanel createTimestampPanel(String currentDate, String currentTimeMillis) {
        timeUnitBox2 = createTimeUnitBox();
        timeZoneBox2 = createTimeZoneBox();
        fromDateTimeField = createTextField(currentDate);
        toTimestampField = createTextField(currentTimeMillis);
        JButton toTimestampButton = createButton(50, "转换", e -> {
            try {
                String dateTimeText = fromDateTimeField.getText().trim();
                if (dateTimeText.isEmpty()) {
                    return;
                }
                // 获取时区
                String zoneIdStr = (String) timeZoneBox2.getSelectedItem();
                ZoneId zoneId = ZoneId.of(zoneIdStr != null ? zoneIdStr : ZoneId.systemDefault().getId());
                // 解析日期时间并转换为时间戳
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeText, formatter);
                long timestamp = localDateTime.atZone(zoneId).toInstant().toEpochMilli();
                // 根据单位调整时间戳
                if ("s".equals(timeUnitBox2.getSelectedItem())) {
                    timestamp /= 1000;
                }
                toTimestampField.setText(String.valueOf(timestamp));
            } catch (Exception ex) {
                toTimestampField.setText("无效的日期时间");
            }
        });

        JPanel toTimestampPanel = new JPanel();
        toTimestampPanel.setLayout(new BoxLayout(toTimestampPanel, BoxLayout.X_AXIS));
        toTimestampPanel.add(fromDateTimeField);
        toTimestampPanel.add(timeZoneBox2);
        toTimestampPanel.add(toTimestampButton);
        toTimestampPanel.add(toTimestampField);
        toTimestampPanel.add(timeUnitBox2);
        return toTimestampPanel;
    }

    private @NotNull JPanel createToDatePanel(String currentDate, String currentTimeMillis) {
        timeUnitBox1 = createTimeUnitBox();
        timeZoneBox1 = createTimeZoneBox();
        fromTimeField = createTextField(currentTimeMillis);
        toDateField = createTextField(currentDate);
        JButton toDateButton = createButton(50, "转换", e -> {
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
                String zoneIdStr = (String) timeZoneBox1.getSelectedItem();
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

        JPanel toDatePanel = new JPanel();
        toDatePanel.setLayout(new BoxLayout(toDatePanel, BoxLayout.X_AXIS));
        toDatePanel.add(fromTimeField);
        toDatePanel.add(timeUnitBox1);
        toDatePanel.add(toDateButton);
        toDatePanel.add(toDateField);
        toDatePanel.add(timeZoneBox1);
        return toDatePanel;
    }

    @Override
    public void refreshToolData() {
        extendableTextField.setText(String.valueOf(System.currentTimeMillis()));
    }

    private @NotNull JBTextField createTextField(String currentTimeMillis) {
        // fromTimeField.setPreferredSize(JBUI.size(150, 35));
        return new JBTextField(currentTimeMillis);
    }

    private @NotNull ComboBox<String> createTimeZoneBox() {
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
        comboBox.setPreferredSize(JBUI.size(65,35));
        comboBox.addItem("ms");
        comboBox.addItem("s");
        return comboBox;
    }

    @Override
    public void manualRefresh() {
        Date date = new Date();
        String dateFormat = DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);
        String time = String.valueOf(date.getTime());
        extendableTextField.setText(time);
        // 设置时间戳转日期时间部分的默认值
        timeUnitBox1.setSelectedItem("ms");
        timeZoneBox1.setSelectedItem("Asia/Shanghai");
        fromTimeField.setText(time);
        toDateField.setText(dateFormat);
        // 设置日期时间转时间戳部分的默认值
        timeUnitBox2.setSelectedItem("ms");
        timeZoneBox2.setSelectedItem("Asia/Shanghai");
        fromDateTimeField.setText(dateFormat);
        toTimestampField.setText(time);
    }
}
