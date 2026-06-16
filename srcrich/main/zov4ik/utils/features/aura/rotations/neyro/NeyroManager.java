package im.zov4ik.utils.features.aura.rotations.neyro;

import im.zov4ik.utils.display.interfaces.QuickImports;
import im.zov4ik.utils.features.aura.utils.MathAngle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NeyroManager implements QuickImports {
    public static final NeyroManager INSTANCE = new NeyroManager();

    File neyroDirectory;
    boolean recording;
    NeyroRecording currentRecording;
    NeyroRecording activeRecording;
    int playbackIndex;
    boolean playing;
    long lastFrameTime;
    float prevYaw;
    float prevPitch;
    Entity trainingTarget;
    List<NeyroSequenceSegment> segments = new ArrayList<>();
    int currentSegmentIndex;
    int frameInSegment;

    public NeyroManager() {
        MinecraftClient client = MinecraftClient.getInstance();
        File clientDir = new File(client.runDirectory, "zov4ik");
        neyroDirectory = new File(clientDir, "neyro");
        if (!neyroDirectory.exists()) {
            neyroDirectory.mkdirs();
        }
    }

    public void startRecording() {
        if (recording) {
            return;
        }
        recording = true;
        currentRecording = new NeyroRecording();
        lastFrameTime = System.currentTimeMillis();

        if (mc.player != null) {
            prevYaw = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
        }

        spawnTrainingNpc();
    }

    public void stopRecording() {
        recording = false;
        removeTrainingNpc();
    }

    public void recordFrame() {
        if (!recording || currentRecording == null || mc.player == null) {
            return;
        }
        if (mc.currentScreen != null) {
            lastFrameTime = System.currentTimeMillis();
            return;
        }

        long now = System.currentTimeMillis();
        long deltaTime = now - lastFrameTime;
        lastFrameTime = now;

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float deltaYaw = currentYaw - prevYaw;
        float deltaPitch = currentPitch - prevPitch;
        float yawVelocity = deltaTime > 0 ? deltaYaw / deltaTime : 0;
        float pitchVelocity = deltaTime > 0 ? deltaPitch / deltaTime : 0;
        boolean attacked = mc.player.getAttackCooldownProgress(0.5F) < 0.1F && mc.player.handSwinging;

        currentRecording.addFrame(new NeyroFrame(
                deltaYaw,
                deltaPitch,
                yawVelocity,
                pitchVelocity,
                deltaTime,
                attacked,
                mc.player.isSprinting(),
                mc.player.isSneaking(),
                mc.player.input.movementForward,
                mc.player.input.movementSideways,
                mc.player.getAttackCooldownProgress(0.5F)
        ));

        prevYaw = currentYaw;
        prevPitch = currentPitch;
    }

    public void performTrainingAttack() {
        if (!recording || mc.player == null || mc.world == null || trainingTarget == null || mc.currentScreen != null) {
            return;
        }
        if (mc.player.getAttackCooldownProgress(0.5F) < 1.0F) {
            return;
        }

        HitResult hitResult = mc.player.raycast(4.0D, 1.0F, false);
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == trainingTarget) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public boolean saveRecording(String name) {
        if (currentRecording == null || currentRecording.isEmpty()) {
            return false;
        }
        currentRecording.setName(name);
        try {
            currentRecording.saveToFile(neyroDirectory);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean loadRecording(String name) {
        try {
            activeRecording = NeyroRecording.loadFromFile(neyroDirectory, name);
            buildSegments();
            resetPlayback();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public void startPlayback() {
        if (activeRecording == null || activeRecording.isEmpty()) {
            return;
        }
        playing = true;
        resetPlayback();
    }

    public void stopPlayback() {
        playing = false;
    }

    public NeyroFrame getNextFrame() {
        if (activeRecording == null || activeRecording.isEmpty() || !playing) {
            return null;
        }

        List<NeyroFrame> frames = activeRecording.getFrames();
        if (segments.isEmpty()) {
            if (playbackIndex >= frames.size()) {
                playbackIndex = 0;
            }
            return frames.get(playbackIndex++);
        }

        NeyroSequenceSegment segment = segments.get(currentSegmentIndex);
        int actualIndex = segment.startIndex + frameInSegment;
        if (actualIndex >= segment.endIndex || actualIndex >= frames.size()) {
            advanceToNextSegment();
            segment = segments.get(currentSegmentIndex);
            actualIndex = segment.startIndex + frameInSegment;
        }
        if (actualIndex >= frames.size()) {
            resetPlayback();
            return getNextFrame();
        }

        NeyroFrame frame = frames.get(actualIndex);
        frameInSegment++;
        return frame;
    }

    public boolean deleteRecording(String name) {
        return NeyroRecording.deleteFile(neyroDirectory, name);
    }

    public void clearAllRecordings() {
        if (!neyroDirectory.exists()) {
            return;
        }
        File[] files = neyroDirectory.listFiles((dir, fileName) -> fileName.endsWith(".json"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    public List<String> listRecordings() {
        return NeyroRecording.listRecordings(neyroDirectory);
    }

    public void clearCurrentSession() {
        if (currentRecording != null) {
            currentRecording.clear();
        }
        recording = false;
        removeTrainingNpc();
    }

    private void buildSegments() {
        segments.clear();
        if (activeRecording == null || activeRecording.isEmpty()) {
            return;
        }

        List<NeyroFrame> frames = activeRecording.getFrames();
        int segmentStart = 0;
        for (int index = 0; index < frames.size(); index++) {
            if (frames.get(index).isAttacked() && index > segmentStart) {
                segments.add(new NeyroSequenceSegment(segmentStart, index + 1));
                segmentStart = index + 1;
            }
        }

        if (segmentStart < frames.size()) {
            segments.add(new NeyroSequenceSegment(segmentStart, frames.size()));
        }
    }

    private void resetPlayback() {
        if (segments.isEmpty()) {
            playbackIndex = 0;
            currentSegmentIndex = 0;
            frameInSegment = 0;
            return;
        }
        currentSegmentIndex = ThreadLocalRandom.current().nextInt(segments.size());
        frameInSegment = 0;
        playbackIndex = segments.get(currentSegmentIndex).startIndex;
    }

    private void advanceToNextSegment() {
        if (segments.size() <= 1) {
            currentSegmentIndex = 0;
            frameInSegment = 0;
            return;
        }

        int nextIndex;
        do {
            nextIndex = ThreadLocalRandom.current().nextInt(segments.size());
        } while (nextIndex == currentSegmentIndex && segments.size() > 1);

        currentSegmentIndex = nextIndex;
        frameInSegment = 0;
    }

    private void spawnTrainingNpc() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        Vec3d lookVector = MathAngle.cameraAngle().toVector();
        Vec3d spawnPos = mc.player.getPos().add(lookVector.multiply(3.0D));
        ArmorStandEntity npc = new ArmorStandEntity(mc.world, spawnPos.x, spawnPos.y, spawnPos.z);
        npc.setCustomName(Text.literal("Neyro [Training]"));
        npc.setCustomNameVisible(true);
        npc.setNoGravity(true);
        npc.setShowArms(true);
        npc.setId(-7777);

        mc.world.addEntity(npc);
        trainingTarget = npc;
    }

    private void removeTrainingNpc() {
        if (trainingTarget == null || mc.world == null) {
            return;
        }
        trainingTarget.remove(Entity.RemovalReason.DISCARDED);
        trainingTarget = null;
    }

    @Getter
    public static class NeyroSequenceSegment {
        final int startIndex;
        final int endIndex;

        public NeyroSequenceSegment(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}
