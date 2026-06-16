/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.gui.hud.BossBarHud
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.client.option.Perspective
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.EquipmentSlot$Type
 *  net.minecraft.entity.decoration.DisplayEntity$ItemDisplayEntity
 *  net.minecraft.entity.decoration.DisplayEntity$TextDisplayEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.resource.Resource
 *  net.minecraft.resource.ResourceManager
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Axis
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.World
 */
package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@ModuleInfo(name="Counter Mine", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0443\u0431\u0438\u0432\u0430\u0435\u0442 \u043a\u0440\u0438\u043f\u0435\u0440\u043e\u0432 \u043d\u0430 \u0444\u0430\u0440\u043c\u0438\u043b\u043a\u0435")
public class CounterMineOld
extends BaseModule {
    private final BooleanSetting moderDetect = new BooleanSetting(this, "ModerDetect");
    private final BooleanSetting wallHack = new BooleanSetting(this, "WallHack");
    private final BooleanSetting noF5 = new BooleanSetting(this, "No F5");
    private final BooleanSetting noSmoke = new BooleanSetting(this, "NoSmoke");
    private final BooleanSetting aim = new BooleanSetting(this, "Aim");
    private final BooleanSetting silent = new BooleanSetting((SettingsContainer)this, "Silent", () -> !this.aim.isEnabled());
    private final BooleanSetting autoShoot = new BooleanSetting((SettingsContainer)this, "AutoShoot", () -> !this.aim.isEnabled());
    private final BooleanSetting antiAim = new BooleanSetting(this, "AntAim");
    private final BooleanSetting fakeLag = new BooleanSetting(this, "FakeLag");
    private final Timer shootingTimer = new Timer();
    private final Timer moderTimer = new Timer();
    boolean waitRelease;
    boolean stopping;
    private final Timer movementPauseTimer = new Timer();
    private boolean temporaryStopping = false;
    private boolean hasShotDuringPause = false;
    private final List<Head> heads = new ArrayList<Head>();
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (this.moderDetect.isEnabled() && this.moderTimer.finished(5000L)) {
            if (this.isPlayerOnline("johnebik")) {
                MessageUtility.info(Text.of((String)"johnebik"));
                CounterMineOld.mc.player.networkHandler.sendChatCommand("hub");
                this.moderTimer.reset();
            }
            if (this.isPlayerOnline("sherlock")) {
                MessageUtility.info(Text.of((String)"sherlock"));
                CounterMineOld.mc.player.networkHandler.sendChatCommand("hub");
                this.moderTimer.reset();
            }
        }
        for (Entity entity : CounterMineOld.mc.world.getEntities()) {
            String modelJson;
            DisplayEntity.ItemDisplayEntity itemDisplay;
            String modelId;
            Box boundingBox = entity.getBoundingBox();
            if (entity instanceof DisplayEntity.ItemDisplayEntity) {
                String modelJson2;
                String modelId2;
                DisplayEntity.ItemDisplayEntity itemDisplay2 = (DisplayEntity.ItemDisplayEntity)entity;
                if (CounterMineOld.mc.player.distanceTo(entity) < 3.0f && (modelId2 = CounterMineOld.getModelIdFromNbt(itemDisplay2.getItemStack(), (RegistryWrapper.WrapperLookup)CounterMineOld.mc.player.getRegistryManager())) != null && (modelJson2 = CounterMineOld.findHashedModel(modelId2)) != null && CounterMineOld.mc.player.age % 200 == 0) {
                    System.out.println(modelJson2);
                }
            }
            if (boundingBox.maxX != boundingBox.minX || CounterMineOld.mc.player.distanceTo(entity) < 2.0f || !entity.getName().getString().contains("\u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430")) continue;
            boolean hologramNearby = CounterMineOld.isHologramNearby(entity, CounterMineOld.mc.world, 2.5);
            if (!(entity instanceof DisplayEntity.ItemDisplayEntity) || (modelId = CounterMineOld.getModelIdFromNbt((itemDisplay = (DisplayEntity.ItemDisplayEntity)entity).getItemStack(), (RegistryWrapper.WrapperLookup)CounterMineOld.mc.player.getRegistryManager())) == null || (modelJson = CounterMineOld.findHashedModel(modelId)) == null) continue;
            if (modelJson.contains("\"textures\":{\"particle\":\"item/") && modelJson.contains("\",\"skin\":\"item/")) {
                entity.setGlowing(true);
            }
            boolean sex = false;
            if (!modelJson.contains("{\"elements\":[{\"from\":[7.765625,8.0,7.765625]")) continue;
            boolean nearGround = CounterMineOld.isEntityNearGround((Entity)itemDisplay, (World)CounterMineOld.mc.world, 1.0);
            Head inList = null;
            for (Head head : this.heads) {
                if (head.entity != entity) continue;
                inList = head;
            }
            if (!nearGround) {
                if (inList != null) {
                    inList.poses.get((int)0).cords = entity.getPos();
                    continue;
                }
                Head addHead = new Head(entity, hologramNearby);
                addHead.poses.add(new Position(entity.getPos()));
                this.heads.add(addHead);
                continue;
            }
            if (inList == null) continue;
            this.heads.remove(inList);
        }
        for (Head head : new ArrayList<Head>(this.heads)) {
            if (!CounterMineOld.mc.world.hasEntity(head.entity)) {
                this.heads.remove(head);
            }
            for (Position hologramNearby : new ArrayList<Position>(head.poses)) {
            }
        }
        this.stopping = false;
        if (this.waitRelease) {
            CounterMineOld.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
            this.waitRelease = false;
        }
        if (this.antiAim.isEnabled()) {
            int age = CounterMineOld.mc.player.age;
            float yaw = CounterMineOld.mc.player.getYaw() - 90.0f - (float)(age % 5 == 0 ? 0 : (age % 5 == 1 ? 180 : 90));
            float pitch = 90.0f;
            CounterMineOld.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(CounterMineOld.mc.player.getX(), CounterMineOld.mc.player.getY(), CounterMineOld.mc.player.getZ(), yaw, pitch, CounterMineOld.mc.player.isOnGround(), CounterMineOld.mc.player.horizontalCollision));
            if (this.aim.isEnabled() && this.autoShoot.isEnabled()) {
                Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
            }
        }
        if (CounterMineOld.mc.player == null || CounterMineOld.mc.world == null || !this.aim.isEnabled()) {
            return;
        }
        TargetSettings settings = new TargetSettings.Builder().targetPlayers(true).requiredRange(200.0f).sortBy(TargetComparators.FOV).build();
        Rockstar.getInstance().getTargetManager().update(settings);
        Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (targetEntity == null) {
            return;
        }
        boolean notShoot = true;
        if (!MathUtility.canShoot(targetEntity.getPos())) {
            this.shootingTimer.reset();
            return;
        }
        Rotation toTarget = this.calculateRotation(targetEntity.getPos());
        float yaw = toTarget.getYaw();
        float pitch = toTarget.getPitch();
        notShoot = false;
        if (this.silent.isEnabled()) {
            if (!this.autoShoot.isEnabled()) {
                Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
            }
        } else {
            CounterMineOld.mc.player.setYaw(yaw);
            CounterMineOld.mc.player.setPitch(pitch);
            CounterMineOld.mc.player.setHeadYaw(yaw);
        }
        if (this.autoShoot.isEnabled()) {
            Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0f, 180.0f, 180.0f, RotationPriority.TO_TARGET);
            if (!this.temporaryStopping) {
                this.temporaryStopping = true;
                this.hasShotDuringPause = false;
                this.movementPauseTimer.reset();
                EntityUtility.setSpeed(0.0);
            }
            if (this.temporaryStopping) {
                if (this.shootingTimer.finished(70L) && !this.hasShotDuringPause) {
                    CounterMineOld.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
                    this.waitRelease = true;
                    this.hasShotDuringPause = true;
                    this.shootingTimer.reset();
                }
                if (this.movementPauseTimer.finished(200L) && this.hasShotDuringPause) {
                    this.temporaryStopping = false;
                    this.stopping = false;
                }
                this.stopping = this.temporaryStopping;
            }
        }
    };
    private final EventListener<InputEvent> onMove = event -> {
        if (this.stopping) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        boolean bebra;
        if (this.noF5.isEnabled()) {
            this.removeAllArmor();
        }
        if (CounterMineOld.mc.player.age % 200 == 0) {
            System.out.println("==============================================================");
        }
        BossBarHud boss = CounterMineOld.mc.inGameHud.getBossBarHud();
        boolean bl = bebra = CounterMineOld.mc.options.getPerspective() != Perspective.FIRST_PERSON;
        if (boss != null && bebra) {
            Class<BossBarHud> bossbarklass = BossBarHud.class;
            try {
                Field field = bossbarklass.getField("bossBars");
                Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)field.get(boss);
                for (UUID uuid : bossBars.keySet()) {
                    ClientBossBar clientBossBar = bossBars.get(uuid);
                    List<Text> siblings = clientBossBar.getName().getSiblings();
                    MutableText newText = Text.literal((String)"");
                    AtomicInteger i = new AtomicInteger();
                    siblings.stream().allMatch(text -> {
                        if (!text.getString().contains("\ub445\ua223\ua203\ub444\ua223\ua205")) {
                            newText.append(text);
                        }
                        i.getAndIncrement();
                        return true;
                    });
                    clientBossBar.setName((Text)newText);
                }
            }
            catch (Exception field) {
                // empty catch block
            }
        }
        if (CounterMineOld.mc.world == null || CounterMineOld.mc.player == null || !this.wallHack.isEnabled()) {
            return;
        }
        MatrixStack matrices = event.getMatrices();
        Camera camera = CounterMineOld.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Head head : this.heads) {
            for (Position pos : head.poses) {
                Draw3DUtility.renderOutlinedBox(matrices, linesBuffer, new Box(pos.cords.add((double)-0.05f, (double)-0.05f, (double)-0.05f), pos.cords.add((double)0.05f, (double)0.05f, (double)0.05f)).offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()), (head.isFriend ? ColorRGBA.GREEN : ColorRGBA.RED).withAlpha(100.0f));
            }
        }
        BuiltBuffer builtLinesBuffer = linesBuffer.endNullable();
        if (builtLinesBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtLinesBuffer);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final List<Packet<?>> packets = new ArrayList();
    private final Timer timer = new Timer();
    private Vec3d lastPos;
    private boolean replaying;
    private final EventListener<SendPacketEvent> sendListener = this::savePacket;
    private final EventListener<Render3DEvent> event3d = e -> {
        if (CounterMineOld.mc.options.getPerspective() != Perspective.FIRST_PERSON && this.fakeLag.isEnabled()) {
            MatrixStack ms = e.getMatrices();
            BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Vec3d cameraPos = CounterMineOld.mc.gameRenderer.getCamera().getPos();
            ms.push();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Draw3DUtility.renderOutlinedBox(ms, quadsBuffer, CounterMineOld.mc.player.getBoundingBox().offset(this.lastPos.subtract(CounterMineOld.mc.player.getPos())).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z), ColorRGBA.WHITE.withAlpha(180.0f));
            BuiltBuffer buildQuadsBuffer = quadsBuffer.endNullable();
            if (buildQuadsBuffer != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buildQuadsBuffer);
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            ms.pop();
        }
    };
    private final EventListener<WorldChangeEvent> world = e -> this.stop();

    public void removeAllArmor() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack currentArmor;
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR || (currentArmor = CounterMineOld.mc.player.getEquippedStack(slot)).isEmpty()) continue;
            CounterMineOld.mc.player.getInventory().insertStack(currentArmor.copy());
            CounterMineOld.mc.player.equipStack(slot, ItemStack.EMPTY);
        }
    }

    private Rotation calculateRotation(Vec3d targetPos) {
        Vec3d eyes = CounterMineOld.mc.player.getCameraPosVec(1.0f);
        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Rotation(yaw, pitch);
    }

    private Vec3d getAimPosition(Entity target) {
        Vec3d pos = target.getPos();
        return pos.add(0.0, (double)0.1f, 0.0);
    }

    public static boolean shouldHideEntity(DisplayEntity.ItemDisplayEntity entity) {
        String modelId = CounterMineOld.getModelIdFromNbt(entity.getItemStack(), (RegistryWrapper.WrapperLookup)MinecraftClient.getInstance().player.getRegistryManager());
        CounterMineOld mod = Rockstar.getInstance().getModuleManager().getModule(CounterMineOld.class);
        if (modelId != null) {
            String modelJson = CounterMineOld.findHashedModel(modelId);
            return modelJson != null && (modelJson.contains("smoke_sprite_transparent") && mod.noSmoke.isEnabled() || modelJson.contains(",\"textures\":{\"arms\":\"") && mod.noF5.isEnabled() && CounterMineOld.mc.options.getPerspective() != Perspective.FIRST_PERSON || modelJson.contains("\"textures\":{\"particle\":\"item/") && modelJson.contains("\",\"skin\":\"item/") && mod.noF5.isEnabled() && CounterMineOld.mc.options.getPerspective() != Perspective.FIRST_PERSON);
        }
        return false;
    }

    public void savePacket(SendPacketEvent e) {
        if (this.replaying || !EntityUtility.isInGame() || !this.fakeLag.isEnabled()) {
            return;
        }
        this.packets.add(e.getPacket());
        e.cancel();
        if (this.timer.finished(600L) || !this.packets.stream().filter(packet -> packet instanceof PlayerActionC2SPacket).toList().isEmpty()) {
            this.stop();
            this.start();
            this.timer.reset();
        }
    }

    public void start() {
        if (CounterMineOld.mc.player == null) {
            return;
        }
        this.packets.clear();
        this.lastPos = CounterMineOld.mc.player.getPos();
        this.timer.reset();
        this.replaying = false;
    }

    public void stop() {
        if (CounterMineOld.mc.player == null) {
            return;
        }
        this.replaying = true;
        for (Packet<?> p : this.packets) {
            CounterMineOld.mc.player.networkHandler.sendPacket(p);
        }
        this.replaying = false;
        this.packets.clear();
        this.lastPos = null;
    }

    @Override
    public void onDisable() {
        this.stop();
        Rockstar.getInstance().getTargetManager().reset();
    }

    public boolean isPlayerOnline(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }
        for (PlayerListEntry entry : networkHandler.getPlayerList()) {
            if (!entry.getProfile().getName().equals(playerName)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String findHashedModel(String hashedId) {
        ResourceManager resourceManager = mc.getResourceManager();
        Identifier modelPath = Identifier.of((String)"minecraft", (String)("models/item/" + hashedId.replace("minecraft:", "") + ".json"));
        Optional resource = resourceManager.getResource(modelPath);
        if (!resource.isPresent()) {
            return null;
        }
        try (BufferedReader reader = ((Resource)resource.get()).getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438: " + e.getMessage());
            return null;
        }
    }

    public static String getModelIdFromNbt(ItemStack itemStack, RegistryWrapper.WrapperLookup registryManager) {
        NbtCompound components;
        NbtCompound compound;
        NbtElement nbt = itemStack.toNbtAllowEmpty(registryManager);
        if (nbt instanceof NbtCompound && (compound = (NbtCompound)nbt).contains("components", 10) && (components = compound.getCompound("components")).contains("minecraft:item_model", 8)) {
            return components.getString("minecraft:item_model");
        }
        return null;
    }

    public static boolean isHologramNearby(Entity entity, ClientWorld world, double searchRadius) {
        for (Entity nearbyEntity : world.getEntities()) {
            DisplayEntity.TextDisplayEntity textDisplay;
            if (!(nearbyEntity instanceof DisplayEntity.TextDisplayEntity) || (textDisplay = (DisplayEntity.TextDisplayEntity)nearbyEntity).getText() == null || textDisplay.getText().getString().isEmpty() || !((double)entity.distanceTo((Entity)textDisplay) < searchRadius)) continue;
            return true;
        }
        return false;
    }

    public static boolean isEntityNearGround(Entity entity, World world, double maxDistance) {
        BlockPos entityPos = entity.getBlockPos();
        double entityY = entity.getY();
        for (int y = entityPos.getY(); y >= entityPos.getY() - 3 && y >= world.getBottomY(); --y) {
            BlockPos checkPos = new BlockPos(entityPos.getX(), y, entityPos.getZ());
            BlockState blockState = world.getBlockState(checkPos);
            VoxelShape collisionShape = blockState.getCollisionShape((BlockView)world, checkPos);
            if (collisionShape.isEmpty()) continue;
            double blockTopY = (double)checkPos.getY() + collisionShape.getMax(Direction.Axis.Y);
            double distance = entityY - blockTopY;
            if (!(distance <= maxDistance) || !(distance >= 0.0)) break;
            return true;
        }
        return false;
    }

    static class Head {
        Entity entity;
        boolean isFriend;
        final List<Position> poses = new ArrayList<Position>();

        @Generated
        public Head(Entity entity, boolean isFriend) {
            this.entity = entity;
            this.isFriend = isFriend;
        }
    }

    static class Position {
        Vec3d cords;
        final Timer age = new Timer();

        @Generated
        public Position(Vec3d cords) {
            this.cords = cords;
        }
    }
}
