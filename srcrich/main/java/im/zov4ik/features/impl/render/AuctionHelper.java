package im.zov4ik.features.impl.render;

import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.ColorSetting;
import im.zov4ik.utils.client.Instance;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionHelper extends Module {

    private final ColorSetting cheapSlotColor = new ColorSetting("Цвет дешевого", "Цвет для самого дешевого предмета.")
            .setColor(0x8C40FF40);
    private final ColorSetting goodSlotColor = new ColorSetting("Цвет выгодного", "Цвет для самого выгодного предмета.")
            .setColor(0x8CFFFF40);

    private Slot lowSumSlotId    = null;
    private Slot lowAllSumSlotId = null;
    private boolean isAuc        = false;

    private static final Pattern PRICE_DOLLAR = Pattern.compile("\\$\\s*([0-9][0-9\\s,]*)");
    private static final Pattern PRICE_LABEL  = Pattern.compile("(?iu)\\$?\\s*цен[аa]\\s*:?\\s*\\$?\\s*([0-9][0-9\\s,]*)");

    public AuctionHelper() {
        super("AuctionHelper", "Помогает найти самые оптимальные и дешевые и выгодные предметы по команде /ah на SpookyTime/FunTime", ModuleCategory.RENDER);
        setup(cheapSlotColor, goodSlotColor);
    }

    public static AuctionHelper getInstance() {
        return Instance.get(AuctionHelper.class);
    }

    public void tick(ScreenHandler handler) {
        if (!isState()) return;

        if (!this.isAuc) {
            this.isAuc = isAuction(handler);
        }

        if (!this.isAuc) return;

        int lowSum = Integer.MAX_VALUE;
        int allSum = Integer.MAX_VALUE;

        for (int i = 0; i < 44; ++i) {
            Slot slot = handler.slots.get(i);
            if (!slot.getStack().isEmpty()) {
                int sum = getPrice(slot);
                if (sum < lowSum) {
                    this.lowSumSlotId = slot;
                    lowSum = sum;
                }

                int perItem = sum / slot.getStack().getCount();
                if (perItem < allSum) {
                    allSum = perItem;
                    this.lowAllSumSlotId = slot;
                }
            }
        }
    }

    public void renderSlot(DrawContext context, Slot slot) {
        if (!isState()) return;
        if (slot == this.lowSumSlotId) {
            renderCheat(context, slot);
        } else if (slot == this.lowAllSumSlotId) {
            renderGood(context, slot);
        }
    }

    public void reset() {
        this.isAuc        = false;
        this.lowSumSlotId    = null;
        this.lowAllSumSlotId = null;
    }

    public void renderCheat(DrawContext context, Slot slot) {
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, this.cheapSlotColor.getColor());
    }

    public void renderGood(DrawContext context, Slot slot) {
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, this.goodSlotColor.getColor());
    }

    private int getPrice(Slot slot) {
        int price = parsePrice(slot.getStack());
        return price >= 0 ? price : Integer.MAX_VALUE;
    }

    private int parsePrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;

        List<Integer> prices = new ArrayList<>();

        var lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                collectPrices(prices, line.getString());
            }
        }

        if (prices.isEmpty()) {
            collectPrices(prices, stack.getName().getString());
        }

        if (prices.isEmpty()) {
            String raw = stack.getComponents().toString();
            int start = raw.indexOf("literal{ $");
            if (start >= 0) {
                int end = raw.indexOf("}", start + 10);
                if (end > start) collectPrices(prices, raw.substring(start + 10, end));
            }
            if (prices.isEmpty()) collectPrices(prices, raw);
        }

        if (prices.isEmpty()) return -1;
        return prices.stream().max(Integer::compareTo).orElse(-1);
    }

    private void collectPrices(List<Integer> out, String line) {
        if (line == null || line.isEmpty()) return;
        String clean = line.replaceAll("§.", "");

        Matcher m = PRICE_LABEL.matcher(clean);
        while (m.find()) parseAndAdd(out, m.group(1));

        m = PRICE_DOLLAR.matcher(clean);
        while (m.find()) parseAndAdd(out, m.group(1));
    }

    private void parseAndAdd(List<Integer> out, String raw) {
        if (raw == null || raw.isBlank()) return;
        try {
            out.add(Integer.parseInt(raw.replaceAll("[\\s,]", "")));
        } catch (NumberFormatException ignored) {}
    }

    private boolean isAuction(ScreenHandler handler) {
        return handler != null
                && handler.slots.size() == 90
                && handler.getSlot(49).getStack().getItem() == Items.NETHER_STAR;
    }
}