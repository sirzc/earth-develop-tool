package com.myth.earth.develop.ui.toolkit.views;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.service.logtosql.MybatisLogParser;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * SQL转换菜单
 *
 * @author Inger
 * @since 2025/10/23
 */
@Tool(category = ToolCategory.SQL, name = "MyBatis日志转SQL", description = "将MyBatis输出的日志快速转换为可执行的SQL语句")
public class SqlConverterToolViewImpl extends AbstractToolView {

    private final JBTextArea inputTextArea;
    private final JBTextArea outputTextArea;
    private final JButton convertButton;


    public SqlConverterToolViewImpl(@NotNull Project project) {
        super(project);
        // 创建输入和输出文本区域
        inputTextArea = new JBTextArea();
        inputTextArea.setMargin(JBUI.insets(5));
        inputTextArea.setToolTipText("请输入MyBatis日志");

        outputTextArea = new JBTextArea();
        outputTextArea.setMargin(JBUI.insets(5));
        outputTextArea.setEditable(false);
        outputTextArea.setToolTipText("转换后的SQL语句");

        // 创建转换按钮
        convertButton = createButton(50, "转换", e -> convert());

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .addComponent(convertButton)
                                        .addComponentFillVertically(createBoxLabelPanel("MyBatis日志输入:", new JBScrollPane(inputTextArea)), 5)
                                        .addComponentFillVertically(createBoxLabelPanel("SQL输出:", new JBScrollPane(outputTextArea)), 5)
                                        .getPanel();
        add(centerPanel, BorderLayout.CENTER);
    }


    private void convert() {
        String input = inputTextArea.getText();
        if (input == null || input.trim().isEmpty()) {
            outputTextArea.setText("");
            return;
        }

        String result = convertMyBatisLogToSql(input);
        outputTextArea.setText(result);
    }

    /**
     * 将MyBatis日志转换为可执行的SQL语句
     *
     * @param myBatisLog MyBatis日志内容
     * @return 转换后的SQL语句
     */
    private String convertMyBatisLogToSql(String myBatisLog) {
        if (myBatisLog == null || myBatisLog.trim().isEmpty()) {
            return "";
        }

        return MybatisLogParser.parse(myBatisLog);
    }

    @Override
    public void manualRefresh() {
        inputTextArea.setText(null);
        outputTextArea.setText(null);
    }
}
