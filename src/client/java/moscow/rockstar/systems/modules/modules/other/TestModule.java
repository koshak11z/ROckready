/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.mob.MobEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 */
package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.event.impl.window.ChatTypeEvent;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@ModuleInfo(name="Test", category=ModuleCategory.OTHER)
public class TestModule
extends BaseModule {
    private final RangeSetting testBoolean = new RangeSetting(this, "Name").min(1.0f).max(10.0f).step(1.0f).firstValue(3.0f).secondValue(6.0f);
    private Popup popup = new Popup(100.0f, 100.0f);
    private AnimatedNumber time;
    private final EventListener<ChatTypeEvent> onChat = event -> {};
    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (TestModule.mc.player == null) {
            return;
        }
    };
    private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
        UIContext context = UIContext.of(event.getContext(), TestModule.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(), TestModule.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
    };

    @Override
    public void tick() {
        if (TestModule.mc.player == null) {
            return;
        }
        Entity entity = TestModule.mc.targetedEntity;
        boolean isAnimal = entity instanceof AnimalEntity;
        boolean isMob = entity instanceof MobEntity;
        MessageUtility.info(Text.of((String)("Is Animal: " + isAnimal + " | IsMob: " + isMob)));
        super.tick();
    }

    @Override
    public void onEnable() {
        this.popup = new Popup(100.0f, 100.0f, 90.0f).text("Sosalin1337").separator().checkbox("\u0414\u0440\u0443\u0433", false).checkbox("\u0412\u0440\u0430\u0433", true).checkbox("\u0413\u043b\u0430\u0432\u043d\u044b\u0439 \u0432\u0440\u0430\u0433 \u0432\u0441\u0435\u0445 \u043d\u0430\u0440\u043e\u0434\u043e\u0432", false).separator().checkbox("Glabos", true).checkbox("Sosia", true).checkbox("x, x, x", false);
        for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            System.out.println(String.format("modules.descriptions.%s=%s", module.getName().toLowerCase().replace(" ", "_"), module.getDescription()));
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private Vec3d getRenderPos() {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)TestModule.mc.player.prevX, (double)TestModule.mc.player.getX()), MathHelper.lerp((double)tickDelta, (double)(TestModule.mc.player.prevY + (double)TestModule.mc.player.getEyeHeight(TestModule.mc.player.getPose())), (double)(TestModule.mc.player.getY() + (double)TestModule.mc.player.getEyeHeight(TestModule.mc.player.getPose()))), MathHelper.lerp((double)tickDelta, (double)TestModule.mc.player.prevZ, (double)TestModule.mc.player.getZ()));
    }

    private void renderTexture(MatrixStack matrices, Identifier identifier, ColorRGBA color, float size) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, 0.0f, -size, 0.0f).texture(0.0f, 0.0f).color(color.getRGB());
        builder.vertex(matrix, -size, -size, 0.0f).texture(0.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, -size, 0.0f, 0.0f).texture(1.0f, 1.0f).color(color.getRGB());
        builder.vertex(matrix, 0.0f, 0.0f, 0.0f).texture(1.0f, 0.0f).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
    }
}

