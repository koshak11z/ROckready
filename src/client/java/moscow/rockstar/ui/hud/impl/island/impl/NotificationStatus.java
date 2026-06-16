/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.notifications.Notification;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.utility.colors.Colors;

public class NotificationStatus
extends IslandStatus {
    public NotificationStatus(SelectSetting setting) {
        super(setting, "alerts");
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        List<Notification> notifications = Rockstar.getInstance().getNotificationManager().getNotifications();
        if (notifications.isEmpty()) {
            return;
        }
        Notification active = notifications.getLast();
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        float width = this.size.width = 18.0f + Fonts.MEDIUM.getFont(7.0f).width(active.getText());
        this.size.height = 15.0f;
        float height = 15.0f;
        for (Notification notification : notifications) {
            notification.getShowing().setDuration(500L);
            notification.getShowing().update(active == notification);
            context.drawRoundedRect(x - 6.0f + 10.0f * this.animation.getValue() * notification.getShowing().getValue(), y + 4.0f, 7.0f, 7.0f, BorderRadius.all(3.0f), notification.getType().getColor().withAlpha(255.0f * notification.getShowing().getValue()));
            context.drawText(Fonts.MEDIUM.getFont(7.0f), notification.getText(), x + 25.0f - 10.0f * this.animation.getValue() * notification.getShowing().getValue(), y + 5.0f, Colors.getTextColor().withAlpha(255.0f * notification.getShowing().getValue()));
        }
    }

    @Override
    public boolean canShow() {
        List<Notification> notifications = Rockstar.getInstance().getNotificationManager().getNotifications();
        return !notifications.isEmpty() && !notifications.getLast().getTimer().finished(notifications.getLast().getDuration());
    }
}

