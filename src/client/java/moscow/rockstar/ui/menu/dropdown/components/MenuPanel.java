/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.menu.dropdown.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.sounds.ClientSounds;

public class MenuPanel
extends CustomComponent
implements IScaledResolution {
    private final MenuCategory category;
    private final Animation swapping = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation sizing = new Animation(500L, Easing.BAKEK_SMALLER);
    private final List<ModuleComponent> moduleComponents = new ArrayList<ModuleComponent>();
    private final ScrollHandler modulesScroll = new ScrollHandler();
    private final ScrollHandler settingsScroll = new ScrollHandler();
    private Font titleFont;
    private ModuleComponent lastSelected;
    private ModuleComponent selectedModuleComponent;

    public MenuPanel(MenuCategory category) {
        this.category = category;
    }

    @Override
    public void onInit() {
        List<Module> filteredModules = Rockstar.getInstance().getModuleManager().getModules().stream().sorted(Comparator.comparing(Module::getName)).filter(module -> module.getCategory().equals((Object)this.category.getCategory())).toList();
        for (Module module2 : filteredModules) {
            ModuleComponent component = new ModuleComponent(module2, this);
            component.setWidth(this.width);
            component.setHeight(20.0f);
            this.moduleComponents.add(component);
            component.onInit();
        }
        this.titleFont = Fonts.SEMIBOLD.getFont(9.0f);
        this.modulesScroll.reset();
        this.settingsScroll.reset();
        super.onInit();
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
    }

    private void updateScale(UIContext context) {
        this.sizing.setEasing(Easing.BAKEK);
        this.sizing.setDuration(400L);
        this.sizing.update(Rockstar.getInstance().getMenuScreen().getMenuAnimation().getValue());
    }

    public void scale(UIContext context) {
        float scale = 0.5f + 0.5f * this.sizing.getValue();
        if (Interface.glassSelected()) {
            RenderUtility.scale(context.getMatrices(), sr.getScaledWidth() / 2.0f, this.y + this.height / 2.0f, scale);
        } else {
            RenderUtility.scale(context.getMatrices(), this.x + this.width / 2.0f, this.y + this.height / 2.0f, scale);
        }
    }

    public void renderBackground(UIContext context) {
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        this.scale(context);
        context.drawRoundedRect(this.x + 1.0f, this.y + 1.0f, this.width - 2.0f, this.height - 2.0f, BorderRadius.all(10.0f), Colors.getBackgroundColor().mulAlpha(Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK ? 0.55f : 0.7f));
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void renderShadow(UIContext context) {
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        this.scale(context);
        context.drawShadow(this.x, this.y, this.width, this.height, 25.0f, BorderRadius.all(10.0f), ColorRGBA.BLACK.withAlpha(51.0f));
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void renderBlur(UIContext context) {
        float alpha = this.sizing.getValue();
        this.scale(context);
        if (Interface.showMinimalizm() && Interface.blurPanelsEnabled()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 10.0f, BorderRadius.all(10.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, this.y, this.width, this.height, 10.0f, 0.08f, BorderRadius.all(10.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        }
        RenderUtility.end(context.getMatrices());
    }

    public void push(UIContext context) {
        float headerHeight = 24.0f;
        float separatorHeight = 4.0f;
        float offset = Interface.glass() * 2.0f;
        if (this.selectedModuleComponent != null) {
            ScissorUtility.push(context.getMatrices(), this.x + offset, this.y + headerHeight * 2.0f + separatorHeight + offset, this.width - offset * 2.0f, this.height - headerHeight * 2.0f - separatorHeight - 0.5f - offset * 2.0f);
        } else {
            ScissorUtility.push(context.getMatrices(), this.x + offset, this.y + headerHeight + separatorHeight + offset, this.width - offset * 2.0f, this.height - headerHeight - separatorHeight - 0.5f - offset * 2.0f);
        }
    }

    @Override
    protected void renderComponent(UIContext context) {
        float x;
        this.modulesScroll.update();
        this.settingsScroll.update();
        float headerHeight = 24.0f;
        this.updateScale(context);
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        this.scale(context);
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        context.drawSquircle(this.x, this.y, this.width, this.height, 10.0f, BorderRadius.all(10.0f), Colors.getBackgroundColor().mulAlpha(dark ? 0.9f - 0.7f * Interface.glass() : 0.7f));
        float separatorHeight = 4.0f;
        float titleLeftPadding = 10.0f;
        float titleHeight = this.titleFont.height();
        context.drawText(this.titleFont, this.category.getName(), this.x + titleLeftPadding, this.y + GuiUtility.getMiddleOfBox(titleHeight, headerHeight) + 0.5f, Colors.getTextColor());
        if (Interface.showMinimalizm()) {
            context.drawRect(this.x, this.y + headerHeight, this.width, separatorHeight, Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm()));
        }
        if (this.selectedModuleComponent != null) {
            this.lastSelected = this.selectedModuleComponent;
        }
        this.swapping.update(this.selectedModuleComponent != null ? 1.0f : 0.0f);
        if (this.swapping.getValue() != 1.0f) {
            x = this.x + -this.width * this.swapping.getValue();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(alpha * (1.0f - this.swapping.getValue())));
            ScissorUtility.push(context.getMatrices(), this.x, this.y + headerHeight + separatorHeight, this.width, this.height - headerHeight - separatorHeight - 0.5f);
            float offset = 0.0f;
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                if (this.searchCheck(moduleComponent)) continue;
                moduleComponent.setX(x);
                moduleComponent.setY((float)((double)(this.y + offset) - this.modulesScroll.getValue()) + headerHeight + separatorHeight - 1.0f);
                moduleComponent.render(context);
                this.modulesScroll.setMax(-(offset += moduleComponent.getHeight()) + this.height - headerHeight - separatorHeight);
            }
            ScissorUtility.pop();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        }
        if (this.swapping.getValue() != 0.0f) {
            x = this.x + this.width * (1.0f - this.swapping.getValue());
            float y = this.y + headerHeight + separatorHeight;
            float leftPadding = 6.0f;
            float arrowIconSize = 8.0f;
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(alpha * this.swapping.getValue()));
            ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
            if (GuiUtility.isHovered((double)x, (double)(this.y + 28.0f), (double)this.width, 20.0, context.getMouseX(), context.getMouseY())) {
                CursorUtility.set(CursorType.HAND);
            }
            context.drawTexture(Rockstar.id("icons/arrow.png"), x + leftPadding, y + GuiUtility.getMiddleOfBox(arrowIconSize, headerHeight) - 2.0f, arrowIconSize, arrowIconSize, Colors.getTextColor());
            context.drawText(Fonts.REGULAR.getFont(8.0f), this.lastSelected.getModule().getName(), x + arrowIconSize + 8.0f, y + GuiUtility.getMiddleOfBox(arrowIconSize, headerHeight) - 1.0f, Colors.getTextColor().withAlpha(255.0f));
            if (Interface.showMinimalizm()) {
                context.drawRect(x, y + headerHeight - separatorHeight, this.width, separatorHeight, Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm()));
            }
            ScissorUtility.pop();
            ScissorUtility.push(context.getMatrices(), this.x, y + headerHeight, this.width, this.height - headerHeight * 2.0f - separatorHeight - 0.5f - Interface.glass() * 5.0f);
            float settingsY = y + headerHeight;
            float offset = 0.0f;
            for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(settingComponent.getOpacity() * this.swapping.getValue()));
                settingComponent.getVisibilityAnimation().update(settingComponent.getSetting().isVisible() ? 1.0f : 0.0f);
                settingComponent.setX(x);
                settingComponent.setY((float)((double)(settingsY + offset) - this.settingsScroll.getValue()));
                settingComponent.setWidth(this.width);
                context.pushMatrix();
                context.getMatrices().translate(0.0f, (-settingComponent.getHeight() + settingComponent.getHeight() * settingComponent.getOpacity()) / 2.0f, 0.0f);
                settingComponent.render(context);
                context.popMatrix();
                offset += settingComponent.getHeight() * settingComponent.getOpacity();
            }
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
            this.settingsScroll.setMax(-offset + this.height - headerHeight * 2.0f - separatorHeight - (float)(Interface.glassSelected() ? 5 : 0));
            ScissorUtility.pop();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        }
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void drawRegular8(UIContext context) {
        float x;
        float headerHeight = 24.0f;
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        float separatorHeight = 4.0f;
        if (this.selectedModuleComponent != null) {
            this.lastSelected = this.selectedModuleComponent;
        }
        if (this.swapping.getValue() != 1.0f) {
            x = this.x + -this.width * this.swapping.getValue();
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                if (this.searchCheck(moduleComponent) || !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY()) && !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY() + moduleComponent.getHeight())) continue;
                moduleComponent.drawRegular8(context);
            }
        }
        if (this.swapping.getValue() != 0.0f) {
            x = this.x + this.width * (1.0f - this.swapping.getValue());
            float y = this.y + headerHeight + separatorHeight;
            for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
                if (!settingComponent.getSetting().isVisible() || !GuiUtility.isHovered((double)x, (double)y, (double)this.width, (double)this.height, settingComponent.getX(), settingComponent.getY()) && !GuiUtility.isHovered((double)x, (double)y, (double)this.width, (double)this.height, settingComponent.getX(), settingComponent.getY() + settingComponent.getHeight())) continue;
                settingComponent.drawRegular8(context);
            }
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void drawIcons(UIContext context) {
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        if (this.selectedModuleComponent != null) {
            this.lastSelected = this.selectedModuleComponent;
        }
        if (this.swapping.getValue() != 1.0f) {
            float x = this.x + -this.width * this.swapping.getValue();
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                if (this.searchCheck(moduleComponent) || !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY()) && !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY() + moduleComponent.getHeight())) continue;
                moduleComponent.drawIcons(context);
            }
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void drawType(UIContext context) {
        float headerHeight = 24.0f;
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        this.scale(context);
        float iconScale = 8.0f;
        float rightPadding = 10.0f;
        context.drawSprite(this.category.getMenuSprite(), this.x + this.width - rightPadding - iconScale, this.y + GuiUtility.getMiddleOfBox(iconScale, headerHeight) + 0.5f, iconScale, iconScale, Colors.getTextColor().withAlpha(255.0f * alpha));
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void drawSplit(UIContext context) {
        float x;
        float headerHeight = 24.0f;
        float alpha = this.sizing.getValue();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alpha);
        float separatorHeight = 4.0f;
        if (this.selectedModuleComponent != null) {
            this.lastSelected = this.selectedModuleComponent;
        }
        if (this.swapping.getValue() != 1.0f) {
            x = this.x + -this.width * this.swapping.getValue();
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                if (this.searchCheck(moduleComponent) || !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY()) && !GuiUtility.isHovered((double)x, (double)this.y, (double)this.width, (double)this.height, moduleComponent.getX(), moduleComponent.getY() + moduleComponent.getHeight())) continue;
                moduleComponent.drawSplit(context);
            }
        }
        if (this.swapping.getValue() != 0.0f) {
            x = this.x + this.width * (1.0f - this.swapping.getValue());
            float y = this.y + headerHeight + separatorHeight;
            for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
                if (!settingComponent.getSetting().isVisible() || !GuiUtility.isHovered((double)x, (double)y, (double)this.width, (double)this.height, settingComponent.getX(), settingComponent.getY()) && !GuiUtility.isHovered((double)x, (double)y, (double)this.width, (double)this.height, settingComponent.getX(), settingComponent.getY() + settingComponent.getHeight())) continue;
                settingComponent.drawSplit(context);
            }
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        block5: {
            block4: {
                float y;
                if (this.selectedModuleComponent == null) break block4;
                if (GuiUtility.isHovered((double)this.x, (double)(this.y + 52.0f), (double)this.width, (double)(this.height - 52.0f), mouseX, mouseY)) {
                    for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
                        if (!settingComponent.getSetting().isVisible()) continue;
                        settingComponent.onMouseClicked(mouseX, mouseY, button);
                    }
                }
                if (!GuiUtility.isHovered((double)this.x, (double)(y = this.y + 28.0f), (double)this.width, 20.0, mouseX, mouseY) || button != MouseButton.LEFT) break block5;
                this.selectedModuleComponent = null;
                if (!Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) break block5;
                ClientSounds.CLICKGUI_OPEN.play(0.8f, 1.2f);
                break block5;
            }
            if (GuiUtility.isHovered((double)this.x, (double)(this.y + 28.0f), (double)this.width, (double)(this.height - 28.0f), mouseX, mouseY)) {
                for (ModuleComponent moduleComponent : this.moduleComponents) {
                    if (this.searchCheck(moduleComponent)) continue;
                    moduleComponent.onMouseClicked(mouseX, mouseY, button);
                }
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (this.selectedModuleComponent != null) {
            for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.onMouseReleased(mouseX, mouseY, button);
            }
        } else {
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                moduleComponent.onMouseReleased(mouseX, mouseY, button);
            }
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isHovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
            if (this.selectedModuleComponent != null) {
                this.settingsScroll.onKeyPressed(keyCode);
            } else {
                this.modulesScroll.onKeyPressed(keyCode);
            }
        }
        if (this.selectedModuleComponent != null) {
            for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.onKeyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            for (ModuleComponent moduleComponent : this.moduleComponents) {
                moduleComponent.onKeyPressed(keyCode, scanCode, modifiers);
            }
        }
        super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.selectedModuleComponent != null) {
            for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
                if (!settingComponent.getSetting().isVisible()) continue;
                settingComponent.charTyped(chr, modifiers);
            }
        }
        return super.charTyped(chr, modifiers);
    }

    private boolean searchCheck(ModuleComponent component) {
        MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
        if (menuScreen instanceof DropDownScreen) {
            DropDownScreen dropDownScreen = (DropDownScreen)menuScreen;
            TextField search = dropDownScreen.getSearchField();
            return search != null && !search.getBuiltText().isBlank() && !component.getModule().getName().toLowerCase().contains(search.getBuiltText().toLowerCase()) && !component.getModule().getName().replace(" ", "").toLowerCase().contains(search.getBuiltText().toLowerCase());
        }
        return true;
    }

    @Override
    public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.isHovered(mouseX, mouseY)) {
            if (this.selectedModuleComponent != null) {
                this.settingsScroll.scroll(verticalAmount);
            } else {
                this.modulesScroll.scroll(verticalAmount);
            }
        }
    }

    @Generated
    public MenuCategory getCategory() {
        return this.category;
    }

    @Generated
    public Animation getSwapping() {
        return this.swapping;
    }

    @Generated
    public Animation getSizing() {
        return this.sizing;
    }

    @Generated
    public List<ModuleComponent> getModuleComponents() {
        return this.moduleComponents;
    }

    @Generated
    public ScrollHandler getModulesScroll() {
        return this.modulesScroll;
    }

    @Generated
    public ScrollHandler getSettingsScroll() {
        return this.settingsScroll;
    }

    @Generated
    public Font getTitleFont() {
        return this.titleFont;
    }

    @Generated
    public ModuleComponent getLastSelected() {
        return this.lastSelected;
    }

    @Generated
    public ModuleComponent getSelectedModuleComponent() {
        return this.selectedModuleComponent;
    }

    @Generated
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    @Generated
    public void setLastSelected(ModuleComponent lastSelected) {
        this.lastSelected = lastSelected;
    }

    @Generated
    public void setSelectedModuleComponent(ModuleComponent selectedModuleComponent) {
        this.selectedModuleComponent = selectedModuleComponent;
    }
}

