package im.zov4ik.display.hud;

import com.mojang.authlib.GameProfile;
import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.common.repository.staff.StaffRepository;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.client.Instance;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Список Стаффа — styled to match HotKeys (header + rows with name + role).
 */
public class StaffList extends AbstractDraggable {
    public static StaffList getInstance() {
        return Instance.getDraggable(StaffList.class);
    }

    public final Map<PlayerListEntry, Animation> list = new HashMap<>();
    private final Set<String> notifiedPlayers = new HashSet<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private long lastColorChange = 0L;
    private int currentColorIndex = 0;
    private float animatedWidth;

    private static final Map<String, String> CHAR_TO_NAME = new HashMap<>();
    private static final Map<String, Integer> PREFIX_COLORS = new HashMap<>();
    private static final List<String> EXAMPLE_PREFIXES = List.of(
            "Vanish", "helper", "moder", "moder+", "st.moder", "admin", "yt", "media"
    );

    private static final float HEADER_HEIGHT = 20.0F;
    private static final float ROW_HEIGHT = 15.0F;
    private static final float ROW_GAP = 2.0F;
    private static final float MIN_WIDTH = 100.0F;
    private static final float PADDING_X = 6.0F;

    static {
        CHAR_TO_NAME.put("\uA500", "player");
        CHAR_TO_NAME.put("\uA504", "hero");
        CHAR_TO_NAME.put("\uA508", "titan");
        CHAR_TO_NAME.put("\uA512", "avenger");
        CHAR_TO_NAME.put("\uA516", "overlord");
        CHAR_TO_NAME.put("\uA520", "magister");
        CHAR_TO_NAME.put("\uA524", "imperator");
        CHAR_TO_NAME.put("\uA528", "dragon");
        CHAR_TO_NAME.put("\uA532", "bull");
        CHAR_TO_NAME.put("\uA552", "rabbit");
        CHAR_TO_NAME.put("\uA536", "tiger");
        CHAR_TO_NAME.put("\uA544", "dracula");
        CHAR_TO_NAME.put("\uA556", "bunny");
        CHAR_TO_NAME.put("\uA540", "hydra");
        CHAR_TO_NAME.put("\uA548", "cobra");
        CHAR_TO_NAME.put("\uA501", "media");
        CHAR_TO_NAME.put("\uA505", "yt");
        CHAR_TO_NAME.put("\uA560", "d.helper");
        CHAR_TO_NAME.put("\uA509", "helper");
        CHAR_TO_NAME.put("\uA513", "ml.moder");
        CHAR_TO_NAME.put("\uA517", "moder");
        CHAR_TO_NAME.put("\uA521", "moder+");
        CHAR_TO_NAME.put("\uA525", "st.moder");
        CHAR_TO_NAME.put("\uA529", "gl.moder");
        CHAR_TO_NAME.put("\uA533", "ml.admin");
        CHAR_TO_NAME.put("\uA537", "admin");

        PREFIX_COLORS.put("media", new Color(255, 0, 0, 255).getRGB());
        PREFIX_COLORS.put("yt", new Color(255, 0, 0, 255).getRGB());
        PREFIX_COLORS.put("d.helper", new Color(255, 255, 0, 255).getRGB());
        PREFIX_COLORS.put("helper", new Color(255, 255, 0, 255).getRGB());
        PREFIX_COLORS.put("ml.moder", new Color(0, 255, 255, 255).getRGB());
        PREFIX_COLORS.put("moder", new Color(0, 0, 255, 255).getRGB());
        PREFIX_COLORS.put("moder+", new Color(0, 0, 255, 255).getRGB());
        PREFIX_COLORS.put("st.moder", new Color(128, 0, 128, 255).getRGB());
        PREFIX_COLORS.put("gl.moder", new Color(128, 0, 128, 255).getRGB());
        PREFIX_COLORS.put("ml.admin", new Color(0, 255, 255, 255).getRGB());
        PREFIX_COLORS.put("admin", new Color(255, 0, 0, 255).getRGB());
        PREFIX_COLORS.put("Vanish", new Color(255, 0, 0, 255).getRGB());
    }

    public StaffList() {
        super("Список Стаффа", 115, 40, 100, 34, true);
        this.animatedWidth = 100.0F;
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected(getName())
                && Hud.getInstance().state
                && (!list.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        if (mc.world == null || mc.player == null || mc.getNetworkHandler() == null) {
            list.clear();
            return;
        }

        Collection<PlayerListEntry> playerList = mc.getNetworkHandler().getPlayerList();
        Scoreboard scoreboard = mc.world.getScoreboard();
        Set<String> addedNames = new HashSet<>();

        if (list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastColorChange >= 1000L) {
                currentColorIndex = (currentColorIndex + 1) % EXAMPLE_PREFIXES.size();
                lastColorChange = currentTime;
            }
            return;
        }

        for (PlayerListEntry entry : playerList) {
            String name = entry.getProfile().getName();
            if (addedNames.contains(name) || list.containsKey(entry)) {
                continue;
            }
            String display = entry.getDisplayName() != null ? entry.getDisplayName().getString() : name;
        }

        for (StaffRepository.Staff staff : StaffRepository.getStaff()) {
            String staffName = staff.getName();
            if (addedNames.contains(staffName) || list.keySet().stream().anyMatch(e -> e.getProfile().getName().equals(staffName))) {
                continue;
            }
            playerList.stream()
                    .filter(p -> p.getProfile().getName().equalsIgnoreCase(staffName))
                    .findFirst()
                    .ifPresent(entry -> {
                        list.put(entry, new Decelerate().setMs(150).setValue(1));
                        addedNames.add(staffName);
                    });
        }

        List<Team> teams = new ArrayList<>(scoreboard.getTeams());
        teams.sort(Comparator.comparing(Team::getName));
        Collection<PlayerListEntry> online = mc.getNetworkHandler().getPlayerList();

        for (Team team : teams) {
            Collection<String> members = team.getPlayerList();
            if (members.size() != 1) continue;

            String name = members.iterator().next();
            if (!namePattern.matcher(name).matches() || addedNames.contains(name)) continue;

            boolean present = online.stream().anyMatch(e -> e.getProfile() != null && name.equals(e.getProfile().getName()));
            if (present) continue;
            if (list.keySet().stream().anyMatch(e -> e.getProfile().getName().equals(name))) continue;

            String teamPrefix = team.getPrefix().getString();
            String prefix = CHAR_TO_NAME.entrySet().stream()
                    .filter(e -> teamPrefix.contains(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("");

            MutableText displayName = Text.empty();
            if (Network.isReallyWorld()) {
                displayName.append(Text.literal(name).formatted(Formatting.GRAY))
                        .append(Text.literal(" [").formatted(Formatting.GRAY))
                        .append(Text.literal(prefix.isEmpty() ? "V" : prefix).formatted(Formatting.RESET))
                        .append(Text.literal("]").formatted(Formatting.GRAY));
            } else {
                displayName.append(Text.literal("[").formatted(Formatting.GRAY))
                        .append(Text.literal(prefix.isEmpty() ? "V" : prefix).formatted(Formatting.RESET))
                        .append(Text.literal("] ").formatted(Formatting.GRAY))
                        .append(Text.literal(name).formatted(Formatting.GRAY));
            }

            GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), name);
            PlayerListEntry fake = new PlayerListEntry(fakeProfile, mc.isInSingleplayer());
            fake.setDisplayName(displayName);
            fake.setListOrder(Integer.MIN_VALUE);
            list.put(fake, new Decelerate().setMs(150).setValue(1));
            addedNames.add(name);

            if (Hud.getInstance().notificationSettings.isSelected("Staff Join") && !notifiedPlayers.contains(name)) {
                Notifications.getInstance().addList(Text.literal(name + " - Зашел на сервер!"), 5000);
                notifiedPlayers.add(name);
            }
        }

        list.entrySet().removeIf(entry -> {
            String name = entry.getKey().getProfile().getName();
            boolean isFromRepo = StaffRepository.isStaff(name);
            boolean inPlayerList = playerList.stream().anyMatch(p -> p.getProfile().getName().equals(name));
            boolean inTeam = scoreboard.getTeams().stream().flatMap(t -> t.getPlayerList().stream()).anyMatch(name::equals);
            boolean shouldRemove = false;

            if (isFromRepo) {
                if (!inPlayerList) shouldRemove = true;
            } else {
                if (inPlayerList || !inTeam) shouldRemove = true;
            }

            if (shouldRemove) entry.getValue().setDirection(Direction.BACKWARDS);

            if (entry.getValue().isFinished(Direction.BACKWARDS)) {
                notifiedPlayers.remove(name);
                if (!inPlayerList && Hud.getInstance().notificationSettings.isSelected("Staff Leave")) {
                    Notifications.getInstance().addList(Text.literal(name + " - Вышел с сервера!"), 5000);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();

        FontRenderer headerFont = Fonts.getSize(18, Fonts.Type.BOLD);
        FontRenderer nameFont = Fonts.getSize(15, Fonts.Type.DEFAULT);
        FontRenderer roleFont = Fonts.getSize(14, Fonts.Type.SEMI);
        FontRenderer headerIconFont = Fonts.getSize(20, Fonts.Type.ICONS);

        boolean drawExample = list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen);

        float targetWidth = calculateTargetWidth(headerFont, nameFont, roleFont, drawExample);
        animateWidth(targetWidth);

        setWidth((int) animatedWidth);
        setHeight((int) calculateHeight(drawExample));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 5.5F);

        drawHeader(matrix, headerFont, headerIconFont);

        if (drawExample) {
            drawExampleRow(matrix, nameFont, roleFont);
        } else {
            drawStaffRows(matrix, nameFont, roleFont);
        }
    }

    private void animateWidth(float targetWidth) {
        if (animatedWidth <= 0.0F) animatedWidth = targetWidth;
        animatedWidth += (targetWidth - animatedWidth) * 0.18F;
        if (Math.abs(targetWidth - animatedWidth) < 0.25F) animatedWidth = targetWidth;
    }

    private float calculateTargetWidth(FontRenderer headerFont, FontRenderer nameFont, FontRenderer roleFont, boolean drawExample) {
        float maxWidth = MIN_WIDTH;
        float headerWidth = PADDING_X + headerFont.getStringWidth("Список Стаффа") + 25.0F;
        maxWidth = Math.max(maxWidth, headerWidth);

        if (drawExample) {
            String prefix = getExamplePrefix();
            float rowW = PADDING_X + nameFont.getStringWidth("Стафф") + 15.0F + roleFont.getStringWidth(prefix) + PADDING_X;
            return Math.max(maxWidth, rowW);
        }

        Collection<PlayerListEntry> playerList = mc.player != null && mc.player.networkHandler != null
                ? mc.player.networkHandler.getPlayerList() : List.of();

        for (Map.Entry<PlayerListEntry, Animation> staff : list.entrySet()) {
            float animation = staff.getValue().getOutput().floatValue();
            if (animation <= 0.0F || staff.getKey() == null) continue;

            PlayerListEntry player = staff.getKey();
            String name = player.getProfile().getName();
            PlayerListEntry renderEntry = getRenderEntry(playerList, player, name);
            String prefix = getPrefix(renderEntry, name);

            float rowW = PADDING_X + nameFont.getStringWidth(name) + 15.0F + roleFont.getStringWidth(prefix) + PADDING_X;
            maxWidth = Math.max(maxWidth, rowW);
        }
        return maxWidth;
    }

    private float calculateHeight(boolean drawExample) {
        if (drawExample) return HEADER_HEIGHT + ROW_GAP + ROW_HEIGHT + 4.0F;
        float totalHeight = HEADER_HEIGHT;
        boolean hasVisible = false;
        for (Map.Entry<PlayerListEntry, Animation> staff : list.entrySet()) {
            float animation = staff.getValue().getOutput().floatValue();
            if (animation <= 0.0F || staff.getKey() == null) continue;
            totalHeight += ROW_GAP + ROW_HEIGHT * animation;
            hasVisible = true;
        }
        if (!hasVisible) totalHeight += ROW_GAP + ROW_HEIGHT;
        return totalHeight + 4.0F;
    }

    private void drawHeader(MatrixStack matrix, FontRenderer headerFont, FontRenderer headerIconFont) {
        headerFont.drawString(matrix, "Список Стаффа", getX() + PADDING_X, getY() + 6.0F, HudTheme.TEXT);
        headerIconFont.drawString(matrix, "E", getX() + getWidth() - 18.0F, getY() + 5.0F, HudTheme.ACCENT);
    }

    private void drawExampleRow(MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont) {
        String prefix = getExamplePrefix();
        int prefixColor = PREFIX_COLORS.getOrDefault(prefix, HudTheme.MUTED_TEXT);
        drawRow(matrix, nameFont, roleFont, getY() + HEADER_HEIGHT + ROW_GAP, "Стафф", prefix, prefixColor);
    }

    private void drawStaffRows(MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont) {
        Collection<PlayerListEntry> playerList = mc.player != null && mc.player.networkHandler != null
                ? mc.player.networkHandler.getPlayerList() : List.of();

        float rowY = getY() + HEADER_HEIGHT + ROW_GAP;
        float centerX = getX() + getWidth() / 2.0F;

        for (Map.Entry<PlayerListEntry, Animation> staff : list.entrySet()) {
            PlayerListEntry player = staff.getKey();
            if (player == null) continue;
            float animation = staff.getValue().getOutput().floatValue();
            if (animation <= 0.0F) continue;

            String name = player.getProfile().getName();
            float currentRowY = rowY;
            PlayerListEntry renderEntry = getRenderEntry(playerList, player, name);
            String prefix = getPrefix(renderEntry, name);
            int prefixColor = PREFIX_COLORS.getOrDefault(prefix, new Color(255, 0, 0, 255).getRGB());

            Calculate.scale(matrix, centerX, currentRowY + ROW_HEIGHT / 2.0F, 1.0F, animation, () -> {
                drawRow(matrix, nameFont, roleFont, currentRowY, name, prefix, prefixColor);
            });

            rowY += ROW_HEIGHT * animation + ROW_GAP;
        }
    }

    private void drawRow(MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont,
                         float rowY, String name, String role, int roleColor) {
        nameFont.drawString(matrix, name, getX() + PADDING_X, rowY + 4.0F, HudTheme.TEXT);
        float roleWidth = roleFont.getStringWidth(role);
        roleFont.drawString(matrix, role, getX() + getWidth() - PADDING_X - roleWidth, rowY + 4.5F, roleColor);
    }

    private PlayerListEntry getRenderEntry(Collection<PlayerListEntry> playerList, PlayerListEntry fallback, String name) {
        return playerList.stream()
                .filter(p -> p.getProfile().getName().equals(name))
                .findFirst()
                .orElse(fallback);
    }

    private String getPrefix(PlayerListEntry renderEntry, String name) {
        String displayName = renderEntry.getDisplayName() != null ? renderEntry.getDisplayName().getString() : name;
        return CHAR_TO_NAME.entrySet().stream()
                .filter(e -> displayName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Vanish");
    }

    private String getExamplePrefix() {
        return EXAMPLE_PREFIXES.get(currentColorIndex % EXAMPLE_PREFIXES.size());
    }
}
