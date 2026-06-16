/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.opengl.GL33C
 */
package moscow.rockstar.systems.test;

import lombok.Generated;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL33C;

public final class DrawCallRainTest {
    private static int vao;
    private static int vbo;
    private static int shaderProgram;

    public static void init() {
        String vertexShader = "#version 330 core\nlayout (location = 0) in vec3 aPos;\nvoid main() {\n    gl_Position = vec4(aPos, 1.0);\n}\n";
        String fragmentShader = "#version 330 core\nout vec4 FragColor;\nvoid main() {\n    FragColor = vec4(0.0, 1.0, 0.0, 1.0); // \u0417\u0435\u043b\u0435\u043d\u044b\u0439\n}\n";
        int vs = GL33C.glCreateShader((int)35633);
        GL33C.glShaderSource((int)vs, (CharSequence)vertexShader);
        GL33C.glCompileShader((int)vs);
        int fs = GL33C.glCreateShader((int)35632);
        GL33C.glShaderSource((int)fs, (CharSequence)fragmentShader);
        GL33C.glCompileShader((int)fs);
        shaderProgram = GL33C.glCreateProgram();
        GL33C.glAttachShader((int)shaderProgram, (int)vs);
        GL33C.glAttachShader((int)shaderProgram, (int)fs);
        GL33C.glLinkProgram((int)shaderProgram);
        float[] vertices = new float[]{-0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};
        vao = GL33C.glGenVertexArrays();
        vbo = GL33C.glGenBuffers();
        GL33C.glBindVertexArray((int)vao);
        GL33C.glBindBuffer((int)34962, (int)vbo);
        GL33C.glBufferData((int)34962, (float[])vertices, (int)35044);
        GL33C.glVertexAttribPointer((int)0, (int)3, (int)5126, (boolean)false, (int)12, (long)0L);
        GL33C.glEnableVertexAttribArray((int)0);
        GL33C.glBindBuffer((int)34962, (int)0);
        GL33C.glBindVertexArray((int)0);
    }

    public static void renderSquare(MatrixStack matrices) {
        GL33C.glUseProgram((int)shaderProgram);
        GL33C.glBindVertexArray((int)vao);
        GL33C.glDrawArrays((int)6, (int)0, (int)4);
        GL33C.glBindVertexArray((int)0);
        GL33C.glUseProgram((int)0);
    }

    @Generated
    private DrawCallRainTest() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

