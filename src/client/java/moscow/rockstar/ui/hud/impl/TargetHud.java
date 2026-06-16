/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class TargetHud
extends HudElement {
    private final BooleanSetting rayTrace = new BooleanSetting(this, "hud.targethud.look");
    private final ModeSetting armor = new ModeSetting(this, "hud.targethud.armor");
    private final ModeSetting.Value armorNone = new ModeSetting.Value(this.armor, "hud.targethud.armor.none");
    private final ModeSetting.Value armorNumber = new ModeSetting.Value(this.armor, "hud.targethud.armor.number").select();
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

    public TargetHud() {
        super("hud.targethud", "icons/hud/target.png");
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = new Animation(300L, 0.0f, Easing.BAKEK);
        }
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        this.width = 103.0f;
        this.height = 31.0f;
    }

    @Override
    protected void renderComponent(UIContext context) {
        float f;
        float f2;
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.target = target;
        }
        if (this.target == null) {
            return;
        }
        Font regular7 = Fonts.REGULAR.getFont(7.0f);
        Font semibold6 = Fonts.SEMIBOLD.getFont(6.0f);
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA bgColor = Colors.getBackgroundColor().mulAlpha(dark ? 0.8f - 0.6f * Interface.glass() : 0.7f);
        boolean hover = GuiUtility.isHovered(this.x + 30.0f, this.y + 3.0f + 6.0f * this.content.getValue(), 60.0, 6.0, context);
        if (!hover || this.copyTimer.finished(1000L)) {
            this.copied = false;
        }
        boolean isEating = this.target.isUsingItem() && this.target.getActiveItem().contains(DataComponentTypes.FOOD);
        this.eatingPulse.update(isEating);
        if (isEating) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 100.0) * 0.5f + 0.5f;
            this.pulseIntensity.setValue(pulse);
        }
        this.copy.update(hover);
        this.success.update(this.copied);
        this.content.update(this.animation.getValue() * this.visible.getValue() >= 1.0f);
        LivingEntity livingEntity = this.target;
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)livingEntity;
            f2 = EntityUtility.getHealth(player);
        } else {
            f2 = this.target.getHealth();
        }
        this.health.update(f2 / this.target.getMaxHealth());
        this.golden.update(this.target.getAbsorptionAmount() / 20.0f);
        LivingEntity livingEntity2 = this.target;
        if (livingEntity2 instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)livingEntity2;
            f = EntityUtility.getHealth(player);
        } else {
            f = this.target.getHealth();
        }
        float healthNum = f;
        this.number.update(healthNum);
        if (this.animation.getValue() == 0.0f) {
            return;
        }
        if (!this.armorNone.isSelected()) {
            float prev = RenderSystem.getShaderColor()[3];
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            context.drawItem(Items.DIAMOND_CHESTPLATE, -992.0f, 994.0f, 1.0f);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)prev);
            float animOff = 0.0f;
            int i = 0;
            ItemStack[] handItems = new ItemStack[]{this.target.getMainHandStack(), this.target.getOffHandStack()};
            for (ItemStack itemStack : this.target.getArmorItems()) {
                if (itemStack.isEmpty()) continue;
                animOff += (this.armorIcon.isSelected() ? 11.0f : 5.0f + semibold6.width(this.calculateDurabilityPercent(itemStack))) + 2.0f;
            }
            float itemSize = 11.0f;
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
                float panelY = this.y + this.height - 4.0f + 6.0f * anim;
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(prev * anim));
                if (Interface.blurHudEnabled()) {
                    context.drawBlurredRect(panelX, panelY, panelWidth, panelHeight, 5.0f, BorderRadius.all(1.5f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
                }
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)prev);
                context.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, BorderRadius.all(1.5f), bgColor.withAlpha(bgColor.getAlpha() * anim));
                ScissorUtility.push(context.getMatrices(), panelX, panelY, panelWidth, panelHeight);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(prev * anim * 0.5f));
                if (this.armorNumber.isSelected()) {
                    context.drawItem(itemStack, panelX - 11.0f + panelWidth / 2.0f + 2.0f, panelY - 4.0f, 1.0f);
                } else {
                    context.drawItem(itemStack, panelX - 11.0f + panelWidth / 2.0f + 5.5f, panelY, 0.7f);
                }
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)prev);
                ScissorUtility.pop();
                if (this.armorNumber.isSelected()) {
                    context.drawText(semibold6, percent, panelX + 3.0f, panelY + 2.5f, Colors.getTextColor().withAlpha(255.0f * anim));
                }
                xOffset += (panelWidth + 2.0f) * anim;
                ++i;
            }
            float handWidth = Arrays.stream(handItems).mapToInt(item -> item.isEmpty() ? 0 : (int)(itemSize + 2.0f)).sum() - 2;
            float handX = this.armorNumber.isSelected() ? -handWidth / 2.0f : xOffset;
            for (ItemStack handItem : handItems) {
                if (handItem.isEmpty()) continue;
                float handItemX = this.x + this.width / 2.0f + handX;
                float handItemY = this.y + this.height - 4.0f + 6.0f * this.content.getValue() + (float)(this.armorNumber.isSelected() ? 12 : 0);
                float alpha = this.content.getValue() * (isEating && this.target.getActiveItem() == handItem ? 0.5f + 0.7f * this.pulseIntensity.getValue() : 1.0f);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(prev * alpha));
                if (Interface.blurHudEnabled()) {
                    context.drawBlurredRect(handItemX, handItemY, itemSize, itemSize, 5.0f, BorderRadius.all(1.5f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
                }
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)prev);
                context.drawRoundedRect(handItemX, handItemY, itemSize, itemSize, BorderRadius.all(1.5f), bgColor.withAlpha(bgColor.getAlpha() * alpha));
                ScissorUtility.push(context.getMatrices(), handItemX, handItemY, itemSize, itemSize);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(prev * alpha));
                context.drawItem(handItem, handItemX - 11.0f + itemSize / 2.0f + 5.5f, handItemY, 0.7f);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)prev);
                ScissorUtility.pop();
                handX += itemSize + 2.0f;
                if (!this.armorIcon.isSelected()) continue;
                xOffset += (itemSize + 2.0f) * this.content.getValue();
            }
        }
        context.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(63.75f * this.dragAnim.getValue()));
        if (Interface.showMinimalizm() && Interface.blurHudEnabled()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.08f - 0.07f * this.dragAnim.getValue(), BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue() * Interface.glass()));
        }
        context.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, BorderRadius.all(6.0f), bgColor);
        float alpha = 255.0f * this.content.getValue();
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
        LivingEntity livingEntity3 = this.target;
        if (livingEntity3 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)livingEntity3;
            context.drawHead(player, this.x + 6.0f * this.content.getValue(), this.y + 6.0f, 19.0f, BorderRadius.all(3.0f), Colors.WHITE.withAlpha(alpha));
        } else {
            context.drawRoundedTexture(Rockstar.id(Interface.glassSelected() ? "icons/hud/whoglass.png" : (Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK ? "icons/hud/whodark.png" : "icons/hud/who.png")), this.x + 6.0f * this.content.getValue(), this.y + 6.0f, 19.0f, 19.0f, BorderRadius.all(3.0f), Colors.WHITE.withAlpha(alpha));
        }
        String numberText = healthNum == 1000.0f ? "?" : TextUtility.formatNumber(this.number.getValue()).replace(",", ".");
        context.drawFadeoutText(regular7, this.target.getName().getString(), this.x + 30.0f + 8.0f * this.copy.getValue(), this.y + 3.0f + 6.0f * this.content.getValue(), Colors.getTextColor().withAlpha(alpha), 0.7f, 1.0f, this.width - 40.0f - 8.0f * this.copy.getValue() - semibold6.width(numberText));
        RenderUtility.rotate(context.getMatrices(), this.x + 28.0f + 5.0f * this.copy.getValue(), this.y + 6.0f + 6.0f * this.content.getValue(), 90.0f * this.success.getValue());
        context.drawTexture(Rockstar.id("icons/hud/copy.png"), this.x + 25.0f + 5.0f * this.copy.getValue(), this.y + 3.0f + 6.0f * this.content.getValue(), 6.0f, 6.0f, Colors.getTextColor().withAlpha(alpha * this.copy.getValue() * (1.0f - this.success.getValue())));
        RenderUtility.end(context.getMatrices());
        RenderUtility.rotate(context.getMatrices(), this.x + 28.0f + 5.0f * this.copy.getValue(), this.y + 6.0f + 6.0f * this.content.getValue(), -90.0f + 90.0f * this.success.getValue());
        context.drawTexture(Rockstar.id("icons/check.png"), this.x + 25.0f + 5.0f * this.copy.getValue(), this.y + 3.0f + 6.0f * this.content.getValue(), 6.0f, 6.0f, Colors.GREEN.withAlpha(alpha * this.copy.getValue() * this.success.getValue()));
        RenderUtility.end(context.getMatrices());
        context.drawRightText(semibold6, numberText, this.x + this.width - 7.0f, this.y + 4.0f + 6.0f * this.content.getValue(), Colors.ACCENT.withAlpha(alpha));
        context.drawRoundedRect(this.x + 30.0f, this.y + this.height - 6.0f - 6.0f * this.content.getValue(), 65.0f, 3.0f, BorderRadius.all(0.7f), Colors.getAdditionalColor().withAlpha(alpha * (1.0f - 0.7f * Interface.glass())));
        context.drawRoundedRect(this.x + 30.0f, this.y + this.height - 6.0f - 6.0f * this.content.getValue(), 65.0f * Math.clamp(this.health.getValue(), 0.0f, 1.0f), 3.0f, BorderRadius.all(0.7f), Colors.ACCENT.withAlpha(alpha));
        context.drawRoundedRect(this.x + 95.0f - 65.0f * Math.clamp(this.golden.getValue(), 0.0f, 1.0f), this.y + this.height - 6.0f - 6.0f * this.content.getValue(), 65.0f * Math.clamp(this.golden.getValue(), 0.0f, 1.0f), 3.0f, BorderRadius.all(0.7f), new ColorRGBA(255.0f, 220.0f, 81.0f, alpha));
        ScissorUtility.pop();
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
        double durabilityPercent = 100.0 - (double)currentDamage / (double)maxDurability * 100.0;
        return String.format("%.0f%%", durabilityPercent);
    }

    private LivingEntity getTarget() {
        Entity entity;
        LivingEntity target2;
        LivingEntity mainTarget;
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        LivingEntity livingEntity = mainTarget = target1 instanceof LivingEntity ? (target2 = (LivingEntity)target1) : null;
        if (mainTarget != null) {
            return mainTarget;
        }
        if (this.rayTrace.isEnabled() && (entity = TargetHud.mc.targetedEntity) instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            return livingEntity2;
        }
        return null;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (GuiUtility.isHovered((double)(this.x + 30.0f), (double)(this.y + 3.0f + 6.0f * this.content.getValue()), 60.0, 6.0, mouseX, mouseY)) {
            TextUtility.copyText(TargetHud.mc.player.getName().getString());
            this.copyTimer.reset();
            this.copied = true;
            return;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean show() {
        return this.getTarget() != null;
    }
}
