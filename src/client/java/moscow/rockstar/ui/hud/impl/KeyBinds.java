package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.Comparator;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * Hotkeys panel redesigned to the reference: a black rounded panel with a "Hotkeys" header + icon
 * and rows of [module name] ... [keycap with the bound key].
 */
public class KeyBinds extends HudList {
    private static final float HEADER = 20.0f;
    private static final float ROW = 18.0f;

    private int lastSize = -1;
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");

    public KeyBinds() {
        super("hud.keybinds", "icons/hud/keybinds.png");
        this.showing = true;
    }

    @Override
    public void update(UIContext context) {
        this.width = 96.0f;
        this.height = HEADER;
        Font font = Fonts.MEDIUM.getFont(7.0f);
        boolean any = false;
        for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            boolean forward = module.isEnabled() && module.getKey() != -1;
            module.getKeybindsAnimation().update(forward);
            module.getKeybindsAnimation().setEasing(Easing.BAKEK);
            float a = module.getKeybindsAnimation().getValue();
            if (a > 0.0f) {
                any = true;
                String key = TextUtility.getKeyName(module.getKey());
                float rowWidth = 10.0f + font.width(module.getName()) + 16.0f + (font.width(key) + 8.0f) + 10.0f;
                this.width = Math.max(rowWidth, this.width);
            }
            this.height += ROW * a;
        }
        if (any) {
            this.height += 4.0f;
        }
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        Font header = Fonts.SEMIBOLD.getFont(7.5f);
        Font font = Fonts.MEDIUM.getFont(7.0f);

        float h = Math.max(HEADER, this.height);
        Glyphs.background(context, this.x, this.y, this.width, h, 7.0f, this.animation.getValue());

        // header
        context.drawText(header, "Hotkeys", this.x + 9.0f, this.y + (HEADER - header.height()) / 2.0f - 0.5f, Colors.getTextColor());
        context.drawTexture(Rockstar.id(this.icon), this.x + this.width - 9.0f - 8.0f, this.y + (HEADER - 8.0f) / 2.0f, 8.0f, 8.0f, Colors.getAccentColor());
        context.drawRect(this.x + 8.0f, this.y + HEADER - 1.0f, this.width - 16.0f, 0.6f, Glyphs.divider(1.0f));

        ArrayList<Module> modules = new ArrayList<>(Rockstar.getInstance().getModuleManager().getModules());
        if (this.lastSize != modules.size()) {
            modules.sort(Comparator.comparingDouble(m -> font.width(m.getName())));
            this.lastSize = modules.size();
        }

        float offset = HEADER + 4.0f;
        for (Module module : modules) {
            Animation anim = module.getKeybindsAnimation();
            float a = anim.getValue();
            if (a == 0.0f) {
                continue;
            }
            float rowCy = this.y + offset + ROW / 2.0f;
            float alpha = 255.0f * a;

            context.drawText(font, module.getName(), this.x + 10.0f, rowCy - font.height() / 2.0f - 0.5f, Colors.getTextColor().withAlpha(alpha));

            // keycap
            String key = TextUtility.getKeyName(module.getKey());
            float capW = font.width(key) + 8.0f;
            float capH = 11.0f;
            float capX = this.x + this.width - 10.0f - capW;
            float capY = rowCy - capH / 2.0f;
            context.drawRoundedRect(capX, capY, capW, capH, BorderRadius.all(3.0f), Glyphs.surface(alpha));
            context.drawCenteredText(font, key, capX + capW / 2.0f, capY + (capH - font.height()) / 2.0f - 0.2f, Colors.getTextColor().withAlpha(alpha * 0.92f));

            offset += ROW * a;
        }
    }

    @Override
    public boolean show() {
        return !Rockstar.getInstance().getModuleManager().getModules().stream()
                .filter(module -> module.isEnabled() && module.getKey() != -1).toList().isEmpty()
                || this.alwaysDisplay.isEnabled()
                || KeyBinds.mc.currentScreen instanceof ChatScreen;
    }
}
