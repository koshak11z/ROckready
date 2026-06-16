/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.RenderEvent;
import baritone.api.pathing.goals.*;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.interfaces.IGoalRenderPos;
import baritone.behavior.PathingBehavior;
import baritone.pathing.path.PathExecutor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.dimension.DimensionType;

/**
 * @author Brady
 * @since 8/9/2018
 */
public final class PathRenderer implements IRenderer {

    private static final Identifier TEXTURE_BEACON_BEAM = Identifier.of("textures/entity/beacon_beam.png");


    private PathRenderer() {}

    public static double posX() {
        return renderManager.renderPosX();
    }

    public static double posY() {
        return renderManager.renderPosY();
    }

    public static double posZ() {
        return renderManager.renderPosZ();
    }

    public static void render(RenderEvent event, PathingBehavior behavior) {
        final IPlayerContext ctx = behavior.ctx;
        if (ctx.world() == null) {
            return;
        }
        if (ctx.minecraft().currentScreen instanceof GuiClick) {
            ((GuiClick) ctx.minecraft().currentScreen).onRender(event.getModelViewStack(), event.getProjectionMatrix());
        }

        final float partialTicks = event.getPartialTicks();
        final Goal goal = behavior.getGoal();

        final DimensionType thisPlayerDimension = ctx.world().getDimension();
        final DimensionType currentRenderViewDimension = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().world().getDimension();

        if (thisPlayerDimension != currentRenderViewDimension) {
            // this is a path for a bot in a different dimension, don't render it
            return;
        }

        if (goal != null && settings.renderGoal.value) {
            drawGoal(event.getModelViewStack(), ctx, goal, partialTicks, settings.colorGoalBox.value);
        }

        if (!settings.renderPath.value) {
            return;
        }

        PathExecutor current = behavior.getCurrent(); // this should prevent most race conditions?
        PathExecutor next = behavior.getNext(); // like, now it's not possible for current!=null to be true, then suddenly false because of another thread
        if (current != null && settings.renderSelectionBoxes.value) {
            drawManySelectionBoxes(event.getModelViewStack(), ctx.player(), current.toBreak(), settings.colorBlocksToBreak.value);
            drawManySelectionBoxes(event.getModelViewStack(), ctx.player(), current.toPlace(), settings.colorBlocksToPlace.value);
            drawManySelectionBoxes(event.getModelViewStack(), ctx.player(), current.toWalkInto(), settings.colorBlocksToWalkInto.value);
        }

        //drawManySelectionBoxes(player, Collections.singletonList(behavior.pathStart()), partialTicks, Color.WHITE);

        // Render the current path, if there is one
        if (current != null && current.getPath() != null) {
            int renderBegin = Math.max(current.getPosition() - 3, 0);
            drawPath(event.getModelViewStack(), current.getPath().positions(), renderBegin, settings.colorCurrentPath.value, settings.fadePath.value, 10, 20);
        }

        if (next != null && next.getPath() != null) {
            drawPath(event.getModelViewStack(), next.getPath().positions(), 0, settings.colorNextPath.value, settings.fadePath.value, 10, 20);
        }

        // If there is a path calculation currently running, render the path calculation process
        behavior.getInProgress().ifPresent(currentlyRunning -> {
            currentlyRunning.bestPathSoFar().ifPresent(p -> {
                drawPath(event.getModelViewStack(), p.positions(), 0, settings.colorBestPathSoFar.value, settings.fadePath.value, 10, 20);
            });

            currentlyRunning.pathToMostRecentNodeConsidered().ifPresent(mr -> {
                drawPath(event.getModelViewStack(), mr.positions(), 0, settings.colorMostRecentConsidered.value, settings.fadePath.value, 10, 20);
                drawManySelectionBoxes(event.getModelViewStack(), ctx.player(), Collections.singletonList(mr.getDest()), settings.colorMostRecentConsidered.value);
            });
        });
    }

    public static void drawPath(MatrixStack stack, List<BetterBlockPos> positions, int startIndex, Color color, boolean fadeOut, int fadeStart0, int fadeEnd0) {
        drawPath(stack, positions, startIndex, color, fadeOut, fadeStart0, fadeEnd0, 0.5D);
    }

    public static void drawPath(MatrixStack stack, List<BetterBlockPos> positions, int startIndex, Color color, boolean fadeOut, int fadeStart0, int fadeEnd0, double offset) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

        int fadeStart = fadeStart0 + startIndex;
        int fadeEnd = fadeEnd0 + startIndex;

        for (int i = startIndex, next; i < positions.size() - 1; i = next) {
            BetterBlockPos start = positions.get(i);
            BetterBlockPos end = positions.get(next = i + 1);

            int dirX = end.x - start.x;
            int dirY = end.y - start.y;
            int dirZ = end.z - start.z;

            while (next + 1 < positions.size() && (!fadeOut || next + 1 < fadeStart) &&
                    (dirX == positions.get(next + 1).x - end.x &&
                            dirY == positions.get(next + 1).y - end.y &&
                            dirZ == positions.get(next + 1).z - end.z)) {
                end = positions.get(++next);
            }

            if (fadeOut) {
                float alpha;

                if (i <= fadeStart) {
                    alpha = 0.4F;
                } else {
                    if (i > fadeEnd) {
                        break;
                    }
                    alpha = 0.4F * (1.0F - (float) (i - fadeStart) / (float) (fadeEnd - fadeStart));
                }
                RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
            }

            emitPathLine(bufferBuilder, stack, start.x, start.y, start.z, end.x, end.y, end.z, offset);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private static void emitPathLine(BufferBuilder bufferBuilder, MatrixStack stack, double x1, double y1, double z1, double x2, double y2, double z2, double offset) {
        final double extraOffset = offset + 0.03D;

        double vpX = posX();
        double vpY = posY();
        double vpZ = posZ();
        boolean renderPathAsFrickinThingy = !settings.renderPathAsLine.value;

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x1 + offset - vpX), (float) (y1 + offset - vpY), (float) (z1 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x2 + offset - vpX), (float) (y2 + offset - vpY), (float) (z2 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        if (renderPathAsFrickinThingy) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x2 + offset - vpX), (float) (y2 + extraOffset - vpY), (float) (z2 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x2 + offset - vpX), (float) (y2 + extraOffset - vpY), (float) (z2 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x1 + offset - vpX), (float) (y1 + extraOffset - vpY), (float) (z1 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) (x1 + offset - vpX), (float) (y1 + offset - vpY), (float) (z1 + offset - vpZ)).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        }
    }

    public static void drawManySelectionBoxes(MatrixStack stack, Entity player, Collection<BlockPos> positions, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

        //BlockPos blockpos = movingObjectPositionIn.getBlockPos();
        BlockStateInterface bsi = new BlockStateInterface(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext()); // TODO this assumes same dimension between primary baritone and render view? is this safe?

        positions.forEach(pos -> {
            BlockState state = bsi.get0(pos);
            VoxelShape shape = state.getOutlineShape(player.getWorld(), pos);
            Box toDraw = shape.isEmpty() ? VoxelShapes.fullCube().getBoundingBox() : shape.getBoundingBox();
            toDraw = toDraw.offset(pos);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.minX, (float) toDraw.minY, (float) toDraw.minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.maxX, (float) toDraw.minY, (float) toDraw.minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.maxX, (float) toDraw.minY, (float) toDraw.maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.minX, (float) toDraw.minY, (float) toDraw.maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.minX, (float) toDraw.maxY, (float) toDraw.minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.maxX, (float) toDraw.maxY, (float) toDraw.minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.maxX, (float) toDraw.maxY, (float) toDraw.maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) toDraw.minX, (float) toDraw.maxY, (float) toDraw.maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        });

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawGoal(MatrixStack stack, IPlayerContext ctx, Goal goal, float partialTicks, Color color) {
        drawGoal(null, stack, ctx, goal, partialTicks, color, true);
    }

    private static void drawGoal(@Nullable BufferBuilder bufferBuilder, MatrixStack stack, IPlayerContext ctx, Goal goal, float partialTicks, Color color, boolean setupRender) {
        if (!setupRender && bufferBuilder == null) {
            throw new RuntimeException("BufferBuilder must not be null if setupRender is false");
        }
        double renderPosX = posX();
        double renderPosY = posY();
        double renderPosZ = posZ();
        double minX, maxX;
        double minZ, maxZ;
        double minY, maxY;
        double y, y1, y2;
        if (!settings.renderGoalAnimated.value) {
            // y = 1 causes rendering issues when the player is at the same y as the top of a block for some reason
            y = 0.999F;
        } else {
            y = MathHelper.cos((float) (((float) ((System.nanoTime() / 100000L) % 20000L)) / 20000F * Math.PI * 2));
        }
        if (goal instanceof IGoalRenderPos) {
            BlockPos goalPos = ((IGoalRenderPos) goal).getGoalPos();
            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y /= 2;
            }
            y1 = 1 + y + goalPos.getY() - renderPosY;
            y2 = 1 - y + goalPos.getY() - renderPosY;
            minY = goalPos.getY() - renderPosY;
            maxY = minY + 2;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y1 -= 0.5;
                y2 -= 0.5;
                maxY--;
            }
            drawDankLitGoalBox(bufferBuilder, stack, color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        } else if (goal instanceof GoalXZ) {
            GoalXZ goalPos = (GoalXZ) goal;
            minY = ctx.world().getBottomY();
            maxY = ctx.world().getTopYInclusive();

            if (settings.renderGoalXZBeacon.value) {
                //TODO: check
                textureManager.getTexture(TEXTURE_BEACON_BEAM).bindTexture();
                if (settings.renderGoalIgnoreDepth.value) {
                    RenderSystem.disableDepthTest();
                }

                stack.push(); // push
                stack.translate(goalPos.getX() - renderPosX, -renderPosY, goalPos.getZ() - renderPosZ); // translate

                //TODO: check
                BeaconBlockEntityRenderer.renderBeam(
                        stack,
                        ctx.minecraft().getBufferBuilders().getEntityVertexConsumers(),
                        TEXTURE_BEACON_BEAM,
                        settings.renderGoalAnimated.value ? partialTicks : 0,
                        1.0F,
                        settings.renderGoalAnimated.value ? ctx.world().getTime() : 0,
                        (int) minY,
                        (int) maxY,
                        color.getRGB(),

                        // Arguments filled by the private method lol
                        0.2F,
                        0.25F
                );

                stack.pop(); // pop

                if (settings.renderGoalIgnoreDepth.value) {
                    RenderSystem.enableDepthTest();
                }
                return;
            }

            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;

            y1 = 0;
            y2 = 0;
            minY -= renderPosY;
            maxY -= renderPosY;
            drawDankLitGoalBox(bufferBuilder, stack, color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        } else if (goal instanceof GoalComposite) {
            // Simple way to determine if goals can be batched, without having some sort of GoalRenderer
            boolean batch = Arrays.stream(((GoalComposite) goal).goals()).allMatch(IGoalRenderPos.class::isInstance);
            BufferBuilder buf = bufferBuilder;
            if (batch) {
                buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            }
            for (Goal g : ((GoalComposite) goal).goals()) {
                drawGoal(buf, stack, ctx, g, partialTicks, color, !batch);
            }
            if (batch) {
                BufferRenderer.drawWithGlobalProgram(buf.end());
            }
        } else if (goal instanceof GoalInverted) {
            drawGoal(stack, ctx, ((GoalInverted) goal).origin, partialTicks, settings.colorInvertedGoalBox.value);
        } else if (goal instanceof GoalYLevel) {
            GoalYLevel goalpos = (GoalYLevel) goal;
            minX = ctx.player().getPos().x - settings.yLevelBoxSize.value - renderPosX;
            minZ = ctx.player().getPos().z - settings.yLevelBoxSize.value - renderPosZ;
            maxX = ctx.player().getPos().x + settings.yLevelBoxSize.value - renderPosX;
            maxZ = ctx.player().getPos().z + settings.yLevelBoxSize.value - renderPosZ;
            minY = ((GoalYLevel) goal).level - renderPosY;
            maxY = minY + 2;
            y1 = 1 + y + goalpos.level - renderPosY;
            y2 = 1 - y + goalpos.level - renderPosY;
            drawDankLitGoalBox(bufferBuilder, stack, color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        }
    }

    private static void drawDankLitGoalBox(BufferBuilder bufferBuilder, MatrixStack stack, Color colorIn, double minX, double maxX, double minZ, double maxZ, double minY, double maxY, double y1, double y2, boolean setupRender) {
        if (setupRender) {
            bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        }

        renderHorizontalQuad(bufferBuilder, stack, minX, maxX, minZ, maxZ, y1);
        renderHorizontalQuad(bufferBuilder, stack, minX, maxX, minZ, maxZ, y2);

        for (double y = minY; y < maxY; y += 16) {
            double max = Math.min(maxY, y + 16);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) y, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) max, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) y, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) max, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) y, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) max, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) y, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) max, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        }

        if (setupRender) {
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }

    private static void renderHorizontalQuad(BufferBuilder bufferBuilder, MatrixStack stack, double minX, double maxX, double minZ, double maxZ, double y) {
        if (y != 0) {
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) y, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) y, (float) minZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) maxX, (float) y, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
            bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float) minX, (float) y, (float) maxZ).color(1f, 1f, 1f, 1f).normal(0, 1, 0);
        }
    }
}

