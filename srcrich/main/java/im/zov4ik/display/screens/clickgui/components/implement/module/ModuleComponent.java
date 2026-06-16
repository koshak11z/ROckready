package im.zov4ik.display.screens.clickgui.components.implement.module;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import org.joml.Matrix4f;

import im.zov4ik.features.module.Module;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.features.module.setting.SettingComponentAdder;
import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.utils.client.chat.StringHelper;
import im.zov4ik.zov4ik;
import im.zov4ik.display.screens.clickgui.ClickGuiPainter;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;
import im.zov4ik.display.screens.clickgui.components.AbstractComponent;
import im.zov4ik.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
public class ModuleComponent extends AbstractComponent {
    private static final float ROW_HEIGHT = 18F;

    private final List<AbstractSettingComponent> components = new ArrayList<>();
    private final Module module;
    private boolean expanded;
    private float cachedHeight = 15F;
    private final Animation expandAnimation = new Decelerate().setMs(220).setValue(1);
    private final Map<AbstractSettingComponent, Float> settingVisibility = new HashMap<>();
    private final Map<AbstractSettingComponent, Float> settingHeight = new HashMap<>();
    private boolean binding;
    private float toggleProgress;

    public ModuleComponent(Module module) {
        this.module = module;
        new SettingComponentAdder().addSettingComponent(module.settings(), components);
        this.expanded = switch (module.getVisibleName()) {
            case "AutoGapple", "BowAimbot", "ElytraFly", "EnchantmentColor", "JumpCircle", "AutoTool", "AutoBuy" -> true;
            default -> false;
        };
        expandAnimation.setDirection(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
        for (AbstractSettingComponent component : components) {
            settingVisibility.put(component, 0F);
            settingHeight.put(component, 16F);
        }
        toggleProgress = module.isState() ? 1F : 0F;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float rowHeight = ROW_HEIGHT;
        float cardHeight = Math.max(rowHeight, getComponentHeight());
        boolean hovered = Calculate.isHovered(mouseX, mouseY, x, y, width, rowHeight);
        expandAnimation.setDirection(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
        float expand = expandAnimation.getOutput().floatValue();
        toggleProgress = Calculate.interpolateSmooth(3.6F, toggleProgress, module.isState() ? 1F : 0F);

        blur.render(ShapeProperties.create(context.getMatrices(), x + 0.2F, y + 0.15F, width - 0.4F, cardHeight - 0.3F)
                .round(ClickGuiTheme.ITEM_RADIUS + 0.15F)
                .softness(14F)
                .color(ColorAssist.multAlpha(0xFF050910, 0.09F + toggleProgress * 0.025F))
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, cardHeight)
                .round(ClickGuiTheme.ITEM_RADIUS)
                .thickness(0.95F)
                .outlineColor(ClickGuiTheme.moduleRowOutline(toggleProgress, hovered))
                .color(ClickGuiTheme.moduleRowFill(toggleProgress, hovered), ClickGuiTheme.moduleRowFill(toggleProgress, hovered),
                        ClickGuiTheme.moduleRowFillBottom(toggleProgress, hovered), ClickGuiTheme.moduleRowFillBottom(toggleProgress, hovered))
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 3.0F, y + 4.2F, 1.25F, rowHeight - 8.4F)
                .round(0.8F)
                .color(ColorAssist.multAlpha(ClickGuiTheme.accent(), 0.82F))
                .build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 5, y + rowHeight + 0.1F, width - 10, 0.4F)
                .round(0.3F).color(ClickGuiTheme.SEPARATOR).build());

        int textColor = ClickGuiTheme.moduleEnabledText(toggleProgress);
        String name = binding ? module.getVisibleName() + " ..." : module.getVisibleName();
        String bindLabel = getBindLabel();
        float toggleX = x + width - ClickGuiTheme.TOGGLE_WIDTH - 5.5F;
        float toggleY = y + rowHeight / 2F - ClickGuiTheme.TOGGLE_HEIGHT / 2F;
        float bindWidth = getBindBadgeWidth(bindLabel);
        float bindHeight = 8.8F;
        float bindX = toggleX - bindWidth - 1.4F;
        float rightEdge = bindLabel == null ? toggleX : bindX;
        drawModuleName(context, name, y + 6.45F, textColor, rightEdge - (x + 10.5F) - 5F);
        if (bindLabel != null) {
            drawBindBadge(context, bindLabel, bindX, y + rowHeight / 2F - bindHeight / 2F + 0.55F, bindWidth, bindHeight, toggleProgress);
        }
        ClickGuiPainter.drawToggle(context, toggleX, toggleY, toggleProgress);

        float offset = y + rowHeight + 1.5F;
        float settingsHeight = getAnimatedSettingsHeight(expand);
        float visibleSettingsHeight = settingsHeight;
        if (visibleSettingsHeight > 0.2F) {
            float innerX = x + 1.8F;
            float innerW = width - 3.6F;

            ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
            Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
            scissorManager.push(positionMatrix, innerX, offset, innerW, visibleSettingsHeight);
            for (AbstractSettingComponent component : components) {
                Supplier<Boolean> visible = component.getSetting().getVisible();
                boolean shouldBeVisible = visible == null || visible.get();
                float visibility = settingVisibility.getOrDefault(component, shouldBeVisible ? 1F : 0F) * expand;

                float baseHeight = settingHeight.getOrDefault(component, 16F);
                float elementHeight = baseHeight * visibility;

                component.x = innerX + 1F;
                component.y = offset;
                component.width = innerW - 2F;

                if (elementHeight > 0.4F) {
                    scissorManager.push(positionMatrix, component.x, component.y, component.width, elementHeight);
                    component.render(context, mouseX, mouseY, delta);
                    scissorManager.pop();
                    float measured = Math.max(16F, component.height);
                    settingHeight.put(component, measured);
                    baseHeight = measured;
                    elementHeight = baseHeight * visibility;
                }

                offset += elementHeight;
            }
            scissorManager.pop();
        }

        cachedHeight = rowHeight + 1.5F + settingsHeight;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float rowHeight = ROW_HEIGHT;
        boolean hoveredRow = Calculate.isHovered(mouseX, mouseY, x, y, width, rowHeight);

        if (hoveredRow && button == 2) {
            binding = !binding;
            return true;
        }

        if (binding && button >= 2) {
            module.setKey(button);
            binding = false;
            return true;
        }

        if (hoveredRow && button == 1) {
            expanded = !expanded;
            return true;
        }

        if (hoveredRow && button == 0 && !binding) {
            module.switchState();
            return true;
        }

        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            for (int i = components.size() - 1; i >= 0; i--) {
                AbstractSettingComponent component = components.get(i);
                if (component.isHover(mouseX, mouseY) && component.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            for (int i = components.size() - 1; i >= 0; i--) {
                if (components.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            components.forEach(component -> component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            components.forEach(component -> component.mouseScrolled(mouseX, mouseY, amount));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            module.setKey(keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode);
            binding = false;
            return true;
        }
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            components.forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            components.forEach(component -> component.charTyped(chr, modifiers));
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, getComponentHeight())) {
            return true;
        }
        if (expanded || !expandAnimation.isFinished(Direction.BACKWARDS)) {
            for (AbstractSettingComponent component : components) {
                if (component.isHover(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getComponentHeight() {
        return Math.max((int) ROW_HEIGHT, Math.round(cachedHeight));
    }

    private void drawModuleName(DrawContext context, String text, float drawY, int color, float availableWidth) {
        FontRenderer font = Fonts.getSize(ClickGuiTheme.MODULE_FONT_SIZE, Fonts.Type.BOLD);
        float drawX = x + 10.5F;
        availableWidth = Math.max(10F, availableWidth);

        if (font.getStringWidth(text) <= availableWidth) {
            font.drawString(context.getMatrices(), text, drawX, drawY, color);
            return;
        }

        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
        scissorManager.push(positionMatrix, drawX, y + 3F, availableWidth, 16F);
        font.drawStringWithScroll(context.getMatrices(), text, drawX, drawY, availableWidth, color);
        scissorManager.pop();
    }

    private void drawBindBadge(DrawContext context, String label, float badgeX, float badgeY, float badgeWidth, float badgeHeight, float activeProgress) {
        ClickGuiPainter.drawControlSurface(context, badgeX, badgeY, badgeWidth, badgeHeight, 3.7F, activeProgress);
        Fonts.getSize(10, Fonts.Type.BOLD).drawCenteredString(context.getMatrices(), label, badgeX + badgeWidth / 2F + 0.25F, badgeY + badgeHeight / 2F - 1.0F,
                ColorAssist.overCol(ClickGuiTheme.TEXT_MUTED, ClickGuiTheme.TEXT_PRIMARY, activeProgress));
    }

    private float getBindBadgeWidth(String label) {
        if (label == null) {
            return 0F;
        }
        return Math.max(15.5F, Fonts.getSize(10, Fonts.Type.BOLD).getStringWidth(label) + 8F);
    }

    private String getBindLabel() {
        if (binding || module.getKey() < 0) {
            return null;
        }

        String bind = StringHelper.getBindName(module.getKey());
        if (bind == null || bind.equals("N/A")) {
            return null;
        }

        if (bind.startsWith("MOUSE ")) {
            return "M" + bind.substring(6);
        }

        String collapsed = bind.replace(" ", "");
        if (collapsed.length() <= 3) {
            return collapsed;
        }

        StringBuilder initials = new StringBuilder();
        for (String part : bind.split(" ")) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }

        if (initials.length() >= 2 && initials.length() <= 3) {
            return initials.toString();
        }

        return collapsed.substring(0, 3);
    }

    private float getSettingsHeight() {
        float h = 0;
        for (AbstractSettingComponent component : components) {
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get()) {
                continue;
            }
            h += component.height <= 0 ? 16 : component.height;
        }
        return h;
    }

    private float getAnimatedSettingsHeight(float expand) {
        float h = 0F;
        for (AbstractSettingComponent component : components) {
            Supplier<Boolean> visible = component.getSetting().getVisible();
            boolean shouldBeVisible = visible == null || visible.get();
            float target = shouldBeVisible ? 1F : 0F;
            float current = settingVisibility.getOrDefault(component, 0F);
            float smoothed = Calculate.interpolateSmooth(3, current, target);
            settingVisibility.put(component, smoothed);
            h += settingHeight.getOrDefault(component, 16F) * smoothed * expand;
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleComponent that = (ModuleComponent) o;
        return module.equals(that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }
}
