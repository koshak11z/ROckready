/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.commands.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class InventoryCommand
implements IMinecraft {
    private final Map<String, Map<Integer, Integer>> inventories = new HashMap<String, Map<Integer, Integer>>();

    @Compile
    public Command command() {
        return CommandBuilder.begin("inv").aliases("inventory", "slot", "\u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c").desc("commands.inventory.description").param("action", p -> p.literal("save", "create", "add", "\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "load", "use", "\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c")).param("name", p -> p.optional().validator(text -> text.length() < 2 ? ValidationResult.error("commands.prefix.invalid_length") : ValidationResult.ok(text))).handler(this::handle).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().get(0);
        String name = (String)ctx.arguments().get(1);
        if (action.equals("save") || action.equals("create") || action.equals("add") || action.equals("\u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c")) {
            this.saveInventory(name);
            MessageUtility.info(Text.of((String)Localizator.translate("commands.inventory.saved", name)));
        } else if (action.equals("load") || action.equals("use") || action.equals("\u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c")) {
            this.loadInventory(name);
        } else {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.inventory.invalid_action")));
        }
    }

    @Compile
    private void saveInventory(String name) {
        if (InventoryCommand.mc.player == null) {
            return;
        }
        HashMap<Integer, Integer> inventory = new HashMap<Integer, Integer>();
        for (int i = 0; i <= 45; ++i) {
            ItemStack stack = InventoryCommand.mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            inventory.put(i, Item.getRawId((Item)stack.getItem()));
        }
        this.inventories.put(name, inventory);
    }

    private void loadInventory(String name) {
        if (!this.inventories.containsKey(name)) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.inventory.not_found", name)));
            return;
        }
        Map<Integer, Integer> savedInventory = this.inventories.get(name);
        boolean anyItemRendered = false;
        for (Map.Entry<Integer, Integer> entry : savedInventory.entrySet()) {
            int slotIndex = entry.getKey();
            Item item = Item.byRawId((int)entry.getValue());
            ItemStack ghostStack = new ItemStack((ItemConvertible)item);
            ghostStack.setCount(1);
            InventoryCommand.mc.player.currentScreenHandler.getSlot(slotIndex).setStack(ghostStack);
            anyItemRendered = true;
        }
        if (anyItemRendered) {
            MessageUtility.info(Text.of((String)Localizator.translate("commands.inventory.loaded")));
        } else {
            MessageUtility.error(Text.of((String)Localizator.translate("commands.inventory.empty")));
        }
    }

    @Compile
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Map<Integer, Integer>> entry : this.inventories.entrySet()) {
            JsonObject innerJson = new JsonObject();
            for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                innerJson.addProperty(innerEntry.getKey().toString(), (Number)innerEntry.getValue());
            }
            jsonObject.add(entry.getKey(), (JsonElement)innerJson);
        }
        return jsonObject;
    }

    @Compile
    public void load(JsonElement jsonElement) {
        this.inventories.clear();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry entry : jsonObject.entrySet()) {
            JsonObject innerJson = ((JsonElement)entry.getValue()).getAsJsonObject();
            HashMap<Integer, Integer> innerMap = new HashMap<Integer, Integer>();
            for (Map.Entry innerEntry : innerJson.entrySet()) {
                Integer intKey = Integer.valueOf((String)innerEntry.getKey());
                Integer value = ((JsonElement)innerEntry.getValue()).getAsInt();
                innerMap.put(intKey, value);
            }
            this.inventories.put((String)entry.getKey(), innerMap);
        }
    }
}

