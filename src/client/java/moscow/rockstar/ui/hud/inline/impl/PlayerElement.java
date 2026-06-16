/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.inline.impl;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.inline.InlineElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;

public class PlayerElement
extends InlineElement {
    private final InlineValue fps;
    private final InlineValue speed;
    private final BooleanSetting ySpeed;
    private final Animation animation;

    public PlayerElement() {
        super("hud.player", "icons/hud/player.png");
        this.fps = new InlineValue(this.elements, "FPS", "FPS");
        this.speed = new InlineValue(this.elements, "speed", "BPS");
        this.ySpeed = new BooleanSetting(this, "hud.player.speedY").enable();
        this.animation = new Animation(300L, 0.0f, Easing.SMOOTH_STEP);
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        double motion = !this.ySpeed.isEnabled() ? Math.hypot(PlayerElement.mc.player.getX() - PlayerElement.mc.player.prevX, PlayerElement.mc.player.getZ() - PlayerElement.mc.player.prevZ) : Math.hypot(PlayerElement.mc.player.getY() - PlayerElement.mc.player.prevY, Math.hypot(PlayerElement.mc.player.getX() - PlayerElement.mc.player.prevX, PlayerElement.mc.player.getZ() - PlayerElement.mc.player.prevZ));
        this.speed.update(String.format("%.2f", motion * 20.0).replace(",", "."));
        this.fps.update("" + Math.round(this.animation.update(mc.getCurrentFps())));
    }
}

