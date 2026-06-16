package im.zov4ik.features.impl.misc.autobuy.catalog.originalitems;

import im.zov4ik.features.impl.misc.autobuy.catalog.items.AutoBuyableItem;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.DonatorProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.HolyWorldProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.SpookyTimeProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.ManualProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.util.krushprovider.KrushProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.MiscProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.PotionProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.SphereProvider;
import im.zov4ik.features.impl.misc.autobuy.catalog.items.list.TalismanProvider;

import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static List<AutoBuyableItem> allItems = null;

    public static List<AutoBuyableItem> getAllItems() {
        if (allItems == null) {
            allItems = new ArrayList<>();
            allItems.addAll(getKrush());
            allItems.addAll(getTalismans());
            allItems.addAll(getSpheres());
            allItems.addAll(getMisc());
            allItems.addAll(getDonator());
            allItems.addAll(getPotions());
            allItems.addAll(getHolyWorld());
        }
        return allItems;
    }

    public static void reload() {
        allItems = null;
        HolyWorldProvider.reload();
        SpookyTimeProvider.reload();
    }

    public static List<AutoBuyableItem> getKrush() {
        return KrushProvider.getKrush();
    }

    public static List<AutoBuyableItem> getTalismans() {
        return TalismanProvider.getTalismans();
    }

    public static List<AutoBuyableItem> getSpheres() {
        return SphereProvider.getSpheres();
    }

    public static List<AutoBuyableItem> getMisc() {
        return MiscProvider.getMisc();
    }

    public static List<AutoBuyableItem> getDonator() {
        return DonatorProvider.getDonator();
    }

    public static List<AutoBuyableItem> getPotions() {
        return PotionProvider.getPotions();
    }

    public static List<AutoBuyableItem> getHolyWorld() {
        List<AutoBuyableItem> items = new ArrayList<>();
        items.addAll(HolyWorldProvider.getItems());
        return items;
    }

    public static List<AutoBuyableItem> getSpookyTime() {
        List<AutoBuyableItem> items = new ArrayList<>();
        items.addAll(ManualProvider.getBaseItems());
        items.addAll(ManualProvider.getSpookyNbtItems());
        return items;
    }

    
    public static List<AutoBuyableItem> getFunTimeItems() {
        List<AutoBuyableItem> items = new ArrayList<>();
        items.addAll(ManualProvider.getBaseItems());
        items.addAll(KrushProvider.getKrush());
        items.addAll(TalismanProvider.getTalismans());
        items.addAll(SphereProvider.getSpheres());
        items.addAll(MiscProvider.getMisc());
        items.addAll(DonatorProvider.getDonator());
        items.addAll(PotionProvider.getPotions());
        return items;
    }
}
