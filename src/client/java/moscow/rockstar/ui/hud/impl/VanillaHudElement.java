package moscow.rockstar.ui.hud.impl;

import java.util.EnumMap;
import java.util.Map;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * A draggable handle that repositions a vanilla HUD element (hotbar / health / hunger / xp /
 * scoreboard / boss bar). The element itself is still drawn by Minecraft; the matching mixin
 * translates the vanilla render by {@link #offsetX}/{@link #offsetY}.
 *
 * <p>The persisted source of truth is the OFFSET from the vanilla anchor (resolution-independent),
 * not an absolute position. The live handle box is recomputed every frame as {@code anchor + offset}
 * so it always tracks the element even when the GUI scale, resolution or scoreboard size changes —
 * this is what keeps health/hunger/xp from "disappearing" after a resolution change and keeps the
 * scoreboard from drifting off-screen. The handle is only drawn while the HUD editor (chat) is open.
 */
public class VanillaHudElement extends HudElement {
    public enum Type {
        SCOREBOARD("hud.vanilla.scoreboard"),
        BOSSBAR("hud.vanilla.bossbar"),
        HOTBAR("hud.vanilla.hotbar"),
        HEALTH("hud.vanilla.health"),
        HUNGER("hud.vanilla.hunger"),
        XP("hud.vanilla.xp");

        final String key;

        Type(String key) {
            this.key = key;
        }
    }

    private static final Map<Type, VanillaHudElement> REGISTRY = new EnumMap<>(Type.class);

    private final Type type;
    // persisted drag offset from the vanilla anchor (resolution-independent)
    private float offX = 0.0f;
    private float offY = 0.0f;
    // live scoreboard size (adapts the SCOREBOARD handle to the real sidebar)
    private float sbW = 88.0f;
    private float sbH = 80.0f;

    public VanillaHudElement(Type type) {
        super(type.key, "icons/hud/drag.png");
        this.type = type;
        this.showing = true;
        REGISTRY.put(type, this);
    }

    // ─── anchor (vanilla default) + size, in scaled-GUI pixels ───
    private float anchorX() {
        float sw = IScaledResolution.sr.getScaledWidth();
        return switch (this.type) {
            case HOTBAR, HEALTH, XP, BOSSBAR -> sw / 2.0f - 91.0f;
            case HUNGER -> sw / 2.0f + 10.0f;
            case SCOREBOARD -> sw - this.sbW - 1.0f;
        };
    }

    private float anchorY() {
        float sh = IScaledResolution.sr.getScaledHeight();
        return switch (this.type) {
            case HOTBAR -> sh - 22.0f;
            case HEALTH, HUNGER -> sh - 39.0f;
            case XP -> sh - 29.0f;
            case BOSSBAR -> 12.0f;
            case SCOREBOARD -> sh / 2.0f - this.sbH / 2.0f;
        };
    }

    private float boxW() {
        return switch (this.type) {
            case HOTBAR, XP, BOSSBAR -> 182.0f;
            case HEALTH, HUNGER -> 81.0f;
            case SCOREBOARD -> this.sbW;
        };
    }

    private float boxH() {
        return switch (this.type) {
            case HOTBAR -> 22.0f;
            case HEALTH, HUNGER -> 10.0f;
            case XP -> 6.0f;
            case BOSSBAR -> 19.0f;
            case SCOREBOARD -> this.sbH;
        };
    }

    /** Adapt the SCOREBOARD handle to the live sidebar's real size (widest line incl. score). */
    private void computeScoreboardSize() {
        try {
            if (mc.world == null || mc.textRenderer == null) {
                return;
            }
            net.minecraft.scoreboard.Scoreboard sb = mc.world.getScoreboard();
            net.minecraft.scoreboard.ScoreboardObjective obj = sb.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
            if (obj == null) {
                return;
            }
            int lines = 0;
            float maxW = mc.textRenderer.getWidth(obj.getDisplayName());
            for (net.minecraft.scoreboard.ScoreboardEntry entry : sb.getScoreboardEntries(obj)) {
                if (entry.hidden()) {
                    continue;
                }
                lines++;
                float lineW = mc.textRenderer.getWidth(entry.name()) + mc.textRenderer.getWidth(" " + entry.value());
                maxW = Math.max(maxW, lineW);
            }
            this.sbH = (float) (lines + 1) * 9.0f + 1.0f;
            this.sbW = maxW + 2.0f;
        } catch (Exception ignored) {
            // keep last known size on any API hiccup
        }
    }

    /** Offset applied by the mixin: how far the element has been dragged from its vanilla anchor. */
    public static float offsetX(Type type) {
        VanillaHudElement e = REGISTRY.get(type);
        return e == null || !e.isShowing() ? 0.0f : e.offX;
    }

    public static float offsetY(Type type) {
        VanillaHudElement e = REGISTRY.get(type);
        return e == null || !e.isShowing() ? 0.0f : e.offY;
    }

    public float getOffX() {
        return this.offX;
    }

    public float getOffY() {
        return this.offY;
    }

    /** Restore persisted offsets (called from the config loader). */
    public void setOffsets(float offX, float offY) {
        this.offX = offX;
        this.offY = offY;
    }

    @Override
    public void update(UIContext context) {
        if (this.type == Type.SCOREBOARD) {
            this.computeScoreboardSize();
        }
        this.width = this.boxW();
        this.height = this.boxH();
        super.update(context);
        // While dragging the base class drives this.x/this.y from the mouse — read the offset back.
        // Otherwise lock the handle to anchor + offset so it always tracks the live element.
        if (this.isDragging()) {
            this.offX = this.x - this.anchorX();
            this.offY = this.y - this.anchorY();
        } else {
            this.x = this.anchorX() + this.offX;
            this.y = this.anchorY() + this.offY;
        }
    }

    @Override
    protected void renderComponent(UIContext context) {
        // only show the drag handle while editing the HUD (chat screen open)
        if (!(VanillaHudElement.mc.currentScreen instanceof ChatScreen)) {
            return;
        }
        ColorRGBA accent = Colors.getAccentColor();
        context.drawRoundedRect(this.x, this.y, this.width, this.height, BorderRadius.all(4.0f), accent.mulAlpha(0.12f));
        context.drawRoundedBorder(this.x, this.y, this.width, this.height, 1.0f, BorderRadius.all(4.0f), accent.mulAlpha(0.6f));
        Glyphs.diamond(context, this.x + 3.0f, this.y + 3.0f, 6.0f, accent);
        Font font = Fonts.MEDIUM.getFont(7.0f);
        context.drawCenteredText(font, moscow.rockstar.systems.localization.Localizator.translate(this.type.key),
                this.x + this.width / 2.0f, this.y + (this.height - font.height()) / 2.0f, ColorRGBA.WHITE.withAlpha(220.0f));
    }

    @Override
    public boolean show() {
        return VanillaHudElement.mc.currentScreen instanceof ChatScreen;
    }
}
