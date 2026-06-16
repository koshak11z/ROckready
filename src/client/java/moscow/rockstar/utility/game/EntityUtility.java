/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.MaceItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.scoreboard.ReadableScoreboardScore
 *  net.minecraft.scoreboard.ScoreHolder
 *  net.minecraft.scoreboard.ScoreboardDisplaySlot
 *  net.minecraft.scoreboard.ScoreboardObjective
 *  net.minecraft.scoreboard.number.NumberFormat
 *  net.minecraft.scoreboard.number.StyledNumberFormat
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Position
 *  net.minecraft.world.World
 */
package moscow.rockstar.utility.game;

import lombok.Generated;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public final class EntityUtility
implements IMinecraft {
    private static float timer = 1.0f;

    public static void resetTimer() {
        timer = 1.0f;
    }

    public static Block getBlock() {
        return EntityUtility.getBlock(0.0, 0.0, 0.0);
    }

    public static Block getBlock(double x, double y, double z) {
        return !EntityUtility.isInGame() ? Blocks.AIR : EntityUtility.mc.world.getBlockState(BlockPos.ofFloored((Position)EntityUtility.mc.player.getPos().add(x, y, z))).getBlock();
    }

    public static boolean collideWith(LivingEntity entity) {
        return EntityUtility.collideWith(entity, 0.0f);
    }

    public static boolean collideWith(LivingEntity entity, float grow) {
        Box box = EntityUtility.mc.player.getBoundingBox();
        Box targetbox = entity.getBoundingBox().expand((double)grow, 0.0, (double)grow);
        return box.maxX > targetbox.minX && box.maxY > targetbox.minY && box.maxZ > targetbox.minZ && box.minX < targetbox.maxX && box.minY < targetbox.maxY && box.minZ < targetbox.maxZ;
    }

    public static void setSpeed(double speed) {
        double forward = EntityUtility.mc.player.input.movementForward;
        double strafe = EntityUtility.mc.player.input.movementSideways;
        float yaw = EntityUtility.mc.player.getYaw();
        if (forward == 0.0 && strafe == 0.0) {
            EntityUtility.mc.player.setVelocity(0.0, EntityUtility.mc.player.getVelocity().y, 0.0);
            return;
        }
        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
                yaw += (float)(forward > 0.0 ? 45 : -45);
            }
            strafe = 0.0;
            forward = forward > 0.0 ? 1.0 : -1.0;
        }
        double motionX = forward * speed * Math.cos(Math.toRadians((double)yaw + 90.0)) + strafe * speed * Math.sin(Math.toRadians((double)yaw + 90.0));
        double motionZ = forward * speed * Math.sin(Math.toRadians((double)yaw + 90.0)) - strafe * speed * Math.cos(Math.toRadians((double)yaw + 90.0));
        EntityUtility.mc.player.setVelocity(motionX, EntityUtility.mc.player.getVelocity().y, motionZ);
    }

    public static boolean isPlayerMoving() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.world == null || EntityUtility.mc.player.input == null) {
            return false;
        }
        return (double)EntityUtility.mc.player.forwardSpeed != 0.0 || (double)EntityUtility.mc.player.input.movementSideways != 0.0;
    }

    public static Block getBlockBelow(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.getBlockPos().down();
        return EntityUtility.getBlockAt(pos, entity.getWorld());
    }

    public static Block getBlockAbove(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.getBlockPos().add(0, Math.round(entity.getHeight()), 0).up();
        return EntityUtility.getBlockAt(pos, entity.getWorld());
    }

    public static Block getBlockBelowPlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.world == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.getBlockPos().down().up();
        return EntityUtility.getBlockAt(pos, (World)EntityUtility.mc.world);
    }

    public static Block getBlockAbovePlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.world == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.getBlockPos().up();
        return EntityUtility.getBlockAt(pos, (World)EntityUtility.mc.world);
    }

    public static Block getBlockStandingOn(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.getBlockPos();
        return EntityUtility.getBlockAt(pos, entity.getWorld());
    }

    public static double getVelocity() {
        return Math.hypot(EntityUtility.mc.player.getVelocity().x, EntityUtility.mc.player.getVelocity().z);
    }

    public static Block getBlockStandingOnPlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.world == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.getBlockPos();
        return EntityUtility.getBlockAt(pos, (World)EntityUtility.mc.world);
    }

    public static Block getBlockAt(BlockPos pos, World world) {
        return world.getBlockState(pos).getBlock();
    }

    public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (moveForward < 0.0) {
            forward = -0.5f;
        } else if (moveForward > 0.0) {
            forward = 0.5f;
        }
        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0f * forward;
        }
        if (moveStrafing < 0.0) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static boolean isInGame() {
        return EntityUtility.mc.player != null && EntityUtility.mc.world != null;
    }

    public static float getHealth(PlayerEntity ent) {
        if (ent == null) {
            return 0.0f;
        }
        if (!ServerUtility.isServerForHPFix()) {
            return ent.getHealth() + ent.getAbsorptionAmount();
        }
        ScoreboardObjective scoreBoard = ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
        if (scoreBoard != null) {
            ReadableScoreboardScore score = ent.getScoreboard().getScore((ScoreHolder)ent, scoreBoard);
            String text = ReadableScoreboardScore.getFormattedScore((ReadableScoreboardScore)score, (NumberFormat)scoreBoard.getNumberFormatOr((NumberFormat)StyledNumberFormat.EMPTY)).getString();
            String digits = text.replaceAll("[^0-9.]", "");
            try {
                return Float.parseFloat(digits);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return ent.getMaxHealth();
    }

    public static boolean isHoldingWeapon() {
        if (EntityUtility.mc.player == null) {
            return false;
        }
        ItemStack heldStack = EntityUtility.mc.player.getMainHandStack();
        Item heldItem = heldStack.getItem();
        if (heldStack.isEmpty()) {
            return false;
        }
        return heldItem instanceof SwordItem || heldItem instanceof AxeItem || heldItem instanceof TridentItem || heldItem instanceof MaceItem;
    }

    @Generated
    private EntityUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setTimer(float timer) {
        EntityUtility.timer = timer;
    }

    @Generated
    public static float getTimer() {
        return timer;
    }
}

