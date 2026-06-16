/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.hud.impl.island;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.IslandSize;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;

public abstract class IslandStatus
extends SelectSetting.Value
implements IScaledResolution {
    protected IslandSize size = new IslandSize(48.0f, 15.0f);
    protected final Animation animation = new Animation(500L, Easing.BAKEK_SIZE);

    public IslandStatus(SelectSetting parent, String name) {
        super(parent, "hud.dynamic_island.statuses." + name);
        this.select();
    }

    public void draw(CustomDrawContext context) {
    }

    public void drawWithAlpha(CustomDrawContext context) {
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.animation.getValue());
        this.draw(context);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void click(float mouseX, float mouseY, int button) {
    }

    public abstract boolean canShow();

    public ColorRGBA getColor() {
        return Colors.getBackgroundColor();
    }

    @Generated
    public IslandSize getSize() {
        return this.size;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }
}

