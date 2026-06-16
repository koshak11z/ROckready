package moscow.rockstar.systems.modules.modules.combat.neyro;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
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

public class NeyroManager implements IMinecraft {
    public static final NeyroManager INSTANCE = new NeyroManager();

    private final File neyroDirectory;
    private boolean recording;
    private NeyroRecording currentRecording = new NeyroRecording();
    private NeyroRecording activeRecording;
    private int playbackIndex;
    private boolean playing;
    private long lastFrameTime;
    private float prevYaw;
    private float prevPitch;
    private Entity trainingTarget;
    private boolean subscribed;
    private String lastRecordingName = "default";

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (this.recording) {
            this.performTrainingAttack();
            this.recordFrame();
        }
    };
    private final List<NeyroSequenceSegment> segments = new ArrayList<>();
    private int currentSegmentIndex;
    private int frameInSegment;

    private NeyroManager() {
        MinecraftClient client = MinecraftClient.getInstance();
        File clientDir = new File(client.runDirectory, "rockstar");
        this.neyroDirectory = new File(clientDir, "neyro");
        if (!this.neyroDirectory.exists()) this.neyroDirectory.mkdirs();
    }

    public void startRecording() {
        this.startRecording(this.lastRecordingName);
    }

    public void startRecording(String name) {
        if (this.recording) return;
        this.lastRecordingName = this.safeName(name == null || name.isBlank() ? "default" : name);
        this.recording = true;
        this.currentRecording = new NeyroRecording(this.lastRecordingName);
        this.lastFrameTime = System.currentTimeMillis();
        if (mc.player != null) {
            this.prevYaw = mc.player.getYaw();
            this.prevPitch = mc.player.getPitch();
        }
        this.ensureSubscribed();
        this.spawnTrainingNpc();
    }

    public void stopRecording() {
        this.recording = false;
        this.removeTrainingNpc();
        this.unsubscribeIfIdle();
    }

    public boolean stopAndSaveRecording(String name) {
        if (name != null && !name.isBlank()) this.lastRecordingName = this.safeName(name);
        this.stopRecording();
        return this.saveRecording(this.lastRecordingName);
    }

    public void recordFrame() {
        if (!this.recording || this.currentRecording == null || mc.player == null) return;
        if (mc.currentScreen != null) {
            this.lastFrameTime = System.currentTimeMillis();
            return;
        }
        long now = System.currentTimeMillis();
        long deltaTime = now - this.lastFrameTime;
        this.lastFrameTime = now;
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float deltaYaw = currentYaw - this.prevYaw;
        float deltaPitch = currentPitch - this.prevPitch;
        float yawVelocity = deltaTime > 0L ? deltaYaw / (float)deltaTime : 0.0f;
        float pitchVelocity = deltaTime > 0L ? deltaPitch / (float)deltaTime : 0.0f;
        boolean attacked = mc.player.getAttackCooldownProgress(0.5f) < 0.1f && mc.player.handSwinging;
        this.currentRecording.addFrame(new NeyroFrame(deltaYaw, deltaPitch, yawVelocity, pitchVelocity, deltaTime, attacked, mc.player.isSprinting(), mc.player.isSneaking(), mc.player.input.movementForward, mc.player.input.movementSideways, mc.player.getAttackCooldownProgress(0.5f)));
        this.prevYaw = currentYaw;
        this.prevPitch = currentPitch;
    }

    public void performTrainingAttack() {
        if (!this.recording || mc.player == null || mc.world == null || this.trainingTarget == null || mc.currentScreen != null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        HitResult hitResult = mc.player.raycast(4.0, 1.0f, false);
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == this.trainingTarget) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public boolean saveRecording(String name) {
        if (this.currentRecording == null || this.currentRecording.isEmpty()) return false;
        this.lastRecordingName = this.safeName(name == null || name.isBlank() ? this.lastRecordingName : name);
        this.currentRecording.setName(this.lastRecordingName);
        try {
            this.currentRecording.saveToFile(this.neyroDirectory);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean loadRecording(String name) {
        try {
            this.activeRecording = NeyroRecording.loadFromFile(this.neyroDirectory, name);
            this.buildSegments();
            this.resetPlayback();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public void startPlayback() {
        if (this.activeRecording == null || this.activeRecording.isEmpty()) return;
        this.playing = true;
        this.resetPlayback();
    }

    public void stopPlayback() { this.playing = false; }

    public NeyroFrame getNextFrame() {
        if (this.activeRecording == null || this.activeRecording.isEmpty() || !this.playing) return null;
        List<NeyroFrame> frames = this.activeRecording.getFrames();
        if (this.segments.isEmpty()) {
            if (this.playbackIndex >= frames.size()) this.playbackIndex = 0;
            return frames.get(this.playbackIndex++);
        }
        NeyroSequenceSegment segment = this.segments.get(this.currentSegmentIndex);
        int actualIndex = segment.startIndex + this.frameInSegment;
        if (actualIndex >= segment.endIndex || actualIndex >= frames.size()) {
            this.advanceToNextSegment();
            segment = this.segments.get(this.currentSegmentIndex);
            actualIndex = segment.startIndex + this.frameInSegment;
        }
        if (actualIndex >= frames.size()) {
            this.resetPlayback();
            return this.getNextFrame();
        }
        NeyroFrame frame = frames.get(actualIndex);
        this.frameInSegment++;
        return frame;
    }

    public boolean deleteRecording(String name) { return NeyroRecording.deleteFile(this.neyroDirectory, name); }
    public void clearAllRecordings() {
        File[] files = this.neyroDirectory.listFiles((dir, fileName) -> fileName.endsWith(".json"));
        if (files != null) for (File file : files) file.delete();
    }
    public List<String> listRecordings() { return NeyroRecording.listRecordings(this.neyroDirectory); }
    public void clearCurrentSession() { if (this.currentRecording != null) this.currentRecording.clear(); this.recording = false; this.removeTrainingNpc(); this.unsubscribeIfIdle(); }

    private void buildSegments() {
        this.segments.clear();
        if (this.activeRecording == null || this.activeRecording.isEmpty()) return;
        List<NeyroFrame> frames = this.activeRecording.getFrames();
        int segmentStart = 0;
        for (int index = 0; index < frames.size(); index++) {
            if (frames.get(index).isAttacked() && index > segmentStart) {
                this.segments.add(new NeyroSequenceSegment(segmentStart, index + 1));
                segmentStart = index + 1;
            }
        }
        if (segmentStart < frames.size()) this.segments.add(new NeyroSequenceSegment(segmentStart, frames.size()));
    }

    private void resetPlayback() {
        if (this.segments.isEmpty()) {
            this.playbackIndex = 0;
            this.currentSegmentIndex = 0;
            this.frameInSegment = 0;
            return;
        }
        this.currentSegmentIndex = ThreadLocalRandom.current().nextInt(this.segments.size());
        this.frameInSegment = 0;
        this.playbackIndex = this.segments.get(this.currentSegmentIndex).startIndex;
    }

    private void advanceToNextSegment() {
        if (this.segments.size() <= 1) {
            this.currentSegmentIndex = 0;
            this.frameInSegment = 0;
            return;
        }
        int nextIndex;
        do {
            nextIndex = ThreadLocalRandom.current().nextInt(this.segments.size());
        } while (nextIndex == this.currentSegmentIndex && this.segments.size() > 1);
        this.currentSegmentIndex = nextIndex;
        this.frameInSegment = 0;
    }

    private void spawnTrainingNpc() {
        if (mc.player == null || mc.world == null) return;
        Vec3d spawnPos = mc.player.getEyePos().add(mc.player.getRotationVector().multiply(3.0));
        ArmorStandEntity npc = new ArmorStandEntity(mc.world, spawnPos.x, spawnPos.y - 1.0, spawnPos.z);
        npc.setCustomName(Text.literal("Neyro [Training]"));
        npc.setCustomNameVisible(true);
        npc.setNoGravity(true);
        npc.setShowArms(true);
        npc.setId(-7777);
        mc.world.addEntity(npc);
        this.trainingTarget = npc;
    }

    private void removeTrainingNpc() {
        if (this.trainingTarget == null) return;
        this.trainingTarget.remove(Entity.RemovalReason.DISCARDED);
        this.trainingTarget = null;
    }

    private void ensureSubscribed() {
        if (this.subscribed) return;
        Rockstar.getInstance().getEventManager().subscribe(this);
        this.subscribed = true;
    }

    private void unsubscribeIfIdle() {
        if (!this.subscribed || this.recording) return;
        Rockstar.getInstance().getEventManager().unsubscribe(this);
        this.subscribed = false;
    }

    private String safeName(String name) {
        String safe = name == null ? "default" : name.replaceAll("[^A-Za-z0-9_а-яА-Я-]", "_");
        return safe.isBlank() ? "default" : safe;
    }

    public File getNeyroDirectory() { return this.neyroDirectory; }
    public boolean isRecording() { return this.recording; }
    public NeyroRecording getCurrentRecording() { return this.currentRecording; }
    public NeyroRecording getActiveRecording() { return this.activeRecording; }
    public boolean isPlaying() { return this.playing; }
    public String getLastRecordingName() { return this.lastRecordingName; }

    private static class NeyroSequenceSegment {
        private final int startIndex;
        private final int endIndex;
        private NeyroSequenceSegment(int startIndex, int endIndex) { this.startIndex = startIndex; this.endIndex = endIndex; }
    }
}
