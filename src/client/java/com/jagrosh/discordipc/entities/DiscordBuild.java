/*
 * Decompiled with CFR 0.152.
 */
package com.jagrosh.discordipc.entities;

public enum DiscordBuild {
    CANARY("canary"),
    PTB("ptb"),
    STABLE("stable"),
    UNKNOWN("unknown");

    private final String key;

    private DiscordBuild(String string2) {
        this.key = string2;
    }

    public String getKey() {
        return this.key;
    }

    public static DiscordBuild from(String string) {
        for (DiscordBuild discordBuild : DiscordBuild.values()) {
            if (!discordBuild.key.equals(string)) continue;
            return discordBuild;
        }
        return UNKNOWN;
    }
}

