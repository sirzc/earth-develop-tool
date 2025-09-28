package com.myth.earth.develop.ui.toolkit.views;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;

/**
 * 数值进制转换工具
 *
 * @author Inger
 * @since 2025/9/11
 */
@Tool(category = ToolCategory.NUMBER, name = "进制转换", description = "数值二进制、八进制、十进制、十六进制转换")
public class RadixConversionToolViewImpl extends AbstractToolView {

    private final JBTextArea       inputTextArea;
    private final JBTextArea       outputTextArea;
    private final ComboBox<String> fromRadixBox;
    private final ComboBox<String> toRadixBox;

    public RadixConversionToolViewImpl(@NotNull Project project) {
        super(project);
        // 创建输入和输出文本区域
        inputTextArea = new JBTextArea();
        inputTextArea.setMargin(JBUI.insets(5));
        inputTextArea.setToolTipText("输入需要转换的数值，每行一个");

        outputTextArea = createTextArea();

        // 创建进制选择框
        fromRadixBox = createRadixBox();
        toRadixBox = createRadixBox();

        // 默认选择10进制到16进制
        fromRadixBox.setSelectedItem("10");
        toRadixBox.setSelectedItem("16");

        // 创建转换按钮
        JButton convertButton = createButton("转换", e -> convert());

        // 创建面板布局
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JBLabel("从:"));
        topPanel.add(fromRadixBox);
        topPanel.add(new JBLabel("到:"));
        topPanel.add(toRadixBox);
        topPanel.add(convertButton);

        JPanel centerPanel = FormBuilder.createFormBuilder()
                                        .addComponentFillVertically(createBoxLabelPanel("输入数值（每行一个）", new JBScrollPane(inputTextArea)),5)
                                        .addComponentFillVertically(createBoxLabelPanel("转换结果:", new JBScrollPane(outputTextArea)), 5)
                                        .getPanel();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void convert() {
        String input = inputTextArea.getText();
        if (input == null || input.trim().isEmpty()) {
            outputTextArea.setText("");
            return;
        }

        int fromRadix = Integer.parseInt((String) fromRadixBox.getSelectedItem());
        int toRadix = Integer.parseInt((String) toRadixBox.getSelectedItem());

        StringBuilder result = new StringBuilder();
        String[] lines = input.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]; // 保留原始行内容
            if (!line.isEmpty()) {
                // 提取核心数值部分，去除前后特殊字符
                String coreValue = extractCoreValue(line.trim(), fromRadix);
                if (!coreValue.isEmpty()) {
                    try {
                        // 处理可能的进制前缀
                        String normalizedValue = normalizeValue(coreValue, fromRadix);
                        // 使用BigInteger支持大数转换
                        BigInteger value = new BigInteger(normalizedValue, fromRadix);
                        result.append(value.toString(toRadix));
                    } catch (NumberFormatException e) {
                        result.append("无效数值");
                    }
                } else {
                    result.append("无效数值");
                }
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        outputTextArea.setText(result.toString());
    }

    private String extractCoreValue(String line, int radix) {
        if (line.isEmpty()) {
            return "";
        }

        // 找到第一个有效字符的位置
        int start = 0;
        while (start < line.length() && !isValidRadixChar(line.charAt(start), radix)) {
            start++;
        }

        // 找到最后一个有效字符的位置
        int end = line.length() - 1;
        while (end >= start && !isValidRadixChar(line.charAt(end), radix)) {
            end--;
        }

        if (start > end) {
            return "";
        }

        return line.substring(start, end + 1);
    }

    private String normalizeValue(String value, int radix) {
        switch (radix) {
            case 2:
                // 处理二进制可能的0b前缀
                if (value.toLowerCase().startsWith("0b")) {
                    return value.substring(2);
                }
                break;
            case 8:
                // 处理八进制可能的0前缀
                if (value.startsWith("0") && value.length() > 1 && !value.startsWith("0x") && !value.startsWith("0X")) {
                    // 检查是否真的是八进制格式（只包含0-7）
                    for (int i = 1; i < value.length(); i++) {
                        if (value.charAt(i) < '0' || value.charAt(i) > '7') {
                            return value; // 不是八进制格式，返回原值
                        }
                    }
                    return value;
                }
                break;
            case 16:
                // 处理十六进制可能的0x前缀
                if (value.toLowerCase().startsWith("0x")) {
                    return value.substring(2);
                }
                break;
        }
        return value;
    }

    private boolean isValidRadixChar(char c, int radix) {
        switch (radix) {
            case 2: // 二进制: 0, 1
                return c == '0' || c == '1' || c == 'b' || c == 'B' || c == 'x' || c == 'X';
            case 8: // 八进制: 0-7
                return (c >= '0' && c <= '7') || c == '0' || c == 'x' || c == 'X';
            case 10: // 十进制: 0-9
                return c >= '0' && c <= '9';
            case 16: // 十六进制: 0-9, A-F, a-f
                return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f') || c == 'x' || c == 'X';
            default:
                return false;
        }
    }

    private static @NotNull ComboBox<String> createRadixBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPreferredSize(JBUI.size(60, 35));
        comboBox.addItem("2");
        comboBox.addItem("8");
        comboBox.addItem("10");
        comboBox.addItem("16");
        return comboBox;
    }

}
