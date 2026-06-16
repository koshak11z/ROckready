/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.inline.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.inline.InlineElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.server.ServerUtility;

public class WorldElement
extends InlineElement {
    private final InlineValue cords;
    private final InlineValue server;
    private final InlineValue tps;
    private final BooleanSetting shortName;

    public WorldElement() {
        super("hud.world", "icons/hud/world.png");
        this.cords = new InlineValue(this.elements, "coords");
        this.server = new InlineValue(this.elements, "server");
        this.tps = new InlineValue(this.elements, "TPS", "TPS");
        this.shortName = new BooleanSetting(this, "hud.world.compact_server").enable();
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        this.cords.update(String.format("%s %s %s", Math.round(WorldElement.mc.player.getX()), Math.round(WorldElement.mc.player.getY()), Math.round(WorldElement.mc.player.getZ())));
        this.server.update(ServerUtility.getServerName(this.shortName.isEnabled()), ServerUtility.getIP());
        this.tps.update(TextUtility.formatNumber(Rockstar.getInstance().getTpsHandler().getTPS()).replace(",", ".").replace(".0", ""));
    }
}

