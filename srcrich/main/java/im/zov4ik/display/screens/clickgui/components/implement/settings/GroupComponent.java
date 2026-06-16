package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.features.module.setting.SettingComponentAdder;
import im.zov4ik.features.module.setting.implement.GroupSetting;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.display.scissor.ScissorAssist;
import im.zov4ik.zov4ik;
import im.zov4ik.display.screens.clickgui.ClickGuiPainter;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GroupComponent extends AbstractSettingComponent {
    private static final float HEADER_HEIGHT = 24F;

    private final GroupSetting setting;
    private final List<AbstractSettingComponent> children = new ArrayList<>();
    private final Animation openAnimation = new Decelerate().setMs(180).setValue(1);
    private final boolean alwaysExpanded;
    private boolean expanded;
    private float toggleProgress;

    public GroupComponent(GroupSetting setting) {
        super(setting);
        this.setting = setting;
        new SettingComponentAdder().addSettingComponent(setting.getSubSettings(), children);

        this.alwaysExpanded = setting.getName().equalsIgnoreCase("Move correction")
                || setting.getName().equalsIgnoreCase("Target Esp");
        if (alwaysExpanded) {
            setting.setValue(true);
        }

        this.expanded = alwaysExpanded || setting.isValue();
        openAnimation.setDirection(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
        toggleProgress = this.expanded ? 1F : 0F;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (alwaysExpanded) {
            setting.setValue(true);
        }

        expanded = alwaysExpanded || setting.isValue();
        openAnimation.setDirection(expanded ? Direction.FORWARDS : Direction.BACKWARDS);
        float open = openAnimation.getOutput().floatValue();
        toggleProgress = Calculate.interpolateSmooth(3.5F, toggleProgress, expanded ? 1F : 0F);

        float badgeSize = ClickGuiTheme.STATE_BADGE_SIZE;
        float badgeX = x + width - badgeSize - 6.5F;
        drawSettingLabel(context, setting.getName(), alwaysExpanded ? x + width - 2F : badgeX - 5F);

        if (!alwaysExpanded) {
            ClickGuiPainter.drawStateBadge(context, badgeX, y + HEADER_HEIGHT / 2F - badgeSize / 2F - 0.65F, badgeSize, toggleProgress);
        }

        float childTotal = 0F;
        for (AbstractSettingComponent child : children) {
            Supplier<Boolean> visible = child.getSetting().getVisible();
            if (visible != null && !visible.get()) {
                continue;
            }
            childTotal += getChildBaseHeight(child);
        }

        float visibleHeight = childTotal * open;
        float offsetY = y + HEADER_HEIGHT;

        if (visibleHeight > 0.2F) {
            ScissorAssist scissorManager = zov4ik.getInstance().getScissorManager();
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            scissorManager.push(matrix, x + 2, offsetY, width - 4, visibleHeight);

            for (AbstractSettingComponent child : children) {
                Supplier<Boolean> visible = child.getSetting().getVisible();
                if (visible != null && !visible.get()) {
                    continue;
                }

                child.x = x + 2;
                child.y = offsetY;
                child.width = width - 4;
                child.render(context, mouseX, mouseY, delta);
                offsetY += child.height <= 0 ? getChildBaseHeight(child) : child.height;
            }

            scissorManager.pop();
        }

        height = HEADER_HEIGHT + visibleHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float badgeSize = ClickGuiTheme.STATE_BADGE_SIZE;
        float badgeX = x + width - badgeSize - 6.5F;

        if (!alwaysExpanded && button == 0 && Calculate.isHovered(mouseX, mouseY, badgeX, y + HEADER_HEIGHT / 2F - badgeSize / 2F, badgeSize, badgeSize)) {
            setting.setValue(!setting.isValue());
            expanded = setting.isValue();
            return true;
        }

        if (!alwaysExpanded && button == 0 && Calculate.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            setting.setValue(!setting.isValue());
            expanded = setting.isValue();
            return true;
        }

        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            for (int i = children.size() - 1; i >= 0; i--) {
                AbstractSettingComponent child = children.get(i);
                if (child.isHover(mouseX, mouseY) && child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            for (int i = children.size() - 1; i >= 0; i--) {
                if (children.get(i).mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            children.forEach(child -> child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            children.forEach(child -> child.mouseReleased(mouseX, mouseY, button));
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            children.forEach(child -> child.mouseScrolled(mouseX, mouseY, amount));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            children.forEach(child -> child.keyPressed(keyCode, scanCode, modifiers));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            children.forEach(child -> child.charTyped(chr, modifiers));
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height)) {
            return true;
        }

        if (expanded || !openAnimation.isFinished(Direction.BACKWARDS)) {
            for (AbstractSettingComponent child : children) {
                if (child.isHover(mouseX, mouseY)) {
                    return true;
                }
            }
        }

        return false;
    }

    private float getChildBaseHeight(AbstractSettingComponent child) {
        return Math.max(22F, child.height);
    }
}
