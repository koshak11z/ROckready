/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.BossBarHud
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.ui.hud.impl.VanillaHudElement;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.impl.PVPStatus;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BossBarHud.class})
public class BossBarHudMixin
implements IMinecraft {
    @Shadow
    @Final
    private Map<UUID, ClientBossBar> field_2060;
    @Unique
    private static final Pattern PVP_TIME_PATTERN = Pattern.compile("(\\d+)\\s*[\u0441c][\u0435e][\u043ak](?=$|\\s|\\p{Punct})", 66);
    private static final String FILTERED_TEXT = "\ub445\ua223\ua203\ub444\ua223\ua205";
    private final Map<UUID, String> lastProcessedNames = new HashMap<UUID, String>();

    @WrapMethod(method="render")
    private void rockstar$moveBossBar(DrawContext context, Operation<Void> original) {
        float dx = VanillaHudElement.offsetX(VanillaHudElement.Type.BOSSBAR);
        float dy = VanillaHudElement.offsetY(VanillaHudElement.Type.BOSSBAR);
        if (dx == 0.0f && dy == 0.0f) {
            original.call(context);
            return;
        }
        context.getMatrices().push();
        context.getMatrices().translate(dx, dy, 0.0f);
        original.call(context);
        context.getMatrices().pop();
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void onRenderHead(DrawContext context, CallbackInfo ci) {
        int ctTimer = 0;
        for (ClientBossBar bossBar : this.field_2060.values()) {
            String name;
            if (bossBar.getName() == null || !(name = bossBar.getName().getString().toLowerCase()).contains("\u0431\u043e\u0439") && !name.contains("pvp")) continue;
            Matcher matcher = PVP_TIME_PATTERN.matcher(bossBar.getName().getString());
            if (!matcher.find()) break;
            ctTimer = Integer.parseInt(matcher.group(1));
            break;
        }
        ServerUtility.setHasCT(ctTimer > 0);
        ServerUtility.setCtTime(ctTimer);
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBossBar().isSelected()) {
            return;
        }
        if (!(!Rockstar.getInstance().getHud().getIsland().isShowing() || this.field_2060.isEmpty() || removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.isCM())) {
            boolean islandShowingPvp;
            DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
            boolean bl = islandShowingPvp = island.isShowing() && island.statuses().stream().anyMatch(status -> status instanceof PVPStatus);
            if (removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.hasCT && islandShowingPvp) {
                return;
            }
            context.getMatrices().push();
            context.getMatrices().translate(0.0f, Rockstar.getInstance().getHud().getIsland().getSize().height + 7.0f, 0.0f);
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(CallbackInfo ci) {
        boolean islandShowingPvp;
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
        boolean bl = islandShowingPvp = island.isShowing() && island.statuses().stream().anyMatch(status -> status instanceof PVPStatus);
        if (removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.hasCT && islandShowingPvp) {
            ci.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void onRenderReturn(DrawContext context, CallbackInfo ci) {
        int j = 19 * this.field_2060.size();
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBossBar().isSelected()) {
            return;
        }
        if (!(!Rockstar.getInstance().getHud().getIsland().isShowing() || this.field_2060.isEmpty() || removals.isEnabled() && removals.getBossBar().isSelected() || ServerUtility.isCM())) {
            context.getMatrices().pop();
        }
    }
}

