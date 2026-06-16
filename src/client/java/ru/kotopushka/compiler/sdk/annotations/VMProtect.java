/*
 * Decompiled with CFR 0.152.
 */
package ru.kotopushka.compiler.sdk.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface VMProtect {
    public VMProtectType type();
}

