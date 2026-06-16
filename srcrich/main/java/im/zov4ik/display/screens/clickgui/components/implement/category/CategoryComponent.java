package im.zov4ik.display.screens.clickgui.components.implement.category;

import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.MenuScreen;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.module.ModuleComponent;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.zov4ik;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CategoryComponent extends AbstractComponent {
    private static final int COLUMNS = 3;
    private static final float PAD = 7.0F;
    private static final float COLUMN_GAP = 6.0F;
    private static final float ROW_GAP = 4.0F;

    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final ModuleCategory category;

    private float listX;
    private float listY;
    private float listWidth;
    private float listHeight;

    public CategoryComponent(ModuleCategory category) {
        this.category = category;
        reloadModules();
    }

    public ModuleCategory getCategory() {
        return category;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        validateModuleCache();

        float panelX = Math.round(x);
        float panelY = Math.round(y);
        float panelWidth = Math.round(width);
        float panelHeight = Math.round(height);
        float panelRadius = 5.2F;
        float innerInset = 1.15F;

        blur.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, panelWidth, panelHeight)
                .round(panelRadius)
                .softness(18F)
                .color(new Color(0, 0, 0, 55).getRGB())
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), panelX, panelY, panelWidth, panelHeight)
                .round(panelRadius)
                .thickness(1.0F)
                .outlineColor(new Color(52, 58, 68, 165).getRGB())
                .color(
                        new Color(16, 18, 22, 220).getRGB(),
                        new Color(16, 18, 22, 220).getRGB(),
                        new Color(10, 12, 15, 228).getRGB(),
                        new Color(10, 12, 15, 228).getRGB())
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), panelX + innerInset, panelY + innerInset, panelWidth - innerInset * 2F, panelHeight - innerInset * 2F)
                .round(panelRadius - 1.2F)
                .color(
                        new Color(16, 18, 22, 220).getRGB(),
                        new Color(16, 18, 22, 220).getRGB(),
                        new Color(10, 12, 15, 228).getRGB(),
                        new Color(10, 12, 15, 228).getRGB())
                .build());

        listX = panelX + PAD;
        listY = panelY + PAD;
        listWidth = panelWidth - PAD * 2.0F;
        listHeight = panelHeight - PAD * 2.0F;

        List<ModuleComponent> visibleModules = moduleComponents.stream()
                .filter(this::matchesSearch)
                .toList();
        float columnWidth = (listWidth - COLUMN_GAP * (COLUMNS - 1)) / COLUMNS;
        List<List<ModuleComponent>> columns = distributeColumns(visibleModules);
        float contentHeight = getColumnsHeight(columns);

        scroll = MathHelper.clamp(scroll, -Math.max(0, contentHeight - listHeight), 0);
        smoothedScroll = Calculate.interpolateSmooth(2, (float) smoothedScroll, (float) scroll);

        ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        scissorManager.push(positionMatrix, listX, listY, listWidth, listHeight);
        for (int column = 0; column < COLUMNS; column++) {
            float columnX = listX + column * (columnWidth + COLUMN_GAP);
            float offset = listY + (float) smoothedScroll;
            for (ModuleComponent component : columns.get(column)) {
                float componentHeight = component.getComponentHeight();
                component.x = columnX;
                component.y = offset;
                component.width = columnWidth;
                if (offset + componentHeight >= listY - ROW_GAP && offset <= listY + listHeight + ROW_GAP) {
                    component.render(context, mouseX, mouseY, delta);
                }
                offset += componentHeight + ROW_GAP;
            }
        }
        scissorManager.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Calculate.isHovered(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        for (ModuleComponent component : moduleComponents) {
            if (matchesSearch(component) && isModuleVisible(component) && component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        moduleComponents.forEach(component -> {
            if (matchesSearch(component) && isModuleVisible(component)) {
                component.mouseReleased(mouseX, mouseY, button);
            }
        });
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        moduleComponents.forEach(component -> {
            if (matchesSearch(component) && isModuleVisible(component)) {
                component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        });
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height)) {
            scroll += amount * 16;
            return true;
        }

        moduleComponents.forEach(component -> {
            if (matchesSearch(component) && isModuleVisible(component)) {
                component.mouseScrolled(mouseX, mouseY, amount);
            }
        });
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        moduleComponents.forEach(component -> {
            if (matchesSearch(component) && isModuleVisible(component)) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        });
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        moduleComponents.forEach(component -> {
            if (matchesSearch(component) && isModuleVisible(component)) {
                component.charTyped(chr, modifiers);
            }
        });
        return super.charTyped(chr, modifiers);
    }

    private void reloadModules() {
        moduleComponents.clear();
        zov4ik.getInstance().getModuleRepository().modules().stream()
                .filter(module -> module.getCategory() == category)
                .sorted(Comparator.comparing(module -> module.getVisibleName().toLowerCase()))
                .forEach(module -> moduleComponents.add(new ModuleComponent(module)));
    }

    private void validateModuleCache() {
        long actual = zov4ik.getInstance().getModuleRepository().modules().stream()
                .filter(module -> module.getCategory() == category)
                .count();
        if (moduleComponents.size() != actual) {
            reloadModules();
        }
    }

    private boolean isModuleVisible(ModuleComponent component) {
        return component.y + component.getComponentHeight() > listY && component.y < listY + listHeight;
    }

    private boolean matchesSearch(ModuleComponent component) {
        String search = MenuScreen.INSTANCE.getSearchText();
        return search == null || search.isBlank()
                || component.getModule().getName().toLowerCase().contains(search.toLowerCase())
                || component.getModule().getVisibleName().toLowerCase().contains(search.toLowerCase());
    }

    private List<List<ModuleComponent>> distributeColumns(List<ModuleComponent> visibleModules) {
        List<List<ModuleComponent>> columns = new ArrayList<>();
        float[] heights = new float[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            columns.add(new ArrayList<>());
        }
        for (ModuleComponent component : visibleModules) {
            int shortest = 0;
            for (int i = 1; i < COLUMNS; i++) {
                if (heights[i] < heights[shortest]) {
                    shortest = i;
                }
            }
            columns.get(shortest).add(component);
            heights[shortest] += component.getComponentHeight() + ROW_GAP;
        }
        return columns;
    }

    private float getColumnsHeight(List<List<ModuleComponent>> columns) {
        float maxHeight = 0.0F;
        for (List<ModuleComponent> column : columns) {
            float columnHeight = 0.0F;
            for (ModuleComponent component : column) {
                columnHeight += component.getComponentHeight() + ROW_GAP;
            }
            maxHeight = Math.max(maxHeight, Math.max(0.0F, columnHeight - ROW_GAP));
        }
        return maxHeight;
    }
}
