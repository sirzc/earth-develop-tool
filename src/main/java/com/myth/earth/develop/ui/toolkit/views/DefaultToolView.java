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

import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;

import javax.swing.*;

/**
 * 示例
 *
 * @author zhouchao
 * @date 2025-09-07 下午10:23
 */
@Tool(category = ToolCategory.DEFAULT, name = "默认工具", description = "这是一个默认的工具示例")
public class DefaultToolView extends AbstractToolView {

    @Override
    public JComponent centerPanel() {
        return new JPanel();
    }
}
