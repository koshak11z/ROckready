package com.viaversion.viafabricplus;

public final class ViaFabricPlus {
    private ViaFabricPlus() {
    }

    public static Impl getImpl() {
        return new Impl();
    }

    public static final class Impl {
        public String getTargetVersion() {
            return "unknown";
        }
    }
}
