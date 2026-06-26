/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.InputUtil
 */
package moscow.rockstar.ui.menu.modern;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
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
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.components.popup.list.CheckBox;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.ui.menu.modern.ModernCategory;
import moscow.rockstar.ui.menu.modern.components.ModernModule;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.impl.FadeOutBatching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RoundedRectBatching;
import moscow.rockstar.utility.render.batching.impl.SquircleBatching;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ModernScreen
extends MenuScreen
implements IMinecraft,
IScaledResolution {
    private final Rect menuWindow;
    private float dragX;
    private float dragY;
    private boolean drag;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private MenuCategory current = MenuCategory.COMBAT;
    private final List<ColorPicker> colorPickers = new LinkedList<ColorPicker>();
    private final List<ModernCategory> categories = new ArrayList<ModernCategory>();
    private final Animation currentCategory = new Animation(300L, Easing.BAKEK_SMALLER);
    private boolean themeEditorOpen;
    private final TextField searchField;
    private final PenisPlayer searchPenis;
    private boolean prevFocused;
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
    private ColorPicker themeAutoPicker;
    private Popup visualsModulesPopup;
    private Popup blurElementsPopup;
    Timer timer = new Timer();

    private static final float TOP_BAR = 36.0f;
    private static final float TAB_W = 34.0f;

    public ModernScreen() {
        float width = 510.0f;
        float height = 300.0f;
        this.menuWindow = new Rect(sr.getScaledWidth() / 2.0f - width / 2.0f, sr.getScaledHeight() / 2.0f - height / 2.0f, width, height);
        this.categories.clear();
        for (MenuCategory category : MenuCategory.values()) {
            LinkedList<ModernModule> filteredModules = new LinkedList<ModernModule>();
            ModernCategory modern = new ModernCategory(category, filteredModules);
            try {
                modern.setPenis(new PenisPlayer(Rockstar.id("penises/" + category.getName().toLowerCase() + ".penis")));
            }
            catch (RuntimeException runtimeException) {
                // empty catch block
            }
            this.categories.add(modern);
            filteredModules.addAll(Rockstar.getInstance().getModuleManager().getModules().stream().sorted(Comparator.comparing(Module::getName)).filter(module -> module.getCategory().equals((Object)category.getCategory())).map(module -> new ModernModule((Module)module, modern)).toList());
        }
        this.searchField = new TextField(Fonts.MEDIUM.getFont(6.0f));
        HashMap<String, FieldAction> append = new HashMap<String, FieldAction>();
        for (Module module2 : Rockstar.getInstance().getModuleManager().getModules()) {
            FieldAction action = new FieldAction(module2::toggle, () -> this.categories.forEach(panel -> panel.getModules().stream().filter(component -> component.getModule() == module2).forEach(modernModule -> System.out.println("poka pichego"))));
            append.put(module2.getName().replace(" ", ""), action);
            append.put(module2.getName(), action);
        }
        this.searchField.setAppend(append);
        this.searchField.setPreview("\u041f\u043e\u0438\u0441\u043a");
        this.searchPenis = new PenisPlayer(Rockstar.id("penises/search.penis"));
        this.searchPenis.stop();
    }

    @Compile
    protected void init() {
        this.closing = false;
        for (ModernCategory category : this.categories) {
            if (category.getPenis() == null) continue;
            category.getPenis().stop();
        }
        super.init();
    }

    public void tick() {
        this.handleMovementKeys();
        super.tick();
    }

    @Override
    @Compile
    public void render(UIContext context) {
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        this.menuAnimation.setEasing(!this.closing ? Easing.BAKEK : Easing.BAKEK_BACK);
        this.menuAnimation.setDuration(this.closing ? 160L : 220L);
        this.scrollHandler.update();
        if (this.drag) {
            this.menuWindow.setX((float)context.getMouseX() - this.dragX);
            this.menuWindow.setY((float)context.getMouseY() - this.dragY);
        }
        if (this.searchField.isFocused() && !this.prevFocused) {
            this.searchPenis.playOnce();
        }
        this.prevFocused = this.searchField.isFocused();
        // while searching, jump to the category that contains a match
        if (this.searchField != null && !this.searchField.getBuiltText().isBlank()
                && this.activeCategory().getModules().stream().noneMatch(m -> !this.searchCheck(m))) {
            for (ModernCategory c : this.categories) {
                if (c.getModules().stream().anyMatch(m -> !this.searchCheck(m))) {
                    this.current = c.getCategory();
                    break;
                }
            }
        }
        float scroll = (float)(-this.scrollHandler.getValue());
        float alpha = Math.min(1.0f, this.menuAnimation.getValue());
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        RenderUtility.scale(context.getMatrices(), this.menuWindow.getX() + this.menuWindow.getWidth() / 2.0f, this.menuWindow.getY() + this.menuWindow.getHeight() / 2.0f, 0.5f + 0.5f * this.menuAnimation.getValue());
        if (Interface.blurMenuEnabled()) {
            context.drawBlurredRect(this.menuWindow.getX(), this.menuWindow.getY(), this.menuWindow.getWidth(), this.menuWindow.getHeight(), 45.0f, 5.0f, BorderRadius.all(16.0f), Colors.WHITE);
        }
        // liquid glass synced with the rest of the HUD
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.menuWindow.getX(), this.menuWindow.getY(), this.menuWindow.getWidth(), this.menuWindow.getHeight(), 5.0f, 0.08f, BorderRadius.all(16.0f), Colors.WHITE.withAlpha(255.0f * alpha * Interface.glass()));
        }
        context.drawSquircle(this.menuWindow.getX(), this.menuWindow.getY(), this.menuWindow.getWidth(), this.menuWindow.getHeight(), 5.0f, BorderRadius.all(16.0f), (dark ? Colors.getAdditionalColor().mulAlpha(0.98f) : Colors.getBackgroundColor().mulAlpha(0.95f)).mulAlpha(1.0f - 0.4f * Interface.glass()));
        float x = this.menuWindow.getX();
        float y = this.menuWindow.getY();
        float w = this.menuWindow.getWidth();
        // window border + top-bar divider
        context.drawRoundedBorder(x, y, w, this.menuWindow.getHeight(), 1.0f, BorderRadius.all(16.0f), Colors.getAccentColor().mulAlpha(0.12f));
        context.drawRect(x + 1.0f, y + TOP_BAR, w - 2.0f, 1.0f, Colors.getTextColor().mulAlpha(0.06f));


        
        // brand logo (left)
        Glyphs.zLogo(context, x + 14.0f, y + (TOP_BAR - 14.0f) / 2.0f, 14.0f, Colors.getAccentColor());
        // search field (top-right)
        float fieldW = 108.0f;
        float fieldX = x + w - 14.0f - fieldW;
        float fieldY = y + (TOP_BAR - 15.0f) / 2.0f;
        context.drawRoundedRect(fieldX, fieldY, fieldW, 15.0f, BorderRadius.all(5.0f), dark ? Colors.getBackgroundColor().mulAlpha(0.6f) : Colors.getBackgroundColor().mulAlpha(0.5f));
        DrawUtility.drawAnimationSprite(context.getMatrices(), this.searchPenis.getCurrentSprite(), fieldX + 5.0f, fieldY + 3.5f, 8.0f, 8.0f, Colors.getTextColor().mulAlpha(0.5f));
        this.searchField.set(fieldX + 16.0f, fieldY, fieldW - 20.0f, 15.0f);
        this.searchField.setTextColor(Colors.getTextColor().mulAlpha(0.6f));
        this.searchField.render(context);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        // category tabs (centered)
        int n = this.categories.size();
        float tabsStart = x + w / 2.0f - (float)n * TAB_W / 2.0f;
        int curIdx = 0;
        for (int i = 0; i < n; ++i) {
            if (this.categories.get(i).getCategory() == this.current) {
                curIdx = i;
            }
        }
        this.currentCategory.setDuration(220L);
        this.currentCategory.setEasing(Easing.QUAD_OUT);
        this.currentCategory.update((float)curIdx * TAB_W);
        float iconY = y + (TOP_BAR - 11.0f) / 2.0f - 1.0f;
        IconBatching iconBatchingCat = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
        for (int i = 0; i < n; ++i) {
            ModernCategory cat = this.categories.get(i);
            cat.getSelected().update(cat.getCategory() == this.current);
            float tx = tabsStart + (float)i * TAB_W;
            if (GuiUtility.isHovered(tx, y, TAB_W, TOP_BAR, context)) {
                CursorUtility.set(CursorType.HAND);
            }
            if (cat.getPenis() == null) {
                context.drawSprite(cat.getCategory().getMenuSprite(), tx + (TAB_W - 11.0f) / 2.0f, iconY, 11.0f, 11.0f, Colors.getTextColor().mulAlpha(0.5f).mix(Colors.getAccentColor(), cat.getSelected().getValue()));
            }
        }
        iconBatchingCat.draw();
        IconBatching iconBatching = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
        for (int i = 0; i < n; ++i) {
            ModernCategory cat = this.categories.get(i);
            float tx = tabsStart + (float)i * TAB_W;
            if (cat.getPenis() != null) {
                DrawUtility.drawAnimationSprite(context.getMatrices(), cat.getPenis().getCurrentSprite(), tx + (TAB_W - 11.0f) / 2.0f, iconY, 11.0f, 11.0f, Colors.getTextColor().mulAlpha(0.5f).mix(Colors.getAccentColor(), cat.getSelected().getValue()));
            }
        }
        iconBatching.draw();
        float ulW = 16.0f;
        context.drawRoundedRect(tabsStart + this.currentCategory.getValue() + (TAB_W - ulW) / 2.0f, y + TOP_BAR - 3.0f, ulW, 2.0f, BorderRadius.all(1.0f), Colors.getAccentColor());

        // theme editor toggle button (top-bar, left of search) — opens a separate panel, not inline
        float teSize = 13.0f;
        float teX = fieldX - 10.0f - teSize;
        float teY = y + (TOP_BAR - teSize) / 2.0f;
        boolean teHover = GuiUtility.isHovered(teX, teY, teSize, teSize, context);
        if (teHover) {
            CursorUtility.set(CursorType.HAND);
        }
        Glyphs.diamond(context, teX, teY, teSize, (this.themeEditorOpen ? Colors.getAccentColor() : Colors.getTextColor().mulAlpha(0.55f)).mix(Colors.getAccentColor(), teHover ? 0.4f : 0.0f));

        // ── single active category, 3-column masonry, inline settings ──
        int columns = 3;
        float contentX = x + 12.0f;
        float contentW = w - 24.0f;
        float gap = 7.0f;
        float cardW = (contentW - (columns - 1) * gap) / columns;
        float top = y + TOP_BAR + 8.0f;
        float[] colY = new float[columns];
        for (int i = 0; i < columns; i++) {
            colY[i] = top + scroll;
        }
        ModernCategory active = this.activeCategory();
        ScissorUtility.push(context.getMatrices(), this.menuWindow.getX(), this.menuWindow.getY() + TOP_BAR + 1.0f, this.menuWindow.getWidth(), this.menuWindow.getHeight() - TOP_BAR - 2.0f);
        // 1) card backgrounds (+ compute layout)
        SquircleBatching squircleBatching = new SquircleBatching(5.0f);
        for (ModernModule modernModule : active.getModules()) {
            modernModule.getVisible().update(true);
            if (this.searchCheck(modernModule)) continue;
            int col = 0;
            for (int c = 1; c < columns; c++) {
                if (colY[c] < colY[col]) {
                    col = c;
                }
            }
            float cardX = contentX + (float) col * (cardW + gap);
            modernModule.set(cardX, colY[col], cardW, modernModule.fullHeight());
            modernModule.render(context);
            if (GuiUtility.isHovered(modernModule.getX(), modernModule.getY(), modernModule.getWidth(), ModernModule.HEADER, context)) {
                CursorUtility.set(CursorType.HAND);
            }
            colY[col] += modernModule.fullHeight() + gap;
        }
        squircleBatching.draw();
        RoundedRectBatching roundBatching = new RoundedRectBatching();
        for (ModernModule modernModule : active.getModules()) {
            if (this.searchCheck(modernModule)) continue;
            modernModule.renderRounds(context);
        }
        roundBatching.draw();
        RoundedRectBatching roundedRectBatching = new RoundedRectBatching();
        for (ModernModule module : active.getModules()) {
            if (this.searchCheck(module)) continue;
            module.renderInto(context);
        }
        roundedRectBatching.draw();
        FontBatching mediumBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (ModernModule modernModule : active.getModules()) {
            if (this.searchCheck(modernModule)) continue;
            modernModule.renderMedium(context);
        }
        mediumBatching.draw();
        // 2) inline settings — drawn with NO active batch (mixed primitives draw immediately)
        for (ModernModule modernModule : active.getModules()) {
            if (this.searchCheck(modernModule)) continue;
            modernModule.renderSettings(context);
        }
        float maxColY = colY[0];
        for (int c = 1; c < columns; c++) {
            maxColY = Math.max(maxColY, colY[c]);
        }
        float used = maxColY - scroll - top;
        float visibleHeight = this.menuWindow.getHeight() - TOP_BAR - 16.0f;
        float maxScroll = -Math.max(0.0f, used - visibleHeight);
        this.scrollHandler.setMax(maxScroll - 8.0f);
        ScissorUtility.pop();
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.renderThemeEditor(context, x, y, dark);
        for (ColorPicker colorPicker2 : this.colorPickers) {
            colorPicker2.render(context);
        }
        this.colorPickers.removeIf(colorPicker -> colorPicker.getAnimation().getValue() == 0.0f && !colorPicker.isShowing());
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.render(context);
            if (!(ModernScreen.mc.currentScreen instanceof ModernScreen) || !this.themeEditorOpen) {
                this.visualsModulesPopup.setShowing(false);
            }
            if (this.visualsModulesPopup.getAnimation().getValue() == 0.0f && !this.visualsModulesPopup.isShowing()) {
                this.visualsModulesPopup = null;
            }
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.render(context);
            if (!(ModernScreen.mc.currentScreen instanceof ModernScreen) || !this.themeEditorOpen) {
                this.blurElementsPopup.setShowing(false);
            }
            if (this.blurElementsPopup.getAnimation().getValue() == 0.0f && !this.blurElementsPopup.isShowing()) {
                this.blurElementsPopup = null;
            }
        }
    }

    private void renderThemeEditor(UIContext context, float menuX, float menuY, boolean dark) {
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
        this.themeAutoPicker = this.clearClosedPicker(this.themeAutoPicker);

        if (!this.themeEditorOpen) {
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
            return;
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

        float width = 134.0f;
        float rowHeight = 13.0f;
        float panelX = menuX + this.menuWindow.getWidth() + 10.0f;
        float panelW = width + 16.0f;
        float startY = menuY + 30.0f;
        float panelH = 18.0f * rowHeight + 26.0f;
        // separate side panel background
        context.drawSquircle(panelX, menuY, panelW, panelH, 5.0f, BorderRadius.all(12.0f), dark ? Colors.getAdditionalColor().mulAlpha(0.98f) : Colors.getBackgroundColor().mulAlpha(0.95f));
        context.drawRoundedBorder(panelX, menuY, panelW, panelH, 1.0f, BorderRadius.all(12.0f), Colors.getAccentColor().mulAlpha(0.12f));
        float x = panelX + 8.0f;

        context.drawText(Fonts.SEMIBOLD.getFont(7.5f), Localizator.translate("theme_editor.title"), x + 2.0f, menuY + 11.0f, Colors.getTextColor());

        // ── Auto theme: pick a color -> derive the whole theme ──
        if (this.themeAutoPicker != null) {
            Rockstar.getInstance().getThemeManager().applyAutoTheme(this.themeAutoPicker.built());
        }
        float autoW = 44.0f;
        float autoH = 12.0f;
        float autoX = panelX + panelW - autoW - 6.0f;
        float autoY = menuY + 5.0f;
        boolean autoHover = GuiUtility.isHovered(autoX, autoY, autoW, autoH, context);
        if (autoHover) {
            CursorUtility.set(CursorType.HAND);
        }
        context.drawRoundedRect(autoX, autoY, autoW, autoH, BorderRadius.all(4.0f), Colors.getAccentColor().mulAlpha(autoHover ? 0.95f : 0.65f));
        context.drawCenteredText(Fonts.MEDIUM.getFont(6.5f), Localizator.translate("theme_editor.auto"), autoX + autoW / 2.0f, autoY + (autoH - Fonts.MEDIUM.getFont(6.5f).height()) / 2.0f, Colors.WHITE);

        this.renderThemeEditorRow(context, x, startY + 0.0f * rowHeight, width, rowHeight, "theme_editor.accent", Rockstar.getInstance().getThemeManager().getCustomAccentColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 1.0f * rowHeight, width, rowHeight, "theme_editor.background", Rockstar.getInstance().getThemeManager().getCustomBackgroundColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 2.0f * rowHeight, width, rowHeight, "theme_editor.additional", Rockstar.getInstance().getThemeManager().getCustomAdditionalColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 3.0f * rowHeight, width, rowHeight, "theme_editor.text", Rockstar.getInstance().getThemeManager().getCustomTextColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 4.0f * rowHeight, width, rowHeight, "theme_editor.outline", Rockstar.getInstance().getThemeManager().getCustomOutlineColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 5.0f * rowHeight, width, rowHeight, "theme_editor.flat", Rockstar.getInstance().getThemeManager().getCustomFlatColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 6.0f * rowHeight, width, rowHeight, "theme_editor.gui_text_active", Rockstar.getInstance().getThemeManager().getCustomGuiTextActiveColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 7.0f * rowHeight, width, rowHeight, "theme_editor.gui_text_inactive", Rockstar.getInstance().getThemeManager().getCustomGuiTextInactiveColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 8.0f * rowHeight, width, rowHeight, "theme_editor.header_text", Rockstar.getInstance().getThemeManager().getCustomHeaderTextColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 9.0f * rowHeight, width, rowHeight, "theme_editor.logo_background", Rockstar.getInstance().getThemeManager().getCustomLogoBackgroundColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 10.0f * rowHeight, width, rowHeight, "theme_editor.logo_text", Rockstar.getInstance().getThemeManager().getCustomLogoTextColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 11.0f * rowHeight, width, rowHeight, "theme_editor.visuals", Rockstar.getInstance().getThemeManager().getCustomTargetESPColor(), dark);
        this.renderThemeEditorModulesRow(context, x, startY + 12.0f * rowHeight, width, rowHeight, dark);
        this.renderThemeEditorBlurElementsRow(context, x, startY + 13.0f * rowHeight, width, rowHeight, dark);
        this.renderThemeEditorRow(context, x, startY + 14.0f * rowHeight, width, rowHeight, "theme_editor.slider_track", Rockstar.getInstance().getThemeManager().getCustomSliderTrackColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 15.0f * rowHeight, width, rowHeight, "theme_editor.slider_circle", Rockstar.getInstance().getThemeManager().getCustomSliderCircleColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 16.0f * rowHeight, width, rowHeight, "theme_editor.slider_window", Rockstar.getInstance().getThemeManager().getCustomSliderWindowColor(), dark);
        this.renderThemeEditorRow(context, x, startY + 17.0f * rowHeight, width, rowHeight, "theme_editor.tooltip_text", Rockstar.getInstance().getThemeManager().getCustomTooltipTextColor(), dark);
    }

    private void renderThemeEditorModulesRow(UIContext context, float x, float y, float width, float height, boolean dark) {
        boolean hovered = GuiUtility.isHovered(x, y, width, height, context);
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float rightPadding = 4.0f;
        float arrowSize = 7.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(3.0f), (dark ? Colors.getAdditionalColor().mulAlpha(0.55f) : Colors.getBackgroundColor().mulAlpha(0.55f)).mix(Colors.WHITE, hovered ? 0.06f : 0.0f));
        context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate("theme_editor.visuals_modules"), x + 4.0f, y + 4.0f, Colors.getTextColor().mulAlpha(0.8f));
        float arrowX = x + width - rightPadding - arrowSize;
        float arrowY = y + (height - arrowSize) / 2.0f;
        float rotation = this.visualsModulesPopup != null && this.visualsModulesPopup.isShowing() ? 90.0f : 0.0f;
        RenderUtility.rotate(context.getMatrices(), arrowX + arrowSize / 2.0f, arrowY + arrowSize / 2.0f, rotation);
        context.drawTexture(Rockstar.id("icons/arrow.png"), arrowX, arrowY, arrowSize, arrowSize, Colors.getTextColor().mulAlpha(0.8f));
        RenderUtility.end(context.getMatrices());
    }

    private void renderThemeEditorBlurElementsRow(UIContext context, float x, float y, float width, float height, boolean dark) {
        boolean hovered = GuiUtility.isHovered(x, y, width, height, context);
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float rightPadding = 4.0f;
        float arrowSize = 7.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(3.0f), (dark ? Colors.getAdditionalColor().mulAlpha(0.55f) : Colors.getBackgroundColor().mulAlpha(0.55f)).mix(Colors.WHITE, hovered ? 0.06f : 0.0f));
        context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate("theme_editor.blur_elements"), x + 4.0f, y + 4.0f, Colors.getTextColor().mulAlpha(0.8f));
        float arrowX = x + width - rightPadding - arrowSize;
        float arrowY = y + (height - arrowSize) / 2.0f;
        float rotation = this.blurElementsPopup != null && this.blurElementsPopup.isShowing() ? 90.0f : 0.0f;
        RenderUtility.rotate(context.getMatrices(), arrowX + arrowSize / 2.0f, arrowY + arrowSize / 2.0f, rotation);
        context.drawTexture(Rockstar.id("icons/arrow.png"), arrowX, arrowY, arrowSize, arrowSize, Colors.getTextColor().mulAlpha(0.8f));
        RenderUtility.end(context.getMatrices());
    }

    private void renderThemeEditorRow(UIContext context, float x, float y, float width, float height, String nameKey, moscow.rockstar.utility.colors.ColorRGBA color, boolean dark) {
        boolean hovered = GuiUtility.isHovered(x, y, width, height, context);
        if (hovered) {
            CursorUtility.set(CursorType.HAND);
        }
        float previewSize = 7.0f;
        float rightPadding = 4.0f;
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(3.0f), (dark ? Colors.getAdditionalColor().mulAlpha(0.55f) : Colors.getBackgroundColor().mulAlpha(0.55f)).mix(Colors.WHITE, hovered ? 0.06f : 0.0f));
        context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate(nameKey), x + 4.0f, y + 4.0f, Colors.getTextColor().mulAlpha(0.8f));
        context.drawRoundedRect(x + width - rightPadding - previewSize - 2.0f, y + (height - previewSize) / 2.0f, previewSize + 2.0f, previewSize, BorderRadius.all(3.0f), Colors.getOutlineColor().mulAlpha(0.8f));
        context.drawRoundedRect(x + width - rightPadding - previewSize - 1.0f, y + (height - previewSize) / 2.0f + 1.0f, previewSize, previewSize - 2.0f, BorderRadius.all(3.0f), color);
    }

    private ColorPicker clearClosedPicker(ColorPicker picker) {
        if (picker == null) {
            return null;
        }
        return picker.getAnimation().getValue() == 0.0f && !picker.isShowing() ? null : picker;
    }

    @Compile
    private void handleMovementKeys() {
        KeyBinding[] movementKeys;
        if (ModernScreen.mc.player == null || this.isTyping()) {
            return;
        }
        long windowHandle = mc.getWindow().getHandle();
        for (KeyBinding key : movementKeys = new KeyBinding[]{ModernScreen.mc.options.forwardKey, ModernScreen.mc.options.backKey, ModernScreen.mc.options.leftKey, ModernScreen.mc.options.rightKey, ModernScreen.mc.options.jumpKey}) {
            int keyCode = InputUtil.fromTranslationKey((String)key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
        if (ModernScreen.mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey((String)ModernScreen.mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            ModernScreen.mc.options.sneakKey.setPressed(InputUtil.isKeyPressed((long)windowHandle, (int)keyCode));
        }
    }

    private boolean isTyping() {
        return ModernScreen.mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
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
        float x = this.menuWindow.getX();
        float y = this.menuWindow.getY();
        float w = this.menuWindow.getWidth();
        // theme editor toggle button
        float fieldW = 108.0f;
        float fieldX = x + w - 14.0f - fieldW;
        float teSize = 13.0f;
        float teX = fieldX - 10.0f - teSize;
        float teY = y + (TOP_BAR - teSize) / 2.0f;
        if (button == MouseButton.LEFT && GuiUtility.isHovered((double) teX, (double) teY, (double) teSize, (double) teSize, mouseX, mouseY)) {
            this.themeEditorOpen = !this.themeEditorOpen;
            if (this.themeEditorOpen) {
                // opening the editor implies you want the custom theme active, otherwise edits don't apply
                Rockstar.getInstance().getModuleManager().getModule(Interface.class).getCustom().select();
            }
            return;
        }
        // category tabs
        int n = this.categories.size();
        float tabsStart = x + w / 2.0f - (float)n * TAB_W / 2.0f;
        for (int i = 0; i < n; ++i) {
            ModernCategory category = this.categories.get(i);
            float tx = tabsStart + (float)i * TAB_W;
            if (GuiUtility.isHovered((double)tx, (double)y, (double)TAB_W, (double)TOP_BAR, mouseX, mouseY) && category.getCategory() != this.current) {
                this.current = category.getCategory();
                this.scrollHandler.setValue(0.0);
                this.scrollHandler.scroll(0.0);
                if (category.getPenis() != null) {
                    category.getPenis().playOnce();
                }
                return;
            }
        }
        // search field lives in the top bar — handle it first so a scrolled-up card can't steal the click
        if (mouseY <= y + TOP_BAR) {
            if (button != MouseButton.MIDDLE) {
                this.searchField.onMouseClicked(mouseX, mouseY, button);
            }
            // drag the window only by the empty part of the top bar (not the search / theme button)
            if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY) && mouseX < teX) {
                this.drag = true;
                this.dragX = (float)(mouseX - (double)this.menuWindow.getX());
                this.dragY = (float)(mouseY - (double)this.menuWindow.getY());
            }
            super.onMouseClicked(mouseX, mouseY, button);
            return;
        }
        // module cards in the active category (header click + inline settings) — content area only
        for (ModernModule module : this.activeCategory().getModules()) {
            if (this.searchCheck(module)) continue;
            boolean overHeader = GuiUtility.isHovered((double)module.getX(), (double)module.getY(), (double)module.getWidth(), (double)ModernModule.HEADER, mouseX, mouseY);
            boolean overCard = GuiUtility.isHovered((double)module.getX(), (double)module.getY(), (double)module.getWidth(), (double)module.getHeight(), mouseX, mouseY);
            if (!overHeader && !(module.isExpanded() && overCard)) continue;
            module.onMouseClicked(mouseX, mouseY, button);
            return;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    private boolean handleThemeEditorClick(double mouseX, double mouseY, MouseButton button) {
        if (!this.themeEditorOpen || button != MouseButton.LEFT) {
            return false;
        }
        // Auto-theme button
        float panelX = this.menuWindow.getX() + this.menuWindow.getWidth() + 10.0f;
        float panelW = 134.0f + 16.0f;
        float autoW = 44.0f;
        float autoX = panelX + panelW - autoW - 6.0f;
        float autoY = this.menuWindow.getY() + 5.0f;
        if (GuiUtility.isHovered((double) autoX, (double) autoY, (double) autoW, 12.0, mouseX, mouseY)) {
            Rockstar.getInstance().getModuleManager().getModule(Interface.class).getCustom().select();
            this.themeAutoPicker = new ColorPicker((float) mouseX, (float) mouseY, 6.0f, false, Rockstar.getInstance().getThemeManager().getCustomAccentColor(), Localizator.translate("theme_editor.auto"));
            this.colorPickers.add(this.themeAutoPicker);
            return true;
        }
        float x = this.menuWindow.getX() + this.menuWindow.getWidth() + 10.0f + 8.0f;
        float width = 134.0f;
        float height = 13.0f;
        float startY = this.menuWindow.getY() + 30.0f;
        if (GuiUtility.isHovered((double)x, (double)(startY + 0.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeAccentPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomAccentColor(), Localizator.translate("theme_editor.accent"));
            this.colorPickers.add(this.themeAccentPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 1.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeBackgroundPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomBackgroundColor(), Localizator.translate("theme_editor.background"));
            this.colorPickers.add(this.themeBackgroundPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 2.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeAdditionalPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomAdditionalColor(), Localizator.translate("theme_editor.additional"));
            this.colorPickers.add(this.themeAdditionalPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 3.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomTextColor(), Localizator.translate("theme_editor.text"));
            this.colorPickers.add(this.themeTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 4.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeOutlinePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomOutlineColor(), Localizator.translate("theme_editor.outline"));
            this.colorPickers.add(this.themeOutlinePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 5.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeFlatPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomFlatColor(), Localizator.translate("theme_editor.flat"));
            this.colorPickers.add(this.themeFlatPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 6.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeGuiTextActivePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomGuiTextActiveColor(), Localizator.translate("theme_editor.gui_text_active"));
            this.colorPickers.add(this.themeGuiTextActivePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 7.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeGuiTextInactivePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomGuiTextInactiveColor(), Localizator.translate("theme_editor.gui_text_inactive"));
            this.colorPickers.add(this.themeGuiTextInactivePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 8.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeHeaderTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomHeaderTextColor(), Localizator.translate("theme_editor.header_text"));
            this.colorPickers.add(this.themeHeaderTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 9.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeLogoBackgroundPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomLogoBackgroundColor(), Localizator.translate("theme_editor.logo_background"));
            this.colorPickers.add(this.themeLogoBackgroundPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 10.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeLogoTextPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomLogoTextColor(), Localizator.translate("theme_editor.logo_text"));
            this.colorPickers.add(this.themeLogoTextPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 11.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeTargetESPPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomTargetESPColor(), Localizator.translate("theme_editor.visuals"));
            this.colorPickers.add(this.themeTargetESPPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 12.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.openVisualsModulesPopup((float)mouseX, (float)mouseY);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 13.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.openBlurElementsPopup((float)mouseX, (float)mouseY);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 14.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeSliderTrackPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderTrackColor(), Localizator.translate("theme_editor.slider_track"));
            this.colorPickers.add(this.themeSliderTrackPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 15.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeSliderCirclePicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderCircleColor(), Localizator.translate("theme_editor.slider_circle"));
            this.colorPickers.add(this.themeSliderCirclePicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 16.0f * height), (double)width, (double)height, mouseX, mouseY)) {
            this.themeSliderWindowPicker = new ColorPicker((float)mouseX, (float)mouseY, 6.0f, true, Rockstar.getInstance().getThemeManager().getCustomSliderWindowColor(), Localizator.translate("theme_editor.slider_window"));
            this.colorPickers.add(this.themeSliderWindowPicker);
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)(startY + 17.0f * height), (double)width, (double)height, mouseX, mouseY)) {
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
        moscow.rockstar.systems.modules.modules.visuals.World world = Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.World.class);
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

    @Override
    @Compile
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onMouseReleased(mouseX, mouseY, button);
        }
        for (ModernModule module : this.activeCategory().getModules()) {
            if (this.searchCheck(module)) continue;
            module.onMouseReleased(mouseX, mouseY, button);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused()) {
            this.searchField.onMouseReleased(mouseX, mouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Compile
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.visualsModulesPopup != null) {
            this.visualsModulesPopup.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (this.blurElementsPopup != null) {
            this.blurElementsPopup.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY)) {
            this.scrollHandler.scroll(verticalAmount);
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
        if (!this.searchField.isFocused() && Screen.hasControlDown() && keyCode == 70) {
            this.searchField.setFocused(true);
        }
        this.scrollHandler.onKeyPressed(keyCode);
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchField.isFocused() && !this.isBindingModule()) {
            this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
        }
        for (ModernModule module : this.activeCategory().getModules()) {
            if (this.searchCheck(module)) continue;
            module.onKeyPressed(keyCode, scanCode, modifiers);
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
        for (ModernModule module : this.activeCategory().getModules()) {
            if (this.searchCheck(module)) continue;
            module.charTyped(chr, modifiers);
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

    private boolean searchCheck(ModernModule component) {
        TextField search = this.searchField;
        return search != null && !search.getBuiltText().isBlank() && !component.getModule().getName().toLowerCase().contains(search.getBuiltText().toLowerCase()) && !component.getModule().getName().replace(" ", "").toLowerCase().contains(search.getBuiltText().toLowerCase());
    }

    private ModernCategory activeCategory() {
        for (ModernCategory c : this.categories) {
            if (c.getCategory() == this.current) {
                return c;
            }
        }
        return this.categories.get(0);
    }

    public boolean isBindingModule() {
        return this.categories.stream().flatMap(panel -> panel.getModules().stream()).anyMatch(ModernModule::isBinding);
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
    public Rect getMenuWindow() {
        return this.menuWindow;
    }

    @Generated
    public float getDragX() {
        return this.dragX;
    }

    @Generated
    public float getDragY() {
        return this.dragY;
    }

    @Generated
    public boolean isDrag() {
        return this.drag;
    }

    @Generated
    public ScrollHandler getScrollHandler() {
        return this.scrollHandler;
    }

    @Generated
    public MenuCategory getCurrent() {
        return this.current;
    }

    @Generated
    public List<ColorPicker> getColorPickers() {
        return this.colorPickers;
    }

    @Generated
    public List<ModernCategory> getCategories() {
        return this.categories;
    }

    @Generated
    public Animation getCurrentCategory() {
        return this.currentCategory;
    }

    @Generated
    public TextField getSearchField() {
        return this.searchField;
    }

    @Generated
    public PenisPlayer getSearchPenis() {
        return this.searchPenis;
    }

    @Generated
    public boolean isPrevFocused() {
        return this.prevFocused;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
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

