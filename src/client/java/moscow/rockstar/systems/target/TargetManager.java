/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.text.Text
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.systems.target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TargetManager
implements IMinecraft {
    @Nullable
    private Entity currentTarget = null;
    private final List<String> target = new ArrayList<String>();

    public void update(TargetSettings targetSettings) {
        this.currentTarget = this.getBestTarget(targetSettings);
    }

    public void addTarget(String name) {
        if (Rockstar.getInstance().getFriendManager().listFriends().contains(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.target.friend_error")));
            return;
        }
        if (this.target.contains(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.target.already_exists", name)));
            return;
        }
        if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.target.self_error")));
            return;
        }
        this.target.add(name);
        MessageUtility.info(Text.of((String)Localizator.translate("commands.target.added", name)));
    }

    public void removeTarget(String name) {
        if (!this.target.contains(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.target.not_found", name)));
            return;
        }
        this.target.remove(name);
        MessageUtility.info(Text.of((String)Localizator.translate("commands.target.removed", name)));
    }

    public void clearTarget() {
        if (this.target.isEmpty()) {
            MessageUtility.info(Text.of((String)Localizator.translate("commands.target.empty")));
            return;
        }
        this.target.clear();
        MessageUtility.info(Text.of((String)Localizator.translate("commands.target.cleared")));
    }

    public void listTarget() {
        if (this.target.isEmpty()) {
            MessageUtility.info(Text.of((String)Localizator.translate("commands.target.empty")));
            return;
        }
        for (int i = 0; i < this.target.size(); ++i) {
            String name = this.target.get(i);
            MessageUtility.info(Text.of((String)String.format(Localizator.translate("commands.target.list_entry"), i + 1, name)));
        }
    }

    @Nullable
    public Entity getBestTarget(TargetSettings settings) {
        if (TargetManager.mc.world == null) {
            return null;
        }
        Comparator<Entity> comparator = Comparator.comparing((Entity e) -> !this.target.contains(e.getName().getString())).thenComparing(settings.getTargetComparator());
        return StreamSupport.stream(TargetManager.mc.world.getEntities().spliterator(), false).filter(settings::isEntityValid).min(comparator).orElse(null);
    }

    public void reset() {
        this.currentTarget = null;
    }

    public boolean isTarget(String name) {
        return this.target.contains(name);
    }

    public LivingEntity getLivingTarget() {
        LivingEntity target2;
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        return target1 instanceof LivingEntity ? (target2 = (LivingEntity)target1) : null;
    }

    @Nullable
    @Generated
    public Entity getCurrentTarget() {
        return this.currentTarget;
    }

    @Generated
    public List<String> getTarget() {
        return this.target;
    }
}
