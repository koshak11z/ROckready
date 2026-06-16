/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package moscow.rockstar.systems.commands;

import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;

public sealed interface ValidationResult {
    public static <T> Ok<T> ok(T value) {
        return new Ok<T>(value);
    }

    public static Error error(String msg) {
        MessageUtility.error(Text.of((String)msg));
        return new Error(msg);
    }

    public record Ok<T>(T value) implements ValidationResult
    {
    }

    public record Error(String message) implements ValidationResult
    {
    }
}

