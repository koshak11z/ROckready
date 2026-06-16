package im.zov4ik.display.screens.clickgui;

import im.zov4ik.common.animation.Easy.Direction;
import im.zov4ik.common.animation.Easy.EaseBackIn;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.other.CategoryContainerComponent;
import im.zov4ik.display.screens.clickgui.components.implement.other.SearchComponent;
import im.zov4ik.display.screens.clickgui.components.implement.settings.TextComponent;
import im.zov4ik.display.hud.HudTheme;
import im.zov4ik.features.impl.misc.SelfDestruct;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.client.sound.SoundManager;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static im.zov4ik.common.animation.Easy.Direction.BACKWARDS;
import static im.zov4ik.common.animation.Easy.Direction.FORWARDS;

@Setter
@Getter
public class MenuScreen extends Screen implements QuickImports {
    public static MenuScreen INSTANCE = new MenuScreen();
    private final List<AbstractComponent> components = new ArrayList<>();
    private final CategoryContainerComponent categoryContainerComponent = new CategoryContainerComponent();
    private final SearchComponent searchComponent = new SearchComponent();
    public final EaseBackIn animation = new EaseBackIn(325, 1f, 1.5f);
    public ModuleCategory category = ModuleCategory.COMBAT;
    public int x, y, width, height;

    private static final float TOP_BAR_H = 26.0F;
    private static final float TAB_W = 22.0F;
    private static final ModuleCategory[] CATEGORIES = {
            ModuleCategory.COMBAT, ModuleCategory.MOVEMENT,
            ModuleCategory.RENDER, ModuleCategory.PLAYER, ModuleCategory.MISC
    };

    private float tabIndicatorX = -1.0F;

    public MenuScreen() {
        super(Text.of("MenuScreen"));
        initialize();
    }

    public void initialize() {
        animation.setDirection(FORWARDS);
        categoryContainerComponent.initializeCategoryComponents(category);
        components.clear();
        components.add(categoryContainerComponent);
        components.add(searchComponent);
    }

    @Override
    public void tick() {
        close();
        components.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        width = Math.min(window.getScaledWidth() - 170, 628);
        height = Math.min(window.getScaledHeight() - 108, 372);
        width = Math.max(width, 480);
        height = Math.max(height, 320);
        x = window.getScaledWidth() / 2 - width / 2;
        y = window.getScaledHeight() / 2 - height / 2;

        blur.setup();
        rectangle.render(ShapeProperties.create(context.getMatrices(), 0, 0, window.getScaledWidth(), window.getScaledHeight())
                .color(Calculate.applyOpacity(0xFF05070B, 18 * getScaleAnimation()))
                .build());

        float contentY = y + TOP_BAR_H + 4.0F;
        categoryContainerComponent.position(x, contentY).size(width, height - TOP_BAR_H - 6.0F);
        searchComponent.position(x + width - 88.0F, y + 6.0F).size(66, 13);

        Calculate.scale(context.getMatrices(), x + (float) width / 2, y + (float) height / 2, getScaleAnimation(), () -> {
            drawTopBar(context, mouseX, mouseY);
            components.forEach(component -> component.render(context, mouseX, mouseY, delta));
            windowManager.render(context, mouseX, mouseY, delta);
        });
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTopBar(DrawContext context, int mouseX, int mouseY) {
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, TOP_BAR_H)
                .round(ClickGuiTheme.PANEL_RADIUS)
                .thickness(0.75F)
                .outlineColor(ClickGuiTheme.PANEL_OUTLINE)
                .color(ClickGuiTheme.PANEL_HEADER_BG, ClickGuiTheme.PANEL_HEADER_BG,
                        ClickGuiTheme.PANEL_HEADER_BG_DARK, ClickGuiTheme.PANEL_HEADER_BG_DARK)
                .build());

        int accent = ClickGuiTheme.accent();
        HudTheme.icon(context, HudTheme.ICON_DIAMOND, x + 12.0F, y + 9.3F, 7.0F, accent);

        float tabsW = CATEGORIES.length * TAB_W;
        float tabsX = x + width / 2.0F - tabsW / 2.0F;
        float selectedCenter = tabsX + TAB_W / 2.0F;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i] == category) {
                selectedCenter = tabsX + i * TAB_W + TAB_W / 2.0F;
                break;
            }
        }
        if (tabIndicatorX < 0.0F) tabIndicatorX = selectedCenter;
        tabIndicatorX += (selectedCenter - tabIndicatorX) * 0.22F;

        for (int i = 0; i < CATEGORIES.length; i++) {
            ModuleCategory cat = CATEGORIES[i];
            float tabX = tabsX + i * TAB_W;
            boolean selected = cat == category;
            boolean hovered = Calculate.isHovered(mouseX, mouseY, tabX, y, TAB_W, TOP_BAR_H);
            int color = selected ? accent : hovered ? 0xFFE5E8F0 : 0xFF828897;
            HudTheme.icon(context, categoryTexture(cat), tabX + TAB_W / 2.0F - 3.0F, y + 9.4F, 6.0F, color);
        }

        rectangle.render(ShapeProperties.create(context.getMatrices(), tabIndicatorX - 4.5F, y + TOP_BAR_H - 2.0F, 9.0F, 1.1F)
                .round(0.8F)
                .color(accent)
                .build());
    }

    private Identifier categoryTexture(ModuleCategory category) {
        return switch (category) {
            case COMBAT -> HudTheme.ICON_SWORD;
            case MOVEMENT -> HudTheme.ICON_ACTIVITY;
            case RENDER -> HudTheme.ICON_EYE;
            case PLAYER -> HudTheme.ICON_USER;
            case MISC -> HudTheme.ICON_SETTINGS;
            case CONFIGS -> HudTheme.ICON_PACKAGE;
            case AUTOBUY -> HudTheme.ICON_ZAP;
        };
    }

    public void openGui() {
        if (SelfDestruct.unhooked) return;
        animation.setDirection(Direction.FORWARDS);
        animation.reset();
        mc.setScreen(this);
        SoundManager.playSound(SoundManager.OPEN_GUI);
    }

    public float getScaleAnimation() {
        return (float) animation.getOutput();
    }

    public String getSearchText() {
        return searchComponent.getText();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float tabsW = CATEGORIES.length * TAB_W;
            float tabsX = x + width / 2.0F - tabsW / 2.0F;
            for (int i = 0; i < CATEGORIES.length; i++) {
                float tabX = tabsX + i * TAB_W;
                if (Calculate.isHovered(mouseX, mouseY, tabX, y, TAB_W, TOP_BAR_H)) {
                    category = CATEGORIES[i];
                    categoryContainerComponent.initializeCategoryComponents(category);
                    SoundManager.playSound(SoundManager.CATEGORY_CLICK);
                    return true;
                }
            }
        }

        boolean windowHandled = windowManager.mouseClicked(mouseX, mouseY, button);
        if (!windowHandled) {
            for (AbstractComponent component : components) {
                component.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractComponent component : components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
        windowManager.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean windowHandled = windowManager.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (!windowHandled) {
            for (AbstractComponent component : components) {
                component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        boolean windowHandled = windowManager.mouseScrolled(mouseX, mouseY, vertical);
        if (!windowHandled) {
            for (AbstractComponent component : components) {
                component.mouseScrolled(mouseX, mouseY, vertical);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && shouldCloseOnEsc()) {
            SoundManager.playSound(SoundManager.CLOSE_GUI);
            animation.setDirection(BACKWARDS);
            return true;
        }
        if (!windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            for (AbstractComponent component : components) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!windowManager.charTyped(chr, modifiers)) {
            for (AbstractComponent component : components) {
                component.charTyped(chr, modifiers);
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (animation.finished(BACKWARDS)) {
            TextComponent.typing = false;
            super.close();
        }
    }
}
