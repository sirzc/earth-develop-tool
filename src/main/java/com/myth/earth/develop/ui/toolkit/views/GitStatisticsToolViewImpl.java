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
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.WrapLayout;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.service.git.*;
import com.myth.earth.develop.ui.intellij.MyDarculaComboBoxUI;
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
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Git 代码统计工具
 *
 * @author IngerChao
 * @date 2025-01-15
 */
@Tool(category = ToolCategory.GIT, level = ToolLevel.HIGH, name = "Git代码统计", description = "统计项目中一定时间范围的代码提交行数和提交次数")
public class GitStatisticsToolViewImpl extends AbstractToolView {

    private final JPanel                  userWrapPanel;
    private final DefaultTableModel       resultTableModel;
    private final GitCommandExecutor      executor;
    private final GitRepositoryFinder     repositoryFinder;
    private final JLabel                  tipLabel;
    private final JBLoadingPanel          loadingPanel;
    private       List<GitRepository>     repositories       = new ArrayList<>();
    private final ComboBox<GitRepository> repositoryBox;
    private final ComboBox<String>        timeRangeBox;
    private final ComboBox<String>        branchBox;
    private final JBTable                 resultTable;
    private final JButton                 statisticsButton;
    private final JButton                 copyButton;
    private final List<JBCheckBox>        authorCheckBoxList = new ArrayList<>(8);

    public GitStatisticsToolViewImpl(@NotNull Project project) {
        super(project);

        File projectRootFile = new File(project.getBasePath());
        executor = new GitCommandExecutor(projectRootFile);
        repositoryFinder = new GitRepositoryFinder(projectRootFile);

        // 初始化仓库选择组件
        repositoryBox = new ComboBox<>();
        repositoryBox.setUI(new MyDarculaComboBoxUI());

        // 初始化组件
        timeRangeBox = new ComboBox<>();
        timeRangeBox.setUI(new MyDarculaComboBoxUI());
        timeRangeBox.addItem("最近7天");
        timeRangeBox.addItem("最近30天");
        timeRangeBox.addItem("最近1年");
        timeRangeBox.addItem("全部");

        branchBox = new ComboBox<>();
        branchBox.setUI(new MyDarculaComboBoxUI());

        resultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 设置所有单元格不可编辑
            }
        };
        resultTableModel.addColumn("作者");
        resultTableModel.addColumn("提交次数");
        resultTableModel.addColumn("增加行数");
        resultTableModel.addColumn("删除行数");
        resultTableModel.addColumn("修改文件数");

        resultTable = new JBTable(resultTableModel);
        resultTable.getTableHeader().setVisible(true);
        resultTable.setRowHeight(25);
        resultTable.setRowSorter(new TableRowSorter<>(resultTable.getModel()));
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        statisticsButton = createButton(50, "统计", e -> executeStatistics());
        copyButton = createButton(80, "复制数据", e -> copyTableData());
        // 添加仓库选择变更监听器
        repositoryBox.addActionListener(e -> loadBranch());
        branchBox.addActionListener(e -> loadAuthors());

        userWrapPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 6, 6));
        JBScrollPane userViewPanel = createScrollPane(userWrapPanel);
        userViewPanel.setPreferredSize(JBUI.size(-1, 66));

        JPanel gitInfoPanel = new JPanel();
        gitInfoPanel.setLayout(new BoxLayout(gitInfoPanel, BoxLayout.X_AXIS));
        gitInfoPanel.add(createLineLabelPanel(50, "仓库", repositoryBox));
        gitInfoPanel.add(Box.createHorizontalStrut(5));
        gitInfoPanel.add(createLineLabelPanel(50, "分支", branchBox));

        JPanel selectOptionPanel = new JPanel();
        selectOptionPanel.setLayout(new BoxLayout(selectOptionPanel, BoxLayout.X_AXIS));
        selectOptionPanel.add(createLineLabelPanel(50, "范围", timeRangeBox));
        selectOptionPanel.add(Box.createHorizontalStrut(5));
        selectOptionPanel.add(statisticsButton);
        selectOptionPanel.add(Box.createHorizontalStrut(5));
        selectOptionPanel.add(copyButton);

        tipLabel = new JBLabel();
        tipLabel.setText("统计结果");

        JBScrollPane scrollPane = createScrollPane(resultTable);
        scrollPane.setBorder(JBUI.Borders.empty());

        loadingPanel = new JBLoadingPanel(new BorderLayout(), Disposer.newDisposable());
        loadingPanel.setLoadingText("数据加载中...");
        loadingPanel.add(scrollPane);

        JPanel formPanel = FormBuilder.createFormBuilder()
                                      .addComponent(gitInfoPanel)
                                      .addComponent(createLineLabelPanel(50, "作者", userViewPanel))
                                      .addComponent(selectOptionPanel)
                                      .addComponentFillVertically(createBoxLabelPanel(tipLabel, loadingPanel), 10)
                                      .getPanel();

        add(formPanel, BorderLayout.CENTER);

        loadRepositories();
    }

    @Override
    public void manualRefresh() {
        refreshErrorTip("已刷新");
        for (int i = resultTableModel.getRowCount() - 1; i >= 0; i--) {
            resultTableModel.removeRow(i);
        }
        resultTable.revalidate();
        resultTable.repaint();
        loadRepositories();
    }

    /**
     * 扫描并加载项目内所有 Git 仓库
     */
    private void loadRepositories() {
        repositoryBox.removeAllItems();
        repositories = repositoryFinder.findRepositories();
        if (!repositories.isEmpty()) {
            for (GitRepository repo : repositories) {
                repositoryBox.addItem(repo);
            }
            // 自动选择主仓库
            GitRepository mainRepo = repositories.stream().filter(GitRepository::isMainRepository).findFirst().orElse(repositories.get(0));
            repositoryBox.setSelectedItem(mainRepo);
        } else {
            refreshErrorTip("未识别到仓库信息，可刷新重试！");
        }
    }

    private void loadBranch() {
        try {
            branchBox.removeAllItems();
            GitRepository selectedRepo = (GitRepository) repositoryBox.getSelectedItem();
            if (selectedRepo == null) {
                return;
            }
            // 重新加载分支列表
            executor.setWorkingDirectory(selectedRepo.getPath());
            List<String> branches = executor.getBranches();
            for (String branch : branches) {
                branchBox.addItem(branch);
            }

            String currentBranch = executor.getCurrentBranch();
            if (currentBranch != null && branches.contains(currentBranch)) {
                branchBox.setSelectedItem(currentBranch);
            } else if (!branches.isEmpty()) {
                branchBox.setSelectedIndex(0);
            }
        } catch (GitException e) {
            refreshErrorTip("Git获取分支失败：" + e.getMessage());
        }
    }

    private void loadAuthors() {
        try {
            userWrapPanel.removeAll();
            String selectedBranch = (String) branchBox.getSelectedItem();
            if (selectedBranch != null) {
                List<String> authors = executor.getAuthors(selectedBranch, null, null);
                authorCheckBoxList.clear();
                for (String author : authors) {
                    JBCheckBox jbCheckBox = new JBCheckBox(author);
                    jbCheckBox.setSelected(true);
                    userWrapPanel.add(jbCheckBox);
                    authorCheckBoxList.add(jbCheckBox);
                }
            }
        } catch (GitException e) {
            refreshErrorTip("Git获取作者失败：" + e.getMessage());
        } finally {
            userWrapPanel.revalidate();
            userWrapPanel.repaint();
        }
    }

    private void executeStatistics() {
        refreshNormalTip("");
        String selectedBranch = (String) branchBox.getSelectedItem();
        if (selectedBranch == null) {
            refreshErrorTip("请选择分支");
            return;
        }

        if (loadingPanel.isLoading()) {
            return;
        }

        Date startDate = getStartDate();
        Date endDate = new Date();
        List<String> selectedAuthors = authorCheckBoxList.stream().filter(JBCheckBox::isSelected).map(JBCheckBox::getText).collect(Collectors.toList());

        try {
            loadingPanel.startLoading();
            Map<String, GitStatistics> stats = executor.getStatistics(selectedBranch, startDate, endDate, selectedAuthors);
            updateResultTable(stats);
            refreshNormalTip("统计完成");
        } catch (GitException e) {
            refreshErrorTip("Git 统计失败：" + e.getMessage());
        } catch (Exception e) {
            refreshErrorTip("异常：" + e.getMessage());
        } finally {
            loadingPanel.stopLoading();
        }
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

        return Date.from(startLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void updateResultTable(Map<String, GitStatistics> stats) {
        // 清除现有数据
        for (int i = resultTableModel.getRowCount() - 1; i >= 0; i--) {
            resultTableModel.removeRow(i);
        }

        int totalCommits = 0;
        int totalAdded = 0;
        int totalRemoved = 0;
        int totalFilesModified = 0;

        for (GitStatistics stat : stats.values()) {
            resultTableModel.addRow(new Object[] {stat.getAuthor(), stat.getCommitCount(), stat.getLinesAdded(), stat.getLinesRemoved(), stat.getFilesModified()});
            totalCommits += stat.getCommitCount();
            totalAdded += stat.getLinesAdded();
            totalRemoved += stat.getLinesRemoved();
            totalFilesModified += stat.getFilesModified();
        }

        // 添加总计行
        resultTableModel.addRow(new Object[] {"总计", totalCommits, totalAdded, totalRemoved, totalFilesModified});

        // 刷新表格显示
        resultTable.revalidate();
        resultTable.repaint();
    }

    private void copyTableData() {
        int rows = resultTableModel.getRowCount();
        int cols = resultTableModel.getColumnCount();

        if (rows == 0) {
            refreshErrorTip("暂无统计结果");
            return;
        }

        StringBuilder sb = new StringBuilder();
        // 表头
        for (int i = 0; i < cols; i++) {
            if (i > 0) {
                sb.append("\t");
            }
            sb.append(resultTableModel.getColumnName(i));
        }
        sb.append("\n");

        // 数据行
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (j > 0) {
                    sb.append("\t");
                }
                sb.append(resultTableModel.getValueAt(i, j));
            }
            sb.append("\n");
        }

        ClipboardKit.copy(sb.toString());
        refreshNormalTip("已复制");
    }

    private void refreshNormalTip(String tip) {
        beautifyLabel(tipLabel, ColorLevel.GREEN, "统计结果", tip);
    }

    private void refreshErrorTip(String tip) {
        beautifyLabel(tipLabel, ColorLevel.ORANGE, "统计结果", tip);
    }
}
