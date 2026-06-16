/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.TrapdoorBlock
 *  net.minecraft.client.gui.hud.BossBarHud
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  net.minecraft.world.World
 */
package moscow.rockstar.utility.math;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.ElytraTarget;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.calculator.ExpressionBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class MathUtility
implements IMinecraft {
    private static final int TABLE_SIZE = 65536;
    private static final double TWO_PI = Math.PI * 2;
    private static final double[] TRIG_TABLE = new double[65536];

    public static double sin(double radians) {
        int index = (int)(radians * 10430.378350470453) & 0xFFFF;
        return TRIG_TABLE[index];
    }

    public static double cos(double radians) {
        int index = (int)(radians * 10430.378350470453 + 16384.0) & 0xFFFF;
        return TRIG_TABLE[index];
    }

    public static float random(double min, double max) {
        return (float)(min + (max - min) * Math.random());
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        return Math.pow(1.0 - t, 3.0) * p0 + 3.0 * t * Math.pow(1.0 - t, 2.0) * p1 + 3.0 * Math.pow(t, 2.0) * (1.0 - t) * p2 + Math.pow(t, 3.0) * p3;
    }

    public static boolean canSeen(Vec3d targetVec) {
        return MathUtility.mc.world.raycast(new RaycastContext(MathUtility.mc.player.getEyePos(), targetVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)MathUtility.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canShoot(Vec3d targetVec) {
        Vec3d start = MathUtility.mc.player.getEyePos();
        Vec3d direction = targetVec.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();
        HashSet<BlockPos> checkedBlocks = new HashSet<BlockPos>();
        int solidBlocks = 0;
        double step = 0.25;
        for (double d = 0.0; d <= distance; d += step) {
            VoxelShape collisionShape;
            Vec3d currentPos = start.add(direction.multiply(d));
            BlockPos blockPos = BlockPos.ofFloored((Position)currentPos);
            if (checkedBlocks.contains(blockPos)) continue;
            checkedBlocks.add(blockPos);
            BlockState blockState = MathUtility.mc.world.getBlockState(blockPos);
            if (blockState.isAir()) continue;
            Block block = blockState.getBlock();
            if (blockState.isOf(Blocks.GLASS) || blockState.isOf(Blocks.GLASS_PANE) || blockState.getBlock() instanceof TrapdoorBlock || (collisionShape = blockState.getCollisionShape((BlockView)MathUtility.mc.world, blockPos)).isEmpty()) continue;
            ++solidBlocks;
        }
        AtomicBoolean snipe = new AtomicBoolean(false);
        BossBarHud boss = MathUtility.mc.inGameHud.getBossBarHud();
        if (boss != null) {
            Class<BossBarHud> bossbarklass = BossBarHud.class;
            try {
                Field field = bossbarklass.getField("bossBars");
                Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)field.get(boss);
                for (UUID uuid : bossBars.keySet()) {
                    ClientBossBar clientBossBar = bossBars.get(uuid);
                    List<Text> siblings = clientBossBar.getName().getSiblings();
                    siblings.stream().allMatch(text -> {
                        if (text.getString().contains("\ub8f3\ua223\ua203\ub8f2\ua223\ua205")) {
                            snipe.set(true);
                        }
                        return true;
                    });
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return solidBlocks <= (snipe.get() ? 3 : (MathUtility.mc.player.getInventory().selectedSlot == 0 ? 2 : 1));
    }

    public static int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[] dp = new int[m + 1];
        for (int j = 0; j <= m; ++j) {
            dp[j] = j;
        }
        for (int i = 1; i <= n; ++i) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= m; ++j) {
                int tmp = dp[j];
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prev + cost);
                prev = tmp;
            }
        }
        return dp[m];
    }

    public static float interpolate(double oldValue, double newValue, double interpolationValue) {
        return (float)(oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static HitResult rayTrace(double rayTraceDistance, float yaw, float pitch, Entity entity) {
        Vec3d startVec = MathUtility.mc.player.getCameraPosVec(1.0f);
        Vec3d directionVec = MathUtility.getVectorForRotation(pitch, yaw);
        Vec3d endVec = startVec.add(directionVec.x * rayTraceDistance, directionVec.y * rayTraceDistance, directionVec.z * rayTraceDistance);
        return MathUtility.mc.world.raycast(new RaycastContext(startVec, endVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
    }

    public static boolean tracedTo(Entity shooter, Vec3d startVec, Vec3d endVec, Box boundingBox, Predicate<Entity> filter, double distance, Entity target) {
        World world = shooter.getWorld();
        double d0 = distance;
        for (Entity entity1 : world.getOtherEntities(shooter, boundingBox, filter)) {
            Box box = entity1.getBoundingBox().expand((double)entity1.getTargetingMargin());
            Optional optional = box.raycast(startVec, endVec);
            if (box.contains(startVec)) {
                if (!(d0 >= 0.0)) continue;
                if (entity1 == target) {
                    return true;
                }
                d0 = 0.0;
                continue;
            }
            if (!optional.isPresent()) continue;
            Vec3d vec3d1 = (Vec3d)optional.get();
            double d1 = startVec.squaredDistanceTo(vec3d1);
            if (entity1.getRootVehicle() == shooter.getRootVehicle()) {
                if (d0 != 0.0 || entity1 != target) continue;
                return true;
            }
            if (entity1 == target) {
                return true;
            }
            d0 = d1;
        }
        return false;
    }

    public static boolean canTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target, boolean checkBlocks) {
        double targetDistSq;
        double blockDistSq;
        BlockHitResult blockHit;
        Vec3d endPos;
        if (target == null || entity == null || MathUtility.mc.world == null) {
            return false;
        }
        float partialTicks = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d startPos = entity.getCameraPosVec(partialTicks);
        if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)target;
            endPos = player.getBoundingBox().offset(ElytraPredictionSystem.predictPlayerPosition(player)).offset(-player.getX(), -player.getY(), -player.getZ()).getCenter();
        } else {
            endPos = target.getBoundingBox().getCenter();
        }
        if (checkBlocks && (blockHit = MathUtility.mc.world.raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))) != null && blockHit.getType() == HitResult.Type.BLOCK && (blockDistSq = blockHit.getPos().squaredDistanceTo(startPos)) < (targetDistSq = endPos.squaredDistanceTo(startPos))) {
            return false;
        }
        Vec3d direction = MathUtility.getVectorForRotation(pitch, yaw);
        Vec3d rayEnd = startPos.add(direction.multiply(rayTraceDistance));
        Box searchBox = entity.getBoundingBox().stretch(direction.multiply(rayTraceDistance)).expand(1.0);
        return MathUtility.tracedTo(entity, startPos, rayEnd, searchBox, e -> !e.isSpectator() && e.canHit(), rayTraceDistance * rayTraceDistance, target);
    }

    public static Vec3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float)Math.PI / 180) - (float)Math.PI;
        float pitchRadians = -pitch * ((float)Math.PI / 180);
        float cosYaw = MathHelper.cos((float)yawRadians);
        float sinYaw = MathHelper.sin((float)yawRadians);
        float cosPitch = -MathHelper.cos((float)pitchRadians);
        float sinPitch = MathHelper.sin((float)pitchRadians);
        return new Vec3d((double)(sinYaw * cosPitch), (double)sinPitch, (double)(cosYaw * cosPitch));
    }

    public static float angleDifference(float angle1, float angle2) {
        float diff = (angle1 - angle2) % 360.0f;
        if (diff < -180.0f) {
            diff += 360.0f;
        } else if (diff > 180.0f) {
            diff -= 360.0f;
        }
        return diff;
    }

    public static String calculate(String expression) {
        if ((expression = expression.replaceAll("\\s+", "")).isEmpty()) {
            return "";
        }
        try {
            double result = new ExpressionBuilder(expression).build().evaluate();
            return String.valueOf(result);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return expression;
        }
    }

    @Generated
    private MathUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            MathUtility.TRIG_TABLE[i] = Math.sin((double)i * (Math.PI * 2) / 65536.0);
        }
    }
}
