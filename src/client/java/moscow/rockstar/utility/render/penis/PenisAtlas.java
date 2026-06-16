/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.resource.Resource
 *  net.minecraft.resource.ResourceManager
 *  net.minecraft.util.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.utility.render.penis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import moscow.rockstar.utility.render.penis.PenisMeta;
import moscow.rockstar.utility.render.penis.PenisSprite;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PenisAtlas {
    private static final Logger LOGGER = LoggerFactory.getLogger(PenisAtlas.class);
    private static final List<PenisAtlas> INSTANCES = new ArrayList<PenisAtlas>();
    private final Map<Identifier, AnimationRegion> animationRegions = new HashMap<Identifier, AnimationRegion>();
    private final List<AnimationFrameData> allFrames = new ArrayList<AnimationFrameData>();
    private Identifier atlasTexture;
    private boolean isBuilt = false;
    private final int frameWidth;
    private final int frameHeight;

    public static PenisAtlas getOrCreateAtlasFor(int width, int height) {
        for (PenisAtlas atlas : INSTANCES) {
            if (atlas.frameWidth != width || atlas.frameHeight != height || atlas.isBuilt()) continue;
            return atlas;
        }
        PenisAtlas newAtlas = new PenisAtlas(width, height);
        INSTANCES.add(newAtlas);
        return newAtlas;
    }

    private PenisAtlas(int frameWidth, int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public void registerAnimation(Identifier animationId, PenisMeta meta, List<NativeImage> frames) {
        if (this.isBuilt) {
            throw new RuntimeException("\u0410\u0442\u043b\u0430\u0441 \u0443\u0436\u0435 \u0441\u043e\u0431\u0440\u0430\u043d! \u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0442\u0435 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0434\u043e \u0432\u044b\u0437\u043e\u0432\u0430 buildAtlas()");
        }
        if (frames.isEmpty()) {
            LOGGER.warn("\u041f\u0443\u0441\u0442\u0430\u044f \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f: {}", (Object)animationId);
            return;
        }
        for (NativeImage frame : frames) {
            if (frame.getWidth() == this.frameWidth && frame.getHeight() == this.frameHeight) continue;
            throw new RuntimeException(String.format("\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0430\u0434\u0440\u043e\u0432 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 %s (%dx%d) \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442 \u0441 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u043c \u044d\u0442\u043e\u0433\u043e \u0430\u0442\u043b\u0430\u0441\u0430 (%dx%d)", animationId, frame.getWidth(), frame.getHeight(), this.frameWidth, this.frameHeight));
        }
        int startIndex = this.allFrames.size();
        for (int i = 0; i < frames.size(); ++i) {
            this.allFrames.add(new AnimationFrameData(animationId, i, frames.get(i)));
        }
        AnimationRegion region = new AnimationRegion(animationId, meta, startIndex, frames.size(), null);
        this.animationRegions.put(animationId, region);
        LOGGER.info("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u0430 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f {} \u0441 {} \u043a\u0430\u0434\u0440\u0430\u043c\u0438 \u0432 \u0430\u0442\u043b\u0430\u0441\u0435 {}x{}", new Object[]{animationId, frames.size(), this.frameWidth, this.frameHeight});
    }

    public void registerAnimationFromPenisFile(Identifier penisFile) {
        try {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            Optional resourceOpt = resourceManager.getResource(penisFile);
            if (resourceOpt.isEmpty()) {
                throw new RuntimeException("\u0424\u0430\u0439\u043b \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + String.valueOf(penisFile));
            }
            Resource resource = (Resource)resourceOpt.get();
            PenisMeta meta = null;
            ArrayList<NativeImage> frames = new ArrayList<NativeImage>();
            try (InputStream inputStream = resource.getInputStream();
                 ZipInputStream zipStream = new ZipInputStream(inputStream);){
                ZipEntry entry;
                TreeMap<String, byte[]> frameData = new TreeMap<String, byte[]>();
                ByteArrayOutputStream baos;
                byte[] buffer;
                int len;
                while ((entry = zipStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    if ("meta.json".equals(entryName)) {
                        baos = new ByteArrayOutputStream();
                        buffer = new byte[1024];
                        while ((len = zipStream.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        String jsonContent = baos.toString(StandardCharsets.UTF_8);
                        meta = PenisMeta.fromJson(jsonContent);
                    } else if (entryName.startsWith("frames/") && entryName.endsWith(".png")) {
                        baos = new ByteArrayOutputStream();
                        buffer = new byte[1024];
                        while ((len = zipStream.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        frameData.put(entryName, baos.toByteArray());
                    }
                    zipStream.closeEntry();
                }
                for (byte[] pngData : frameData.values()) {
                    NativeImage frameImage = NativeImage.read((InputStream)new ByteArrayInputStream(pngData));
                    frames.add(frameImage);
                }
            }
            if (meta == null) {
                throw new RuntimeException("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d meta.json \u0432 " + String.valueOf(penisFile));
            }
            if (frames.isEmpty()) {
                throw new RuntimeException("\u041d\u0435\u0442 \u043a\u0430\u0434\u0440\u043e\u0432 \u0434\u043b\u044f \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 " + String.valueOf(penisFile));
            }
            if (((NativeImage)frames.get(0)).getWidth() != this.frameWidth || ((NativeImage)frames.get(0)).getHeight() != this.frameHeight) {
                throw new RuntimeException(String.format("\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0430\u0434\u0440\u043e\u0432 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 %s (%dx%d) \u043d\u0435 \u0441\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442 \u0441 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u043c \u044d\u0442\u043e\u0433\u043e \u0430\u0442\u043b\u0430\u0441\u0430 (%dx%d)", penisFile, ((NativeImage)frames.get(0)).getWidth(), ((NativeImage)frames.get(0)).getHeight(), this.frameWidth, this.frameHeight));
            }
            this.registerAnimation(penisFile, meta, frames);
        }
        catch (Exception e) {
            throw new RuntimeException("\u041e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0438\u0437 " + String.valueOf(penisFile), e);
        }
    }

    public void buildAtlas() {
        if (this.isBuilt) {
            LOGGER.warn("\u0410\u0442\u043b\u0430\u0441 \u0443\u0436\u0435 \u0441\u043e\u0431\u0440\u0430\u043d!");
            return;
        }
        if (this.allFrames.isEmpty()) {
            LOGGER.warn("\u041d\u0435\u0442 \u043a\u0430\u0434\u0440\u043e\u0432 \u0434\u043b\u044f \u0441\u043e\u0437\u0434\u0430\u043d\u0438\u044f \u0430\u0442\u043b\u0430\u0441\u0430!");
            return;
        }
        int totalFrames = this.allFrames.size();
        int columns = (int)Math.ceil(Math.sqrt(totalFrames));
        int rows = (int)Math.ceil((double)totalFrames / (double)columns);
        int atlasWidth = columns * this.frameWidth;
        int atlasHeight = rows * this.frameHeight;
        NativeImage atlasImage = new NativeImage(atlasWidth, atlasHeight, false);
        for (int x = 0; x < atlasWidth; ++x) {
            for (int y = 0; y < atlasHeight; ++y) {
                atlasImage.setColor(x, y, 0);
            }
        }
        for (int frameIndex = 0; frameIndex < totalFrames; ++frameIndex) {
            int col = frameIndex % columns;
            int row = frameIndex / columns;
            int atlasX = col * this.frameWidth;
            int atlasY = row * this.frameHeight;
            NativeImage sourceFrame = this.allFrames.get((int)frameIndex).image;
            for (int x = 0; x < this.frameWidth; ++x) {
                for (int y = 0; y < this.frameHeight; ++y) {
                    atlasImage.setColor(atlasX + x, atlasY + y, sourceFrame.getColor(x, y));
                }
            }
        }
        this.atlasTexture = Identifier.of((String)"rockstar", (String)("global_animation_atlas_" + this.frameWidth + "x" + this.frameHeight));
        NativeImageBackedTexture atlasTextureObj = new NativeImageBackedTexture(atlasImage);
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.atlasTexture, (AbstractTexture)atlasTextureObj);
        for (AnimationRegion region : this.animationRegions.values()) {
            region.atlasTexture = this.atlasTexture;
            ArrayList<PenisSprite> sprites = new ArrayList<PenisSprite>();
            for (int frameIndex = 0; frameIndex < region.frameCount; ++frameIndex) {
                int globalIndex = region.startIndex + frameIndex;
                int col = globalIndex % columns;
                int row = globalIndex / columns;
                float u1 = (float)col / (float)columns;
                float v1 = (float)row / (float)rows;
                float u2 = (float)(col + 1) / (float)columns;
                float v2 = (float)(row + 1) / (float)rows;
                PenisSprite sprite = new PenisSprite(this.atlasTexture, u1, v1, u2, v2, this.frameWidth, this.frameHeight);
                sprites.add(sprite);
            }
            region.sprites = sprites;
        }
        this.isBuilt = true;
        LOGGER.info("\u0410\u0442\u043b\u0430\u0441 {}x{} \u0441\u043e\u0431\u0440\u0430\u043d \u0441 {} \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f\u043c\u0438 \u0438 {} \u043a\u0430\u0434\u0440\u0430\u043c\u0438", new Object[]{this.frameWidth, this.frameHeight, this.animationRegions.size(), totalFrames});
    }

    public static AnimationRegion getAnimationRegion(Identifier animationId) {
        for (PenisAtlas atlas : INSTANCES) {
            AnimationRegion region = atlas.animationRegions.get(animationId);
            if (region == null) continue;
            return region;
        }
        return null;
    }

    public Identifier getAtlasTexture() {
        return this.atlasTexture;
    }

    public boolean isBuilt() {
        return this.isBuilt;
    }

    public void clear() {
        if (this.atlasTexture != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(this.atlasTexture);
        }
        for (AnimationFrameData frameData : this.allFrames) {
            try {
                frameData.image.close();
            }
            catch (Exception exception) {}
        }
        this.animationRegions.clear();
        this.allFrames.clear();
        this.isBuilt = false;
    }

    public static void clearAllAtlases() {
        for (PenisAtlas atlas : INSTANCES) {
            atlas.clear();
        }
        INSTANCES.clear();
    }

    private static class AnimationFrameData {
        public final Identifier animationId;
        public final int frameIndex;
        public final NativeImage image;

        public AnimationFrameData(Identifier animationId, int frameIndex, NativeImage image) {
            this.animationId = animationId;
            this.frameIndex = frameIndex;
            this.image = image;
        }
    }

    public static class AnimationRegion {
        public final Identifier animationId;
        public final PenisMeta meta;
        public final int startIndex;
        public final int frameCount;
        public Identifier atlasTexture;
        public List<PenisSprite> sprites;

        public AnimationRegion(Identifier animationId, PenisMeta meta, int startIndex, int frameCount, Identifier atlasTexture) {
            this.animationId = animationId;
            this.meta = meta;
            this.startIndex = startIndex;
            this.frameCount = frameCount;
            this.atlasTexture = atlasTexture;
        }

        public PenisSprite getFrameSprite(int frameIndex) {
            if (this.sprites == null || frameIndex < 0 || frameIndex >= this.sprites.size()) {
                return null;
            }
            return this.sprites.get(frameIndex);
        }
    }
}
