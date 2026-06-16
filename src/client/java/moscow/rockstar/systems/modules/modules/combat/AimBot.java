/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.entity.Entity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name="Aim Bot", category=ModuleCategory.COMBAT, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043d\u0430\u0432\u043e\u0434\u0438\u0442\u0441\u044f \u043b\u0443\u043a\u043e\u043c, \u0442\u0440\u0435\u0437\u0443\u0431\u0446\u0435\u043c \u0438\u043b\u0438 \u0430\u0440\u0431\u0430\u043b\u0435\u0442\u043e\u043c")
public class AimBot
extends BaseModule {
    private SelectSetting.Value bow;
    private SelectSetting.Value crossbow;
    private SelectSetting.Value trident;
    private SliderSetting distance;
    private SliderSetting fov;
    private BooleanSetting prediction;
    private BooleanSetting silent;
    private SelectSetting.Value players;
    private SelectSetting.Value animals;
    private SelectSetting.Value mobs;
    private SelectSetting.Value invisibles;
    private SelectSetting.Value naked;
    private SelectSetting.Value friends;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (AimBot.mc.player == null || AimBot.mc.world == null || !this.isHoldingSelected()) {
            return;
        }
        TargetSettings settings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetMobs(this.mobs.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetNakedPlayers(this.naked.isSelected()).targetFriends(this.friends.isSelected()).requiredRange(this.distance.getCurrentValue()).sortBy(TargetComparators.FOV).build();
        Rockstar.getInstance().getTargetManager().update(settings);
        Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (targetEntity == null) {
            return;
        }
        Vec3d aimPos = this.getAimPosition(targetEntity);
        Rotation toTarget = this.calculateRotation(aimPos);
        float yaw = toTarget.getYaw();
        float pitch = toTarget.getPitch();
        float deltaYaw = RotationMath.getAngleDifference(yaw, AimBot.mc.player.getYaw());
        if (deltaYaw > this.fov.getCurrentValue() || !MathUtility.canSeen(targetEntity.getPos())) {
            return;
        }
        if (this.silent.isEnabled()) {
            Rockstar.getInstance().getRotationHandler().rotate(toTarget);
        } else {
            AimBot.mc.player.setYaw(yaw);
            AimBot.mc.player.setPitch(pitch);
            AimBot.mc.player.setHeadYaw(yaw);
        }
    };

    public AimBot() {
        this.initialize();
    }

    @VMProtect(type=VMProtectType.VIRTUALIZATION)
    private void initialize() {
        SelectSetting items = new SelectSetting(this, "items");
        this.bow = new SelectSetting.Value(items, "bow").select();
        this.crossbow = new SelectSetting.Value(items, "crossbow");
        this.trident = new SelectSetting.Value(items, "trident");
        this.prediction = new BooleanSetting(this, "predict");
        this.silent = new BooleanSetting(this, "silent_aim");
        this.distance = new SliderSetting((SettingsContainer)this, "distance", "\u041c\u0430\u043a\u0441. \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u044f").min(0.0f).max(100.0f).step(1.0f).currentValue(30.0f);
        this.fov = new SliderSetting(this, "fov").min(1.0f).max(180.0f).step(1.0f).currentValue(90.0f);
        SelectSetting targets = new SelectSetting(this, "targets");
        this.players = new SelectSetting.Value(targets, "players").select();
        this.animals = new SelectSetting.Value(targets, "animals").select();
        this.mobs = new SelectSetting.Value(targets, "mobs").select();
        this.invisibles = new SelectSetting.Value(targets, "invisibles").select();
        this.naked = new SelectSetting.Value(targets, "nakedPlayers").select();
        this.friends = new SelectSetting.Value(targets, "friends");
    }

    private boolean isHoldingSelected() {
        if (AimBot.mc.player == null || AimBot.mc.player.isDead()) {
            return false;
        }
        Item main = AimBot.mc.player.getMainHandStack().getItem();
        if (main == Items.BOW && this.bow.isSelected()) {
            return AimBot.mc.player.isUsingItem();
        }
        if (main == Items.CROSSBOW && this.crossbow.isSelected()) {
            return CrossbowItem.isCharged((ItemStack)AimBot.mc.player.getMainHandStack());
        }
        if (main == Items.TRIDENT && this.trident.isSelected()) {
            return AimBot.mc.player.isUsingItem();
        }
        return false;
    }

    private Rotation calculateRotation(Vec3d targetPos) {
        Vec3d eyes = AimBot.mc.player.getCameraPosVec(1.0f);
        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Rotation(yaw, pitch);
    }

    private Vec3d getAimPosition(Entity target) {
        Vec3d pos = target.getPos();
        if (!this.prediction.isEnabled()) {
            return pos.add(0.0, (double)(target.getHeight() / 2.0f), 0.0);
        }
        Vec3d motion = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ).multiply(10.0);
        return pos.add(motion).add(0.0, (double)(target.getHeight() / 2.0f), 0.0);
    }

    @Override
    public void onDisable() {
        Rockstar.getInstance().getTargetManager().reset();
    }
}

