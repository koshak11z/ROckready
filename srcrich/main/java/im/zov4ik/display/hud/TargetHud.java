package im.zov4ik.display.hud;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.features.impl.combat.Aura;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.math.time.StopWatch;
import im.zov4ik.zov4ik;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class TargetHud extends AbstractDraggable {
    private final Animation faceAlphaAnimation = new Decelerate().setMs(125).setValue(1);
    private final StopWatch stopWatch = new StopWatch();
    private LivingEntity lastTarget;
    private float health;
    private float absorption;

    private static final float PANEL_H = 38.0F;
    private static final float MIN_W = 132.0F;
    private static final float PAD = 8.5F;
    private static final float FACE = 25.0F;
    private static final float BAR_H = 2.0F;
    private static final float ITEM_SCALE = 0.31F;
    private static final float ITEM_STEP = 6.7F;

    public TargetHud() {
        super("Target Hud", 10, 80, 116, 36, true);
    }

    @Override
    public boolean visible() {
        return scaleAnimation.isDirection(Direction.FORWARDS);
    }

    @Override
    public void tick() {
        LivingEntity auraTarget = Aura.getInstance().getTarget();
        if (auraTarget != null) {
            lastTarget = auraTarget;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (PlayerInteractionHelper.isChat(mc.currentScreen)) {
            lastTarget = mc.player;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (stopWatch.finished(500)) {
            stopAnimation();
            faceAlphaAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (Hud.getInstance().interfaceSettings.isSelected("Target Hud") && Hud.getInstance().state && lastTarget != null) {
            drawPanel(context, context.getMatrices());
        }
    }

    private void drawPanel(DrawContext context, MatrixStack matrix) {
        FontRenderer nameFont = Fonts.getSize(14, Fonts.Type.SEMI);
        FontRenderer hpFont = Fonts.getSize(12, Fonts.Type.SEMI);

        float hp = PlayerInteractionHelper.getHealth(lastTarget);
        String hpString = (lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime())
                ? "??" : PlayerInteractionHelper.getHealthString(hp);
        health = MathHelper.clamp(Calculate.interpolateSmooth(1, health, hp / lastTarget.getMaxHealth()), 0, 1);
        absorption = MathHelper.clamp(Calculate.interpolateSmooth(1, absorption, lastTarget.getAbsorptionAmount() / 20.0F), 0, 1);

        String name = lastTarget.getName().getString();
        float contentX = getX() + PAD + FACE + 6.0F;
        float hpBlockW = 7.0F + 3.0F + hpFont.getStringWidth(hpString);
        float wantedW = Math.max(MIN_W, contentX - getX() + Math.min(nameFont.getStringWidth(name), 64.0F) + hpBlockW + 13.0F);

        setWidth((int) Math.ceil(wantedW));
        setHeight((int) PANEL_H);

        HudTheme.panel(matrix, getX(), getY(), getWidth(), PANEL_H, 4.2F);
        drawFace(context);

        float nameY = getY() + 6.4F;
        float hpX = getX() + getWidth() - PAD - hpBlockW;
        float maxNameW = Math.max(32.0F, hpX - (contentX + 11.0F) - 6.0F);
        HudTheme.iconSlot(context, HudTheme.ICON_TARGET, contentX, getY() + 5.55F, 7.0F, 5.6F, HudTheme.ACCENT);
        drawName(matrix, nameFont, name, contentX + 11.0F, nameY, maxNameW);

        HudTheme.iconSlot(context, HudTheme.ICON_HEART, hpX, getY() + 5.65F, 7.0F, 5.4F, HudTheme.ACCENT);
        hpFont.drawString(matrix, hpString, hpX + 9.0F, getY() + 7.45F, HudTheme.ACCENT);

        drawItemStrip(context, contentX, getY() + 18.7F);

        float barX = contentX;
        float barY = getY() + PANEL_H - 5.6F;
        float barW = getWidth() - (barX - getX()) - PAD;
        HudTheme.track(matrix, barX, barY, barW, BAR_H);
        HudTheme.accentBar(matrix, barX, barY, barW, BAR_H, health);

        if (absorption > 0.0F && !Network.isFunTime()) {
            rectangle.render(ShapeProperties.create(matrix, barX, barY - 1.6F, barW * absorption, 0.85F)
                    .round(0.45F)
                    .color(new Color(255, 204, 72, 210).getRGB())
                    .build());
        }
    }

    private void drawItemStrip(DrawContext context, float x, float y) {
        ItemStack[] slots = new ItemStack[] {
                lastTarget.getEquippedStack(EquipmentSlot.HEAD),
                lastTarget.getEquippedStack(EquipmentSlot.CHEST),
                lastTarget.getEquippedStack(EquipmentSlot.LEGS),
                lastTarget.getEquippedStack(EquipmentSlot.FEET),
                lastTarget.getMainHandStack(),
                lastTarget.getOffHandStack()
        };

        float itemX = x;
        int drawn = 0;
        for (ItemStack stack : slots) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            Render2D.defaultDrawStack(context, stack, itemX, y, false, false, ITEM_SCALE);
            itemX += ITEM_STEP;
            if (++drawn >= 6) {
                break;
            }
        }
    }

    private void drawName(MatrixStack matrix, FontRenderer font, String name, float x, float y, float width) {
        if (font.getStringWidth(name) <= width) {
            font.drawString(matrix, name, x, y, HudTheme.TEXT);
            return;
        }
        ScissorAssist scissor = zov4ik.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x, getY(), width, PANEL_H);
        font.drawString(matrix, name, x, y, HudTheme.TEXT);
        scissor.pop();
    }

    private void drawFace(DrawContext context) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer<?, ?, ?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer =
                (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(lastTarget, tickCounter.getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);
        float alpha = faceAlphaAnimation.getOutput().floatValue();
        rectangle.render(ShapeProperties.create(context.getMatrices(),
                        getX() + PAD, getY() + (PANEL_H - FACE) / 2.0F,
                        FACE, FACE)
                .round(4.0F)
                .thickness(0.75F)
                .outlineColor(HudTheme.PANEL_BORDER)
                .color(new Color(9, 10, 14, 218).getRGB())
                .build());
        Calculate.setAlpha(alpha, () -> Render2D.drawTexture(context, textureLocation,
                getX() + PAD, getY() + (PANEL_H - FACE) / 2.0F,
                FACE, 4.0F, 8, 8, 64,
                ColorAssist.getRect(1),
                ColorAssist.multRed(-1, 1 + lastTarget.hurtTime / 4F)));
    }
}
