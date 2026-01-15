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
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.kit.PluginNotifyKit;
import com.myth.earth.develop.service.git.GitCommandExecutor;
import com.myth.earth.develop.service.git.GitException;
import com.myth.earth.develop.service.git.GitStatistics;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.ui.toolkit.core.ToolLevel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Git 代码统计工具
 *
 * @author IngerChao
 * @date 2025-01-15
 */
@Tool(category = ToolCategory.GIT, level = ToolLevel.HIGH, name = "Git代码统计",
        description = "统计项目中一定时间范围的代码提交行数和提交次数")
public class GitStatisticsToolViewImpl extends AbstractToolView {

    private GitCommandExecutor executor;
    private ComboBox<String> timeRangeBox;
    private ComboBox<String> branchBox;
    private JList<String> authorList;
    private JTable resultTable;
    private JBLabel statusLabel;
    private JButton statisticsButton;
    private JButton refreshButton;
    private JButton copyButton;

    public GitStatisticsToolViewImpl(@NotNull Project project) {
        super(project);

        File projectRootFile = new File(project.getBasePath());
        executor = new GitCommandExecutor(projectRootFile);

        // 初始化组件
        timeRangeBox = new ComboBox<>();
        timeRangeBox.addItem("最近7天");
        timeRangeBox.addItem("最近30天");
        timeRangeBox.addItem("最近1年");
        timeRangeBox.addItem("全部");

        branchBox = new ComboBox<>();
        branchBox.addItem("加载中...");

        authorList = new JList<>(); // 使用JList代替ComboBox以支持多选
        authorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        authorList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value != null ? value.toString() : "");
                return this;
            }
        });
        JBScrollPane authorScrollPane = new JBScrollPane(authorList);
        authorScrollPane.setPreferredSize(JBUI.size(-1, 80));

        resultTable = new JTable();
        resultTable.setModel(new DefaultTableModel(
                new String[]{"作者", "提交次数", "增加行数", "删除行数", "修改文件数"},
                0));
        resultTable.setEnabled(false);

        // 配置行排序器
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) resultTable.getModel());
        resultTable.setRowSorter(sorter);

        statusLabel = new JBLabel("初始化中...");

        // 创建按钮
        refreshButton = createButton(50, "刷新", e -> loadBranchAndAuthors());
        statisticsButton = createButton(50, "统计", e -> executeStatistics());
        copyButton = createButton(60, "复制数据", e -> copyTableData());

        // 参数选择区域
        JPanel parameterPanel = new JBPanel<>(new BorderLayout());
        parameterPanel.setBorder(JBUI.Borders.empty(10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JBLabel("时间范围:"));
        topPanel.add(timeRangeBox);
        topPanel.add(new JBLabel("分支:"));
        topPanel.add(branchBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(statisticsButton);
        buttonPanel.add(copyButton);

        JPanel formPanel = FormBuilder.createFormBuilder()
                .addComponent(topPanel)
                .addLabeledComponent("作者（支持多选）:", authorScrollPane)
                .addComponent(buttonPanel)
                .addComponent(statusLabel)
                .getPanel();

        parameterPanel.add(formPanel, BorderLayout.NORTH);

        // 结果显示区域
        JPanel resultPanel = new JBPanel<>(new BorderLayout());
        resultPanel.setBorder(IdeBorderFactory.createTitledBorder("统计结果"));
        JBScrollPane scrollPane = new JBScrollPane(resultTable);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, parameterPanel, resultPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.3);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // 初始化时加载分支和作者
        loadBranchAndAuthors();
    }

    private void loadBranchAndAuthors() {
        statusLabel.setText("加载中...");

        new Thread(() -> {
            try {
                // 加载分支列表
                List<String> branches = executor.getBranches();
                SwingUtilities.invokeLater(() -> {
                    branchBox.removeAllItems();
                    for (String branch : branches) {
                        branchBox.addItem(branch);
                    }
                    if (!branches.isEmpty()) {
                        branchBox.setSelectedIndex(0);
                        loadAuthors();
                    } else {
                        statusLabel.setText("未找到分支");
                    }
                });
            } catch (GitException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("错误: " + e.getMessage());
                    PluginNotifyKit.error(project, e.getMessage());
                });
            }
        }).start();
    }

    private void loadAuthors() {
        String selectedBranch = (String) branchBox.getSelectedItem();
        if (selectedBranch == null) {
            return;
        }

        new Thread(() -> {
            try {
                List<String> authors = executor.getAuthors(selectedBranch, null, null);
                SwingUtilities.invokeLater(() -> {
                    authorList.setListData(authors.toArray(new String[0])); // 设置JList的数据
                });
            } catch (GitException e) {
                statusLabel.setText("无法加载作者列表: " + e.getMessage());
            }
        }).start();
    }

    private void executeStatistics() {
        String selectedBranch = (String) branchBox.getSelectedItem();
        if (selectedBranch == null) {
            PluginNotifyKit.error(project, "请选择分支");
            return;
        }

        Date startDate = getStartDate();
        Date endDate = new Date();

        List<String> selectedAuthors = authorList.getSelectedValuesList(); // 从JList获取选中值

        statusLabel.setText("统计中...");
        statisticsButton.setEnabled(false);

        new Thread(() -> {
            try {
                Map<String, GitStatistics> stats = executor.getStatistics(
                        selectedBranch,
                        startDate,
                        endDate,
                        selectedAuthors.isEmpty() ? null : selectedAuthors);

                SwingUtilities.invokeLater(() -> {
                    if (stats.isEmpty()) {
                        statusLabel.setText("统计完成，共 0 个作者");
                    } else {
                        updateResultTable(stats);
                        statusLabel.setText("统计完成，共 " + stats.size() + " 个作者");
                    }
                    statisticsButton.setEnabled(true);
                });
            } catch (GitException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("统计失败: " + e.getMessage());
                    PluginNotifyKit.error(project, "Git 统计失败：" + e.getMessage());
                    statisticsButton.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("异常: " + e.getMessage());
                    PluginNotifyKit.error(project, "异常：" + e.getMessage());
                    statisticsButton.setEnabled(true);
                });
            }
        }).start();
    }

    private Date getStartDate() {
        String selected = (String) timeRangeBox.getSelectedItem();
        LocalDate today = LocalDate.now();
        LocalDate startLocal;

        switch (Optional.ofNullable(selected).orElse("最近7天")) {
            case "最近7天":
                startLocal = today.minusDays(7);
                break;
            case "最近30天":
                startLocal = today.minusDays(30);
                break;
            case "最近1年":
                startLocal = today.minusYears(1);
                break;
            default:
                startLocal = today.minusYears(10);
                break;
        }
        return java.sql.Date.valueOf(startLocal);
    }

    private void updateResultTable(Map<String, GitStatistics> stats) {
        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
        model.setRowCount(0);

        int totalCommits = 0;
        int totalAdded = 0;
        int totalRemoved = 0;
        int totalFilesModified = 0;

        for (GitStatistics stat : stats.values()) {
            model.addRow(new Object[]{
                    stat.getAuthor(),
                    stat.getCommitCount(),
                    stat.getLinesAdded(),
                    stat.getLinesRemoved(),
                    stat.getFilesModified()
            });
            totalCommits += stat.getCommitCount();
            totalAdded += stat.getLinesAdded();
            totalRemoved += stat.getLinesRemoved();
            totalFilesModified += stat.getFilesModified();
        }

        // 添加总计行
        model.addRow(new Object[]{
                "总计",
                totalCommits,
                totalAdded,
                totalRemoved,
                totalFilesModified
        });

        // 刷新表格显示
        resultTable.revalidate();
        resultTable.repaint();
    }

    private void copyTableData() {
        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
        int rows = model.getRowCount();
        int cols = model.getColumnCount();

        if (rows == 0) {
            PluginNotifyKit.warn(project, "没有数据可复制");
            return;
        }

        StringBuilder sb = new StringBuilder();
        // 表头
        for (int i = 0; i < cols; i++) {
            if (i > 0) {
                sb.append("\t");
            }
            sb.append(model.getColumnName(i));
        }
        sb.append("\n");

        // 数据行
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j > 0) {
                    sb.append("\t");
                }
                sb.append(model.getValueAt(i, j));
            }
            sb.append("\n");
        }

        ClipboardKit.copy(sb.toString());
        PluginNotifyKit.info(project, "已复制到剪贴板");
    }

}
