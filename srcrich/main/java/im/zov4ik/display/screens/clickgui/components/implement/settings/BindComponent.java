package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.client.chat.StringHelper;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;

public class BindComponent extends AbstractSettingComponent {
    private final BindSetting setting;
    private boolean binding;
    private float bindingProgress;

    public BindComponent(BindSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 20;
        String bindName = StringHelper.getBindName(setting.getKey());
        String text = binding ? "..." : bindName;
        float boxWidth = getBindBoxWidth(text);
        float boxX = x + width - boxWidth - ClickGuiTheme.CONTROL_MARGIN;
        bindingProgress = Calculate.interpolateSmooth(3.4F, bindingProgress, binding ? 1F : 0F);
        drawSettingLabel(context, setting.getName(), boxX, 6.95F);
        drawControlSurface(context, boxX, y + 1.7F, boxWidth, bindingProgress);
        drawControlText(context, text, boxX, y + 1.7F, boxWidth);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String bindName = StringHelper.getBindName(setting.getKey());
        String text = binding ? "..." : bindName;
        float boxWidth = getBindBoxWidth(text);
        float boxX = x + width - boxWidth - ClickGuiTheme.CONTROL_MARGIN;
        boolean hoveredBox = Calculate.isHovered(mouseX, mouseY, boxX, y + 1.7F, boxWidth, ClickGuiTheme.CONTROL_HEIGHT);

        if (!hoveredBox && !binding) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (hoveredBox && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setting.setKey(button);
            binding = false;
            return true;
        }

        if (hoveredBox && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            binding = !binding;
            return true;
        }

        if (binding && button >= 2) {
            setting.setKey(button);
            binding = false;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        setting.setKey(keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode);
        binding = false;
        return true;
    }

    private float getBindBoxWidth(String text) {
        float desired = Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.MANROPEBOLD).getStringWidth(text) + 9F;
        return Math.max(24F, Math.min(40F, desired));
    }
}
