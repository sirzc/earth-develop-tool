/*
 * Copyright (c) 2026 周潮. All rights reserved.
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

import com.intellij.openapi.actionSystem.ActionButtonComponent;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyLabelButton extends JButton {

    private boolean hovered = false;

    public MyLabelButton(OnClickHandler onClickHandler) {
        addActionListener(e -> onClickHandler.onClick(MyLabelButton.this));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public MyLabelButton(Icon icon, OnClickHandler onClickHandler) {
        setIcon(icon);
        addActionListener(e -> onClickHandler.onClick(MyLabelButton.this));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (hovered) {
            ActionButtonLook.SYSTEM_LOOK.paintBackground(g, this, ActionButtonComponent.SELECTED);
        }
        super.paintComponent(g);
    }

    @FunctionalInterface
    public interface OnClickHandler {

        void onClick(JComponent button);
    }
}