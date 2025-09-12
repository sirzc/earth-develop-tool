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

package com.myth.earth.develop.ui.component;

import com.intellij.openapi.observable.properties.AtomicBooleanProperty;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.TitledSeparator;
import com.intellij.util.ui.IndentedIcon;
import com.intellij.util.ui.UIUtil;
import kotlin.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;


/**
 * 可折叠的标题分隔符
 *
 * @author zhouchao
 * @date 2025/9/12 下午3:45
 **/
@SuppressWarnings("all")
public class CollapsibleTitledSeparator extends TitledSeparator {
    private final AtomicBooleanProperty expandedProperty = new AtomicBooleanProperty(true);
    private       boolean               expanded;

    public CollapsibleTitledSeparator(@NlsContexts.Separator String title) {
        super(title);
        this.expanded = expandedProperty.get();
        expandedProperty.afterChange(l -> {
            updateIcon();
            return Unit.INSTANCE;
        });
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                setExpanded(!isExpanded());
            }
        });
    }

    public void onAction(Consumer<Boolean> listener) {
        expandedProperty.afterChange(l -> {
            listener.accept(l);
            return Unit.INSTANCE;
        });
    }

    public void updateIcon() {
        Icon treeExpandedIcon = UIUtil.getTreeExpandedIcon();
        Icon treeCollapsedIcon = UIUtil.getTreeCollapsedIcon();
        int width = Math.max(treeExpandedIcon.getIconWidth(), treeCollapsedIcon.getIconWidth());
        Icon icon = isExpanded() ? treeExpandedIcon : treeCollapsedIcon;
        int extraSpace = width - icon.getIconWidth();
        if (extraSpace > 0) {
            int left = extraSpace / 2;
            icon = new IndentedIcon(icon, extraSpace - left);
        }
        getLabel().setIcon(icon);
        getLabel().setDisabledIcon(IconLoader.getTransparentIcon(icon, 0.5f));
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        expandedProperty.set(expanded);
    }
}
