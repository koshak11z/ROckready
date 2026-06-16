package im.zov4ik.utils.features.price;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HolyWorldPriceParser extends SpookyTimePriceParser {
    private static final Pattern UNIT_PRICE_PATTERN = Pattern.compile("(?iu)^[^\\p{L}\\p{N}]*цена\\s*за\\s*1\\s*(?:ед\\.?|единицу|шт\\.?|штуку)?\\s*[:\\-]?\\s*([0-9\\s,.]+)");
    private static final Pattern TOTAL_PRICE_PATTERN = Pattern.compile("(?iu)^[^\\p{L}\\p{N}]*цена\\s*[:\\-]?\\s*([0-9\\s,.]+)");

    public int getUnitPrice(ItemStack stack) {
        int parsed = parseLorePrice(stack, UNIT_PRICE_PATTERN);
        if (parsed > 0) {
            return parsed;
        }
        return super.getUnitPrice(stack);
    }

    @Override
    public int getPrice(ItemStack stack) {
        int parsed = parseLorePrice(stack, TOTAL_PRICE_PATTERN);
        if (parsed > 0) {
            return parsed;
        }
        return super.getPrice(stack);
    }

    private int parseLorePrice(ItemStack stack, Pattern pattern) {
        var lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return -1;
        }

        for (Text line : lore.lines()) {
            Matcher matcher = pattern.matcher(line.getString());
            if (!matcher.find()) {
                continue;
            }

            String digits = matcher.group(1).replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                continue;
            }

            try {
                return Integer.parseInt(digits);
            } catch (NumberFormatException ignored) {
            }
        }

        return -1;
    }
}
