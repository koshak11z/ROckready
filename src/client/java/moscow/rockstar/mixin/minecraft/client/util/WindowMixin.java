/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.Icons
 *  net.minecraft.client.util.Window
 *  net.minecraft.resource.InputSupplier
 *  net.minecraft.resource.ResourcePack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.client.util;

import java.io.InputStream;
import java.util.List;
import moscow.rockstar.Rockstar;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Window.class})
public class WindowMixin {
    @Redirect(method={"setIcon"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/Icons;getIcons(Lnet/minecraft/resource/ResourcePack;)Ljava/util/List;"))
    public List<InputSupplier<InputStream>> setCustomIcon(Icons instance, ResourcePack resourcePack) {
        if (Rockstar.getInstance().isPanic()) {
            try {
                return instance.getIcons(resourcePack);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        InputStream icon16x = Rockstar.class.getResourceAsStream("/assets/%s/icons/window/icon16x16.png".formatted(Rockstar.MOD_ID));
        InputStream icon32x = Rockstar.class.getResourceAsStream("/assets/%s/icons/window/icon32x32.png".formatted(Rockstar.MOD_ID));
        return List.of(() -> icon16x, () -> icon32x);
    }
}

