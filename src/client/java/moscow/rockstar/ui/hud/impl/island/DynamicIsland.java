/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screen.ChatScreen
 */
package moscow.rockstar.ui.hud.impl.island;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.impl.island.ExtandableStatus;
import moscow.rockstar.ui.hud.impl.island.IslandSize;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.ui.hud.impl.island.impl.BlinkStatus;
import moscow.rockstar.ui.hud.impl.island.impl.DefaultStatus;
import moscow.rockstar.ui.hud.impl.island.impl.EventStatus;
import moscow.rockstar.ui.hud.impl.island.impl.MineStatus;
import moscow.rockstar.ui.hud.impl.island.impl.MusicStatus;
import moscow.rockstar.ui.hud.impl.island.impl.NotificationStatus;
import moscow.rockstar.ui.hud.impl.island.impl.PVPStatus;
import moscow.rockstar.ui.hud.impl.island.impl.XrayStatus;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.ChatScreen;

public class DynamicIsland
extends HudElement
implements IMinecraft,
IScaledResolution {
    private final SelectSetting statuses = new SelectSetting(this, "hud.dynamic_island.statuses").draggable();
    private final IslandSize size = new IslandSize(48.0f, 15.0f);
    private boolean extended;
    private final Animation extendingAnim = new Animation(200L, 0.0f, Easing.LINEAR);
    private final Animation widthAnim = new Animation(500L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation heightAnim = new Animation(500L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation showPing = new Animation(500L, 0.0f, Easing.BAKEK);
    private final ColorAnimation backgroundColor = new ColorAnimation(300L, new ColorRGBA(0.0f, 0.0f, 0.0f), Easing.FIGMA_EASE_IN_OUT);
    private final ColorAnimation adaptColor = new ColorAnimation(300L, new ColorRGBA(255.0f, 255.0f, 255.0f), Easing.LINEAR);
    private final Timer timer = new Timer();
    private boolean dark;
    private boolean useDark;
    private IslandStatus last;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (!(this.active() instanceof MusicStatus) || DynamicIsland.mc.player.age % 2 == 0) {
            // empty if block
        }
    };

    public DynamicIsland() {
        super("hud.dynamic_island", "icons/hud/island.png");
        new NotificationStatus(this.statuses);
        new BlinkStatus(this.statuses);
        new PVPStatus(this.statuses);
        new EventStatus(this.statuses);
        new MineStatus(this.statuses);
        new XrayStatus(this.statuses);
        new MusicStatus(this.statuses);
        new DefaultStatus(this.statuses).alwaysEnabled();
        Rockstar.getInstance().getFileManager().readFile("client");
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    @Override
    protected void renderComponent(UIContext context) {
        this.width = this.size.width;
        this.height = this.size.height;
        this.x = sr.getScaledWidth() / 2.0f - this.width / 2.0f;
        this.y = 7.0f;
        BorderRadius radius = BorderRadius.all(7.0f + 11.0f * this.extendingAnim.getValue());
        String time = TextUtility.getCurrentTime();
        if (this.timer.finished(500L)) {
            boolean check;
            float pixelY;
            float pixelX = (float)mc.getWindow().getWidth() / 2.0f;
            ColorRGBA pixel = ColorRGBA.fromPixel(pixelX, pixelY = (float)mc.getWindow().getHeight() - (this.y + 5.0f));
            this.useDark = check = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3.0f > 70.0f;
            this.timer.reset();
        }
        this.adaptColor.update(this.useDark ? new ColorRGBA(0.0f, 0.0f, 0.0f) : new ColorRGBA(255.0f, 255.0f, 255.0f));
        this.dark = this.useDark;
        ColorRGBA elmtColor = this.adaptColor.getColor().withAlpha(255.0f * (1.0f - this.extendingAnim.getValue()));
        if (DynamicIsland.mc.player != null) {
            context.drawText(Fonts.MEDIUM.getFont(7.0f), time, this.x - Fonts.MEDIUM.getFont(9.0f).width(time) - 4.0f, this.y + 5.0f, elmtColor);
            if (mc.isInSingleplayer() || DynamicIsland.mc.player.networkHandler.getPlayerListEntry(DynamicIsland.mc.player.getUuid()) == null) {
                context.drawTexture(Rockstar.id("icons/airplane.png"), this.x + this.width + 8.0f, this.y + 3.5f, 8.0f, 8.0f, elmtColor);
            } else {
                int i;
                this.showPing.update(GuiUtility.isHovered(this.x + this.width + 4.0f + 4.0f * this.showPing.getValue(), this.y + 5.0f, 12.8f, 7.0, context));
                int ping = DynamicIsland.mc.player.networkHandler.getPlayerListEntry(DynamicIsland.mc.player.getUuid()).getLatency();
                int[] pings = new int[]{450, 300, 150, 75};
                context.drawText(Fonts.MEDIUM.getFont(7.0f), ping + " ms", this.x + this.width + 4.0f + 4.0f * this.showPing.getValue(), this.y + 5.0f, elmtColor.mulAlpha(this.showPing.getValue()));
                for (i = 0; i < 4; ++i) {
                    context.drawRoundedRect(this.x + this.width + 9.0f + (float)i * 2.7f + 4.0f * this.showPing.getValue(), this.y + 8.0f - (float)i, 2.0f, (float)(3 + i), BorderRadius.all(0.1f), elmtColor.withAlpha(elmtColor.getAlpha() * 0.2f * (1.0f - this.showPing.getValue())));
                }
                for (i = 0; i < 4; ++i) {
                    if (ping >= pings[i]) continue;
                    context.drawRoundedRect(this.x + this.width + 9.0f + (float)i * 2.7f + 4.0f * this.showPing.getValue(), this.y + 8.0f - (float)i, 2.0f, (float)(3 + i), BorderRadius.all(0.1f), elmtColor.mulAlpha(1.0f - this.showPing.getValue()));
                }
            }
        }
        IslandStatus status = this.active();
        this.backgroundColor.update(status.getColor());
        context.drawSquircle(this.x - 1.0f, this.y - 1.0f, this.width + 2.0f, this.height + 2.0f, 2.0f + 5.0f * this.extendingAnim.getValue(), BorderRadius.all(8.0f + 11.0f * this.extendingAnim.getValue()), Colors.WHITE.withAlpha(this.adaptColor.getColor().getRed() * 0.1f));
        if (Interface.blurHudEnabled()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 2.0f + 2.0f * this.extendingAnim.getValue(), radius, ColorRGBA.WHITE.withAlpha(255.0f));
        }
        context.drawSquircle(this.x, this.y, this.width, this.height, 2.0f + 5.0f * this.extendingAnim.getValue(), radius, this.backgroundColor.getColor().withAlpha(216.75f));
        for (SelectSetting.Value islandStatus : this.statuses.getValues()) {
            ((IslandStatus)islandStatus).getAnimation().update(status == islandStatus ? 1.0f : 0.0f);
        }
        ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
        if (status.getAnimation().getValue() == 1.0f) {
            status.draw(context);
        } else {
            for (SelectSetting.Value islandStatus : this.statuses.getValues()) {
                if (!(((IslandStatus)islandStatus).getAnimation().getValue() > 0.0f)) continue;
                ((IslandStatus)islandStatus).drawWithAlpha(context);
            }
        }
        ScissorUtility.pop();
        this.widthAnim.update(status.size.width);
        this.heightAnim.update(status.size.height);
        this.size.width = this.widthAnim.getValue();
        this.size.height = this.heightAnim.getValue();
        this.extendingAnim.update(this.extended ? 1.0f : 0.0f);
        if (!(status instanceof ExtandableStatus)) {
            this.extended = false;
        }
        if (!(DynamicIsland.mc.currentScreen instanceof ChatScreen) && !(DynamicIsland.mc.currentScreen instanceof DropDownScreen) && DynamicIsland.mc.player != null || this.select) {
            this.extended = false;
        }
        if (!this.extended && status instanceof ExtandableStatus && GuiUtility.isHovered((double)this.x, (double)this.y, (double)this.width, (double)this.height, GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
            CursorUtility.set(CursorType.HAND);
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        super.onMouseClicked(mouseX, mouseY, button);
        this.handleClick((float)mouseX, (float)mouseY, button.getButtonIndex());
    }

    public boolean handleClick(float mouseX, float mouseY, int button) {
        float x = sr.getScaledWidth() / 2.0f - this.size.width / 2.0f;
        float y = 7.0f;
        if (this.extended) {
            if (!GuiUtility.isHovered((double)x, (double)y, (double)this.size.width, (double)this.size.height, mouseX, mouseY)) {
                this.extended = false;
            } else {
                this.active().click(mouseX, mouseY, button);
            }
            return true;
        }
        if (GuiUtility.isHovered((double)x, (double)y, (double)this.size.width, (double)this.size.height, mouseX, mouseY) && this.active() instanceof ExtandableStatus) {
            this.extended = true;
            return true;
        }
        return false;
    }

    public IslandStatus active() {
        return this.statuses().getLast();
    }

    public List<IslandStatus> statuses() {
        return this.statuses.getValues().stream().filter(islandStatus -> ((IslandStatus)islandStatus).canShow() && islandStatus.isSelected()).map(islandStatus -> (IslandStatus)islandStatus).toList().reversed();
    }

    @Generated
    public SelectSetting getStatuses() {
        return this.statuses;
    }

    @Generated
    public IslandSize getSize() {
        return this.size;
    }

    @Generated
    public boolean isExtended() {
        return this.extended;
    }

    @Generated
    public Animation getExtendingAnim() {
        return this.extendingAnim;
    }

    @Override
    @Generated
    public Animation getWidthAnim() {
        return this.widthAnim;
    }

    @Override
    @Generated
    public Animation getHeightAnim() {
        return this.heightAnim;
    }

    @Generated
    public Animation getShowPing() {
        return this.showPing;
    }

    @Generated
    public ColorAnimation getBackgroundColor() {
        return this.backgroundColor;
    }

    @Generated
    public ColorAnimation getAdaptColor() {
        return this.adaptColor;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public boolean isDark() {
        return this.dark;
    }

    @Generated
    public boolean isUseDark() {
        return this.useDark;
    }

    @Generated
    public IslandStatus getLast() {
        return this.last;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getOnTick() {
        return this.onTick;
    }
}

