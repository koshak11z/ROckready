package im.zov4ik.display.hud;

import im.zov4ik.common.animation.Animation;
import im.zov4ik.common.animation.Direction;
import im.zov4ik.common.animation.implement.Decelerate;
import im.zov4ik.events.packet.PacketEvent;
import im.zov4ik.features.impl.render.Hud;
import im.zov4ik.utils.client.managers.api.draggable.AbstractDraggable;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.*;
import java.util.List;

public class StaffList extends AbstractDraggable {
    private final List<StaffEntry> staffEntries = new ArrayList<>();
    private float animatedWidth;

    private static final float HEADER_HEIGHT = 16.5F;
    private static final float ROW_HEIGHT = 14.0F;
    private static final float ROW_GAP = 1.0F;
    private static final float MIN_WIDTH = 110.0F;
    private static final float FACE_SIZE = 8.0F;

    private static final Map<String, RoleInfo> ROLE_COLORS = new LinkedHashMap<>();

    static {
        ROLE_COLORS.put("owner", new RoleInfo("owner", new Color(255, 187, 0)));
        ROLE_COLORS.put("admin", new RoleInfo("admin", new Color(255, 85, 85)));
        ROLE_COLORS.put("dev", new RoleInfo("dev", new Color(100, 180, 255)));
        ROLE_COLORS.put("javadev", new RoleInfo("javadev", new Color(100, 180, 255)));
        ROLE_COLORS.put("webdev", new RoleInfo("webdev", new Color(150, 150, 165)));
        ROLE_COLORS.put("designer", new RoleInfo("designer", new Color(255, 120, 200)));
        ROLE_COLORS.put("moder", new RoleInfo("moder", new Color(100, 255, 150)));
        ROLE_COLORS.put("helper", new RoleInfo("helper", new Color(130, 200, 255)));
        ROLE_COLORS.put("builder", new RoleInfo("builder", new Color(200, 160, 100)));
        ROLE_COLORS.put("staff", new RoleInfo("staff", new Color(180, 180, 200)));
    }

    public StaffList() {
        super("Staff", 10, 280, 120, 34, true);
        this.animatedWidth = 120.0F;
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected("Staff")
                && Hud.getInstance().state
                && (!staffEntries.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        staffEntries.removeIf(e -> e.anim.isFinished(Direction.BACKWARDS));

        if (mc.getNetworkHandler() == null) return;

        Set<String> currentNames = new HashSet<>();
        for (StaffEntry entry : staffEntries) {
            if (entry.anim.getDirection() != Direction.BACKWARDS) {
                currentNames.add(entry.name);
            }
        }

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            Text displayName = entry.getDisplayName();
            if (displayName == null) continue;

            String raw = displayName.getString().toLowerCase();
            String name = entry.getProfile().getName();
            RoleInfo role = detectRole(raw, name);
            if (role == null) continue;

            if (!currentNames.contains(name)) {
                staffEntries.add(new StaffEntry(
                        name,
                        role,
                        entry.getSkinTextures().texture(),
                        new Decelerate().setMs(200).setValue(1.0F)
                ));
                currentNames.add(name);
            }
        }

        // Remove staff that left
        Set<String> onlineNames = new HashSet<>();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            onlineNames.add(entry.getProfile().getName());
        }
        for (StaffEntry entry : staffEntries) {
            if (!onlineNames.contains(entry.name)) {
                entry.anim.setDirection(Direction.BACKWARDS);
            }
        }
    }

    @Override
    public void packet(PacketEvent e) {
        switch (e.getPacket()) {
            case PlayerRemoveS2CPacket packet -> {
                for (UUID uuid : packet.profileIds()) {
                    staffEntries.stream()
                            .filter(s -> mc.getNetworkHandler() != null)
                            .forEach(s -> s.anim.setDirection(Direction.BACKWARDS));
                }
            }
            case GameJoinS2CPacket p -> staffEntries.clear();
            default -> {}
        }
    }

    private RoleInfo detectRole(String raw, String name) {
        for (Map.Entry<String, RoleInfo> entry : ROLE_COLORS.entrySet()) {
            if (raw.contains(entry.getKey()) || raw.contains("[" + entry.getKey() + "]")) {
                return entry.getValue();
            }
        }

        // Check formatting codes for colored prefixes (common staff indicator)
        if (raw.contains("§4") || raw.contains("§c") || raw.contains("§6")) {
            // Likely staff with colored prefix
            for (Map.Entry<String, RoleInfo> entry : ROLE_COLORS.entrySet()) {
                if (raw.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();

        FontRenderer headerFont = Fonts.getSize(19, Fonts.Type.DEFAULT);
        FontRenderer nameFont = Fonts.getSize(16, Fonts.Type.DEFAULT);
        FontRenderer roleFont = Fonts.getSize(13, Fonts.Type.SEMI);
        FontRenderer headerIconFont = Fonts.getSize(25, Fonts.Type.ICONS);

        boolean drawExample = staffEntries.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen);

        float targetWidth = calculateTargetWidth(headerFont, headerIconFont, nameFont, roleFont, drawExample);
        animateWidth(targetWidth);

        setWidth((int) animatedWidth);
        setHeight((int) calculateHeight(drawExample));

        HudTheme.panel(matrix, getX(), getY(), getWidth(), getHeight(), 5.5F);

        drawHeader(matrix, headerFont, headerIconFont);

        if (drawExample) {
            drawExampleRows(matrix, nameFont, roleFont);
        } else {
            drawStaffRows(context, matrix, nameFont, roleFont);
        }
    }

    private void animateWidth(float targetWidth) {
        if (animatedWidth <= 0.0F) animatedWidth = targetWidth;
        animatedWidth += (targetWidth - animatedWidth) * 0.18F;
        if (Math.abs(targetWidth - animatedWidth) < 0.25F) animatedWidth = targetWidth;
    }

    private float calculateTargetWidth(FontRenderer headerFont, FontRenderer headerIconFont, FontRenderer nameFont, FontRenderer roleFont, boolean drawExample) {
        float maxWidth = MIN_WIDTH;
        float headerWidth = 6.0F + headerFont.getStringWidth("Staff") + 5.0F + headerIconFont.getStringWidth("G") + 6.0F;
        maxWidth = Math.max(maxWidth, headerWidth);

        if (drawExample) {
            return Math.max(maxWidth, calcRowWidth(nameFont, roleFont, "PlayerName", "designer"));
        }

        for (StaffEntry entry : staffEntries) {
            if (entry.anim.getOutput().floatValue() <= 0.0F) continue;
            maxWidth = Math.max(maxWidth, calcRowWidth(nameFont, roleFont, entry.name, entry.role.name));
        }
        return maxWidth;
    }

    private float calcRowWidth(FontRenderer nameFont, FontRenderer roleFont, String name, String role) {
        return 6.0F + FACE_SIZE + 5.0F + nameFont.getStringWidth(name) + 8.0F + roleFont.getStringWidth(role) + 10.0F;
    }

    private float calculateHeight(boolean drawExample) {
        if (drawExample) return HEADER_HEIGHT + (ROW_GAP + ROW_HEIGHT) * 3;
        float h = HEADER_HEIGHT;
        boolean any = false;
        for (StaffEntry entry : staffEntries) {
            float a = entry.anim.getOutput().floatValue();
            if (a <= 0.0F) continue;
            h += ROW_GAP + ROW_HEIGHT * a;
            any = true;
        }
        if (!any) h += ROW_GAP + ROW_HEIGHT;
        return h;
    }

    private void drawHeader(MatrixStack matrix, FontRenderer headerFont, FontRenderer headerIconFont) {
        headerFont.drawString(matrix, "Staff", getX() + 3.0F, getY() + 5.0F, HudTheme.TEXT);
        float iconX = getX() + getWidth() - headerIconFont.getStringWidth("G") - 8.0F;
        headerIconFont.drawString(matrix, "G", iconX, getY() + 4.5F, HudTheme.TEXT);
    }

    private void drawExampleRows(MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont) {
        float rowY = getY() + HEADER_HEIGHT + ROW_GAP;
        drawRowSimple(matrix, nameFont, roleFont, rowY, "kam1kadz", new RoleInfo("designer", new Color(255, 120, 200)));
        rowY += ROW_HEIGHT + ROW_GAP;
        drawRowSimple(matrix, nameFont, roleFont, rowY, "nikitasigma", new RoleInfo("javadev", new Color(100, 180, 255)));
        rowY += ROW_HEIGHT + ROW_GAP;
        drawRowSimple(matrix, nameFont, roleFont, rowY, "nomefxck", new RoleInfo("webdev", new Color(150, 150, 165)));
    }

    private void drawRowSimple(MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont, float rowY, String name, RoleInfo role) {
        float drawX = getX() + 5.0F;

        // Face placeholder
        rectangle.render(ShapeProperties.create(matrix, drawX, rowY + 3.0F, FACE_SIZE, FACE_SIZE)
                .round(2.0F)
                .color(ColorAssist.rgba(40, 40, 48, 180))
                .build());
        drawX += FACE_SIZE + 4.0F;

        // Name
        nameFont.drawString(matrix, name, drawX, rowY + 3.5F, HudTheme.TEXT);

        // Role tag (right-aligned, colored)
        float roleW = roleFont.getStringWidth(role.name);
        float roleX = getX() + getWidth() - roleW - 6.0F;
        roleFont.drawString(matrix, role.name, roleX, rowY + 4.5F, role.color.getRGB());
    }

    private void drawStaffRows(DrawContext context, MatrixStack matrix, FontRenderer nameFont, FontRenderer roleFont) {
        float rowY = getY() + HEADER_HEIGHT + ROW_GAP;
        float centerX = getX() + getWidth() / 2.0F;

        for (StaffEntry entry : staffEntries) {
            float animation = entry.anim.getOutput().floatValue();
            if (animation <= 0.0F) continue;

            float currentRowY = rowY;
            Calculate.scale(matrix, centerX, currentRowY + ROW_HEIGHT / 2.0F, 1.0F, animation, () -> {
                float drawX = getX() + 5.0F;

                // Face
                rectangle.render(ShapeProperties.create(matrix, drawX, currentRowY + 3.0F, FACE_SIZE, FACE_SIZE)
                        .round(2.0F)
                        .color(ColorAssist.rgba(40, 40, 48, 180))
                        .build());
                if (entry.skin != null) {
                    PlayerSkinDrawer.draw(context, entry.skin, (int) drawX, (int) (currentRowY + 3.0F), (int) FACE_SIZE);
                }
                drawX += FACE_SIZE + 4.0F;

                // Name
                nameFont.drawString(matrix, entry.name, drawX, currentRowY + 3.5F, HudTheme.TEXT);

                // Role tag
                float roleW = roleFont.getStringWidth(entry.role.name);
                float roleX = getX() + getWidth() - roleW - 6.0F;
                roleFont.drawString(matrix, entry.role.name, roleX, currentRowY + 4.5F, entry.role.color.getRGB());
            });

            rowY += ROW_HEIGHT * animation + ROW_GAP;
        }
    }

    private record RoleInfo(String name, Color color) {}
    private record StaffEntry(String name, RoleInfo role, Identifier skin, Animation anim) {}
}
