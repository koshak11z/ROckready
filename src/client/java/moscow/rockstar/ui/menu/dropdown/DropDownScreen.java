/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.InputUtil
 */
package moscow.rockstar.ui.menu.dropdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.RussianRoulette;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.Ambience;
import moscow.rockstar.systems.modules.modules.visuals.Arrows;
import moscow.rockstar.systems.modules.modules.visuals.CustomFog;
import moscow.rockstar.systems.modules.modules.visuals.FriendMarkers;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.modules.modules.visuals.Nametags;
import moscow.rockstar.systems.modules.modules.visuals.Prediction;
import moscow.rockstar.systems.modules.modules.visuals.TargetESP;
import moscow.rockstar.systems.modules.modules.visuals.World;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.components.popup.list.CheckBox;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class DropDownScreen
extends MenuScreen
implements IMinecraft {
    private final Animation searchAnimation = new Animation(300L, Easing.BAKEK);
    private final Animation appendingAnim = new Animation(300L, Easing.BAKEK);
    private boolean closing;
    private List<MenuPanel> panels = new ArrayList<MenuPanel>();
    private float panelWidth;
    private float panelHeight;
    private String desc = "";
    private AnimatedText descText;
    private final List<ColorPicker> colorPickers = new ArrayList<ColorPicker>();
    private ColorPicker themeAccentPicker;
    private ColorPicker themeBackgroundPicker;
    private ColorPicker themeAdditionalPicker;
    private ColorPicker themeTextPicker;
    private ColorPicker themeGuiTextActivePicker;
    private ColorPicker themeGuiTextInactivePicker;
    private ColorPicker themeHeaderTextPicker;
    private ColorPicker themeLogoBackgroundPicker;
    private ColorPicker themeLogoTextPicker;
    private ColorPicker themeTargetESPPicker;
    private ColorPicker themeOutlinePicker;
    private ColorPicker themeFlatPicker;
    private ColorPicker themeSliderTrackPicker;
    private ColorPicker themeSliderCirclePicker;
    private ColorPicker themeSliderWindowPicker;
    private ColorPicker themeTooltipTextPicker;
    private TextField searchField;
    private Popup visualsModulesPopup;
    private Popup blurElementsPopup;

    @Compile
    protected void init() {
        this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0f), 10.0f, 300L, Easing.BAKEK).centered();
        this.closing = false;
        this.panelWidth = 115.0f;
        this.panelHeight = 252.0f;
        this.panels = Arrays.stream(MenuCategory.values()).map(MenuPanel::new).toList();
        for (MenuPanel panel : this.panels) {
            panel.setWidth(this.panelWidth);
            panel.setHeight(this.panelHeight);
            panel.onInit();
        }
        this.searchField = new TextField(Fonts.REGULAR.getFont(12.0f));
        HashMap<String, FieldAction> append = new HashMap<String, FieldAction>();
        for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
            FieldAction action = new FieldAction(module::toggle, () -> this.panels.forEach(panel -> panel.getModuleComponents().stream().filter(component -> component.getModule() == module).forEach(ModuleComponent::open)));
            append.put(module.getName().replace(" ", ""), action);
            append.put(module.getName(), action);
        }
        this.searchField.setAppend(append);
        super.init();
    }

    public void tick() {
        this.handleMovementKeys();
        super.tick();
    }

    @Override
    @Compile
    public void render(UIContext context) {
        this.menuAnimation.setEasing(Easing.LINEAR);
        this.menuAnimation.update(this.isClosing() ? 0.0f : 1.0f);
        this.menuAnimation.setDuration(this.isClosing() ? 160L : 220L);
        this.desc = "";
        float spacing = 10.0f;
        boolean showThemeEditor = Interface.customSelected();
        float totalPanels = (float)this.panels.size() + (showThemeEditor ? 1.0f : 0.0f);
        float x = ((float)this.width - (this.panelWidth + spacing) * totalPanels + spacing) / 2.0f;
        float y = ((float)this.height - this.panelHeight) / 2.0f;
        context.pushMatrix();
        float offset = showThemeEditor ? this.panelWidth + spacing : 0.0f;
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.setX(MathUtility.interpolate(x + offset, (float)this.width / 2.0f - this.panelWidth / 2.0f, this.closing ? (double)(1.0f - this.menuAnimation.getValue()) : 0.0));
            menuPanel.setY(y);
            menuPanel.setWidth(this.panelWidth);
            menuPanel.setHeight(this.panelHeight);
            offset += this.panelWidth + spacing;
        }
        if (showThemeEditor) {
            this.renderThemeEditorPanel(context, x, y);
        }
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.renderBlur(context);
            offset += this.panelWidth + spacing;
        }
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.render(context);
        }
        IconBatching icon = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
        for (MenuPanel panel : this.panels) {
            panel.drawType(context);
        }
        ((Batching)icon).draw();
        for (MenuPanel panel : this.panels) {
            this.scissor(context, panel, () -> {
                FontBatching font = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
                panel.drawRegular8(context);
                ((Batching)font).draw();
                IconBatching icon1 = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
                panel.drawIcons(context);
                ((Batching)icon1).draw();
                RectBatching split = new RectBatching(VertexFormats.POSITION_COLOR, context.getMatrices());
                panel.drawSplit(context);
                ((Batching)split).draw();
            });
        }
        context.popMatrix();
        if (this.menuAnimation.getValue() < 0.5f) {
            this.desc = "";
        }
        this.searchAnimation.update(this.searchField.isFocused());
        float f = this.menuAnimation.getValue() * this.searchAnimation.getValue();
        if (f > 0.0f) {
            if (Interface.showMinimalizm() && Interface.blurSearchEnabled()) {
                context.drawBlurredRect(this.searchField.getX(), this.searchField.getY(), this.searchField.getWidth(), this.searchField.getHeight(), 45.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * f));
            }
            if (Interface.showGlass()) {
                context.drawLiquidGlass(this.searchField.getX(), this.searchField.getY(), this.searchField.getWidth(), this.searchField.getHeight(), 2.0f, 0.08f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * f));
            }
            boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
            context.drawRoundedRect(this.searchField.getX(), this.searchField.getY(), this.searchField.getWidth(), this.searchField.getHeight(), BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha((dark ? 0.9f - 0.7f * Interface.glass() : 0.7f) * f));
            this.searchField.set((float)this.width / 2.0f - this.searchField.getWidth() / 2.0f, (float)(this.height - 20) - 20.0f * f, 100.0f, 20.0f);
            this.searchField.setAlpha(f);
            this.searchField.setTextColor(Colors.getTextColor());
            this.searchField.render(context);
            this.appendingAnim.update(!this.searchField.getAppending().isBlank());
            context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip.tab"), (float)this.width / 2.0f, (float)(this.height - 65) - 10.0f * f * this.appendingAnim.getValue(), ColorRGBA.WHITE.withAlpha(150.0f * f * this.appendingAnim.getValue()));
            context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip.enter"), (float)this.width / 2.0f, (float)(this.height - 50) - 10.0f * f * this.appendingAnim.getValue(), ColorRGBA.WHITE.withAlpha(150.0f * f * this.appendingAnim.getValue()));
        } else {
            this.searchField.clear();
        }
        context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip"), (float)this.width / 2.0f, (float)(this.height - 20) - 10.0f * this.menuAnimation.getValue() * (1.0f - this.searchAnimation.getValue()), ColorRGBA.WHITE.withAlpha(150.0f * this.menuAnimation.getValue() * (1.0f - this.searchAnimation.getValue())));
        this.descText.pos((float)this.width / 2.0f, (float)this.height / 2.0f - 150.0f);
        if (!this.desc.contains(".description")) {
            this.descText.update(this.desc);
            this.descText.render(context);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.render(context);
            if (DropDownScreen.mc.currentScreen instanceof DropDownScreen) continue;
            colorPicker.setShowing(false);
        }
        this.colorPickers.removeIf(popup -> popup.getAnimation().getValue() == 0.0f && !popup.isShowing());
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.render(context);
            if (!(DropDownScreen.mc.currentScreen instanceof DropDownScreen) || !Interface.customSelected()) {
                this.visualsModulesPopup.setShowing(false);
            }
            if (this.visualsModulesPopup.getAnimation().getValue() == 0.0f && !this.visualsModulesPopup.isShowing()) {
                this.visualsModulesPopup = null;
            }
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.render(context);
            if (!(DropDownScreen.mc.currentScreen instanceof DropDownScreen) || !Interface.customSelected()) {
                this.blurElementsPopup.setShowing(false);
            }
            if (this.blurElementsPopup.getAnimation().getValue() == 0.0f && !this.blurElementsPopup.isShowing()) {
                this.blurElementsPopup = null;
            }
        }
        RussianRoulette russianRoulette = Rockstar.getInstance().getModuleManager().getModule(RussianRoulette.class);
        if (russianRoulette.isEnabled()) {
            if (russianRoulette.getQrTexture() == null) {
                return;
            }
            if (russianRoulette.getQrAnimation().getValue() == 0.0f && russianRoulette.isQrRemoving()) {
                return;
            }
            float scale = 180.0f;
            float xQR = ((float)mc.getWindow().getScaledWidth() - scale) / 2.0f;
            float yQR = ((float)mc.getWindow().getScaledHeight() - scale) / 2.0f;
            context.drawTexture(russianRoulette.getQrTexture(), xQR, yQR, scale, scale, Colors.WHITE.withAlpha((int)(255.0f * russianRoulette.getQrAnimation().getValue())));
        }
    }

    @Compile
    private void handleMovementKeys() {
        KeyBinding[] movementKeys;
        if (DropDownScreen.mc.player == null || this.isTyping()) {
            return;
        }
        long windowHandle = mc.getWindow().getHandle();
        for (KeyBinding key : movementKeys = new KeyBinding[]{DropDownScreen.mc.options.forwardKey, DropDownScreen.mc.options.backKey, DropDownScreen.mc.options.leftKey, DropDownScreen.mc.options.rightKey, DropDownScreen.mc.options.jumpKey}) {
            int keyCode = InputUtil.fromTranslationKey((String)key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
        if (DropDownScreen.mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey((String)DropDownScreen.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            DropDownScreen.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
    }

    private boolean isTyping() {
        return DropDownScreen.mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
    }

    public boolean isBindingModule() {
        return this.panels.stream().flatMap(panel -> panel.getModuleComponents().stream()).anyMatch(ModuleComponent::isBindingMode);
    }

    private void scissor(UIContext context, MenuPanel panel, Runnable runnable) {
        panel.scale(context);
        panel.push(context);
        runnable.run();
        ScissorUtility.pop();
        RenderUtility.end(context.getMatrices());
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (Rockstar.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
            return;
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onMouseClicked(mouseX, mouseY, button);
            if (this.blurElementsPopup.isHovered(mouseX, mouseY)) {
                return;
            }
            this.blurElementsPopup.setShowing(false);
        }
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onMouseClicked(mouseX, mouseY, button);
            if (this.visualsModulesPopup.isHovered(mouseX, mouseY)) {
                return;
            }
            this.visualsModulesPopup.setShowing(false);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            boolean isPick = colorPicker.isPick();
            colorPicker.onMouseClicked(mouseX, mouseY, button);
            if (colorPicker.isHovered(mouseX, mouseY) || isPick) {
                return;
            }
            colorPicker.setShowing(false);
        }
        if (this.handleThemeEditorClick(mouseX, mouseY, button)) {
            return;
        }
        for (MenuPanel panel : this.panels) {
            if (!panel.isHovered(mouseX, mouseY)) continue;
            panel.onMouseClicked(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused() && button != MouseButton.MIDDLE) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    private void renderThemeEditorPanel(UIContext context, float x, float y) {
        this.themeAccentPicker = this.clearClosedPicker(this.themeAccentPicker);
        this.themeBackgroundPicker = this.clearClosedPicker(this.themeBackgroundPicker);
        this.themeAdditionalPicker = this.clearClosedPicker(this.themeAdditionalPicker);
        this.themeTextPicker = this.clearClosedPicker(this.themeTextPicker);
        this.themeGuiTextActivePicker = this.clearClosedPicker(this.themeGuiTextActivePicker);
        this.themeGuiTextInactivePicker = this.clearClosedPicker(this.themeGuiTextInactivePicker);
        this.themeHeaderTextPicker = this.clearClosedPicker(this.themeHeaderTextPicker);
        this.themeLogoBackgroundPicker = this.clearClosedPicker(this.themeLogoBackgroundPicker);
        this.themeLogoTextPicker = this.clearClosedPicker(this.themeLogoTextPicker);
        this.themeTargetESPPicker = this.clearClosedPicker(this.themeTargetESPPicker);
        this.themeOutlinePicker = this.clearClosedPicker(this.themeOutlinePicker);
        this.themeFlatPicker = this.clearClosedPicker(this.themeFlatPicker);
        this.themeSliderTrackPicker = this.clearClosedPicker(this.themeSliderTrackPicker);
        this.themeSliderCirclePicker = this.clearClosedPicker(this.themeSliderCirclePicker);
        this.themeSliderWindowPicker = this.clearClosedPicker(this.themeSliderWindowPicker);
        this.themeTooltipTextPicker = this.clearClosedPicker(this.themeTooltipTextPicker);

        float alpha = this.menuAnimation.getValue();
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;

        if (this.isClosing() || !Interface.customSelected()) {
            if (this.themeAccentPicker != null) {
                this.themeAccentPicker.setShowing(false);
            }
            if (this.themeBackgroundPicker != null) {
                this.themeBackgroundPicker.setShowing(false);
            }
            if (this.themeAdditionalPicker != null) {
                this.themeAdditionalPicker.setShowing(false);
            }
            if (this.themeTextPicker != null) {
                this.themeTextPicker.setShowing(false);
            }
            if (this.themeGuiTextActivePicker != null) {
                this.themeGuiTextActivePicker.setShowing(false);
            }
            if (this.themeGuiTextInactivePicker != null) {
                this.themeGuiTextInactivePicker.setShowing(false);
            }
            if (this.themeHeaderTextPicker != null) {
                this.themeHeaderTextPicker.setShowing(false);
            }
            if (this.themeLogoBackgroundPicker != null) {
                this.themeLogoBackgroundPicker.setShowing(false);
            }
            if (this.themeLogoTextPicker != null) {
                this.themeLogoTextPicker.setShowing(false);
            }
            if (this.themeTargetESPPicker != null) {
                this.themeTargetESPPicker.setShowing(false);
            }
            if (this.themeOutlinePicker != null) {
                this.themeOutlinePicker.setShowing(false);
            }
            if (this.themeFlatPicker != null) {
                this.themeFlatPicker.setShowing(false);
            }
            if (this.themeSliderTrackPicker != null) {
                this.themeSliderTrackPicker.setShowing(false);
            }
            if (this.themeSliderCirclePicker != null) {
                this.themeSliderCirclePicker.setShowing(false);
            }
            if (this.themeSliderWindowPicker != null) {
                this.themeSliderWindowPicker.setShowing(false);
            }
            if (this.themeTooltipTextPicker != null) {
                this.themeTooltipTextPicker.setShowing(false);
            }
        }

        if (this.themeAccentPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomAccentColor(this.themeAccentPicker.built());
        }
        if (this.themeBackgroundPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomBackgroundColor(this.themeBackgroundPicker.built());
        }
        if (this.themeAdditionalPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomAdditionalColor(this.themeAdditionalPicker.built());
        }
        if (this.themeTextPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomTextColor(this.themeTextPicker.built());
        }
        if (this.themeGuiTextActivePicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomGuiTextActiveColor(this.themeGuiTextActivePicker.built());
        }
        if (this.themeGuiTextInactivePicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomGuiTextInactiveColor(this.themeGuiTextInactivePicker.built());
        }
        if (this.themeHeaderTextPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomHeaderTextColor(this.themeHeaderTextPicker.built());
        }
        if (this.themeLogoBackgroundPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomLogoBackgroundColor(this.themeLogoBackgroundPicker.built());
        }
        if (this.themeLogoTextPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomLogoTextColor(this.themeLogoTextPicker.built());
        }
        if (this.themeTargetESPPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomTargetESPColor(this.themeTargetESPPicker.built());
        }
        if (this.themeOutlinePicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomOutlineColor(this.themeOutlinePicker.built());
        }
        if (this.themeFlatPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomFlatColor(this.themeFlatPicker.built());
        }
        if (this.themeSliderTrackPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomSliderTrackColor(this.themeSliderTrackPicker.built());
        }
        if (this.themeSliderCirclePicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomSliderCircleColor(this.themeSliderCirclePicker.built());
        }
        if (this.themeSliderWindowPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomSliderWindowColor(this.themeSliderWindowPicker.built());
        }
        if (this.themeTooltipTextPicker != null) {
            Rockstar.getInstance().getThemeManager().setCustomTooltipTextColor(this.themeTooltipTextPicker.built());
        }

        if (Interface.showMinimalizm() && Interface.blurThemeEditorEnabled()) {
            context.drawBlurredRect(x, y, this.panelWidth, this.panelHeight, 45.0f, BorderRadius.all(10.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(x, y, this.panelWidth, this.panelHeight, 10.0f, 0.08f, BorderRadius.all(10.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        }
        context.drawRoundedRect(x, y, this.panelWidth, this.panelHeight, BorderRadius.all(10.0f), Colors.getBackgroundColor().mulAlpha((dark ? 0.9f - 0.7f * Interface.glass() : 0.7f) * alpha));

        float headerHeight = 24.0f;
        float rowHeight = 12.0f;
        float rowX = x + 8.0f;
        float rowWidth = this.panelWidth - 16.0f;
        float rowStartY = y + headerHeight + 6.0f;

        context.drawText(Fonts.SEMIBOLD.getFont(9.0f), Localizator.translate("theme_editor.title"), x + 10.0f, y + 9.0f, Colors.getHeaderTextColor().mulAlpha(alpha));

        this.renderThemeEditorRow(context, rowX, rowStartY + 0.0f * rowHeight, rowWidth, rowHeight, "theme_editor.accent", Rockstar.getInstance().getThemeManager().getCustomAccentColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 1.0f * rowHeight, rowWidth, rowHeight, "theme_editor.background", Rockstar.getInstance().getThemeManager().getCustomBackgroundColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 2.0f * rowHeight, rowWidth, rowHeight, "theme_editor.additional", Rockstar.getInstance().getThemeManager().getCustomAdditionalColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 3.0f * rowHeight, rowWidth, rowHeight, "theme_editor.text", Rockstar.getInstance().getThemeManager().getCustomTextColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 4.0f * rowHeight, rowWidth, rowHeight, "theme_editor.outline", Rockstar.getInstance().getThemeManager().getCustomOutlineColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 5.0f * rowHeight, rowWidth, rowHeight, "theme_editor.flat", Rockstar.getInstance().getThemeManager().getCustomFlatColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 6.0f * rowHeight, rowWidth, rowHeight, "theme_editor.gui_text_active", Rockstar.getInstance().getThemeManager().getCustomGuiTextActiveColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 7.0f * rowHeight, rowWidth, rowHeight, "theme_editor.gui_text_inactive", Rockstar.getInstance().getThemeManager().getCustomGuiTextInactiveColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 8.0f * rowHeight, rowWidth, rowHeight, "theme_editor.header_text", Rockstar.getInstance().getThemeManager().getCustomHeaderTextColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 9.0f * rowHeight, rowWidth, rowHeight, "theme_editor.logo_background", Rockstar.getInstance().getThemeManager().getCustomLogoBackgroundColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 10.0f * rowHeight, rowWidth, rowHeight, "theme_editor.logo_text", Rockstar.getInstance().getThemeManager().getCustomLogoTextColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 11.0f * rowHeight, rowWidth, rowHeight, "theme_editor.visuals", Rockstar.getInstance().getThemeManager().getCustomTargetESPColor(), alpha);
        this.renderThemeEditorModulesRow(context, rowX, rowStartY + 12.0f * rowHeight, rowWidth, rowHeight, alpha);
        this.renderThemeEditorBlurElementsRow(context, rowX, rowStartY + 13.0f * rowHeight, rowWidth, rowHeight, alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 14.0f * rowHeight, rowWidth, rowHeight, "theme_editor.slider_track", Rockstar.getInstance().getThemeManager().getCustomSliderTrackColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 15.0f * rowHeight, rowWidth, rowHeight, "theme_editor.slider_circle", Rockstar.getInstance().getThemeManager().getCustomSliderCircleColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 16.0f * rowHeight, rowWidth, rowHeight, "theme_editor.slider_window", Rockstar.getInstance().getThemeManager().getCustomSliderWindowColor(), alpha);
        this.renderThemeEditorRow(context, rowX, rowStartY + 17.0f * rowHeight, rowWidth, rowHeight, "theme_editor.tooltip_text", Rockstar.getInstance().getThemeManager().getCustomTooltipTextColor(), alpha);
    }

    private void renderThemeEditorModulesRow(UIContext context, float x, float y, float width, float height, float alpha) {
        boolean hovered = GuiUtility.isHovered((double)x, (double)y, (double)width, (double)height, context.getMouseX(), context.getMouseY());
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float rightPadding = 4.0f;
        float arrowSize = 8.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(4.0f), Colors.getAdditionalColor().mulAlpha(0.35f + (hovered ? 0.1f : 0.0f)).withAlpha(255.0f * alpha));
        context.drawText(Fonts.REGULAR.getFont(8.0f), Localizator.translate("theme_editor.visuals_modules"), x + 5.0f, y + GuiUtility.getMiddleOfBox(Fonts.REGULAR.getFont(8.0f).height(), height) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + (hovered ? 0.25f : 0.0f)) * alpha));
        float arrowX = x + width - rightPadding - arrowSize;
        float arrowY = y + (height - arrowSize) / 2.0f;
        float rotation = this.visualsModulesPopup != null && this.visualsModulesPopup.isShowing() ? 90.0f : 0.0f;
        RenderUtility.rotate(context.getMatrices(), arrowX + arrowSize / 2.0f, arrowY + arrowSize / 2.0f, rotation);
        context.drawTexture(Rockstar.id("icons/arrow.png"), arrowX, arrowY, arrowSize, arrowSize, Colors.getTextColor().withAlpha(255.0f * alpha));
        RenderUtility.end(context.getMatrices());
    }

    private void renderThemeEditorBlurElementsRow(UIContext context, float x, float y, float width, float height, float alpha) {
        boolean hovered = GuiUtility.isHovered((double)x, (double)y, (double)width, (double)height, context.getMouseX(), context.getMouseY());
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float rightPadding = 4.0f;
        float arrowSize = 8.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(4.0f), Colors.getAdditionalColor().mulAlpha(0.35f + (hovered ? 0.1f : 0.0f)).withAlpha(255.0f * alpha));
        context.drawText(Fonts.REGULAR.getFont(8.0f), Localizator.translate("theme_editor.blur_elements"), x + 5.0f, y + GuiUtility.getMiddleOfBox(Fonts.REGULAR.getFont(8.0f).height(), height) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + (hovered ? 0.25f : 0.0f)) * alpha));
        float arrowX = x + width - rightPadding - arrowSize;
        float arrowY = y + (height - arrowSize) / 2.0f;
        float rotation = this.blurElementsPopup != null && this.blurElementsPopup.isShowing() ? 90.0f : 0.0f;
        RenderUtility.rotate(context.getMatrices(), arrowX + arrowSize / 2.0f, arrowY + arrowSize / 2.0f, rotation);
        context.drawTexture(Rockstar.id("icons/arrow.png"), arrowX, arrowY, arrowSize, arrowSize, Colors.getTextColor().withAlpha(255.0f * alpha));
        RenderUtility.end(context.getMatrices());
    }

    private void renderThemeEditorRow(UIContext context, float x, float y, float width, float height, String nameKey, ColorRGBA color, float alpha) {
        boolean hovered = GuiUtility.isHovered((double)x, (double)y, (double)width, (double)height, context.getMouseX(), context.getMouseY());
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float previewSize = 8.0f;
        float rightPadding = 4.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(4.0f), Colors.getAdditionalColor().mulAlpha(0.35f + (hovered ? 0.1f : 0.0f)).withAlpha(255.0f * alpha));
        context.drawText(Fonts.REGULAR.getFont(8.0f), Localizator.translate(nameKey), x + 5.0f, y + GuiUtility.getMiddleOfBox(Fonts.REGULAR.getFont(8.0f).height(), height) - 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + (hovered ? 0.25f : 0.0f)) * alpha));
        context.drawRoundedRect(x + width - rightPadding - previewSize - 2.0f, y + (height - previewSize) / 2.0f, previewSize + 2.0f, previewSize, BorderRadius.all(3.0f), Colors.getOutlineColor().withAlpha(255.0f * alpha));
        context.drawRoundedRect(x + width - rightPadding - previewSize - 1.0f, y + (height - previewSize) / 2.0f + 1.0f, previewSize, previewSize - 2.0f, BorderRadius.all(3.0f), color);
    }

    private boolean handleThemeEditorClick(double mouseX, double mouseY, MouseButton button) {
        if (!Interface.customSelected() || button != MouseButton.LEFT) {
            return false;
        }
        float spacing = 10.0f;
        float totalPanels = (float)this.panels.size() + 1.0f;
        float x = ((float)this.width - (this.panelWidth + spacing) * totalPanels + spacing) / 2.0f;
        float y = ((float)this.height - this.panelHeight) / 2.0f;

        float headerHeight = 24.0f;
        float rowHeight = 12.0f;
        float rowX = x + 8.0f;
        float rowWidth = this.panelWidth - 16.0f;
        float rowStartY = y + headerHeight + 6.0f;

        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 0.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeAccentPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomAccentColor(), Localizator.translate("theme_editor.accent"));
            this.colorPickers.add(this.themeAccentPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 1.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeBackgroundPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomBackgroundColor(), Localizator.translate("theme_editor.background"));
            this.colorPickers.add(this.themeBackgroundPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 2.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeAdditionalPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomAdditionalColor(), Localizator.translate("theme_editor.additional"));
            this.colorPickers.add(this.themeAdditionalPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 3.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomTextColor(), Localizator.translate("theme_editor.text"));
            this.colorPickers.add(this.themeTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 4.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeOutlinePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomOutlineColor(), Localizator.translate("theme_editor.outline"));
            this.colorPickers.add(this.themeOutlinePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 5.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeFlatPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomFlatColor(), Localizator.translate("theme_editor.flat"));
            this.colorPickers.add(this.themeFlatPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 6.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeGuiTextActivePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomGuiTextActiveColor(), Localizator.translate("theme_editor.gui_text_active"));
            this.colorPickers.add(this.themeGuiTextActivePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 7.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeGuiTextInactivePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomGuiTextInactiveColor(), Localizator.translate("theme_editor.gui_text_inactive"));
            this.colorPickers.add(this.themeGuiTextInactivePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 8.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeHeaderTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomHeaderTextColor(), Localizator.translate("theme_editor.header_text"));
            this.colorPickers.add(this.themeHeaderTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 9.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeLogoBackgroundPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomLogoBackgroundColor(), Localizator.translate("theme_editor.logo_background"));
            this.colorPickers.add(this.themeLogoBackgroundPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 10.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeLogoTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomLogoTextColor(), Localizator.translate("theme_editor.logo_text"));
            this.colorPickers.add(this.themeLogoTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 11.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeTargetESPPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomTargetESPColor(), Localizator.translate("theme_editor.visuals"));
            this.colorPickers.add(this.themeTargetESPPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 12.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.openVisualsModulesPopup((float)mouseX, (float)mouseY);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 13.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.openBlurElementsPopup((float)mouseX, (float)mouseY);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 14.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeSliderTrackPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderTrackColor(), Localizator.translate("theme_editor.slider_track"));
            this.colorPickers.add(this.themeSliderTrackPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 15.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeSliderCirclePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderCircleColor(), Localizator.translate("theme_editor.slider_circle"));
            this.colorPickers.add(this.themeSliderCirclePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 16.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeSliderWindowPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderWindowColor(), Localizator.translate("theme_editor.slider_window"));
            this.colorPickers.add(this.themeSliderWindowPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)rowX, (double)(rowStartY + 17.0f * rowHeight), (double)rowWidth, (double)rowHeight, mouseX, mouseY)) {
            this.themeTooltipTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomTooltipTextColor(), Localizator.translate("theme_editor.tooltip_text"));
            this.colorPickers.add(this.themeTooltipTextPicker);
            return true;
        }
        return false;
    }

    private void openVisualsModulesPopup(float x, float y) {
        if (this.visualsModulesPopup != null) {
            if (this.visualsModulesPopup.isShowing()) {
                this.visualsModulesPopup.setShowing(false);
                return;
            }
            this.visualsModulesPopup.setShowing(false);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.setShowing(false);
        }
        Ambience ambience = Rockstar.getInstance().getModuleManager().getModule(Ambience.class);
        Arrows arrows = Rockstar.getInstance().getModuleManager().getModule(Arrows.class);
        CustomFog customFog = Rockstar.getInstance().getModuleManager().getModule(CustomFog.class);
        FriendMarkers friendMarkers = Rockstar.getInstance().getModuleManager().getModule(FriendMarkers.class);
        Nametags nameTags = Rockstar.getInstance().getModuleManager().getModule(Nametags.class);
        Prediction prediction = Rockstar.getInstance().getModuleManager().getModule(Prediction.class);
        TargetESP targetESP = Rockstar.getInstance().getModuleManager().getModule(TargetESP.class);
        World world = Rockstar.getInstance().getModuleManager().getModule(World.class);
        boolean arrowsEnabled = arrows.isEnabled() && !arrows.getLines().isEnabled();
        boolean tracersEnabled = arrows.isEnabled() && arrows.getLines().isEnabled();
        Popup popup = new Popup(x, y, 120.0f, 6.0f);
        CheckBox ambienceBox = new CheckBox("Ambience").enabled(ambience.isEnabled() && ambience.getEndSky().isEnabled());
        ambienceBox.action(enabled -> {
            ambience.setEnabled(enabled, true);
            ambience.getEndSky().setEnabled(enabled);
        });
        CheckBox arrowsBox = new CheckBox("Arrows").enabled(arrowsEnabled);
        CheckBox tracersBox = new CheckBox("Arrows Tracers").enabled(tracersEnabled);
        arrowsBox.action(enabled -> {
            if (enabled) {
                arrows.setEnabled(true, true);
                arrows.getLines().setEnabled(false);
                tracersBox.enabled(false);
            } else {
                arrows.getLines().setEnabled(false);
                arrows.setEnabled(false, true);
            }
        });
        tracersBox.action(enabled -> {
            if (enabled) {
                arrows.setEnabled(true, true);
                arrows.getLines().setEnabled(true);
                arrowsBox.enabled(false);
            } else {
                arrows.getLines().setEnabled(false);
                arrows.setEnabled(false, true);
            }
        });
        popup.add(ambienceBox);
        popup.add(arrowsBox);
        popup.add(tracersBox);
        popup.add(new CheckBox("Custom Fog").enabled(customFog.isEnabled()).action(enabled -> customFog.setEnabled(enabled, true)));
        popup.add(new CheckBox("Friend Markers").enabled(friendMarkers.isEnabled()).action(enabled -> friendMarkers.setEnabled(enabled, true)));
        popup.add(new CheckBox("Name Tags").enabled(nameTags.isEnabled()).action(enabled -> nameTags.setEnabled(enabled, true)));
        popup.add(new CheckBox("Prediction").enabled(prediction.isEnabled()).action(enabled -> prediction.setEnabled(enabled, true)));
        popup.add(new CheckBox("Target ESP").enabled(targetESP.isEnabled()).action(enabled -> targetESP.setEnabled(enabled, true)));
        popup.add(new CheckBox("World").enabled(world.isEnabled()).action(enabled -> world.setEnabled(enabled, true)));
        this.visualsModulesPopup = popup;
    }

    private void openBlurElementsPopup(float x, float y) {
        if (this.blurElementsPopup != null) {
            if (this.blurElementsPopup.isShowing()) {
                this.blurElementsPopup.setShowing(false);
                return;
            }
            this.blurElementsPopup.setShowing(false);
        }
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.setShowing(false);
        }
        Interface interfaceModule = Rockstar.getInstance().getModuleManager().getModule(Interface.class);
        Popup popup = new Popup(x, y, 140.0f, 6.0f);
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_menu")).enabled(interfaceModule.getBlurMenu().isEnabled()).action(enabled -> interfaceModule.getBlurMenu().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_sidebar")).enabled(interfaceModule.getBlurSidebar().isEnabled()).action(enabled -> interfaceModule.getBlurSidebar().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_panels")).enabled(interfaceModule.getBlurPanels().isEnabled()).action(enabled -> interfaceModule.getBlurPanels().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_theme_editor")).enabled(interfaceModule.getBlurThemeEditor().isEnabled()).action(enabled -> interfaceModule.getBlurThemeEditor().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_search")).enabled(interfaceModule.getBlurSearch().isEnabled()).action(enabled -> interfaceModule.getBlurSearch().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_hud")).enabled(interfaceModule.getBlurHud().isEnabled()).action(enabled -> interfaceModule.getBlurHud().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_popups")).enabled(interfaceModule.getBlurPopups().isEnabled()).action(enabled -> interfaceModule.getBlurPopups().setEnabled(enabled)));
        popup.add(new CheckBox(Localizator.translate("modules.settings.interface.blur_notifications")).enabled(interfaceModule.getBlurNotifications().isEnabled()).action(enabled -> interfaceModule.getBlurNotifications().setEnabled(enabled)));
        this.blurElementsPopup = popup;
    }

    private ColorPicker clearClosedPicker(ColorPicker picker) {
        if (picker == null) {
            return null;
        }
        return picker.getAnimation().getValue() == 0.0f && !picker.isShowing() ? null : picker;
    }

    @Override
    @Compile
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onMouseReleased(mouseX, mouseY, button);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onMouseReleased(mouseX, mouseY, button);
        }
        for (MenuPanel panel : this.panels) {
            panel.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused()) {
            this.searchField.onMouseReleased(mouseX, mouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        for (MenuPanel panel : this.panels) {
            panel.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Compile
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onKeyPressed(keyCode, scanCode, modifiers);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchField != null && !this.searchField.isFocused() && Screen.hasControlDown() && keyCode == 70) {
            this.searchField.setFocused(true);
        }
        for (MenuPanel panel : this.panels) {
            panel.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchField.isFocused() && !this.isBindingModule()) {
            this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Compile
    public boolean charTyped(char chr, int modifiers) {
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.charTyped(chr, modifiers);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.charTyped(chr, modifiers);
        }
        if (this.searchField.isFocused() && !this.isBindingModule()) {
            this.searchField.charTyped(chr, modifiers);
        }
        for (MenuPanel panel : this.panels) {
            panel.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Compile
    public void close() {
        this.closing = true;
        Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).disable();
        Sounds soundsModule = Rockstar.getInstance().getModuleManager().getModule(Sounds.class);
        if (soundsModule.isEnabled()) {
            ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 1.0f);
        }
        Rockstar.getInstance().getFileManager().writeFile("client");
        if (Rockstar.getInstance().getConfigManager().getCurrent() != null) {
            Rockstar.getInstance().getConfigManager().getCurrent().save();
        }
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
        }
        super.close();
    }

    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Generated
    public Animation getSearchAnimation() {
        return this.searchAnimation;
    }

    @Generated
    public Animation getAppendingAnim() {
        return this.appendingAnim;
    }

    @Override
    @Generated
    public boolean isClosing() {
        return this.closing;
    }

    @Generated
    public List<MenuPanel> getPanels() {
        return this.panels;
    }

    @Generated
    public float getPanelWidth() {
        return this.panelWidth;
    }

    @Generated
    public float getPanelHeight() {
        return this.panelHeight;
    }

    @Generated
    public String getDesc() {
        return this.desc;
    }

    @Generated
    public AnimatedText getDescText() {
        return this.descText;
    }

    @Generated
    public List<ColorPicker> getColorPickers() {
        return this.colorPickers;
    }

    @Override
    @Generated
    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    @Generated
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Generated
    public TextField getSearchField() {
        return this.searchField;
    }

    static {
        new MenuPanel(null);
        new BezierSettingComponent(null, null);
        new BindSettingComponent(null, null);
        new BooleanSettingComponent(null, null);
        new ModeSettingComponent(null, null);
        new ButtonSettingComponent(null, null);
        new ColorSettingComponent(null, null);
        new StringSettingComponent(null, null);
        new RangeSettingComponent(null, null);
        new SliderSettingComponent(null, null);
    }
}

