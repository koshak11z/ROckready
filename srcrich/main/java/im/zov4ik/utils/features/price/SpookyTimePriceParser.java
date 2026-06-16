package im.zov4ik.utils.features.price;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpookyTimePriceParser extends PriceParser {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d[\\d\\s,.]*)");

    public int getUnitPrice(ItemStack stack) {
        int totalPrice = getPrice(stack);
        if (totalPrice <= 0) {
            return -1;
        }
        return Math.max(1, totalPrice / Math.max(1, stack.getCount()));
    }

    @Override
    public int getPrice(ItemStack stack) {
        int parsed = super.getPrice(stack);
        if (parsed > 0) {
            return parsed;
        }

        var lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return -1;
        }

        for (Text line : lore.lines()) {
            String text = line.getString();
            Matcher matcher = NUMBER_PATTERN.matcher(text);
            String last = null;
            while (matcher.find()) {
                last = matcher.group(1);
            }
            if (last != null && !last.isBlank()) {
                try {
                    return Integer.parseInt(last.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return -1;
    }
}
