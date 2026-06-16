package im.zov4ik.display.hud;

import im.zov4ik.common.animation.Direction;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.features.module.Module;
import im.zov4ik.utils.client.chat.StringHelper;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.zov4ik;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class HotKeys extends AbstractDraggable {
    private final List<Module> keysList = new ArrayList<>();
    private float animatedWidth = 88.0F;

    private static final float PAD = 8.5F;
    private static final float HEADER_H = 16.0F;
    private static final float ROW_H = 12.0F;
    private static final float MIN_W = 84.0F;
    private static final float ICON_SLOT = 7.0F;
    private static final float HEADER_ICON = 5.8F;
    private static final float ROW_ICON = 5.2F;

    public HotKeys() {
        super("Hot Keys", 300, 40, 88, 34, true);
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected("Hot Keys")
                && (!keysList.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        keysList.clear();
        keysList.addAll(zov4ik.getInstance().getModuleProvider().getModules().stream()
                .filter(module -> module.getAnimation().getOutput().floatValue() != 0.0F && module.getKey() != -1)
                .toList());
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer titleFont = Fonts.getSize(15, Fonts.Type.BOLD);
        FontRenderer rowFont = Fonts.getSize(14, Fonts.Type.REGULAR);
        FontRenderer bindFont = Fonts.getSize(13, Fonts.Type.SEMI);

        List<Row> rows = getRows();
        float targetWidth = MIN_W;
        for (Row row : rows) {
            targetWidth = Math.max(targetWidth, PAD * 2.0F + ICON_SLOT + 5.0F
                    + rowFont.getStringWidth(row.name) + bindFont.getStringWidth(row.bind) + 10.0F);
        }
        animatedWidth += (targetWidth - animatedWidth) * 0.20F;
        if (Math.abs(targetWidth - animatedWidth) < 0.35F) {
            animatedWidth = targetWidth;
        }

        setWidth((int) Math.ceil(animatedWidth));
        setHeight((int) Math.ceil(HEADER_H + rows.size() * ROW_H + 2.5F));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 4.2F);

        titleFont.drawString(matrix, "Hotkeys", getX() + PAD, getY() + 4.25F, HudTheme.TEXT);
        HudTheme.iconSlot(context, HudTheme.ICON_KEYBOARD, getX() + getWidth() - PAD - ICON_SLOT, getY() + 4.45F, ICON_SLOT, HEADER_ICON, HudTheme.ACCENT);
        HudTheme.hairline(matrix, getX() + PAD, getY() + HEADER_H, getWidth() - PAD * 2.0F);

        float rowY = getY() + HEADER_H + 1.9F;
        for (Row row : rows) {
            float anim = row.animation;
            float centerX = getX() + getWidth() / 2.0F;
            float currentY = rowY;
            Calculate.scale(matrix, centerX, currentY + ROW_H / 2.0F, 1.0F, anim, () -> {
                HudTheme.iconSlot(context, row.icon, getX() + PAD, currentY + 2.75F, ICON_SLOT, ROW_ICON, row.iconColor);
                rowFont.drawString(matrix, row.name, getX() + PAD + ICON_SLOT + 4.0F, currentY + 3.75F, HudTheme.TEXT);
                float bindW = bindFont.getStringWidth(row.bind);
                bindFont.drawString(matrix, row.bind, getX() + getWidth() - PAD - bindW, currentY + 3.80F, HudTheme.TEXT_DIM);
            });
            rowY += ROW_H * anim;
        }
    }

    private List<Row> getRows() {
        if (keysList.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            return List.of(new Row("ESP", "-", 1.0F, HudTheme.ICON_EYE, HudTheme.ACCENT));
        }
        return keysList.stream()
                .map(module -> new Row(module.getName(), formatBind(StringHelper.getBindName(module.getKey())),
                        module.getAnimation().getOutput().floatValue(), HudTheme.ICON_ZAP, HudTheme.ACCENT))
                .toList();
    }

    private String formatBind(String bind) {
        if (bind == null || bind.isBlank() || bind.equalsIgnoreCase("NONE")) {
            return "-";
        }
        return bind.toUpperCase();
    }

    private record Row(String name, String bind, float animation, net.minecraft.util.Identifier icon, int iconColor) {
    }
}
