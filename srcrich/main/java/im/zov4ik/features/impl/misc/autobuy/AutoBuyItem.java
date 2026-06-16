package im.zov4ik.features.impl.misc.autobuy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.features.module.setting.implement.TextSetting;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils;
import im.zov4ik.features.impl.misc.autobuy.matcher.ZOVAuctionMatcher;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoBuyItem {
    private static final String THORNS_ANY = "\u041e\u0431\u0430";
    private static final String THORNS_ONLY = "\u0428\u0438\u043f\u044b";
    private static final String THORNS_NONE = "\u0410\u043d\u0442\u0438\u0448\u0438\u043f";
    private static final String PRICE_DISABLED = "\u043d\u0435 \u0432\u043a\u043b\u044e\u0447\u0451\u043d";
    private static final Pattern STAR_PATTERN = Pattern.compile("[\\u2605\\u2606\\u2726\\u2727\\u2729\\u272A\\u272B\\u272C\\u272D\\u272E\\u272F\\u2730\\u2734\\u2735\\u2736\\u2737\\u2738\\u2739\\u273A\\u273B\\u273C\\u273D\\u273E\\u2742\\u2743\\u2747\\u2748\\u2749\\u274A\\u274B\\u2B50\\uD83C\\uDF1F\\u2728]");

    String key;
    String displayName;
    String searchName;
    AutoBuyCategory category;
    ItemStack iconStack;
    TextSetting priceSetting;
    TextSetting durabilitySetting;
    SelectSetting thornsSetting;
    BooleanSetting sellEnabledSetting;
    BooleanSetting setupEnabledSetting;
    boolean needsAdditionalCheck;

    public AutoBuyItem(String key, String displayName, String searchName, AutoBuyCategory category, ItemStack iconStack, TextSetting priceSetting, TextSetting durabilitySetting, SelectSetting thornsSetting, BooleanSetting sellEnabledSetting, BooleanSetting setupEnabledSetting, boolean needsAdditionalCheck) {
        this.key = key;
        this.displayName = displayName;
        this.searchName = searchName;
        this.category = category;
        this.iconStack = iconStack.copy();
        this.priceSetting = priceSetting;
        this.durabilitySetting = durabilitySetting;
        this.thornsSetting = thornsSetting;
        this.sellEnabledSetting = sellEnabledSetting;
        this.setupEnabledSetting = setupEnabledSetting;
        this.needsAdditionalCheck = needsAdditionalCheck;
    }

    public String getRawPrice() {
        return normalizeDigits(priceSetting.getText());
    }

    public ItemStack getIconStack() {
        return iconStack.copy();
    }

    public void setRawPrice(String rawPrice) {
        priceSetting.setText(normalizeDigits(rawPrice));
    }

    public String getRawDurability() {
        return normalizeDigits(durabilitySetting.getText());
    }

    public void setRawDurability(String rawDurability) {
        durabilitySetting.setText(normalizeDigits(rawDurability));
    }

    public int getMinDurabilityPercent() {
        String raw = getRawDurability();
        if (raw.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(raw)));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public ThornsMode getThornsMode() {
        String selected = thornsSetting.getSelected();
        if (THORNS_ONLY.equalsIgnoreCase(selected)) {
            return ThornsMode.ONLY;
        }
        if (THORNS_NONE.equalsIgnoreCase(selected)) {
            return ThornsMode.NONE;
        }
        return ThornsMode.ANY;
    }

    public void setThornsMode(ThornsMode mode) {
        if (mode == ThornsMode.ONLY) {
            thornsSetting.setSelected(THORNS_ONLY);
        } else if (mode == ThornsMode.NONE) {
            thornsSetting.setSelected(THORNS_NONE);
        } else {
            thornsSetting.setSelected(THORNS_ANY);
        }
    }

    public boolean hasPrice() {
        String raw = getRawPrice();
        return !raw.isBlank() && getPriceValue() > 0;
    }

    public boolean isSellEnabled() {
        return sellEnabledSetting != null && sellEnabledSetting.isValue();
    }

    public void setSellEnabled(boolean enabled) {
        if (sellEnabledSetting != null) {
            sellEnabledSetting.setValue(enabled);
        }
    }

    public boolean isSetupEnabled() {
        return setupEnabledSetting != null && setupEnabledSetting.isValue();
    }

    public void setSetupEnabled(boolean enabled) {
        if (setupEnabledSetting != null) {
            setupEnabledSetting.setValue(enabled);
        }
    }

    public boolean supportsDurability() {
        return iconStack.isDamageable() && iconStack.getMaxDamage() > 0;
    }

    public boolean supportsThornsMode() {
        return AuctionUtils.isArmorItem(iconStack);
    }

    public long getPriceValue() {
        String raw = getRawPrice();
        if (raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    public String getFormattedPrice() {
        return formatPrice(getRawPrice());
    }

    public boolean matches(ItemStack stack, List<String> tooltipLines) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (isAnyShulker()) {
            if (!isShulkerBox(stack)) {
                return false;
            }
        } else if (stack.getItem() != iconStack.getItem()) {
            return false;
        }

        if (!needsAdditionalCheck && !namesMatch(stack)) {
            return false;
        }

        if (supportsDurability()) {
            int minDurability = getMinDurabilityPercent();
            if (minDurability > 0 && stack.isDamageable() && stack.getMaxDamage() > 0) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                int percent = Math.round(remaining * 100.0F / stack.getMaxDamage());
                if (percent < minDurability) {
                    return false;
                }
            }
        }

        if (supportsThornsMode()) {
            ThornsMode mode = getThornsMode();
            if (mode != ThornsMode.ANY) {
                boolean hasThorns = AuctionUtils.hasThornsEnchantment(stack);
                if (mode == ThornsMode.ONLY && !hasThorns) {
                    return false;
                }
                if (mode == ThornsMode.NONE && hasThorns) {
                    return false;
                }
            }
        }

        if (!needsAdditionalCheck) {
            return true;
        }

        return ZOVAuctionMatcher.compare(stack, iconStack);
    }

    private boolean namesMatch(ItemStack stack) {
        if (isAnyShulker()) {
            return true;
        }

        String templateName = normalizeName(iconStack.getName().getString());
        if (templateName.isBlank()) {
            return true;
        }

        String defaultName = normalizeName(new ItemStack(iconStack.getItem()).getName().getString());
        if (templateName.equals(defaultName)) {
            return true;
        }

        String candidateName = normalizeName(stack.getName().getString());
        if (candidateName.isBlank()) {
            return false;
        }

        return candidateName.contains(templateName) || templateName.contains(candidateName);
    }

    public enum ThornsMode {
        ANY,
        ONLY,
        NONE
    }

    public static String normalizeLine(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "")
                .replace('\u0451', '\u0435')
                .replace('\u0401', '\u0415')
                .replace('\u00A0', ' ')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String normalizeDigits(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^0-9]", "");
    }

    private static String normalizeName(String text) {
        String normalized = normalizeLine(text);
        normalized = STAR_PATTERN.matcher(normalized).replaceAll("");
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private boolean isAnyShulker() {
        String name = normalizeLine(displayName);
        return name.contains("\u0448\u0430\u043b\u043a\u0435\u0440") && name.contains("\u0432\u0441\u0435");
    }

    private boolean isShulkerBox(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        return id.contains("shulker_box");
    }

    public static String formatPrice(String digits) {
        String clean = normalizeDigits(digits);
        if (clean.isBlank()) {
            return PRICE_DISABLED;
        }

        StringBuilder builder = new StringBuilder(clean);
        for (int i = builder.length() - 3; i > 0; i -= 3) {
            builder.insert(i, ' ');
        }
        return builder.toString();
    }
}
