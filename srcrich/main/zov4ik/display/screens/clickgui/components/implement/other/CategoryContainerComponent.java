package im.zov4ik.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;

import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.display.screens.clickgui.components.implement.settings.TextComponent;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.category.CategoryComponent;
import im.zov4ik.utils.interactions.inv.InventoryFlowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Setter
@Accessors(chain = true)
public class CategoryContainerComponent extends AbstractComponent {
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();

    public void initializeCategoryComponents() {
        categoryComponents.clear();
        for (ModuleCategory category : Arrays.asList(
                ModuleCategory.COMBAT,
                ModuleCategory.MOVEMENT,
                ModuleCategory.RENDER,
                ModuleCategory.PLAYER,
                ModuleCategory.MISC
        )) {
            categoryComponents.add(new CategoryComponent(category));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (width <= 40 || height <= 20) {
            return;
        }

        int panelCount = categoryComponents.size();
        int panelGap = 5;
        int outerPadding = 4;
        int usableWidth = Math.max(0, Math.round(width) - outerPadding * 2 - panelGap * Math.max(0, panelCount - 1));
        int basePanelWidth = panelCount == 0 ? 0 : usableWidth / panelCount;
        int remainder = panelCount == 0 ? 0 : usableWidth % panelCount;
        int panelHeight = Math.max(0, Math.round(height) - outerPadding * 2);
        int offsetX = Math.round(x) + outerPadding;
        int panelY = Math.round(y) + outerPadding;

        for (int i = 0; i < categoryComponents.size(); i++) {
            CategoryComponent component = categoryComponents.get(i);
            int currentWidth = basePanelWidth + (i < remainder ? 1 : 0);
            component.x = offsetX;
            component.y = panelY;
            component.width = currentWidth;
            component.height = panelHeight;
            component.render(context, mouseX, mouseY, delta);
            offsetX += currentWidth + panelGap;
        }
    }

    @Override
    public void tick() {
        if (TextComponent.typing || SearchComponent.typing) InventoryFlowManager.unPressMoveKeys();
        else InventoryFlowManager.updateMoveKeys();
        categoryComponents.forEach(AbstractComponent::tick);
        super.tick();
    }

    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        categoryComponents.forEach(categoryComponent -> categoryComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }
}
