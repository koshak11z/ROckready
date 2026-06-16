package moscow.rockstar.systems.modules.modules.other;

import com.google.gson.*;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import org.lwjgl.glfw.GLFW;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.other.autobuy.AutoBuyCatalog;
import moscow.rockstar.systems.modules.modules.other.autobuy.AutoBuyCategory;
import moscow.rockstar.systems.modules.modules.other.autobuy.AutoBuyItem;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.autobuy.AutoBuyScreen;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "AutoBuy", category = ModuleCategory.OTHER, desc = "Full AutoBuy: catalog GUI, parser, buy/confirm/history")
public class AutoBuy extends BaseModule {
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9][0-9\\s.,]{1,})");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<AutoBuyItem> items = new ArrayList<>(AutoBuyCatalog.defaults());
    private final List<PurchaseHistoryEntry> purchaseHistory = new ArrayList<>();
    private final Set<String> blockedListings = new HashSet<>();

    private final ModeSetting serverMode = new ModeSetting(this, "Server Mode");
    private final ModeSetting.Value funTime = new ModeSetting.Value(this.serverMode, "FunTime").select();
    private final ModeSetting.Value holyWorld = new ModeSetting.Value(this.serverMode, "HolyWorld");
    private final ModeSetting.Value spookyTime = new ModeSetting.Value(this.serverMode, "SpookyTime");
    private final ModeSetting.Value universal = new ModeSetting.Value(this.serverMode, "Universal");
    private final SliderSetting refreshDelay = new SliderSetting(this, "Refresh delay").min(25.0f).max(3000.0f).step(25.0f).currentValue(450.0f).suffix(" ms");
    private final SliderSetting buyDelay = new SliderSetting(this, "Buy delay").min(0.0f).max(1000.0f).step(5.0f).currentValue(25.0f).suffix(" ms");
    private final BooleanSetting autoOpen = new BooleanSetting(this, "Auto open /ah").enable();
    private final BooleanSetting autoRefresh = new BooleanSetting(this, "Auto refresh").enable();
    private final BooleanSetting confirmBuy = new BooleanSetting(this, "Confirm buy").enable();
    private final BooleanSetting skipDuplicates = new BooleanSetting(this, "Skip duplicates").enable();
    private final BooleanSetting debug = new BooleanSetting(this, "Debug");
    private final BindSetting openGuiBind = new BindSetting(this, "Open GUI Bind").key(GLFW.GLFW_KEY_P);
    private final BooleanSetting auctionAutoBuy = new BooleanSetting(this, "Auction button AutoBuy").enable();
    private final BooleanSetting auctionAutoSell = new BooleanSetting(this, "Auction button AutoSell");
    private final BooleanSetting auctionAutoSetup = new BooleanSetting(this, "Auction button AutoSetup");
    private final BooleanSetting auctionQuickRefresh = new BooleanSetting(this, "Auction button Refresh").enable();
    private final ButtonSetting openGui = new ButtonSetting(this, "Open AutoBuy GUI").action(() -> this.openConfigGui());

    private final Timer refreshTimer = new Timer();
    private final Timer buyTimer = new Timer();
    private AutoBuyCandidate pendingCandidate;
    private int lastClickedSlot = -1;
    private ButtonBounds autoBuyBounds;
    private ButtonBounds autoSellBounds;
    private ButtonBounds autoSetupBounds;
    private ButtonBounds refreshBounds;

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;
        if (mc.currentScreen instanceof AutoBuyScreen) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            if (this.autoOpen.isEnabled() && this.refreshTimer.finished((long)this.refreshDelay.getCurrentValue())) {
                mc.player.networkHandler.sendChatCommand(this.ahCommand());
                this.refreshTimer.reset();
            }
            return;
        }
        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        if (this.isConfirmScreen(title)) {
            this.handleConfirmScreen();
            return;
        }
        if (!this.isAuctionScreen(title)) return;
        if (this.auctionAutoBuy.isEnabled() && this.pendingCandidate == null && this.buyTimer.finished((long)this.buyDelay.getCurrentValue())) {
            this.pendingCandidate = this.findBestCandidate();
        }
        if (this.auctionAutoBuy.isEnabled() && this.pendingCandidate != null && this.buyTimer.finished((long)this.buyDelay.getCurrentValue())) {
            this.clickCandidate(this.pendingCandidate);
            this.pendingCandidate = null;
        } else if (this.autoRefresh.isEnabled() && this.auctionQuickRefresh.isEnabled() && this.refreshTimer.finished((long)this.refreshDelay.getCurrentValue())) {
            this.clickRefresh();
            this.refreshTimer.reset();
        }
    };

    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (!(event.getPacket() instanceof GameMessageS2CPacket packet)) return;
        String text = packet.content().getString();
        String low = text.toLowerCase(Locale.ROOT);
        if (low.contains("купил") || low.contains("купили") || low.contains("успешно куп" ) || low.contains("purchase")) {
            this.purchaseHistory.add(new PurchaseHistoryEntry(System.currentTimeMillis(), text));
            while (this.purchaseHistory.size() > 50) this.purchaseHistory.remove(0);
        }
        if (this.debug.isEnabled() && (low.contains("аук") || low.contains("куп") || low.contains("buy"))) {
            MessageUtility.info(Text.of("§a[AutoBuy] §7" + text));
        }
    };

    private final EventListener<KeyPressEvent> onKeyPress = event -> {
        if (event.getAction() != GLFW.GLFW_PRESS || !this.openGuiBind.isKey(event.getKey())) return;
        if (mc.currentScreen instanceof AutoBuyScreen screen && screen.belongsTo(this)) {
            screen.close();
        } else {
            this.openConfigGui();
        }
    };

    private final EventListener<ScreenRenderEvent> onScreenRender = event -> {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !this.isAuctionScreen(screen.getTitle().getString().toLowerCase(Locale.ROOT))) {
            this.clearOverlayBounds();
            return;
        }
        UIContext context = UIContext.of(event.getContext(), (int)GuiUtility.getMouse().getX(), (int)GuiUtility.getMouse().getY(), event.getTickDelta());
        this.renderAuctionButtons(context);
    };

    private final EventListener<MouseEvent> onMouse = event -> {
        if (event.getAction() != GLFW.GLFW_PRESS || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
        double mouseX = GuiUtility.getMouse().getX();
        double mouseY = GuiUtility.getMouse().getY();
        if (this.autoBuyBounds != null && this.autoBuyBounds.contains(mouseX, mouseY)) {
            this.auctionAutoBuy.toggle();
            if (!this.auctionAutoBuy.isEnabled()) this.pendingCandidate = null;
            this.saveAutoBuyConfig();
            return;
        }
        if (this.autoSellBounds != null && this.autoSellBounds.contains(mouseX, mouseY)) {
            this.auctionAutoSell.toggle();
            this.saveAutoBuyConfig();
            return;
        }
        if (this.autoSetupBounds != null && this.autoSetupBounds.contains(mouseX, mouseY)) {
            this.auctionAutoSetup.toggle();
            this.saveAutoBuyConfig();
            return;
        }
        if (this.refreshBounds != null && this.refreshBounds.contains(mouseX, mouseY)) {
            this.auctionQuickRefresh.toggle();
            this.saveAutoBuyConfig();
        }
    };

    public AutoBuy() {
        this.loadAutoBuyConfig();
    }

    @Override
    public void onEnable() {
        this.lastClickedSlot = -1;
        this.pendingCandidate = null;
        this.refreshTimer.reset();
        this.buyTimer.reset();
        if (this.autoOpen.isEnabled() && mc.player != null) mc.player.networkHandler.sendChatCommand(this.ahCommand());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.saveAutoBuyConfig();
        super.onDisable();
    }

    private void openConfigGui() {
        if (mc != null) mc.setScreen(new AutoBuyScreen(this));
    }

    private void renderAuctionButtons(UIContext context) {
        int bgW = 176;
        int bgH = 166;
        int offsetX = (mc.currentScreen.width - bgW) / 2;
        int offsetY = Math.max(2, (mc.currentScreen.height - bgH) / 2 - 24);
        int gap = 4;
        int h = 20;
        int w = Math.max(76, Math.min(112, (bgW - gap * 3) / 4));
        int total = w * 4 + gap * 3;
        int x = offsetX + (bgW - total) / 2;
        this.autoBuyBounds = new ButtonBounds(x, offsetY, w, h);
        this.autoSellBounds = new ButtonBounds(x + (w + gap), offsetY, w, h);
        this.autoSetupBounds = new ButtonBounds(x + (w + gap) * 2, offsetY, w, h);
        this.refreshBounds = new ButtonBounds(x + (w + gap) * 3, offsetY, w, h);
        this.drawAuctionButton(context, this.autoBuyBounds, "AutoBuy", this.auctionAutoBuy.isEnabled());
        this.drawAuctionButton(context, this.autoSellBounds, "AutoSell", this.auctionAutoSell.isEnabled());
        this.drawAuctionButton(context, this.autoSetupBounds, "AutoSetup", this.auctionAutoSetup.isEnabled());
        this.drawAuctionButton(context, this.refreshBounds, "Refresh", this.auctionQuickRefresh.isEnabled());
    }

    private void drawAuctionButton(UIContext context, ButtonBounds bounds, String label, boolean enabled) {
        boolean hover = bounds.contains(context.getMouseX(), context.getMouseY());
        ColorRGBA bg = enabled ? Colors.getAccentColor().mulAlpha(hover ? 0.95f : 0.78f) : new ColorRGBA(120.0f, 34.0f, 34.0f, hover ? 210.0f : 165.0f);
        context.drawSquircle(bounds.x, bounds.y, bounds.w, bounds.h, 7.0f, BorderRadius.all(5.0f), bg);
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), label, bounds.x + bounds.w / 2.0f, bounds.y + 4.0f, Colors.WHITE);
        context.drawCenteredText(Fonts.REGULAR.getFont(6.5f), enabled ? "вкл" : "выкл", bounds.x + bounds.w / 2.0f, bounds.y + 12.0f, enabled ? Colors.WHITE : Colors.getGuiTextInactiveColor());
    }

    private void clearOverlayBounds() {
        this.autoBuyBounds = null;
        this.autoSellBounds = null;
        this.autoSetupBounds = null;
        this.refreshBounds = null;
    }

    public List<AutoBuyItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public String getSelectedServerName() {
        return this.serverMode.getValue() == null ? "Universal" : this.serverMode.getValue().getName();
    }

    private boolean itemAllowedOnSelectedServer(AutoBuyItem item) {
        String server = this.getSelectedServerName();
        return item.getServer().equalsIgnoreCase(server) || item.getServer().equalsIgnoreCase("Universal");
    }

    public List<AutoBuyItem> getItemsByCategory(AutoBuyCategory category) {
        return this.items.stream()
                .filter(this::itemAllowedOnSelectedServer)
                .filter(item -> category == AutoBuyCategory.ALL || item.getCategory() == category)
                .toList();
    }

    public List<AutoBuyItem> getConfiguredItems() {
        return this.items.stream()
                .filter(this::itemAllowedOnSelectedServer)
                .filter(item -> item.isBuyEnabled() && item.getMaxBuyPrice() > 0L)
                .toList();
    }

    public List<PurchaseHistoryEntry> getPurchaseHistory() {
        return Collections.unmodifiableList(this.purchaseHistory);
    }

    public boolean isAuctionAutoBuyEnabled() { return this.auctionAutoBuy.isEnabled(); }
    public boolean isAuctionAutoSellEnabled() { return this.auctionAutoSell.isEnabled(); }
    public boolean isAuctionAutoSetupEnabled() { return this.auctionAutoSetup.isEnabled(); }
    public boolean isAuctionQuickRefreshEnabled() { return this.auctionQuickRefresh.isEnabled(); }

    private AutoBuyCandidate findBestCandidate() {
        List<AutoBuyItem> configured = this.getConfiguredItems();
        if (configured.isEmpty() || mc.player == null) return null;
        AutoBuyCandidate best = null;
        for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.id >= 45) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            List<Text> tooltip = stack.getTooltip(Item.TooltipContext.create((World)mc.world), mc.player, mc.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);
            long price = this.extractPrice(stack, tooltip);
            if (price <= 0L) continue;
            String fingerprint = this.fingerprint(stack, price, slot.id);
            if (this.skipDuplicates.isEnabled() && this.blockedListings.contains(fingerprint)) continue;
            for (AutoBuyItem item : configured) {
                if (!item.matches(stack, tooltip)) continue;
                long unitPrice = Math.max(1L, price / Math.max(1, stack.getCount()));
                if (unitPrice <= item.getMaxBuyPrice() && (best == null || unitPrice < best.unitPrice())) {
                    best = new AutoBuyCandidate(slot.id, item, stack.copy(), price, unitPrice, fingerprint);
                }
            }
        }
        return best;
    }

    private void clickCandidate(AutoBuyCandidate candidate) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (candidate.slot() == this.lastClickedSlot) return;
        this.lastClickedSlot = candidate.slot();
        this.blockedListings.add(candidate.fingerprint());
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, candidate.slot(), 0, SlotActionType.PICKUP, mc.player);
        this.buyTimer.reset();
        this.refreshTimer.reset();
        this.purchaseHistory.add(new PurchaseHistoryEntry(System.currentTimeMillis(), "CLICK " + candidate.item().getDisplayName() + " price=" + candidate.price()));
    }

    private void handleConfirmScreen() {
        if (!this.confirmBuy.isEnabled() || mc.player == null || mc.interactionManager == null) return;
        int slot = this.findConfirmSlot();
        if (slot >= 0 && this.buyTimer.finished((long)this.buyDelay.getCurrentValue())) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            this.buyTimer.reset();
        }
    }

    private int findConfirmSlot() {
        for (Slot slot : mc.player.currentScreenHandler.slots) {
            Item item = slot.getStack().getItem();
            String text = slot.getStack().getName().getString().toLowerCase(Locale.ROOT);
            if (text.contains("подтверд") || text.contains("confirm") || Registries.ITEM.getId(item).toString().contains("lime")) return slot.id;
        }
        return this.holyWorld.isSelected() ? 10 : 13;
    }

    private void clickRefresh() {
        if (mc.player == null || mc.interactionManager == null) return;
        int refreshSlot = this.holyWorld.isSelected() ? 47 : 49;
        if (refreshSlot < mc.player.currentScreenHandler.slots.size()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, refreshSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private boolean isAuctionScreen(String title) {
        return title.contains("аук") || title.contains("auction") || title.contains("ah") || title.contains("поиск");
    }

    private boolean isConfirmScreen(String title) {
        return title.contains("подтверж") || title.contains("confirm") || title.contains("покуп");
    }

    private String ahCommand() {
        return this.holyWorld.isSelected() ? "ah" : "ah";
    }

    private long extractPrice(ItemStack stack, List<Text> tooltip) {
        long best = -1L;
        for (Text line : tooltip) {
            String lower = line.getString().toLowerCase(Locale.ROOT);
            if (!(lower.contains("цен") || lower.contains("price") || lower.contains("стоим") || lower.contains("$"))) continue;
            Matcher matcher = PRICE_PATTERN.matcher(lower);
            while (matcher.find()) {
                long value = parseLong(matcher.group(1));
                if (value > 0L && (best < 0L || value < best)) best = value;
            }
        }
        if (best < 0L) {
            Matcher matcher = PRICE_PATTERN.matcher(stack.getName().getString());
            while (matcher.find()) {
                long value = parseLong(matcher.group(1));
                if (value > 0L && (best < 0L || value < best)) best = value;
            }
        }
        return best;
    }

    private long parseLong(String s) {
        try {
            String digits = s.replaceAll("[^0-9]", "");
            return digits.isBlank() ? -1L : Long.parseLong(digits);
        } catch (Exception ignored) {
            return -1L;
        }
    }

    private String fingerprint(ItemStack stack, long price, int slot) {
        return Registries.ITEM.getId(stack.getItem()) + ":" + stack.getName().getString() + ":" + stack.getCount() + ":" + price + ":" + slot;
    }

    public void saveAutoBuyConfig() {
        try {
            Path path = this.configPath();
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();
            for (AutoBuyItem item : this.items) {
                JsonObject obj = new JsonObject();
                obj.addProperty("server", item.getServer());
                obj.addProperty("key", item.getKey());
                obj.addProperty("buy", item.isBuyEnabled());
                obj.addProperty("sell", item.isSellEnabled());
                obj.addProperty("setup", item.isSetupEnabled());
                obj.addProperty("maxBuy", item.getMaxBuyPrice());
                obj.addProperty("minSell", item.getMinSellPrice());
                obj.addProperty("durability", item.getMinDurabilityPercent());
                obj.addProperty("thorns", item.getThornsMode().name());
                array.add(obj);
            }
            root.addProperty("auctionAutoBuy", this.auctionAutoBuy.isEnabled());
            root.addProperty("auctionAutoSell", this.auctionAutoSell.isEnabled());
            root.addProperty("auctionAutoSetup", this.auctionAutoSetup.isEnabled());
            root.addProperty("auctionQuickRefresh", this.auctionQuickRefresh.isEnabled());
            root.add("items", array);
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {}
    }

    public void loadAutoBuyConfig() {
        try {
            Path path = this.configPath();
            if (!Files.exists(path)) return;
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("auctionAutoBuy")) this.auctionAutoBuy.setEnabled(root.get("auctionAutoBuy").getAsBoolean());
            if (root.has("auctionAutoSell")) this.auctionAutoSell.setEnabled(root.get("auctionAutoSell").getAsBoolean());
            if (root.has("auctionAutoSetup")) this.auctionAutoSetup.setEnabled(root.get("auctionAutoSetup").getAsBoolean());
            if (root.has("auctionQuickRefresh")) this.auctionQuickRefresh.setEnabled(root.get("auctionQuickRefresh").getAsBoolean());
            Map<String, AutoBuyItem> byKey = new HashMap<>();
            for (AutoBuyItem item : this.items) byKey.put(item.getKey(), item);
            JsonArray array = root.getAsJsonArray("items");
            if (array == null) return;
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                AutoBuyItem item = byKey.get(obj.get("key").getAsString());
                if (item == null) continue;
                if (obj.has("buy")) item.setBuyEnabled(obj.get("buy").getAsBoolean());
                if (obj.has("sell")) item.setSellEnabled(obj.get("sell").getAsBoolean());
                if (obj.has("setup")) item.setSetupEnabled(obj.get("setup").getAsBoolean());
                if (obj.has("maxBuy")) item.setMaxBuyPrice(obj.get("maxBuy").getAsLong());
                if (obj.has("minSell")) item.setMinSellPrice(obj.get("minSell").getAsLong());
                if (obj.has("durability")) item.setMinDurabilityPercent(obj.get("durability").getAsInt());
                if (obj.has("thorns")) item.setThornsMode(AutoBuyItem.ThornsMode.valueOf(obj.get("thorns").getAsString()));
            }
        } catch (Exception ignored) {}
    }

    private Path configPath() {
        return mc.runDirectory.toPath().resolve("rockstar").resolve("autobuy.json");
    }

    private record AutoBuyCandidate(int slot, AutoBuyItem item, ItemStack stack, long price, long unitPrice, String fingerprint) {}
    public record PurchaseHistoryEntry(long time, String text) {}

    private record ButtonBounds(int x, int y, int w, int h) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.w && mouseY >= this.y && mouseY < this.y + this.h;
        }
    }

}
