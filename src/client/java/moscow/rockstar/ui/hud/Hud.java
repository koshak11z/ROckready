/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.event.impl.window.ChatKeyPressEvent;
import moscow.rockstar.systems.event.impl.window.ChatReleaseEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Nametags;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.hud.Grid;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudHistoryManager;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.ui.hud.impl.ArmorHud;
import moscow.rockstar.ui.hud.impl.Effects;
import moscow.rockstar.ui.hud.impl.KeyBinds;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.ui.hud.impl.VanillaHudElement;
import moscow.rockstar.ui.hud.impl.Watermark;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.inline.impl.PlayerElement;
import moscow.rockstar.ui.hud.inline.impl.WorldElement;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class Hud
implements IMinecraft,
IScaledResolution {
    private final List<HudElement> elements = new ArrayList<HudElement>();
    private final List<Popup> popups = new ArrayList<Popup>();
    public DynamicIsland island;
    private final HudHistoryManager historyManager = new HudHistoryManager();
    private final Grid grid = new Grid();
    private String desc = "";
    private AnimatedText descText;
    private final Timer timer = new Timer();
    private final EventListener<HudRenderEvent> onHud = event -> {
        UIContext context = UIContext.of(event.getContext(), Hud.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(), Hud.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        if (this.descText == null) {
            this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0f), 10.0f, 300L, Easing.BAKEK).centered();
        }
        this.desc = "";
        this.grid.draw(context);
        this.grid.update();
        for (HudElement element : this.elements) {
            element.render(context);
            if (!(element.getSelecting().getValue() >= 0.0f)) continue;
            float anim = element.getAnimation().getValue() * element.getVisible().getValue();
            float scale = 0.5f + anim * 0.5f - 0.05f * element.getSelecting().getValue();
            element.getLoadingAnim().setDuration(1500L);
            element.getLoadingAnim().update(1.0f);
            if (element.getLoadingAnim().getValue() == 1.0f) {
                element.getLoadingAnim().setValue(0.0f);
            }
            RenderUtility.scale(context.getMatrices(), element.getX() + element.getWidth() / 2.0f, element.getY() + element.getHeight() / 2.0f, scale);
            context.drawLoadingRect(element.getX(), element.getY(), element.getWidth(), element instanceof HudList ? Math.max(20.0f, element.getHeight()) : element.getHeight(), element.getLoadingAnim().getValue() * 2.2f - 0.5f, BorderRadius.all(element instanceof DynamicIsland ? 7.0f : 6.0f), ColorRGBA.WHITE.withAlpha(100.0f * element.getSelecting().getValue()));
            RenderUtility.end(context.getMatrices());
        }
        this.descText.pos(sr.getScaledWidth() / 2.0f, 30.0f);
        if (!this.desc.contains(".description")) {
            this.descText.update(this.desc);
            this.descText.render(context);
        }
        for (Popup popup2 : this.popups) {
            if (Hud.mc.currentScreen instanceof ChatScreen) continue;
            popup2.setShowing(false);
        }
        if (!(Hud.mc.currentScreen instanceof ChatScreen)) {
            CursorUtility.set(CursorType.DEFAULT);
        }
        this.popups.removeIf(popup -> popup.getAnimation().getValue() == 0.0f && !popup.isShowing());
    };
    private final EventListener<ChatRenderEvent> onPostHud = event -> {
        UIContext context = UIContext.of(event.getContext(), Hud.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(), Hud.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 2000.0f);
        for (Popup popup : this.popups) {
            if (popup.getY() + popup.getHeight() > sr.getScaledHeight()) {
                popup.setY(sr.getScaledHeight() - 10.0f - popup.getHeight());
            }
            popup.render(context);
        }
        context.getMatrices().pop();
    };
    private final EventListener<ChatKeyPressEvent> onKeyPress = event -> {
        int modifiers = event.getModifiers();
        int keyCode = event.getKeyCode();
        if (keyCode == 90 && (modifiers & 2) != 0) {
            Rockstar.getInstance().getHud().getHistoryManager().undo();
            return;
        }
        if (keyCode == 89 && (modifiers & 2) != 0) {
            Rockstar.getInstance().getHud().getHistoryManager().redo();
            return;
        }
    };
    private final EventListener<ChatClickEvent> onClick = event -> {
        for (Entity entity : Rockstar.getInstance().getModuleManager().getModule(Nametags.class).getEntityList()) {
            Vec3d pos = Utils.getInterpolatedPos(entity, 1.0f).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.PLAYER || !this.handleClick((ChatClickEvent)event, entity, screenPos)) continue;
            return;
        }
        for (Popup popup : this.popups) {
            popup.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if (popup.isHovered(event.getX(), event.getY())) {
                return;
            }
            popup.setShowing(false);
        }
        for (HudElement element : this.elements) {
            element.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if ((!element.isHovered(event.getX(), event.getY()) || !element.isShowing()) && !element.isDragging()) continue;
            return;
        }
        if (event.getButton() == 1 && !this.disabledElements().isEmpty()) {
            Popup popup = new Popup(event.getX(), event.getY(), 90.0f, 6.0f).title(Localizator.translate("whatadd")).separator();
            for (HudElement element : this.disabledElements()) {
                popup.button(Localizator.translate(element.getName()), element.getIcon(), popup1 -> {
                    element.pos(event.getX(), event.getY());
                    element.setShowing(true);
                    popup1.setShowing(false);
                    Rockstar.getInstance().getFileManager().writeFile("client");
                });
            }
            this.popups.add(popup);
        } else if (event.getButton() == 1 && this.disabledElements().isEmpty() && this.timer.finished(600L)) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 \u043d\u0435\u0442", "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0438\u0441\u044c, \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435\u0447\u0435\u0433\u043e");
            this.timer.reset();
        }
    };
    private final EventListener<ChatReleaseEvent> onRelease = event -> {
        for (Popup popup : this.popups) {
            popup.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if (!popup.isHovered(event.getX(), event.getY())) continue;
            return;
        }
        for (HudElement element : this.elements) {
            element.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
        }
    };

    @CompileBytecode
    private void initialize() {
        Rockstar.getInstance().getEventManager().subscribe(this);
        this.island = new DynamicIsland();
        // Dynamic Island and PlayerElement are intentionally NOT added to the HUD (disabled by request).
        // island is still constructed because other systems (e.g. BossBarHud mixin) read getIsland().
        this.elements.addAll(List.of(new Watermark(), new Effects(), new KeyBinds(), new TargetHud(), new ArmorHud(), new WorldElement()));
        // Only scoreboard/bossbar/hotbar are movable. Health/hunger/xp are left fully vanilla
        // (wrapping their renders caused them to disappear at random).
        for (VanillaHudElement.Type type : new VanillaHudElement.Type[]{
                VanillaHudElement.Type.SCOREBOARD, VanillaHudElement.Type.BOSSBAR, VanillaHudElement.Type.HOTBAR}) {
            this.elements.add(new VanillaHudElement(type));
        }
    }

    public Hud() {
        this.initialize();
    }

    public List<HudElement> enabledElements() {
        return this.elements.stream().filter(HudElement::isShowing).toList();
    }

    public List<HudElement> disabledElements() {
        return this.elements.stream().filter(element -> !element.isShowing()).toList();
    }

    public <T extends HudElement> T getElementByName(String name) {
        return (T)((HudElement)this.elements.stream().filter(element -> element.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    private boolean handleClick(ChatClickEvent event, Entity entity, Vec2f screenPos) {
        float distance = entity.distanceTo((Entity)Hud.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        Text displayName = Nametags.displayName(entity);
        float textWidth = Fonts.MEDIUM.getFont(11.0f).width(displayName);
        float textHeight = Fonts.MEDIUM.getFont(11.0f).height();
        float rectWidth = textWidth + 5.0f;
        float rectHeight = textHeight + 6.0f;
        float rectOffsetX = -textWidth / 2.0f - 3.0f;
        float rectOffsetY = 2.0f;
        float scaledRectWidth = rectWidth * scale;
        float scaledRectHeight = rectHeight * scale;
        float scaledRectX = screenPos.x + rectOffsetX * scale;
        float scaledRectY = screenPos.y + rectOffsetY * scale;
        return GuiUtility.isHovered((double)scaledRectX, (double)scaledRectY, (double)scaledRectWidth, (double)scaledRectHeight, event.getX(), event.getY());
    }

    @Generated
    public List<HudElement> getElements() {
        return this.elements;
    }

    @Generated
    public List<Popup> getPopups() {
        return this.popups;
    }

    @Generated
    public DynamicIsland getIsland() {
        return this.island;
    }

    @Generated
    public HudHistoryManager getHistoryManager() {
        return this.historyManager;
    }

    @Generated
    public Grid getGrid() {
        return this.grid;
    }

    @Generated
    public String getDesc() {
        return this.desc;
    }

    @Generated
    public AnimatedText getDescText() {
        return this.descText;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public EventListener<HudRenderEvent> getOnHud() {
        return this.onHud;
    }

    @Generated
    public EventListener<ChatRenderEvent> getOnPostHud() {
        return this.onPostHud;
    }

    @Generated
    public EventListener<ChatKeyPressEvent> getOnKeyPress() {
        return this.onKeyPress;
    }

    @Generated
    public EventListener<ChatClickEvent> getOnClick() {
        return this.onClick;
    }

    @Generated
    public EventListener<ChatReleaseEvent> getOnRelease() {
        return this.onRelease;
    }

    @Generated
    public void setDesc(String desc) {
        this.desc = desc;
    }
}

