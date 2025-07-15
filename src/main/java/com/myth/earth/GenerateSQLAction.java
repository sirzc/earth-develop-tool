package com.myth.earth;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

/**
 * SQL生成选项
 *
 * @author Inger
 * @since 2025/7/15
 */
public class GenerateSQLAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            System.out.println("Editor is null");
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            System.out.println("Selected text is empty");
            return;
        }

        System.out.println("Selected Text:\n" + selectedText);
        // 解析 MyBatis 日志并生成带参数的 SQL
        String sql = MybatisLogParser.parse(selectedText);
        if (!sql.isEmpty()) {
            // 弹窗显示或插入到编辑器等操作
            System.out.println("生成的 SQL:\n" + sql);
            CopyableMessageDialog.show(sql);
        } else {
            System.out.println("无法解析 MyBatis 日志");
        }
    }
}
