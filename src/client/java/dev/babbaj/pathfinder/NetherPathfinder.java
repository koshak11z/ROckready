package dev.babbaj.pathfinder;

public final class NetherPathfinder {
    public static final int CACHE_MISS_SOLID = 1;
    private NetherPathfinder() {}
    public static boolean isThisSystemSupported() { return false; }
    public static long newContext(long seed) { return 0L; }
    public static void freeContext(long context) {}
    public static void cancel(long context) {}
    public static boolean hasChunkFromJava(long context, int chunkX, int chunkZ) { return false; }
    public static long getOrCreateChunk(long context, int chunkX, int chunkZ) { return 0L; }
    public static long getChunkPointer(long context, int chunkX, int chunkZ) { return 0L; }
    public static void cullFarChunks(long context, int chunkX, int chunkZ, int maxDistanceBlocks) {}
    public static PathSegment pathFind(long context, int sx, int sy, int sz, int dx, int dy, int dz, boolean a, boolean b, int max, boolean noPredict) { return null; }
    public static boolean isVisible(long context, int cacheMiss, double sx, double sy, double sz, double dx, double dy, double dz) { return false; }
    public static int isVisibleMulti(long context, int cacheMiss, int count, double[] src, double[] dst, boolean any) { return -1; }
    public static void raytrace(long context, int cacheMiss, int count, double[] src, double[] dst, boolean[] hitsOut, double[] hitPosOut) {}
}
