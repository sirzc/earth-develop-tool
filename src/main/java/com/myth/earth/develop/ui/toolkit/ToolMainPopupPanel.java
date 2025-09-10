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

package com.myth.earth.develop.ui.toolkit;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.MouseChecker;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.*;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.HtmlPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.util.ui.tree.TreeUtil;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.ui.toolkit.core.ToolView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * tool 主要弹窗
 *
 * @author zhouchao
 * @date 2025-09-09 下午3:41
 */
public class ToolMainPopupPanel extends BorderLayoutPanel implements Disposable, MouseChecker {

    public static final  String                                                 TOOLKIT_TITLE    = "工具箱";
    private static final Color                                                  LINE_COLOR       = new JBColor(Gray._189, Gray._100);
    private static final Color                                                  LABEL_BACKGROUND = new JBColor(Gray._234, new Color(69, 73, 74));
    private final        Project                                                project;
    private final        SearchTextField                                        mySearchField;
    private final        Tree                                                   toolTree;
    private final        JPanel                                                 toolCustomizerPanel;
    private final        HintHtmlLabel                                          hintHtmlLabel;
    private              boolean                                                pinWindow;
    private              JBPopup                                                showPopup;
    private              Map<DefaultMutableTreeNode, Class<? extends ToolView>> toolNodeMapper;

    public ToolMainPopupPanel(Project project, Map<ToolCategory, List<Class<? extends ToolView>>> toolCategoryListMap) {
        this.project = project;
        this.mySearchField = createSearchField();
        this.mySearchField.setVisible(false);
        this.toolCustomizerPanel = new JPanel(new BorderLayout());
        this.hintHtmlLabel = createBottomHint();
        this.toolTree = createToolTree();
        this.toolNodeMapper = new ConcurrentHashMap<>(16);
        initToolTreeAction();
        initToolTreeData(toolCategoryListMap);

        JPanel topLeftPanel = createTopLeftPanel();
        JPanel topRightPanel = createTopRightPanel();
        // 顶部panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new CustomLineBorder(JBUI.insetsBottom(1)));
        topPanel.setOpaque(false);
        topPanel.add(topLeftPanel, BorderLayout.WEST);
        topPanel.add(topRightPanel, BorderLayout.EAST);
        topPanel.add(mySearchField, BorderLayout.SOUTH);
        // 添加移动监听
        WindowMoveListener moveListener = new WindowMoveListener(this);
        topPanel.addMouseListener(moveListener);
        topPanel.addMouseMotionListener(moveListener);
        // 滚动树结构面板
        JScrollPane toolTreeScroll = new JBScrollPane(toolTree);
        toolTreeScroll.setBorder(BorderFactory.createEmptyBorder());
        toolTreeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // 左右可调整面板
        OnePixelSplitter onePixelSplitter = new OnePixelSplitter(false, 0.3f, 0.25f, 0.35f);
        onePixelSplitter.setFirstComponent(toolTreeScroll);
        onePixelSplitter.setSecondComponent(toolCustomizerPanel);
        // 添加内容
        addToTop(topPanel);
        addToCenter(onePixelSplitter);
        addToBottom(hintHtmlLabel);
    }

    private void initToolTreeData(Map<ToolCategory, List<Class<? extends ToolView>>> toolCategoryListMap) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) toolTree.getModel().getRoot();
        for (Map.Entry<ToolCategory, List<Class<? extends ToolView>>> entry : toolCategoryListMap.entrySet()) {
            ToolCategory category = entry.getKey();
            List<Class<? extends ToolView>> tools = entry.getValue();
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            for (Class<? extends ToolView> tool : tools) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(tool.getAnnotation(Tool.class));
                categoryNode.add(child);
                toolNodeMapper.put(child, tool);
            }
            rootNode.add(categoryNode);
        }
        TreeUtil.expandAll(toolTree);
    }

    private void initToolTreeAction() {
        TreeUtil.installActions(toolTree);
        create(e -> TreeUtil.moveDown(toolTree)).registerCustomShortcutSet(KeyEvent.VK_DOWN, 0, mySearchField);
        create(e -> TreeUtil.moveUp(toolTree)).registerCustomShortcutSet(KeyEvent.VK_UP, 0, mySearchField);
        // 添加双击树节点事件
        toolTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) toolTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                   return;
                }

                Object userObject = selectedNode.getUserObject();
                if (userObject instanceof ToolCategory) {
                    refreshHintContent(((ToolCategory) userObject).getName());
                    return;
                }

                Class<? extends ToolView> toolViewClass = toolNodeMapper.get(selectedNode);
                if (toolViewClass != null) {
                    // 刷新提示内容
                    Tool tool = toolViewClass.getAnnotation(Tool.class);
                    refreshHintContent(tool.name() + "：" +tool.description());
                    // 展示具体工具内容
                    if (e.getClickCount() == 2) {
                        ToolView toolView = ToolkitProjectService.getInstance(project).get(toolViewClass);
                        refreshToolCustomizerPanel(toolView.refreshView(project));
                    }
                }
            }
        });
    }

    private Tree createToolTree() {
        Tree tree = new Tree();
        tree.setRootVisible(false);
        tree.setFocusable(false);
        tree.setFocusCycleRoot(true);
        tree.setRowHeight(25);
        tree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();
                if (userObject instanceof ToolCategory) {
                    append(((ToolCategory) userObject).getName());
                    return;
                }

                if (userObject instanceof Tool) {
                    append(((Tool) userObject).name());
                }
            }
        });
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("toolkit")));
        return tree;
    }

    protected JPanel createTopLeftPanel() {
        JBLabel jbLabel = new JBLabel(TOOLKIT_TITLE);
        jbLabel.setBorder(JBUI.Borders.empty(0, 12));
        jbLabel.setPreferredSize(JBUI.size(100, JBUI.scale(29)));
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        jPanel.add(jbLabel);
        return jPanel;
    }

    protected JPanel createTopRightPanel() {
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.X_AXIS));
        res.setOpaque(false);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.addAction(new FixedWindowAction());
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_POPUP + ".toolkit.main.toolbar", actionGroup, true);
        toolbar.setTargetComponent(this);
        toolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);
        toolbar.updateActionsImmediately();

        JComponent toolbarComponent = toolbar.getComponent();
        toolbarComponent.setOpaque(false);
        toolbarComponent.setBorder(JBUI.Borders.empty(2, 18, 2, 2));
        res.add(toolbarComponent);
        return res;
    }

    private SearchTextField createSearchField() {
        SearchTextField searchTextField = new SearchTextField(false);
        Border line = JBUI.Borders.customLine(LINE_COLOR, 1, 0, 1, 0);
        Border border = searchTextField.getTextEditor().getBorder();
        Insets insets = border.getBorderInsets(searchTextField.getTextEditor());
        Border empty = JBUI.Borders.empty(insets.top, insets.left, insets.bottom, insets.right);
        Border merge = JBUI.Borders.merge(line, empty, false);
        searchTextField.getTextEditor().setBorder(merge);
        searchTextField.getTextEditor().setFocusTraversalKeysEnabled(false);
        return searchTextField;
    }

    private HintHtmlLabel createBottomHint() {
        HintHtmlLabel label = new HintHtmlLabel();
        label.setBorder(JBUI.Borders.empty(5));
        label.setBackground(LABEL_BACKGROUND);
        label.setOpaque(true);
        label.setAndUpdateText("可使用搜索快速选择工具！");
        label.setPreferredSize(JBUI.size(0, 25));
        label.updateUI();
        return label;
    }

    protected JBTextField getSearchField() {
        return mySearchField.getTextEditor();
    }

    @Override
    public Dimension getPreferredSize() {
        return JBUI.size(800, 600);
    }

    @Override
    public void dispose() {
        showPopup.cancel();
    }

    @Override
    public boolean check(MouseEvent event) {
        // pinWindow
        return event.getID() == MouseEvent.MOUSE_PRESSED && !pinWindow;
    }

    public void refreshPopup(@NotNull JBPopup popup) {
        this.showPopup = popup;
    }

    public void refreshHintContent(String text) {
        text = text == null ? "" : text;
        hintHtmlLabel.setAndUpdateText(text);
        hintHtmlLabel.updateUI();
    }

    public void refreshToolCustomizerPanel(@NotNull JComponent component) {
        toolCustomizerPanel.add(component, BorderLayout.CENTER);
        toolCustomizerPanel.updateUI();
    }

    private DumbAwareAction create(Consumer<AnActionEvent> action) {

        return new DumbAwareAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                action.accept(e);
            }
        };
    }

    private static class HintHtmlLabel extends HtmlPanel {
        private String detailText = "";

        protected @NotNull String getBody() {
            return detailText == null ? "" : detailText;
        }

        public void setAndUpdateText(String detailText) {
            this.detailText = detailText;
        }

        public void setBody(String text) {
            if (text.isEmpty()) {
                setText("");
            } else {
                setText("<html><head>" + UIUtil.getCssFontDeclaration(UIUtil.getLabelFont().deriveFont((float) (UIUtil.getLabelFont().getSize() - 2)),
                                                                      UIUtil.getLabelForeground(), null, null) + "</head><body>" + text + "</body></html>");
            }
        }
    }

    /**
     * 固定按钮action
     */
    private class FixedWindowAction extends ToggleAction {

        private static final String DESC = "Pin Window";

        public FixedWindowAction() {
            super(DESC, DESC, AllIcons.General.Pin_tab);
            int mask = SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
            registerCustomShortcutSet(KeyEvent.VK_D, mask, ToolMainPopupPanel.this);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return pinWindow;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            pinWindow = state;
        }
    }

}
