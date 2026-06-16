package im.zov4ik.features.impl.render;

import im.zov4ik.utils.interactions.interact.PlayerInteractionHelper;
import im.zov4ik.utils.client.Instance;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import im.zov4ik.features.module.setting.implement.BooleanSetting;
import im.zov4ik.features.module.setting.implement.SelectSetting;
import im.zov4ik.utils.client.managers.event.EventHandler;
import im.zov4ik.features.module.Module;
import im.zov4ik.features.module.ModuleCategory;
import im.zov4ik.features.module.setting.implement.MultiSelectSetting;
import im.zov4ik.features.module.setting.implement.SliderSettings;
import im.zov4ik.common.repository.friend.FriendUtils;
import im.zov4ik.utils.display.font.FontRenderer;
import im.zov4ik.utils.display.font.Fonts;
import im.zov4ik.utils.display.shape.ShapeProperties;
import im.zov4ik.events.render.WorldRenderEvent;
import im.zov4ik.utils.display.color.ColorAssist;
import im.zov4ik.utils.display.geometry.Render3D;
import im.zov4ik.utils.client.packet.network.Network;
import im.zov4ik.utils.math.projection.Projection;
import im.zov4ik.utils.display.geometry.Render2D;
import im.zov4ik.events.player.TickEvent;
import im.zov4ik.events.render.DrawEvent;
import im.zov4ik.events.render.WorldLoadEvent;
import im.zov4ik.features.impl.combat.AntiBot;
import im.zov4ik.utils.math.calc.Calculate;
import im.zov4ik.display.hud.HudTheme;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Esp extends Module {
    public static Esp getInstance() {
        return Instance.get(Esp.class);
    }
    Identifier TEXTURE = Identifier.of("textures/features/esp/container.png");
    List<PlayerEntity> players = new ArrayList<>();
    Map<RegistryKey<Enchantment>, String> encMap = new HashMap<>();

    public MultiSelectSetting entityType = new MultiSelectSetting("Тип сущности", "Сущности, которые будут отображаться")
            .value("Player", "Item", "TNT").selected("Player", "Item");
    MultiSelectSetting playerSetting = new MultiSelectSetting("Настройки игрока", "Настройки для игроков")
            .value("Box", "Armor", "NameTags", "Hand Items").selected("Box", "Armor", "NameTags", "Hand Items").visible(() -> entityType.isSelected("Player"));
    public SelectSetting boxType = new SelectSetting("Тип", "Тип")
            .value("Corner", "Full", "3D Box", "Skeleton").selected("3D Box").visible(() -> playerSetting.isSelected("Box"));
    public BooleanSetting flatBoxOutline = new BooleanSetting("Контур", "Контур для плоских боксов").visible(() -> playerSetting.isSelected("Box") && (boxType.isSelected("Corner") || boxType.isSelected("Full")));
    public SliderSettings boxAlpha = new SliderSettings("Прозрачность", "Прозрачность бокса")
            .setValue(1.0F).range(0.1F, 1.0F).visible(() -> boxType.isSelected("3D Box"));
    public SliderSettings skeletonWidth = new SliderSettings("Толщина линий", "Толщина линий скелета")
            .setValue(2.5f).range(2.5f, 4.0f).visible(() -> boxType.isSelected("Skeleton"));

    private static final float DISTANCE = 128.0f;

    public Esp() {
        super("Esp", "Esp", ModuleCategory.RENDER);
        setup(entityType, playerSetting, boxType, flatBoxOutline, boxAlpha, skeletonWidth);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        players.clear();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        players.clear();
        if (mc.world != null) {
            mc.world.getPlayers().stream()
                    .filter(player -> player != mc.player)
                    .filter(player -> player.getCustomName() == null || !player.getCustomName().getString().startsWith("Ghost_"))
                    .forEach(players::add);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (!entityType.isSelected("Player")) return;
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        for (PlayerEntity player : players) {
            if (player == null) continue;
            if (player == mc.player) continue;
            if (player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) continue;
            double interpX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
            double interpY = MathHelper.lerp(tickDelta, player.prevY, player.getY());
            double interpZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
            Vec3d interpCenter = new Vec3d(interpX, interpY, interpZ);
            float distance = (float) mc.getEntityRenderDispatcher().camera.getPos().distanceTo(interpCenter);
            if (distance < 1) continue;
            boolean friend = FriendUtils.isFriend(player);
            int baseColor = friend ? ColorAssist.getFriendColor() : ColorAssist.getClientColor();
            int alpha = (int) (boxAlpha.getValue() * 255);
            int fillColor = (baseColor & 0x00FFFFFF) | (alpha << 24);
            int outlineColor = baseColor | 0xFF000000;

            if (boxType.isSelected("3D Box")) {
                Box interpBox = player.getDimensions(player.getPose()).getBoxAt(interpX, interpY, interpZ);
                Render3D.drawBox(interpBox, fillColor, 2, true, true, true);
                Render3D.drawBox(interpBox, outlineColor, 2, true, true, true);
            } else if (boxType.isSelected("Skeleton") && playerSetting.isSelected("Box")) {
                if (distance > DISTANCE) continue;
                    renderSkeleton(player, tickDelta, baseColor);
            }
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        DrawContext context = e.getDrawContext();
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(13, Fonts.Type.SEMI);
        FontRenderer bigFont = Fonts.getSize(13 + 2, Fonts.Type.SEMI);
        if (entityType.isSelected("Player")) {
            for (PlayerEntity player : players) {
                if (player == null) continue;
                if (player == mc.player) continue;
                if (player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) continue;
                Vector4d vec4d = Projection.getVector4D(player);
                float distance = (float) mc.getEntityRenderDispatcher().camera.getPos().distanceTo(player.getBoundingBox().getCenter());
                boolean friend = FriendUtils.isFriend(player);
                if (distance < 1) continue;
                if (Projection.cantSee(vec4d)) continue;
                if (playerSetting.isSelected("Box") && !boxType.isSelected("Skeleton")) drawBox(friend, vec4d, player);
                if (playerSetting.isSelected("NameTags")) {
                    drawPlayerNametag(context, matrix, player, vec4d, friend);
                }
            }
        }
        List<Entity> entities = PlayerInteractionHelper.streamEntities()
                .sorted(Comparator.comparing(ent -> ent instanceof ItemEntity item && item.getStack().getName().getContent().toString().equals("empty")))
                .toList();
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity item && entityType.isSelected("Item")) {
                Vector4d vec4d = Projection.getVector4D(entity);
                ItemStack stack = item.getStack();
                ContainerComponent compoundTag = stack.get(DataComponentTypes.CONTAINER);
                List<ItemStack> list = compoundTag != null ? compoundTag.stream().toList() : List.of();
                if (Projection.cantSee(vec4d)) continue;
                Text text = item.getStack().getName();
                if (stack.getCount() > 1) text = text.copy().append(Formatting.RESET + " [" + Formatting.RED + stack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
                if (!list.isEmpty()) drawShulkerBox(context, stack, list, vec4d);
                else drawText(matrix, text, Projection.centerX(vec4d), vec4d.y, text.getContent().toString().equals("empty") ? bigFont : font);
            } else if (entity instanceof TntEntity tnt && entityType.isSelected("TNT")) {
                Vector4d vec4d = Projection.getVector4D(entity);
                if (Projection.cantSee(vec4d)) continue;
                drawText(matrix, tnt.getStyledDisplayName(), Projection.centerX(vec4d), vec4d.y, font);
            }
        }
    }

    private void renderSkeleton(PlayerEntity player, float partialTicks, int color) {
        Vec3d pos = Calculate.interpolate(player);
        float width = skeletonWidth.getValue();

        float limbSwing = player.limbAnimator.getPos(partialTicks);
        float limbSwingAmount = player.limbAnimator.getSpeed(partialTicks);

        float bodyYaw = MathHelper.lerpAngleDegrees(partialTicks, player.prevBodyYaw, player.bodyYaw);
        float bodyYawRad = (float) Math.toRadians(-bodyYaw + 90);

        boolean isSwimming = player.isSwimming() || player.isGliding();
        float sneakOffset = player.isSneaking() ? 0.2f : 0f;
        float swimOffset = isSwimming ? 0.6f : 0f;

        Vec3d head = pos.add(0, 1.62f - sneakOffset - swimOffset, 0);
        Vec3d neck = pos.add(0, 1.4f - sneakOffset - swimOffset, 0);
        Vec3d body = pos.add(0, 0.9f - sneakOffset - swimOffset, 0);
        Vec3d pelvis = pos.add(0, 0.6f - sneakOffset - swimOffset, 0);

        Render3D.drawLine(head, neck, color, width, false);
        Render3D.drawLine(neck, body, color, width, false);
        Render3D.drawLine(body, pelvis, color, width, false);

        float rightArmSwing = MathHelper.cos(limbSwing * 0.6662f) * limbSwingAmount * 0.5f;
        float leftArmSwing = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * limbSwingAmount * 0.5f;
        float rightLegSwing = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * limbSwingAmount * 0.7f;
        float leftLegSwing = MathHelper.cos(limbSwing * 0.6662f) * limbSwingAmount * 0.7f;

        Vec3d rightShoulder = neck.add(
                Math.sin(bodyYawRad) * 0.3,
                -0.1,
                Math.cos(bodyYawRad) * 0.3
        );

        Vec3d rightElbow = rightShoulder.add(
                Math.sin(bodyYawRad) * 0.05 + Math.sin(bodyYawRad + Math.PI/2) * rightArmSwing * 0.15,
                -0.25 - Math.abs(rightArmSwing) * 0.1,
                Math.cos(bodyYawRad) * 0.05 + Math.cos(bodyYawRad + Math.PI/2) * rightArmSwing * 0.15
        );

        Vec3d rightHand = rightElbow.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightArmSwing * 0.1,
                -0.25 - Math.abs(rightArmSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * rightArmSwing * 0.1
        );

        Render3D.drawLine(rightShoulder, rightElbow, color, width, false);
        Render3D.drawLine(rightElbow, rightHand, color, width, false);

        Vec3d leftShoulder = neck.add(
                -Math.sin(bodyYawRad) * 0.3,
                -0.1,
                -Math.cos(bodyYawRad) * 0.3
        );

        Vec3d leftElbow = leftShoulder.add(
                -Math.sin(bodyYawRad) * 0.05 + Math.sin(bodyYawRad + Math.PI/2) * leftArmSwing * 0.15,
                -0.25 - Math.abs(leftArmSwing) * 0.1,
                -Math.cos(bodyYawRad) * 0.05 + Math.cos(bodyYawRad + Math.PI/2) * leftArmSwing * 0.15
        );

        Vec3d leftHand = leftElbow.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftArmSwing * 0.1,
                -0.25 - Math.abs(leftArmSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * leftArmSwing * 0.1
        );

        Render3D.drawLine(leftShoulder, leftElbow, color, width, false);
        Render3D.drawLine(leftElbow, leftHand, color, width, false);

        Vec3d rightHip = pelvis.add(
                Math.sin(bodyYawRad) * 0.15,
                0,
                Math.cos(bodyYawRad) * 0.15
        );

        Vec3d rightKnee = rightHip.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightLegSwing * 0.1,
                -0.35 + Math.max(0, rightLegSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * rightLegSwing * 0.1
        );

        Vec3d rightFoot = rightKnee.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightLegSwing * 0.08,
                -0.35 - Math.max(0, -rightLegSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * rightLegSwing * 0.08
        );

        Render3D.drawLine(rightHip, rightKnee, color, width, false);
        Render3D.drawLine(rightKnee, rightFoot, color, width, false);

        Vec3d leftHip = pelvis.add(
                -Math.sin(bodyYawRad) * 0.15,
                0,
                -Math.cos(bodyYawRad) * 0.15
        );

        Vec3d leftKnee = leftHip.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftLegSwing * 0.1,
                -0.35 + Math.max(0, leftLegSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * leftLegSwing * 0.1
        );

        Vec3d leftFoot = leftKnee.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftLegSwing * 0.08,
                -0.35 - Math.max(0, -leftLegSwing) * 0.05,
                Math.cos(bodyYawRad + Math.PI/2) * leftLegSwing * 0.08
        );

        Render3D.drawLine(leftHip, leftKnee, color, width, false);
        Render3D.drawLine(leftKnee, leftFoot, color, width, false);

        Render3D.drawLine(rightShoulder, leftShoulder, color, width, false);
        Render3D.drawLine(rightHip, leftHip, color, width, false);
    }

    private void drawBox(boolean friend, Vector4d vec, PlayerEntity player) {
        if (boxType.isSelected("3D Box") || boxType.isSelected("Skeleton")) {
            return;
        }
        int client = friend ? ColorAssist.getFriendColor() : ColorAssist.getClientColor();
        int black = ColorAssist.HALF_BLACK;
        float posX = (float) vec.x;
        float posY = (float) vec.y;
        float endPosX = (float) vec.z;
        float endPosY = (float) vec.w;
        float size = (endPosX - posX) / 3;
        if (boxType.isSelected("Corner")) {
            Render2D.drawQuad(posX - 0.5F, posY - 0.5F, size, 0.5F, client);
            Render2D.drawQuad(posX - 0.5F, posY, 0.5F, size + 0.5F, client);
            Render2D.drawQuad(posX - 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
            Render2D.drawQuad(posX - 0.5F, endPosY - 0.5F, size, 0.5F, client);
            Render2D.drawQuad(endPosX - size + 1, posY - 0.5F, size, 0.5F, client);
            Render2D.drawQuad(endPosX + 0.5F, posY, 0.5F, size + 0.5F, client);
            Render2D.drawQuad(endPosX + 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
            Render2D.drawQuad(endPosX - size + 1, endPosY - 0.5F, size, 0.5F, client);
            if (flatBoxOutline.isValue()) {
                Render2D.drawQuad(posX - 1F, posY - 1, size + 1, 1.5F, black);
                Render2D.drawQuad(posX - 1F, posY + 0.5F, 1.5F, size + 0.5F, black);
                Render2D.drawQuad(posX - 1F, endPosY - size - 1, 1.5F, size, black);
                Render2D.drawQuad(posX - 1F, endPosY - 1, size + 1, 1.5F, black);
                Render2D.drawQuad(endPosX - size + 0.5F, posY - 1, size + 1, 1.5F, black);
                Render2D.drawQuad(endPosX, posY + 0.5F, 1.5F, size + 0.5F, black);
                Render2D.drawQuad(endPosX, endPosY - size - 1, 1.5F, size, black);
                Render2D.drawQuad(endPosX - size + 0.5F, endPosY - 1, size + 1, 1.5F, black);
            }
        } else if (boxType.isSelected("Full")) {
            if (flatBoxOutline.isValue()) {
                Render2D.drawQuad(posX - 1F, posY - 1F, endPosX - posX + 2F, 1.5F, black);
                Render2D.drawQuad(posX - 1F, posY - 1F, 1.5F, endPosY - posY + 2F, black);
                Render2D.drawQuad(posX - 1F, endPosY - 1F, endPosX - posX + 2F, 1.5F, black);
                Render2D.drawQuad(endPosX - 0.5F, posY - 1F, 1.5F, endPosY - posY + 2F, black);
            }
            Render2D.drawQuad(posX - 0.5F, posY - 0.5F, endPosX - posX + 1F, 0.5F, client);
            Render2D.drawQuad(posX - 0.5F, posY - 0.5F, 0.5F, endPosY - posY + 1F, client);
            Render2D.drawQuad(posX - 0.5F, endPosY - 0.5F, endPosX - posX + 1F, 0.5F, client);
            Render2D.drawQuad(endPosX, posY - 0.5F, 0.5F, endPosY - posY + 1F, client);
        }
    }

    private void drawArmor(DrawContext context, PlayerEntity player, Vector4d vec, FontRenderer font) {
        MatrixStack matrix = context.getMatrices();
        List<ItemStack> items = new ArrayList<>();
        player.getEquippedItems().forEach(s -> {if (!s.isEmpty()) items.add(s);});
        float posX = (float) (Projection.centerX(vec) - items.size() * 5.5);
        float posY = (float) (vec.y - 13 / 1.5 - 15);
        String offhandLabel = getHolyWorldOffhandLabel(player.getOffHandStack());
        float offset = -11;
        if (!offhandLabel.isBlank()) {
            drawText(matrix, Text.literal(offhandLabel), Projection.centerX(vec), posY - 3.5F, Fonts.getSize(11, Fonts.Type.SEMI));
        }
        if (!items.isEmpty()) {
            matrix.push();
            matrix.translate(posX, posY, 0);
            for (ItemStack stack : items) {
                offset += 11;
                Render2D.defaultDrawStack(context, stack, offset, 0, false, false, 0.5F);
            }
            matrix.pop();
        }
    }

    private void drawHands(MatrixStack matrix, PlayerEntity player, FontRenderer font, Vector4d vec) {
        double posY = vec.w;
        for (ItemStack stack : player.getHandItems()) {
            if (stack.isEmpty()) continue;
            MutableText text = Text.empty().append(stack.getName());
            if (stack.getCount() > 1) text.append(Formatting.RESET + " [" + Formatting.RED + stack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
            posY += font.getStringHeight(text) / 2 + 3;
            drawText(matrix, text, Projection.centerX(vec), posY, font);
        }
    }

    private void drawShulkerBox(DrawContext context, ItemStack itemStack, List<ItemStack> stacks, Vector4d vec) {
        MatrixStack matrix = context.getMatrices();
        int width = 176;
        int height = 67;
        int color = ColorAssist.multBright(ColorAssist.replAlpha(((BlockItem) itemStack.getItem()).getBlock().getDefaultMapColor().color, 1F), 1);
        matrix.push();
        matrix.translate(Projection.centerX(vec) - (double) width / 4, vec.w + 2, -200 + Math.cos(vec.x));
        matrix.scale(0.5F, 0.5F, 1);
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, 0, 0, width, height, width, height, color);
        int posX = 7;
        int posY = 6;
        for (ItemStack stack : stacks.stream().toList()) {
            Render2D.defaultDrawStack(context, stack, posX, posY, false, true, 1);
            posX += 18;
            if (posX >= 165) {
                posY += 18;
                posX = 7;
            }
        }
        matrix.pop();
    }

    private void drawText(MatrixStack matrix, Text text, double startX, double startY, FontRenderer font) {
        int paddingX = 2;
        float paddingY = 0.75F;
        float height = font.getFont().getSize() / 1.5F;
        float width = font.getStringWidth(text);
        float posX = (float) (startX - width / 2);
        float posY = (float) startY - height;
        rectangle.render(ShapeProperties.create(matrix, posX - paddingX, posY - paddingY, width + paddingX * 2, height + paddingY * 2)
                .round(2f)
                .outlineColor(new Color(33, 33, 33, 0).getRGB())
                .color(ColorAssist.getRect(0.65f))
                .build());
        font.drawText(matrix, text, posX, posY + 3);
    }

    private void drawPlayerNametag(DrawContext context, MatrixStack matrix, PlayerEntity player, Vector4d vec, boolean friend) {
        FontRenderer nameFont = Fonts.getSize(13, Fonts.Type.MANROPEBOLD);
        FontRenderer smallFont = Fonts.getSize(12, Fonts.Type.INST);

        String name = player.getName().getString();
        String donate = extractDonate(player);
        String hp = PlayerInteractionHelper.getHealthString(player);

        List<ItemStack> items = new ArrayList<>();
        if (playerSetting.isSelected("Armor")) {
            ItemStack[] armor = new ItemStack[] {
                    player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
                    player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
                    player.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
                    player.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
            };
            for (ItemStack stack : armor) {
                if (!stack.isEmpty()) items.add(stack);
            }
        }
        if (playerSetting.isSelected("Hand Items")) {
            if (!player.getMainHandStack().isEmpty()) items.add(player.getMainHandStack());
            if (!player.getOffHandStack().isEmpty()) items.add(player.getOffHandStack());
        }

        float iconSlot = 7.5F;
        float icon = 6.2F;
        float height = 12.5F;
        float width = 4.0F + iconSlot + 3.0F + nameFont.getStringWidth(name) + 4.0F;
        if (!donate.isEmpty()) width += iconSlot + 2.5F + smallFont.getStringWidth(donate) + 4.0F;
        width += iconSlot + 2.5F + smallFont.getStringWidth(hp) + 4.0F;
        if (!items.isEmpty()) width += 2.0F + items.size() * 7.2F;

        float x = (float) Projection.centerX(vec) - width / 2.0F;
        float y = (float) vec.y - height - 4.0F;
        float cy = y + (height - iconSlot) / 2.0F;
        float tx = x + 4.0F;
        float textY = y + 4.1F;

        HudTheme.panel(matrix, x, y, width, height, 2.8F);
        HudTheme.iconSlot(context, HudTheme.ICON_USER, tx, cy, iconSlot, icon, HudTheme.ACCENT);
        tx += iconSlot + 3.0F;

        int nameColor = friend ? ColorAssist.getFriendColor() : HudTheme.TEXT;
        if (AntiBot.getInstance().isBot(player)) nameColor = HudTheme.DANGER;
        nameFont.drawString(matrix, name, tx, y + 3.95F, nameColor);
        tx += nameFont.getStringWidth(name) + 4.0F;

        if (!donate.isEmpty()) {
            HudTheme.iconSlot(context, HudTheme.ICON_ORBIT, tx, cy, iconSlot, icon, HudTheme.ACCENT);
            tx += iconSlot + 2.5F;
            smallFont.drawString(matrix, donate, tx, textY, HudTheme.TEXT_DIM);
            tx += smallFont.getStringWidth(donate) + 4.0F;
        }

        HudTheme.iconSlot(context, HudTheme.ICON_HEART, tx, cy, iconSlot, icon, HudTheme.ACCENT);
        tx += iconSlot + 2.5F;
        smallFont.drawString(matrix, hp, tx, textY, PlayerInteractionHelper.getHealth(player) <= 6.0F ? HudTheme.DANGER : HudTheme.GOOD);
        tx += smallFont.getStringWidth(hp) + 3.0F;

        for (ItemStack stack : items) {
            Render2D.defaultDrawStack(context, stack, tx, y + 1.65F, false, false, 0.31F);
            tx += 7.2F;
        }
    }

    private String extractDonate(PlayerEntity player) {
        String display = player.getDisplayName() == null ? "" : stripFormatting(player.getDisplayName().getString());
        String name = player.getName().getString();
        if (display.isBlank() || display.equals(name)) {
            String sphere = getSphere(player.getOffHandStack());
            return stripFormatting(sphere).replace("[", "").replace("]", "").trim();
        }
        return display.replace(name, "").replace("[", "").replace("]", "").trim();
    }

    private String stripFormatting(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.replaceAll("(?i)[\\u00A7&][0-9a-fk-or]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private MutableText getTextPlayer(PlayerEntity player, boolean friend) {
        float health = PlayerInteractionHelper.getHealth(player);
        MutableText text = Text.empty();
        if (friend) text.append("[" + Formatting.GREEN + "F" + Formatting.RESET + "] ");
        if (AntiBot.getInstance().isBot(player)) text.append("[" + Formatting.DARK_RED + "BOT" + Formatting.RESET + "] ");
        if (playerSetting.isSelected("NameTags")) text.append(player.getDisplayName()); else text.append(player.getName());
        if (player.getOffHandStack().getItem().equals(Items.PLAYER_HEAD) || player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING))
            text.append(Formatting.RESET + getSphere(player.getOffHandStack()));
        if (health >= 0 && health <= player.getMaxHealth())
            text.append(Formatting.RESET + " [" + Formatting.RED + PlayerInteractionHelper.getHealthString(player) + Formatting.RESET + "]");
        return text;
    }

    private String getSphere(ItemStack stack) {
        var component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (Network.isFunTime() && component != null) {
            NbtCompound compound = component.copyNbt();
            if (compound.getInt("tslevel") != 0) {
                return " [" + Formatting.GOLD + compound.getString("don-item").replace("sphere-", "").toUpperCase() + Formatting.RESET + "]";
            }
        }
        return "";
    }

    private String getHolyWorldOffhandLabel(ItemStack stack) {
        if (!isHolyWorldLike() || stack.isEmpty()) {
            return "";
        }
        if (stack.getItem() != Items.PLAYER_HEAD && stack.getItem() != Items.TOTEM_OF_UNDYING) {
            return "";
        }

        String fallback = stack.getItem() == Items.PLAYER_HEAD ? "Голова игрока" : "Тотем бессмертия";
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return fallback;
        }

        NbtCompound nbt = customData.copyNbt();
        if (nbt == null || nbt.isEmpty()) {
            return fallback;
        }

        String displayName = stack.getName().getString().replace('\n', ' ').trim();
        if (!displayName.isBlank() && !displayName.equalsIgnoreCase(fallback)) {
            return displayName;
        }

        NbtCompound sphereEffect = nbt.getCompound("sphereEffect");
        if (sphereEffect != null && sphereEffect.contains("mascot")) {
            NbtCompound mascot = sphereEffect.getCompound("mascot");
            String mascotName = mascot.getString("name");
            if (!mascotName.isBlank()) {
                return buildHolyWorldArtifactName(stack, mascotName);
            }
        }

        NbtCompound serviceId = nbt.getCompound("itemServiceId");
        if (serviceId != null) {
            String serviceName = serviceId.getString("name");
            if (!serviceName.isBlank()) {
                return buildHolyWorldArtifactName(stack, serviceName);
            }
        }

        return fallback;
    }

    private String buildHolyWorldArtifactName(ItemStack stack, String rawName) {
        String name = rawName.trim()
                .replace("сфера_", "")
                .replace("sphere_", "")
                .replace("талисман_", "")
                .replace("talisman_", "")
                .replace('_', ' ');

        name = switch (name.toLowerCase(Locale.ROOT)) {
            case "eternity" -> "Этернити";
            default -> name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        };

        String prefix = stack.getItem() == Items.TOTEM_OF_UNDYING ? "Талисман " : "Сфера ";
        return prefix + name;
    }

    private boolean isHolyWorldLike() {
        return Network.isHolyWorld() || "HollyWorld".equalsIgnoreCase(Network.getServer());
    }
}
