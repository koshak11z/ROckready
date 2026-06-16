package im.zov4ik.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;

import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.display.screens.clickgui.MenuScreen;
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
        initializeCategoryComponents(MenuScreen.INSTANCE != null ? MenuScreen.INSTANCE.getCategory() : ModuleCategory.COMBAT);
    }

    public void initializeCategoryComponents(ModuleCategory selected) {
        categoryComponents.clear();
        categoryComponents.add(new CategoryComponent(selected));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (width <= 40 || height <= 20) return;

        ModuleCategory selected = MenuScreen.INSTANCE != null ? MenuScreen.INSTANCE.getCategory() : ModuleCategory.COMBAT;
        if (categoryComponents.isEmpty() || categoryComponents.getFirst().getCategory() != selected) {
            initializeCategoryComponents(selected);
        }

        int outerPadding = 4;
        int panelWidth = Math.max(0, Math.round(width) - outerPadding * 2);
        int panelHeight = Math.max(0, Math.round(height) - outerPadding * 2);
        int panelX = Math.round(x) + outerPadding;
        int panelY = Math.round(y) + outerPadding;

        for (CategoryComponent component : categoryComponents) {
            component.x = panelX;
            component.y = panelY;
            component.width = panelWidth;
            component.height = panelHeight;
            component.render(context, mouseX, mouseY, delta);
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
        categoryComponents.forEach(c -> c.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        categoryComponents.forEach(c -> c.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        categoryComponents.forEach(c -> c.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        categoryComponents.forEach(c -> c.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        categoryComponents.forEach(c -> c.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        categoryComponents.forEach(c -> c.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }
}
