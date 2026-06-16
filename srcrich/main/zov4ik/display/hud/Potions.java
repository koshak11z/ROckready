package im.zov4ik.display.hud;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Potions extends AbstractDraggable {
    private final List<Potion> list = new ArrayList<>();
    private long lastEffectChange = 0L;
    private RegistryEntry<StatusEffect> currentRandomEffect = StatusEffects.SPEED;
    private float animatedWidth = 96.0F;

    private static final RegistryEntry<StatusEffect>[] NEGATIVE_EFFECTS = new RegistryEntry[] {
            StatusEffects.POISON, StatusEffects.WITHER, StatusEffects.NAUSEA, StatusEffects.BLINDNESS,
            StatusEffects.HUNGER, StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE, StatusEffects.INSTANT_DAMAGE,
            StatusEffects.WEAKNESS, StatusEffects.LEVITATION, StatusEffects.UNLUCK, StatusEffects.BAD_OMEN
    };

    private static final float PAD = 8.5F;
    private static final float HEADER_H = 16.0F;
    private static final float ROW_H = 12.0F;
    private static final float MIN_W = 92.0F;
    private static final float ICON_SLOT = 7.0F;
    private static final float HEADER_ICON = 5.8F;
    private static final float EFFECT_ICON = 6.0F;
    private static final float TIMER_SIZE = 6.4F;

    public Potions() {
        super("Potions", 200, 40, 96, 34, true);
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected(getName())
                && Hud.getInstance().state
                && (!list.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        list.removeIf(p -> p.anim.isFinished(Direction.BACKWARDS));

        if (!PlayerInteractionHelper.nullCheck()) {
            list.forEach(p -> p.effect.update(mc.player, null));
        }

        if (list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEffectChange >= 1000L) {
                List<RegistryEntry<StatusEffect>> effects = new ArrayList<>();
                for (Identifier id : Registries.STATUS_EFFECT.getIds()) {
                    Registries.STATUS_EFFECT.getEntry(id).ifPresent(effects::add);
                }
                if (!effects.isEmpty()) {
                    currentRandomEffect = effects.get(new Random().nextInt(effects.size()));
                    lastEffectChange = currentTime;
                }
            }
        }
    }

    @Override
    public void packet(PacketEvent e) {
        switch (e.getPacket()) {
            case EntityStatusEffectS2CPacket effect -> {
                if (!PlayerInteractionHelper.nullCheck() && effect.getEntityId() == Objects.requireNonNull(mc.player).getId()) {
                    RegistryEntry<StatusEffect> effectId = effect.getEffectId();
                    list.stream()
                            .filter(p -> p.effect.getEffectType().getIdAsString().equals(effectId.getIdAsString()))
                            .forEach(p -> p.anim.setDirection(Direction.BACKWARDS));

                    list.add(new Potion(
                            new StatusEffectInstance(effectId, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()),
                            new Decelerate().setMs(150).setValue(1.0F)
                    ));
                }
            }
            case RemoveEntityStatusEffectS2CPacket effect -> list.stream()
                    .filter(p -> p.effect.getEffectType().getIdAsString().equals(effect.effect().getIdAsString()))
                    .forEach(p -> p.anim.setDirection(Direction.BACKWARDS));
            case PlayerRespawnS2CPacket p -> list.clear();
            case GameJoinS2CPacket p -> list.clear();
            default -> {
            }
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer titleFont = Fonts.getSize(15, Fonts.Type.BOLD);
        FontRenderer rowFont = Fonts.getSize(13, Fonts.Type.REGULAR);
        FontRenderer timeFont = Fonts.getSize(13, Fonts.Type.SEMI);

        boolean drawExample = list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen);
        List<Row> rows = collectRows(drawExample);

        float targetWidth = Math.max(MIN_W, PAD * 2.0F + titleFont.getStringWidth(getName()) + ICON_SLOT + 11.0F);
        for (Row row : rows) {
            targetWidth = Math.max(targetWidth, PAD * 2.0F + EFFECT_ICON + 4.5F
                    + rowFont.getStringWidth(row.name) + timeFont.getStringWidth(row.duration) + TIMER_SIZE + 15.0F);
        }

        animatedWidth += (targetWidth - animatedWidth) * 0.20F;
        if (Math.abs(targetWidth - animatedWidth) < 0.35F) {
            animatedWidth = targetWidth;
        }

        setWidth((int) Math.ceil(animatedWidth));
        setHeight((int) Math.ceil(HEADER_H + rows.size() * ROW_H + 2.5F));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 4.2F);
        titleFont.drawString(matrix, getName(), getX() + PAD, getY() + 4.25F, HudTheme.TEXT);
        HudTheme.iconSlot(context, HudTheme.ICON_FLASK, getX() + getWidth() - PAD - ICON_SLOT, getY() + 4.45F, ICON_SLOT, HEADER_ICON, HudTheme.ACCENT);
        HudTheme.hairline(matrix, getX() + PAD, getY() + HEADER_H, getWidth() - PAD * 2.0F);

        float rowY = getY() + HEADER_H + 1.9F;
        float centerX = getX() + getWidth() / 2.0F;
        for (Row row : rows) {
            float currentY = rowY;
            Calculate.scale(matrix, centerX, currentY + ROW_H / 2.0F, 1.0F, row.animation, () ->
                    drawRow(matrix, rowFont, timeFont, row, currentY)
            );
            rowY += ROW_H * row.animation;
        }
    }

    private List<Row> collectRows(boolean drawExample) {
        if (drawExample) {
            return List.of(new Row(currentRandomEffect, "Effect", "**:**", false, 255, 1.0F, 1.0F));
        }

        List<Row> rows = new ArrayList<>();
        for (Potion potion : list) {
            float animation = potion.anim.getOutput().floatValue();
            if (animation <= 0.0F) {
                continue;
            }
            StatusEffectInstance effect = potion.effect;
            String name = effect.getEffectType().value().getName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }
            rows.add(new Row(effect.getEffectType(), name, getDuration(effect), isBadEffect(effect.getEffectType()),
                    getEffectAlpha(effect), animation, getTimerProgress(effect)));
        }
        return rows;
    }

    private void drawRow(MatrixStack matrix, FontRenderer rowFont, FontRenderer timeFont, Row row, float rowY) {
        int nameColor = row.badEffect
                ? ColorAssist.rgba(255, 86, 92, row.alpha)
                : ColorAssist.multAlpha(HudTheme.TEXT_DIM, row.alpha / 255.0F);
        int iconColor = row.badEffect ? nameColor : ColorAssist.multAlpha(HudTheme.ACCENT, row.alpha / 255.0F);

        float iconX = getX() + PAD;
        Render2D.drawSprite(matrix, mc.getStatusEffectSpriteManager().getSprite(row.effectType), iconX, rowY + 3.0F,
                (int) EFFECT_ICON, (int) EFFECT_ICON, iconColor);
        rowFont.drawString(matrix, row.name, iconX + EFFECT_ICON + 4.0F, rowY + 3.75F, nameColor);

        float durationW = timeFont.getStringWidth(row.duration);
        float durationX = getX() + getWidth() - PAD - durationW;
        float timerX = durationX - TIMER_SIZE - 4.5F;
        float timerY = rowY + 2.85F;

        arc.render(ShapeProperties.create(matrix, timerX, timerY, TIMER_SIZE, TIMER_SIZE)
                .round(0.40F)
                .thickness(0.22F)
                .end(360.0F)
                .color(ColorAssist.multAlpha(HudTheme.TEXT_MUTED, 0.32F))
                .build());
        arc.render(ShapeProperties.create(matrix, timerX, timerY, TIMER_SIZE, TIMER_SIZE)
                .round(0.40F)
                .thickness(0.24F)
                .end(360.0F * row.timerProgress)
                .color(iconColor)
                .build());
        timeFont.drawString(matrix, row.duration, durationX, rowY + 3.85F, ColorAssist.multAlpha(HudTheme.TEXT_DIM, row.alpha / 255.0F));
    }

    private int getEffectAlpha(StatusEffectInstance effect) {
        if (effect.getDuration() <= 200 && effect.getDuration() > 0) {
            double output = 0.5 + 0.5 * Math.cos(2 * Math.PI * (System.currentTimeMillis() % 700) / 700.0);
            return (int) (100 + (155 * output));
        }
        return effect.getDuration() == 0 ? 0 : 255;
    }

    private float getTimerProgress(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return 1.0F;
        }
        return Math.max(0.08F, Math.min(1.0F, effect.getDuration() / 1200.0F));
    }

    private String getDuration(StatusEffectInstance pe) {
        int duration = pe.getDuration();
        int mins = duration / 1200;
        return pe.isInfinite() || mins > 60 ? "**:**" : mins + ":" + String.format("%02d", (duration % 1200) / 20);
    }

    private boolean isBadEffect(RegistryEntry<StatusEffect> effect) {
        for (RegistryEntry<StatusEffect> negativeEffect : NEGATIVE_EFFECTS) {
            if (effect == negativeEffect) {
                return true;
            }
        }
        return false;
    }

    private record Potion(StatusEffectInstance effect, Animation anim) {
    }

    private record Row(RegistryEntry<StatusEffect> effectType, String name, String duration,
                       boolean badEffect, int alpha, float animation, float timerProgress) {
    }
}
