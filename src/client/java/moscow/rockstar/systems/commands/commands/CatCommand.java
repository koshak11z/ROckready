/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.systems.commands.commands;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.WebUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class CatCommand
implements IMinecraft {
    private final Animation animation = new Animation(1000L, Easing.CUBIC_IN_OUT);
    private boolean removing = false;
    private Identifier catTexture = null;
    private boolean loading = false;
    private final EventListener<PreHudRenderEvent> onPreHudRender = event -> {
        if (this.catTexture == null) {
            return;
        }
        if ((double)this.animation.getValue() == 1.0 && !this.removing) {
            this.removing = true;
        }
        this.animation.update(this.removing ? 0.0f : 1.0f);
        if (this.animation.getValue() == 0.0f && this.removing) {
            return;
        }
        float textureScale = 200.0f;
        float textureX = ((float)mc.getWindow().getScaledWidth() - textureScale) / 2.0f;
        float textureY = ((float)mc.getWindow().getScaledHeight() - textureScale) / 2.0f;
        event.getContext().drawTexture(this.catTexture, textureX, textureY, textureScale, textureScale, Colors.WHITE.withAlpha(255.0f * this.animation.getValue()));
    };

    public CatCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("cat", b -> b.aliases("kitty").desc("commands.cat.description").handler(ctx -> this.loadRandomCatImage())).build();
    }

    private void loadRandomCatImage() {
        if (this.loading) {
            return;
        }
        this.loading = true;
        CompletableFuture.supplyAsync(() -> {
            try {
                String json = WebUtility.fetchJson("https://api.thecatapi.com/v1/images/search");
                String imageUrl = WebUtility.extractImageUrl(json);
                if (imageUrl == null) {
                    return null;
                }
                BufferedImage bufferedImage = ImageIO.read(new URL(imageUrl));
                if (bufferedImage == null) {
                    return null;
                }
                return WebUtility.bufferedImageToNativeImage(bufferedImage, false);
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(nativeImage -> mc.execute(() -> {
            if (nativeImage != null) {
                if (this.catTexture != null) {
                    mc.getTextureManager().destroyTexture(this.catTexture);
                }
                Identifier id = Rockstar.id("temp/cat/" + String.valueOf(UUID.randomUUID()));
                mc.getTextureManager().registerTexture(id, (AbstractTexture)new NativeImageBackedTexture(nativeImage));
                this.catTexture = id;
                this.animation.update(1.0f);
                this.removing = false;
            }
            this.loading = false;
        }));
    }
}

