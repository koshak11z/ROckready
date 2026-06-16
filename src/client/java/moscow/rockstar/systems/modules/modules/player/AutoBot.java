/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.block.Blocks
 *  net.minecraft.registry.Registries
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3i
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.time.Timer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@ModuleInfo(name="Auto Bot", category=ModuleCategory.PLAYER)
public class AutoBot
extends BaseModule {
    private final Timer scanTimer = new Timer();
    private Action action = Action.IDLE;

    @Override
    public void onEnable() {
        if (!FabricLoader.getInstance().isModLoaded("baritone")) {
            MessageUtility.info(Text.of((String)("\u0414\u043b\u044f \u0440\u0430\u0431\u043e\u0442\u044b " + this.getName() + " \u043d\u0443\u0436\u0435\u043d \u043c\u043e\u0434 baritone")));
            this.toggle();
            return;
        }
        this.action = Action.IDLE;
        this.msg("#allowBreak true");
    }

    @Override
    public void onDisable() {
        this.msg("#stop");
        this.msg("#sel clear");
    }

    @Override
    public void tick() {
        if (this.scanTimer.finished(200L)) {
            BlockPos player = AutoBot.mc.player.getBlockPos();
            int radius = 10;
            for (int x = 0; x < radius * 2; ++x) {
                for (int z = 0; z < radius * 2; ++z) {
                    for (int y = 0; y < radius * 2; ++y) {
                        BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                        BlockPos pos = player.add((Vec3i)offset);
                        if (!this.logic(pos)) continue;
                        return;
                    }
                }
            }
            this.scanTimer.reset();
        }
    }

    private boolean logic(BlockPos obsidian) {
        BlockPos target = obsidian.up();
        if (AutoBot.mc.world.getBlockState(obsidian).getBlock() != Blocks.OBSIDIAN || AutoBot.mc.world.getBlockState(obsidian.east()).getBlock() != Blocks.OBSIDIAN || AutoBot.mc.world.getBlockState(obsidian.north()).getBlock() != Blocks.OBSIDIAN || AutoBot.mc.world.getBlockState(obsidian.south()).getBlock() != Blocks.OBSIDIAN || AutoBot.mc.world.getBlockState(obsidian.west()).getBlock() != Blocks.OBSIDIAN || AutoBot.mc.world.getBlockState(target.east()).getBlock() != Blocks.AIR || AutoBot.mc.world.getBlockState(target.north()).getBlock() != Blocks.AIR || AutoBot.mc.world.getBlockState(target.south()).getBlock() != Blocks.AIR || AutoBot.mc.world.getBlockState(target.west()).getBlock() != Blocks.AIR) {
            return false;
        }
        if (target.equals((Object)AutoBot.mc.player.getBlockPos())) {
            if (AutoBot.mc.world.getBlockState(target).getBlock() == Blocks.TORCH) {
                if (this.action != Action.BREAK) {
                    this.select(target);
                    this.msg("#sel cleararea");
                    this.action = Action.BREAK;
                    this.scanTimer.reset();
                    return true;
                }
            } else if (this.action != Action.PLACE) {
                this.select(target);
                this.msg("#sel fill " + String.valueOf(Registries.BLOCK.getId(Blocks.TORCH)));
                this.action = Action.PLACE;
                this.scanTimer.reset();
                return true;
            }
        } else if (this.action != Action.FOLLOW && this.action != Action.PLACE) {
            this.msg(String.format("#goto %s.5 %s %s.5", target.getX(), target.getY(), target.getZ()));
            this.action = Action.FOLLOW;
            this.scanTimer.reset();
            return true;
        }
        return false;
    }

    private void select(BlockPos pos) {
        this.msg("#sel clear");
        this.msg(String.format("#sel pos1 %s %s %s", pos.getX(), pos.getY(), pos.getZ()));
        this.msg(String.format("#sel pos2 %s %s %s", pos.getX(), pos.getY(), pos.getZ()));
    }

    private void msg(String msg) {
        AutoBot.mc.player.networkHandler.sendChatMessage(msg);
    }

    static enum Action {
        IDLE,
        FOLLOW,
        PLACE,
        BREAK;

    }
}
