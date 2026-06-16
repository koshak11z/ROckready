package im.zov4ik.features.impl.misc.autobuy;

import net.minecraft.item.Items;

public enum AutoBuyCategory {
    ALL("\u0412\u0441\u0435", "", Items.CHEST, 0xFFF4F5F7),
    SPHERES("\u0421\u0444\u0435\u0440\u044b", "", Items.PLAYER_HEAD, 0xFFF06C78),
    ARMOR("\u0411\u0440\u043e\u043d\u044f", "", Items.ELYTRA, 0xFFF5B75E),
    WEAPONS("\u041e\u0440\u0443\u0436\u0438\u0435", "", Items.NETHERITE_SWORD, 0xFFE869A1),
    TOOLS("\u0418\u043d\u0441\u0442\u0440\u0443\u043c.", "", Items.NETHERITE_PICKAXE, 0xFF73D1FF),
    EXPERIENCE("\u041e\u043f\u044b\u0442", "", Items.EXPERIENCE_BOTTLE, 0xFF63E6BE),
    BLOCKS("\u0411\u043b\u043e\u043a\u0438", "", Items.TNT, 0xFFFF8E72),
    ALCHEMY("\u0410\u043b\u0445\u0438\u043c\u0438\u044f", "", Items.POTION, 0xFF84A7FF),
    FOOD("\u0415\u0434\u0430", "", Items.GOLDEN_APPLE, 0xFFF3D35C),
    MISC("\u0420\u0430\u0437\u043d\u043e\u0435", "", Items.COMPASS, 0xFFB693FF);

    private final String name;
    private final String description;
    private final net.minecraft.item.Item icon;
    private final int color;

    AutoBuyCategory(String name, String description, net.minecraft.item.Item icon, int color) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public net.minecraft.item.Item getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }
}
