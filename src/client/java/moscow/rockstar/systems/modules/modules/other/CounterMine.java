/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.gui.hud.BossBarHud
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.client.option.Perspective
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  net.minecraft.client.render.BuiltBuffer
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.DisplayEntity$ItemDisplayEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.game.countermine.AntiAim;
import moscow.rockstar.utility.game.countermine.CMUtility;
import moscow.rockstar.utility.game.countermine.Point;
import moscow.rockstar.utility.game.countermine.PositionScanner;
import moscow.rockstar.utility.game.countermine.RageBot;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name="Counter Mine", category=ModuleCategory.OTHER, desc="\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0443\u0431\u0438\u0432\u0430\u0435\u0442 \u043a\u0440\u0438\u043f\u0435\u0440\u043e\u0432 \u043d\u0430 \u0444\u0430\u0440\u043c\u0438\u043b\u043a\u0435")
public class CounterMine
extends BaseModule {
    private final BooleanSetting moderDetect = new BooleanSetting(this, "ModerDetect");
    private final BooleanSetting wallHack = new BooleanSetting(this, "WallHack");
    private final BooleanSetting noF5 = new BooleanSetting(this, "No F5");
    private final BooleanSetting noSmoke = new BooleanSetting(this, "HideSmoke");
    private final BooleanSetting hideScope = new BooleanSetting(this, "HideScope");
    private final BindSetting pickAssist = new BindSetting(this, "Pick Assist");
    private final BindSetting minDamageBind = new BindSetting(this, "MinDamage");
    private boolean minDamage;
    private final Timer moderTimer = new Timer();
    private final AntiAim antiAim;
    private final RageBot rageBot;
    private final PositionScanner scanner = new PositionScanner();
    private final Timer jumping = new Timer();
    private boolean jump = true;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (this.moderTimer.finished(5000L)) {
            List<String> moders = Arrays.asList("corisabi", "petiuka", "johnebik", "sherlock");
            for (String moderator : moders) {
                if (!CMUtility.isPlayerOnline(moderator)) continue;
                MessageUtility.info(Text.of((String)("\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440: " + moderator)));
                if (this.moderDetect.isEnabled()) {
                    CounterMine.mc.player.networkHandler.sendChatCommand("hub");
                }
                this.moderTimer.reset();
                break;
            }
        }
        if (this.jumping.finished(100L) && CounterMine.mc.player.isOnGround() && !this.jump) {
            this.minDamage = true;
            CounterMine.mc.player.jump();
            this.minDamage = false;
            this.jump = true;
        }
    };
    private final EventListener<HudRenderEvent> onRender = event -> {
        if (this.minDamage) {
            event.getContext().drawCenteredText(Fonts.MEDIUM.getFont(11.0f), "Min-Damange", sr.getScaledWidth() / 2.0f, sr.getScaledHeight() / 2.0f + 10.0f, ColorRGBA.WHITE);
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (event.getAction() == 1 && CounterMine.mc.currentScreen == null) {
            if (this.pickAssist.isKey(event.getKey())) {
                CounterMine.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(CounterMine.mc.player.getX(), CounterMine.mc.player.getY() + 10.0, CounterMine.mc.player.getZ(), CounterMine.mc.player.getYaw(), CounterMine.mc.player.getPitch(), false, false));
            }
            if (this.minDamageBind.isKey(event.getKey())) {
                this.minDamage = !this.minDamage;
            }
        }
    };
    private final EventListener<MouseEvent> onMouseButtonPress = event -> {
        if (event.getAction() == 1 && CounterMine.mc.currentScreen == null) {
            if (this.pickAssist.isKey(event.getButton())) {
                CounterMine.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(CounterMine.mc.player.getX(), CounterMine.mc.player.getY() + 10.0, CounterMine.mc.player.getZ(), CounterMine.mc.player.getYaw(), CounterMine.mc.player.getPitch(), false, false));
            }
            if (this.minDamageBind.isKey(event.getButton())) {
                this.minDamage = !this.minDamage;
            }
        }
    };
    private final EventListener<HudRenderEvent> on2DRender = event -> {
        if (this.noF5.isEnabled()) {
            CMUtility.removeAllArmor();
        }
    };
    private final EventListener<ReceivePacketEvent> onPacket = event -> {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            event.cancel();
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        BossBarHud boss = CounterMine.mc.inGameHud.getBossBarHud();
        if (boss != null && this.hideScope.isEnabled()) {
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
                        if (!text.getString().contains("\ub8f3\ua223\ua203\ub8f2\ua223\ua205")) {
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
        if (CounterMine.mc.world == null || CounterMine.mc.player == null || !this.wallHack.isEnabled()) {
            return;
        }
        MatrixStack ms = event.getMatrices();
        Camera camera = CounterMine.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        ms.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Identifier id = Rockstar.id("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)id);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Point point : this.scanner.getPoints()) {
            float bigSize = 4.0f;
            float size = 1.2f;
            ms.push();
            RenderUtility.prepareMatrices(ms, point.getPos());
            ms.multiply(camera.getRotation());
            DrawUtility.drawImage(ms, builder, (double)(-bigSize / 2.0f), (double)(-bigSize / 2.0f), 0.0, (double)bigSize, (double)bigSize, (point.isFriend() ? Colors.GREEN : Colors.ACCENT).withAlpha(12.75f));
            DrawUtility.drawImage(ms, builder, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, (point.isFriend() ? Colors.GREEN : Colors.ACCENT).withAlpha(102.0f));
            ms.pop();
        }
        BuiltBuffer builtLinesBuffer1 = builder.endNullable();
        if (builtLinesBuffer1 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtLinesBuffer1);
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        ms.pop();
    };
    private final EventListener<EntityJumpEvent> onJump = event -> {
        if (event.getEntity() == CounterMine.mc.player && !this.minDamage) {
            event.cancel();
            this.jumping.reset();
            this.jump = false;
        }
    };

    public CounterMine() {
        this.rageBot = new RageBot(this);
        this.antiAim = new AntiAim(this);
    }

    public static boolean shouldHideEntity(DisplayEntity.ItemDisplayEntity entity) {
        String modelId = CMUtility.getModelIdFromNbt(entity.getItemStack(), (RegistryWrapper.WrapperLookup)MinecraftClient.getInstance().player.getRegistryManager());
        CounterMine mod = Rockstar.getInstance().getModuleManager().getModule(CounterMine.class);
        if (modelId != null) {
            String modelJson = CMUtility.findHashedModel(modelId);
            return modelJson != null && (modelJson.contains("smoke_sprite_transparent") && mod.noSmoke.isEnabled() || modelJson.contains(",\"textures\":{\"arms\":\"") && mod.noF5.isEnabled() && CounterMine.mc.options.getPerspective() != Perspective.FIRST_PERSON || modelJson.contains("\"textures\":{\"particle\":\"item/") && modelJson.contains("\",\"skin\":\"item/") && mod.noF5.isEnabled() && CounterMine.mc.options.getPerspective() != Perspective.FIRST_PERSON && CounterMine.mc.player.distanceTo((Entity)entity) < 3.0f);
        }
        return false;
    }

    @Override
    public void onEnable() {
        Rockstar.getInstance().getEventManager().subscribe(this.scanner);
        Rockstar.getInstance().getEventManager().subscribe(this.antiAim);
        Rockstar.getInstance().getEventManager().subscribe(this.rageBot);
    }

    @Override
    public void onDisable() {
        Rockstar.getInstance().getTargetManager().reset();
        Rockstar.getInstance().getEventManager().unsubscribe(this.scanner);
        Rockstar.getInstance().getEventManager().unsubscribe(this.antiAim);
        Rockstar.getInstance().getEventManager().unsubscribe(this.rageBot);
    }

    @Generated
    public BooleanSetting getHideScope() {
        return this.hideScope;
    }

    @Generated
    public boolean isMinDamage() {
        return this.minDamage;
    }

    @Generated
    public AntiAim getAntiAim() {
        return this.antiAim;
    }

    @Generated
    public PositionScanner getScanner() {
        return this.scanner;
    }

    @Generated
    public Timer getJumping() {
        return this.jumping;
    }
}
