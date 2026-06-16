package im.zov4ik.features.impl.misc.autobuy.catalog.items.customitem;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuyItemSettings;
import im.zov4ik.features.impl.misc.autobuy.catalog.settings.AutoBuySettingsManager;

@Getter
@Setter
public class SimpleItem implements AutoBuyableItem {
    private final Item material;
    private final int price;
    private final String displayName;
    private final AutoBuyItemSettings settings;
    private boolean enabled;

    public SimpleItem(Item material, int price) {
        this.material = material;
        this.price = price;
        this.displayName = new ItemStack(material).getName().getString();
        this.enabled = true;
        this.settings = new AutoBuyItemSettings(price, material, displayName);
        AutoBuySettingsManager.getInstance().loadSettings(displayName, this.settings);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSearchName() {
        return displayName;
    }

    @Override
    public ItemStack createItemStack() {
        return new ItemStack(material);
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
}
