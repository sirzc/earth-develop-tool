package com.myth.earth.develop.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.myth.earth.develop.service.MybatisSqlGenerator;
import com.myth.earth.develop.ui.GenerateSqlResultDialog;
import com.myth.earth.develop.utils.MybatisXmlParser;
import org.w3c.dom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 在 MyBatis XML 文件中右键 statement id 生成预执行 SQL 的 Action。
 * <p>
 * 注册在编辑器右键菜单（EditorPopup）中，仅当光标位于 statement 标签
 * （select/insert/update/delete）的 id 属性上时可用。
 *
 * @author Claude
 * @since 2026/8/3
 */
public class MybatisGenerateSqlAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file == null) {
            return;
        }

        try {
            String content = new String(file.contentsToByteArray(), file.getCharset());
            int offset = editor.getCaretModel().getOffset();

            // 获取 statement id
            String statementId = MybatisXmlParser.findStatementIdAtOffset(content, offset);
            if (statementId == null) {
                return;
            }

            // 解析 XML
            org.w3c.dom.Document xmlDoc = MybatisXmlParser.parseXml(content);
            Element stmtEl = MybatisXmlParser.findStatementById(xmlDoc, statementId);
            if (stmtEl == null) {
                Messages.showWarningDialog(project,
                        "未找到 id 为 '" + statementId + "' 的 statement",
                        "Generate SQL");
                return;
            }

            // 获取 sql 片段映射
            Map<String, Element> sqlFragments = MybatisXmlParser.getSqlFragments(xmlDoc);

            // 生成 SQL
            MybatisSqlGenerator generator = new MybatisSqlGenerator();
            String sql = generator.generate(stmtEl, sqlFragments);

            // 展示结果
            GenerateSqlResultDialog.show(sql);

        } catch (Exception ex) {
            Messages.showErrorDialog(project,
                    "生成 SQL 失败：" + ex.getMessage(),
                    "Generate SQL Error");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = editor != null ? FileDocumentManager.getInstance().getFile(editor.getDocument()) : null;

        // 仅对 XML 文件显示此 Action
        boolean visible = file != null
                && file.getName().toLowerCase().endsWith(".xml")
                && isCursorOnStatementId(editor);

        e.getPresentation().setEnabledAndVisible(visible);
    }

    /**
     * 检测光标是否位于 statement 标签的 id 属性值上。
     * <p>
     * 使用文本扫描方式（非 XML 解析），适合高频调用。
     */
    private boolean isCursorOnStatementId(Editor editor) {
        if (editor == null) {
            return false;
        }
        Document document = editor.getDocument();
        String content = document.getText();
        int offset = editor.getCaretModel().getOffset();
        return MybatisXmlParser.isOffsetOnStatementId(content, offset);
    }
}
