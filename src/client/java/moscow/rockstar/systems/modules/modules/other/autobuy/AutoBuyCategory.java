package moscow.rockstar.systems.modules.modules.other.autobuy;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum AutoBuyCategory {
    ALL("Все", Items.CHEST, 0xFFF4F5F7),
    SPHERES("Сферы", Items.PLAYER_HEAD, 0xFFF06C78),
    ARMOR("Броня", Items.ELYTRA, 0xFFF5B75E),
    WEAPONS("Оружие", Items.NETHERITE_SWORD, 0xFFE869A1),
    TOOLS("Инструм.", Items.NETHERITE_PICKAXE, 0xFF73D1FF),
    EXPERIENCE("Опыт", Items.EXPERIENCE_BOTTLE, 0xFF63E6BE),
    BLOCKS("Блоки", Items.TNT, 0xFFFF8E72),
    ALCHEMY("Алхимия", Items.POTION, 0xFF84A7FF),
    FOOD("Еда", Items.GOLDEN_APPLE, 0xFFF3D35C),
    MISC("Разное", Items.COMPASS, 0xFFB693FF);

    private final String name;
    private final Item icon;
    private final int color;

    AutoBuyCategory(String name, Item icon, int color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public String getName() { return this.name; }
    public Item getIcon() { return this.icon; }
    public int getColor() { return this.color; }
}
