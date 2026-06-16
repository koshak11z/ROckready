package im.zov4ik.features.impl.render;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.display.hud.HudTheme;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.events.render.DrawEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Arrows extends Module {
    Animation radiusAnim = new Decelerate().setMs(150).setValue(6);

    SliderSettings radiusSetting = new SliderSettings("Radius", "Arrow radius")
            .setValue(38).range(25, 72);

    SliderSettings sizeSetting = new SliderSettings("Size", "Arrow size")
            .setValue(6.5F).range(5, 12);

    public Arrows() {
        super("Arrows", "Arrows", ModuleCategory.RENDER);
        setup(radiusSetting, sizeSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player != null) {
            radiusAnim.setDirection(mc.player.isSprinting() ? Direction.FORWARDS : Direction.BACKWARDS);
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        if (mc.player == null || mc.world == null || mc.options.hudHidden
                || !mc.options.getPerspective().equals(Perspective.FIRST_PERSON)) {
            return;
        }

        MatrixStack matrix = e.getDrawContext().getMatrices();
        DrawContext context = e.getDrawContext();
        FontRenderer distFont = Fonts.getSize(8, Fonts.Type.MANROPEBOLD);

        List<AbstractClientPlayerEntity> players = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && p.getHealth() > 0)
                .filter(p -> !isGhostPlayer(p))
                .toList();

        float middleW = mc.getWindow().getScaledWidth() / 2f;
        float middleH = mc.getWindow().getScaledHeight() / 2f;
        float radius = radiusSetting.getValue() + radiusAnim.getOutput().floatValue();
        float size = sizeSetting.getValue();
        List<LabelRect> labels = new ArrayList<>();

        for (AbstractClientPlayerEntity player : players) {
            float yaw = getRotations(player) - mc.player.getYaw();
            double rad = Math.toRadians(yaw);
            float arrowX = middleW + (float) Math.sin(rad) * radius;
            float arrowY = middleH - (float) Math.cos(rad) * radius;

            matrix.push();
            matrix.translate(arrowX, arrowY, 0.0F);
            matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
            drawChevron(context, size);
            matrix.pop();

            String distance = Math.round(mc.player.getPos().distanceTo(player.getPos())) + "m";
            float labelWidth = distFont.getStringWidth(distance);
            float labelX = arrowX - labelWidth / 2.0F;
            float labelY = arrowY + size * 0.50F + 1.3F;
            LabelRect label = new LabelRect(labelX, labelY - 1.0F, labelWidth, 6.0F);
            if (labels.stream().noneMatch(label::intersects)) {
                distFont.drawString(matrix, distance, labelX, labelY,
                        new Color(235, 236, 242, 202).getRGB());
                labels.add(label);
            }
        }
    }

    private void drawChevron(DrawContext context, float size) {
        int shadow = new Color(0, 0, 0, 132).getRGB();
        int stroke = new Color(247, 248, 252, 240).getRGB();

        HudTheme.icon(context, HudTheme.ICON_CHEVRON_UP, -size / 2.0F + 0.6F, -size / 2.0F + 0.8F, size, shadow);
        HudTheme.icon(context, HudTheme.ICON_CHEVRON_UP, -size / 2.0F, -size / 2.0F, size, stroke);
    }

    private boolean isGhostPlayer(AbstractClientPlayerEntity player) {
        if (player.getCustomName() != null) {
            String name = player.getCustomName().getString();
            return name != null && name.startsWith("Ghost_");
        }
        return player.getClass().getSimpleName().equals("OtherClientPlayerEntity")
                && player.getPitch() == -30.0f;
    }

    public static float getRotations(Entity entity) {
        double x = Calculate.interpolate(entity.getX(), entity.getX()) - Calculate.interpolate(mc.player.getX(), mc.player.getX());
        double z = Calculate.interpolate(entity.getZ(), entity.getZ()) - Calculate.interpolate(mc.player.getZ(), mc.player.getZ());
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    private record LabelRect(float x, float y, float width, float height) {
        private boolean intersects(LabelRect other) {
            return x < other.x + other.width && x + width > other.x
                    && y < other.y + other.height && y + height > other.y;
        }
    }
}
