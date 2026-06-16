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

import java.util.ArrayList;
import java.util.List;

/**
 * Cool Downs — styled to match HotKeys (header + rows with name + duration).
 */
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

    private static final float HEADER_HEIGHT = 20.0F;
    private static final float ROW_HEIGHT = 15.0F;
    private static final float ROW_GAP = 2.0F;
    private static final float MIN_WIDTH = 100.0F;
    private static final float PADDING_X = 6.0F;

    public CoolDowns() {
        super("Cool Downs", 10, 40, 100, 34, true);
        this.animatedWidth = 100.0F;
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

        FontRenderer headerFont = Fonts.getSize(18, Fonts.Type.BOLD);
        FontRenderer nameFont = Fonts.getSize(15, Fonts.Type.DEFAULT);
        FontRenderer durationFont = Fonts.getSize(14, Fonts.Type.SEMI);
        FontRenderer headerIconFont = Fonts.getSize(20, Fonts.Type.ICONS);

        boolean drawExample = list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen);

        float targetWidth = calculateTargetWidth(headerFont, nameFont, durationFont, drawExample);
        animateWidth(targetWidth);

        setWidth((int) animatedWidth);
        setHeight((int) calculateHeight(drawExample));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 5.5F);

        drawHeader(matrix, headerFont, headerIconFont);

        if (drawExample) {
            drawExampleRow(matrix, nameFont, durationFont);
        } else {
            drawCooldownRows(matrix, nameFont, durationFont);
        }
    }

    private void animateWidth(float targetWidth) {
        if (animatedWidth <= 0.0F) animatedWidth = targetWidth;
        animatedWidth += (targetWidth - animatedWidth) * 0.18F;
        if (Math.abs(targetWidth - animatedWidth) < 0.25F) animatedWidth = targetWidth;
    }

    private float calculateTargetWidth(FontRenderer headerFont, FontRenderer nameFont, FontRenderer durationFont, boolean drawExample) {
        float maxWidth = MIN_WIDTH;
        float headerWidth = PADDING_X + headerFont.getStringWidth("Cool Downs") + 25.0F;
        maxWidth = Math.max(maxWidth, headerWidth);

        if (drawExample) {
            float rowW = PADDING_X + nameFont.getStringWidth("CoolDown") + 15.0F + durationFont.getStringWidth("**:**") + PADDING_X;
            return Math.max(maxWidth, rowW);
        }

        for (CoolDown coolDown : list) {
            float animation = coolDown.anim.getOutput().floatValue();
            if (animation <= 0.0F) continue;
            String name = coolDown.item.getDefaultStack().getName().getString();
            String duration = getCooldownDuration(coolDown);
            float rowW = PADDING_X + nameFont.getStringWidth(name) + 15.0F + durationFont.getStringWidth(duration) + PADDING_X;
            maxWidth = Math.max(maxWidth, rowW);
        }
        return maxWidth;
    }

    private float calculateHeight(boolean drawExample) {
        if (drawExample) return HEADER_HEIGHT + ROW_GAP + ROW_HEIGHT + 4.0F;
        float totalHeight = HEADER_HEIGHT;
        boolean hasVisible = false;
        for (CoolDown coolDown : list) {
            float animation = coolDown.anim.getOutput().floatValue();
            if (animation <= 0.0F) continue;
            totalHeight += ROW_GAP + ROW_HEIGHT * animation;
            hasVisible = true;
        }
        if (!hasVisible) totalHeight += ROW_GAP + ROW_HEIGHT;
        return totalHeight + 4.0F;
    }

    private void drawHeader(MatrixStack matrix, FontRenderer headerFont, FontRenderer headerIconFont) {
        headerFont.drawString(matrix, "Cool Downs", getX() + PADDING_X, getY() + 6.0F, HudTheme.TEXT);
        headerIconFont.drawString(matrix, "D", getX() + getWidth() - 18.0F, getY() + 5.0F, HudTheme.ACCENT);
    }

    private void drawExampleRow(MatrixStack matrix, FontRenderer nameFont, FontRenderer durationFont) {
        drawRow(matrix, nameFont, durationFont, getY() + HEADER_HEIGHT + ROW_GAP, "CoolDown", "**:**");
    }

    private void drawCooldownRows(MatrixStack matrix, FontRenderer nameFont, FontRenderer durationFont) {
        float rowY = getY() + HEADER_HEIGHT + ROW_GAP;
        float centerX = getX() + getWidth() / 2.0F;

        for (CoolDown coolDown : list) {
            float animation = coolDown.anim.getOutput().floatValue();
            if (animation <= 0.0F) continue;

            float currentRowY = rowY;
            String name = coolDown.item.getDefaultStack().getName().getString();
            String duration = getCooldownDuration(coolDown);

            Calculate.scale(matrix, centerX, currentRowY + ROW_HEIGHT / 2.0F, 1.0F, animation, () -> {
                drawRow(matrix, nameFont, durationFont, currentRowY, name, duration);
            });

            rowY += ROW_HEIGHT * animation + ROW_GAP;
        }
    }

    private void drawRow(MatrixStack matrix, FontRenderer nameFont, FontRenderer durationFont,
                         float rowY, String name, String duration) {
        nameFont.drawString(matrix, name, getX() + PADDING_X, rowY + 4.0F, HudTheme.TEXT);
        float durationWidth = durationFont.getStringWidth(duration);
        durationFont.drawString(matrix, duration, getX() + getWidth() - PADDING_X - durationWidth, rowY + 4.5F, HudTheme.ACCENT);
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
