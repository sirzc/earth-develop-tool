package com.myth.earth.develop.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.myth.earth.develop.service.logtosql.MybatisLogParser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * 复制内容弹窗
 *
 * @author Inger
 * @since 2025/7/15
 */
public class CopyableMessageDialog extends DialogWrapper {

    private final String    input;
    private final JTextArea outputTextArea;
    private final JTextArea inputTextArea;

    protected CopyableMessageDialog(String input) {
        // use a model dialog
        super(true);
        this.input = input;
        setTitle("Generated SQL");
        setOKButtonText("Copy and Close");

        inputTextArea = new JTextArea(input);
        inputTextArea.setEditable(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setLineWrap(true);

        String sql = MybatisLogParser.parse(input);
        outputTextArea = new JTextArea(sql);
        outputTextArea.setEditable(false);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setLineWrap(true);

        init();
    }

    @Override
    protected JComponent createCenterPanel() {

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Selected content:"), BorderLayout.NORTH);
        leftPanel.add(new JBScrollPane(inputTextArea), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Generated SQL content:"), BorderLayout.NORTH);
        rightPanel.add(new JBScrollPane(outputTextArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(leftPanel);
        panel.add(rightPanel);
        return panel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                new AbstractAction("Generate") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = inputTextArea.getText();
                        String sql = MybatisLogParser.parse(text);
                        outputTextArea.setText(sql);
                    }
                }
        };
    }

    @Override
    public void doOKAction() {
        // 复制到剪贴板
        StringSelection stringSelection = new StringSelection(outputTextArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
        super.doOKAction();
    }

    public static void show(String selectedText) {
        CopyableMessageDialog dialog = new CopyableMessageDialog(selectedText);
        dialog.pack();
        dialog.setSize(800, 400);
        dialog.show();
//        dialog.setLocationRelativeTo(null);
//        dialog.isVisible(true);
    }
}