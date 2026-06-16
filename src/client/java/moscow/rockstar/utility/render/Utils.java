/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.render.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.utility.render;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public final class Utils
implements IMinecraft {
    private static Matrix4f modelViewMatrix;
    private static Matrix4f projectionMatrix;

    public static void onRender(Matrix4f modelView, Matrix4f projection) {
        modelViewMatrix = new Matrix4f((Matrix4fc)modelView);
        projectionMatrix = new Matrix4f((Matrix4fc)projection);
    }

    public static Vec2f worldToScreen(Vec3d worldCoords) {
        Camera camera = Utils.mc.gameRenderer.getCamera();
        Vec3d delta = worldCoords.subtract(camera.getPos());
        Vector4f clipSpacePos = new Vector4f((float)delta.x, (float)delta.y, (float)delta.z, 1.0f);
        clipSpacePos.mul((Matrix4fc)modelViewMatrix).mul((Matrix4fc)projectionMatrix);
        if (clipSpacePos.w <= 0.0f) {
            return null;
        }
        Vector4f ndcSpacePos = clipSpacePos.div(clipSpacePos.w);
        float screenX = (ndcSpacePos.x + 1.0f) / 2.0f * (float)mc.getWindow().getScaledWidth();
        float screenY = (1.0f - ndcSpacePos.y) / 2.0f * (float)mc.getWindow().getScaledHeight();
        return new Vec2f(screenX, screenY);
    }

    public static Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)entity.prevX, (double)entity.getX()), MathHelper.lerp((double)tickDelta, (double)entity.prevY, (double)entity.getY()), MathHelper.lerp((double)tickDelta, (double)entity.prevZ, (double)entity.getZ()));
    }

    public static Vec3d getInterpolatedPos(Vec3d prev, Vec3d pos, float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)prev.x, (double)pos.getX()), MathHelper.lerp((double)tickDelta, (double)prev.y, (double)pos.getY()), MathHelper.lerp((double)tickDelta, (double)prev.z, (double)pos.getZ()));
    }

    @Generated
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

