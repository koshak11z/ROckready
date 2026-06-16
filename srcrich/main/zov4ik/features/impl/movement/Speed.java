package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.events.player.PlayerTravelEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.utils.interactions.simulate.Simulations;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Speed extends Module {

    SelectSetting mode = new SelectSetting("Режим", "Выберите режим скорости")
            .value("Normal", "Grim", "FunTime One Block", "Contact")
            .selected("Grim");

    SliderSettings speed = new SliderSettings("Скорость", "Настройка скорости передвижения")
            .range(1.0f, 5.0f)
            .setValue(1.5f)
            .visible(() -> mode.isSelected("Normal"));

    BooleanSetting up = new BooleanSetting("Усиление","Увеличивает дистанцию ускорения до цели в Aura").setValue(true).visible(()-> mode.isSelected("Grim"));

    SliderSettings strength = new SliderSettings("Сила", "Фактор умножения до цели")
            .range(1.0f, 6.0f)
            .setValue(1.5f)
            .visible(() -> mode.isSelected("Grim") && up.isValue());

    SliderSettings contactBoost = new SliderSettings("Сила буста", "Сила контактного ускорения")
            .range(1.0f, 20.0f)
            .setValue(8.0f)
            .visible(() -> mode.isSelected("Contact"));

    SliderSettings targetRange = new SliderSettings("Радиус цели", "Радиус поиска цели")
            .range(0.5f, 10.0f)
            .setValue(3.0f)
            .visible(() -> mode.isSelected("Contact"));

    SliderSettings contactRange = new SliderSettings("Радиус контакта", "Радиус контакта с сущностями")
            .range(0.1f, 2.0f)
            .setValue(0.5f)
            .visible(() -> mode.isSelected("Contact"));

    BooleanSetting playersOnly = new BooleanSetting("Только игроки", "Учитывать только игроков")
            .setValue(true)
            .visible(() -> mode.isSelected("Contact"));

    BooleanSetting onlyWhileMoving = new BooleanSetting("Только в движении", "Работать только при движении")
            .setValue(true)
            .visible(() -> mode.isSelected("Contact"));

    BooleanSetting onlyWithAura = new BooleanSetting("Только с Aura", "Работать только с целью Aura")
            .setValue(false)
            .visible(() -> mode.isSelected("Contact"));

    BooleanSetting predict = new BooleanSetting("Предикт", "Предсказывать позицию цели")
            .setValue(true)
            .visible(() -> mode.isSelected("Contact"));

    SliderSettings predictStrength = new SliderSettings("Сила предикта", "Сила предикта")
            .range(0.1f, 10.0f)
            .setValue(2.0f)
            .visible(() -> mode.isSelected("Contact") && predict.isValue());


    public Speed() {
        super("Speed", "Speed", ModuleCategory.MOVEMENT);
        setup(mode, up, strength, speed, contactBoost, targetRange, contactRange, playersOnly, onlyWhileMoving, onlyWithAura, predict, predictStrength);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mode.isSelected("Normal")) {
            Simulations.setVelocity(speed.getValue() / 3);
        }
        if (mode.isSelected("Contact")) {
            handleContactSpeed();
        }
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onMotion(PlayerTravelEvent e) {
        if (mode.isSelected("FunTime One Block")) {
            if (!mc.player.isSwimming() && !mc.player.isGliding() && !mc.player.isSneaking()) {
                if (mc.player.getBoundingBox().maxY - mc.player.getBoundingBox().minY < 1.5f) {
                    float motion = mc.player.hasStatusEffect(StatusEffects.SPEED) ? 0.32f : 0.28f;
                    Simulations.setVelocity(motion);
                }
            }
        }
        if ((mode.isSelected("Grim")) && e.isPre() && Simulations.hasPlayerMovement()) {
            int collisions = 0;
            float box = 0.3F;
            if (Aura.getInstance().isState() && Aura.getInstance().getTarget() != null && Aura.getInstance().getTarget().isSprinting() && mc.player.isSprinting() && up.isValue()) {
                box = strength.getValue();
            }
            for (Entity ent : mc.world.getEntities())
                if (ent != mc.player && (!(ent instanceof ArmorStandEntity)) && (ent instanceof LivingEntity || ent instanceof BoatEntity) && mc.player.getBoundingBox().expand(box).intersects(ent.getBoundingBox()))
                    collisions++;
            double[] motion = Simulations.forward(0.034 * collisions);
            mc.player.addVelocity(motion[0], 0, motion[1]);
        }

    }

    private void handleContactSpeed() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (onlyWithAura.isValue() && (!Aura.getInstance().isState() || Aura.getInstance().getTarget() == null)) {
            return;
        }
        if (onlyWhileMoving.isValue() && !Simulations.hasPlayerMovement()) {
            return;
        }
        Box contactBox = mc.player.getBoundingBox().expand(contactRange.getValue());
        int contactCount = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (isValidTarget(entity) && contactBox.intersects(entity.getBoundingBox())) {
                contactCount++;
            }
        }
        if (contactCount <= 0) {
            return;
        }
        double motionBoost = contactBoost.getValue() * 0.01 * contactCount;
        Entity nearest = findNearestTarget(targetRange.getValue());
        if (nearest == null) {
            return;
        }
        Vec3d targetPos = nearest.getPos();
        if (predict.isValue()) {
            Vec3d targetMotion = nearest.getVelocity();
            double horizontalMotionSq = targetMotion.x * targetMotion.x + targetMotion.z * targetMotion.z;
            if (horizontalMotionSq > 1.0E-4) {
                targetPos = targetPos.add(targetMotion.x * predictStrength.getValue(), 0.0, targetMotion.z * predictStrength.getValue());
            }
        }
        double[] direction = getDirectionToPoint(mc.player.getPos(), targetPos, motionBoost);
        mc.player.addVelocity(direction[0], 0.0, direction[1]);
    }

    private Entity findNearestTarget(double maxRange) {
        Entity nearest = null;
        double bestDistanceSq = Double.MAX_VALUE;
        double maxDistanceSq = maxRange * maxRange;
        for (Entity entity : mc.world.getEntities()) {
            if (isValidTarget(entity)) {
                double dx = entity.getX() - mc.player.getX();
                double dz = entity.getZ() - mc.player.getZ();
                double distanceSq = dx * dx + dz * dz;
                if (distanceSq <= maxDistanceSq && distanceSq < bestDistanceSq) {
                    bestDistanceSq = distanceSq;
                    nearest = entity;
                }
            }
        }
        return nearest;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player || !entity.isAlive() || entity instanceof ArmorStandEntity) {
            return false;
        }
        if (playersOnly.isValue() && !(entity instanceof PlayerEntity)) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof BoatEntity;
    }

    private double[] getDirectionToPoint(Vec3d from, Vec3d to, double speedValue) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double length = Math.sqrt(dx * dx + dz * dz);
        return length < 1.0E-6 ? new double[]{0.0, 0.0} : new double[]{dx / length * speedValue, dz / length * speedValue};
    }
}