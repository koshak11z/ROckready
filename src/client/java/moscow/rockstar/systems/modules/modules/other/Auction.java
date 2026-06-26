/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$TooltipContext
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.item.PotionItem
 *  net.minecraft.item.tooltip.TooltipType
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.text.Text
 *  net.minecraft.world.World
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.mixin.accessors.HandledScreenAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.event.impl.window.ContainerClickEvent;
import moscow.rockstar.systems.event.impl.window.ContainerReleaseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SelectSettingComponent;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@ModuleInfo(name="Auction", category=ModuleCategory.OTHER)
public class Auction
extends BaseModule {
    private final List<AuctionItem> pageItems = new ArrayList<AuctionItem>();
    private double averageEffectivePrice = 0.0;
    private double minEffectivePrice = Double.MAX_VALUE;
    private String title = "";
    private final SelectSetting armor = new SelectSetting((SettingsContainer)this, "modules.settings.auction.armor", () -> this.title.toLowerCase().contains("\u043a\u0438\u0440\u043a\u0430") || this.title.toLowerCase().contains("\u0441\u0438\u043b\u044b") || this.title.toLowerCase().contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438"));
    private final SelectSetting.Value noSpike = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_spike").select();
    private final SelectSetting.Value noProt5 = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_prot5").select();
    private final SelectSetting.Value noDurability = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_durability");
    private final SelectSetting.Value noRepair = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_repair");
    private final SelectSetting pickaxe = new SelectSetting((SettingsContainer)this, "modules.settings.auction.pickaxe", () -> this.title.toLowerCase().contains("\u0448\u043b\u0435\u043c") || this.title.toLowerCase().contains("\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a") || this.title.toLowerCase().contains("\u043f\u043e\u043d\u043e\u0436\u0438") || this.title.toLowerCase().contains("\u0431\u043e\u0442\u0438\u043d\u043a\u0438") || this.title.toLowerCase().contains("\u0431\u0440\u043e\u043d\u044f") || this.title.toLowerCase().contains("\u0441\u0438\u043b\u044b") || this.title.toLowerCase().contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438"));
    private final SelectSetting.Value noSilkTouch = new SelectSetting.Value(this.pickaxe, "modules.settings.auction.pickaxe.silk_touch");
    private final SelectSetting potions = new SelectSetting((SettingsContainer)this, "modules.settings.auction.potions", () -> this.title.toLowerCase().contains("\u0448\u043b\u0435\u043c") || this.title.toLowerCase().contains("\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a") || this.title.toLowerCase().contains("\u043f\u043e\u043d\u043e\u0436\u0438") || this.title.toLowerCase().contains("\u0431\u043e\u0442\u0438\u043d\u043a\u0438") || this.title.toLowerCase().contains("\u0431\u0440\u043e\u043d\u044f") || this.title.toLowerCase().contains("\u043a\u0438\u0440\u043a\u0430"));
    private final SelectSetting.Value noLevel3 = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_level3");
    private final SelectSetting.Value noCombined = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_combined");
    private final Popup popup = new Popup(0.0f, 0.0f);
    private final EventListener<HudRenderEvent> onHud = event -> {
        if (Auction.mc.currentScreen == null) {
            this.title = "";
            this.popup.setShowing(false);
            if (this.popup.getAnimation().getValue() > 0.0f) {
                this.drawPopup(event.getContext());
            }
        }
    };
    private final EventListener<ScreenRenderEvent> onScreen = event -> {
        Screen patt0$temp;
        if (this.pageItems.isEmpty() || !((patt0$temp = Auction.mc.currentScreen) instanceof HandledScreen)) {
            return;
        }
        HandledScreen screen = (HandledScreen)patt0$temp;
        if (!this.isAuction(screen.getTitle().getString())) {
            return;
        }
        this.popup.setShowing(true);
        HandledScreenAccessor accessor = (HandledScreenAccessor)screen;
        try {
            for (AuctionItem item : this.pageItems) {
                Slot slotToHighlight;
                if (item.effectivePrice > this.averageEffectivePrice || (slotToHighlight = screen.getScreenHandler().getSlot(item.slotId)) == null) continue;
                int x = accessor.getX() + slotToHighlight.x;
                int y = accessor.getY() + slotToHighlight.y;
                ColorRGBA color = this.calculateHighlightColor(item.effectivePrice);
                event.getContext().drawRoundedRect((float)x, (float)y, 16.0f, 16.0f, BorderRadius.all(1.0f), new VerticalGradient(color.withAlpha(0.0f), color.withAlpha(0.8f * color.getAlpha())));
            }
        }
        catch (Exception e) {
            this.reset();
        }
        this.drawPopup(event.getContext());
    };
    private final EventListener<ContainerClickEvent> onClick = event -> this.popup.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
    private final EventListener<ContainerReleaseEvent> onRelease = event -> this.popup.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));

    public Auction() {
        for (Setting setting : this.getSettings()) {
            if (!(setting instanceof SelectSetting)) continue;
            SelectSetting selectSetting = (SelectSetting)setting;
            this.popup.add(new SelectSettingComponent(selectSetting, (CustomComponent)this.popup));
        }
    }

    @Override
    public void tick() {
        String title;
        Screen screen = Auction.mc.currentScreen;
        if (!(screen instanceof HandledScreen)) {
            this.reset();
            return;
        }
        HandledScreen screen2 = (HandledScreen)screen;
        this.title = title = screen2.getTitle().getString();
        if (!this.isAuction(title)) {
            this.reset();
            return;
        }
        this.scanAndAnalyzePage(screen2);
        super.tick();
    }

    private void drawPopup(CustomDrawContext orig) {
        UIContext context = UIContext.of(orig, Auction.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(), Auction.mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        this.popup.setWidth(120.0f);
        this.popup.pos(10.0f, sr.getScaledHeight() / 2.0f - this.popup.getHeight() / 2.0f);
        this.popup.render(context);
    }

    private ColorRGBA calculateHighlightColor(double effectivePrice) {
        double range = this.averageEffectivePrice - this.minEffectivePrice;
        double factor = range > 0.0 ? (effectivePrice - this.minEffectivePrice) / range : 0.0;
        factor = Math.max(0.0, Math.min(1.0, factor));
        int red = (int)(60.0 + 195.0 * factor);
        return new ColorRGBA(factor < (double)0.001f ? (float)red : 255.0f, 255.0f, 60.0f, (float)(250.0 * (1.0 - factor)));
    }

    private boolean isAuction(String title) {
        return title.toLowerCase().contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || title.toLowerCase().contains("\u043f\u043e\u0438\u0441\u043a") || title.toLowerCase().contains("\u0431\u0438\u0440\u0436\u0430");
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private boolean shouldHideItem(ItemStack stack, List<Text> tooltip) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem) {
            if (this.noSpike.isSelected() && EnchantmentUtility.hasEnchantments(stack, Enchantments.THORNS)) {
                return true;
            }
            if (this.noProt5.isSelected() && EnchantmentUtility.getEnchantmentLevel(stack, (RegistryKey<Enchantment>)Enchantments.PROTECTION) < 5) {
                return true;
            }
            if (this.noDurability.isSelected() && stack.getMaxDamage() > 0 && stack.isDamaged()) {
                return true;
            }
            if (this.noRepair.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.MENDING)) {
                return true;
            }
        }
        if (item instanceof PickaxeItem && this.noSilkTouch.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.SILK_TOUCH)) {
            return true;
        }
        if (item instanceof PotionItem) {
            List<String> tooltipStrings = tooltip.stream().map(text -> text.getString().toLowerCase()).toList();
            if (this.noLevel3.isSelected() && !this.hasLevel3Potion(tooltipStrings)) {
                return true;
            }
            if (this.noCombined.isSelected() && !this.isCombinedPotion(tooltipStrings)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLevel3Potion(List<String> tooltip) {
        for (String line : tooltip) {
            if (!line.contains("\u0441\u0438\u043b\u0430") && !line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c") || !line.contains("iii") && !line.contains("3") && !line.contains("\u0443\u0441\u0438\u043b\u0435\u043d\u043d")) continue;
            return true;
        }
        return false;
    }

    private boolean isCombinedPotion(List<String> tooltip) {
        boolean hasStrength = tooltip.stream().anyMatch(line -> line.contains("\u0441\u0438\u043b\u0430"));
        boolean hasSpeed = tooltip.stream().anyMatch(line -> line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c"));
        return hasStrength && hasSpeed;
    }

    private void scanAndAnalyzePage(HandledScreen<?> screen) {
        this.pageItems.clear();
        this.minEffectivePrice = Double.MAX_VALUE;
        double totalEffectivePrice = 0.0;
        int pricedItemCount = 0;
        int containerSize = screen.getScreenHandler().slots.size() - 36;
        for (int i = 0; i < containerSize; ++i) {
            List<Text> tooltip;
            Slot slot = screen.getScreenHandler().getSlot(i);
            if (slot == null || !slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            if (this.shouldHideItem(stack, tooltip = stack.getTooltip(Item.TooltipContext.create((World)Auction.mc.world), (PlayerEntity)Auction.mc.player, (TooltipType)(Auction.mc.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC)))) continue;
            long totalPrice = this.parsePrice(tooltip);
            if (totalPrice <= 0L) continue;
            int count = stack.getCount();
            int maxDurability = stack.getMaxDamage();
            int currentDurability = maxDurability - stack.getDamage();
            double pricePerUnit = (double)totalPrice / (double)count;
            double durabilityFactor = 1.0;
            if (maxDurability > 0) {
                durabilityFactor = (double)currentDurability / (double)maxDurability;
                durabilityFactor = Math.max(0.1, durabilityFactor);
            }
            double effectivePrice = pricePerUnit / durabilityFactor;
            this.pageItems.add(new AuctionItem(slot.id, totalPrice, count, maxDurability, currentDurability, effectivePrice));
            totalEffectivePrice += effectivePrice;
            ++pricedItemCount;
            if (!(effectivePrice < this.minEffectivePrice)) continue;
            this.minEffectivePrice = effectivePrice;
        }
        if (pricedItemCount > 0) {
            this.averageEffectivePrice = totalEffectivePrice / (double)pricedItemCount;
        } else {
            this.reset();
        }
    }

    // Число с разделителями: "1 500 000", "1.500.000", "1,500,000", "1.5", "9 999 999"(NBSP) и т.д.
    private static final Pattern NUM = Pattern.compile("\\d[\\d .,\\u00a0]*\\d|\\d");
    // Сгруппированное по тысячам число: "9,999,999", "9 999 999", "1.500.000" — сильный признак цены.
    private static final Pattern GROUPED = Pattern.compile("\\d{1,3}([ .,\\u00a0]\\d{3})+");

    /**
     * Достаёт цену из тултипа. Гораздо устойчивее старого парсера: понимает множители
     * к/кк/ккк (k/kk/kkk, тыс/млн/млрд), разделители разрядов и разные метки цены/валюты.
     */
    private long parsePrice(List<Text> tooltip) {
        List<String> lines = new ArrayList<String>();
        for (Text t : tooltip) {
            lines.add(t.getString());
        }
        // 1) строки с явной меткой цены
        for (String raw : lines) {
            String line = raw.toLowerCase();
            if (containsAny(line, "цена", "цeна", "курс", "стоимост", "price", "buy", "купить", "продаж")) {
                long p = extractAmount(line, false);
                if (p > 0L) return p;
            }
        }
        // 2) строки с валютой
        for (String raw : lines) {
            String line = raw.toLowerCase();
            if (containsAny(line, "монет", "коин", "емеральд", "изумруд", "₽", "$", "руб", " алмаз")) {
                long p = extractAmount(line, false);
                if (p > 0L) return p;
            }
        }
        // 3) число с множителем (к/кк/ккк) ИЛИ сгруппированное по тысячам ("9,999,999", "9 999 999")
        for (String raw : lines) {
            long p = extractAmount(raw.toLowerCase(), true);
            if (p > 0L) return p;
        }
        // 4) совсем крайний случай: самое большое «крупное» число (>= 1000) в тултипе
        long fallback = -1L;
        for (String raw : lines) {
            Matcher matcher = NUM.matcher(raw.toLowerCase());
            while (matcher.find()) {
                long v = plainLong(matcher.group());
                if (v >= 1000L && v > fallback) fallback = v;
            }
        }
        return fallback;
    }

    private static boolean containsAny(String line, String... keys) {
        for (String key : keys) {
            if (line.contains(key)) return true;
        }
        return false;
    }

    /** Берёт наибольшее «ценоподобное» число в строке. requireSuffix — только с множителем ИЛИ сгруппированное. */
    private static long extractAmount(String line, boolean requireSuffix) {
        Matcher matcher = NUM.matcher(line);
        long best = -1L;
        while (matcher.find()) {
            String numStr = matcher.group();
            long mult = multiplierOf(line.substring(matcher.end()));
            boolean grouped = GROUPED.matcher(numStr).matches();
            if (requireSuffix && mult == 1L && !grouped) continue;
            long val = mult > 1L ? decimalTimes(numStr, mult) : plainLong(numStr);
            if (val > 0L && val > best) best = val;
        }
        return best;
    }

    private static long multiplierOf(String rest) {
        String r = rest.trim();
        if (r.isEmpty()) return 1L;
        String[][] groups = {{"ккк", "kkk", "млрд"}, {"кк", "kk", "млн"}, {"к", "k", "тыс"}};
        long[] mults = {1_000_000_000L, 1_000_000L, 1_000L};
        for (int g = 0; g < groups.length; ++g) {
            for (String s : groups[g]) {
                // суффикс должен быть отдельным (за ним не буква), иначе ловим слова вроде «которое»
                if (r.startsWith(s) && (s.length() >= r.length() || !Character.isLetter(r.charAt(s.length())))) {
                    return mults[g];
                }
            }
        }
        return 1L;
    }

    private static long decimalTimes(String numStr, long mult) {
        String norm = numStr.replace(" ", "").replace(" ", "").replace(",", ".");
        norm = norm.replaceAll("[\\s\\u00a0]", "");
        int last = norm.lastIndexOf(46);
        if (last >= 0) {
            norm = norm.substring(0, last).replace(".", "") + "." + norm.substring(last + 1).replace(".", "");
        }
        try {
            return (long) (Double.parseDouble(norm) * (double) mult);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static long plainLong(String numStr) {
        String digits = numStr.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return -1L;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private void reset() {
        this.pageItems.clear();
        this.averageEffectivePrice = 0.0;
        this.minEffectivePrice = Double.MAX_VALUE;
    }

    private record AuctionItem(int slotId, long totalPrice, int count, int maxDurability, int currentDurability, double effectivePrice) {
    }
}
