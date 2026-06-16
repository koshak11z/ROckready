package im.zov4ik.features.impl.movement;

import antidaunleak.api.annotation.Native;
import im.zov4ik.events.player.AttackEvent;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.features.impl.misc.ElytraHelper;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.math.time.StopWatch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class ElytraTarget extends Module {

    private enum ResolverMode {
        SEARCH,
        DIRECT
    }

    public static ElytraTarget getInstance() {
        return Instance.get(ElytraTarget.class);
    }

    public final BooleanSetting elytraTarget = new BooleanSetting("Elytra Target", "Master switch for elytra target logic")
            .setValue(true);

    public final SelectSetting overtakeMode = new SelectSetting("Overtake Mode", "Mode for overtake logic")
            .value("Legit", "Full Rage")
            .selected("Legit")
            .visible(elytraTarget::isValue);

    public final BooleanSetting overtakeEnabled = new BooleanSetting("Elytra Overtake", "Enables lead ticks while chasing airborne targets")
            .setValue(false)
            .visible(elytraTarget::isValue);

    public final SliderSettings elytraFindRange = new SliderSettings("Elytra Find Range", "Extra aura search range while gliding")
            .range(5f, 100f)
            .setValue(30f)
            .visible(elytraTarget::isValue);

    public final SliderSettings elytraForward = new SliderSettings("Overtake Strength", "Forward lead value for moving target")
            .range(0f, 10f)
            .setValue(3f)
            .visible(elytraTarget::isValue);

    public final BooleanSetting resolverEnabled = new BooleanSetting("Elytra Resolver", "Enable resolver support while gliding")
            .setValue(false)
            .visible(elytraTarget::isValue);

    public final SliderSettings resolverDistance = new SliderSettings("Resolver Distance", "Distance threshold for resolver boost")
            .range(3f, 15f)
            .setValue(7f)
            .visible(resolverEnabled::isValue);

    private final Vec3d[] resolverOffsets = new Vec3d[]{
            new Vec3d(0.0, 10.0, 0.0),
            new Vec3d(10.0, 2.0, 0.0),
            new Vec3d(-10.0, 2.0, 0.0),
            new Vec3d(0.0, 2.0, 10.0),
            new Vec3d(0.0, 2.0, -10.0),
            new Vec3d(0.0, -10.0, 0.0)
    };

    private ResolverMode resolverMode = ResolverMode.SEARCH;
    private Vec3d lastOffset = null;
    private Vec3d lastBoostPos = null;
    private boolean switchedOffset = false;

    private final StopWatch fireworkTimer = new StopWatch();
    private final StopWatch relaunchTimer = new StopWatch();
    private final StopWatch offsetSwitchTimer = new StopWatch();

    public static boolean shouldElytraTarget = false;
    public Vec3d resolverPosition = null;

    public ElytraTarget() {
        super("ElytraTarget", "Elytra Target", ModuleCategory.MOVEMENT);
        setup(
                elytraTarget,
                overtakeMode,
                overtakeEnabled,
                elytraFindRange,
                elytraForward,
                resolverEnabled,
                resolverDistance
        );
    }

    @Override
    public void activate() {
        super.activate();
        shouldElytraTarget = elytraTarget.isValue() && overtakeEnabled.isValue();
    }

    @Override
    public void deactivate() {
        shouldElytraTarget = false;
        resetResolver();
        super.deactivate();
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        shouldElytraTarget = elytraTarget.isValue() && overtakeEnabled.isValue();

        Aura aura = Instance.get(Aura.class);
        LivingEntity target = aura.getTarget();
        if (canUseResolver(target)) {
            processResolver(target);
            return;
        }

        Blink.getInstance().setState(false);
        resetResolver();
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        resolverMode = ResolverMode.SEARCH;
    }

    public Vec3d getResolverPosition() {
        return resolverPosition;
    }

    private boolean canUseResolver(LivingEntity target) {
        return resolverEnabled.isValue()
                && elytraTarget.isValue()
                && mc.player.isGliding()
                && target != null
                && !target.isGliding()
                && getHorizontalBps(target) < 15.0;
    }

    private void processResolver(LivingEntity target) {
        Blink blink = Blink.getInstance();
        Vec3d targetEyePos = target.getEyePos();
        Vec3d selfEyePos = mc.player.getEyePos();

        if (resolverMode == ResolverMode.SEARCH && offsetSwitchTimer.finished(400)) {
            updateResolverOffset(targetEyePos);
            offsetSwitchTimer.reset();
        }

        if (resolverMode == ResolverMode.DIRECT) {
            resolverPosition = targetEyePos;
        }

        if (selfEyePos.distanceTo(targetEyePos) > resolverDistance.getValue()) {
            if (fireworkTimer.finished(250)) {
                blink.setState(false);
                Instance.get(ElytraHelper.class).fireWorkMethod();
                lastBoostPos = mc.player.getPos();
                switchedOffset = false;
                fireworkTimer.reset();
                relaunchTimer.reset();
                blink.setState(true);
            }
            resolverMode = ResolverMode.DIRECT;
        }

        if (relaunchTimer.finished(1000)) {
            blink.setState(false);
            if (fireworkTimer.finished(250)) {
                lastBoostPos = mc.player.getPos();
                Instance.get(ElytraHelper.class).fireWorkMethod();
                fireworkTimer.reset();
            }
            switchedOffset = false;
            blink.setState(true);
        }
    }

    private void updateResolverOffset(Vec3d targetEyePos) {
        List<Vec3d> visibleOffsets = new ArrayList<>();
        for (Vec3d offset : resolverOffsets) {
            if (hasLineOfSight(targetEyePos.add(offset))) {
                visibleOffsets.add(offset);
            }
        }

        if (visibleOffsets.isEmpty()) {
            resolverPosition = targetEyePos;
            return;
        }

        if (visibleOffsets.size() == 1) {
            lastOffset = visibleOffsets.get(0);
            resolverPosition = targetEyePos.add(lastOffset);
            switchedOffset = true;
            return;
        }

        for (Vec3d offset : visibleOffsets) {
            if (lastOffset != offset && !switchedOffset) {
                lastOffset = offset;
                resolverPosition = targetEyePos.add(offset);
                switchedOffset = true;
                return;
            }
        }

        lastOffset = visibleOffsets.get(0);
        resolverPosition = targetEyePos.add(lastOffset);
        switchedOffset = true;
    }

    private boolean hasLineOfSight(Vec3d point) {
        Vec3d eyePos = mc.player.getEyePos();
        HitResult result = mc.world.raycast(new RaycastContext(
                eyePos,
                point,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));
        return result.getType() == HitResult.Type.MISS || result.getPos().squaredDistanceTo(point) < 0.25;
    }

    private double getHorizontalBps(LivingEntity entity) {
        return entity.getVelocity().horizontalLength() * 20.0;
    }

    private void resetResolver() {
        resolverPosition = null;
        lastBoostPos = null;
        lastOffset = null;
        switchedOffset = false;
        resolverMode = ResolverMode.SEARCH;
    }
}
