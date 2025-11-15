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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局配置存储
 *
 * @author zhouchao
 * @date 2025-11-15 下午5:14
 */
@State(name = "com.myth.earth.develop.ui.toolkit.ToolkitGlobalState", storages = {@Storage("EarthToolkitGlobalState.xml")})
public class ToolkitGlobalState implements PersistentStateComponent<ToolkitGlobalState> {

    private List<String> closeToolKits;

    public static ToolkitGlobalState getInstance() {
        return ApplicationManager.getApplication().getService(ToolkitGlobalState.class);
    }



    @Override
    public @Nullable ToolkitGlobalState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ToolkitGlobalState toolkitGlobalState) {
        XmlSerializerUtil.copyBean(toolkitGlobalState, this);
    }

    public List<String> getCloseToolKits() {
        if (closeToolKits == null) {
            closeToolKits = new ArrayList<>();
        }
        return closeToolKits;
    }

    public void setCloseToolKits(List<String> closeToolKits) {
        this.closeToolKits = closeToolKits;
    }
}
