/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.commands;

import java.util.Collections;
import java.util.List;
import moscow.rockstar.systems.commands.ValidationResult;

@FunctionalInterface
public interface ParameterValidator<T> {
    public ValidationResult validate(String var1);

    default public List<String> suggestions(String partial) {
        return Collections.emptyList();
    }
}

