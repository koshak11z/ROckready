package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@ModuleInfo(name = "AutoMine", category = ModuleCategory.PLAYER, desc = "Автошахта: полный порт исходной state-machine логики")
public final class AutoMine extends BaseModule {


    private final BooleanSetting disableVisualRotation = new BooleanSetting(
            this,
            "Отключить визуал ротку",
            "Камера визуально не поворачивается, ротация отправляется только серверу"
    );

    // ===== Территория автошахты =====
    private static final double REACH = 4.0;
    private static final double REACH_SQ = REACH * REACH;
    private static final int MIN_X = 43, MAX_X = 61;
    private static final int MIN_Z = 38, MAX_Z = 56;
    private static final int MIN_Y = 73, MAX_Y = 76;

    // ===== Хаб (координаты + радиус) и реконнект =====
    private static final int HUB_X = 317, HUB_Y = 29, HUB_Z = 302;
    private static final double HUB_RADIUS = 20.0;
    private static final double HUB_RADIUS_SQ = HUB_RADIUS * HUB_RADIUS;
    private static final long HUB_JOIN_TIMEOUT_MS = 60000; // минута на заход
    private static final long HUB_NUDGE_MS = 700;          // пройти пару блоков вперёд
    private static final String SERVER_IP = "mc.holyworld.ru";
    private static final long RECONNECT_DELAY_MS = 3000;    // пауза перед попыткой реконнекта

    // ===== Параметры человеческой ротки (быстро, но не телепорт) =====
    private static final float ALIGN_DEG    = 6.0f;
    private static final float ROT_FACTOR   = 0.7f;
    private static final float ROT_BASE     = 6.0f;
    private static final float ROT_MAX_STEP = 80.0f;
    private static final float ROT_NOISE    = 1.5f;

    // ===== Автозаход =====
    private static final int MIN_ORES = 40;
    private static final int MENU_SLOT = 12;
    private static final int MIN_ANARCHY = 1;
    private static final int MAX_ANARCHY = 64;

    private static final Set<Integer> SKIP = new HashSet<>();
    static {
        SKIP.add(1); SKIP.add(2); SKIP.add(3);
        SKIP.add(16); SKIP.add(17); SKIP.add(33); SKIP.add(49);
    }

    private static final int[] LEAGUE_SLOTS = {0, 1, 2, 3};
    private static final int HEAD_FIRST_SLOT = 18;
    private int targetAnarchy = MIN_ANARCHY;

    // ===== Починка кирки =====
    private static final int LOW_DURABILITY    = 50;
    private static final String XP_NAME        = "50";
    private static final long REPAIR_STEP_MS   = 350;
    private static final long REPAIR_COOLDOWN  = 5000;

    private static final long WARP_DELAY_MS = 500;
    private static final long REWARP_MS     = 12000;
    private static final long DROP_INTERVAL = 700;

    private static final long MINE_STUCK_MS      = 4000;
    private static final long BLACKLIST_CLEAR_MS = 20000;
    private static final int  MAX_STUCK_BEFORE_REWARP = 3;

    // ===== Анти-афк (микро-застревание по реальным координатам) =====
    private static final long MICRO_STUCK_MS   = 600;
    private static final long REROUTE_STUCK_MS = 2500;
    private static final double MOVE_EPS_SQ    = 0.0025;

    // ===== Анти-флаг на полублоках =====
    private static final long SLAB_ESCAPE_MS = 800;
    private static final long SLAB_STOP_MS   = 500;
    private static final long SLAB_CLEAR_MS  = 400;

    private static final long SILENT_FRESH_MS = 150;

    private static final long HUB_GUARD_MS      = 250;
    private static final long OPEN_SYNC_MS      = 350;
    private static final long LEAGUE_REFRESH_MS = 350;
    private static final long POST_MS           = 900;
    private static final long STUCK_MS          = 6000;

    private static final Set<Block> ORES = new HashSet<>();
    static {
        ores(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
                Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
                Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
                Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
                Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS);
    }
    private static void ores(Block... b) { for (Block x : b) ORES.add(x); }

    // ===== Состояние =====
    private enum State { MINE, REJOIN, REPAIR }
    private State state = State.MINE;

    private int step = 0;
    private int menuSyncId = -1;
    private boolean leagueClicked = false;

    private int repairStep = 0;
    private int xpSlot = -1;

    private final Timer dropTimer      = new Timer();
    private final Timer warpTimer      = new Timer();
    private final Timer stuckTimer     = new Timer();
    private final Timer actionTimer    = new Timer();
    private final Timer rejoinTimer    = new Timer();
    private final Timer repairTimer    = new Timer();
    private final Timer mineStuckTimer = new Timer();
    private final Timer blacklistTimer = new Timer();
    private final Timer silentTimer    = new Timer();
    private final Timer microStuckTimer = new Timer();
    private final Timer slabFlagTimer  = new Timer();
    private final Timer slabClearTimer = new Timer();

    // ===== Хаб / реконнект =====
    private final Timer hubJoinTimer   = new Timer();
    private final Timer hubNudgeTimer  = new Timer();
    private final Timer reconnectTimer = new Timer();
    private boolean hubActive = false;
    private boolean hubNudging = false;
    private boolean justReconnected = false;

    private boolean warpDone = false;

    private BlockPos currentTarget;
    private BlockPos lastTarget;
    private BlockPos lastPos;
    private int stuckCount = 0;
    private final Set<BlockPos> blacklist = new HashSet<>();

    private double lastMoveX, lastMoveZ;

    // ===== Анти-флаг на полублоках =====
    private boolean slabRecovering = false;
    private int slabStage = 0;

    // ===== Ротация / ходьба =====
    private float serverYaw, serverPitch;
    private float targetYaw, targetPitch;
    private float moveYaw;
    private boolean rotInit = false;
    private boolean silentRotationActive = false;

    @Override
    public void onEnable() {
        warpDone = false;
        state = State.MINE;
        step = 0;
        menuSyncId = -1;
        leagueClicked = false;
        repairStep = 0;
        xpSlot = -1;
        targetAnarchy = firstAnarchy();
        warpTimer.reset();
        stuckTimer.reset();
        dropTimer.reset();
        actionTimer.reset();
        rejoinTimer.reset();
        repairTimer.reset();
        mineStuckTimer.reset();
        blacklistTimer.reset();
        silentTimer.reset();
        microStuckTimer.reset();
        slabFlagTimer.reset();
        slabClearTimer.reset();
        hubJoinTimer.reset();
        hubNudgeTimer.reset();
        reconnectTimer.reset();
        hubActive = false;
        hubNudging = false;
        justReconnected = false;
        currentTarget = null;
        lastTarget = null;
        lastPos = null;
        stuckCount = 0;
        blacklist.clear();
        rotInit = false;
        silentRotationActive = false;
        slabRecovering = false;
        slabStage = 0;
        if (mc.player != null) { lastMoveX = mc.player.getX(); lastMoveZ = mc.player.getZ(); }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        releaseKeys();
        currentTarget = null;
        silentRotationActive = false;
        slabRecovering = false;
        slabStage = 0;
        hubActive = false;
        hubNudging = false;
        super.onDisable();
    }

    // ===== ЕДИНАЯ ХОДЬБА ИЗ ПРИКРЕПЛЁННОГО AutoMine =====
    // Важно: RotationHandler для AutoMine ниже вызывается с MoveCorrection.NONE.
    // Иначе Rockstar RotationUpdateListener делает вторую коррекцию поверх этой и ходьба становится кривой.
    private final EventListener<InputEvent> onMoveInput = event -> {
        if (!disableVisualRotation.isEnabled()) return;
        if (!silentRotationActive) return;
        if (silentTimer.finished(SILENT_FRESH_MS)) return;
        if (mc.player == null) return;
        event.setYaw(serverYaw, moveYaw);
    };

    private final EventListener<ClientPlayerTickEvent> onTick = event -> this.onUpdate();

    private void onUpdate() {
        // ===== Вылет/краш: нет мира/игрока → пробуем реконнект =====
        if (mc.world == null || mc.player == null || mc.interactionManager == null) {
            handleDisconnect();
            return;
        }

        silentRotationActive = false;

        // только что переподключились → целимся на ПЕРВУЮ анархию
        if (justReconnected) {
            justReconnected = false;
            targetAnarchy = firstAnarchy();
            hubActive = false;
            hubNudging = false;
            warpDone = false;
            state = State.MINE;
            hubJoinTimer.reset();
            reconnectTimer.reset();
        }

        // ===== Если стоим в хабе у (317,29,302) в радиусе 20 — заходим на анку =====
        if (inHub()) {
            handleHub();
            return;
        } else {
            hubActive = false;
            hubNudging = false;
        }

        if (state == State.REJOIN) { releaseKeys(); handleRejoin(); return; }
        if (state == State.REPAIR) { releaseKeys(); handleRepair(); return; }

        equipPickaxe();

        ItemStack pick = mc.player.getMainHandStack();
        if (matchesPick(pick) && pick.isDamageable() && repairTimer.finished(REPAIR_COOLDOWN)) {
            int rem = pick.getMaxDamage() - pick.getDamage();
            if (rem <= LOW_DURABILITY) { releaseKeys(); startRepair(); return; }
        }

        if (dropTimer.finished(DROP_INTERVAL)) { dropTrash(); dropTimer.reset(); }

        if (blacklistTimer.finished(BLACKLIST_CLEAR_MS)) { blacklist.clear(); blacklistTimer.reset(); }

        if (!warpDone && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand("warp mine");
            warpDone = true; warpTimer.reset(); stuckTimer.reset();
            mineStuckTimer.reset(); microStuckTimer.reset();
            slabFlagTimer.reset(); slabClearTimer.reset();
            slabRecovering = false; slabStage = 0;
            lastPos = null; lastTarget = null;
            currentTarget = null;
            lastMoveX = mc.player.getX(); lastMoveZ = mc.player.getZ();
            releaseKeys();
            return;
        }
        if (!warpTimer.finished(WARP_DELAY_MS)) { releaseKeys(); return; }

        if (!playerNearRegion()) {
            releaseKeys();
            if (stuckTimer.finished(REWARP_MS)) { warpDone = false; stuckTimer.reset(); }
            return;
        }

        // ===== Анти-флаг на полублоках =====
        if (onSlab() || slabRecovering) {
            handleSlabFlag();
            return;
        }

        Scan scan = scanRegion();
        if (scan.oreCount < MIN_ORES) { releaseKeys(); startRejoin(); return; }

        // ===== Микро-детект застревания =====
        double mdx = mc.player.getX() - lastMoveX;
        double mdz = mc.player.getZ() - lastMoveZ;
        if (mdx * mdx + mdz * mdz > MOVE_EPS_SQ) {
            lastMoveX = mc.player.getX();
            lastMoveZ = mc.player.getZ();
            microStuckTimer.reset();
        }

        // ===== Блочный анти-стак =====
        BlockPos cur = mc.player.getBlockPos();
        boolean moved = (lastPos == null) || !cur.equals(lastPos);
        boolean targetChanged = (currentTarget == null) || (lastTarget == null) || !currentTarget.equals(lastTarget);
        if (moved || targetChanged) {
            lastPos = cur;
            lastTarget = currentTarget;
            mineStuckTimer.reset();
        } else if (mineStuckTimer.finished(MINE_STUCK_MS)) {
            if (currentTarget != null) blacklist.add(currentTarget);
            currentTarget = null;
            lastPos = null;
            lastTarget = null;
            mineStuckTimer.reset();
            microStuckTimer.reset();
            stuckCount++;
            if (stuckCount >= MAX_STUCK_BEFORE_REWARP) {
                stuckCount = 0;
                blacklist.clear();
                warpDone = false;
                releaseKeys();
            }
            return;
        }

        int py = mc.player.getBlockY();

        // ===== Фаза 1: спуск лесенкой до низа =====
        if (py > MIN_Y) {
            descendStaircase(py);
            currentTarget = null;
            return;
        }

        // ===== Фаза 2: фарм =====
        if (currentTarget != null) {
            BlockState st = mc.world.getBlockState(currentTarget);
            if (!isOre(st) || !inRegion(currentTarget) || blacklist.contains(currentTarget)) currentTarget = null;
        }
        if (currentTarget == null) currentTarget = scan.ore;
        if (currentTarget == null) { blacklist.clear(); return; }

        mineTowards(currentTarget);
    }

    // ===================================================================
    // =====================  ХАБ + РЕКОННЕКТ  ===========================
    // ===================================================================

    private boolean inHub() {
        if (mc.player == null) return false;
        double dx = mc.player.getX() - (HUB_X + 0.5);
        double dy = mc.player.getY() - HUB_Y;
        double dz = mc.player.getZ() - (HUB_Z + 0.5);
        return dx * dx + dy * dy + dz * dz <= HUB_RADIUS_SQ;
    }

    // Пока в хабе — крутим заход на анку до победного; за минуту не вышли → пара блоков вперёд и заново.
    private void handleHub() {
        // фаза "пройти пару блоков вперёд и заново"
        if (hubNudging) {
            if (!hubNudgeTimer.finished(HUB_NUDGE_MS)) {
                if (mc.options != null) {
                    mc.options.forwardKey.setPressed(true);
                    mc.options.sprintKey.setPressed(true);
                    mc.options.jumpKey.setPressed(false);
                    mc.options.sneakKey.setPressed(false);
                    mc.options.attackKey.setPressed(false);
                }
                if (mc.player != null) mc.player.setSprinting(true);
                return;
            }
            hubNudging = false;
            releaseKeys();
            hubJoinTimer.reset();
            restartHubJoin();
            return;
        }

        releaseKeys();

        // запуск процесса захода
        if (!hubActive) {
            hubActive = true;
            hubJoinTimer.reset();
            restartHubJoin();
            return;
        }

        // не зашли за минуту → пройти пару блоков и заново
        if (hubJoinTimer.finished(HUB_JOIN_TIMEOUT_MS)) {
            if (mc.currentScreen != null) mc.player.closeHandledScreen();
            hubNudging = true;
            hubNudgeTimer.reset();
            return;
        }

        // крутим стейт-машину захода (меню по компасу → лига → номер анки)
        if (state == State.REJOIN) {
            handleRejoin();
        } else {
            restartHubJoin();
        }
    }

    // Реконнект при вылете/краше: переподключаемся на SERVER_IP, после входа — первая анка.
    private void handleDisconnect() {
        justReconnected = true; // как вернёмся в мир — зайдём на первую анку
        releaseKeys();

        boolean onDisconnectLikeScreen =
                mc.currentScreen instanceof DisconnectedScreen
                        || mc.currentScreen instanceof TitleScreen
                        || mc.currentScreen instanceof MultiplayerScreen;

        // во время самого подключения (ConnectScreen) ничего не спамим
        boolean connecting = mc.currentScreen instanceof ConnectScreen;

        if (onDisconnectLikeScreen && !connecting) {
            if (reconnectTimer.finished(RECONNECT_DELAY_MS)) {
                reconnectToServer();
                reconnectTimer.reset();
            }
        } else {
            reconnectTimer.reset();
        }
    }

    private void reconnectToServer() {
        try {
            ServerInfo info = new ServerInfo("HolyWorld", SERVER_IP, ServerInfo.ServerType.OTHER);
            ServerAddress address = ServerAddress.parse(SERVER_IP);
            if (tryConnectScreen(address, info)) return;
        } catch (Throwable ignored) {
        }
        try { mc.setScreen(new MultiplayerScreen(new TitleScreen())); } catch (Throwable ignoredAgain) {}
    }

    private boolean tryConnectScreen(ServerAddress address, ServerInfo info) {
        for (java.lang.reflect.Method method : ConnectScreen.class.getDeclaredMethods()) {
            if (!"connect".equals(method.getName())) continue;
            try {
                method.setAccessible(true);
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 4) {
                    // 1.21.x private static connect(MinecraftClient, ServerAddress, ServerInfo, CookieStorage)
                    method.invoke(null, mc, address, info, (CookieStorage)null);
                    return true;
                }
                if (params.length == 6) {
                    // 1.21.4 public/static overload: connect(Screen, MinecraftClient, ServerAddress, ServerInfo, boolean, CookieStorage)
                    method.invoke(null, new TitleScreen(), mc, address, info, false, (CookieStorage)null);
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    // ===== Анти-флаг: стоим ли на полублоке =====
    private boolean onSlab() {
        if (mc.player == null) return false;
        if (!mc.player.isOnGround()) return false;
        BlockPos below = mc.player.getBlockPos().down();
        return isSlab(below);
    }

    private void handleSlabFlag() {
        if (!slabRecovering) {
            slabRecovering = true;
            slabStage = 0;
            slabFlagTimer.reset();
            slabClearTimer.reset();
        }

        if (!onSlab()) {
            if (slabClearTimer.finished(SLAB_CLEAR_MS)) {
                endSlabRecovery();
                releaseKeys();
                return;
            }
            if (mc.options != null) {
                mc.options.sneakKey.setPressed(false);
                mc.options.forwardKey.setPressed(true);
                mc.options.sprintKey.setPressed(true);
                mc.options.jumpKey.setPressed(false);
                mc.options.attackKey.setPressed(false);
            }
            if (mc.player != null) mc.player.setSprinting(true);
            faceWalk((MIN_X + MAX_X) / 2.0 + 0.5, (MIN_Z + MAX_Z) / 2.0 + 0.5);
            return;
        }

        slabClearTimer.reset();

        switch (slabStage) {
            case 0:
                if (mc.options != null) {
                    mc.options.sneakKey.setPressed(true);
                    mc.options.forwardKey.setPressed(true);
                    mc.options.sprintKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    mc.options.attackKey.setPressed(false);
                }
                if (mc.player != null) mc.player.setSprinting(false);
                faceWalk((MIN_X + MAX_X) / 2.0 + 0.5, (MIN_Z + MAX_Z) / 2.0 + 0.5);
                if (slabFlagTimer.finished(SLAB_ESCAPE_MS)) {
                    slabStage = 1;
                    slabFlagTimer.reset();
                }
                break;
            case 1:
                releaseKeys();
                if (slabFlagTimer.finished(SLAB_STOP_MS)) {
                    if (onSlab()) {
                        warpDone = false;
                        currentTarget = null; lastPos = null; lastTarget = null;
                    }
                    endSlabRecovery();
                }
                break;
        }
    }

    private void endSlabRecovery() {
        slabRecovering = false;
        slabStage = 0;
        slabFlagTimer.reset();
        slabClearTimer.reset();
    }

    // ===== Спуск лесенкой =====
    private void descendStaircase(int py) {
        if (!inRegionXZ()) {
            double cx = (MIN_X + MAX_X) / 2.0 + 0.5;
            double cz = (MIN_Z + MAX_Z) / 2.0 + 0.5;
            faceWalk(cx, cz);
            setKeys(true);
            return;
        }

        int dirX = 0, dirZ = 0;
        boolean useX = (mc.player.getX() - MIN_X) > (MAX_X - mc.player.getX())
                ? (mc.player.getX() - MIN_X) >= 2
                : (MAX_X - mc.player.getX()) >= 2;
        if (useX) dirX = (mc.player.getX() - MIN_X) > (MAX_X - mc.player.getX()) ? -1 : 1;
        else dirZ = (mc.player.getZ() - MIN_Z) > (MAX_Z - mc.player.getZ()) ? -1 : 1;

        int fx = mc.player.getBlockX() + dirX;
        int fz = mc.player.getBlockZ() + dirZ;

        if (fx < MIN_X || fx > MAX_X) { dirX = 0; dirZ = (mc.player.getZ() - MIN_Z) > (MAX_Z - mc.player.getZ()) ? -1 : 1; fx = mc.player.getBlockX(); fz = mc.player.getBlockZ() + dirZ; }
        if (fz < MIN_Z || fz > MAX_Z) { dirZ = 0; dirX = (mc.player.getX() - MIN_X) > (MAX_X - mc.player.getX()) ? -1 : 1; fz = mc.player.getBlockZ(); fx = mc.player.getBlockX() + dirX; }

        BlockPos head = new BlockPos(fx, py + 1, fz);
        BlockPos body = new BlockPos(fx, py, fz);
        BlockPos down = new BlockPos(fx, py - 1, fz);

        faceWalk(fx + 0.5, fz + 0.5);

        boolean slabAhead = stepHasSlab(fx, fz, py);

        BlockPos toBreak = firstMineableRegion(false, body, head, down);
        if (toBreak != null) {
            aimAt(Vec3d.ofCenter(toBreak));
            breakDirect(toBreak, false);
            setKeys(!slabAhead);
        } else {
            setKeys(!slabAhead);
        }
    }

    // ===== Фарм =====
    private boolean mineTowards(BlockPos ore) {
        Vec3d eyes = mc.player.getEyePos();
        double dx = (ore.getX() + 0.5) - eyes.x;
        double dz = (ore.getZ() + 0.5) - eyes.z;

        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        int feetY = mc.player.getBlockY();

        boolean micro = microStuckTimer.finished(MICRO_STUCK_MS);

        boolean useX = Math.abs(dx) >= Math.abs(dz);
        if (micro) useX = !useX;
        int[] stepA = useX ? new int[]{dx >= 0 ? 1 : -1, 0} : new int[]{0, dz >= 0 ? 1 : -1};
        int[] stepB = useX ? new int[]{0, dz >= 0 ? 1 : -1} : new int[]{dx >= 0 ? 1 : -1, 0};

        int[] chosen = stepA;
        boolean forward = true;
        if (stepHasSlab(px + stepA[0], pz + stepA[1], feetY)) {
            if (!stepHasSlab(px + stepB[0], pz + stepB[1], feetY)) {
                chosen = stepB;
            } else {
                forward = false;
            }
        }

        int fx = px + chosen[0], fz = pz + chosen[1];

        BlockPos head = new BlockPos(fx, feetY + 1, fz);
        BlockPos feet = new BlockPos(fx, feetY, fz);
        BlockPos col = firstMineableRegion(true, head, feet);

        boolean broke;
        if (col != null) {
            aimAt(Vec3d.ofCenter(col));
            broke = breakDirect(col, true);
        } else {
            aimAt(Vec3d.ofCenter(ore));
            broke = breakDirect(ore, true);
        }

        setKeys(forward);

        if (microStuckTimer.finished(REROUTE_STUCK_MS)) {
            if (currentTarget != null) blacklist.add(currentTarget);
            currentTarget = null;
        }
        return broke;
    }

    private boolean stepHasSlab(int fx, int fz, int feetY) {
        return isSlab(new BlockPos(fx, feetY - 1, fz))
                || isSlab(new BlockPos(fx, feetY, fz))
                || isSlab(new BlockPos(fx, feetY + 1, fz));
    }

    private boolean isSlab(BlockPos p) {
        return mc.world.getBlockState(p).getBlock() instanceof SlabBlock;
    }

    private boolean breakDirect(BlockPos target, boolean enforceBand) {
        if (target == null) return false;
        Vec3d eyes = mc.player.getEyePos();
        Vec3d diff = Vec3d.ofCenter(target).subtract(eyes);
        if (diff.lengthSquared() < 1.0E-4) return false;

        double reach = Math.min(REACH, diff.length() + 0.5);
        Vec3d end = eyes.add(diff.normalize().multiply(reach));
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                eyes, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player));

        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = hit.getBlockPos();
        if (!inRegionXZ(pos)) return false;
        if (enforceBand && (pos.getY() < MIN_Y || pos.getY() > MAX_Y)) return false;
        if (!isMineable(mc.world.getBlockState(pos), pos)) return false;
        if (disableVisualRotation.isEnabled()) {
            if (!this.isSilentRotationReady()) return false;
            this.sendSilentLookPacket();
        }

        mc.interactionManager.attackBlock(pos, hit.getSide());
        mc.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private boolean isSilentRotationReady() {
        return Math.abs(wrapDegrees(targetYaw - serverYaw)) <= 2.75f && Math.abs(targetPitch - serverPitch) <= 2.75f;
    }

    private void sendSilentLookPacket() {
        if (mc.player == null || mc.player.networkHandler == null) return;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ(),
                serverYaw,
                serverPitch,
                mc.player.isOnGround(),
                mc.player.horizontalCollision
        ));
    }

    private BlockPos firstMineableRegion(boolean fullRegion, BlockPos... ps) {
        for (BlockPos p : ps) {
            if (!inRegionXZ(p)) continue;
            if (fullRegion && !inRegion(p)) continue;
            if (isMineable(mc.world.getBlockState(p), p)) return p;
        }
        return null;
    }

    private boolean inRegionXZ() {
        int x = mc.player.getBlockX(), z = mc.player.getBlockZ();
        return x >= MIN_X && x <= MAX_X && z >= MIN_Z && z <= MAX_Z;
    }

    private boolean inRegionXZ(BlockPos p) {
        return p.getX() >= MIN_X && p.getX() <= MAX_X && p.getZ() >= MIN_Z && p.getZ() <= MAX_Z;
    }

    // ===== Починка кирки =====
    private void startRepair() {
        state = State.REPAIR;
        repairStep = 0;
        xpSlot = -1;
        repairTimer.reset();
        actionTimer.reset();
    }

    private void handleRepair() {
        switch (repairStep) {
            case 0: {
                equipPickaxe();
                int xp = findByName(XP_NAME);
                if (xp < 0) { state = State.MINE; repairStep = 0; return; }
                xpSlot = xp;
                swapToOffhand(xpSlot);
                repairStep = 1;
                actionTimer.reset();
                break;
            }
            case 1:
                if (actionTimer.finished(REPAIR_STEP_MS)) {
                    if (disableVisualRotation.isEnabled()) {
                        serverYaw = mc.player.getYaw();
                        serverPitch = 90.0f;
                        targetYaw = serverYaw;
                        targetPitch = serverPitch;
                        this.sendSilentLookPacket();
                    }
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                    repairStep = 2;
                    actionTimer.reset();
                }
                break;
            case 2:
                if (actionTimer.finished(REPAIR_STEP_MS)) {
                    if (xpSlot >= 0) swapToOffhand(xpSlot);
                    state = State.MINE;
                    repairStep = 0;
                    xpSlot = -1;
                    actionTimer.reset();
                }
                break;
        }
    }

    // ===== Автозаход =====
    private void startRejoin() { startRejoin(true); }

    // sendHub=false — когда уже в хабе (не телепортируемся /hub-ом заново)
    private void startRejoin(boolean sendHub) {
        state = State.REJOIN;
        step = 0;
        menuSyncId = -1;
        leagueClicked = false;
        currentTarget = null;
        lastTarget = null;
        lastPos = null;
        stuckCount = 0;
        blacklist.clear();
        if (sendHub && mc.getNetworkHandler() != null) mc.getNetworkHandler().sendChatCommand("hub");
        actionTimer.reset();
        rejoinTimer.reset();
    }

    private void restartHubJoin() { startRejoin(false); }

    private void handleRejoin() {
        boolean open = mc.currentScreen instanceof HandledScreen;
        int syncId = open ? mc.player.currentScreenHandler.syncId : -1;

        if (rejoinTimer.finished(STUCK_MS)) {
            if (mc.currentScreen != null) mc.player.closeHandledScreen();
            step = 0; menuSyncId = -1; leagueClicked = false;
            // если в хабе — не спамим /hub, просто переоткроем компас; иначе шлём /hub
            if (!inHub() && mc.getNetworkHandler() != null) mc.getNetworkHandler().sendChatCommand("hub");
            actionTimer.reset(); rejoinTimer.reset();
            return;
        }

        switch (step) {
            case 0:
                if (open) {
                    menuSyncId = syncId;
                    step = 1;
                    actionTimer.reset();
                } else if (actionTimer.finished(HUB_GUARD_MS)) {
                    if (equipCompass()) mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    actionTimer.reset();
                }
                break;
            case 1:
                if (!open) { step = 0; actionTimer.reset(); break; }
                if (syncId != menuSyncId) {
                    menuSyncId = syncId;
                    leagueClicked = false;
                    step = 2;
                    actionTimer.reset();
                    rejoinTimer.reset();
                    break;
                }
                if (actionTimer.finished(OPEN_SYNC_MS)) { clickContainer(MENU_SLOT); actionTimer.reset(); }
                break;
            case 2:
                if (!open) { step = 0; actionTimer.reset(); break; }
                if (!leagueClicked) {
                    if (actionTimer.finished(OPEN_SYNC_MS)) {
                        clickContainer(leagueSlot(targetAnarchy));
                        leagueClicked = true;
                        actionTimer.reset();
                    }
                } else {
                    if (actionTimer.finished(LEAGUE_REFRESH_MS)) {
                        clickContainer(anarchySlot(targetAnarchy));
                        step = 3;
                        actionTimer.reset();
                        rejoinTimer.reset();
                    }
                }
                break;
            case 3:
                if (actionTimer.finished(POST_MS)) {
                    if (mc.currentScreen != null) mc.player.closeHandledScreen();
                    advanceAnarchy();
                    warpDone = false;
                    state = State.MINE;
                    step = 0;
                    menuSyncId = -1;
                    leagueClicked = false;
                }
                break;
        }
    }

    private int firstAnarchy() {
        int a = MIN_ANARCHY;
        while (a <= MAX_ANARCHY && SKIP.contains(a)) a++;
        return a > MAX_ANARCHY ? MIN_ANARCHY : a;
    }

    private void advanceAnarchy() {
        for (int guard = 0; guard < MAX_ANARCHY; guard++) {
            targetAnarchy++;
            if (targetAnarchy > MAX_ANARCHY) targetAnarchy = MIN_ANARCHY;
            if (!SKIP.contains(targetAnarchy)) return;
        }
    }

    private boolean equipCompass() {
        PlayerInventory inv = mc.player.getInventory();
        if (mc.player.getMainHandStack().getItem() == Items.COMPASS) return true;
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() == Items.COMPASS) { inv.selectedSlot = i; return true; }
        }
        return false;
    }

    private void clickContainer(int slot) {
        if (!(mc.currentScreen instanceof HandledScreen)) return;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
    }

    private void swapToOffhand(int invIndex) {
        int handlerSlot = invIndex < 9 ? invIndex + 36 : invIndex;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, handlerSlot, 40, SlotActionType.SWAP, mc.player);
    }

    private int findByName(String sub) {
        PlayerInventory inv = mc.player.getInventory();
        String s = sub.toLowerCase();
        for (int i = 0; i < 36; i++) {
            ItemStack st = inv.getStack(i);
            if (st.isEmpty()) continue;
            if (st.getName().getString().toLowerCase().contains(s)) return i;
        }
        return -1;
    }

    private int leagueSlot(int a) {
        if (a <= 15) return LEAGUE_SLOTS[0];
        if (a <= 31) return LEAGUE_SLOTS[1];
        if (a <= 47) return LEAGUE_SLOTS[2];
        return LEAGUE_SLOTS[3];
    }

    private int anarchySlot(int a) {
        int within;
        if (a <= 15) within = a;
        else if (a <= 31) within = a - 15;
        else if (a <= 47) within = a - 31;
        else within = a - 47;
        return HEAD_FIRST_SLOT + (within - 1);
    }

    // ===================================================================
    // =====================  РОТАЦИЯ + ХОДЬБА  ==========================
    // ===================================================================

    private void aimAt(Vec3d point) {
        Vec3d eyes = mc.player.getEyePos();
        double dx = point.x - eyes.x, dy = point.y - eyes.y, dz = point.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (horiz > 0.05)
                ? (float) Math.toDegrees(Math.atan2(-dx, dz))
                : (rotInit ? serverYaw : mc.player.getYaw());
        double h = horiz < 1.0E-4 ? 1.0E-4 : horiz;
        float pitch = clampPitch((float) (-Math.toDegrees(Math.atan2(dy, h))));
        moveYaw = yaw;
        rotate(yaw, pitch);
    }

    private void faceWalk(double tx, double tz) {
        double dx = tx - mc.player.getX();
        double dz = tz - mc.player.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        moveYaw = yaw;
        rotate(yaw, rotInit ? serverPitch : mc.player.getPitch());
    }

    private void rotate(float yaw, float pitch) {
        pitch = clampPitch(pitch);
        targetYaw = yaw;
        targetPitch = pitch;

        if (!rotInit) {
            serverYaw = mc.player.getYaw();
            serverPitch = mc.player.getPitch();
            rotInit = true;
        }

        float dYaw = wrapDegrees(targetYaw - serverYaw);
        float dPitch = targetPitch - serverPitch;
        serverYaw = wrapDegrees(serverYaw + clampAbs(dYaw, humanStep(dYaw)));
        serverPitch = clampPitch(serverPitch + clampAbs(dPitch, humanStep(dPitch)));

        if (disableVisualRotation.isEnabled()) {
            silentRotationActive = true;
            silentTimer.reset();
            Rotation silent = new Rotation(serverYaw, serverPitch);
            Rockstar.getInstance().getRotationHandler().rotate(
                    silent,
                    MoveCorrection.NONE,
                    180.0f,
                    180.0f,
                    40.0f,
                    RotationPriority.TO_TARGET
            );
            // Не ждём следующий tick RotationHandler: иначе attackBlock может уйти до серверного look-пакета и флагнуть.
            Rockstar.getInstance().getRotationHandler().setCurrentRotation(silent);
            Rockstar.getInstance().getRotationHandler().setPrevRotation(silent);
        } else {
            silentRotationActive = false;
            mc.player.setYaw(serverYaw);
            mc.player.setPitch(serverPitch);
        }
    }

    private float humanStep(float delta) {
        float a = Math.abs(delta);
        float s = a * ROT_FACTOR + ROT_BASE;
        if (s > ROT_MAX_STEP) s = ROT_MAX_STEP;
        s += (float) ((Math.random() - 0.5) * 2.0 * ROT_NOISE);
        if (s < 1.5f) s = 1.5f;
        return s;
    }

    private static float clampAbs(float v, float max) {
        if (v > max) return max;
        if (v < -max) return -max;
        return v;
    }

    private static float wrapDegrees(float deg) {
        deg %= 360f;
        if (deg >= 180f) deg -= 360f;
        if (deg < -180f) deg += 360f;
        return deg;
    }

    private float clampPitch(float p) { return p < -90f ? -90f : (p > 90f ? 90f : p); }

    private void setKeys(boolean forward) {
        if (mc.options == null) return;
        mc.options.forwardKey.setPressed(forward);
        mc.options.sprintKey.setPressed(forward);
        mc.options.sneakKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        if (mc.player != null) mc.player.setSprinting(forward);
    }

    private void releaseKeys() {
        silentRotationActive = false;
        if (mc.options == null) return;
        mc.options.forwardKey.setPressed(false);
        mc.options.sprintKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
        if (mc.player != null) mc.player.setSprinting(false);
    }

    // ===== Скан территории =====
    private Scan scanRegion() {
        Vec3d eyes = mc.player.getEyePos();
        int oreCount = 0;
        BlockPos nearestOre = null; double dOre = Double.MAX_VALUE;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                for (int z = MIN_Z; z <= MAX_Z; z++) {
                    pos.set(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!ORES.contains(state.getBlock())) continue;
                    if (!isMineable(state, pos)) continue;
                    oreCount++;
                    if (blacklist.contains(pos)) continue;
                    double d = eyes.squaredDistanceTo(Vec3d.ofCenter(pos));
                    if (d < dOre) { dOre = d; nearestOre = pos.toImmutable(); }
                }
            }
        }
        return new Scan(oreCount, nearestOre);
    }

    private static final class Scan {
        final int oreCount; final BlockPos ore;
        Scan(int oreCount, BlockPos ore) { this.oreCount = oreCount; this.ore = ore; }
    }

    private boolean isOre(BlockState s) { return ORES.contains(s.getBlock()); }

    private boolean inRegion(BlockPos p) {
        return p.getX() >= MIN_X && p.getX() <= MAX_X
                && p.getY() >= MIN_Y && p.getY() <= MAX_Y
                && p.getZ() >= MIN_Z && p.getZ() <= MAX_Z;
    }

    private boolean playerNearRegion() {
        double cx = (MIN_X + MAX_X) / 2.0, cz = (MIN_Z + MAX_Z) / 2.0;
        double dx = mc.player.getX() - cx, dz = mc.player.getZ() - cz;
        return dx * dx + dz * dz <= 60 * 60;
    }

    private boolean isMineable(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;
        if (state.getBlock() instanceof SlabBlock) return false;
        if (state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.BARRIER) return false;
        if (!state.getFluidState().isEmpty()) return false;
        return state.getHardness(mc.world, pos) >= 0f;
    }

    // ===== Кирка =====
    private void equipPickaxe() {
        PlayerInventory inv = mc.player.getInventory();
        if (matchesPick(mc.player.getMainHandStack())) return;
        for (int i = 0; i < 9; i++) {
            if (matchesPick(inv.getStack(i))) { inv.selectedSlot = i; return; }
        }
        for (int i = 9; i < 36; i++) {
            if (matchesPick(inv.getStack(i))) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 8, SlotActionType.SWAP, mc.player);
                inv.selectedSlot = 8;
                return;
            }
        }
    }

    private boolean matchesPick(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof PickaxeItem) return true;
        String n = stack.getName().getString().toLowerCase();
        return n.contains("кирк") || n.contains("eternity") || n.contains("бур");
    }

    // ===================================================================
    // =====  ДРОП: оставляем крюк, кирку, опыт, незер и ВСЕ руды.    ====
    // =====  Броню любого типа и прочий хлам — выкидываем.           ====
    // ===================================================================
    private void dropTrash() {
        PlayerInventory inv = mc.player.getInventory();
        int syncId = mc.player.currentScreenHandler.syncId;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty()) continue;
            if (shouldKeep(s)) continue;
            int slot = i < 9 ? i + 36 : i;
            mc.interactionManager.clickSlot(syncId, slot, 1, SlotActionType.THROW, mc.player);
        }
    }

    private boolean shouldKeep(ItemStack s) {
        if (s == null || s.isEmpty()) return true;
        Item item = s.getItem();

        if (item == Items.COMPASS) return true;
        if (matchesPick(s)) return true;

        String n = s.getName().getString().toLowerCase();

        // крюк: minecraft:tripwire_hook + грэпл / удочка / кастомный "крюк"
        if (item == Items.TRIPWIRE_HOOK
                || item == Items.FISHING_ROD
                || n.contains("крюк") || n.contains("крючок")
                || n.contains("грэпл") || n.contains("grapple") || n.contains("hook")) return true;

        // опыт
        if (n.contains("опыт") || n.contains(XP_NAME.toLowerCase())) return true;

        // все руды и их дроп
        if (isOreDrop(item)) return true;

        // сам блок руды (если подобрали как блок-предмет)
        if (item instanceof BlockItem && ORES.contains(((BlockItem) item).getBlock())) return true;

        // незер / незерит
        if (item == Items.NETHERITE_INGOT || item == Items.NETHERITE_SCRAP
                || item == Items.NETHERITE_BLOCK || item == Items.ANCIENT_DEBRIS) return true;

        return false;
    }

    private boolean isOreDrop(Item item) {
        return item == Items.COAL
                || item == Items.RAW_COPPER || item == Items.COPPER_INGOT
                || item == Items.RAW_IRON || item == Items.IRON_INGOT || item == Items.IRON_NUGGET
                || item == Items.RAW_GOLD || item == Items.GOLD_INGOT || item == Items.GOLD_NUGGET
                || item == Items.REDSTONE
                || item == Items.LAPIS_LAZULI
                || item == Items.EMERALD
                || item == Items.DIAMOND
                || item == Items.QUARTZ
                || item == Items.NETHERITE_SCRAP
                || item == Items.AMETHYST_SHARD;
    }
}