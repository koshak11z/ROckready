/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.file.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface FileInfo {
    public String name();

    public String fileType() default "rock";
}

