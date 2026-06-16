/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.ArmorStandEntity
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.TimerStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class MineStatus
extends TimerStatus
implements IMinecraft {
    private Vec3d vec = new Vec3d(-52.0, 87.0, 3.0);

    public MineStatus(SelectSetting setting) {
        super(setting, "mine");
    }

    @Override
    public void draw(CustomDrawContext context) {
        ColorRGBA color;
        if (MineStatus.mc.world == null || MineStatus.mc.player == null || !ServerUtility.spawn()) {
            return;
        }
        String time = "";
        String mineType = "";
        List<String> hw_types = List.of("\u043e\u0431\u044b\u0447\u043d\u0430\u044f", "\u0440\u0435\u0434\u043a\u0430\u044f", "\u044d\u043f\u0438\u0447\u0435\u0441\u043a\u0430\u044f", "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f", "\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0430\u044f");
        ArrayList<ArmorStandEntity> nearbyStands = new ArrayList<ArmorStandEntity>();
        for (Entity entity : MineStatus.mc.world.getEntities()) {
            ArmorStandEntity armorStand;
            if (!(entity instanceof ArmorStandEntity) || !this.near(armorStand = (ArmorStandEntity)entity, new Vec3d(-52.0, 87.0, 3.0))) continue;
            nearbyStands.add(armorStand);
        }
        nearbyStands.sort((a, b) -> Double.compare(b.getY(), a.getY()));
        for (int i = 0; i < nearbyStands.size(); ++i) {
            String nextNameStr;
            ArmorStandEntity nextStand;
            Text nextName;
            ArmorStandEntity armorStand = (ArmorStandEntity)nearbyStands.get(i);
            Text customName = armorStand.getCustomName();
            if (customName == null) continue;
            String name = customName.getString().trim();
            if (name.matches("\\d{1,2}:\\d{2}")) {
                time = name.replaceFirst("^0", "");
            } else if (name.contains("\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c:")) {
                int index = name.indexOf(58);
                if (index != -1 && index + 2 < name.length()) {
                    String timeStr = name.substring(index + 2).trim();
                    int minIndex = timeStr.indexOf(" \u043c\u0438\u043d.");
                    int secIndex = timeStr.indexOf(" \u0441\u0435\u043a.");
                    if (minIndex != -1 && secIndex != -1) {
                        int min = Integer.parseInt(timeStr.substring(0, minIndex).trim());
                        int sec = Integer.parseInt(timeStr.substring(minIndex + 5, secIndex).trim());
                        time = String.format("%d:%02d", min, sec);
                    }
                }
            } else if (name.startsWith("\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f:")) {
                int index = name.indexOf(58);
                if (index != -1 && index + 2 < name.length()) {
                    mineType = name.substring(index + 2).trim();
                }
            } else if (name.equals("\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f \u0448\u0430\u0445\u0442\u0430:") && i + 1 < nearbyStands.size() && (nextName = (nextStand = (ArmorStandEntity)nearbyStands.get(i + 1)).getCustomName()) != null && hw_types.contains((nextNameStr = nextName.getString().trim()).toLowerCase().trim())) {
                mineType = nextNameStr;
            }
            if (!time.isEmpty() && !mineType.isEmpty()) break;
        }
        if (time.isEmpty() || mineType.isEmpty()) {
            return;
        }
        if (ServerUtility.is("holyworld")) {
            color = switch (mineType.trim().toLowerCase()) {
                case "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f" -> new ColorRGBA(0.0f, 128.0f, 250.0f);
                case "\u044d\u043f\u0438\u0447\u0435\u0441\u043a\u0430\u044f" -> new ColorRGBA(231.0f, 0.0f, 250.0f);
                default -> new ColorRGBA(243.0f, 151.0f, 250.0f);
            };
        } else {
            color = switch (mineType.trim().toLowerCase()) {
                case "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u0430\u044f" -> new ColorRGBA(84.0f, 152.0f, 152.0f);
                case "\u043c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0430\u044f" -> new ColorRGBA(252.0f, 84.0f, 252.0f);
                default -> new ColorRGBA(252.0f, 168.0f, 0.0f);
            };
        }
        this.update(Integer.parseInt(time.split(":")[0]) + ":", "", Integer.parseInt(time.split(":")[1]), mineType, color);
        super.draw(context);
        this.timeAnim.settings(true, ColorRGBA.WHITE);
    }

    @Override
    public boolean canShow() {
        if (MineStatus.mc.world == null || MineStatus.mc.player == null || !ServerUtility.spawn()) {
            return false;
        }
        this.vec = ServerUtility.is("holyworld") ? new Vec3d(23.0, 41.0, -156.0) : new Vec3d(-52.0, 87.0, 3.0);
        for (Entity entity : MineStatus.mc.world.getEntities()) {
            ArmorStandEntity armorStand;
            if (!(entity instanceof ArmorStandEntity) || !(armorStand = (ArmorStandEntity)entity).isAlive() || !this.near(armorStand, this.vec)) continue;
            return true;
        }
        return false;
    }

    private boolean near(ArmorStandEntity a, Vec3d v) {
        return Math.abs(a.getX() - v.x) <= 2.0 && Math.abs(a.getY() - v.y) <= 2.0 && Math.abs(a.getZ() - v.z) <= 2.0;
    }
}

