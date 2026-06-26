package moscow.rockstar.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.Sprite;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Target HUD redesigned to the Expensive reference: a black rounded panel with the target head,
 * a row of the target's status-effect icons, the name, a diamond + HP value, and a rounded HP bar.
 * Keeps the existing target acquisition, animations, name-copy and (optional) armor row.
 */
public class TargetHud extends HudElement {
    private final BooleanSetting rayTrace = new BooleanSetting(this, "hud.targethud.look");
    private final BooleanSetting effects = new BooleanSetting(this, "hud.targethud.effects").enable();
    private final ModeSetting armor = new ModeSetting(this, "hud.targethud.armor");
    private final ModeSetting.Value armorNone = new ModeSetting.Value(this.armor, "hud.targethud.armor.none").select();
    private final ModeSetting.Value armorNumber = new ModeSetting.Value(this.armor, "hud.targethud.armor.number");
    private final ModeSetting.Value armorIcon = new ModeSetting.Value(this.armor, "hud.targethud.armor.icon");
    private final Animation content = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation health = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation golden = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation number = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation itemsX = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation copy = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation success = new Animation(500L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation eatingPulse = new Animation(150L, 0.0f, Easing.BAKEK);
    private final Animation pulseIntensity = new Animation(50L, 0.0f, Easing.SINE_IN_OUT);
    private final Animation[] items = new Animation[4];
    private LivingEntity target;
    private final Timer copyTimer = new Timer();
    private boolean copied;

    private static final float WIDTH = 116.0f;
    private static final float HEIGHT = 36.0f;
    private static final float LEFT = 33.0f; // right column start

    public TargetHud() {
        super("hud.targethud", "icons/hud/target.png");
        this.showing = true;
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = new Animation(300L, 0.0f, Easing.BAKEK);
        }
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        this.width = WIDTH;
        this.height = HEIGHT;
    }

    @Override
    protected void renderComponent(UIContext context) {
        LivingEntity target = this.getTarget();
        // While editing the HUD (chat open) preview the panel on yourself so it can be positioned.
        if (target == null && TargetHud.mc.currentScreen instanceof ChatScreen) {
            target = TargetHud.mc.player;
        }
        if (target != null) {
            this.target = target;
        }
        if (this.target == null) {
            return;
        }
        Font nameFont = Fonts.SEMIBOLD.getFont(7.5f);
        Font hpFont = Fonts.SEMIBOLD.getFont(7.0f);
        ColorRGBA chip = Glyphs.surface(235.0f);

        boolean hover = GuiUtility.isHovered(this.x + LEFT, this.y + 16.0f, 60.0, 8.0, context);
        if (!hover || this.copyTimer.finished(1000L)) {
            this.copied = false;
        }
        boolean isEating = this.target.isUsingItem() && this.target.getActiveItem().contains(DataComponentTypes.FOOD);
        this.eatingPulse.update(isEating);
        if (isEating) {
            float pulse = (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.5f + 0.5f;
            this.pulseIntensity.setValue(pulse);
        }
        this.copy.update(hover);
        this.success.update(this.copied);
        this.content.update(this.animation.getValue() * this.visible.getValue() >= 1.0f);

        float hp = this.target instanceof PlayerEntity player ? EntityUtility.getHealth(player) : this.target.getHealth();
        this.health.update(hp / this.target.getMaxHealth());
        this.golden.update(this.target.getAbsorptionAmount() / 20.0f);
        this.number.update(hp);
        if (this.animation.getValue() == 0.0f) {
            return;
        }

        // ───── optional armor / hand item row (slides out below the panel) ─────
        if (!this.armorNone.isSelected()) {
            this.renderArmorRow(context, chip);
        }

        // ───── main panel ─────
        context.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f, BorderRadius.all(7.0f), ColorRGBA.BLACK.withAlpha(63.75f * this.dragAnim.getValue()));
        Glyphs.background(context, this.x, this.y, this.width, this.height, 7.0f, this.animation.getValue());

        float alpha = 255.0f * this.content.getValue();
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);

        // head
        float headSize = 22.0f;
        float headX = this.x + 6.0f * this.content.getValue();
        float headY = this.y + (this.height - headSize) / 2.0f;
        if (this.target instanceof AbstractClientPlayerEntity player) {
            context.drawHead(player, headX, headY, headSize, BorderRadius.all(4.0f), Colors.WHITE.withAlpha(alpha));
        } else {
            context.drawRoundedTexture(Rockstar.id(Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK ? "icons/hud/whodark.png" : "icons/hud/who.png"),
                    headX, headY, headSize, headSize, BorderRadius.all(4.0f), Colors.WHITE.withAlpha(alpha));
        }

        // ── buff cells near the name: timer ABOVE each icon + level (III/IV) on the icon ──
        if (this.effects.isEnabled()) {
            Font tf = Fonts.MEDIUM.getFont(5.5f);
            Font lf = Fonts.SEMIBOLD.getFont(5.0f);
            float ix = this.x + LEFT;
            float right = this.x + this.width - 8.0f;
            for (StatusEffectInstance e : this.target.getStatusEffects()) {
                String time = (e.isInfinite() || e.getDuration() >= 999999999) ? "∞" : formatTime(e.getDuration() / 20);
                String lvl = roman(e.getAmplifier()).trim();
                float cellW = Math.max(10.0f, tf.width(time));
                if (ix + cellW > right) {
                    break;
                }
                float cx = ix + cellW / 2.0f;
                Sprite s = mc.getStatusEffectSpriteManager().getSprite(e.getEffectType());
                context.drawCenteredText(tf, time, cx, this.y + 1.0f, TIMER_COLOR.withAlpha(alpha));
                context.drawTexture(s.getAtlasId(), cx - 4.0f, this.y + 5.0f, 8.0f, 8.0f, s.getMinU(), s.getMaxU(), s.getMinV(), s.getMaxV(), ColorRGBA.WHITE.withAlpha(alpha));
                if (!lvl.isEmpty()) {
                    context.drawRightText(lf, lvl, cx + 5.0f, this.y + 9.0f, ColorRGBA.WHITE.withAlpha(alpha));
                }
                ix += cellW + 3.0f;
            }
        }

        // hp value (diamond + number) right-aligned on the name row
        String numberText = hp == 1000.0f ? "?" : TextUtility.formatNumber(this.number.getValue()).replace(",", ".");
        float numRight = this.x + this.width - 8.0f;
        float numWidth = hpFont.width(numberText);
        context.drawRightText(hpFont, numberText, numRight, this.y + 18.0f, Colors.getAccentColor().withAlpha(alpha));
        Glyphs.diamond(context, numRight - numWidth - 9.0f, this.y + 17.5f, 6.5f, Colors.getAccentColor().withAlpha(alpha));

        // name
        context.drawFadeoutText(nameFont, this.target.getName().getString(), this.x + LEFT + 8.0f * this.copy.getValue(), this.y + 18.0f,
                Colors.getTextColor().withAlpha(alpha), 0.7f, 1.0f, this.width - LEFT - 16.0f - numWidth - 9.0f - 8.0f * this.copy.getValue());

        // copy / check icon
        RenderUtility.rotate(context.getMatrices(), this.x + LEFT - 2.0f + 5.0f * this.copy.getValue(), this.y + 21.0f, 90.0f * this.success.getValue());
        context.drawTexture(Rockstar.id("icons/hud/copy.png"), this.x + LEFT - 5.0f + 5.0f * this.copy.getValue(), this.y + 18.0f, 6.0f, 6.0f, Colors.getTextColor().withAlpha(alpha * this.copy.getValue() * (1.0f - this.success.getValue())));
        RenderUtility.end(context.getMatrices());
        RenderUtility.rotate(context.getMatrices(), this.x + LEFT - 2.0f + 5.0f * this.copy.getValue(), this.y + 21.0f, -90.0f + 90.0f * this.success.getValue());
        context.drawTexture(Rockstar.id("icons/check.png"), this.x + LEFT - 5.0f + 5.0f * this.copy.getValue(), this.y + 18.0f, 6.0f, 6.0f, Colors.GREEN.withAlpha(alpha * this.copy.getValue() * this.success.getValue()));
        RenderUtility.end(context.getMatrices());

        // hp bar
        float barX = this.x + LEFT;
        float barW = this.width - LEFT - 8.0f;
        float barY = this.y + this.height - 8.0f;
        context.drawRoundedRect(barX, barY, barW, 3.0f, BorderRadius.all(1.5f), Glyphs.surface(alpha));
        context.drawRoundedRect(barX, barY, barW * Math.clamp(this.health.getValue(), 0.0f, 1.0f), 3.0f, BorderRadius.all(1.5f), Colors.getAccentColor().withAlpha(alpha));
        float gold = Math.clamp(this.golden.getValue(), 0.0f, 1.0f);
        context.drawRoundedRect(barX + barW - barW * gold, barY, barW * gold, 3.0f, BorderRadius.all(1.5f), new ColorRGBA(255.0f, 220.0f, 81.0f, alpha));
        ScissorUtility.pop();
    }

    private static final ColorRGBA TIMER_COLOR = new ColorRGBA(150.0f, 226.0f, 140.0f);

    private static String roman(int amplifier) {
        if (amplifier <= 0) {
            return "";
        }
        int level = amplifier + 1;
        String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return " " + (level < r.length ? r[level] : String.valueOf(level));
    }

    private static String formatTime(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private void renderArmorRow(UIContext context, ColorRGBA bgColor) {
        Font semibold6 = Fonts.SEMIBOLD.getFont(6.0f);
        float prev = RenderSystem.getShaderColor()[3];
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.drawItem(Items.DIAMOND_CHESTPLATE, -992.0f, 994.0f, 1.0f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, prev);
        float animOff = 0.0f;
        int i = 0;
        ItemStack[] handItems = new ItemStack[]{this.target.getMainHandStack(), this.target.getOffHandStack()};
        for (ItemStack itemStack : this.target.getArmorItems()) {
            if (itemStack.isEmpty()) continue;
            animOff += (this.armorIcon.isSelected() ? 11.0f : 5.0f + semibold6.width(this.calculateDurabilityPercent(itemStack))) + 2.0f;
        }
        float itemSize = 11.0f;
        boolean isEating = this.target.isUsingItem() && this.target.getActiveItem().contains(DataComponentTypes.FOOD);
        if (this.armorIcon.isSelected()) {
            for (ItemStack handItem : handItems) {
                if (handItem.isEmpty()) continue;
                animOff += 13.0f;
            }
        }
        this.itemsX.update(animOff - 2.0f);
        float xOffset = -this.itemsX.getValue() / 2.0f;
        for (ItemStack itemStack : this.target.getArmorItems()) {
            this.items[i].update(!itemStack.isEmpty());
            float anim = this.content.getValue() * this.items[i].getValue();
            String percent = this.calculateDurabilityPercent(itemStack);
            boolean isIcon = this.armorIcon.isSelected();
            float panelWidth = isIcon ? 11.0f : 5.0f + semibold6.width(percent);
            float panelHeight = isIcon ? 11.0f : 9.0f;
            float panelX = this.x + this.width / 2.0f + xOffset;
            float panelY = this.y + this.height - 2.0f + 6.0f * anim;
            context.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, BorderRadius.all(2.0f), bgColor.withAlpha(bgColor.getAlpha() * anim));
            ScissorUtility.push(context.getMatrices(), panelX, panelY, panelWidth, panelHeight);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, prev * anim * 0.5f);
            if (this.armorNumber.isSelected()) {
                context.drawItem(itemStack, panelX - 11.0f + panelWidth / 2.0f + 2.0f, panelY - 4.0f, 1.0f);
            } else {
                context.drawItem(itemStack, panelX - 11.0f + panelWidth / 2.0f + 5.5f, panelY, 0.7f);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, prev);
            ScissorUtility.pop();
            if (this.armorNumber.isSelected()) {
                context.drawText(semibold6, percent, panelX + 3.0f, panelY + 2.5f, Colors.getTextColor().withAlpha(255.0f * anim));
            }
            xOffset += (panelWidth + 2.0f) * anim;
            ++i;
        }
        float handWidth = Arrays.stream(handItems).mapToInt(item -> item.isEmpty() ? 0 : (int) (itemSize + 2.0f)).sum() - 2;
        float handX = this.armorNumber.isSelected() ? -handWidth / 2.0f : xOffset;
        for (ItemStack handItem : handItems) {
            if (handItem.isEmpty()) continue;
            float handItemX = this.x + this.width / 2.0f + handX;
            float handItemY = this.y + this.height - 2.0f + 6.0f * this.content.getValue() + (this.armorNumber.isSelected() ? 12 : 0);
            float alpha = this.content.getValue() * (isEating && this.target.getActiveItem() == handItem ? 0.5f + 0.7f * this.pulseIntensity.getValue() : 1.0f);
            context.drawRoundedRect(handItemX, handItemY, itemSize, itemSize, BorderRadius.all(2.0f), bgColor.withAlpha(bgColor.getAlpha() * alpha));
            ScissorUtility.push(context.getMatrices(), handItemX, handItemY, itemSize, itemSize);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, prev * alpha);
            context.drawItem(handItem, handItemX - 11.0f + itemSize / 2.0f + 5.5f, handItemY, 0.7f);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, prev);
            ScissorUtility.pop();
            handX += itemSize + 2.0f;
            if (!this.armorIcon.isSelected()) continue;
            xOffset += (itemSize + 2.0f) * this.content.getValue();
        }
    }

    private String calculateDurabilityPercent(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamageable()) {
            return "100%";
        }
        int maxDurability = itemStack.getMaxDamage();
        int currentDamage = itemStack.getDamage();
        if (currentDamage >= maxDurability) {
            return "0%";
        }
        double durabilityPercent = 100.0 - (double) currentDamage / (double) maxDurability * 100.0;
        return String.format("%.0f%%", durabilityPercent);
    }

    private LivingEntity getTarget() {
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (target1 instanceof LivingEntity living) {
            return living;
        }
        if (this.rayTrace.isEnabled() && TargetHud.mc.targetedEntity instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (GuiUtility.isHovered(this.x + LEFT, this.y + 16.0f, 60.0, 8.0, mouseX, mouseY) && this.target != null) {
            TextUtility.copyText(this.target.getName().getString());
            this.copyTimer.reset();
            this.copied = true;
            return;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean show() {
        return this.getTarget() != null || TargetHud.mc.currentScreen instanceof ChatScreen;
    }
}
