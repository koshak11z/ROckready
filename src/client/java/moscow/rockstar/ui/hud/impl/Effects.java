package moscow.rockstar.ui.hud.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.mixins.StatusEffectInstanceAddition;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

/**
 * Potions panel redesigned to the Expensive reference: a black rounded panel with a "Potions"
 * header + edit glyph, then one row per effect = [colored icon] [name] ... [countdown ring] [timer].
 */
public class Effects extends HudList {
    private static final float HEADER = 20.0f;
    private static final float ROW = 18.0f;

    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    private final BooleanSetting alert = new BooleanSetting(this, "hud.effects.alert");

    private final Map<String, StatusEffectInstance> effects = new TreeMap<>();
    private final Map<StatusEffect, Boolean> ended = new HashMap<>();
    private final Map<String, Integer> maxDuration = new HashMap<>();

    public Effects() {
        super("hud.effects", "icons/hud/potion.png");
        this.showing = true;
    }

    private static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public void update(UIContext context) {
        this.width = 100.0f;
        this.height = HEADER;
        Font font = Fonts.MEDIUM.getFont(7.0f);
        Collection<StatusEffectInstance> original = Effects.mc.player.getStatusEffects();
        for (StatusEffectInstance eff : original) {
            StatusEffect potion = eff.getEffectType().value();
            String realName = potion.getName().getString();
            if (realName == null || ServerUtility.isCM()) {
                continue;
            }
            // remember the largest duration we have seen so the ring can show a fraction
            int dur = eff.getDuration();
            this.maxDuration.merge(realName, dur, Math::max);
            if (this.effects.containsKey(realName)) {
                this.effects.replace(realName, eff);
                Animation anim = ((StatusEffectInstanceAddition) eff).rockstar$getAnimPotion();
                if (anim.getValue() != 0.0f) {
                    continue;
                }
                anim.setValue(1.0f);
                continue;
            }
            this.effects.put(realName, eff);
        }
        if (!this.effects.isEmpty()) {
            this.height += 4.0f;
        }
        for (StatusEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition) eff).rockstar$getAnimPotion();
            StatusEffect potion = eff.getEffectType().value();
            if (this.alert.isEnabled()) {
                String effectName = potion.getName().getString() + " " + (eff.getAmplifier() > 0 ? Integer.valueOf(eff.getAmplifier() + 1) : "");
                if (!Effects.mc.player.hasStatusEffect(eff.getEffectType())) {
                    if (!this.ended.getOrDefault(potion, false) && !potion.getCategory().equals(StatusEffectCategory.HARMFUL)) {
                        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "Эффект " + effectName + " закончился", "Действие эффекта завершено");
                        this.ended.put(potion, true);
                    }
                } else {
                    this.ended.put(potion, false);
                }
            }
            anim.update(original.contains(eff));
            anim.setEasing(Easing.BAKEK);
            String name = potion.getName().getString() + (eff.getAmplifier() > 0 ? " " + (eff.getAmplifier() + 1) : "");
            String time = (eff.isInfinite() || eff.getDuration() >= 999999999) ? "∞" : formatTime(eff.getDuration() / 20);
            float rowWidth = 10.0f + 9.0f + 6.0f + font.width(name) + 14.0f + 9.0f + font.width(time) + 10.0f;
            this.width = Math.max(rowWidth, this.width);
            this.height += ROW * anim.getValue();
        }
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (Effects.mc.player == null || Effects.mc.world == null) {
            return;
        }
        Font header = Fonts.SEMIBOLD.getFont(7.5f);
        Font font = Fonts.MEDIUM.getFont(7.0f);
        Font timeFont = Fonts.MEDIUM.getFont(7.0f);

        float h = Math.max(HEADER, this.height);
        // panel background (full black)
        Glyphs.background(context, this.x, this.y, this.width, h, 7.0f, this.animation.getValue());

        // header: title + pencil glyph
        context.drawText(header, "Potions", this.x + 9.0f, this.y + (HEADER - header.height()) / 2.0f - 0.5f, Colors.getTextColor());
        Glyphs.pencil(context, this.x + this.width - 9.0f - 8.0f, this.y + (HEADER - 8.0f) / 2.0f, 8.0f, Colors.getAccentColor());
        context.drawRect(this.x + 8.0f, this.y + HEADER - 1.0f, this.width - 16.0f, 0.6f, Glyphs.divider(1.0f));

        StatusEffectInstance toRemove = null;
        float offset = HEADER + 4.0f;
        for (StatusEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition) eff).rockstar$getAnimPotion();
            float a = anim.getValue();
            if (a == 0.0f) {
                toRemove = eff;
                continue;
            }
            StatusEffect potion = eff.getEffectType().value();
            float rowY = this.y + offset;
            float rowCy = rowY + ROW / 2.0f;
            float alpha = 255.0f * a;

            // colored effect sprite
            Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(eff.getEffectType());
            context.drawTexture(sprite.getAtlasId(), this.x + 10.0f, rowCy - 4.5f, 9.0f, 9.0f,
                    sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(), ColorRGBA.WHITE.withAlpha(alpha));

            // name
            String name = potion.getName().getString() + (eff.getAmplifier() > 0 ? " " + (eff.getAmplifier() + 1) : "");
            context.drawText(font, name, this.x + 10.0f + 9.0f + 6.0f, rowCy - font.height() / 2.0f - 0.5f, Colors.getTextColor().withAlpha(alpha));

            // timer text (right aligned) + countdown ring just left of it
            boolean infinite = eff.isInfinite() || eff.getDuration() >= 999999999;
            String time = infinite ? "∞" : formatTime(eff.getDuration() / 20);
            float timeRight = this.x + this.width - 10.0f;
            context.drawRightText(timeFont, time, timeRight, rowCy - timeFont.height() / 2.0f - 0.5f, Colors.getTextColor().withAlpha(alpha));

            float ringR = 4.0f;
            float ringCx = timeRight - timeFont.width(time) - 6.0f - ringR;
            float progress = 1.0f;
            if (!infinite) {
                int max = Math.max(1, this.maxDuration.getOrDefault(potion.getName().getString(), eff.getDuration()));
                progress = Math.min(1.0f, eff.getDuration() / (float) max);
            }
            Glyphs.ring(context, ringCx, rowCy, ringR, 1.2f, progress,
                    Colors.getAccentColor().mulAlpha(0.22f * a), Colors.getAccentColor().withAlpha(alpha));

            offset += ROW * a;
        }
        if (toRemove != null) {
            this.effects.remove(toRemove.getEffectType().value().getName().getString(), toRemove);
        }
    }

    @Override
    public boolean show() {
        if (Effects.mc.player == null || Effects.mc.world == null) {
            return false;
        }
        return (!Effects.mc.player.getStatusEffects().isEmpty() || this.alwaysDisplay.isEnabled() || Effects.mc.currentScreen instanceof ChatScreen) && !ServerUtility.isCM();
    }
}
