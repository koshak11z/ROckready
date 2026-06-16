/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.DeathScreen
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;

@ModuleInfo(name="Death Cords", category=ModuleCategory.OTHER, desc="\u041e\u0442\u043f\u0440\u0430\u0432\u043b\u044f\u0435\u0442 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438 \u0432 \u0447\u0430\u0442")
public class DeathCords
extends BaseModule {
    private boolean death;
    private final BooleanSetting wayDeath = new BooleanSetting(this, "\u0421\u0442\u0430\u0432\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443");
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (DeathCords.mc.currentScreen instanceof DeathScreen && DeathCords.mc.player != null) {
            if (this.death) {
                int xCord = (int)DeathCords.mc.player.getX();
                int yCord = (int)DeathCords.mc.player.getY();
                int zCord = (int)DeathCords.mc.player.getZ();
                MessageUtility.info(Text.of((String)("\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438: " + xCord + " " + yCord + " " + zCord)));
                if (this.wayDeath.isEnabled()) {
                    WayPointsManager wayPointsManager = Rockstar.getInstance().getWayPointsManager();
                    if (wayPointsManager.contains("Death")) {
                        wayPointsManager.del("Death");
                    }
                    wayPointsManager.add("Death", xCord, yCord, zCord);
                }
                this.death = false;
            }
        } else {
            this.death = true;
        }
    };
}

