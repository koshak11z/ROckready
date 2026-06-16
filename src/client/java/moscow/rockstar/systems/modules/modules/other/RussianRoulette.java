/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.zxing.BarcodeFormat
 *  com.google.zxing.MultiFormatWriter
 *  com.google.zxing.client.j2se.MatrixToImageWriter
 *  com.google.zxing.common.BitMatrix
 *  lombok.Generated
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.systems.modules.modules.other;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@ModuleInfo(name="Russian Roulette", category=ModuleCategory.OTHER, desc="\u0420\u0443\u0441\u0441\u043a\u0430\u044f \u0440\u0443\u043b\u0435\u0442\u043a\u0430, \u043a\u0430\u043a \u043f\u043e\u0432\u0435\u0437\u0435\u0442 :)")
public class RussianRoulette
extends BaseModule {
    private final ModeSetting mode = new ModeSetting((SettingsContainer)this, "\u0421\u043b\u043e\u0436\u043d\u043e\u0441\u0442\u044c", "\u041b\u0435\u0433\u043a\u0430\u044f - \u0432\u044b\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u0447\u0438\u0442, \u0441\u0440\u0435\u0434\u043d\u044f\u044f - \u0432\u0440\u0443\u0431\u0430\u0435\u0442 \u0441\u043f\u044f\u0449\u0438\u0439 \u0440\u0435\u0436\u0438\u043c \u043d\u0430 \u043f\u043a, \u043e\u0447\u0435\u043d\u044c \u0441\u043b\u043e\u0436\u043d\u0430\u044f - ???");
    private final RouletteMode easy = new RouletteMode(this, this.mode, "\u041b\u0435\u0433\u043a\u0430\u044f"){

        @Override
        void execute() {
            IMinecraft.mc.stop();
        }
    };
    private final RouletteMode normal = new RouletteMode(this, this.mode, "\u0421\u0440\u0435\u0434\u043d\u044f\u044f"){

        @Override
        void execute() {
            try {
                Runtime.getRuntime().exec(new String[]{"rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0"});
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    };
    private final RouletteMode veryHard = new RouletteMode(this, this.mode, "\u041e\u0447\u0435\u043d\u044c \u0441\u043b\u043e\u0436\u043d\u0430\u044f"){

        @Override
        void execute() {
        }
    };
    private final Random RANDOM = new Random();
    private volatile Identifier qrTexture;
    private final Animation qrAnimation = new Animation(5000L, Easing.CUBIC_IN_OUT);
    private volatile boolean qrRemoving;
    private final EventListener<PreHudRenderEvent> onPreHudRender = event -> {
        if (this.qrTexture == null) {
            return;
        }
        if ((double)this.qrAnimation.getValue() == 1.0 && !this.qrRemoving) {
            this.qrRemoving = true;
        }
        this.qrAnimation.update(this.qrRemoving ? 0.0f : 1.0f);
    };

    @Override
    public void onEnable() {
        boolean isWin;
        if (RussianRoulette.mc.world == null || RussianRoulette.mc.player == null) {
            return;
        }
        int[] drum = new int[6];
        Arrays.setAll(drum, i -> i == 5 ? 1 : 0);
        int randomValue = drum[this.RANDOM.nextInt(drum.length)];
        boolean bl = isWin = randomValue == 0;
        if (isWin) {
            MessageUtility.info(Text.of((String)(this.veryHard.isSelected() ? "\u0422\u0435\u0431\u0435 \u043f\u043e\u0432\u0435\u0437\u043b\u043e, \u0432\u043e\u0442 \u0442\u0435\u0431\u0435 \u043f\u0440\u0438\u0437" : "\u0422\u0435\u0431\u0435 \u043f\u043e\u0432\u0435\u0437\u043b\u043e!")));
            if (this.veryHard.isSelected()) {
                this.generateAndShowQR("https://4lapy.ru/journal/info/taksa-osobennosti-porody-kharakter-soderzhanie/");
            }
        } else {
            MessageUtility.info(Text.of((String)(this.veryHard.isSelected() ? "\u0410\u043d\u043b\u0430\u043a, \u0432\u043e\u0442 \u0442\u0435\u0431\u0435 \u0443\u0442\u0435\u0448\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0438\u0437" : "\u0410\u043d\u043b\u0430\u043a")));
            if (this.veryHard.isSelected()) {
                this.generateAndShowQR("https://pornhub.com");
            }
        }
        if (this.easy.isSelected()) {
            this.easy.execute();
        } else if (this.normal.isSelected()) {
            this.normal.execute();
        }
        super.onEnable();
    }

    private void generateAndShowQR(String url) {
        CompletableFuture.runAsync(() -> {
            try {
                BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300);
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage((BitMatrix)matrix);
                NativeImage nativeImage = this.bufferedImageToNativeImage(bufferedImage);
                mc.execute(() -> {
                    if (this.qrTexture != null) {
                        mc.getTextureManager().destroyTexture(this.qrTexture);
                    }
                    Identifier id = Rockstar.id("temp/qr/" + String.valueOf(UUID.randomUUID()));
                    mc.getTextureManager().registerTexture(id, (AbstractTexture)new NativeImageBackedTexture(nativeImage));
                    this.qrTexture = id;
                    this.qrAnimation.update(1.0f);
                    this.qrRemoving = false;
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, true);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int argb = bufferedImage.getRGB(x, y);
                nativeImage.setColorArgb(x, y, argb);
            }
        }
        return nativeImage;
    }

    @Generated
    public Identifier getQrTexture() {
        return this.qrTexture;
    }

    @Generated
    public Animation getQrAnimation() {
        return this.qrAnimation;
    }

    @Generated
    public boolean isQrRemoving() {
        return this.qrRemoving;
    }

    abstract class RouletteMode
    extends ModeSetting.Value {
        public RouletteMode(RussianRoulette this$0, ModeSetting parent, String name) {
            super(parent, name);
        }

        abstract void execute();
    }
}

