package com.myth.earth.develop.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.myth.earth.develop.kit.PluginNotifyKit;
import com.myth.earth.develop.service.logtosql.MybatisLogParser;
import com.myth.earth.develop.ui.CopyableMessageDialog;
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
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            PluginNotifyKit.warn("Selected text is empty", "Please select mybatis log to proceed.");
            return;
        }

        String sql = MybatisLogParser.parse(selectedText);
        if (!sql.isEmpty()) {
            CopyableMessageDialog.show(sql);
        }
    }
}
