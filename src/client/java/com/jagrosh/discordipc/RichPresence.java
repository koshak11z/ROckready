/*
 * Decompiled with CFR 0.152.
 */
package com.jagrosh.discordipc;

import java.time.OffsetDateTime;

public class RichPresence {
    private String state;
    private String details;
    private long startTimestamp;
    private long endTimestamp;
    private String largeImageKey;
    private String largeImageText;
    private String smallImageKey;
    private String smallImageText;
    private String partyId;
    private int partySize;
    private int partyMax;
    private String matchSecret;
    private String joinSecret;
    private String spectateSecret;
    private int instance;

    public String getState() {
        return this.state;
    }

    public RichPresence setState(String string) {
        this.state = string;
        return this;
    }

    public String getDetails() {
        return this.details;
    }

    public RichPresence setDetails(String string) {
        this.details = string;
        return this;
    }

    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    public RichPresence setStartTimestamp(long l) {
        this.startTimestamp = l;
        return this;
    }

    public long getEndTimestamp() {
        return this.endTimestamp;
    }

    public RichPresence setEndTimestamp(long l) {
        this.endTimestamp = l;
        return this;
    }

    public String getLargeImageKey() {
        return this.largeImageKey;
    }

    public RichPresence setLargeImageKey(String string) {
        this.largeImageKey = string;
        return this;
    }

    public String getLargeImageText() {
        return this.largeImageText;
    }

    public RichPresence setLargeImageText(String string) {
        this.largeImageText = string;
        return this;
    }

    public String getSmallImageKey() {
        return this.smallImageKey;
    }

    public RichPresence setSmallImageKey(String string) {
        this.smallImageKey = string;
        return this;
    }

    public String getSmallImageText() {
        return this.smallImageText;
    }

    public RichPresence setSmallImageText(String string) {
        this.smallImageText = string;
        return this;
    }

    public String getPartyId() {
        return this.partyId;
    }

    public RichPresence setPartyId(String string) {
        this.partyId = string;
        return this;
    }

    public int getPartySize() {
        return this.partySize;
    }

    public RichPresence setPartySize(int n) {
        this.partySize = n;
        return this;
    }

    public int getPartyMax() {
        return this.partyMax;
    }

    public RichPresence setPartyMax(int n) {
        this.partyMax = n;
        return this;
    }

    public String getMatchSecret() {
        return this.matchSecret;
    }

    public RichPresence setMatchSecret(String string) {
        this.matchSecret = string;
        return this;
    }

    public String getJoinSecret() {
        return this.joinSecret;
    }

    public RichPresence setJoinSecret(String string) {
        this.joinSecret = string;
        return this;
    }

    public String getSpectateSecret() {
        return this.spectateSecret;
    }

    public RichPresence setSpectateSecret(String string) {
        this.spectateSecret = string;
        return this;
    }

    public int getInstance() {
        return this.instance;
    }

    public RichPresence setInstance(int n) {
        this.instance = n;
        return this;
    }

    public static class Builder {
        private final RichPresence presence = new RichPresence();
        private String button1Text;
        private String button1Url;
        private String button2Text;
        private String button2Url;

        public Builder setDetails(String details) {
            this.presence.setDetails(details);
            return this;
        }

        public Builder setState(String state) {
            this.presence.setState(state);
            return this;
        }

        public Builder setStartTimestamp(OffsetDateTime timestamp) {
            if (timestamp != null) {
                this.presence.setStartTimestamp(timestamp.toEpochSecond());
            }
            return this;
        }

        public Builder setLargeImage(String key, String text) {
            this.presence.setLargeImageKey(key);
            this.presence.setLargeImageText(text);
            return this;
        }

        public Builder setButton1Text(String text) {
            this.button1Text = text;
            return this;
        }

        public Builder setButton1Url(String url) {
            this.button1Url = url;
            return this;
        }

        public Builder setButton2Text(String text) {
            this.button2Text = text;
            return this;
        }

        public Builder setButton2Url(String url) {
            this.button2Url = url;
            return this;
        }

        public RichPresence build() {
            return this.presence;
        }
    }
}
