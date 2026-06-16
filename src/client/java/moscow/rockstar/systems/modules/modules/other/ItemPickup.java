/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.PickupEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@ModuleInfo(name="Item Pickup", category=ModuleCategory.OTHER, enabledByDefault=true, desc="\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u044f\u0435\u0442 \u0432\u0430\u0441 \u043f\u0440\u0438 \u043f\u043e\u0434\u043d\u044f\u0442\u0438\u0438 \u0434\u043e\u043d\u0430\u0442\u043d\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430")
public class ItemPickup
extends BaseModule {
    private final Map<String, String> don = Map.of("krush-helmet", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u0428\u043b\u0435\u043c \u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f!", "krush-chestplate", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a \u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f!", "krush-leggings", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u041f\u043e\u043d\u043e\u0436\u0438 \u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f!", "krush-boots", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u0411\u043e\u0442\u0438\u043d\u043a\u0438 \u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f!", "krush-sword", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u0434\u043e\u043d\u0430\u0442\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442: \u041c\u0435\u0447 \u043a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f");
    private final EventListener<PickupEvent> onPickupEvent = event -> {
        ItemStack stack = event.getItemStack();
        for (Map.Entry<String, String> entry : this.don.entrySet()) {
            if (!ItemUtility.checkDonItem(stack, entry.getKey())) continue;
            MessageUtility.info(Text.of((String)entry.getValue()));
            return;
        }
        if (ItemUtility.isDonItem(stack)) {
            String name = stack.getName().getString();
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "\u0414\u043e\u043d\u0430\u0442\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442", "\u0412\u044b \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b\u0438 \u0434\u043e\u043d\u0430\u0442\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442: " + name);
        }
    };
}

