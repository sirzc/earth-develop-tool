package com.myth.earth.develop.ui;

import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 复制内容弹窗
 *
 * @author Inger
 * @since 2025/7/15
 */
public class CopyableMessageDialog extends DialogWrapper {

    private final String message;

    protected CopyableMessageDialog(String message) {
        // use a model dialog
        super(true);
        this.message = message;
        setTitle("Generated SQL");
        setOKButtonText("Copy and Close");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Generated SQL content:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void doOKAction() {
        // 复制到剪贴板
        StringSelection stringSelection = new StringSelection(message);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
        super.doOKAction();
    }

    public static void show(String sql) {
        CopyableMessageDialog dialog = new CopyableMessageDialog(sql);
        dialog.pack();
        dialog.setSize(600, 400);
        dialog.show();
//        dialog.setLocationRelativeTo(null);
//        dialog.isVisible(true);
    }
}