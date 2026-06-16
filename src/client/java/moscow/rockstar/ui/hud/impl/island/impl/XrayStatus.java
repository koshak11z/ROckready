/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 */
package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.XRay;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.ExtandableStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class XrayStatus
extends ExtandableStatus
implements IMinecraft {
    private static final Item[] ORES = new Item[]{Items.ANCIENT_DEBRIS, Items.DIAMOND_ORE, Items.GOLD_ORE, Items.LAPIS_ORE};
    private static final String[] LABELS = new String[]{"\u0414\u0440\u0435\u0432\u043d\u0438\u0435 \u043e\u0431\u043b\u043e\u043c\u043a\u0438: ", "\u0410\u043b\u043c\u0430\u0437\u043d\u0430\u044f \u0440\u0443\u0434\u0430: ", "\u0417\u043e\u043b\u043e\u0442\u0430\u044f \u0440\u0443\u0434\u0430: ", "\u041b\u0430\u0437\u0443\u0440\u0438\u0442\u043e\u0432\u0430\u044f \u0440\u0443\u0434\u0430: "};

    public XrayStatus(SelectSetting setting) {
        super(setting, "xray");
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        XRay xRay = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
        if (!xRay.isEnabled() || !this.haveOres(xRay)) {
            return;
        }
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        float expHeight = 25 + this.visibleOres(xRay) * 15;
        float expWidth = 114.0f;
        float maxWidth = 90.0f;
        float defaultWidth = 32.0f + Fonts.MEDIUM.getFont(7.0f).width("\u041d\u0430\u0439\u0434\u0435\u043d\u043e \u0430\u043b\u043c\u0430\u0437\u043e\u0432: " + xRay.getDiamonds());
        this.size.width = island.isExtended() ? expWidth : Math.min(defaultWidth, maxWidth);
        float width = this.size.width;
        this.size.height = island.isExtended() ? expHeight : 15.0f;
        float height = this.size.height;
        float extending = island.getExtendingAnim().getValue();
        if (extending != 0.0f) {
            if (extending > 0.7f) {
                float baseY = y + 20.0f;
                float alpha = 255.0f * extending;
                int entryCount = 0;
                context.drawText(Fonts.MEDIUM.getFont(7.0f), "\u041d\u0430\u0439\u0434\u0435\u043d\u043e: ", x + 25.0f - 11.0f * this.animation.getValue(), y + 10.0f, Colors.getTextColor().withAlpha(255.0f * extending));
                for (int i = 0; i < ORES.length; ++i) {
                    int count = this.getOreCount(xRay, i);
                    if (count == 0) continue;
                    float entryY = baseY + (float)(entryCount * 15);
                    this.drawOreEntry(context, x + 25.0f - 11.0f * this.animation.getValue(), entryY, ORES[i], LABELS[i] + count, alpha * 0.7f);
                    ++entryCount;
                }
            }
        } else {
            context.drawRoundedRect(x - 6.0f + 10.0f * this.animation.getValue(), y + 4.0f, 7.0f, 7.0f, BorderRadius.all(3.0f), new ColorRGBA(115.0f, 0.0f, 255.0f));
            context.drawText(Fonts.MEDIUM.getFont(7.0f), "\u041d\u0430\u0439\u0434\u0435\u043d\u043e \u0430\u043b\u043c\u0430\u0437\u043e\u0432: " + xRay.getDiamonds(), x + 25.0f - 10.0f * this.animation.getValue(), y + 5.0f, Colors.getTextColor());
        }
    }

    private int getOreCount(XRay xRay, int index) {
        return switch (index) {
            case 0 -> xRay.getAncient();
            case 1 -> xRay.getDiamonds();
            case 2 -> xRay.getGold();
            case 3 -> xRay.getLapis();
            default -> 0;
        };
    }

    private int visibleOres(XRay xRay) {
        int count = 0;
        if (xRay.getAncient() > 0) {
            ++count;
        }
        if (xRay.getDiamonds() > 0) {
            ++count;
        }
        if (xRay.getGold() > 0) {
            ++count;
        }
        if (xRay.getLapis() > 0) {
            ++count;
        }
        return count;
    }

    private boolean haveOres(XRay xRay) {
        return (xRay.getAncient() > 0 || xRay.getDiamonds() > 0 || xRay.getGold() > 0 || xRay.getLapis() > 0) && xRay.isEnabled();
    }

    private void drawOreEntry(CustomDrawContext context, float x, float y, Item ore, String text, float alpha) {
        context.drawItem(ore, x - 1.0f, y, 0.75f);
        context.drawText(Fonts.MEDIUM.getFont(7.0f), text, x + 15.0f, y + 3.0f, Colors.getTextColor().withAlpha(alpha));
    }

    @Override
    public boolean canShow() {
        XRay xRay = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
        return this.haveOres(xRay);
    }
}

