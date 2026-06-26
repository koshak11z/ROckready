package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.item.ItemStack;

/**
 * ArmorHUD: компактная горизонтальная строка из 4 ячеек брони (шлем→ботинки) + опц. предмет в руке.
 * Сверху каждой ячейки — прочность в процентах, под ней иконка. Пустой слот = чёрный крестик.
 */
public class ArmorHud extends HudList {
    private static final float ITEM = 12.0f;
    private static final float CELL = 15.0f;
    private static final float HEIGHT = 23.0f;
    private static final float ICON_Y = 8.0f;

    private final BooleanSetting durability = new BooleanSetting(this, "hud.armor.durability").enable();
    private final BooleanSetting mainHand = new BooleanSetting(this, "hud.armor.main_hand");

    public ArmorHud() {
        super("hud.armor", "icons/hud/player.png");
        this.showing = true;
        this.x = 4.0f;
        this.y = 150.0f;
    }

    /** Fixed cells (helmet→boots), each may be empty (empty = cross). Main hand appended if enabled. */
    private List<ItemStack> cells() {
        List<ItemStack> list = new ArrayList<>();
        if (mc.player == null) {
            return list;
        }
        for (int i = 3; i >= 0; i--) {
            list.add(mc.player.getInventory().armor.get(i));
        }
        if (this.mainHand.isEnabled()) {
            list.add(mc.player.getMainHandStack());
        }
        return list;
    }

    @Override
    public void update(UIContext context) {
        int n = Math.max(1, this.cells().size());
        this.width = n * CELL + 6.0f;
        this.height = HEIGHT;
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        List<ItemStack> cells = this.cells();
        Font pctFont = Fonts.MEDIUM.getFont(6.0f);

        Glyphs.background(context, this.x, this.y, this.width, Math.max(18.0f, this.height), 5.0f, this.animation.getValue());

        float startX = this.x + (this.width - cells.size() * CELL) / 2.0f;
        for (int i = 0; i < cells.size(); i++) {
            ItemStack stack = cells.get(i);
            float cellLeft = startX + i * CELL;
            float iconX = cellLeft + (CELL - ITEM) / 2.0f;
            float iconY = this.y + ICON_Y;

            if (stack.isEmpty()) {
                float ccx = iconX + ITEM / 2.0f;
                float ccy = iconY + ITEM / 2.0f;
                float r = 3.0f;
                ColorRGBA cross = ColorRGBA.BLACK.withAlpha(235.0f);
                Glyphs.thickLine(context, ccx - r, ccy - r, ccx + r, ccy + r, 1.3f, cross);
                Glyphs.thickLine(context, ccx - r, ccy + r, ccx + r, ccy - r, 1.3f, cross);
                continue;
            }

            if (this.durability.isEnabled() && stack.isDamageable() && stack.getMaxDamage() > 0) {
                float fraction = Math.max(0.0f, 1.0f - (float) stack.getDamage() / (float) stack.getMaxDamage());
                ColorRGBA color = ColorRGBA.fromHSB(fraction * 0.33f, 0.75f, 0.95f);
                String pct = Math.round(fraction * 100.0f) + "%";
                context.drawCenteredText(pctFont, pct, cellLeft + CELL / 2.0f, this.y + 2.5f, color);
            }
            // size is a SCALE (1.0 == 16px); ITEM/16 keeps the icon at ITEM px.
            context.drawItem(stack, iconX, iconY, ITEM / 16.0f);
        }
    }

    @Override
    public boolean show() {
        return mc.player != null && mc.world != null;
    }
}
