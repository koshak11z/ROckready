package im.zov4ik.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.screens.clickgui.ClickGuiTheme;

public class TextComponent extends AbstractSettingComponent {
    public static boolean typing;

    private final TextSetting setting;
    private float boxX, boxY, boxW, boxH;
    private int cursorPosition;
    private float xOffset;
    private String localText = "";
    private float typingProgress;

    public TextComponent(TextSetting setting) {
        super(setting);
        this.setting = setting;
        if (setting.getText() != null) {
            localText = setting.getText();
            cursorPosition = localText.length();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 20;
        String displayText = localText == null ? "" : localText;
        if (!typing && displayText.isEmpty() && setting.getText() != null) {
            displayText = setting.getText();
        }
        boxW = getAdaptiveControlWidth(displayText);
        boxH = ClickGuiTheme.CONTROL_HEIGHT;
        boxX = x + width - boxW - ClickGuiTheme.CONTROL_MARGIN;
        boxY = y + 1.7F;
        typingProgress = Calculate.interpolateSmooth(3.4F, typingProgress, typing ? 1F : 0F);
        drawSettingLabel(context, setting.getName(), boxX, 4.15F);

        drawControlSurface(context, boxX, boxY, boxW, typingProgress);

        FontRenderer font = Fonts.getSize(ClickGuiTheme.CONTROL_FONT_SIZE, Fonts.Type.MANROPEBOLD);
        String text = displayText;

        float cursorX = font.getStringWidth(text.substring(0, Math.min(cursorPosition, text.length())));
        if (cursorX < xOffset) {
            xOffset = cursorX;
        } else if (cursorX - xOffset > boxW - 8) {
            xOffset = cursorX - (boxW - 8);
        }

        if (font.getStringWidth(text) <= boxW - 8F || typing) {
            font.drawString(context.getMatrices(), text, boxX + 3 - xOffset, boxY + 6.9F, ClickGuiTheme.TEXT_PRIMARY);
        } else {
            drawControlText(context, text, boxX, boxY, boxW);
        }

        boolean showCursor = typing && (System.currentTimeMillis() % 900 < 450);
        if (showCursor) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), boxX + 3 - xOffset + cursorX, boxY + 3.4F, 1.0F, 8.6F)
                    .color(ClickGuiTheme.TEXT_PRIMARY).build());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && Calculate.isHovered(mouseX, mouseY, boxX, boxY, boxW, boxH)) {
            typing = true;
            cursorPosition = (localText == null ? 0 : localText.length());
            return true;
        }
        if (button == 0) {
            typing = false;
            commit();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!typing) {
            return super.charTyped(chr, modifiers);
        }

        String text = localText == null ? "" : localText;
        if (text.length() >= setting.getMax()) {
            return true;
        }

        localText = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
        cursorPosition++;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!typing) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        String text = localText == null ? "" : localText;

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
            commit();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorPosition > 0 && !text.isEmpty()) {
                localText = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            cursorPosition = Math.max(0, cursorPosition - 1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            cursorPosition = Math.min(text.length(), cursorPosition + 1);
            return true;
        }

        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_A) {
            cursorPosition = text.length();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void commit() {
        String text = localText == null ? "" : localText.trim();
        if (text.length() >= setting.getMin() && text.length() <= setting.getMax()) {
            setting.setText(text);
            localText = text;
        } else if (setting.getText() != null) {
            localText = setting.getText();
        }
        cursorPosition = localText == null ? 0 : localText.length();
    }
}
