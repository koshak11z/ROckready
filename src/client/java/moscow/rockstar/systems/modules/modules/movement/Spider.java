/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;

@ModuleInfo(name="Spider", category=ModuleCategory.MOVEMENT, desc="modules.descriptions.spider")
public class Spider
extends BaseModule {
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (!Spider.mc.player.horizontalCollision) {
            return;
        }
        Spider.mc.player.setOnGround(Spider.mc.player.age % 3 == 0);
        Spider.mc.player.prevY -= 2.0E-232;
        if (Spider.mc.player.isOnGround()) {
            Spider.mc.player.setVelocity(Spider.mc.player.getVelocity().getX(), 0.42, Spider.mc.player.getVelocity().getZ());
        }
    };
    private final EventListener<EntityJumpEvent> onJump = event -> {};
}

