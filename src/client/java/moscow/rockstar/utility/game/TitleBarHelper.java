/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Pointer
 *  com.sun.jna.platform.win32.WinDef$DWORD
 *  com.sun.jna.platform.win32.WinDef$HWND
 *  com.sun.jna.platform.win32.WinDef$LPVOID
 *  com.sun.jna.ptr.IntByReference
 *  net.minecraft.client.MinecraftClient
 *  org.lwjgl.glfw.GLFWNativeWin32
 */
package moscow.rockstar.utility.game;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import moscow.rockstar.utility.game.DwmApi;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFWNativeWin32;

public class TitleBarHelper {
    private static final int DWM_USE_IMMERSIVE_DARK_MODE = 20;

    public static void setDarkTitleBar() {
        TitleBarHelper.applyTitleBarTheme(1);
    }

    public static void setLightTitleBar() {
        TitleBarHelper.applyTitleBarTheme(0);
    }

    private static void applyTitleBarTheme(int themeValue) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        try {
            long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
            long hwndHandle = GLFWNativeWin32.glfwGetWin32Window((long)windowHandle);
            WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant((long)hwndHandle));
            IntByReference useDarkTheme = new IntByReference(themeValue);
            WinDef.LPVOID pointerToValue = new WinDef.LPVOID(useDarkTheme.getPointer());
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, new WinDef.DWORD(20L), pointerToValue, new WinDef.DWORD(4L));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

