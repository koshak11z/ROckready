package moscow.rockstar.systems.modules.modules.combat.neyro;

public class NeyroFrame {
    private float deltaYaw;
    private float deltaPitch;
    private float yawVelocity;
    private float pitchVelocity;
    private long deltaTimeMs;
    private boolean attacked;
    private boolean sprinting;
    private boolean sneaking;
    private float forwardInput;
    private float sidewaysInput;
    private float attackCooldown;

    public NeyroFrame(float deltaYaw, float deltaPitch, float yawVelocity, float pitchVelocity, long deltaTimeMs, boolean attacked, boolean sprinting, boolean sneaking, float forwardInput, float sidewaysInput, float attackCooldown) {
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
        this.yawVelocity = yawVelocity;
        this.pitchVelocity = pitchVelocity;
        this.deltaTimeMs = deltaTimeMs;
        this.attacked = attacked;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
        this.forwardInput = forwardInput;
        this.sidewaysInput = sidewaysInput;
        this.attackCooldown = attackCooldown;
    }

    public float getDeltaYaw() { return this.deltaYaw; }
    public float getDeltaPitch() { return this.deltaPitch; }
    public float getYawVelocity() { return this.yawVelocity; }
    public float getPitchVelocity() { return this.pitchVelocity; }
    public long getDeltaTimeMs() { return this.deltaTimeMs; }
    public boolean isAttacked() { return this.attacked; }
    public boolean isSprinting() { return this.sprinting; }
    public boolean isSneaking() { return this.sneaking; }
    public float getForwardInput() { return this.forwardInput; }
    public float getSidewaysInput() { return this.sidewaysInput; }
    public float getAttackCooldown() { return this.attackCooldown; }
}
