package com.myth.earth.develop.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
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
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        CopyableMessageDialog.show(selectedText);
    }
}
