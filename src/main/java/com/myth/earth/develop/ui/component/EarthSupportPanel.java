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

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.JBUI;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * earth 系列支持
 *
 * @author zhouchao
 * @date 2025/9/7 上午9:01
 **/
public class EarthSupportPanel extends JBPanel<EarthSupportPanel> {

    private static final ImageIcon PAY_IMG = new ImageIcon(Objects.requireNonNull(EarthSupportPanel.class.getResource("/icons/pay@jz.jpg")));
    private static final ImageIcon WECHAT_IMG = new ImageIcon(Objects.requireNonNull(EarthSupportPanel.class.getResource("/icons/wechat@jz.jpg")));

    public EarthSupportPanel() {
        setLayout(new BorderLayout());
        add(createCenterPanel(), BorderLayout.NORTH);
    }

    protected JComponent createCenterPanel() {
        JPanel rootPanel = new NonOpaquePanel(new VerticalLayout());
        rootPanel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insets(5)));
        rootPanel.add(new JBLabel("<html><body style='color:orange;font-weight:bold;width: 440px'>感谢您对Earth系列插件的认可，earth系列插件致力于解决研发过程中遇到的难点、痛点，简化研发流程，减少重复作业的枯燥，增强编程乐趣。</body></html>"));
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(new JBLabel("目前earth系列插件已上架："));
        rootPanel.add(Box.createVerticalStrut(5));
        rootPanel.add(createActionLink("Earth Run Helper：快速本地JVM调试", "https://plugins.jetbrains.com/plugin/27968-earth-run-helper?noRedirect=true"));
        rootPanel.add(createActionLink("Earth Restful Helper：API调试、文档生成、接口管理", "https://plugins.jetbrains.com/plugin/25517-earth-restful-helper/?noRedirect=true"));
        rootPanel.add(createActionLink("Earth Web Tool：IDE中浏览收藏页面", "https://plugins.jetbrains.com/plugin/27996-earth-web-tool/?noRedirect=true"));
        rootPanel.add(createActionLink("Earth Develop Tool：开发工具箱，收录一些常用开发工具", "https://plugins.jetbrains.com/plugin/28500-earth-develop-tool/?noRedirect=true"));
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(new JBLabel("您可以采取以下任意一种方式来支持Earth系列插件："));
        rootPanel.add(Box.createVerticalStrut(5));
        rootPanel.add(createActionLink("关注我的哔哩哔哩：代码不周到", "https://space.bilibili.com/519315897"));
        rootPanel.add(createActionLink("关注我的微信公众号：代码不周到", "https://mp.weixin.qq.com/mp/appmsgalbum?__biz=Mzk2OTA4NzU3Mg==&action=getalbum&album_id=4034522382747533329#wechat_redirect"));
        rootPanel.add(Box.createVerticalStrut(10));
        rootPanel.add(new JBLabel("<html><body style='color:#59A869;font-weight:bold;'>如果您喜欢这个插件，请考虑捐赠支持一下，这将极大地激励我不断完善此项目</body></html>"));
        rootPanel.add(Box.createVerticalStrut(5));
        rootPanel.add(createImagePanel());
        return rootPanel;
    }

    @NotNull
    private static JPanel createImagePanel() {
        JBLabel payLabel = new JBLabel("微信捐赠", PAY_IMG, SwingConstants.CENTER);
        payLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        payLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JBLabel wechatLabel = new JBLabel("微信公众号", WECHAT_IMG, SwingConstants.CENTER);
        wechatLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        wechatLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.add(payLabel);
        imagePanel.add(wechatLabel);
        return imagePanel;
    }

    private ActionLink createActionLink(String text, String url) {
        // ActionLink actionLink = new ActionLink("关注我的哔哩哔哩");
        // actionLink.addActionListener(e -> JBPopupFactory.getInstance().createHtmlTextBalloonBuilder("显示内容", MessageType.INFO, null).createBalloon()
        //                                                 .show(new RelativePoint(actionLink, new Point(actionLink.getX(), actionLink.getY() / 2)),
        //                                                       Balloon.Position.atRight));
        return new ActionLink(text, e -> {BrowserUtil.browse(url);});

    }
}
