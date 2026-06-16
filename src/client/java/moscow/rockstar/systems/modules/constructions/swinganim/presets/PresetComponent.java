/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetFile;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.render.ScissorUtility;

public class PresetComponent
extends CustomComponent {
    private final Animation addAnim = new Animation(300L, Easing.BAKEK);
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private final TextField textField;
    private final Animation heightAnim = new Animation(300L, Easing.BAKEK_SMALLER);

    public PresetComponent() {
        this.textField = new TextField(Fonts.REGULAR.getFont(8.0f));
        this.textField.setPreview(Localizator.translate("type_name"));
    }

    @Override
    protected void renderComponent(UIContext context) {
        boolean hover;
        float elmtY;
        SwingManager swingManager = Rockstar.getInstance().getSwingManager();
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        List<SwingPresetFile> presets = manager.getSwingPresetFiles();
        float x = this.x + 8.0f;
        float y = this.y - 1.0f;
        float width = this.width - 16.0f;
        this.scrollHandler.update();
        context.drawRoundedRect(x - 1.0f, y + 7.0f, width + 2.0f, 8.0f + this.height - 46.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        ScissorUtility.push(context.getMatrices(), x - 1.0f, y + 7.5f, width + 2.0f, 7.0f + this.height - 46.0f);
        float offset = 0.0f;
        for (SwingPreset swingPreset : Rockstar.getInstance().getSwingManager().getPresets()) {
            elmtY = (float)((double)(y + 14.0f + offset) - this.scrollHandler.getValue());
            hover = GuiUtility.isHovered(x - 1.0f, y + 7.5f, width + 2.0f, 7.0f + this.height - 46.0f, context) && GuiUtility.isHovered((double)(x - 1.0f), (double)(elmtY - 4.0f), (double)(width + 2.0f), 12.0, context.getMouseX(), context.getMouseY());
            swingPreset.getHoverAnimation().update(hover);
            swingPreset.getActiveAnimation().update(Objects.equals(swingPreset.getName(), swingManager.getCurrent()));
            context.drawFadeoutText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(swingPreset.getName()), x + 7.0f, elmtY + 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * swingPreset.getHoverAnimation().getValue() + 0.25f * swingPreset.getActiveAnimation().getValue())), 0.8f, 1.0f, width - 12.0f - swingPreset.getActiveAnimation().getValue() * 10.0f);
            if (hover) {
                CursorUtility.set(CursorType.HAND);
            }
            if (swingPreset.getActiveAnimation().getValue() >= 0.0f) {
                context.drawTexture(Rockstar.id("icons/check.png"), x + width - 11.0f - swingPreset.getActiveAnimation().getValue() * 2.0f, elmtY, 6.0f, 6.0f, Colors.getTextColor().withAlpha(swingPreset.getActiveAnimation().getValue() * 255.0f));
            }
            offset += 12.0f;
        }
        for (SwingPresetFile swingPresetFile : presets) {
            if (swingPresetFile.getFileName().equals("autosave")) continue;
            elmtY = (float)((double)(y + 14.0f + offset) - this.scrollHandler.getValue());
            hover = GuiUtility.isHovered(x - 1.0f, y + 7.5f, width + 2.0f, 7.0f + this.height - 46.0f, context) && GuiUtility.isHovered((double)(x - 1.0f), (double)(elmtY - 4.0f), (double)(width + 2.0f), 12.0, context.getMouseX(), context.getMouseY());
            swingPresetFile.getHoverAnimation().update(hover);
            swingPresetFile.getActiveAnimation().update(Objects.equals(swingPresetFile.getFileName(), swingManager.getCurrent()));
            context.drawFadeoutText(Fonts.REGULAR.getFont(7.0f), swingPresetFile.getFileName(), x + 7.0f + 10.0f * swingPresetFile.getHoverAnimation().getValue(), elmtY + 0.5f, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * swingPresetFile.getHoverAnimation().getValue() + 0.25f * swingPresetFile.getActiveAnimation().getValue())), 0.8f, 1.0f, width - 12.0f - swingPresetFile.getActiveAnimation().getValue() * 10.0f - 10.0f * swingPresetFile.getHoverAnimation().getValue());
            if (hover) {
                CursorUtility.set(CursorType.HAND);
            }
            if (swingPresetFile.getHoverAnimation().getValue() >= 0.0f) {
                context.drawTexture(Rockstar.id("icons/trash.png"), x + 7.0f * swingPresetFile.getHoverAnimation().getValue(), elmtY, 6.0f, 6.0f, Colors.getTextColor().withAlpha(swingPresetFile.getHoverAnimation().getValue() * 255.0f));
            }
            if (swingPresetFile.getActiveAnimation().getValue() >= 0.0f) {
                context.drawTexture(Rockstar.id("icons/check.png"), x + width - 11.0f - swingPresetFile.getActiveAnimation().getValue() * 2.0f, elmtY, 6.0f, 6.0f, Colors.getTextColor().withAlpha(swingPresetFile.getActiveAnimation().getValue() * 255.0f));
            }
            offset += 12.0f;
        }
        ScissorUtility.pop();
        context.drawRoundedRect(x - 1.0f, y + this.height - 25.0f, width + 2.0f, 20.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().mulAlpha(0.3f));
        context.drawTexture(Rockstar.id("icons/add.png"), x + width - 2.0f * this.addAnim.getValue() - 10.0f, y + this.height - 25.0f + 6.0f, 8.0f, 8.0f, Colors.getTextColor().mulAlpha(this.addAnim.getValue()));
        this.textField.set(x - 1.0f, y + this.height - 25.0f, width + 2.0f - 12.0f, 20.0f);
        this.textField.setAlpha(1.0f);
        this.textField.render(context);
        this.addAnim.update(!this.textField.getBuiltText().isBlank());
        if (GuiUtility.isHovered(x + width - 2.0f - 10.0f, y + this.height - 25.0f + 6.0f, 8.0, 8.0, context) && this.addAnim.getValue() > 0.0f) {
            CursorUtility.set(CursorType.HAND);
        }
        this.scrollHandler.setMax(-offset + this.height - 20.0f - 25.0f);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        boolean hover;
        float elmtY;
        this.textField.onMouseClicked(mouseX, mouseY, button);
        SwingManager swingManager = Rockstar.getInstance().getSwingManager();
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        List<SwingPresetFile> presets = manager.getSwingPresetFiles();
        float x = this.x + 8.0f;
        float y = this.y - 1.0f;
        float width = this.width - 16.0f;
        float offset = 0.0f;
        for (SwingPreset swingPreset : Rockstar.getInstance().getSwingManager().getPresets()) {
            elmtY = (float)((double)(y + 14.0f + offset) - this.scrollHandler.getValue());
            boolean bl = hover = GuiUtility.isHovered((double)(x - 1.0f), (double)(y + 7.5f), (double)(width + 2.0f), (double)(7.0f + this.height - 46.0f), mouseX, mouseY) && GuiUtility.isHovered((double)(x - 1.0f), (double)(elmtY - 4.0f), (double)(width + 2.0f), 12.0, mouseX, mouseY);
            if (hover && button == MouseButton.LEFT) {
                swingManager.getBezier().start(swingPreset.getBezierStart()).end(swingPreset.getBezierEnd());
                swingManager.getBack().enabled(swingPreset.isSwingBack());
                swingManager.getSpeed().setCurrentValue(swingPreset.getSpeed());
                SwingPhase start = swingManager.getStartPhase();
                start.getAnchorX().setCurrentValue(swingPreset.getFrom().anchorX());
                start.getAnchorY().setCurrentValue(swingPreset.getFrom().anchorY());
                start.getAnchorZ().setCurrentValue(swingPreset.getFrom().anchorZ());
                start.getMoveX().setCurrentValue(swingPreset.getFrom().moveX());
                start.getMoveY().setCurrentValue(swingPreset.getFrom().moveY());
                start.getMoveZ().setCurrentValue(swingPreset.getFrom().moveZ());
                start.getRotateX().setCurrentValue(swingPreset.getFrom().rotateX());
                start.getRotateY().setCurrentValue(swingPreset.getFrom().rotateY());
                start.getRotateZ().setCurrentValue(swingPreset.getFrom().rotateZ());
                SwingPhase end = swingManager.getEndPhase();
                end.getAnchorX().setCurrentValue(swingPreset.getTo().anchorX());
                end.getAnchorY().setCurrentValue(swingPreset.getTo().anchorY());
                end.getAnchorZ().setCurrentValue(swingPreset.getTo().anchorZ());
                end.getMoveX().setCurrentValue(swingPreset.getTo().moveX());
                end.getMoveY().setCurrentValue(swingPreset.getTo().moveY());
                end.getMoveZ().setCurrentValue(swingPreset.getTo().moveZ());
                end.getRotateX().setCurrentValue(swingPreset.getTo().rotateX());
                end.getRotateY().setCurrentValue(swingPreset.getTo().rotateY());
                end.getRotateZ().setCurrentValue(swingPreset.getTo().rotateZ());
                manager.setCurrent(null);
                swingManager.setCurrent(swingPreset.getName());
            }
            offset += 12.0f;
        }
        for (SwingPresetFile swingPresetFile : new ArrayList<SwingPresetFile>(presets)) {
            if (swingPresetFile.getFileName().equals("autosave")) continue;
            elmtY = (float)((double)(y + 14.0f + offset) - this.scrollHandler.getValue());
            boolean bl = hover = GuiUtility.isHovered((double)(x - 1.0f), (double)(y + 7.5f), (double)(width + 2.0f), (double)(7.0f + this.height - 46.0f), mouseX, mouseY) && GuiUtility.isHovered((double)(x - 1.0f), (double)(elmtY - 4.0f), (double)(width + 2.0f), 12.0, mouseX, mouseY);
            if (hover && GuiUtility.isHovered((double)(x + 7.0f), (double)elmtY, 6.0, 6.0, mouseX, mouseY) && button == MouseButton.LEFT) {
                swingPresetFile.delete();
            } else if (hover && button == MouseButton.LEFT) {
                if (manager.getCurrent() != null) {
                    manager.getCurrent().save();
                }
                swingManager.setCurrent(swingPresetFile.getFileName());
                swingPresetFile.load();
            }
            offset += 12.0f;
        }
        if (GuiUtility.isHovered((double)(x + width - 2.0f - 10.0f), (double)(y + this.height - 25.0f + 6.0f), 8.0, 8.0, mouseX, mouseY) && !this.textField.getBuiltText().isBlank()) {
            this.create();
        }
    }

    private void create() {
        SwingPhase.PhaseSlider slider;
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        SwingManager swingManager = Rockstar.getInstance().getSwingManager();
        swingManager.getBezier().start(0.5f, 1.0f).end(0.5f, 0.0f);
        swingManager.getBack().enabled(true);
        swingManager.getSpeed().setCurrentValue(2.0f);
        for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
            if (!(setting instanceof SwingPhase.PhaseSlider)) continue;
            slider = (SwingPhase.PhaseSlider)setting;
            slider.setCurrentValue(0.0f);
        }
        for (Setting setting : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
            if (!(setting instanceof SwingPhase.PhaseSlider)) continue;
            slider = (SwingPhase.PhaseSlider)setting;
            slider.setCurrentValue(0.0f);
        }
        swingManager.setCurrent(this.textField.getBuiltText());
        manager.createPreset(this.textField.getBuiltText());
        manager.getPreset(this.textField.getBuiltText()).load();
        this.textField.clear();
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.textField.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && !this.textField.getBuiltText().isBlank()) {
            this.create();
            return;
        }
        this.textField.onKeyPressed(keyCode, scanCode, modifiers);
        if (this.isHovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
            this.scrollHandler.onKeyPressed(keyCode);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        this.textField.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollHandler.scroll(verticalAmount);
    }

    @Override
    public float getHeight() {
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        List<SwingPresetFile> presets = manager.getSwingPresetFiles();
        this.height = this.heightAnim.update(Math.min(presets.size() * 12 + Rockstar.getInstance().getSwingManager().getPresets().size() * 12 - 12, 182) + 46);
        return this.height;
    }
}

