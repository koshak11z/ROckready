package moscow.rockstar.systems.modules.modules.other.autobuy;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class AutoBuyItem {
    public enum ThornsMode { ANY, ONLY, NONE }

    private final String server;
    private final String key;
    private final String displayName;
    private final String searchName;
    private final AutoBuyCategory category;
    private final ItemStack iconStack;
    private final boolean needsAdditionalCheck;
    private long maxBuyPrice;
    private long minSellPrice;
    private int minDurabilityPercent;
    private ThornsMode thornsMode = ThornsMode.ANY;
    private boolean buyEnabled;
    private boolean sellEnabled;
    private boolean setupEnabled;

    public AutoBuyItem(String key, String displayName, String searchName, AutoBuyCategory category, ItemStack iconStack, long maxBuyPrice, boolean needsAdditionalCheck) {
        this("Universal", key, displayName, searchName, category, iconStack, maxBuyPrice, needsAdditionalCheck);
    }

    public AutoBuyItem(String server, String key, String displayName, String searchName, AutoBuyCategory category, ItemStack iconStack, long maxBuyPrice, boolean needsAdditionalCheck) {
        this.server = server == null || server.isBlank() ? "Universal" : server;
        this.key = key;
        this.displayName = displayName;
        this.searchName = searchName;
        this.category = category;
        this.iconStack = iconStack.copy();
        this.maxBuyPrice = maxBuyPrice;
        this.minSellPrice = Math.max(1L, Math.round(maxBuyPrice * 1.5D));
        this.needsAdditionalCheck = needsAdditionalCheck;
    }

    public boolean matches(ItemStack stack, List<Text> tooltip) {
        if (stack == null || stack.isEmpty()) return false;
        if (this.isAnyShulker()) {
            if (!(stack.getItem() instanceof BlockItem blockItem) || !blockItem.getBlock().getName().getString().toLowerCase(Locale.ROOT).contains("shulker")) return false;
        } else if (stack.getItem() != this.iconStack.getItem()) {
            return false;
        }
        if (!this.needsAdditionalCheck && !this.namesMatch(stack)) return false;
        if (this.supportsDurability() && this.minDurabilityPercent > 0 && stack.isDamageable() && stack.getMaxDamage() > 0) {
            int remaining = stack.getMaxDamage() - stack.getDamage();
            int percent = Math.round(remaining * 100.0f / stack.getMaxDamage());
            if (percent < this.minDurabilityPercent) return false;
        }
        if (this.supportsThornsMode() && this.thornsMode != ThornsMode.ANY) {
            boolean hasThorns = this.hasThorns(stack, tooltip);
            if (this.thornsMode == ThornsMode.ONLY && !hasThorns) return false;
            if (this.thornsMode == ThornsMode.NONE && hasThorns) return false;
        }
        return true;
    }

    private boolean namesMatch(ItemStack stack) {
        String templateName = normalizeName(this.iconStack.getName().getString());
        String defaultName = normalizeName(new ItemStack(this.iconStack.getItem()).getName().getString());
        if (templateName.isBlank() || templateName.equals(defaultName)) return true;
        return normalizeName(stack.getName().getString()).contains(templateName) || templateName.contains(normalizeName(stack.getName().getString()));
    }

    private boolean hasThorns(ItemStack stack, List<Text> tooltip) {
        for (Text line : tooltip) {
            String s = line.getString().toLowerCase(Locale.ROOT);
            if (s.contains("thorns") || s.contains("шип")) return true;
        }
        return false;
    }

    public boolean supportsDurability() { return this.iconStack.isDamageable() && this.iconStack.getMaxDamage() > 0; }
    public boolean supportsThornsMode() { return this.iconStack.getItem() instanceof ArmorItem; }
    public boolean isAnyShulker() { return this.key.toLowerCase(Locale.ROOT).contains("shulker_any"); }
    public ItemStack getIconStack() { return this.iconStack.copy(); }
    public String getServer() { return this.server; }
    public String getKey() { return this.key; }
    public String getDisplayName() { return this.displayName; }
    public String getSearchName() { return this.searchName; }
    public AutoBuyCategory getCategory() { return this.category; }
    public long getMaxBuyPrice() { return this.maxBuyPrice; }
    public void setMaxBuyPrice(long maxBuyPrice) { this.maxBuyPrice = Math.max(0L, maxBuyPrice); }
    public long getMinSellPrice() { return this.minSellPrice; }
    public void setMinSellPrice(long minSellPrice) { this.minSellPrice = Math.max(0L, minSellPrice); }
    public int getMinDurabilityPercent() { return this.minDurabilityPercent; }
    public void setMinDurabilityPercent(int minDurabilityPercent) { this.minDurabilityPercent = Math.max(0, Math.min(100, minDurabilityPercent)); }
    public ThornsMode getThornsMode() { return this.thornsMode; }
    public void setThornsMode(ThornsMode thornsMode) { this.thornsMode = thornsMode == null ? ThornsMode.ANY : thornsMode; }
    public boolean isBuyEnabled() { return this.buyEnabled; }
    public void setBuyEnabled(boolean buyEnabled) { this.buyEnabled = buyEnabled; }
    public boolean isSellEnabled() { return this.sellEnabled; }
    public void setSellEnabled(boolean sellEnabled) { this.sellEnabled = sellEnabled; }
    public boolean isSetupEnabled() { return this.setupEnabled; }
    public void setSetupEnabled(boolean setupEnabled) { this.setupEnabled = setupEnabled; }

    private static String normalizeName(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[★☆✦✧✩✪✫✬✭✮✯✰✴✵✶✷✸✹✺✻✼✽✾❂❃❇❈❉❊❋⭐🌟✨]", "").replaceAll("§.", "").trim();
    }
}
