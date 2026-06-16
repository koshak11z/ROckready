/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import moscow.rockstar.systems.modules.api.ModuleCategory;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    public String name();

    public ModuleCategory category();

    public int key() default -1;

    public boolean disableOnQuit() default false;

    public boolean enabledByDefault() default false;

    public String desc() default "\u0423 \u044d\u0442\u043e\u0439 \u0444\u0443\u043d\u043a\u0446\u0438\u0438 \u043d\u0435\u0442 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f";
}

