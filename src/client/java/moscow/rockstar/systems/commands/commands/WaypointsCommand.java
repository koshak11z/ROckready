/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 */
package moscow.rockstar.systems.commands.commands;

import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class WaypointsCommand
implements IMinecraft,
IScaledResolution {
    private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
        MatrixStack matrices = event.getContext().getMatrices();
        RectBatching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
        this.renderBack((HudRenderEvent)event, matrices);
        ((Batching)rect).draw();
        FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
        this.renderText((HudRenderEvent)event, matrices);
        batching.draw();
    };

    public WaypointsCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("waypoint").aliases("way").desc("\u041c\u0435\u0442\u043a\u0438").param("action", p -> p.literal("add", "del", "clear")).param("name", p -> p.optional().validator(ValidationResult::ok)).param("x", p -> p.optional().validator(this::verifyCoordinate)).param("y", p -> p.optional().validator(this::verifyCoordinate)).param("z", p -> p.optional().validator(this::verifyCoordinate)).handler(this::handle).build();
    }

    private ValidationResult verifyCoordinate(String input) {
        try {
            Integer.parseInt(input);
            return ValidationResult.ok(input);
        }
        catch (NumberFormatException e) {
            return ValidationResult.error("\u041d\u0435 \u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0435 \u0447\u0438\u0441\u043b\u043e");
        }
    }

    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().get(0);
        String name = (String)ctx.arguments().get(1);
        String x = (String)ctx.arguments().get(2);
        String y = (String)ctx.arguments().get(3);
        String z = (String)ctx.arguments().get(4);
        WayPointsManager wayPointsManager = Rockstar.getInstance().getWayPointsManager();
        switch (action.toLowerCase()) {
            case "add": {
                if (name == null || x == null || y == null || z == null) {
                    MessageUtility.error(Text.of((String)"\u0423\u043a\u0430\u0436\u0438\u0442\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0438 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b (.way add \"\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\" x y z)"));
                    return;
                }
                try {
                    wayPointsManager.add(name, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
                }
                catch (NumberFormatException e) {
                    MessageUtility.error(Text.of((String)"\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0434\u043e\u043b\u0436\u043d\u044b \u0431\u044b\u0442\u044c \u0447\u0438\u0441\u043b\u0430\u043c\u0438"));
                }
                break;
            }
            case "del": {
                if (name == null) {
                    MessageUtility.error(Text.of((String)"\u0423\u043a\u0430\u0436\u0438\u0442\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 (.way del \"\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\")"));
                    return;
                }
                wayPointsManager.del(name);
                break;
            }
            case "clear": {
                wayPointsManager.clear();
            }
        }
    }

    private void renderBack(HudRenderEvent event, MatrixStack matrices) {
        for (Map.Entry<String, Vec3d> entry : Rockstar.getInstance().getWayPointsManager().getEntries()) {
            String name = entry.getKey();
            Vec3d pos = entry.getValue();
            Vec3d renderPos = pos.add(0.0, 0.5, 0.0);
            Vec2f screenPos = Utils.worldToScreen(renderPos);
            if (screenPos == null) continue;
            float distance = (float)WaypointsCommand.mc.player.getPos().distanceTo(pos.add(0.5, 0.5, 0.5));
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            int textWidth = (int)Fonts.MEDIUM.getFont(11.0f).width(name + " " + String.format("%.1f", WaypointsCommand.mc.player.getPos().distanceTo(pos)) + "m");
            int x = -textWidth / 2;
            int y = 5;
            event.getContext().drawRect(x - 3, y - 3, textWidth + 8, Fonts.MEDIUM.getFont(11.0f).height() + 6.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            matrices.pop();
        }
    }

    private void renderText(HudRenderEvent event, MatrixStack matrices) {
        for (Map.Entry<String, Vec3d> entry : Rockstar.getInstance().getWayPointsManager().getEntries()) {
            String name = entry.getKey();
            Vec3d pos = entry.getValue();
            Vec3d renderPos = pos.add(0.0, 0.5, 0.0);
            Vec2f screenPos = Utils.worldToScreen(renderPos);
            if (screenPos == null) continue;
            float distance = (float)WaypointsCommand.mc.player.getPos().distanceTo(pos.add(0.5, 0.5, 0.5));
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0f);
            matrices.scale(scale, scale, 1.0f);
            int textWidth = (int)Fonts.MEDIUM.getFont(11.0f).width(name + " " + String.format("%.1f", WaypointsCommand.mc.player.getPos().distanceTo(pos)) + "m");
            int x = -textWidth / 2;
            int y = 5;
            event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), name + " " + String.format("%.1f", WaypointsCommand.mc.player.getPos().distanceTo(pos)) + "m", x, y, ColorRGBA.WHITE);
            matrices.pop();
        }
    }
}

