package im.zov4ik.mixins.client.screen.ingame;

import im.zov4ik.commands.defaults.FakeFpsCommand;
import im.zov4ik.features.impl.misc.SelfDestruct;
import im.zov4ik.utils.features.aura.warp.TurnsConnection;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    private static final Pattern FPS_PATTERN = Pattern.compile("\\b\\d+\\s+fps\\b");

    @Redirect(
            method = "getLeftText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getYaw()F"
            )
    )
    private float redirectYaw(Entity entity) {
        if (SelfDestruct.unhooked) return entity.getYaw();
        return TurnsConnection.INSTANCE.getRotation().getYaw();
    }

    @Redirect(
            method = "getLeftText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;getPitch()F"
            )
    )
    private float redirectPitch(Entity entity) {
        if (SelfDestruct.unhooked) return entity.getPitch();
        return TurnsConnection.INSTANCE.getRotation().getPitch();
    }

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void overrideFpsLine(CallbackInfoReturnable<List<String>> cir) {
        if (SelfDestruct.unhooked || !FakeFpsCommand.isEnabled()) {
            return;
        }

        List<String> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) {
            return;
        }

        List<String> updated = new ArrayList<>(original);
        int fakeFps = FakeFpsCommand.getFakeFps();
        for (int i = 0; i < updated.size(); i++) {
            String line = updated.get(i);
            Matcher matcher = FPS_PATTERN.matcher(line);
            if (matcher.find()) {
                updated.set(i, matcher.replaceFirst(fakeFps + " fps"));
                cir.setReturnValue(updated);
                return;
            }
        }
    }
}
