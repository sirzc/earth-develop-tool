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

package com.myth.earth.develop.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ListTableModel;
import com.myth.earth.develop.model.ToolKitInfo;
import com.myth.earth.develop.ui.tabel.BooleanColumnInfo;
import com.myth.earth.develop.ui.tabel.StringColumnInfo;
import com.myth.earth.develop.ui.toolkit.ToolkitGlobalState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ToolKitConfigDialog extends JDialog {

    private final Project                     project;
    private final ListTableModel<ToolKitInfo> listTableModel;
    private       JPanel                      contentPane;
    private       JButton                     buttonOK;
    private       JButton                     buttonCancel;
    private       JBTable                     toolTable;

    public ToolKitConfigDialog(@NotNull Project project, @NotNull List<ToolKitInfo> toolKitInfos) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(500, 400);
        setTitle("工具箱设置");
        setResizable(true);
        setLocationRelativeTo(null);

        // 设置列渲染方式
        BooleanColumnInfo<ToolKitInfo> column0 = new BooleanColumnInfo<>("启用", ToolKitInfo::getEnable, ToolKitInfo::setEnable);
        StringColumnInfo<ToolKitInfo> column1 = new StringColumnInfo<>("工具分组", ToolKitInfo::getCategory);
        StringColumnInfo<ToolKitInfo> column2 = new StringColumnInfo<>("工具名称", ToolKitInfo::getName);

        listTableModel = new ListTableModel<>(column0, column1, column2);
        toolTable.setModel(listTableModel);
        toolTable.setRowHeight(24);

        // 设置首例显示样式
        TableColumn tableColumn = toolTable.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(40);
        tableColumn.setMaxWidth(40);

        // 初始化数据
        toolKitInfos.forEach(listTableModel::addRow);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        // 点击 X 时调用 onCancel()
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // 遇到 ESCAPE 时调用 onCancel()
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // 过滤出关闭的并存储
        List<String> hideToolKits = listTableModel.getItems().stream()
                                                  .filter(t -> !t.getEnable()).map(t -> t.getCategory() + "#" + t.getName())
                                                  .collect(Collectors.toList());
        ToolkitGlobalState.getInstance().setHideToolKits(hideToolKits);
        // 刷新主页信息

        // 在此处添加您的代码
        dispose();
    }

    private void onCancel() {
        // 必要时在此处添加您的代码
        dispose();
    }
}
