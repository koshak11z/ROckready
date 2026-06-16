package im.zov4ik.display.hud;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.chat.StringHelper;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.math.time.StopWatch;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.Registries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoolDowns extends AbstractDraggable {
    public static CoolDowns getInstance() {
        return Instance.getDraggable(CoolDowns.class);
    }

    public final List<CoolDown> list = new ArrayList<>();
    private long lastItemChange = 0L;
    private int currentItemIndex = 0;
    private float animatedWidth;

    private static final Item[] EXAMPLE_ITEMS = {
            Items.ENDER_EYE, Items.ENDER_PEARL, Items.SUGAR, Items.MACE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.TRIDENT, Items.CROSSBOW, Items.DRIED_KELP, Items.NETHERITE_SCRAP
    };

    private static final float HEADER_HEIGHT = 16.5F;
    private static final float ROW_HEIGHT = 13.5F;
    private static final float ROW_GAP = 1.0F;
    private static final float MIN_WIDTH = 110.0F;

    public CoolDowns() {
        super("Cool Downs", 10, 40, 110, 34, true);
        this.animatedWidth = 110.0F;
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected(getName())
                && Hud.getInstance().state
                && (!list.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        list.removeIf(c -> c.anim.isFinished(Direction.BACKWARDS));

        if (!PlayerInteractionHelper.nullCheck()) {
            list.stream()
                    .filter(c -> !mc.player.getItemCooldownManager().isCoolingDown(c.item.getDefaultStack()))
                    .forEach(coolDown -> coolDown.anim.setDirection(Direction.BACKWARDS));
        } else {
            list.forEach(coolDown -> coolDown.anim.setDirection(Direction.BACKWARDS));
        }

        if (list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastItemChange >= 1000L) {
                currentItemIndex = (currentItemIndex + 1) % EXAMPLE_ITEMS.length;
                lastItemChange = currentTime;
            }
        }
    }

    @Override
    public void packet(PacketEvent e) {
        if (PlayerInteractionHelper.nullCheck()) return;

        switch (e.getPacket()) {
            case CooldownUpdateS2CPacket c -> {
                Item item = Registries.ITEM.get(c.cooldownGroup());
                list.stream()
                        .filter(coolDown -> coolDown.item.equals(item))
                        .forEach(coolDown -> coolDown.anim.setDirection(Direction.BACKWARDS));

                if (c.cooldown() != 0) {
                    list.add(new CoolDown(
                            item,
                            new StopWatch().setMs(-c.cooldown() * 50L),
                            new Decelerate().setMs(150).setValue(1.0F)
                    ));
                }
            }
            case PlayerRespawnS2CPacket p -> list.clear();
            default -> {}
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();

        FontRenderer headerFont = Fonts.getSize(19, Fonts.Type.DEFAULT);
        FontRenderer rowFont = Fonts.getSize(17, Fonts.Type.DEFAULT);
        FontRenderer headerIconFont = Fonts.getSize(25, Fonts.Type.ICONS);

        boolean drawExample = list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen);

        float targetWidth = calculateTargetWidth(headerFont, headerIconFont, rowFont, drawExample);
        animateWidth(targetWidth);

        setWidth((int) animatedWidth);
        setHeight((int) calculateHeight(drawExample));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 5.5F);

        drawHeader(matrix, headerFont, headerIconFont);

        if (drawExample) {
            drawExampleRow(matrix, rowFont);
        } else {
            drawCooldownRows(matrix, rowFont);
        }
    }

    private void animateWidth(float targetWidth) {
        if (animatedWidth <= 0.0F) animatedWidth = targetWidth;
        animatedWidth += (targetWidth - animatedWidth) * 0.18F;
        if (Math.abs(targetWidth - animatedWidth) < 0.25F) animatedWidth = targetWidth;
    }

    private float calculateTargetWidth(FontRenderer headerFont, FontRenderer headerIconFont, FontRenderer rowFont, boolean drawExample) {
        float maxWidth = MIN_WIDTH;
        float headerWidth = 6.0F + headerFont.getStringWidth("Cooldowns") + 5.0F + headerIconFont.getStringWidth("D") + 6.0F;
        maxWidth = Math.max(maxWidth, headerWidth);

        if (drawExample) {
            return Math.max(maxWidth, calculateRowWidth(rowFont, "CoolDown", "**:**"));
        }

        for (CoolDown coolDown : list) {
            if (coolDown.anim.getOutput().floatValue() <= 0.0F) continue;
            String name = coolDown.item.getDefaultStack().getName().getString();
            String duration = getCooldownDuration(coolDown);
            maxWidth = Math.max(maxWidth, calculateRowWidth(rowFont, name, duration));
        }

        return maxWidth;
    }

    private float calculateRowWidth(FontRenderer rowFont, String name, String duration) {
        return 5.0F + 8.0F + 4.0F + rowFont.getStringWidth(name) + 10.0F + rowFont.getStringWidth(duration) + 8.0F;
    }

    private float calculateHeight(boolean drawExample) {
        if (drawExample) return HEADER_HEIGHT + ROW_GAP + ROW_HEIGHT;
        float totalHeight = HEADER_HEIGHT;
        boolean hasVisible = false;
        for (CoolDown cd : list) {
            float anim = cd.anim.getOutput().floatValue();
            if (anim <= 0.0F) continue;
            totalHeight += ROW_GAP + ROW_HEIGHT * anim;
            hasVisible = true;
        }
        if (!hasVisible) totalHeight += ROW_GAP + ROW_HEIGHT;
        return totalHeight;
    }

    private void drawHeader(MatrixStack matrix, FontRenderer headerFont, FontRenderer headerIconFont) {
        headerFont.drawString(matrix, "Cooldowns", getX() + 3.0F, getY() + 5.0F, HudTheme.TEXT);

        float iconX = getX() + getWidth() - headerIconFont.getStringWidth("D") - 8.0F;
        headerIconFont.drawString(matrix, "D", iconX, getY() + 4.5F, HudTheme.TEXT);
    }

    private void drawExampleRow(MatrixStack matrix, FontRenderer rowFont) {
        Item item = EXAMPLE_ITEMS[currentItemIndex];
        drawRow(matrix, rowFont, getY() + HEADER_HEIGHT + ROW_GAP, item, "CoolDown", "**:**", false);
    }

    private void drawCooldownRows(MatrixStack matrix, FontRenderer rowFont) {
        float rowY = getY() + HEADER_HEIGHT + ROW_GAP;
        float centerX = getX() + getWidth() / 2.0F;

        for (CoolDown coolDown : list) {
            float animation = coolDown.anim.getOutput().floatValue();
            if (animation <= 0.0F) continue;

            float currentRowY = rowY;
            String name = coolDown.item.getDefaultStack().getName().getString();
            String duration = getCooldownDuration(coolDown);
            boolean isLow = isLowCooldown(coolDown);

            Calculate.scale(matrix, centerX, currentRowY + ROW_HEIGHT / 2.0F, 1.0F, animation, () -> {
                drawRow(matrix, rowFont, currentRowY, coolDown.item, name, duration, isLow);
            });

            rowY += ROW_HEIGHT * animation + ROW_GAP;
        }
    }

    private void drawRow(MatrixStack matrix, FontRenderer rowFont, float rowY, Item item, String name, String duration, boolean isLow) {
        int textColor = HudTheme.TEXT;
        int accentColor = isLow ? HudTheme.WARN : HudTheme.ACCENT;

        // Colored dot for item
        HudTheme.dot(matrix, getX() + 5.0F, rowY + 6.5F, 5.0F, accentColor);

        // Item icon
        Render2D.drawStack(matrix, item.getDefaultStack(), getX() + 10.0F, rowY + 2.75F, false, 0.5F);

        // Name
        rowFont.drawString(matrix, name, getX() + 22.0F, rowY + 4.0F, textColor);

        // Duration (right-aligned, accent)
        float durationW = rowFont.getStringWidth(duration);
        rowFont.drawString(matrix, duration, getX() + getWidth() - durationW - 6.0F, rowY + 4.0F, accentColor);
    }

    private boolean isLowCooldown(CoolDown coolDown) {
        long elapsed = coolDown.time.elapsedTime();
        int seconds = (int) (-elapsed / 1000L);
        return seconds <= 3;
    }

    private String getCooldownDuration(CoolDown coolDown) {
        long elapsedTime = coolDown.time.elapsedTime();
        int time;
        if (elapsedTime >= Integer.MIN_VALUE && elapsedTime <= Integer.MAX_VALUE) {
            time = (int) (-elapsedTime / 1000L);
        } else {
            time = elapsedTime < 0L ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        return StringHelper.getDuration(time);
    }

    public record CoolDown(Item item, StopWatch time, Animation anim) {}
}
