package com.myth.earth.develop.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * 生成 SQL 结果展示弹窗
 * <p>
 * 展示 MyBatis XML 生成的预执行 SQL，支持一键复制到剪贴板。
 *
 * @author Claude
 * @since 2026/8/3
 */
public class GenerateSqlResultDialog extends DialogWrapper {

    private final String sql;

    public GenerateSqlResultDialog(String sql) {
        super(true);
        this.sql = sql;
        setTitle("Generated SQL");
        setOKButtonText("Copy and Close");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(700, 400));

        JTextArea textArea = new JTextArea(sql);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setMargin(new Insets(8, 8, 8, 8));

        panel.add(new JBScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                new AbstractAction("Copy") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        copyToClipboard();
                    }
                }
        };
    }

    @Override
    public void doOKAction() {
        copyToClipboard();
        super.doOKAction();
    }

    private void copyToClipboard() {
        StringSelection stringSelection = new StringSelection(sql);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

    /**
     * 快捷展示方法
     *
     * @param sql 生成的 SQL 内容
     */
    public static void show(String sql) {
        GenerateSqlResultDialog dialog = new GenerateSqlResultDialog(sql);
        dialog.pack();
        dialog.setSize(700, 400);
        dialog.show();
    }
}
