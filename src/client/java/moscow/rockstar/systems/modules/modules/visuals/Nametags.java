/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.render.DiffuseLighting
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.brigadier.Message;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.friends.FriendManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.other.NameProtect;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.target.TargetManager;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.hud.Glyphs;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Name Tags", category=ModuleCategory.VISUALS, enabledByDefault=true, desc="\u0422\u0435\u0433\u0438, \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u044e\u0449\u0438\u0435 \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044e \u043e \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044f\u0445")
public class Nametags
extends BaseModule {
    private final List<Entity> entityList = new ArrayList<Entity>();
    private final BooleanSetting armor = new BooleanSetting(this, "modules.settings.name_tags.armor").enable();
    private final BooleanSetting offFriends = new BooleanSetting(this, "modules.settings.name_tags.offFriends");
    private final BooleanSetting items = new BooleanSetting(this, "modules.settings.name_tags.items");
    private final BooleanSetting backItems = new BooleanSetting((SettingsContainer)this, "modules.settings.name_tags.background", () -> !this.items.isEnabled());
    private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
        Vec2f screenPos;
        Vec3d pos;
        MatrixStack matrices = event.getContext().getMatrices();
        float tickDelta = event.getTickDelta();
        Vec3d cameraPos = Nametags.mc.gameRenderer.getCamera().getPos();
        this.entityList.clear();
        for (Entity entity : Nametags.mc.world.getEntities()) {
            if (entity == Nametags.mc.player || entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.ITEM) continue;
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                if (Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString()) && this.offFriends.isEnabled()) continue;
            }
            this.entityList.add(entity);
        }
        LinkedList<List<ItemEntity>> itemGroups = new LinkedList<List<ItemEntity>>();
        HashSet<ItemEntity> processedItems = new HashSet<ItemEntity>();
        for (Entity entity : this.entityList) {
            ItemEntity item;
            if (!(entity instanceof ItemEntity) || processedItems.contains(item = (ItemEntity)entity)) continue;
            List<ItemEntity> group = new LinkedList<ItemEntity>();
            for (Entity other : this.entityList) {
                ItemEntity otherItem;
                if (!(other instanceof ItemEntity) || processedItems.contains(otherItem = (ItemEntity)other) || !(item.squaredDistanceTo((Entity)otherItem) < 1.0)) continue;
                group.add(otherItem);
                processedItems.add(otherItem);
            }
            itemGroups.add(group);
        }
        RectBatching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
        this.drawBack((PreHudRenderEvent)event, (List<List<ItemEntity>>)itemGroups, tickDelta);
        ((Batching)rect).draw();
        for (Entity entity : this.entityList) {
            Vec3d pos2 = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos2 = Utils.worldToScreen(pos2);
            if (screenPos2 == null || entity.getType() != EntityType.PLAYER) continue;
            this.renderPlayerTag((PreHudRenderEvent)event, matrices, (PlayerEntity)entity, screenPos2, Pass.ITEMS);
        }
        for (Entity entity : this.entityList) {
            Vec3d pos2 = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos2 = Utils.worldToScreen(pos2);
            if (screenPos2 == null || entity.getType() != EntityType.ITEM) continue;
            this.renderShulkerDisplay((PreHudRenderEvent)event, matrices, (ItemEntity)entity, screenPos2);
        }
        DiffuseLighting.disableGuiDepthLighting();
        event.getContext().draw();
        FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        for (Entity entity : this.entityList) {
            pos = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.PLAYER) continue;
            this.renderPlayerTag((PreHudRenderEvent)event, matrices, (PlayerEntity)entity, screenPos, Pass.TEXT);
        }
        for (List<ItemEntity> group : itemGroups) {
            ItemEntity first = (ItemEntity)group.getFirst();
            Vec3d pos3 = Utils.getInterpolatedPos((Entity)first, tickDelta).add(0.0, first.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos3 = Utils.worldToScreen(pos3);
            if (screenPos3 == null || !this.items.isEnabled()) continue;
            if (group.size() > 1) {
                this.renderItemsText((PreHudRenderEvent)event, matrices, group, screenPos3);
                continue;
            }
            this.renderItemText((PreHudRenderEvent)event, matrices, first, screenPos3);
        }
        for (Entity entity : this.entityList) {
            pos = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.ITEM) continue;
            this.renderShulkerText((PreHudRenderEvent)event, matrices, (ItemEntity)entity, screenPos);
        }
        fontBatching.draw();
        if (!(Nametags.mc.currentScreen instanceof ChatScreen)) {
            this.active = null;
        }
    };
    private Popup active;
    private final EventListener<ChatRenderEvent> onRender = event -> {
        UIContext context = UIContext.of(event.getContext(), Nametags.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(), Nametags.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        if (this.active != null) {
            this.active.render(context);
        }
    };
    private final EventListener<ChatClickEvent> onClick = event -> {
        if (this.active != null) {
            this.active.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if (this.active.isHovered(event.getX(), event.getY())) {
                return;
            }
            this.active.setShowing(false);
        }
        for (Entity entity : this.entityList) {
            Vec3d pos = Utils.getInterpolatedPos(entity, 1.0f).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.PLAYER) continue;
            this.handleClick((ChatClickEvent)event, entity, screenPos);
        }
    };

    private void drawBack(PreHudRenderEvent event, List<List<ItemEntity>> itemGroups, float tickDelta) {
        Vec2f screenPos;
        Vec3d pos;
        MatrixStack matrices = event.getContext().getMatrices();
        for (Entity entity : this.entityList) {
            pos = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.PLAYER) continue;
            this.renderPlayerTag(event, matrices, (PlayerEntity)entity, screenPos, Pass.BACK);
        }
        for (List list : itemGroups) {
            ItemEntity first = (ItemEntity)list.getFirst();
            Vec3d pos2 = Utils.getInterpolatedPos((Entity)first, tickDelta).add(0.0, first.getBoundingBox().getLengthY() + 0.5, 0.0);
            Vec2f screenPos2 = Utils.worldToScreen(pos2);
            if (screenPos2 == null || !this.backItems.isEnabled() || !this.items.isEnabled()) continue;
            if (list.size() > 1) {
                this.renderItemsBack(event, matrices, list, screenPos2);
                continue;
            }
            this.renderItemBack(event, matrices, first, screenPos2);
        }
        for (Entity entity : this.entityList) {
            pos = Utils.getInterpolatedPos(entity, tickDelta).add(0.0, entity.getBoundingBox().getLengthY() + 0.5, 0.0);
            screenPos = Utils.worldToScreen(pos);
            if (screenPos == null || entity.getType() != EntityType.ITEM) continue;
            this.renderShulkerBack(event, matrices, (ItemEntity)entity, screenPos);
        }
    }

    private void renderItemBack(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        String text = entity.getStack().getName().getString() + " " + entity.getStack().getCount() + "x";
        int textWidth = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
        int x = -textWidth / 2;
        int y = 5;
        event.getContext().drawRect(x - 3, y - 3, textWidth + 6, Fonts.MEDIUM.getFont(11.0f).height() + 6.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
        matrices.pop();
    }

    private void renderItemText(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        MutableText text = entity.getStack().getName().copy().append(" " + entity.getStack().getCount() + "x");
        int textWidth = (int)Fonts.MEDIUM.getFont(11.0f).width((Text)text);
        int x = -textWidth / 2;
        int y = 5;
        event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), (Text)text, x, y);
        matrices.pop();
    }

    private void renderItemsBack(PreHudRenderEvent event, MatrixStack matrices, List<ItemEntity> items, Vec2f screenPos) {
        if (items.isEmpty()) {
            return;
        }
        float distance = items.getFirst().distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        int maxWidth = 0;
        int textHeight = (int)Fonts.MEDIUM.getFont(11.0f).height();
        for (ItemEntity item : items) {
            String text = item.getStack().getName().getString() + " " + item.getStack().getCount() + "x";
            int w = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
            if (w <= maxWidth) continue;
            maxWidth = w;
        }
        int boxWidth = maxWidth + 6;
        int boxHeight = items.size() * textHeight + (items.size() - 1) * 2 + 6;
        event.getContext().drawRect((float)(-maxWidth) / 2.0f - 3.0f, 2.0f, boxWidth, boxHeight, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
        matrices.pop();
    }

    private void renderItemsText(PreHudRenderEvent event, MatrixStack matrices, List<ItemEntity> items, Vec2f screenPos) {
        if (items.isEmpty()) {
            return;
        }
        float distance = items.getFirst().distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        int textHeight = (int)Fonts.MEDIUM.getFont(11.0f).height();
        int maxWidth = 0;
        LinkedList<MutableText> lines = new LinkedList<MutableText>();
        for (ItemEntity item : items) {
            MutableText text = item.getStack().getName().copy().append(" " + item.getStack().getCount() + "x");
            lines.add(text);
            int w = (int)Fonts.MEDIUM.getFont(11.0f).width((Text)text);
            if (w <= maxWidth) continue;
            maxWidth = w;
        }
        int startX = -maxWidth / 2;
        for (int i = 0; i < lines.size(); ++i) {
            Text line = (Text)lines.get(i);
            int lineWidth = (int)Fonts.MEDIUM.getFont(11.0f).width(line);
            int x = startX + (maxWidth - lineWidth) / 2;
            int y = 5 + i * (textHeight + 2);
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), Text.of((Message)line), x, y);
        }
        matrices.pop();
    }

    private enum Pass {
        BACK,
        ITEMS,
        TEXT
    }

    private static final float PILL_H = 15.0f;

    private LinkedList<ItemStack> equipmentOf(PlayerEntity player) {
        LinkedList<ItemStack> items = new LinkedList<ItemStack>();
        items.add((ItemStack)player.getInventory().armor.get(3));
        items.add((ItemStack)player.getInventory().armor.get(2));
        items.add((ItemStack)player.getInventory().armor.get(1));
        items.add((ItemStack)player.getInventory().armor.get(0));
        items.add(player.getMainHandStack());
        items.add(player.getOffHandStack());
        items.removeIf(ItemStack::isEmpty);
        return items;
    }

    /**
     * Draws a player's nametag as a black rounded pill: [person] name(+clan) [HP HP] [armor/held/offhand].
     * Split into three passes so it slots into the existing batched render order (backgrounds, item icons, text).
     */
    private void renderPlayerTag(PreHudRenderEvent event, MatrixStack matrices, PlayerEntity player, Vec2f screenPos, Pass pass) {
        float distance = player.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);

        // NOTE: must stay in the Fonts.MEDIUM family — the TEXT pass runs inside a
        // FontBatching(Fonts.MEDIUM); mixing another atlas would sample wrong glyphs.
        Font font = Fonts.MEDIUM.getFont(9.0f);
        Font hpFont = Fonts.MEDIUM.getFont(8.0f);
        boolean friend = Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString());

        NameProtect np = Rockstar.getInstance().getModuleManager().getModule(NameProtect.class);
        String nameStr = np.isEnabled() ? np.patchName(player.getDisplayName().getString()) : player.getDisplayName().getString();
        int hpVal = (int)EntityUtility.getHealth(player);
        String hpStr = (hpVal == 1000 ? "?" : String.valueOf(hpVal)) + " HP";

        LinkedList<ItemStack> items = this.armor.isEnabled() ? this.equipmentOf(player) : new LinkedList<ItemStack>();

        float pad = 6.0f;
        float iconSz = 8.0f;
        float iconGap = 4.0f;
        float gap = 6.0f;
        float itemSlot = 11.0f;
        float nameW = font.width(nameStr);
        float hpW = hpFont.width(hpStr);
        float itemsW = items.isEmpty() ? 0.0f : (gap + items.size() * itemSlot - 1.0f);
        float pillW = pad + iconSz + iconGap + nameW + gap + hpW + itemsW + pad;

        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);

        float left = -pillW / 2.0f;
        float top = -1.0f;
        float cx = left + pad;
        float nameX = cx + iconSz + iconGap;
        float hpX = nameX + nameW + gap;
        float itemsX = hpX + hpW + gap;

        if (pass == Pass.BACK) {
            event.getContext().drawSquircle(left, top, pillW, PILL_H, 6.0f, BorderRadius.all(5.0f), new ColorRGBA(0.0f, 0.0f, 0.0f, 205.0f));
            event.getContext().drawRoundedBorder(left, top, pillW, PILL_H, 1.0f, BorderRadius.all(5.0f), (friend ? Colors.GREEN : Colors.getAccentColor()).mulAlpha(0.30f));
            Glyphs.person(event.getContext(), cx, top + (PILL_H - iconSz) / 2.0f, iconSz, Colors.getTextColor());
        } else if (pass == Pass.TEXT) {
            event.getContext().drawText(font, nameStr, nameX, top + (PILL_H - font.height()) / 2.0f - 0.5f, friend ? Colors.GREEN : ColorRGBA.WHITE);
            event.getContext().drawText(hpFont, hpStr, hpX, top + (PILL_H - hpFont.height()) / 2.0f - 0.5f, ColorRGBA.WHITE);
        } else {
            float ix = itemsX;
            float itemY = top + (PILL_H - 10.0f) / 2.0f;
            for (ItemStack item : items) {
                event.getContext().drawItem(item, ix, itemY, 0.62f);
                ix += itemSlot;
            }
        }
        matrices.pop();
    }

    private void renderShulkerBack(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
        List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
        if (items.isEmpty()) {
            return;
        }
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        int columns = Math.min(items.size(), 9);
        int rows = (int)Math.ceil((float)items.size() / 9.0f);
        int boxWidth = columns * 18 + 4;
        int boxHeight = rows * 18 + 4;
        event.getContext().drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight, new ColorRGBA(0.0f, 0.0f, 0.0f, 150.0f));
        matrices.pop();
    }

    private void renderShulkerDisplay(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
        List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
        if (items.isEmpty()) {
            return;
        }
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        int columns = Math.min(items.size(), 9);
        int rows = (int)Math.ceil((float)items.size() / 9.0f);
        int boxWidth = columns * 18 + 4;
        int boxHeight = rows * 18 + 4;
        for (int i = 0; i < items.size(); ++i) {
            ItemStack item = items.get(i);
            int x = i % 9 * 18 - boxWidth / 2 + 3;
            int y = i / 9 * 18 - boxHeight / 2 + 3;
            event.getContext().drawBatchItem(item, x, y);
        }
        matrices.pop();
    }

    private void renderShulkerText(PreHudRenderEvent event, MatrixStack matrices, ItemEntity entity, Vec2f screenPos) {
        List<ItemStack> items = ItemUtility.getItemsInShulker(entity.getStack());
        if (items.isEmpty()) {
            return;
        }
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        matrices.push();
        matrices.translate(screenPos.x, screenPos.y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        int columns = Math.min(items.size(), 9);
        int rows = (int)Math.ceil((float)items.size() / 9.0f);
        int boxWidth = columns * 18 + 4;
        int boxHeight = rows * 18 + 4;
        event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), entity.getDisplayName().getString(), (float)(-boxWidth / 2) + 0.5f, -boxHeight / 2 - 11, ColorRGBA.WHITE);
        for (int i = 0; i < items.size(); ++i) {
            ItemStack item = items.get(i);
            int x = i % 9 * 18 - boxWidth / 2 + 3;
            int y = i / 9 * 18 - boxHeight / 2 + 3;
            if (item.getCount() <= 1) continue;
            String count = String.valueOf(item.getCount());
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), count, (float)(x + 16) - Fonts.MEDIUM.getFont(11.0f).width(count), y + 9, ColorRGBA.WHITE);
        }
        matrices.pop();
    }

    public static Text displayName(Entity entity) {
        float f;
        if (entity.getDisplayName() == null) {
            return Text.empty();
        }
        NameProtect nameProtectModule = Rockstar.getInstance().getModuleManager().getModule(NameProtect.class);
        String displayName = nameProtectModule.isEnabled() ? nameProtectModule.patchName(entity.getDisplayName().getString()) : entity.getDisplayName().getString();
        MutableText text = Text.of((String)displayName).copy();
        if (!(entity instanceof LivingEntity)) {
            return text;
        }
        LivingEntity living = (LivingEntity)entity;
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            f = EntityUtility.getHealth(player);
        } else {
            f = living.getHealth();
        }
        int health = (int)f;
        if (!text.getString().endsWith(" ")) {
            text.append(" ");
        }
        return text.append((Text)Text.of((String)("[" + String.valueOf(health == 1000 ? "?" : Integer.valueOf(health)) + "]")).copy().withColor(-2142128));
    }

    private void handleClick(ChatClickEvent event, Entity entity, Vec2f screenPos) {
        float textHeight;
        float rectHeight;
        float scaledRectHeight;
        float rectWidth;
        float scaledRectWidth;
        float rectOffsetY;
        float scaledRectY;
        float distance = entity.distanceTo((Entity)Nametags.mc.player);
        float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
        Text displayName = Nametags.displayName(entity);
        float textWidth = Fonts.MEDIUM.getFont(11.0f).width(displayName);
        float rectOffsetX = -textWidth / 2.0f - 3.0f;
        float scaledRectX = screenPos.x + rectOffsetX * scale;
        if (GuiUtility.isHovered((double)scaledRectX, (double)(scaledRectY = screenPos.y + (rectOffsetY = 2.0f) * scale), (double)(scaledRectWidth = (rectWidth = textWidth + 5.0f) * scale), (double)(scaledRectHeight = (rectHeight = (textHeight = Fonts.MEDIUM.getFont(11.0f).height()) + 6.0f) * scale), event.getX(), event.getY())) {
            FriendManager friendManager = Rockstar.getInstance().getFriendManager();
            TargetManager targetManager = Rockstar.getInstance().getTargetManager();
            String name = entity.getName().getString();
            this.active = new Popup(event.getX(), event.getY(), 100.0f, 6.0f).title(name).separator().checkbox(Localizator.translate("friend"), friendManager.isFriend(name), toggled -> {
                if (toggled) {
                    friendManager.add(name);
                } else {
                    friendManager.remove(name);
                }
            }).checkbox(Localizator.translate("enemy"), targetManager.isTarget(name), toggled -> {
                if (toggled) {
                    targetManager.addTarget(name);
                } else {
                    targetManager.removeTarget(name);
                }
            }).button(Localizator.translate("copy"), "icons/hud/copy.png", popup -> {
                TextUtility.copyText(name);
                popup.setShowing(false);
            });
        }
    }

    @Override
    public void onDisable() {
        this.entityList.clear();
    }

    @Generated
    public List<Entity> getEntityList() {
        return this.entityList;
    }

    @Generated
    public BooleanSetting getArmor() {
        return this.armor;
    }

    @Generated
    public BooleanSetting getOffFriends() {
        return this.offFriends;
    }

    @Generated
    public BooleanSetting getItems() {
        return this.items;
    }

    @Generated
    public BooleanSetting getBackItems() {
        return this.backItems;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getOnHudRenderEvent() {
        return this.onHudRenderEvent;
    }

    @Generated
    public Popup getActive() {
        return this.active;
    }

    @Generated
    public EventListener<ChatRenderEvent> getOnRender() {
        return this.onRender;
    }

    @Generated
    public EventListener<ChatClickEvent> getOnClick() {
        return this.onClick;
    }
}

