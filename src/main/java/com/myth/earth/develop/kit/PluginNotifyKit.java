package com.myth.earth.develop.kit;

import static com.intellij.configurationStore.StorageUtilKt.NOTIFICATION_GROUP_ID;

/**
 *
 *
 * @author Inger
 * @since 2025/7/17
 */
public class PluginNotifyKit {


    public static void warn(String title) {
        warn(title, null);
    }

    public static void warn(String title, String content) {
        // 使用 IntelliJ 提供的通知 API
        com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(title, content, com.intellij.notification.NotificationType.WARNING)
                .notify(null);
    }
}
