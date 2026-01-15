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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.kit.PluginNotifyKit;
import com.myth.earth.develop.service.git.*;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.ui.toolkit.core.ToolLevel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Git 提交周报工具
 *
 * @author zhouchao
 * @date 2025-01-15
 */
@Tool(category = ToolCategory.GIT, level = ToolLevel.HIGH, name = "Git提交报告",
        description = "生成跨仓库的 Git 提交周报，支持自定义时间范围和作者选择")
public class GitCommitReportToolViewImpl extends AbstractToolView {

    private GitCommandExecutor executor;
    private GitRepositoryFinder repositoryFinder;
    private GitCommitReporter commitReporter;
    private List<GitRepository> repositories = new ArrayList<>();

    // UI 组件
    private ComboBox<String> timeRangeBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton dateStartPickerButton;
    private JButton dateEndPickerButton;
    private JPanel customDatePanel;
    private ComboBox<String> authorBox;
    private ComboBox<String> formatBox;
    private JButton generateButton;
    private JButton copyButton;
    private JButton exportButton;
    private JButton clearButton;
    private JTextArea reportArea;
    private JBLabel statusLabel;

    public GitCommitReportToolViewImpl(@NotNull Project project) {
        super(project);

        File projectRootFile = new File(project.getBasePath());
        executor = new GitCommandExecutor(projectRootFile);
        repositoryFinder = new GitRepositoryFinder(projectRootFile);
        commitReporter = new GitCommitReporter();

        // 初始化 UI 组件
        initializeUIComponents();

        // 构建 UI 布局
        buildUILayout();

        // 初始化时加载仓库和作者列表
        loadRepositoriesAndAuthors();
    }

    /**
     * 初始化 UI 组件
     */
    private void initializeUIComponents() {
        // 时间范围选择
        timeRangeBox = new ComboBox<>();
        timeRangeBox.addItem("最近7天");
        timeRangeBox.addItem("最近30天");
        timeRangeBox.addItem("最近1年");
        timeRangeBox.addItem("全部");
        timeRangeBox.addItem("自定义日期");
        timeRangeBox.setSelectedIndex(0);
        timeRangeBox.addActionListener(e -> onTimeRangeChanged());

        // 自定义日期面板（默认隐藏）
        customDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startDateField = new JTextField(12);
        startDateField.setToolTipText("格式: YYYY-MM-DD");
        endDateField = new JTextField(12);
        endDateField.setToolTipText("格式: YYYY-MM-DD");
        dateStartPickerButton = createButton(20, "📅", e -> pickStartDate());
        dateEndPickerButton = createButton(20, "📅", e -> pickEndDate());

        customDatePanel.add(new JBLabel("起始日期:"));
        customDatePanel.add(startDateField);
        customDatePanel.add(dateStartPickerButton);
        customDatePanel.add(new JBLabel("终止日期:"));
        customDatePanel.add(endDateField);
        customDatePanel.add(dateEndPickerButton);
        customDatePanel.setVisible(false);

        // 作者选择
        authorBox = new ComboBox<>();
        authorBox.addItem("加载中...");

        // 格式选择
        formatBox = new ComboBox<>();
        formatBox.addItem("Markdown");
        formatBox.addItem("纯文本");
        formatBox.setSelectedIndex(0);

        // 操作按钮
        generateButton = createButton(60, "生成报告", e -> generateReport());
        copyButton = createButton(60, "复制", e -> copyReport());
        exportButton = createButton(60, "导出文件", e -> exportReport());
        clearButton = createButton(60, "清空", e -> clearReport());

        // 报告显示区域
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        // 状态标签
        statusLabel = new JBLabel("初始化中...");
    }

    /**
     * 构建 UI 布局
     */
    private void buildUILayout() {
        // 时间范围面板
        JPanel timeRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeRangePanel.add(new JBLabel("时间范围:"));
        timeRangePanel.add(timeRangeBox);

        // 作者选择面板
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authorPanel.add(new JBLabel("作者:"));
        authorPanel.add(authorBox);

        // 格式选择面板
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formatPanel.add(new JBLabel("格式:"));
        formatPanel.add(formatBox);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generateButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);

        // 参数选择面板
        JPanel parameterPanel = new JBPanel<>(new BorderLayout());
        parameterPanel.setBorder(IdeBorderFactory.createTitledBorder("参数设置"));

        JPanel innerPanel = FormBuilder.createFormBuilder()
                .addComponent(timeRangePanel)
                .addComponent(customDatePanel)
                .addComponent(authorPanel)
                .addComponent(formatPanel)
                .addComponent(buttonPanel)
                .addComponent(statusLabel)
                .getPanel();
        parameterPanel.add(innerPanel, BorderLayout.NORTH);

        // 报告显示面板
        JPanel reportPanel = new JBPanel<>(new BorderLayout());
        reportPanel.setBorder(IdeBorderFactory.createTitledBorder("报告内容"));
        JBScrollPane scrollPane = new JBScrollPane(reportArea);
        reportPanel.add(scrollPane, BorderLayout.CENTER);

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, parameterPanel, reportPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * 加载仓库和作者列表
     */
    private void loadRepositoriesAndAuthors() {
        statusLabel.setText("扫描仓库和作者中...");

        new Thread(() -> {
            try {
                repositories = repositoryFinder.findRepositories();

                // 收集所有作者
                List<String> allAuthors = new ArrayList<>();
                for (GitRepository repo : repositories) {
                    try {
                        executor.setWorkingDirectory(repo.getPath());
                        List<String> repoAuthors = executor.getAuthors("HEAD", null, null);
                        for (String author : repoAuthors) {
                            if (!allAuthors.contains(author)) {
                                allAuthors.add(author);
                            }
                        }
                    } catch (GitException e) {
                        // 继续处理其他仓库
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    // 更新作者下拉框
                    authorBox.removeAllItems();
                    for (String author : allAuthors) {
                        authorBox.addItem(author);
                    }

                    if (allAuthors.isEmpty()) {
                        statusLabel.setText("未找到作者");
                    } else {
                        statusLabel.setText("就绪");
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    PluginNotifyKit.error(project, "加载作者列表失败: " + e.getMessage());
                    statusLabel.setText("加载失败");
                });
            }
        }).start();
    }

    /**
     * 时间范围选择变更事件
     */
    private void onTimeRangeChanged() {
        String selected = (String) timeRangeBox.getSelectedItem();
        if ("自定义日期".equals(selected)) {
            customDatePanel.setVisible(true);
            // 设置默认值
            if (startDateField.getText().isEmpty()) {
                startDateField.setText(LocalDate.now().minusDays(7).toString());
                endDateField.setText(LocalDate.now().toString());
            }
        } else {
            customDatePanel.setVisible(false);
        }
    }

    /**
     * 打开起始日期选择器
     */
    private void pickStartDate() {
        // TODO: 实现日期选择器
        PluginNotifyKit.info(project, "日期选择器功能待实现");
    }

    /**
     * 打开终止日期选择器
     */
    private void pickEndDate() {
        // TODO: 实现日期选择器
        PluginNotifyKit.info(project, "日期选择器功能待实现");
    }

    /**
     * 生成报告
     */
    private void generateReport() {
        String authorName = (String) authorBox.getSelectedItem();
        if (authorName == null || "加载中...".equals(authorName)) {
            PluginNotifyKit.warn(project, "请选择作者");
            return;
        }

        // 获取时间范围
        LocalDate startDate = null;
        LocalDate endDate = LocalDate.now();

        String timeRange = (String) timeRangeBox.getSelectedItem();
        if ("自定义日期".equals(timeRange)) {
            // 验证自定义日期
            try {
                startDate = LocalDate.parse(startDateField.getText());
                endDate = LocalDate.parse(endDateField.getText());
                if (startDate.isAfter(endDate)) {
                    PluginNotifyKit.error(project, "起始日期不能晚于终止日期");
                    return;
                }
            } catch (Exception e) {
                PluginNotifyKit.error(project, "请输入有效的日期格式 (YYYY-MM-DD)");
                return;
            }
        } else {
            // 快捷选项
            switch (timeRange) {
                case "最近7天":
                    startDate = endDate.minusDays(7);
                    break;
                case "最近30天":
                    startDate = endDate.minusDays(30);
                    break;
                case "最近1年":
                    startDate = endDate.minusYears(1);
                    break;
                case "全部":
                    startDate = null;
                    break;
            }
        }

        statusLabel.setText("正在生成报告...");
        generateButton.setEnabled(false);

        final LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;
        new Thread(() -> {
            try {
                CommitReport report = commitReporter.generateReport(
                        repositories, authorName, finalStartDate, finalEndDate, executor);

                String reportContent;
                String format = (String) formatBox.getSelectedItem();
                if ("纯文本".equals(format)) {
                    reportContent = commitReporter.exportAsPlainText(report);
                } else {
                    reportContent = commitReporter.exportAsMarkdown(report);
                }

                SwingUtilities.invokeLater(() -> {
                    reportArea.setText(reportContent);
                    statusLabel.setText("报告已生成");
                    generateButton.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    PluginNotifyKit.error(project, "生成报告失败: " + e.getMessage());
                    statusLabel.setText("生成失败");
                    generateButton.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * 复制报告
     */
    private void copyReport() {
        String content = reportArea.getText();
        if (content.isEmpty()) {
            PluginNotifyKit.warn(project, "没有报告内容可复制");
            return;
        }

        ClipboardKit.copy(content);
        PluginNotifyKit.info(project, "已复制到剪贴板");
    }

    /**
     * 导出报告为文件
     */
    private void exportReport() {
        String content = reportArea.getText();
        if (content.isEmpty()) {
            PluginNotifyKit.warn(project, "没有报告内容可导出");
            return;
        }

        // TODO: 实现文件导出功能
        PluginNotifyKit.info(project, "文件导出功能待实现");
    }

    /**
     * 清空报告
     */
    private void clearReport() {
        reportArea.setText("");
        statusLabel.setText("已清空");
    }


}
