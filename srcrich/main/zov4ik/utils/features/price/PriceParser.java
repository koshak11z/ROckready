package im.zov4ik.utils.features.price;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceParser {
    // Matches digits with optional space/comma thousands separators and optional .xx cents
    // e.g. "130,000" / "1 500" / "1500" / "1,500.00"
    private final Pattern numberPattern = Pattern.compile("([\\d][\\d\\s,]*(?:\\.\\d{1,2})?)");

    // Fallback: matches "$130,000" or "$ 130 000" in item name
    private final Pattern namePattern = Pattern.compile("\\$\\s*([\\d][\\d\\s,]*(?:\\.\\d{1,2})?)");

    public int getPrice(ItemStack stack) {
        // --- 1. Try component string (lore / display NBT) ---
        ComponentMap tag = stack.getComponents();
        if (tag != null) {
            String componentString = tag.toString();

            // Format: "literal{ $ Цена: 130,000}[style={color=green}]"
            // We grab everything between "literal{ $" and the closing "}"
            String raw = StringUtils.substringBetween(componentString, "literal{ $", "}");
            if (raw != null && !raw.isEmpty()) {
                int parsed = extractNumber(raw);
                if (parsed >= 0) return parsed;
            }

            // Broader fallback: scan whole component string for "Цена:" followed by a number
            Matcher cenaM = Pattern.compile("(?:Цена|Price|Цена:)[:\\s]+([\\d][\\d\\s,]*)").matcher(componentString);
            if (cenaM.find()) {
                int parsed = parseClean(cenaM.group(1));
                if (parsed >= 0) return parsed;
            }
        }

        // --- 2. Try item display name ---
        String customName = stack.getName().getString();
        if (customName != null && !customName.isEmpty()) {
            // "$130,000" or "$ 130 000"
            Matcher m = namePattern.matcher(customName);
            if (m.find()) {
                int parsed = parseClean(m.group(1));
                if (parsed >= 0) return parsed;
            }
            // "Цена: 130,000" without dollar sign
            Matcher cena = Pattern.compile("(?:Цена|Price)[:\\s]+([\\d][\\d\\s,]*)").matcher(customName);
            if (cena.find()) {
                int parsed = parseClean(cena.group(1));
                if (parsed >= 0) return parsed;
            }
        }

        return -1;
    }

    /** Extract the first number found anywhere in a raw string. */
    private int extractNumber(String raw) {
        Matcher m = numberPattern.matcher(raw);
        while (m.find()) {
            int v = parseClean(m.group(1));
            if (v >= 0) return v;
        }
        return -1;
    }

    /** Strip formatting chars and parse to int. */
    private int parseClean(String s) {
        if (s == null || s.isEmpty()) return -1;
        try {
            // Remove spaces, commas, dots used as thousands separators
            String clean = s.replaceAll("[\\s,]", "")
                    .replaceAll("\\.\\d+$", ""); // drop cents
            return Integer.parseInt(clean.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}