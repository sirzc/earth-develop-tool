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

package com.myth.earth.develop.ui.toolkit.views;

import com.google.gson.*;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.WrapLayout;
import com.myth.earth.develop.kit.ClipboardKit;
import com.myth.earth.develop.ui.component.MyEditorTextField;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Json格式化工具
 *
 * @author zhouchao
 * @date 2025-09-13 下午4:32
 */
@Tool(category = ToolCategory.DEVELOP, name = "JSON格式化", description = "JSON字符串格式化工具")
public class JsonFormatToolViewImpl extends AbstractToolView {

    public static final String SHOW_TIP = "<html><body><b style='color:orange;'>所有操作与编辑器中快捷键一致，如Win版中：Ctrl + F 搜索、Ctrl + Alt + L 格式化</b></body></html>";
    private final MyEditorTextField myEditorTextField;

    public JsonFormatToolViewImpl(@NotNull Project project) {
        super(project);
        myEditorTextField = new MyEditorTextField(project, JsonFileType.INSTANCE);
        JPanel topPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 5, 5));
        topPanel.add(createButton(65, "格式化", e -> {
            String text = myEditorTextField.getText();
            myEditorTextField.setText(formatJson(text));
        }));
        topPanel.add(createButton(50, "压缩", e -> {
            String text = myEditorTextField.getText();
            myEditorTextField.setText(compressJson(text));
        }));
        topPanel.add(createButton(110, "复制到剪贴板", e -> {
            String text = myEditorTextField.getText();
            ClipboardKit.copy(text);
        }));

        JBLabel bottomPanel = new JBLabel(SHOW_TIP);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new CustomLineBorder(JBUI.insets(1)));
        centerPanel.add(myEditorTextField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 格式化 JSON 字符串（美化、缩进、换行，易读）
     *
     * @param rawJson 输入的 JSON 字符串（可以是压缩或已格式化的）
     * @return 格式化后的 JSON 字符串，如果解析失败则返回原字符串
     */
    public static String formatJson(String rawJson) {
        try {
            // 解析 JSON 字符串为 JsonElement
            JsonElement jsonElement = JsonParser.parseString(rawJson);
            // 使用 GsonBuilder 设置漂亮打印（缩进2个空格）
            Gson prettyGson = new GsonBuilder().setPrettyPrinting()
                                               .create();
            // 返回格式化后的字符串
            return prettyGson.toJson(jsonElement);
        } catch (JsonSyntaxException e) {
            // 如果不是合法 JSON，则返回原字符串
            return rawJson;
        }
    }

    /**
     * 压缩json
     *
     * @param jsonString json字符串
     * @return 压缩后的结果
     */
    public String compressJson(String jsonString) {
        try {
            JsonElement element = JsonParser.parseString(jsonString);
            Gson gson = new Gson();
            return gson.toJson(element);
        } catch (Exception e) {
            return jsonString;
        }
    }

    @Override
    public void manualRefresh() {
        myEditorTextField.setText(null);
    }
}
