package dev.babbaj.pathfinder;

public final class PathSegment {
    public final long[] packed;
    public final boolean finished;

    public PathSegment(long[] packed, boolean finished) {
        this.packed = packed == null ? new long[0] : packed;
        this.finished = finished;
    }
}
