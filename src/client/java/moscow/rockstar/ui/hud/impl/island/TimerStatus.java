/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.impl.island;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;

public class TimerStatus
extends IslandStatus
implements IMinecraft {
    private String prefix = "";
    private String suffix = "";
    private int time = -1;
    private String text = "text";
    private ColorRGBA color;
    protected AnimatedNumber timeAnim;

    public TimerStatus(SelectSetting setting, String name) {
        super(setting, name);
    }

    @Override
    public void draw(CustomDrawContext context) {
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        float x = sr.getScaledWidth() / 2.0f - island.getSize().width / 2.0f;
        float y = 7.0f;
        Font timeFont = Fonts.MEDIUM.getFont(6.0f);
        if (this.timeAnim == null) {
            this.timeAnim = new AnimatedNumber(Fonts.MEDIUM.getFont(6.0f), 5.0f, 500L, Easing.BAKEK);
        }
        float timeWidth = this.timeAnim.getWidth() + timeFont.width(this.prefix + this.suffix);
        float width = this.size.width = 17.0f + Fonts.MEDIUM.getFont(7.0f).width(this.text) + timeWidth;
        this.size.height = 15.0f;
        float height = 15.0f;
        context.drawRoundedRect(x - 16.0f + 20.0f * this.animation.getValue(), y + 3.5f, 5.5f + timeWidth, 8.0f, BorderRadius.all(3.0f), this.color.withAlpha(255.0f * this.animation.getValue()));
        context.drawText(Fonts.MEDIUM.getFont(6.0f), this.prefix, x - 13.0f + 20.0f * this.animation.getValue(), y + 5.5f, ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
        this.timeAnim.update(this.time);
        this.timeAnim.pos(x - 13.0f + 20.0f * this.animation.getValue() + timeFont.width(this.prefix), y + 5.5f);
        this.timeAnim.settings(true, Colors.WHITE);
        this.timeAnim.render(UIContext.of(context, -1, -1, mc.getRenderTickCounter().getTickDelta(false)));
        context.drawText(Fonts.MEDIUM.getFont(6.0f), this.suffix, x - 13.0f + 20.0f * this.animation.getValue() + timeFont.width(this.prefix) + this.timeAnim.getWidth(), y + 5.5f, ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
        context.drawText(Fonts.MEDIUM.getFont(7.0f), this.text, x + 23.0f - 10.0f * this.animation.getValue() + timeWidth, y + 5.5f, Colors.getTextColor().withAlpha(255.0f * this.animation.getValue()));
    }

    public void update(String suffix, int time, String text, ColorRGBA color) {
        this.update("", suffix, time, text, color);
    }

    public void update(String prefix, String suffix, int time, String text, ColorRGBA color) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.time = time;
        this.text = text;
        this.color = color;
    }

    @Override
    public boolean canShow() {
        return ServerUtility.hasCT;
    }
}

