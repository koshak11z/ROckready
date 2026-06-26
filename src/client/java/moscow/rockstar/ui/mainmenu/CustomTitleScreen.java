package moscow.rockstar.ui.mainmenu;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.mainmenu.alt.AltManagerScreen;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

/**
 * Redesigned main menu: a left sidebar card (brand + vertical nav) over a dark gradient with a faint
 * Z watermark. Nav: Singleplayer / Multiplayer / Alts / Options / Quit. Theme-synced via {@link Colors}.
 */
public class CustomTitleScreen extends CustomScreen implements IMinecraft {
    private static boolean welcomed;

    private final List<Nav> navs = new ArrayList<>();

    private static final class Nav {
        final String labelKey;
        final String icon;        // texture path, or null to use the person glyph
        final Runnable action;
        final Animation hover = new Animation(250L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
        float x, y, w, h;

        Nav(String labelKey, String icon, Runnable action) {
            this.labelKey = labelKey;
            this.icon = icon;
            this.action = action;
        }
    }

    @Override
    protected void init() {
        if (!welcomed) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
                ClientSounds.WELCOME.play(Rockstar.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue());
            }
            welcomed = true;
        }
        this.navs.clear();
        String base = "image/mainmenu/icons/";
        this.navs.add(new Nav("mainmenu.singleplayer", base + "single.png", () -> mc.setScreen(new SelectWorldScreen(this))));
        this.navs.add(new Nav("mainmenu.multiplayer", base + "multi.png", () -> mc.setScreen(new MultiplayerScreen(this))));
        this.navs.add(new Nav("mainmenu.alts", null, () -> mc.setScreen(new AltManagerScreen(this))));
        this.navs.add(new Nav("mainmenu.options", base + "settings.png", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        this.navs.add(new Nav("mainmenu.quit", base + "quit.png", () -> ((MinecraftClient) mc).stop()));
        super.init();
    }

    @Override
    public void render(UIContext context) {
        float mx = context.getMouseX();
        float my = context.getMouseY();
        ColorRGBA accent = Colors.getAccentColor();

        // background
        context.drawRoundedRect(0.0f, 0.0f, this.width, this.height, BorderRadius.ZERO,
                new VerticalGradient(new ColorRGBA(24.0f, 26.0f, 38.0f), new ColorRGBA(6.0f, 5.0f, 12.0f)));
        // faint brand watermark on the right
        Glyphs.zLogo(context, this.width * 0.66f, this.height / 2.0f - 110.0f, 220.0f, accent.mulAlpha(0.06f));

        // sidebar card
        float px = 26.0f;
        float py = 26.0f;
        float pw = 214.0f;
        float ph = this.height - 52.0f;
        Glyphs.background(context, px, py, pw, ph, 12.0f, 1.0f);
        context.drawRoundedBorder(px, py, pw, ph, 1.0f, BorderRadius.all(12.0f), accent.mulAlpha(0.22f));

        // brand
        Glyphs.zLogo(context, px + 18.0f, py + 19.0f, 20.0f, accent);
        context.drawText(Fonts.SEMIBOLD.getFont(14.0f), "RockReady", px + 46.0f, py + 17.0f, Colors.getTextColor());
        context.drawText(Fonts.MEDIUM.getFont(7.0f), "v2.0 • cracked", px + 46.0f, py + 33.0f, Colors.getTextColor().mulAlpha(0.45f));
        context.drawRect(px + 16.0f, py + 52.0f, pw - 32.0f, 0.6f, Glyphs.divider(1.0f));

        // nav rows
        Font label = Fonts.MEDIUM.getFont(9.0f);
        float rowH = 36.0f;
        float ny = py + 66.0f;
        for (Nav nav : this.navs) {
            nav.x = px + 12.0f;
            nav.y = ny;
            nav.w = pw - 24.0f;
            nav.h = rowH - 4.0f;
            boolean hovered = GuiUtility.isHovered((double) nav.x, (double) nav.y, (double) nav.w, (double) nav.h, (double) mx, (double) my);
            nav.hover.update(hovered ? 1.0f : 0.0f);
            float hv = nav.hover.getValue();

            context.drawRoundedRect(nav.x, nav.y, nav.w, nav.h, BorderRadius.all(7.0f), accent.mulAlpha(0.12f * hv));
            // animated accent bar
            float barH = 6.0f + 12.0f * hv;
            context.drawRoundedRect(nav.x + 2.0f, nav.y + (nav.h - barH) / 2.0f, 2.5f, barH, BorderRadius.all(1.25f), accent.mulAlpha(0.25f + 0.75f * hv));
            // icon
            ColorRGBA iconColor = Colors.getTextColor().mulAlpha(0.7f + 0.3f * hv);
            if (nav.icon != null) {
                context.drawTexture(Rockstar.id(nav.icon), nav.x + 14.0f, nav.y + (nav.h - 14.0f) / 2.0f, 14.0f, 14.0f, iconColor);
            } else {
                Glyphs.person(context, nav.x + 14.0f, nav.y + (nav.h - 13.0f) / 2.0f, 13.0f, iconColor);
            }
            context.drawText(label, Localizator.translate(nav.labelKey), nav.x + 38.0f, nav.y + (nav.h - label.height()) / 2.0f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * hv)));
            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }
            ny += rowH;
        }

        // footer: account + clock
        float footY = py + ph - 40.0f;
        context.drawRect(px + 16.0f, footY, pw - 32.0f, 0.6f, Glyphs.divider(1.0f));
        Glyphs.person(context, px + 16.0f, footY + 12.0f, 11.0f, accent);
        context.drawText(Fonts.MEDIUM.getFont(8.0f), mc.getSession().getUsername(), px + 32.0f, footY + 12.0f, Colors.getTextColor());
        context.drawRightText(Fonts.MEDIUM.getFont(7.5f), TextUtility.getCurrentTime(), px + pw - 16.0f, footY + 13.0f, Colors.getTextColor().mulAlpha(0.55f));

        if (this.shouldShowIsland()) {
            Rockstar.getInstance().getHud().getIsland().render(context);
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.shouldShowIsland() && Rockstar.getInstance().getHud().getIsland().handleClick((float) mouseX, (float) mouseY, button.getButtonIndex())) {
            return;
        }
        if (button == MouseButton.LEFT) {
            for (Nav nav : this.navs) {
                if (GuiUtility.isHovered((double) nav.x, (double) nav.y, (double) nav.w, (double) nav.h, mouseX, mouseY)) {
                    nav.action.run();
                    return;
                }
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) {
            Rockstar.getInstance().getThemeManager().switchTheme();
        }
        if (Screen.hasControlDown() && keyCode == 82) {
            mc.setScreen(new MultiplayerScreen(this));
        }
        if (Screen.hasControlDown() && keyCode == 84) {
            mc.setScreen(new SelectWorldScreen(this));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean shouldShowIsland() {
        return Rockstar.getInstance().getMusicTracker().haveActiveSession();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
