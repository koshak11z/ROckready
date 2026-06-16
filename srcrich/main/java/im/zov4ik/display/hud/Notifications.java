package im.zov4ik.display.hud;

import im.zov4ik.common.animation.implement.OutBack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.client.sound.SoundManager;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.events.container.SetScreenEvent;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.features.impl.render.Hud;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Notifications extends AbstractDraggable {
    public static Notifications getInstance() {
        return Instance.getDraggable(Notifications.class);
    }

    private final List<Notification> list = new ArrayList<>();
    private final List<Stack> stacks = new ArrayList<>();

    public Notifications() {
        super("Notifications", 0, 0, 100, 15, false);
    }

    @Override
    public void tick() {
        list.forEach(notif -> {
            if (System.currentTimeMillis() > notif.removeTime || (notif.text.getString().contains("Hi I'm a notification") && !PlayerInteractionHelper.isChat(mc.currentScreen)))
                notif.anim.setDirection(Direction.BACKWARDS);
        });
        list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));
        while (!stacks.isEmpty()) {
            addTextIfNotEmpty(TypePickUp.INVENTORY, "Items raised: ");
            addTextIfNotEmpty(TypePickUp.SHULKER_INVENTORY, "Items placed in shulker: ");
            addTextIfNotEmpty(TypePickUp.SHULKER, "Raised shulker with: ");
        }
    }

    @Override
    public void packet(PacketEvent e) {
        if (!PlayerInteractionHelper.nullCheck()) switch (e.getPacket()) {
            case ItemPickupAnimationS2CPacket item when Hud.getInstance().notificationSettings.isSelected("Item Pick Up") && item.getCollectorEntityId() == Objects.requireNonNull(mc.player).getId() && Objects.requireNonNull(mc.world).getEntityById(item.getEntityId()) instanceof ItemEntity entity -> {
                ItemStack itemStack = entity.getStack();
                ContainerComponent component = itemStack.get(DataComponentTypes.CONTAINER);
                if (component == null) {
                    Text itemText = itemStack.getName();
                    if (itemText.getContent().toString().equals("empty")) {
                        MutableText text = Text.empty().append(itemText);
                        if (itemStack.getCount() > 1) text.append(Formatting.RESET + " [" + Formatting.RED + itemStack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
                        stacks.add(new Stack(TypePickUp.INVENTORY, text));
                    }
                } else component.stream().filter(s -> s.getName().getContent().toString().equals("empty")).forEach(stack -> {
                    MutableText text = Text.empty().append(stack.getName());
                    if (stack.getCount() > 1) text.append(Formatting.RESET + " [" + Formatting.RED + stack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
                    stacks.add(new Stack(TypePickUp.SHULKER, text));
                });
            }
            case ScreenHandlerSlotUpdateS2CPacket slot when Hud.getInstance().notificationSettings.isSelected("Item Pick Up") -> {
                int slotId = slot.getSlot();
                ContainerComponent updatedContainer = slot.getStack().get(DataComponentTypes.CONTAINER);
                if (updatedContainer != null && slotId < Objects.requireNonNull(mc.player).currentScreenHandler.slots.size() && slot.getSyncId() == 0) {
                    ContainerComponent currentContainer = mc.player.currentScreenHandler.getSlot(slotId).getStack().get(DataComponentTypes.CONTAINER);
                    if (currentContainer != null) updatedContainer.stream().filter(stack -> currentContainer.stream().noneMatch(s -> Objects.equals(s.getComponents(), stack.getComponents()) && s.toString().equals(stack.toString()))).forEach(stack -> {
                        MutableText text = Text.empty().append(stack.getName());
                        stacks.add(new Stack(TypePickUp.SHULKER_INVENTORY, text));
                    });
                }
            }
            default -> {}
        }
    }

    @Override
    public void setScreen(SetScreenEvent e) {
        if (e.getScreen() instanceof ChatScreen) {
            addList("Hi I'm a notification", 99999999);
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(13, Fonts.Type.REGULAR);

        int windowHeight = mc.getWindow().getScaledHeight();
        int windowWidth = mc.getWindow().getScaledWidth();
        float offsetY = 0.0F;
        float height = 14.0F;
        float padX = 8.5F;
        float iconSlot = 7.0F;
        float iconSize = 5.5F;
        float baseY = windowHeight - 74.0F;
        setHeight((int) height);
        for (Notification notification : list) {
            float anim = notification.anim.getOutput().floatValue();
            float textWidth = font.getStringWidth(notification.text);
            float width = Math.max(88.0F, padX * 2.0F + iconSlot + 4.0F + textWidth);
            float startY = Math.round(baseY - offsetY);
            float startX = Math.round(windowWidth / 2.0F - width / 2.0F);
            Calculate.setAlpha(anim, () -> {

                boolean danger = notification.text.getString().contains("disabled")
                        || notification.text.getString().contains("Сфера")
                        || notification.text.getString().contains("хоруса");
                int iconColor = danger ? HudTheme.DANGER : HudTheme.ACCENT;

                HudTheme.panel(matrix, startX, startY, width, height, 3.4F);
                HudTheme.iconSlot(context, HudTheme.ICON_BELL, startX + padX, startY + (height - iconSlot) / 2.0F, iconSlot, iconSize, iconColor);

                font.drawText(matrix, notification.text, (int) (startX + padX + iconSlot + 4.0F), startY + 4.55F);
                if (!notification.isExpired()) {
                    float progress;
                    long elapsed = System.currentTimeMillis() - notification.startTime;
                    long totalTime = notification.removeTime - notification.startTime;
                    progress = 1.0f - Math.min(1.0f, (float) elapsed / totalTime);
                    float progressWidth = (width - padX * 2.0F) * progress;
                    if (progressWidth > 0) {
                        HudTheme.accentBar(matrix, startX + padX, startY + height - 1.6F, progressWidth, 0.75F, 1.0F);
                    }
                }
            });
            offsetY += (height + 3.0F) * anim;
        }
    }

    private void addTextIfNotEmpty(TypePickUp type, String prefix) {
        MutableText text = Text.empty();
        List<Stack> list = stacks.stream().filter(stack -> stack.type.equals(type)).toList();
        for (int i = 0, size = list.size(); i < size; i++) {
            Stack stack = list.get(i);
            if (stack.type != type) continue;
            text.append(stack.text);
            stacks.remove(stack);
            if (text.getString().length() > 150) break;
            if (i + 1 != size) text.append(" , ");
        }
        if (!text.equals(Text.empty())) addList(Text.empty().append(prefix).append(text), 8000);
    }

    public void addList(String text, long removeTime) {
        addList(text, removeTime, null);
    }

    public void addList(Text text, long removeTime) {
        addList(text, removeTime, null);
    }

    public void addList(String text, long removeTime, SoundEvent sound) {
        addList(Text.empty().append(text), removeTime, sound);
    }

    public void addList(Text text, long removeTime, SoundEvent sound) {
        list.add(new Notification(text, new OutBack().setMs(400).setValue(1), System.currentTimeMillis(), System.currentTimeMillis() + removeTime));
        if (list.size() > 12) list.removeFirst();
        list.sort(Comparator.comparingDouble(notif -> -notif.removeTime));
        if (sound != null) SoundManager.playSound(sound);
    }

    public record Notification(Text text, Animation anim, long startTime, long removeTime) {
        public boolean isExpired() {
            return System.currentTimeMillis() > removeTime;
        }
    }

    public record Stack(TypePickUp type, MutableText text) {}

    public enum TypePickUp {
        INVENTORY, SHULKER, SHULKER_INVENTORY
    }
}
