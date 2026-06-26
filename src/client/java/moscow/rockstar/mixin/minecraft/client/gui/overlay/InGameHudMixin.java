/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.scoreboard.ScoreboardObjective
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.PostHudRenderEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.ui.hud.impl.VanillaHudElement;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.DrawUtility;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={InGameHud.class})
public class InGameHudMixin
implements IMinecraft {
    @Inject(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScoreboardSidebarHook(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        Removals removals;
        if (objective.getDisplayName().getString().contains("\u0410\u043d\u0430\u0440\u0445\u0438\u044f") && (ServerUtility.isFT() || ServerUtility.isST())) {
            try {
                ServerUtility.ftAn = Integer.parseInt(objective.getDisplayName().getString().split("-")[1].trim());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class)).isEnabled() && removals.getScoreboard().isSelected()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderPortalOverlayHook(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPortal().isSelected()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method={"renderMiscOverlays"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal=0))
    private void onRenderPumpkinOverlay(Args args) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPumpkin().isSelected()) {
            args.set(2, (Object)Float.valueOf(0.0f));
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    public void triggerPreHudRenderEvent(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(context);
        Rockstar.getInstance().getEventManager().triggerEvent(new PreHudRenderEvent(customDrawContext, tickCounter.getTickDelta(false)));
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    public void triggerPostHudRenderEvent(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(context);
        Rockstar.getInstance().getEventManager().triggerEvent(new PostHudRenderEvent(customDrawContext, tickCounter.getTickDelta(false)));
    }

    // Defensive: clear any scissor/shader-color leaked by the custom HUD (previous frame) or by HUD
    // event listeners BEFORE vanilla draws health/food/xp. A leaked scissor clips the whole screen,
    // which is what made the vanilla bars "disappear at random".
    @Inject(method={"renderMainHud"}, at={@At(value="HEAD")})
    private void rockstar$resetHudState(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ScissorUtility.clear();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Inject(method={"renderMainHud"}, at={@At(value="TAIL")})
    private void triggerHudRenderEvent(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CustomDrawContext customDrawContext = CustomDrawContext.of(context);
        DrawUtility.blurProgram.draw();
        Rockstar.getInstance().getEventManager().triggerEvent(new HudRenderEvent(customDrawContext, tickCounter.getTickDelta(false)));
    }

    // ─── movable vanilla HUD: translate each element by its drag offset ───
    @Unique
    private void rockstar$wrap(DrawContext context, VanillaHudElement.Type type, Runnable original) {
        float dx = VanillaHudElement.offsetX(type);
        float dy = VanillaHudElement.offsetY(type);
        boolean translated = dx != 0.0f || dy != 0.0f;
        if (translated) {
            context.getMatrices().push();
            context.getMatrices().translate(dx, dy, 0.0f);
        }
        try {
            original.run();
        } catch (Throwable ignored) {
            // CRITICAL: never let a transient vanilla render error (e.g. during a dimension/anka
            // switch or a rapid PvP update) propagate out of renderMainHud. If it did, the rest of
            // renderMainHud — including the health/food/xp status bars — would be skipped, which is
            // exactly the "hearts disappear when switching ankas / in PvP" bug.
        } finally {
            if (translated) {
                context.getMatrices().pop();
            }
        }
    }

    @WrapMethod(method="renderHotbar")
    private void rockstar$moveHotbar(DrawContext context, RenderTickCounter tickCounter, Operation<Void> original) {
        this.rockstar$wrap(context, VanillaHudElement.Type.HOTBAR, () -> original.call(context, tickCounter));
    }

    // NOTE: health / food / experience are intentionally NOT wrapped — wrapping those vanilla
    // status-bar renders made them vanish at random on this toolchain. They now render purely vanilla.

    @WrapMethod(method="renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V")
    private void rockstar$moveScoreboard(DrawContext context, ScoreboardObjective objective, Operation<Void> original) {
        this.rockstar$wrap(context, VanillaHudElement.Type.SCOREBOARD, () -> original.call(context, objective));
    }
}

