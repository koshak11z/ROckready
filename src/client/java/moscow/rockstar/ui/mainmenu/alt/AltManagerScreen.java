package moscow.rockstar.ui.mainmenu.alt;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.ScissorUtility;
import net.minecraft.client.gui.screen.Screen;

/**
 * Expensive-style offline alt manager: list of saved nicks, a name field with add, and
 * Login / Delete / Back actions. Login swaps the live offline session ({@link AltManager#login}).
 */
public class AltManagerScreen extends CustomScreen implements IMinecraft {
    private static final float CARD_W = 300.0f;
    private static final float CARD_H = 232.0f;
    private static final float ROW_H = 22.0f;

    private final Screen parent;
    private String input = "";
    private boolean inputFocused;
    private int selected = -1;
    private float scroll;

    public AltManagerScreen(Screen parent) {
        this.parent = parent;
    }

    private float cardX() {
        return (this.width - CARD_W) / 2.0f;
    }

    private float cardY() {
        return (this.height - CARD_H) / 2.0f;
    }

    private float listTop() {
        return this.cardY() + 40.0f;
    }

    private float listBottom() {
        return this.cardY() + CARD_H - 70.0f;
    }

    private float fieldX() {
        return this.cardX() + 12.0f;
    }

    private float fieldY() {
        return this.cardY() + CARD_H - 58.0f;
    }

    private float fieldW() {
        return CARD_W - 24.0f - 56.0f;
    }

    private float addBtnX() {
        return this.fieldX() + this.fieldW() + 6.0f;
    }

    private float[] actionRect(int i) {
        float total = CARD_W - 24.0f;
        float gap = 6.0f;
        float w = (total - 2.0f * gap) / 3.0f;
        return new float[]{this.cardX() + 12.0f + i * (w + gap), this.cardY() + CARD_H - 32.0f, w, 20.0f};
    }

    private float maxScroll() {
        float content = AltManager.getAlts().size() * ROW_H;
        float view = this.listBottom() - this.listTop();
        return Math.max(0.0f, content - view);
    }

    @Override
    public void render(UIContext context) {
        this.scroll = Math.max(0.0f, Math.min(this.scroll, this.maxScroll()));
        float mx = context.getMouseX();
        float my = context.getMouseY();

        context.drawRoundedRect(0.0f, 0.0f, this.width, this.height, BorderRadius.ZERO,
                new VerticalGradient(new ColorRGBA(20.0f, 22.0f, 30.0f), new ColorRGBA(6.0f, 6.0f, 12.0f)));

        float x = this.cardX();
        float y = this.cardY();
        Glyphs.background(context, x, y, CARD_W, CARD_H, 10.0f, 1.0f);
        context.drawRoundedBorder(x, y, CARD_W, CARD_H, 1.0f, BorderRadius.all(10.0f), Colors.getAccentColor().mulAlpha(0.25f));

        // header
        Glyphs.zLogo(context, x + 14.0f, y + 12.0f, 12.0f, Colors.getAccentColor());
        context.drawText(Fonts.SEMIBOLD.getFont(11.0f), "Alt Manager", x + 32.0f, y + 12.0f, Colors.getTextColor());
        context.drawRightText(Fonts.MEDIUM.getFont(7.5f), AltManager.currentName(), x + CARD_W - 14.0f, y + 14.0f, Colors.getAccentColor().mulAlpha(0.85f));
        context.drawRect(x + 12.0f, y + 32.0f, CARD_W - 24.0f, 0.6f, Glyphs.divider(1.0f));

        // list
        List<String> alts = AltManager.getAlts();
        Font rowFont = Fonts.MEDIUM.getFont(8.0f);
        float listX = x + 12.0f;
        float listW = CARD_W - 24.0f;
        ScissorUtility.push(context.getMatrices(), listX, this.listTop(), listW, this.listBottom() - this.listTop());
        if (alts.isEmpty()) {
            context.drawCenteredText(rowFont, "Список пуст — добавьте ник ниже", x + CARD_W / 2.0f,
                    (this.listTop() + this.listBottom()) / 2.0f - rowFont.height() / 2.0f, Colors.getTextColor().mulAlpha(0.45f));
        }
        for (int i = 0; i < alts.size(); i++) {
            float rowY = this.listTop() + i * ROW_H - this.scroll;
            if (rowY + ROW_H < this.listTop() || rowY > this.listBottom()) {
                continue;
            }
            boolean hovered = GuiUtility.isHovered((double) listX, (double) rowY, (double) listW, (double) (ROW_H - 2.0f), (double) mx, (double) my);
            boolean sel = i == this.selected;
            ColorRGBA bg = sel ? Colors.getAccentColor().mulAlpha(0.30f) : Colors.getTextColor().mulAlpha(hovered ? 0.10f : 0.05f);
            context.drawRoundedRect(listX, rowY, listW, ROW_H - 2.0f, BorderRadius.all(5.0f), bg);
            Glyphs.person(context, listX + 7.0f, rowY + (ROW_H - 2.0f - 10.0f) / 2.0f, 10.0f, Colors.getTextColor().mulAlpha(0.8f));
            context.drawText(rowFont, alts.get(i), listX + 24.0f, rowY + (ROW_H - 2.0f - rowFont.height()) / 2.0f, Colors.getTextColor());
            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }
        }
        ScissorUtility.pop();

        // name field
        boolean fieldHover = GuiUtility.isHovered((double) this.fieldX(), (double) this.fieldY(), (double) this.fieldW(), 18.0, (double) mx, (double) my);
        context.drawRoundedRect(this.fieldX(), this.fieldY(), this.fieldW(), 18.0f, BorderRadius.all(5.0f),
                Colors.getTextColor().mulAlpha(this.inputFocused ? 0.12f : 0.07f));
        if (this.inputFocused) {
            context.drawRoundedBorder(this.fieldX(), this.fieldY(), this.fieldW(), 18.0f, 1.0f, BorderRadius.all(5.0f), Colors.getAccentColor().mulAlpha(0.6f));
        }
        Font fieldFont = Fonts.MEDIUM.getFont(8.0f);
        boolean caret = this.inputFocused && (System.currentTimeMillis() / 500L) % 2L == 0L;
        String shown = this.input.isEmpty() && !this.inputFocused ? "Никнейм..." : this.input + (caret ? "_" : "");
        ColorRGBA fieldColor = this.input.isEmpty() && !this.inputFocused ? Colors.getTextColor().mulAlpha(0.4f) : Colors.getTextColor();
        context.drawText(fieldFont, shown, this.fieldX() + 7.0f, this.fieldY() + (18.0f - fieldFont.height()) / 2.0f, fieldColor);
        if (fieldHover) {
            CursorUtility.set(CursorType.HAND);
        }

        // add button
        boolean addHover = GuiUtility.isHovered((double) this.addBtnX(), (double) this.fieldY(), 50.0, 18.0, (double) mx, (double) my);
        boolean canAdd = AltManager.isValid(this.input);
        context.drawRoundedRect(this.addBtnX(), this.fieldY(), 50.0f, 18.0f, BorderRadius.all(5.0f),
                Colors.getAccentColor().mulAlpha(canAdd ? (addHover ? 0.95f : 0.8f) : 0.3f));
        context.drawCenteredText(fieldFont, "Добавить", this.addBtnX() + 25.0f, this.fieldY() + (18.0f - fieldFont.height()) / 2.0f, Colors.WHITE);
        if (addHover && canAdd) {
            CursorUtility.set(CursorType.HAND);
        }

        // action buttons
        String[] labels = {"Войти", "Удалить", "Назад"};
        boolean hasSel = this.selected >= 0 && this.selected < alts.size();
        for (int i = 0; i < 3; i++) {
            float[] r = this.actionRect(i);
            boolean enabled = i == 2 || hasSel;
            boolean hovered = GuiUtility.isHovered((double) r[0], (double) r[1], (double) r[2], (double) r[3], (double) mx, (double) my);
            ColorRGBA base = i == 0 ? Colors.getAccentColor()
                    : i == 1 ? new ColorRGBA(220.0f, 80.0f, 80.0f) : Colors.getAdditionalColor();
            context.drawRoundedRect(r[0], r[1], r[2], r[3], BorderRadius.all(5.0f), base.mulAlpha(enabled ? (hovered ? 0.95f : 0.7f) : 0.25f));
            context.drawCenteredText(fieldFont, labels[i], r[0] + r[2] / 2.0f, r[1] + (r[3] - fieldFont.height()) / 2.0f, Colors.getTextColor());
            if (hovered && enabled) {
                CursorUtility.set(CursorType.HAND);
            }
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button != MouseButton.LEFT) {
            super.onMouseClicked(mouseX, mouseY, button);
            return;
        }
        List<String> alts = AltManager.getAlts();

        // list rows
        float listX = this.cardX() + 12.0f;
        float listW = CARD_W - 24.0f;
        boolean clickedRow = false;
        for (int i = 0; i < alts.size(); i++) {
            float rowY = this.listTop() + i * ROW_H - this.scroll;
            if (rowY + ROW_H < this.listTop() || rowY > this.listBottom()) {
                continue;
            }
            if (GuiUtility.isHovered((double) listX, (double) rowY, (double) listW, (double) (ROW_H - 2.0f), mouseX, mouseY)) {
                this.selected = i;
                clickedRow = true;
                break;
            }
        }

        // name field focus
        this.inputFocused = GuiUtility.isHovered((double) this.fieldX(), (double) this.fieldY(), (double) this.fieldW(), 18.0, mouseX, mouseY);

        // add
        if (GuiUtility.isHovered((double) this.addBtnX(), (double) this.fieldY(), 50.0, 18.0, mouseX, mouseY)) {
            this.addCurrent();
        }

        // actions
        boolean hasSel = this.selected >= 0 && this.selected < alts.size();
        if (GuiUtility.isHovered((double) this.actionRect(0)[0], (double) this.actionRect(0)[1], (double) this.actionRect(0)[2], (double) this.actionRect(0)[3], mouseX, mouseY) && hasSel) {
            AltManager.login(alts.get(this.selected));
        } else if (GuiUtility.isHovered((double) this.actionRect(1)[0], (double) this.actionRect(1)[1], (double) this.actionRect(1)[2], (double) this.actionRect(1)[3], mouseX, mouseY) && hasSel) {
            AltManager.remove(alts.get(this.selected));
            this.selected = -1;
            this.save();
        } else if (GuiUtility.isHovered((double) this.actionRect(2)[0], (double) this.actionRect(2)[1], (double) this.actionRect(2)[2], (double) this.actionRect(2)[3], mouseX, mouseY)) {
            mc.setScreen(this.parent);
        }

        super.onMouseClicked(mouseX, mouseY, button);
    }

    private void addCurrent() {
        if (AltManager.isValid(this.input)) {
            AltManager.add(this.input);
            this.input = "";
            this.save();
        }
    }

    private void save() {
        Rockstar.getInstance().getFileManager().writeFile("client");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.inputFocused) {
            if (keyCode == 259 && !this.input.isEmpty()) {
                this.input = this.input.substring(0, this.input.length() - 1);
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                this.addCurrent();
                return true;
            }
            if (keyCode == 256) {
                this.inputFocused = false;
                return true;
            }
            return true;
        }
        if (keyCode == 256) {
            mc.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.inputFocused && this.input.length() < 16 && (Character.isLetterOrDigit(chr) || chr == '_')) {
            this.input = this.input + chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll = Math.max(0.0f, Math.min(this.scroll - (float) verticalAmount * 12.0f, this.maxScroll()));
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
