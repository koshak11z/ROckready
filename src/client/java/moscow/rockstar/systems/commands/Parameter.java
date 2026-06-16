/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.commands;

import moscow.rockstar.systems.commands.ParameterValidator;

public record Parameter<T>(String name, boolean required, boolean vararg, ParameterValidator<T> validator) {
}

