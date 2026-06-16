/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  net.minecraft.world.Difficulty
 */
package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;

public class ReHubCommand
implements IMinecraft {
    private boolean processing;
    private final Timer timer = new Timer();
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (!this.processing || ReHubCommand.mc.world == null || ReHubCommand.mc.player == null) {
            return;
        }
        if ((ServerUtility.isFT() || ServerUtility.isFT()) && ReHubCommand.mc.world.getDifficulty() == Difficulty.EASY && this.timer.finished(1000L)) {
            ReHubCommand.mc.player.networkHandler.sendChatCommand("an" + ServerUtility.ftAn);
            this.timer.reset();
            this.processing = false;
        }
    };

    public ReHubCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("rct").aliases("reconnect").desc("commands.rehub.description").handler(this::handle).build();
    }

    private void handle(CommandContext ctx) {
        if (ReHubCommand.mc.player == null || ReHubCommand.mc.world == null) {
            return;
        }
        if (ServerUtility.hasCT) {
            MessageUtility.error(Text.of((String)Localizator.translate("commands_rehub.ct")));
            return;
        }
        this.timer.reset();
        ReHubCommand.mc.player.networkHandler.sendChatCommand("hub");
        this.processing = true;
    }
}

