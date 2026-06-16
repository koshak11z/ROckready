package im.zov4ik.features.impl.misc.autobuy.matcher;

import im.zov4ik.features.impl.misc.autobuy.catalog.util.AuctionUtils;
import net.minecraft.item.ItemStack;

public final class ZOVAuctionMatcher {
    private ZOVAuctionMatcher() {
    }

    public static boolean compare(ItemStack candidate, ItemStack template) {
        if (candidate == null || template == null || candidate.isEmpty() || template.isEmpty()) {
            return false;
        }
        return AuctionUtils.compareItem(candidate, template);
    }
}
