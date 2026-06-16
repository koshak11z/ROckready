/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.util.math.MathHelper
 */
package moscow.rockstar.ui.components.textfield;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;

public class TextField
extends CustomComponent
implements IMinecraft {
    public static TextField LAST_FIELD;
    private final HashMap<Character, Float> charSoundCache = new HashMap();
    private final List<TypedText> texts = new ArrayList<TypedText>();
    private final Font font;
    private String lastBuilt = "";
    private String builtText = "";
    private final Animation focusing = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private boolean focused;
    private final Animation cursorAnim = new Animation(300L, 0.0f, Easing.BAKEK);
    private int cursor;
    private Selection selection;
    private float startX = 0.0f;
    private float endX = 0.0f;
    private int drag = -1;
    private long lastClickTime = 0L;
    private int clickCount = 0;
    private final Timer typingTimer = new Timer();
    private String preview = "";
    private String icon = "";
    private Map<String, FieldAction> append = new HashMap<String, FieldAction>();
    private String appending = "";
    private float xPos;
    private final Timer moveTimer = new Timer();
    private float alpha = 1.0f;
    private ColorRGBA textColor = ColorRGBA.WHITE;

    @Override
    protected void renderComponent(UIContext context) {
        String text2;
        float offset = 0.0f;
        float cleanOffset = 0.0f;
        float cursorOffset = 0.0f;
        float fontOffset = this.height / 2.0f - this.font.height() / 2.0f;
        float cursorWidth = this.font.height() / 8.0f;
        this.texts.removeIf(text -> text.showing.getValue() == 0.0f && text.removing);
        this.focusing.update(this.focused);
        if (this.selection != null && this.selection.getStart() == this.selection.getEnd()) {
            this.selection = null;
        }
        if (this.drag != -1) {
            this.typingTimer.reset();
            int current = -1;
            float v = 0.0f;
            for (TypedText typedText : this.texts) {
                String text3 = String.valueOf(typedText.type);
                if ((float)context.getMouseX() < this.x + this.xPos + v + this.font.width(text3) + this.font.width(text3) / 2.0f) {
                    current = this.texts.indexOf(typedText);
                    break;
                }
                v += this.font.width(text3);
            }
            if (current == -1) {
                current = this.texts.size();
            }
            if (current != this.drag) {
                this.selection = new Selection(current > this.drag, Math.min(this.drag, current), Math.max(this.drag, current));
                this.cursor = current;
            } else {
                if (this.selection != null) {
                    this.cursor = this.selection.getStart();
                }
                this.selection = null;
            }
        }
        if (this.isHovered(context)) {
            CursorUtility.set(CursorType.TEXT);
        }
        this.updateAppend();
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)this.alpha);
        context.drawRect(this.x + this.xPos + fontOffset + this.startX, this.y + fontOffset - 1.0f, this.endX - this.startX, this.font.height() + 2.0f, ColorRGBA.BLUE.mix(new ColorRGBA(76.0f, 99.0f, 122.0f), 0.7f).withAlpha(255.0f * this.focusing.getValue()));
        this.startX = 0.0f;
        this.endX = 0.0f;
        FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, this.font.getFont());
        if (this.texts.isEmpty()) {
            context.drawText(this.font, this.preview, this.x + offset + fontOffset, this.y + fontOffset - 2.0f * this.focusing.getValue(), this.textColor.mulAlpha(0.75f * (1.0f - this.focusing.getValue())));
        }
        if (!this.appending.isEmpty() && this.appending.toLowerCase().startsWith(this.builtText.toLowerCase()) && !this.builtText.isEmpty()) {
            context.drawText(this.font, this.builtText + this.appending.substring(this.builtText.length()), this.x + offset + fontOffset, this.y + fontOffset, this.textColor.withAlpha(150.0f * this.focusing.getValue()));
        }
        for (TypedText typedText : this.texts) {
            text2 = String.valueOf(typedText.type);
            typedText.showing.setDuration(200L);
            typedText.showing.update(!typedText.removing);
            context.drawText(this.font, text2, this.x + offset + fontOffset + this.xPos, this.y + fontOffset + 2.0f - 2.0f * typedText.showing.getValue(), this.textColor.withAlpha(255.0f * typedText.showing.getValue()));
            offset += this.font.width(text2) * typedText.showing.getValue();
            cleanOffset += this.font.width(text2);
            if (this.texts.indexOf(typedText) == this.cursor - 1) {
                cursorOffset = cleanOffset;
            }
            if (this.selection == null) continue;
            if (this.texts.indexOf(typedText) == this.selection.getStart() - 1) {
                this.startX = cleanOffset;
            }
            if (this.texts.indexOf(typedText) != this.selection.getEnd() - 1) continue;
            this.endX = cleanOffset;
        }
        ((Batching)fontBatching).draw();
        cursorOffset += (float)(this.cursor == this.texts.size() ? 1 : 0);
        if (this.moveTimer.finished(10L)) {
            for (TypedText typedText : this.texts) {
                text2 = String.valueOf(typedText.type);
                if (cursorOffset + fontOffset + this.xPos > this.width - 5.0f) {
                    this.xPos -= this.font.width(text2);
                    this.moveTimer.reset();
                    break;
                }
                if (!(cursorOffset + fontOffset + this.xPos < 5.0f)) continue;
                this.xPos += this.font.width(text2);
                this.moveTimer.reset();
                break;
            }
            if (this.font.width(this.builtText) < this.width - 10.0f) {
                this.xPos = 0.0f;
            }
        }
        this.cursorAnim.setEasing(Easing.BAKEK_SMALLER);
        this.cursorAnim.update(cursorOffset);
        RenderUtility.rotate(context.getMatrices(), this.x + fontOffset + this.xPos + this.cursorAnim.getValue() + cursorWidth / 2.0f, this.y + fontOffset - 1.0f, Math.clamp(cursorOffset - this.cursorAnim.getValue(), -20.0f, 20.0f));
        context.drawRect(this.x + fontOffset + this.cursorAnim.getValue() + this.xPos, this.y + fontOffset - 1.0f, cursorWidth, this.font.height() + 2.0f, this.textColor.withAlpha((float)((double)(200.0f * this.focusing.getValue()) * (!this.typingTimer.finished(300L) ? 3.0 : MathUtility.sin((double)System.currentTimeMillis() / 200.0) + 2.0) / 3.0)));
        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        ScissorUtility.pop();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == MouseButton.LEFT) {
                long currentTime = System.currentTimeMillis();
                this.clickCount = currentTime - this.lastClickTime < 500L ? ++this.clickCount : 1;
                this.lastClickTime = currentTime;
                this.focused = true;
                float offset = 0.0f;
                int newCursor = this.texts.size();
                for (TypedText typedText : this.texts) {
                    String text = String.valueOf(typedText.type);
                    if (mouseX < (double)(this.x + this.xPos + offset + this.font.width(text) + this.font.width(text) / 2.0f)) {
                        newCursor = this.texts.indexOf(typedText);
                        break;
                    }
                    offset += this.font.width(text);
                }
                this.cursor = newCursor;
                if (this.clickCount == 2) {
                    this.selectWordAtCursor();
                    this.drag = -1;
                } else {
                    this.selection = null;
                    this.drag = this.cursor;
                }
            }
        } else {
            this.focused = false;
        }
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = -1;
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.focused) {
            return;
        }
        if ((keyCode == 259 || keyCode == 261) && this.selection != null) {
            this.clearSelection();
            ClientSounds.TYPING.play(0.3f, 1.2f);
            this.moveCursor(0);
        } else if (keyCode == 259 && this.cursor > 0) {
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(false)) : 1;
            for (int i = 0; i < offset; ++i) {
                this.moveCursor(-1);
                TypedText last = null;
                for (TypedText text : this.texts) {
                    if (!text.removing) {
                        last = text;
                    }
                    if (this.texts.indexOf(text) != this.cursor) continue;
                    break;
                }
                if (last == null) continue;
                last.removing = true;
            }
            ClientSounds.TYPING.play(0.3f, 1.2f);
        } else if (keyCode == 261 && this.cursor < this.texts.size()) {
            int offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(true)) : 1;
            block2: for (int i = 0; i < offset && this.cursor < this.texts.size(); ++i) {
                for (int j = this.cursor; j < this.texts.size(); ++j) {
                    TypedText text = this.texts.get(j);
                    if (text.removing) continue;
                    text.removing = true;
                    continue block2;
                }
            }
            this.moveCursor(0);
            ClientSounds.TYPING.play(0.3f, 1.2f);
        } else if (keyCode == 263) {
            int offset;
            ClientSounds.TYPING.play(0.3f, 1.3f);
            int n = offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(false)) : 1;
            if (Screen.hasShiftDown()) {
                this.select(-offset);
            } else if (this.selection != null) {
                this.cursor = this.selection.getStart();
                this.selection = null;
                return;
            }
            this.moveCursor(-offset);
        } else if (keyCode == 262) {
            int offset;
            ClientSounds.TYPING.play(0.3f, 1.3f);
            int n = offset = Screen.hasControlDown() ? Math.max(1, this.getWordSize(true)) : 1;
            if (Screen.hasShiftDown()) {
                this.select(offset);
            } else if (this.selection != null) {
                this.cursor = this.selection.getEnd();
                this.selection = null;
                return;
            }
            this.moveCursor(offset);
        } else if (Screen.isSelectAll((int)keyCode)) {
            this.selection = new Selection(true, 0, this.texts.size());
        } else if (Screen.isCopy((int)keyCode)) {
            if (this.selection != null) {
                TextField.mc.keyboard.setClipboard(this.getSelectedText());
                return;
            }
            TextField.mc.keyboard.setClipboard(this.builtText);
        } else if (Screen.isCut((int)keyCode)) {
            if (this.selection != null) {
                TextField.mc.keyboard.setClipboard(this.getSelectedText());
                this.clearSelection();
                this.moveCursor(0);
                return;
            }
            TextField.mc.keyboard.setClipboard(this.builtText);
            for (TypedText text : this.texts) {
                text.removing = true;
            }
            this.builtText = "";
        } else if (Screen.isPaste((int)keyCode)) {
            this.paste(TextField.mc.keyboard.getClipboard());
        } else if (keyCode == 258 || keyCode == 257) {
            for (Map.Entry<String, FieldAction> sugg : this.append.entrySet()) {
                if (!sugg.getKey().toLowerCase().startsWith(this.builtText.toLowerCase()) || this.builtText.isEmpty() || sugg.getValue() == null) continue;
                this.clear();
                if (keyCode == 257) {
                    sugg.getValue().getEnter().run();
                } else {
                    sugg.getValue().getTab().run();
                }
                this.focused = false;
                return;
            }
            if (keyCode == 257) {
                this.focused = false;
            }
        } else if (keyCode == 259 && this.texts.isEmpty()) {
            this.focused = false;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.focused) {
            return false;
        }
        if (chr == ' ') {
            ClientSounds.TYPING.play(0.3f, 0.8f);
        } else {
            if (!this.charSoundCache.containsKey(Character.valueOf(chr))) {
                this.charSoundCache.put(Character.valueOf(chr), Float.valueOf(MathUtility.random(0.8, 1.2)));
            }
            ClientSounds.TYPING.play(0.3f, this.charSoundCache.get(Character.valueOf(chr)).floatValue());
        }
        this.typeChar(chr);
        return true;
    }

    private void updateAppend() {
        this.lastBuilt = this.builtText;
        StringBuilder builder = new StringBuilder();
        for (TypedText text : this.texts) {
            builder.append(text.type);
        }
        this.builtText = builder.toString();
        if (!this.lastBuilt.equals(this.builtText)) {
            this.appending = "";
            for (String string : this.append.keySet()) {
                if (!string.toLowerCase().startsWith(this.builtText.toLowerCase()) || this.builtText.isEmpty()) continue;
                this.appending = string;
            }
        }
    }

    public int getWordSize(boolean forward) {
        int counter = 0;
        if (forward) {
            for (int i = this.cursor; i < this.texts.size(); ++i) {
                TypedText text = this.texts.get(i);
                if (!text.removing && text.type != ' ') {
                    ++counter;
                    continue;
                }
                break;
            }
        } else {
            for (int i = this.cursor - 1; i >= 0; --i) {
                TypedText text = this.texts.get(i);
                if (!text.removing && text.type != ' ') {
                    ++counter;
                    continue;
                }
                break;
            }
        }
        return counter;
    }

    public void paste(String paste) {
        for (char c : paste.toCharArray()) {
            this.typeChar(c);
        }
    }

    public void typeChar(char c) {
        this.clearSelection();
        this.texts.add(Math.clamp((long)this.cursor, 0, Math.max(0, this.texts.size())), new TypedText(c));
        this.moveCursor(1);
        LAST_FIELD = this;
    }

    private void moveCursor(int offset) {
        this.cursor = MathHelper.clamp((int)(this.cursor + offset), (int)0, (int)this.texts.size());
        this.typingTimer.reset();
    }

    public void clear() {
        this.texts.clear();
        this.builtText = "";
    }

    private void selectWordAtCursor() {
        TypedText text;
        if (this.texts.isEmpty()) {
            return;
        }
        int wordStart = this.cursor;
        int wordEnd = this.cursor;
        int i = this.cursor - 1;
        while (i >= 0) {
            text = this.texts.get(i);
            if (text.removing || text.type == ' ' || !Character.isLetterOrDigit(text.type)) break;
            wordStart = i--;
        }
        for (i = this.cursor; i < this.texts.size(); ++i) {
            text = this.texts.get(i);
            if (text.removing || text.type == ' ' || !Character.isLetterOrDigit(text.type)) break;
            wordEnd = i + 1;
        }
        if (wordStart != wordEnd) {
            this.selection = new Selection(true, wordStart, wordEnd);
            this.cursor = wordEnd;
        }
    }

    private void clearSelection() {
        if (this.selection == null) {
            return;
        }
        for (TypedText text : this.getSelected()) {
            text.removing = true;
        }
        this.cursor = this.selection.getStart();
        this.selection = null;
    }

    private List<TypedText> getSelected() {
        ArrayList<TypedText> typedTexts = new ArrayList<TypedText>();
        boolean inSelection = false;
        for (TypedText text : this.texts) {
            if (this.texts.indexOf(text) == this.selection.getStart()) {
                inSelection = true;
            }
            if (this.texts.indexOf(text) == this.selection.getEnd()) {
                inSelection = false;
            }
            if (!inSelection) continue;
            typedTexts.add(text);
        }
        return typedTexts;
    }

    private String getSelectedText() {
        StringBuilder builder = new StringBuilder();
        boolean inSelection = false;
        for (TypedText text : this.texts) {
            if (this.texts.indexOf(text) == this.selection.getStart()) {
                inSelection = true;
            }
            if (this.texts.indexOf(text) == this.selection.getEnd()) {
                inSelection = false;
            }
            if (!inSelection) continue;
            builder.append(text.type);
        }
        return builder.toString();
    }

    private void select(int offset) {
        if (this.selection == null) {
            this.selection = new Selection(offset > 0, this.cursor, this.cursor);
        }
        if (!this.selection.forward) {
            this.selection.start = MathHelper.clamp((int)(this.selection.getStart() + offset), (int)0, (int)this.texts.size());
        } else {
            this.selection.end = MathHelper.clamp((int)(this.selection.getEnd() + offset), (int)0, (int)this.texts.size());
        }
    }

    @Generated
    public TextField(Font font) {
        this.font = font;
    }

    @Generated
    public String getBuiltText() {
        return this.builtText;
    }

    @Generated
    public boolean isFocused() {
        return this.focused;
    }

    @Generated
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Generated
    public String getPreview() {
        return this.preview;
    }

    @Generated
    public String getIcon() {
        return this.icon;
    }

    @Generated
    public void setPreview(String preview) {
        this.preview = preview;
    }

    @Generated
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Generated
    public void setAppend(Map<String, FieldAction> append) {
        this.append = append;
    }

    @Generated
    public String getAppending() {
        return this.appending;
    }

    @Generated
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Generated
    public ColorRGBA getTextColor() {
        return this.textColor;
    }

    @Generated
    public void setTextColor(ColorRGBA textColor) {
        this.textColor = textColor;
    }

    static class Selection {
        final boolean forward;
        int start;
        int end;

        int getStart() {
            return Math.min(this.end, this.start);
        }

        int getEnd() {
            return Math.max(this.end, this.start);
        }

        @Generated
        public Selection(boolean forward, int start, int end) {
            this.forward = forward;
            this.start = start;
            this.end = end;
        }
    }

    static class TypedText {
        final Animation showing = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
        boolean removing;
        final char type;

        @Generated
        public TypedText(char type) {
            this.type = type;
        }
    }
}

