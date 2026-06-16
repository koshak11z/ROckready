/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.DiffuseLighting
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.render.item.ItemRenderState
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ModelTransformationMode
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.crash.CrashException
 *  net.minecraft.util.crash.CrashReport
 *  net.minecraft.util.crash.CrashReportSection
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.world.World
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.framework.base;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.obj.CustomSprite;
import moscow.rockstar.utility.render.obj.Rect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CustomDrawContext
extends DrawContext
implements IMinecraft {
    private final DrawContext originalContext;

    protected CustomDrawContext(DrawContext originalContext) {
        super(MinecraftClient.getInstance(), ((DrawContextAccessor)originalContext).getVertexConsumers());
        this.originalContext = originalContext;
    }

    public static CustomDrawContext of(DrawContext originalContext) {
        return new CustomDrawContext(originalContext);
    }

    public void drawClientRect(float x, float y, float width, float height, float alpha, float dragAnim, float squircle) {
        if (Interface.showMinimalizm() && Interface.blurHudEnabled()) {
            this.drawBlurredRect(x, y, width, height, 45.0f, squircle, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            this.drawLiquidGlass(x, y, width, height, squircle, 0.08f - 0.07f * dragAnim, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.glass()));
        }
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        this.drawSquircle(x, y, width, height, squircle, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(dark ? 0.8f - 0.6f * Interface.glass() : 0.7f));
    }

    public void pushMatrix() {
        this.getMatrices().push();
    }

    public void popMatrix() {
        this.getMatrices().pop();
    }

    public void drawRect(float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawRect(this.getMatrices(), x, y, width, height, color);
    }

    public void drawLine(Vec2f from, Vec2f to, ColorRGBA color) {
        DrawUtility.drawLine(this.getMatrices(), from, to, color);
    }

    public void drawBezier(Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3, ColorRGBA color, int resolution) {
        DrawUtility.drawBezier(this.getMatrices(), p0, p1, p2, p3, color, resolution);
    }

    public void drawSquircle(float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawSquircle(this.getMatrices(), x, y, width, height, squirt, borderRadius, color);
    }

    public void drawRoundedRect(float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedRect(this.getMatrices(), x, y, width, height, borderRadius, color);
    }

    public void drawRoundedRect(float x, float y, float width, float height, BorderRadius borderRadius, Gradient gradient) {
        DrawUtility.drawRoundedRect(this.getMatrices(), x, y, width, height, borderRadius, gradient);
    }

    public void drawLiquidGlass(float x, float y, float width, float height, float squirt, float power, BorderRadius borderRadius, ColorRGBA color) {
        borderRadius = new BorderRadius(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        DrawUtility.drawLiquidGlass(this.getMatrices(), x - 5.0f * Interface.minimalizm(), y - 5.0f * Interface.minimalizm(), width + 10.0f * Interface.minimalizm(), height + 10.0f * Interface.minimalizm(), borderRadius, color, color.getAlpha() / 255.0f * Interface.glass(), (float)(height == 240.0f ? 100 : 50) * Interface.glass(), color.withAlpha(255.0f), 1.0f, true, 0.0f, power * Interface.glass(), squirt, false);
    }

    public void drawLiquidGlass(float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color, boolean clean) {
        borderRadius = new BorderRadius(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        DrawUtility.drawLiquidGlass(this.getMatrices(), x, y, width, height, borderRadius, color, color.getAlpha() / 255.0f, height == 240.0f ? 100.0f : 50.0f, color.withAlpha(255.0f), 1.0f, true, 0.0f, 0.08f, squirt, clean);
    }

    public void drawLoadingRect(float x, float y, float width, float height, float progress, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawLoadingRect(this.getMatrices(), x, y, width, height, progress, borderRadius, color);
    }

    public void drawRoundedBorder(float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor) {
        DrawUtility.drawRoundedBorder(this.getMatrices(), x, y, width, height, borderThickness, borderRadius, borderColor);
    }

    public void drawTexture(Identifier identifier, Rect rect) {
        this.drawTexture(identifier, rect, ColorRGBA.WHITE);
    }

    public void drawTexture(Identifier identifier, Rect rect, ColorRGBA color) {
        DrawUtility.drawTexture(this.getMatrices(), identifier, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height) {
        DrawUtility.drawTexture(this.getMatrices(), identifier, x, y, width, height, ColorRGBA.WHITE);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA color) {
        DrawUtility.drawTexture(this.getMatrices(), identifier, x, y, width, height, u1, u2, v1, v2, color);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        DrawUtility.drawTexture(this.getMatrices(), identifier, x, y, width, height, textureColor);
    }

    public void drawSprite(CustomSprite sprite, float x, float y, float width, float height, ColorRGBA textureColor) {
        DrawUtility.drawSprite(this.getMatrices(), sprite, x, y, width, height, textureColor);
    }

    public void drawRoundedTexture(Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius) {
        DrawUtility.drawRoundedTexture(this.getMatrices(), identifier, x, y, width, height, borderRadius);
    }

    public void drawRoundedTexture(Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTexture(this.getMatrices(), identifier, x, y, width, height, borderRadius, color);
    }

    public void drawShadow(float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawShadow(this.getMatrices(), x, y, width, height, softness, borderRadius, color);
    }

    public void drawBlurredRect(float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        float finalRadius = Math.max(0.0f, blurRadius * Interface.blur());
        DrawUtility.drawBlur(this.getMatrices(), x, y, width, height, finalRadius, borderRadius, color);
    }

    public void drawBlurredRect(float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        float finalRadius = Math.max(0.0f, blurRadius * Interface.blur());
        DrawUtility.drawBlur(this.getMatrices(), x, y, width, height, finalRadius, squirt, borderRadius, color);
    }

    public void drawText(Font font, String text, float x, float y, ColorRGBA color) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.getMatrices().peek().getPositionMatrix(), x, y, 0.0f);
    }

    public void drawText(Font font, Text text, float x, float y) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), this.getMatrices().peek().getPositionMatrix(), x, y, 0.0f);
    }

    public void drawFadeoutText(Font font, String text, float x, float y, ColorRGBA color, float fadeoutStart, float fadeoutEnd) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.getMatrices().peek().getPositionMatrix(), x, y, 0.0f, true, fadeoutStart, fadeoutEnd);
    }

    public void drawFadeoutText(Font font, String text, float x, float y, ColorRGBA color, float fadeoutStart, float fadeoutEnd, float maxWidth) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.getMatrices().peek().getPositionMatrix(), x, y, 0.0f, true, fadeoutStart, fadeoutEnd, maxWidth);
    }

    public void drawCenteredText(Font font, String text, float x, float y, ColorRGBA color) {
        this.drawText(font, text, x - font.getFont().getWidth(text, font.getSize()) / 2.0f, y, color);
    }

    public void drawRightText(Font font, String text, float x, float y, ColorRGBA color) {
        this.drawText(font, text, x - font.getFont().getWidth(text, font.getSize()), y, color);
    }

    public void drawItem(Item item, float x, float y, float size) {
        this.drawItem(item.getDefaultStack(), x, y, size);
    }

    public void drawItem(ItemStack item, float x, float y, float size) {
        this.getMatrices().push();
        this.getMatrices().translate(x, y, 0.0f);
        this.getMatrices().scale(size, size, size);
        DiffuseLighting.disableGuiDepthLighting();
        this.drawItem(item, 0, 0);
        DiffuseLighting.disableGuiDepthLighting();
        this.getMatrices().pop();
    }

    public void drawHead(AbstractClientPlayerEntity player, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawPlayerHeadWithHat(this.getMatrices(), player, x, y, size, borderRadius, color);
    }

    public void drawHead(LivingEntity entity, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawEntityHeadWithHat(this.getMatrices(), entity, x, y, size, borderRadius, color);
    }

    public void drawBatchItem(ItemStack item, int x, int y) {
        this.drawBatchItem((LivingEntity)CustomDrawContext.mc.player, (World)CustomDrawContext.mc.world, item, x, y, 0);
    }

    private void drawBatchItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
        this.drawBatchItem(entity, world, stack, x, y, seed, 0);
    }

    private void drawBatchItem(@Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z) {
        MatrixStack matrices = this.getMatrices();
        ItemRenderState itemRenderState = ((DrawContextAccessor)this.originalContext).getItemRenderState();
        VertexConsumerProvider.Immediate vertexConsumers = ((DrawContextAccessor)this.originalContext).getVertexConsumers();
        if (!stack.isEmpty()) {
            mc.getItemModelManager().update(itemRenderState, stack, ModelTransformationMode.GUI, false, world, entity, seed);
            matrices.push();
            matrices.translate((float)(x + 8), (float)(y + 8), (float)(150 + (itemRenderState.hasDepth() ? z : 0)));
            try {
                boolean bl;
                matrices.scale(16.0f, -16.0f, 16.0f);
                boolean bl2 = bl = !itemRenderState.isSideLit();
                if (bl) {
                    DiffuseLighting.disableGuiDepthLighting();
                }
                itemRenderState.render(matrices, (VertexConsumerProvider)vertexConsumers, 0xF000F0, OverlayTexture.DEFAULT_UV);
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting();
                }
            }
            catch (Throwable var11) {
                Throwable throwable = var11;
                CrashReport crashReport = CrashReport.create((Throwable)throwable, (String)"Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
                crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                throw new CrashException(crashReport);
            }
            matrices.pop();
        }
    }
}

