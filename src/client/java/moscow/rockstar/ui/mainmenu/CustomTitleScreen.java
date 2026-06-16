/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 *  net.minecraft.client.gui.screen.option.OptionsScreen
 *  net.minecraft.client.gui.screen.world.SelectWorldScreen
 */
package moscow.rockstar.ui.mainmenu;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.ui.mainmenu.CustomButton;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class CustomTitleScreen
extends CustomScreen
implements IMinecraft {
    private static boolean once;
    private static final List<CustomButton> buttons;
    private boolean active;
    private final Animation activeAnimation = new Animation(1000L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final ColorRGBA dateColor = new ColorRGBA(171.0f, 254.0f, 255.0f);
    private final ColorRGBA timeColor = new ColorRGBA(203.0f, 254.0f, 255.0f);

    @Compile
    @VMProtect(type=VMProtectType.MUTATION)
    protected void init() {
        String basePath = "image/mainmenu/icons/";
        if (!once) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
                ClientSounds.WELCOME.play(Rockstar.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue());
            }
            buttons.add(new CustomButton(basePath + "single.png", 12.0f, () -> mc.setScreen((Screen)new SelectWorldScreen((Screen)this))));
            buttons.add(new CustomButton(basePath + "multi.png", 12.0f, () -> mc.setScreen((Screen)new MultiplayerScreen((Screen)this))));
            buttons.add(new CustomButton(basePath + "settings.png", 12.0f, () -> mc.setScreen((Screen)new OptionsScreen((Screen)this, CustomTitleScreen.mc.options))));
            buttons.add(new CustomButton(basePath + "quit.png", 14.0f, () -> ((MinecraftClient)mc).stop()));
            once = true;
        }
        super.init();
    }

    @Override
    public void render(UIContext context) {
        Font timeFont = Fonts.ROUND_BOLD.getFont(65.0f);
        Font dateFont = Fonts.MEDIUM.getFont(16.0f);
        Font unlockFont = Fonts.REGULAR.getFont(10.0f);
        float textAlpha = 255.0f * (0.5f + 0.5f * this.activeAnimation.getValue());
        float timeOffset = MathUtility.interpolate((float)this.height / 2.0f - 20.0f, 80.0, this.activeAnimation.getValue());
        Rect rect = new Rect((float)(-this.width) / 2.0f, (float)(-this.width) / 3.0f, (float)this.width * 1.5f, this.width);
        this.activeAnimation.update(this.active);
        context.drawRoundedRect(0.0f, 0.0f, (float)this.width, (float)this.height, BorderRadius.ZERO, new VerticalGradient(new ColorRGBA(26.0f, 34.0f, 56.0f), new ColorRGBA(5.0f, 3.0f, 12.0f)));
        RenderUtility.scale(context.getMatrices(), (float)this.width / 2.0f, (float)this.height / 2.0f, 1.1f - 0.1f * this.activeAnimation.getValue());
        context.drawTexture(Rockstar.id("image/mainmenu/background.png"), rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        RenderUtility.end(context.getMatrices());
        context.drawCenteredText(dateFont, TextUtility.getFormattedDate(), (float)this.width / 2.0f, timeOffset - 23.0f, ColorRGBA.WHITE.withAlpha(textAlpha));
        context.drawCenteredText(timeFont, TextUtility.getCurrentTime(), (float)this.width / 2.0f, timeOffset, ColorRGBA.WHITE.withAlpha(textAlpha));
        context.drawRoundedRect((float)this.width / 2.0f - 36.0f, (float)(this.height - 5) - 3.0f * this.activeAnimation.getValue(), 72.0f, 3.0f, BorderRadius.all(1.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.activeAnimation.getValue()));
        context.drawCenteredText(unlockFont, Localizator.translate("mainmenu.next"), (float)this.width / 2.0f, (float)(this.height - 15) + 3.0f * this.activeAnimation.getValue(), ColorRGBA.WHITE.withAlpha(155.0f * (1.0f - this.activeAnimation.getValue())));
        DrawUtility.blurProgram.draw();
        float offset = 0.0f;
        for (CustomButton button : buttons) {
            button.getActiveAnim().update((float)(buttons.size() - buttons.indexOf(button)) > (1.0f - this.activeAnimation.getValue()) * (float)buttons.size() + 0.5f);
            button.set((float)this.width / 2.0f - 69.0f + offset, (this.height > 500 ? (float)this.height / 2.0f : (float)this.height / 1.25f) - 5.0f - 10.0f * button.getActiveAnim().getValue(), 30.0f, 30.0f);
            offset += button.getWidth() + 6.0f;
            button.draw(context);
        }
        if (this.shouldShowIsland()) {
            Rockstar.getInstance().getHud().getIsland().render(context);
        }
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.shouldShowIsland() && Rockstar.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
            return;
        }
        for (CustomButton customButton : buttons) {
            if (!customButton.hovered(mouseX, mouseY) || customButton.getActiveAnim().getValue() != 1.0f) continue;
            customButton.click(mouseX, mouseY, button.getButtonIndex());
            return;
        }
        this.active = !this.active;
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Compile
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 69) {
            Rockstar.getInstance().getThemeManager().switchTheme();
        }
        if (Screen.hasControlDown() && keyCode == 82) {
            MinecraftClient.getInstance().setScreen((Screen)new MultiplayerScreen((Screen)this));
        }
        if (Screen.hasControlDown() && keyCode == 84) {
            MinecraftClient.getInstance().setScreen((Screen)new SelectWorldScreen((Screen)this));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean shouldShowIsland() {
        return Rockstar.getInstance().getMusicTracker().haveActiveSession();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    static {
        buttons = new ArrayList<CustomButton>();
    }
}

