/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.World
 */
package moscow.rockstar.utility.game.server;

import java.util.ArrayList;
import java.util.Arrays;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public final class ServerUtility
implements IMinecraft {
    public static boolean hasCT;
    public static int ctTime;
    public static int ftAn;

    public static boolean isCM() {
        return ServerUtility.is("cherry.pizza");
    }

    public static boolean isST() {
        return ServerUtility.is("spooky");
    }

    public static boolean isFT() {
        return ServerUtility.is("funtime") || ServerUtility.is("playft");
    }

    public static boolean isRW() {
        return ServerUtility.is("reallyworld") || ServerUtility.is("playrw");
    }

    public static boolean isFS() {
        return ServerUtility.is("funsky");
    }

    public static boolean isHW() {
        return ServerUtility.is("holy") || ServerUtility.is("holly") || ServerUtility.is("playhw");
    }

    public static boolean isSunWay() {
        return ServerUtility.is("sunw");
    }

    public static boolean isSaturn() {
        return ServerUtility.is("saturn");
    }

    public static boolean isSR() {
        return ServerUtility.is("sunmc");
    }

    public static boolean isIntave() {
        return ServerUtility.is("mineblaze") || ServerUtility.is("dexland");
    }

    public static boolean isServerForHPFix() {
        return ServerUtility.isFT() || ServerUtility.isRW() || ServerUtility.isFS();
    }

    public static boolean isPastaFT() {
        return ServerUtility.isFT() || ServerUtility.isST() || ServerUtility.isFS();
    }

    public static String getIP() {
        if (ServerUtility.mc.player == null || ServerUtility.mc.player.networkHandler.getServerInfo() == null) {
            return "single";
        }
        return ServerUtility.mc.player.networkHandler.getServerInfo().address;
    }

    public static boolean is(String ip) {
        return ServerUtility.getIP().toLowerCase().contains(ip.toLowerCase());
    }

    public static String getServerName(boolean shortName) {
        String ip = ServerUtility.getIP();
        String[] parts = ip.split("\\.");
        if (mc.isInSingleplayer()) {
            return ServerUtility.applyCase(ip, shortName);
        }
        if (parts.length == 3) {
            return ServerUtility.applyCase(parts[1], shortName);
        }
        if (parts.length == 2) {
            return ServerUtility.applyCase(parts[0], shortName);
        }
        if (ip.contains(":")) {
            return ip.split(":")[0];
        }
        return ip;
    }

    private static String applyCase(String server, boolean shortName) {
        server = ((String)server).replace("-", "");
        ArrayList<Data> datas = new ArrayList<Data>();
        String[] suffixes = new String[]{"legacy", "bars", "world", "best", "times", "time", "shine", "sky", "lands", "land", "trainer", "server", "blaze", "mine", "lord", "cube", "grief", "craft", "rise", "force", "project", "lite"};
        Arrays.stream(suffixes).forEach(suffix -> datas.add(ServerUtility.genData(suffix)));
        Arrays.stream(new Data[]{new Data("mc", "MC", "-MC"), new Data("hvh", "HVH", "-HVH"), new Data("pvp", "PVP", "PVP")}).forEach(datas::add);
        if (mc.isInSingleplayer() && !shortName) {
            server = "LocalHost";
        }
        if (ServerUtility.isSR()) {
            Object object = server = shortName ? "SR" : "SunRise";
        }
        if (ServerUtility.isSaturn()) {
            Object object = server = shortName ? "S-X" : "SaturnX";
        }
        if (ServerUtility.isSunWay()) {
            server = shortName ? "SW" : "SunWay";
        }
        for (Data data : datas) {
            if (!((String)server).contains(data.orig)) continue;
            if (shortName) {
                server = ((String)server).substring(0, 1).toUpperCase() + data.small;
            } else {
                server = ((String)server).replace(data.orig, data.big);
                server = ((String)server).substring(0, 1).toUpperCase() + ((String)server).substring(1);
            }
            return server;
        }
        server = ((String)server).substring(0, 1).toUpperCase() + ((String)server).substring(1);
        return server;
    }

    public static boolean spawn() {
        if (ServerUtility.mc.player == null || ServerUtility.mc.world == null) {
            return false;
        }
        BlockPos pos = ServerUtility.mc.player.getBlockPos();
        Block blockBelow = ServerUtility.mc.world.getBlockState(pos.down(1)).getBlock();
        Block blockAtZero = ServerUtility.mc.world.getBlockState(new BlockPos(pos.getX(), 0, pos.getZ())).getBlock();
        if (ServerUtility.isFT() && ServerUtility.mc.world.getDifficulty() == Difficulty.NORMAL) {
            return false;
        }
        if (ServerUtility.isFT() || ServerUtility.isST()) {
            return blockBelow == Blocks.AIR || blockAtZero == Blocks.AIR;
        }
        if (ServerUtility.mc.world.getRegistryKey() == World.OVERWORLD) {
            return blockAtZero == Blocks.BEDROCK || blockBelow == Blocks.BEDROCK;
        }
        return false;
    }

    private static Data genData(String full) {
        return new Data(full, full.substring(0, 1).toUpperCase() + full.substring(1), full.substring(0, 1).toUpperCase());
    }

    @Generated
    private ServerUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setHasCT(boolean hasCT) {
        ServerUtility.hasCT = hasCT;
    }

    @Generated
    public static void setCtTime(int ctTime) {
        ServerUtility.ctTime = ctTime;
    }

    static {
        ftAn = -1;
    }

    record Data(String orig, String big, String small) {
    }
}
