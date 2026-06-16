/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.consume.UseAction
 *  net.minecraft.util.Arm
 *  org.joml.Quaternionf
 */
package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingAnimScreen;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import org.joml.Quaternionf;

@ModuleInfo(name="Swing Animation", category=ModuleCategory.VISUALS, desc="\u0418\u0437\u043c\u0435\u043d\u044f\u0435\u0442 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0440\u0443\u043a \u043f\u0440\u0438 \u0432\u0437\u043c\u0430\u0445\u0435")
public class SwingAnimation
extends BaseModule {
    private final BooleanSetting onlyAura = new BooleanSetting(this, "swing.only_aura");
    private final ButtonSetting button = new ButtonSetting(this, "swing.open_menu").action(() -> mc.setScreen((Screen)new SwingAnimScreen()));
    private final EventListener<HandRenderEvent> onHandRender = event -> {
        if (this.shouldApplyAnimation(event.getItemStack()) && event.getArm() == Arm.RIGHT) {
            MatrixStack matrices = event.getMatrices();
            float swingProgress = event.getSwingProgress();
            float equipProgress = event.getEquipProgress();
            SwingTransformations trans = Rockstar.getInstance().getSwingManager().transformations(swingProgress);
            matrices.translate(trans.anchorX(), trans.anchorY(), trans.anchorZ());
            matrices.translate(trans.moveX(), trans.moveY(), trans.moveZ());
            matrices.multiply(new Quaternionf().rotationXYZ((float)Math.toRadians(trans.rotateX()), (float)Math.toRadians(trans.rotateY()), (float)Math.toRadians(trans.rotateZ())));
            matrices.translate(-trans.anchorX(), -trans.anchorY(), -trans.anchorZ());
            event.cancel();
        }
    };

    public boolean shouldApplyAnimation(ItemStack itemStack) {
        Aura auraModule = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
        Entity target = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        Item item = itemStack.getItem();
        if (this.onlyAura.isEnabled() && (!auraModule.isEnabled() || target == null)) {
            return false;
        }
        return item != Items.AIR && item != Items.FILLED_MAP && item != Items.CROSSBOW && item != Items.BOW && item != Items.TRIDENT && item.getUseAction(itemStack) != UseAction.DRINK && item.getUseAction(itemStack) != UseAction.EAT;
    }
}

