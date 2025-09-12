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

package com.myth.earth.develop.ui.toolkit.core;

import lombok.Getter;

import javax.swing.*;

/**
 * 工具分组
 *
 * @author zhouchao
 * @date 2025-09-07 下午9:25
 */
@Getter
public enum ToolCategory {

    DEFAULT("默认", null),
    DEVELOP("开发工具", null),
    NUMBER("数字工具", null),
    NETWORK("网络工具", null),
    IMAGE("图像工具", null)
    ;

    /**
     * 显示名称
     */
    private String name;

    /**
     * 显示图标
     */
    private Icon   icon;

    ToolCategory(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }
}
