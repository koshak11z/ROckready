/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.util.Window
 */
package moscow.rockstar.utility.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public interface IWindow {
    public static final Window mw = MinecraftClient.getInstance().getWindow();
}

