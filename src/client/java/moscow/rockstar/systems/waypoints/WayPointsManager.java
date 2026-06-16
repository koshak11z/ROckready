/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class WayPointsManager {
    private final Map<String, Vec3d> waypoints = new HashMap<String, Vec3d>();

    public void add(String name, int x, int y, int z) {
        Vec3d pos = new Vec3d((double)x, (double)y, (double)z);
        if (this.waypoints.containsKey(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("modules.waypoints.exists", name)));
            return;
        }
        this.waypoints.put(name, pos);
        MessageUtility.info(Text.of((String)Localizator.translate("modules.waypoints.added", name, x, y, z)));
    }

    public void del(String name) {
        if (this.waypoints.remove(name) != null) {
            MessageUtility.info(Text.of((String)Localizator.translate("modules.waypoints.deleted", name)));
        } else {
            MessageUtility.info(Text.of((String)Localizator.translate("modules.waypoints.not_found", name)));
        }
    }

    public void clear() {
        this.waypoints.clear();
        MessageUtility.info(Text.of((String)Localizator.translate("modules.waypoints.cleared")));
    }

    public boolean contains(String name) {
        return this.waypoints.containsKey(name);
    }

    public Set<Map.Entry<String, Vec3d>> getEntries() {
        return this.waypoints.entrySet();
    }
}

