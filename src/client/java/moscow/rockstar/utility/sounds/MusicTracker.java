/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.utility.sounds;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.sounds.LyricsFetcher;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public class MusicTracker
implements IMinecraft {
    private final Thread thread;
    private IMediaSession session;
    private ColorRGBA mediaColor = ColorRGBA.WHITE;
    private final Map<Integer, Identifier> textureCache = new ConcurrentHashMap<Integer, Identifier>();
    private final Map<Integer, ColorRGBA> colorCache = new ConcurrentHashMap<Integer, ColorRGBA>();
    private static final Random RANDOM = new Random();
    private String lyrics = "";
    private String lastTrack = "";

    public MusicTracker() {
        this.thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100L);
                    this.onScheduleTask();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.thread.setDaemon(true);
        this.thread.start();
    }

    private void onScheduleTask() {
        try {
            String trackId;
            MediaPlayerInfo.INSTANCE.getMediaSessions().clear();
            List<IMediaSession> sessions = MediaPlayerInfo.INSTANCE.getMediaSessions();
            this.session = sessions.stream().filter(session1 -> !session1.getMedia().getArtist().isEmpty() && !session1.getMedia().getTitle().isEmpty()).findFirst().orElse(null);
            if (this.session != null && !(trackId = this.session.getMedia().getArtist() + " - " + this.session.getMedia().getTitle()).equals(this.lastTrack)) {
                this.lastTrack = trackId;
                this.lyrics = "";
                String l = LyricsFetcher.fetchFromGenius(this.session.getMedia().getArtist(), this.session.getMedia().getTitle());
                if (l != null) {
                    this.lyrics = l;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public Identifier getImage() {
        try {
            NativeImage originalImage;
            if (this.textureCache.size() > 10) {
                this.textureCache.clear();
                this.colorCache.clear();
            }
            boolean spotify = this.session.getOwner().toLowerCase().contains("spotify");
            byte[] imageData = this.session.getMedia().getArtworkPng();
            int imageHash = Arrays.hashCode(imageData);
            if (this.textureCache.containsKey(imageHash)) {
                this.mediaColor = this.colorCache.get(imageHash);
                return this.textureCache.get(imageHash);
            }
            Identifier identifier = Rockstar.id("temp/" + MusicTracker.randomString());
            NativeImage processedImage = originalImage = NativeImage.read((byte[])imageData);
            if (spotify) {
                int width = originalImage.getWidth();
                int height = originalImage.getHeight();
                int leftCut = (int)((double)width * 0.11);
                int rightCut = (int)((double)width * 0.11);
                int bottomCut = (int)((double)height * 0.22);
                int newWidth = width - leftCut - rightCut;
                int newHeight = height - bottomCut;
                if (newWidth > 0 && newHeight > 0) {
                    processedImage = new NativeImage(originalImage.getFormat(), newWidth, newHeight, false);
                    for (int y = 0; y < newHeight; ++y) {
                        for (int x = 0; x < newWidth; ++x) {
                            int srcX = x + leftCut;
                            int color = originalImage.getColorArgb(srcX, y);
                            processedImage.setColorArgb(x, y, color);
                        }
                    }
                    originalImage.close();
                }
            }
            NativeImage finalImage = processedImage;
            mc.execute(() -> mc.getTextureManager().registerTexture(identifier, (AbstractTexture)new NativeImageBackedTexture(finalImage)));
            this.mediaColor = this.getAverageColor(processedImage, 1);
            this.colorCache.put(imageHash, this.mediaColor);
            this.textureCache.put(imageHash, identifier);
            return identifier;
        }
        catch (Exception e) {
            return null;
        }
    }

    public ColorRGBA getAverageColor(NativeImage image, int step) {
        int width = image.getWidth();
        int height = image.getHeight();
        long totalA = 0L;
        long totalR = 0L;
        long totalG = 0L;
        long totalB = 0L;
        int sampledPixels = 0;
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int argb = image.getColorArgb(x, y);
                int a = argb >> 24 & 0xFF;
                if (a == 0) continue;
                totalA += (long)a;
                totalR += (long)(argb >> 16 & 0xFF);
                totalG += (long)(argb >> 8 & 0xFF);
                totalB += (long)(argb & 0xFF);
                ++sampledPixels;
            }
        }
        if (sampledPixels == 0) {
            return ColorRGBA.WHITE;
        }
        float additional = 50.0f;
        return new ColorRGBA((float)totalR / (float)sampledPixels + additional, (float)totalG / (float)sampledPixels + additional, (float)totalB / (float)sampledPixels + additional);
    }

    private static String randomString() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; ++i) {
            char c = (char)(97 + RANDOM.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    public boolean haveActiveSession() {
        return this.session != null;
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MusicTracker that = (MusicTracker)o;
        return Objects.equals(this.thread, that.thread) && Objects.equals(this.session, that.session) && Objects.equals(this.mediaColor, that.mediaColor) && Objects.equals(this.textureCache, that.textureCache) && Objects.equals(this.colorCache, that.colorCache) && Objects.equals(this.lyrics, that.lyrics) && Objects.equals(this.lastTrack, that.lastTrack);
    }

    public int hashCode() {
        return Objects.hash(this.thread, this.session, this.mediaColor, this.textureCache, this.colorCache, this.lyrics, this.lastTrack);
    }

    @Generated
    public Thread getThread() {
        return this.thread;
    }

    @Generated
    public IMediaSession getSession() {
        return this.session;
    }

    @Generated
    public ColorRGBA getMediaColor() {
        return this.mediaColor;
    }

    @Generated
    public Map<Integer, Identifier> getTextureCache() {
        return this.textureCache;
    }

    @Generated
    public Map<Integer, ColorRGBA> getColorCache() {
        return this.colorCache;
    }

    @Generated
    public String getLyrics() {
        return this.lyrics;
    }

    @Generated
    public String getLastTrack() {
        return this.lastTrack;
    }
}
