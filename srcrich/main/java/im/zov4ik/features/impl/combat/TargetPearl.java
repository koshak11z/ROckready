package im.zov4ik.features.impl.combat;

import antidaunleak.api.annotation.Native;
import im.zov4ik.utils.features.aura.warp.Turns;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.interactions.inv.InventoryFlowManager;
import im.zov4ik.utils.interactions.inv.InventoryTask;
import im.zov4ik.utils.interactions.simulate.PlayerSimulation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.client.managers.event.types.EventType;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.common.repository.friend.FriendUtils;
import im.zov4ik.utils.math.time.StopWatch;
import im.zov4ik.utils.math.task.TaskPriority;
import im.zov4ik.utils.math.script.Script;
import im.zov4ik.events.player.EntitySpawnEvent;
import im.zov4ik.events.player.PostMotionEvent;
import im.zov4ik.events.player.RotationUpdateEvent;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import im.zov4ik.utils.features.aura.warp.TurnsConfig;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import im.zov4ik.utils.features.aura.rotations.impl.SnapAngle;
import im.zov4ik.features.impl.render.ProjectilePrediction;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TargetPearl extends Module {
    StopWatch stopWatch = new StopWatch();
    Script script = new Script();

    SelectSetting modeSetting = new SelectSetting("Mode", "When will target pearl work")
            .value("Bind", "Always").selected("Always");

    SelectSetting targetSetting = new SelectSetting("Targets", "Targets for which pearls will be thrown")
            .value("Aura Target", "All").selected("Aura Target");

    BindSetting throwSetting = new BindSetting("Throw","Throw Key").visible(()-> modeSetting.isSelected("Bind"));

    SliderSettings distanceSetting = new SliderSettings("Distance", "Target Pearl Trigger Distance")
            .setValue(10).range(5, 15);

    public TargetPearl() {
        super("TargetPearl","Target Pearl", ModuleCategory.COMBAT);
        setup(modeSetting, targetSetting, throwSetting, distanceSetting);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof EnderPearlEntity pearl) mc.world.getPlayers().stream().filter(p -> p.distanceTo(pearl) <= 3)
                .min(Comparator.comparingDouble(p -> p.distanceTo(pearl))).ifPresent(pearl::setOwner);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginMutation)
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() == EventType.PRE) {
            LivingEntity target = Aura.getInstance().getLastTarget();
            Slot slot = InventoryTask.getSlot(Items.ENDER_PEARL);

            if (slot == null || !stopWatch.finished(1000)) return;
            if (modeSetting.isSelected("Bind") && !PlayerInteractionHelper.isKey(throwSetting)) return;
            if (PlayerInteractionHelper.streamEntities().filter(EnderPearlEntity.class::isInstance).map(EnderPearlEntity.class::cast)
                    .anyMatch(pearl -> Objects.equals(pearl.getOwner(), mc.player))) {
                stopWatch.reset();
                return;
            }

            ProjectilePrediction prediction = ProjectilePrediction.getInstance();
            PlayerInteractionHelper.streamEntities().filter(EnderPearlEntity.class::isInstance).map(EnderPearlEntity.class::cast)
                    .filter(pearl -> !FriendUtils.isFriend(pearl.getOwner()) && (targetSetting.isSelected("All") || (target != null && target.equals(pearl.getOwner()))))
                    .min(Comparator.comparingDouble(pearl -> TurnsConnection.computeRotationDifference(MathAngle.cameraAngle(), MathAngle.calculateAngle(prediction.calcTrajectory(pearl).getPos()))))
                    .ifPresent(pearl -> {
                        HitResult targetResult = prediction.calcTrajectory(pearl);
                        if (targetResult == null || mc.player.getPos().distanceTo(targetResult.getPos()) <= distanceSetting.getInt()) return;
                        Vec3d eyePos = mc.player.getEyePos().add(mc.player.getPos().subtract(PlayerSimulation.simulateLocalPlayer(1).pos));
                        float yaw = MathAngle.fromVec3d(targetResult.getPos().subtract(eyePos)).getYaw();
                        IntStream.range(-89, 89).mapToObj(pitch -> new Turns(yaw, pitch)).filter(angle -> {
                            HitResult playerResult = prediction.checkTrajectory(angle.toVector(), new EnderPearlEntity(mc.world, mc.player, slot.getStack()), 1.5);
                            return playerResult != null && playerResult.getPos().distanceTo(targetResult.getPos()) <= 3F;
                        }).max(Comparator.comparingDouble(Turns::getPitch)).ifPresent(angle -> {
                            TurnsConnection.INSTANCE.rotateTo(new Turns.VecRotation(angle, angle.toVector()), mc.player, 1, new TurnsConfig(new SnapAngle(),true,true), TaskPriority.HIGH_IMPORTANCE_3, this);
                            InventoryFlowManager.unPressMoveKeys();
                            script.cleanup().addTickStep(0, () -> {
                                InventoryTask.swapAndUse(Items.ENDER_PEARL, angle, false);
                                InventoryFlowManager.enableMoveKeys();
                            });
                            pearl.setOwner(null);
                            stopWatch.reset();
                        });
                    });
        }
    }

    @EventHandler
    public void onPostMotion(PostMotionEvent e) {
        script.update();
    }
}