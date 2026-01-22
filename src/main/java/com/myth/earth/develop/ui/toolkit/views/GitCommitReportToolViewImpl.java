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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.service.git.*;
import com.myth.earth.develop.ui.intellij.MyDarculaComboBoxUI;
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
import java.util.Optional;

/**
 * Git 提交周报工具
 *
 * @author zhouchao
 * @date 2025-01-15
 */
@Tool(category = ToolCategory.GIT, level = ToolLevel.HIGH, name = "Git提交报告", description = "生成跨仓库的 Git 提交周报，支持自定义时间范围和作者选择")
public class GitCommitReportToolViewImpl extends AbstractToolView {

    private GitCommandExecutor  executor;
    private GitRepositoryFinder repositoryFinder;
    private GitCommitReporter   commitReporter;
    private List<GitRepository> repositories = new ArrayList<>();
    // UI 组件
    private ComboBox<String>    timeRangeBox;
    private JTextField          startDateField;
    private JTextField          endDateField;
    private ComboBox<String>    authorBox;
    private ComboBox<String>    formatBox;
    private JButton             generateButton;
    private JButton             copyButton;
    private JButton             clearButton;
    private JTextArea           reportArea;
    private JBLabel             statusLabel;

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

    @Override
    public void manualRefresh() {
        reportArea.setText("");
        statusLabel.setText("报告内容");
        timeRangeBox.setSelectedIndex(0);
        formatBox.setSelectedIndex(0);
        loadRepositoriesAndAuthors();
    }

    /**
     * 初始化 UI 组件
     */
    private void initializeUIComponents() {
        // 状态标签
        statusLabel = new JBLabel("报告内容");

        // 日期选项
        startDateField = new JTextField(12);
        startDateField.setToolTipText("格式: YYYY-MM-DD");
        endDateField = new JTextField(12);
        endDateField.setToolTipText("格式: YYYY-MM-DD");

        // 时间范围选择
        timeRangeBox = new ComboBox<>();
        timeRangeBox.setUI(new MyDarculaComboBoxUI());
        timeRangeBox.addItem("最近7天");
        timeRangeBox.addItem("最近30天");
        timeRangeBox.addItem("最近1年");
        timeRangeBox.addItem("最近5年");
        timeRangeBox.addActionListener(e -> onTimeRangeChanged());
        timeRangeBox.setSelectedIndex(0);

        // 作者选择
        authorBox = new ComboBox<>();
        authorBox.setUI(new MyDarculaComboBoxUI());

        // 格式选择
        formatBox = new ComboBox<>();
        formatBox.setUI(new MyDarculaComboBoxUI());
        formatBox.addItem("Markdown");
        formatBox.addItem("纯文本");
        formatBox.setSelectedIndex(0);

        // 操作按钮
        generateButton = createButton(80, "生成报告", e -> generateReport());
        copyButton = createButton(50, "复制", e -> Optional.ofNullable(reportArea.getText()).ifPresent(ClipboardKit::copy));
        clearButton = createButton(50, "清空", e -> reportArea.setText(""));

        // 报告显示区域
        reportArea = new JTextArea();
        reportArea.setMargin(JBUI.insets(5));
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
    }

    /**
     * 构建 UI 布局
     */
    private void buildUILayout() {
        JPanel oneLinePanel = new JPanel();
        oneLinePanel.setLayout(new BoxLayout(oneLinePanel, BoxLayout.X_AXIS));
        oneLinePanel.add(createLineLabelPanel(50, "作者", authorBox));
        oneLinePanel.add(Box.createHorizontalStrut(5));
        oneLinePanel.add(createLineLabelPanel(50, "格式", formatBox));

        JPanel twoLinePanel = new JPanel();
        twoLinePanel.setLayout(new BoxLayout(twoLinePanel, BoxLayout.X_AXIS));
        twoLinePanel.add(createLineLabelPanel(50, "开始", startDateField));
        twoLinePanel.add(Box.createHorizontalStrut(5));
        twoLinePanel.add(createLineLabelPanel(50, "结束", endDateField));

        JPanel threeLinePanel = new JPanel();
        threeLinePanel.setLayout(new BoxLayout(threeLinePanel, BoxLayout.X_AXIS));
        threeLinePanel.add(createLineLabelPanel(50, "范围", timeRangeBox));
        threeLinePanel.add(Box.createHorizontalStrut(5));
        threeLinePanel.add(generateButton);
        threeLinePanel.add(Box.createHorizontalStrut(5));
        threeLinePanel.add(copyButton);
        threeLinePanel.add(Box.createHorizontalStrut(5));
        threeLinePanel.add(clearButton);

        JPanel mainPanel = FormBuilder.createFormBuilder()
                                      .setVerticalGap(5)
                                      .addComponent(oneLinePanel)
                                      .addComponent(twoLinePanel)
                                      .addComponent(threeLinePanel)
                                      .addComponentFillVertically(createBoxLabelPanel(statusLabel, createScrollPane(reportArea)), 10)
                                      .getPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * 加载仓库和作者列表
     */
    private void loadRepositoriesAndAuthors() {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
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

            if (authorBox.getItemCount() > 0) {
                authorBox.removeAllItems();
            }

            for (String author : allAuthors) {
                authorBox.addItem(author);
            }

            if (allAuthors.isEmpty()) {
                refreshErrorTip("未找到作者");
            }
        });
    }

    private void onTimeRangeChanged() {
        String selected = (String) timeRangeBox.getSelectedItem();
        if (selected != null) {
            LocalDate now = LocalDate.now();
            switch (selected) {
                case "最近7天":
                    startDateField.setText(now.minusDays(7).toString());
                    endDateField.setText(now.toString());
                    break;
                case "最近30天":
                    startDateField.setText(now.minusDays(30).toString());
                    endDateField.setText(now.toString());
                    break;

                case "最近1年":
                    startDateField.setText(now.minusYears(1).toString());
                    endDateField.setText(now.toString());
                    break;
                case "最近5年":
                    startDateField.setText(now.minusYears(5).toString());
                    endDateField.setText(now.toString());
                    break;
                default:
                    // 如果选择了其他项（如自定义），则不自动设置日期
                    break;
            }
        }
    }

    /**
     * 生成报告
     */
    private void generateReport() {
        String authorName = (String) authorBox.getSelectedItem();
        if (authorName == null) {
            return;
        }

        // 获取时间范围
        LocalDate startDate = parse(startDateField.getText());
        LocalDate endDate = parse(endDateField.getText());
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            refreshErrorTip("开始、结束日期格式内容不合格！");
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                generateButton.setEnabled(false);
                CommitReport report = commitReporter.generateReport(repositories, authorName, startDate, endDate, executor);
                String reportContent;
                String format = (String) formatBox.getSelectedItem();
                if ("纯文本".equals(format)) {
                    reportContent = commitReporter.exportAsPlainText(report);
                } else {
                    reportContent = commitReporter.exportAsMarkdown(report);
                }
                reportArea.setText(reportContent);
            } catch (Exception e) {
                refreshErrorTip("生成失败:" + e.getMessage());
            } finally {
                generateButton.setEnabled(true);
            }
        });
    }

    private LocalDate parse(String text) {
        if (StringUtil.isEmpty(text)) {
            return null;
        }

        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private void refreshErrorTip(String tip) {
        beautifyLabel(statusLabel, ColorLevel.ORANGE, "报告内容", tip);
    }
}
