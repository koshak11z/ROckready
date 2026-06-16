/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.texture.Sprite
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectCategory
 *  net.minecraft.entity.effect.StatusEffectInstance
 */
package moscow.rockstar.ui.hud.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.mixins.StatusEffectInstanceAddition;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public class Effects
extends HudList {
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    int lastSize = -1;
    private final Map<String, StatusEffectInstance> effects = new TreeMap<String, StatusEffectInstance>();
    private final Map<StatusEffect, Boolean> ended = new HashMap<StatusEffect, Boolean>();
    private final BooleanSetting alert = new BooleanSetting(this, "hud.effects.alert");

    public Effects() {
        super("hud.effects", "icons/hud/potion.png");
    }

    @Override
    public void update(UIContext context) {
        this.width = 92.0f;
        this.height = 18.0f;
        Collection<StatusEffectInstance> original = Effects.mc.player.getStatusEffects();
        for (StatusEffectInstance eff : original) {
            StatusEffect potion = (StatusEffect)eff.getEffectType().value();
            String realName = potion.getName().getString();
            if (realName == null || ServerUtility.isCM()) continue;
            if (this.effects.containsKey(realName)) {
                this.effects.replace(realName, eff);
                Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
                if (anim.getValue() != 0.0f) continue;
                anim.setValue(1.0f);
                continue;
            }
            this.effects.put(realName, eff);
        }
        if (!this.effects.isEmpty()) {
            this.height += 5.0f;
        }
        for (StatusEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
            StatusEffect potion = (StatusEffect)eff.getEffectType().value();
            if (this.alert.isEnabled()) {
                String effectName = potion.getName().getString() + " " + String.valueOf(eff.getAmplifier() > 0 ? Integer.valueOf(eff.getAmplifier() + 1) : "");
                if (!Effects.mc.player.hasStatusEffect(eff.getEffectType())) {
                    if (!this.ended.getOrDefault(potion, false).booleanValue() && !potion.getCategory().equals((Object)StatusEffectCategory.HARMFUL)) {
                        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "\u042d\u0444\u0444\u0435\u043a\u0442 " + effectName + " \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0441\u044f", "\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e");
                        this.ended.put(potion, true);
                    }
                } else {
                    this.ended.put(potion, false);
                }
            }
            anim.update(original.contains(eff));
            anim.setEasing(Easing.BAKEK);
            this.width = Math.max(Fonts.REGULAR.getFont(7.0f).width(potion.getName().getString()) + 60.0f, this.width);
            this.height += 18.0f * anim.getValue();
        }
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (Effects.mc.player == null || Effects.mc.world == null) {
            return;
        }
        Font font = Fonts.REGULAR.getFont(7.0f);
        float offset = 22.0f;
        super.renderComponent(context);
        StatusEffectInstance toRemove = null;
        RectBatching split = new RectBatching(VertexFormats.POSITION_COLOR, context.getMatrices());
        for (StatusEffectInstance statusEffectInstance : this.effects.values()) {
            Animation animation = ((StatusEffectInstanceAddition)statusEffectInstance).rockstar$getAnimPotion();
            if (animation.getValue() == 0.0f) {
                toRemove = statusEffectInstance;
                continue;
            }
            float off = -4.5f + 4.5f * animation.getValue();
            if (offset != 22.0f) {
                context.drawRect(this.x, this.y + offset + off, this.width, 0.5f, Colors.getTextColor().withAlpha(5.1f * animation.getValue()));
            }
            offset += 18.0f * animation.getValue();
        }
        ((Batching)split).draw();
        offset = 22.0f;
        IconBatching texture = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
        for (StatusEffectInstance statusEffectInstance : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)statusEffectInstance).rockstar$getAnimPotion();
            if (anim.getValue() == 0.0f) continue;
            float off = -4.5f + 4.5f * anim.getValue();
            Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(statusEffectInstance.getEffectType());
            context.drawTexture(sprite.getAtlasId(), this.x + 7.0f * anim.getValue(), this.y + offset + off + GuiUtility.getMiddleOfBox(8.0f, 18.0f) + 1.0f, 8.0f, 8.0f, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), ColorRGBA.WHITE.withAlpha(255.0f * anim.getValue()));
            offset += 18.0f * anim.getValue();
        }
        ((Batching)texture).draw();
        FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, font.getFont());
        offset = 22.0f;
        for (StatusEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
            AnimatedNumber timeAnimation = ((StatusEffectInstanceAddition)eff).rockstar$getTimeAnimation();
            StatusEffect potion = (StatusEffect)eff.getEffectType().value();
            if (anim.getValue() == 0.0f) continue;
            float off = -4.5f + 4.5f * anim.getValue();
            String effectName = potion.getName().getString() + " " + String.valueOf(eff.getAmplifier() > 0 ? Integer.valueOf(eff.getAmplifier() + 1) : "");
            if (eff.isInfinite() || eff.getDuration() >= 999999999) {
                String duration = "**:**";
                float timeX = this.x + this.width - 7.0f * anim.getValue();
                float timeY = this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f);
                context.drawRightText(font, duration, timeX, timeY, Colors.getTextColor().withAlpha((int)(255.0f * anim.getValue())));
            } else {
                int totalSeconds = eff.getDuration() / 20;
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                String minutesAndSeparator = String.format("%02d:", minutes);
                float timeX = this.x + this.width - 7.0f * anim.getValue();
                float timeY = this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f);
                float minutesWidth = font.width(minutesAndSeparator);
                float totalWidth = font.width(timeStr);
                context.drawText(font, minutesAndSeparator, timeX - totalWidth, timeY, Colors.getTextColor().withAlpha(255.0f * anim.getValue()));
                timeAnimation.settings(true, Colors.getTextColor().withAlpha(255.0f * anim.getValue()));
                timeAnimation.update(seconds);
                timeAnimation.pos(timeX - totalWidth + minutesWidth, timeY);
                timeAnimation.render(context);
            }
            context.drawText(font, effectName, this.x + 13.0f + 7.0f * anim.getValue(), this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f), Colors.getTextColor().withAlpha(255.0f * anim.getValue()));
            offset += 18.0f * anim.getValue();
        }
        ((Batching)fontBatching).draw();
        if (toRemove != null) {
            StatusEffect statusEffect = (StatusEffect)toRemove.getEffectType().value();
            this.effects.remove(statusEffect.getName().getString(), toRemove);
        }
    }

    @Override
    public boolean show() {
        if (Effects.mc.player == null || Effects.mc.world == null) {
            return false;
        }
        return (!Effects.mc.player.getStatusEffects().isEmpty() || this.alwaysDisplay.isEnabled()) && !ServerUtility.isCM();
    }
}
