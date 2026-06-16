package im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuyItemSettings;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;

@Getter
@Setter
public class NbtDefinedItem implements AutoBuyableItem {
    private final String displayName;
    private final String searchName;
    private final ItemStack template;
    private final int price;
    private final boolean additionalCheck;
    private final AutoBuyItemSettings settings;
    private boolean enabled;

    public NbtDefinedItem(String displayName, String searchName, ItemStack template, int price, boolean additionalCheck) {
        this.displayName = displayName;
        this.searchName = searchName;
        this.template = template.copy();
        this.price = price;
        this.additionalCheck = additionalCheck;
        this.enabled = true;
        this.settings = new AutoBuyItemSettings(price, template.getItem(), displayName);
        AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
    }

    public NbtDefinedItem(String displayName, ItemStack template, int price, boolean additionalCheck) {
        this(displayName, displayName, template, price, additionalCheck);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSearchName() {
        return searchName;
    }

    @Override
    public ItemStack createItemStack() {
        return template.copy();
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public AutoBuyItemSettings getSettings() {
        return settings;
    }

    @Override
    public boolean needsAdditionalCheck() {
        return additionalCheck;
    }
}
