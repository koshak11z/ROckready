package im.zov4ik.features.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.lwjgl.glfw.GLFW;
import im.zov4ik.display.hud.Notifications;
import im.zov4ik.events.keyboard.KeyEvent;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.BindSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.utils.math.time.StopWatch;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NBTParser extends Module {
    BindSetting copyBind = new BindSetting("Copy NBT", "Copy held item NBT to clipboard").setKey(GLFW.GLFW_KEY_O);
    StopWatch actionWatch = new StopWatch();

    public NBTParser() {
        super("NBTParser", ModuleCategory.MISC);
        setup(copyBind);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (!e.isKeyDown(copyBind.getKey(), true) || mc.player == null) {
            return;
        }
        if (!actionWatch.finished(150)) {
            return;
        }
        actionWatch.reset();

        ItemStack stack = mc.player.getMainHandStack();
        if (stack == null || stack.isEmpty()) {
            Notifications.getInstance().addList("NBTParser: нет предмета в руке", 2500);
            return;
        }

        String nbtText = serializeStack(stack);
        if (nbtText.isBlank()) {
            Notifications.getInstance().addList("NBTParser: не удалось получить NBT", 2500);
            return;
        }

        GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), nbtText);
        Notifications.getInstance().addList("NBTParser: NBT скопирован", 2500);
    }

    private String serializeStack(ItemStack stack) {
        RegistryWrapper.WrapperLookup registry = mc.world != null ? mc.world.getRegistryManager() : null;
        try {
            if (registry != null) {
                NbtElement element = stack.toNbt(registry);
                if (element != null) {
                    String text = element.toString();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            var ops = registry != null ? RegistryOps.of(NbtOps.INSTANCE, registry) : NbtOps.INSTANCE;
            NbtElement encoded = ItemStack.CODEC.encodeStart(ops, stack).result().orElse(null);
            if (encoded != null) {
                String text = encoded.toString();
                if (!text.isBlank()) {
                    return text;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            String components = stack.getComponents().toString();
            if (components != null && !components.isBlank()) {
                return "{components:" + components + ",count:" + stack.getCount() + ",id:\"" + Registries.ITEM.getId(stack.getItem()) + "\"}";
            }
        } catch (Exception ignored) {
        }

        return "{id:\"" + Registries.ITEM.getId(stack.getItem()) + "\",count:" + stack.getCount() + "}";
    }
}
