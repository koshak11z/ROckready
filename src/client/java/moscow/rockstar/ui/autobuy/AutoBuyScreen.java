package moscow.rockstar.ui.autobuy;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.other.AutoBuy;
import moscow.rockstar.systems.modules.modules.other.autobuy.AutoBuyCategory;
import moscow.rockstar.systems.modules.modules.other.autobuy.AutoBuyItem;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RoundedRectBatching;
import moscow.rockstar.utility.render.batching.impl.SquircleBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AutoBuyScreen extends MenuScreen implements IMinecraft {
    // FULL ROCKSTAR RENDER PIPELINE: UIContext + MenuScreen + batched shapes/fonts + drawBatchItem + TextField pass.
    private static final float WIDTH = 540.0f;
    private static final float HEIGHT = 326.0f;
    private static final float SIDEBAR = 104.0f;
    private static final float DETAILS = 178.0f;
    private static final float PAD = 10.0f;

    private final AutoBuy module;
    private final ScrollHandler itemScroll = new ScrollHandler();
    private AutoBuyCategory category = AutoBuyCategory.ALL;
    private AutoBuyItem selected;
    private final TextField buyField = new TextField(Fonts.REGULAR.getFont(8.5f));
    private final TextField sellField = new TextField(Fonts.REGULAR.getFont(8.5f));
    private final TextField durabilityField = new TextField(Fonts.REGULAR.getFont(8.5f));

    public AutoBuyScreen(AutoBuy module) {
        this.module = module;
        this.buyField.setPreview("Buy <=");
        this.sellField.setPreview("Sell >=");
        this.durabilityField.setPreview("Durability %");
    }

    public boolean belongsTo(AutoBuy module) {
        return this.module == module;
    }

    @Override
    protected void init() {
        this.closing = false;
        this.menuAnimation.reset();
        this.itemScroll.reset();
        super.init();
    }

    @Override
    public void tick() {
        this.handleMovementKeys();
        super.tick();
    }

    @Override
    public void render(UIContext context) {
        this.menuAnimation.setDuration(this.closing ? 150L : 220L);
        this.menuAnimation.setEasing(this.closing ? Easing.BAKEK_BACK : Easing.BAKEK);
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        this.itemScroll.update();

        float alpha = Math.min(1.0f, this.menuAnimation.getValue());
        float x = ((float)this.width - WIDTH) / 2.0f;
        float y = ((float)this.height - HEIGHT) / 2.0f;
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderUtility.scale(context.getMatrices(), x + WIDTH / 2.0f, y + HEIGHT / 2.0f, 0.65f + 0.35f * alpha);

        this.fullRenderEffects(context, x, y, alpha);
        this.fullRenderShapeBatch(context, x, y, dark);
        this.fullRenderItemBatch(context, x, y);
        this.fullRenderFontBatch(context, x, y);
        this.fullRenderTextFields(context, x, y);

        RenderUtility.end(context.getMatrices());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (this.menuAnimation.getValue() <= 0.01f && this.closing) {
            super.close();
        }
    }

    private void fullRenderEffects(UIContext context, float x, float y, float alpha) {
        context.drawShadow(x + 4.0f, y + 6.0f, WIDTH, HEIGHT, 28.0f, BorderRadius.all(16.0f), Colors.BLACK.mulAlpha(0.22f * alpha));
        if (Interface.blurMenuEnabled() || Interface.blurPanelsEnabled()) {
            context.drawBlurredRect(x, y, WIDTH, HEIGHT, 45.0f, 8.0f, BorderRadius.all(16.0f), Colors.WHITE.withAlpha(255.0f * alpha));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(x, y, WIDTH, HEIGHT, 8.0f, 0.08f, BorderRadius.all(16.0f), Colors.WHITE.withAlpha(180.0f * alpha));
        }
        if (Interface.blurPanelsEnabled()) {
            this.panelBlur(context, x + PAD, y + PAD + 34.0f, SIDEBAR, HEIGHT - PAD * 2.0f - 34.0f, 12.0f);
            this.panelBlur(context, x + SIDEBAR + PAD * 2.0f, y + PAD + 34.0f, WIDTH - SIDEBAR - DETAILS - PAD * 4.0f, HEIGHT - PAD * 2.0f - 34.0f, 12.0f);
            this.panelBlur(context, x + WIDTH - DETAILS - PAD, y + PAD + 34.0f, DETAILS, HEIGHT - PAD * 2.0f - 34.0f, 12.0f);
        }
    }

    private void fullRenderShapeBatch(UIContext context, float x, float y, boolean dark) {
        SquircleBatching squircle = new SquircleBatching(8.0f);
        context.drawSquircle(x, y, WIDTH, HEIGHT, 8.0f, BorderRadius.all(16.0f), dark ? Colors.getAdditionalColor().mulAlpha(0.98f) : Colors.getBackgroundColor().mulAlpha(0.95f));
        this.drawSidebarShapes(context, x + PAD, y + PAD, SIDEBAR, HEIGHT - PAD * 2.0f);
        this.drawItemsShapes(context, x + SIDEBAR + PAD * 2.0f, y + PAD, WIDTH - SIDEBAR - DETAILS - PAD * 4.0f, HEIGHT - PAD * 2.0f);
        this.drawDetailsShapes(context, x + WIDTH - DETAILS - PAD, y + PAD, DETAILS, HEIGHT - PAD * 2.0f);
        squircle.draw();

        RoundedRectBatching rounded = new RoundedRectBatching();
        if (Interface.showMinimalizm()) {
            context.drawRoundedRect(x, y + 36.0f, WIDTH, 3.0f, BorderRadius.ZERO, Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm()));
        }
        rounded.draw();
    }

    private void drawSidebarShapes(UIContext context, float x, float y, float w, float h) {
        this.panelFill(context, x, y + 34.0f, w, h - 34.0f, 12.0f);
        float cy = y + 60.0f;
        for (AutoBuyCategory cat : AutoBuyCategory.values()) {
            boolean active = cat == this.category;
            boolean hover = GuiUtility.isHovered(x + 6.0f, cy, w - 12.0f, 19.0f, context);
            if (hover) CursorUtility.set(CursorType.HAND);
            context.drawSquircle(x + 6.0f, cy, w - 12.0f, 19.0f, 8.0f, BorderRadius.all(5.0f), active ? Colors.getAccentColor() : (hover ? Colors.getBackgroundColor().mulAlpha(0.55f) : Colors.getBackgroundColor().mulAlpha(0.25f)));
            cy += 22.0f;
        }
    }

    private void drawItemsShapes(UIContext context, float x, float y, float w, float h) {
        this.panelFill(context, x, y + 34.0f, w, h - 34.0f, 12.0f);
        List<AutoBuyItem> items = this.module.getItemsByCategory(this.category);
        float gridX = x + 9.0f;
        float gridY = y + 60.0f;
        float gridW = w - 18.0f;
        float gridH = h - 70.0f;
        float cell = 52.0f;
        float gap = 6.0f;
        int cols = Math.max(1, (int)((gridW + gap) / (cell + gap)));
        float contentHeight = (float)Math.ceil(items.size() / (double)cols) * (cell + gap);
        this.itemScroll.setMax(Math.min(0.0, gridH - contentHeight - 4.0f));
        float scroll = (float)this.itemScroll.getValue();
        for (int i = 0; i < items.size(); i++) {
            AutoBuyItem item = items.get(i);
            float cx = gridX + (i % cols) * (cell + gap);
            float cy = gridY + (i / cols) * (cell + gap) - scroll;
            if (cy + cell < gridY || cy > gridY + gridH) continue;
            boolean hover = GuiUtility.isHovered(cx, cy, cell, cell, context);
            boolean active = item == this.selected;
            if (hover) CursorUtility.set(CursorType.HAND);
            ColorRGBA bg = active ? Colors.getAccentColor().mulAlpha(0.55f) : item.isBuyEnabled() ? new ColorRGBA(31.0f, 110.0f, 63.0f, 165.0f) : hover ? Colors.getBackgroundColor().mulAlpha(0.75f) : Colors.getBackgroundColor().mulAlpha(0.45f);
            context.drawSquircle(cx, cy, cell, cell, 8.0f, BorderRadius.all(8.0f), bg);
        }
    }

    private void drawDetailsShapes(UIContext context, float x, float y, float w, float h) {
        this.panelFill(context, x, y + 34.0f, w, h - 34.0f, 12.0f);
        if (this.selected == null) return;
        this.toggleShape(context, x + 12.0f, y + 96.0f, w - 24.0f, 20.0f, this.selected.isBuyEnabled());
        this.toggleShape(context, x + 12.0f, y + 120.0f, w - 24.0f, 20.0f, this.selected.isSellEnabled());
        this.toggleShape(context, x + 12.0f, y + 144.0f, w - 24.0f, 20.0f, this.selected.isSetupEnabled());
        this.fieldShape(context, this.buyField, x + 12.0f, y + 174.0f, w - 24.0f, 20.0f);
        this.fieldShape(context, this.sellField, x + 12.0f, y + 198.0f, w - 24.0f, 20.0f);
        this.fieldShape(context, this.durabilityField, x + 12.0f, y + 222.0f, w - 24.0f, 20.0f);
        this.toggleShape(context, x + 12.0f, y + 252.0f, w - 24.0f, 20.0f, false);
    }

    private void fullRenderItemBatch(UIContext context, float x, float y) {
        float itemsX = x + SIDEBAR + PAD * 2.0f;
        float itemsY = y + PAD;
        float itemsW = WIDTH - SIDEBAR - DETAILS - PAD * 4.0f;
        List<AutoBuyItem> items = this.module.getItemsByCategory(this.category);
        float gridX = itemsX + 9.0f;
        float gridY = itemsY + 60.0f;
        float gridW = itemsW - 18.0f;
        float gridH = HEIGHT - PAD * 2.0f - 70.0f;
        float cell = 52.0f;
        float gap = 6.0f;
        int cols = Math.max(1, (int)((gridW + gap) / (cell + gap)));
        float scroll = (float)this.itemScroll.getValue();
        ScissorUtility.push(context.getMatrices(), gridX, gridY, gridW, gridH);
        for (int i = 0; i < items.size(); i++) {
            AutoBuyItem item = items.get(i);
            float cx = gridX + (i % cols) * (cell + gap);
            float cy = gridY + (i / cols) * (cell + gap) - scroll;
            if (cy + cell < gridY || cy > gridY + gridH) continue;
            context.drawBatchItem(item.getIconStack(), Math.round(cx + cell / 2.0f - 8.0f), Math.round(cy + 7.0f));
        }
        ScissorUtility.pop();
        if (this.selected != null) {
            float dx = x + WIDTH - DETAILS - PAD;
            float dy = y + PAD;
            context.drawBatchItem(this.selected.getIconStack(), Math.round(dx + 12.0f), Math.round(dy + 62.0f));
        }
    }

    private void fullRenderFontBatch(UIContext context, float x, float y) {
        FontBatching semi = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(16.0f), "AutoBuy", x + 16.0f, y + 12.0f, Colors.getTextColor());
        semi.draw();

        FontBatching medium = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        this.drawMediumTexts(context, x, y);
        medium.draw();

        FontBatching regular = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
        this.drawRegularTexts(context, x, y);
        regular.draw();
    }

    private void drawMediumTexts(UIContext context, float x, float y) {
        float sx = x + PAD;
        float sy = y + PAD;
        float cy = sy + 60.0f;
        for (AutoBuyCategory cat : AutoBuyCategory.values()) {
            boolean active = cat == this.category;
            context.drawText(Fonts.MEDIUM.getFont(8.0f), cat.getName(), sx + 13.0f, cy + 6.0f, active ? Colors.WHITE : Colors.getTextColor().mulAlpha(0.75f));
            cy += 22.0f;
        }
        float dx = x + WIDTH - DETAILS - PAD;
        float dy = y + PAD;
        if (this.selected == null) {
            context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), "Выбери предмет", dx + DETAILS / 2.0f, dy + 92.0f, Colors.getGuiTextInactiveColor());
            return;
        }
        AutoBuyItem item = this.selected;
        context.drawText(Fonts.MEDIUM.getFont(9.5f), trim(item.getDisplayName(), 22), dx + 41.0f, dy + 67.0f, Colors.getTextColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), item.isBuyEnabled() ? "ON" : "OFF", dx + DETAILS - 42.0f, dy + 102.0f, item.isBuyEnabled() ? Colors.WHITE : Colors.getGuiTextInactiveColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), item.isSellEnabled() ? "ON" : "OFF", dx + DETAILS - 42.0f, dy + 126.0f, item.isSellEnabled() ? Colors.WHITE : Colors.getGuiTextInactiveColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), item.isSetupEnabled() ? "ON" : "OFF", dx + DETAILS - 42.0f, dy + 150.0f, item.isSetupEnabled() ? Colors.WHITE : Colors.getGuiTextInactiveColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), "OFF", dx + DETAILS - 42.0f, dy + 258.0f, Colors.getGuiTextInactiveColor());
    }

    private void drawRegularTexts(UIContext context, float x, float y) {
        context.drawText(Fonts.REGULAR.getFont(7.5f), "Rockstar full render / " + this.module.getSelectedServerName() + " / bind P", x + 86.0f, y + 18.0f, Colors.getGuiTextInactiveColor());
        float sx = x + PAD;
        float sy = y + PAD;
        context.drawText(Fonts.REGULAR.getFont(7.0f), "Категории", sx + 9.0f, sy + 45.0f, Colors.getTextColor().mulAlpha(0.45f));
        context.drawText(Fonts.REGULAR.getFont(7.0f), "ПКМ по предмету", sx + 9.0f, sy + HEIGHT - PAD * 2.0f - 22.0f, Colors.getGuiTextInactiveColor().mulAlpha(0.7f));
        context.drawText(Fonts.REGULAR.getFont(7.0f), "быстро вкл/выкл", sx + 9.0f, sy + HEIGHT - PAD * 2.0f - 12.0f, Colors.getGuiTextInactiveColor().mulAlpha(0.7f));
        float itemsX = x + SIDEBAR + PAD * 2.0f;
        float itemsY = y + PAD;
        float itemsW = WIDTH - SIDEBAR - DETAILS - PAD * 4.0f;
        context.drawText(Fonts.REGULAR.getFont(7.0f), "Предметы", itemsX + 9.0f, itemsY + 45.0f, Colors.getTextColor().mulAlpha(0.45f));
        this.drawItemTexts(context, itemsX, itemsY, itemsW, HEIGHT - PAD * 2.0f);
        float dx = x + WIDTH - DETAILS - PAD;
        float dy = y + PAD;
        context.drawText(Fonts.REGULAR.getFont(7.0f), "Настройка", dx + 9.0f, dy + 45.0f, Colors.getTextColor().mulAlpha(0.45f));
        if (this.selected != null) {
            AutoBuyItem item = this.selected;
            context.drawText(Fonts.REGULAR.getFont(8.0f), "Покупать", dx + 20.0f, dy + 102.0f, item.isBuyEnabled() ? Colors.WHITE : Colors.getTextColor().mulAlpha(0.75f));
            context.drawText(Fonts.REGULAR.getFont(8.0f), "AutoSell", dx + 20.0f, dy + 126.0f, item.isSellEnabled() ? Colors.WHITE : Colors.getTextColor().mulAlpha(0.75f));
            context.drawText(Fonts.REGULAR.getFont(8.0f), "AutoSetup skip", dx + 20.0f, dy + 150.0f, item.isSetupEnabled() ? Colors.WHITE : Colors.getTextColor().mulAlpha(0.75f));
            context.drawText(Fonts.REGULAR.getFont(8.0f), "Thorns: " + item.getThornsMode().name(), dx + 20.0f, dy + 258.0f, Colors.getTextColor().mulAlpha(0.75f));
        }
    }

    private void drawItemTexts(UIContext context, float x, float y, float w, float h) {
        List<AutoBuyItem> items = this.module.getItemsByCategory(this.category);
        float gridX = x + 9.0f;
        float gridY = y + 60.0f;
        float gridW = w - 18.0f;
        float gridH = h - 70.0f;
        float cell = 52.0f;
        float gap = 6.0f;
        int cols = Math.max(1, (int)((gridW + gap) / (cell + gap)));
        float scroll = (float)this.itemScroll.getValue();
        ScissorUtility.push(context.getMatrices(), gridX, gridY, gridW, gridH);
        for (int i = 0; i < items.size(); i++) {
            AutoBuyItem item = items.get(i);
            float cx = gridX + (i % cols) * (cell + gap);
            float cy = gridY + (i / cols) * (cell + gap) - scroll;
            if (cy + cell < gridY || cy > gridY + gridH) continue;
            context.drawCenteredText(Fonts.REGULAR.getFont(7.2f), trim(item.getDisplayName(), 11), cx + cell / 2.0f, cy + 31.5f, Colors.getTextColor());
            context.drawCenteredText(Fonts.REGULAR.getFont(7.0f), item.getMaxBuyPrice() > 0L ? String.valueOf(item.getMaxBuyPrice()) : "off", cx + cell / 2.0f, cy + 42.0f, Colors.getGuiTextInactiveColor());
        }
        ScissorUtility.pop();
    }

    private void fullRenderTextFields(UIContext context, float x, float y) {
        if (this.selected == null) return;
        float dx = x + WIDTH - DETAILS - PAD;
        float dy = y + PAD;
        this.drawField(context, this.buyField, dx + 12.0f, dy + 174.0f, DETAILS - 24.0f, 20.0f, "Buy <=");
        this.drawField(context, this.sellField, dx + 12.0f, dy + 198.0f, DETAILS - 24.0f, 20.0f, "Sell >=");
        this.drawField(context, this.durabilityField, dx + 12.0f, dy + 222.0f, DETAILS - 24.0f, 20.0f, "Durability %");
    }

    private void panelBlur(UIContext context, float x, float y, float w, float h, float radius) {
        context.drawBlurredRect(x, y, w, h, 35.0f, radius, BorderRadius.all(radius), Colors.WHITE.withAlpha(190.0f));
        if (Interface.showGlass()) {
            context.drawLiquidGlass(x, y, w, h, radius, 0.08f, BorderRadius.all(radius), Colors.WHITE.withAlpha(180.0f));
        }
    }

    private void panelFill(UIContext context, float x, float y, float w, float h, float radius) {
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        context.drawSquircle(x, y, w, h, radius, BorderRadius.all(radius), Colors.getBackgroundColor().mulAlpha(dark ? 0.72f : 0.62f));
    }

    private void toggleShape(UIContext context, float x, float y, float w, float h, boolean enabled) {
        boolean hover = GuiUtility.isHovered(x, y, w, h, context);
        if (hover) CursorUtility.set(CursorType.HAND);
        ColorRGBA bg = enabled ? Colors.getAccentColor().mulAlpha(hover ? 0.95f : 0.78f) : Colors.getAdditionalColor().mulAlpha(hover ? 0.75f : 0.55f);
        context.drawSquircle(x, y, w, h, 7.0f, BorderRadius.all(5.0f), bg);
    }

    private void fieldShape(UIContext context, TextField field, float x, float y, float w, float h) {
        boolean focused = field.isFocused();
        context.drawSquircle(x, y, w, h, 7.0f, BorderRadius.all(5.0f), focused ? Colors.getAccentColor().mulAlpha(0.34f) : Colors.getAdditionalColor().mulAlpha(0.52f));
    }

    private void drawField(UIContext context, TextField field, float x, float y, float w, float h, String preview) {
        field.setPreview(preview);
        field.setTextColor(Colors.getTextColor());
        field.set(x + 5.0f, y + 1.0f, w - 10.0f, h - 2.0f);
        field.render(context);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.buyField.onMouseClicked(mouseX, mouseY, button);
        this.sellField.onMouseClicked(mouseX, mouseY, button);
        this.durabilityField.onMouseClicked(mouseX, mouseY, button);

        float x = ((float)this.width - WIDTH) / 2.0f;
        float y = ((float)this.height - HEIGHT) / 2.0f;
        float sidebarX = x + PAD;
        float cy = y + PAD + 60.0f;
        for (AutoBuyCategory cat : AutoBuyCategory.values()) {
            if (GuiUtility.isHovered(sidebarX + 6.0f, cy, SIDEBAR - 12.0f, 19.0f, mouseX, mouseY)) {
                this.commitInputs();
                this.category = cat;
                this.itemScroll.reset();
                ClientSounds.CLICKGUI_OPEN.play(0.25f, 1.15f);
                return;
            }
            cy += 22.0f;
        }

        float itemsX = x + SIDEBAR + PAD * 2.0f;
        float itemsY = y + PAD;
        float itemsW = WIDTH - SIDEBAR - DETAILS - PAD * 4.0f;
        List<AutoBuyItem> items = this.module.getItemsByCategory(this.category);
        float gridX = itemsX + 9.0f;
        float gridY = itemsY + 60.0f;
        float gridW = itemsW - 18.0f;
        float cell = 52.0f;
        float gap = 6.0f;
        int cols = Math.max(1, (int)((gridW + gap) / (cell + gap)));
        float scroll = (float)this.itemScroll.getValue();
        for (int i = 0; i < items.size(); i++) {
            float cx = gridX + (i % cols) * (cell + gap);
            float itemY = gridY + (i / cols) * (cell + gap) - scroll;
            if (GuiUtility.isHovered(cx, itemY, cell, cell, mouseX, mouseY)) {
                this.commitInputs();
                this.selected = items.get(i);
                this.loadSelectedToFields();
                if (button == MouseButton.RIGHT) this.selected.setBuyEnabled(!this.selected.isBuyEnabled());
                this.module.saveAutoBuyConfig();
                ClientSounds.CLICKGUI_OPEN.play(0.25f, 1.15f);
                return;
            }
        }

        if (this.selected != null) {
            float dx = x + WIDTH - DETAILS - PAD;
            float dy = y + PAD;
            if (GuiUtility.isHovered(dx + 12.0f, dy + 96.0f, DETAILS - 24.0f, 20.0f, mouseX, mouseY)) this.selected.setBuyEnabled(!this.selected.isBuyEnabled());
            else if (GuiUtility.isHovered(dx + 12.0f, dy + 120.0f, DETAILS - 24.0f, 20.0f, mouseX, mouseY)) this.selected.setSellEnabled(!this.selected.isSellEnabled());
            else if (GuiUtility.isHovered(dx + 12.0f, dy + 144.0f, DETAILS - 24.0f, 20.0f, mouseX, mouseY)) this.selected.setSetupEnabled(!this.selected.isSetupEnabled());
            else if (GuiUtility.isHovered(dx + 12.0f, dy + 252.0f, DETAILS - 24.0f, 20.0f, mouseX, mouseY)) this.selected.setThornsMode(nextThorns(this.selected.getThornsMode()));
            this.module.saveAutoBuyConfig();
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float x = ((float)this.width - WIDTH) / 2.0f;
        float y = ((float)this.height - HEIGHT) / 2.0f;
        float itemsX = x + SIDEBAR + PAD * 2.0f;
        float itemsY = y + PAD + 60.0f;
        float itemsW = WIDTH - SIDEBAR - DETAILS - PAD * 4.0f;
        float itemsH = HEIGHT - PAD * 2.0f - 70.0f;
        if (GuiUtility.isHovered(itemsX, itemsY, itemsW, itemsH, mouseX, mouseY)) {
            this.itemScroll.scroll(verticalAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        this.buyField.onKeyPressed(keyCode, scanCode, modifiers);
        this.sellField.onKeyPressed(keyCode, scanCode, modifiers);
        this.durabilityField.onKeyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.commitInputs();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean typed = false;
        if (Character.isDigit(chr)) {
            typed |= this.buyField.charTyped(chr, modifiers);
            typed |= this.sellField.charTyped(chr, modifiers);
            typed |= this.durabilityField.charTyped(chr, modifiers);
        }
        return typed || super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        this.commitInputs();
        this.module.saveAutoBuyConfig();
        this.closing = true;
        if (this.menuAnimation.getValue() <= 0.01f) super.close();
    }

    private void loadSelectedToFields() {
        if (this.selected == null) return;
        this.setField(this.buyField, String.valueOf(this.selected.getMaxBuyPrice()));
        this.setField(this.sellField, String.valueOf(this.selected.getMinSellPrice()));
        this.setField(this.durabilityField, String.valueOf(this.selected.getMinDurabilityPercent()));
    }

    private void setField(TextField field, String value) {
        field.clear();
        field.paste(value == null ? "" : value);
        field.setFocused(false);
    }

    private void commitInputs() {
        if (this.selected == null) return;
        String buy = this.buyField.getBuiltText();
        String sell = this.sellField.getBuiltText();
        String durability = this.durabilityField.getBuiltText();
        if (!buy.isBlank()) this.selected.setMaxBuyPrice(parse(buy));
        if (!sell.isBlank()) this.selected.setMinSellPrice(parse(sell));
        if (!durability.isBlank()) this.selected.setMinDurabilityPercent((int)parse(durability));
        this.module.saveAutoBuyConfig();
    }

    private AutoBuyItem.ThornsMode nextThorns(AutoBuyItem.ThornsMode mode) {
        return switch (mode) {
            case ANY -> AutoBuyItem.ThornsMode.ONLY;
            case ONLY -> AutoBuyItem.ThornsMode.NONE;
            case NONE -> AutoBuyItem.ThornsMode.ANY;
        };
    }

    private long parse(String value) {
        try { return Long.parseLong(value.replaceAll("[^0-9]", "")); } catch (Exception ignored) { return 0L; }
    }

    private void handleMovementKeys() {
        KeyBinding[] movementKeys;
        if (mc.player == null || this.isTyping()) return;
        long windowHandle = mc.getWindow().getHandle();
        for (KeyBinding key : movementKeys = new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey}) {
            int keyCode = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
        }
        if (mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
        }
    }

    private boolean isTyping() {
        return mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
