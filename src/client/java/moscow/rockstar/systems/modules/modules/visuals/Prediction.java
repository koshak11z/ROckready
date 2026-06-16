/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexFormat$DrawMode
 *  net.minecraft.client.render.VertexFormats
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ExperienceOrbEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.PersistentProjectileEntity
 *  net.minecraft.entity.projectile.ProjectileEntity
 *  net.minecraft.entity.projectile.ProjectileUtil
 *  net.minecraft.entity.projectile.TridentEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.entity.projectile.thrown.PotionEntity
 *  net.minecraft.entity.projectile.thrown.SnowballEntity
 *  net.minecraft.entity.projectile.thrown.ThrownItemEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.TridentItem
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec2f
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  net.minecraft.world.World
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.shared.PredicateValue;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@ModuleInfo(name="Prediction", category=ModuleCategory.VISUALS)
public class Prediction
extends BaseModule {
    private final List<Predicted> predicted = new ArrayList<Predicted>();
    private final List<Landed> landed = new ArrayList<Landed>();
    private final SelectSetting entities = new SelectSetting(this, "modules.settings.prediction.entities");
    private final ModeSetting renderMode = new ModeSetting(this, "modules.settings.prediction.render_mode");
    private final ModeSetting.Value defaultMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.default");
    private final ModeSetting.Value glowMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.glow").select();
    private final BooleanSetting inHand = new BooleanSetting(this, "modules.settings.prediction.hand").enable();
    private final BooleanSetting walls = new BooleanSetting(this, "modules.settings.prediction.walls").enable();
    private final BooleanSetting hud = new BooleanSetting(this, "modules.settings.prediction.hud");
    private final EventListener<HudRenderEvent> onRender2D = event -> {
        CustomDrawContext context = event.getContext();
        MatrixStack ms = context.getMatrices();
        for (Predicted predict : this.predicted) {
            Entity patt3$temp;
            ProjectileEntity projectile;
            Entity patt2$temp;
            Entity selector1$temp;
            Vec2f screenPos = Utils.worldToScreen(predict.vectors.getLast());
            if (screenPos == null) continue;
            float x = screenPos.x;
            float y = screenPos.y;
            Font font = Fonts.MEDIUM.getFont(13.0f);
            float height = font.height() + 6.0f;
            float yOff = -height;
            Object name = predict.entity.getName().getString().replace("\u0411\u0440\u043e\u0448\u0435\u043d\u043d\u044b\u0439 \u044d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433", "\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433");
            Entity patt0$temp = predict.entity;
            if (patt0$temp instanceof PotionEntity) {
                PotionEntity potion = (PotionEntity)patt0$temp;
                name = potion.getStack().getFormattedName().getString();
            }
            name = ((String)name).replace("] ", "").replace("[", "") + String.format(" (%s \u0441\u0435\u043a)", TextUtility.formatNumber((float)predict.ticks / 20.0f));
            ItemStack stack;
            Entity entity = predict.entity;
            if (entity instanceof ThrownItemEntity item) {
                stack = item.getStack();
            } else if (entity instanceof PersistentProjectileEntity item) {
                stack = item.getItemStack();
            } else if (entity instanceof ItemEntity item) {
                stack = item.getStack();
            } else {
                stack = Items.ARROW.getDefaultStack();
            }
            float distance = (float)predict.vectors.getLast().distanceTo(Prediction.mc.player.getEyePos());
            float scale = MathHelper.clamp((float)(1.0f - distance / 20.0f), (float)0.5f, (float)1.0f);
            ms.push();
            ms.translate(x, y, 0.0f);
            ms.scale(scale, scale, 1.0f);
            float firstWidth = font.width((String)name) + 20.0f;
            context.drawRect(-firstWidth / 2.0f, yOff, firstWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            context.drawItem(stack, -firstWidth / 2.0f, yOff, 1.0f);
            context.drawText(font, (String)name, -firstWidth / 2.0f + 17.0f, yOff + 3.0f, Colors.WHITE);
            yOff += height;
            Entity patt1$temp = predict.entity;
            if (patt1$temp instanceof ProjectileEntity && (patt2$temp = (projectile = (ProjectileEntity)patt1$temp).getOwner()) instanceof AbstractClientPlayerEntity) {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)patt2$temp;
                String owner = "\u041e\u0442 " + (projectile.getOwner() == Prediction.mc.player ? "\u0412\u0430\u0441" : projectile.getOwner().getName().getString());
                float secondWidth = font.width(owner) + 22.0f;
                context.drawRect(-secondWidth / 2.0f, yOff, secondWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                context.drawHead(player, -secondWidth / 2.0f, yOff, height, BorderRadius.ZERO, Colors.WHITE);
                context.drawText(font, owner, -secondWidth / 2.0f + 19.0f, yOff + 3.0f, Colors.WHITE);
                yOff += height;
            }
            if ((patt3$temp = predict.entity) instanceof PotionEntity) {
                PotionEntity potion = (PotionEntity)patt3$temp;
                for (StatusEffectInstance effect : PotionUtility.effects(potion.getStack())) {
                    String potionName = ((StatusEffect)effect.getEffectType().value()).getName().getString();
                    int amplifier = effect.getAmplifier();
                    int duration = effect.getDuration();
                    String potionLevel = amplifier > 0 ? " " + (amplifier + 1) : "";
                    String potionTime = this.formatDuration(duration);
                    String fullPotionText = potionName + potionLevel + " (" + potionTime + ")";
                    float potionWidth = font.width(fullPotionText) + 6.0f;
                    context.drawRect(-potionWidth / 2.0f, yOff + 5.0f, potionWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                    context.drawText(font, fullPotionText, -potionWidth / 2.0f + 3.0f, yOff + 8.0f, ColorRGBA.fromInt(((StatusEffect)effect.getEffectType().value()).getColor()).withAlpha(255.0f));
                    yOff += height;
                }
            }
            ms.pop();
        }
        if (this.hud.isEnabled()) {
            Font font = Fonts.MEDIUM.getFont(10.0f);
            float yOff = 0.0f;
            for (Predicted predict : this.predicted) {
                if (predict.collidedEntity != Prediction.mc.player || predict.entity instanceof EnderPearlEntity) continue;
                String name = predict.entity.getName().getString().replace("\u0411\u0440\u043e\u0448\u0435\u043d\u043d\u044b\u0439 \u044d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433", "\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433") + String.format(" (%s \u0441\u0435\u043a)", TextUtility.formatNumber((float)predict.ticks / 20.0f));
                context.drawCenteredText(font, "\u0412 \u0432\u0430\u0441 \u043b\u0435\u0442\u0438\u0442 " + name, sr.getScaledWidth() / 2.0f, sr.getScaledHeight() / 2.0f + 20.0f + yOff, Colors.WHITE);
                yOff += font.height() + 3.0f;
            }
        }
    };
    private final EventListener<Render3DEvent> onRender3D = event -> {
        MatrixStack ms = event.getMatrices();
        ms.push();
        RenderUtility.setupRender3D(true);
        RenderUtility.prepareMatrices(ms);
        RenderSystem.enableDepthTest();
        if (this.walls.isEnabled()) {
            RenderSystem.disableDepthTest();
        }
        if (this.defaultMode.isSelected()) {
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (Predicted predicted : this.predicted) {
                Vec3d vec3d = predicted.vectors.getFirst();
                Draw3DUtility.drawLine(ms, builder, Utils.getInterpolatedPos(predicted.entity, event.getTickDelta()), vec3d, Colors.ACCENT);
                Vec3d prevPos = vec3d;
                for (int i = 1; i < predicted.vectors.size(); ++i) {
                    Vec3d pos = predicted.vectors.get(i);
                    Draw3DUtility.drawLine(ms, builder, prevPos, pos, Colors.ACCENT);
                    prevPos = pos;
                }
            }
            RenderUtility.buildBuffer(builder);
        } else {
            Identifier id = Rockstar.id("textures/bloom.png");
            RenderSystem.setShaderTexture((int)0, (Identifier)id);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (Predicted predicted : this.predicted) {
                Vec3d prevPos = predicted.vectors.getFirst();
                Vec3d entityPos = Utils.getInterpolatedPos(predicted.entity, event.getTickDelta());
                if (entityPos.distanceTo(Prediction.mc.player.getEyePos()) > 2.0) {
                    for (int i = 0; i < 10; ++i) {
                        float t = (float)i / 10.0f;
                        Vec3d interpolatedPos = entityPos.add(prevPos.subtract(entityPos).multiply((double)t));
                        this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) / 3.0f, 1.0f);
                        this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) * 2.0f, 0.05f);
                    }
                }
                for (Vec3d pos : predicted.vectors) {
                    if (pos.distanceTo(Prediction.mc.player.getEyePos()) > 2.0) {
                        for (int i = 0; i < 10; ++i) {
                            float t = (float)i / 10.0f;
                            Vec3d interpolatedPos = prevPos.add(pos.subtract(prevPos).multiply((double)t));
                            this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) / 3.0f, 1.0f);
                            this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) * 2.0f, 0.05f);
                        }
                    }
                    prevPos = pos;
                }
                float size = 9.0f;
                if (!(predicted.entity instanceof PotionEntity)) continue;
                ms.push();
                ms.translate(predicted.vectors.getLast());
                ms.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0f));
                DrawUtility.drawImage(ms, buffer, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0f));
                ms.pop();
            }
            RenderUtility.buildBuffer(buffer);
        }
        float size = 1.0f;
        Identifier id = Rockstar.id("textures/hit.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)id);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (Landed landed : this.landed) {
            if (landed.collidedEntity != null) continue;
            ms.push();
            ms.translate(landed.hitResult.getPos());
            ms.multiply(landed.hitResult.getSide().getRotationQuaternion());
            ms.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0f));
            DrawUtility.drawImage(ms, bufferBuilder, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0f));
            ms.pop();
        }
        RenderUtility.buildBuffer(bufferBuilder);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        Camera camera = Prediction.mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (Landed landed : this.landed) {
            if (landed.collidedEntity == null) continue;
            Draw3DUtility.renderFilledBox(ms, quadsBuffer, landed.collidedEntity.getBoundingBox(), Colors.ACCENT.mulAlpha(0.5f));
        }
        RenderUtility.buildBuffer(quadsBuffer);
        BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (Landed landed : this.landed) {
            if (landed.collidedEntity == null) continue;
            Draw3DUtility.renderOutlinedBox(ms, linesBuffer, landed.collidedEntity.getBoundingBox(), Colors.ACCENT);
        }
        RenderUtility.buildBuffer(linesBuffer);
        RenderUtility.endRender3D();
        ms.pop();
    };

    public Prediction() {
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.pearls", entity -> entity instanceof EnderPearlEntity).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.tridents", entity -> entity instanceof TridentEntity).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.snowballs", entity -> entity instanceof SnowballEntity).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.arrows", entity -> entity instanceof ArrowEntity).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.potions", entity -> entity instanceof PotionEntity).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.items", entity -> entity instanceof ItemEntity);
    }

    @Override
    public void tick() {
        this.predicted.clear();
        this.landed.clear();
        ArrayList<ArrowEntity> projectiles = new ArrayList<ArrowEntity>();
        if (this.inHand.isEnabled()) {
            ItemStack handStack = Prediction.mc.player.getMainHandStack();
            ProjectileEntity inHand = null;
            if (handStack.getItem() instanceof EnderPearlItem) {
                inHand = new EnderPearlEntity((World)Prediction.mc.world, (LivingEntity)Prediction.mc.player, handStack);
            } else if (handStack.getItem() instanceof TridentItem && Prediction.mc.player.isUsingItem()) {
                inHand = new TridentEntity((World)Prediction.mc.world, (LivingEntity)Prediction.mc.player, handStack);
            } else if (handStack.getItem() instanceof BowItem && Prediction.mc.player.isUsingItem()) {
                ItemStack arrowStack = new ItemStack((ItemConvertible)Items.ARROW);
                inHand = new ArrowEntity((World)Prediction.mc.world, (LivingEntity)Prediction.mc.player, arrowStack, handStack);
            } else if (handStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged((ItemStack)handStack)) {
                boolean hasMultishot = EnchantmentUtility.getEnchantmentLevel(handStack, (RegistryKey<Enchantment>)Enchantments.MULTISHOT) > 0;
                ItemStack arrowStack = new ItemStack((ItemConvertible)Items.ARROW);
                if (hasMultishot) {
                    for (int i = 0; i < 3; ++i) {
                        ArrowEntity arrow = new ArrowEntity((World)Prediction.mc.world, (LivingEntity)Prediction.mc.player, arrowStack, handStack);
                        projectiles.add(arrow);
                    }
                } else {
                    inHand = new ArrowEntity((World)Prediction.mc.world, (LivingEntity)Prediction.mc.player, arrowStack, handStack);
                }
            }
            if (inHand != null) {
                ProjectileEntity projectile = inHand;
                float speed = 1.5f;
                if (inHand instanceof TridentEntity) {
                    speed = 2.5f;
                } else if (inHand instanceof ArrowEntity) {
                    speed = 3.0f;
                }
                this.setVelocity(projectile, (Entity)Prediction.mc.player, Prediction.mc.player.getPitch(), Prediction.mc.player.getYaw(), 0.0f, speed, 1.0f);
                this.predict((Entity)projectile, true);
            }
        }
        if (!projectiles.isEmpty()) {
            float speed = 3.15f;
            float spreadAngle = 10.0f;
            for (int i = 0; i < projectiles.size(); ++i) {
                ProjectileEntity projectile = (ProjectileEntity)projectiles.get(i);
                float yawOffset = 0.0f;
                if (i == 0) {
                    yawOffset = -spreadAngle;
                } else if (i == 2) {
                    yawOffset = spreadAngle;
                }
                this.setVelocity(projectile, (Entity)Prediction.mc.player, Prediction.mc.player.getPitch(), Prediction.mc.player.getYaw() + yawOffset, 0.0f, speed, 1.0f);
                this.predict((Entity)projectile, true);
            }
        }
        for (Entity entity : Prediction.mc.world.getEntities()) {
            this.predict(entity, false);
        }
    }

    private void predict(Entity entity, boolean inHand) {
        List<AbstractClientPlayerEntity> sortedPlayers;
        ProjectileEntity pearl;
        if (!this.isValid(entity)) {
            return;
        }
        if (entity instanceof ProjectileEntity && (pearl = (ProjectileEntity)entity).getOwner() == null && !(sortedPlayers = Prediction.mc.world.getPlayers()).isEmpty()) {
            Collections.sort(sortedPlayers, Comparator.comparingDouble(player -> player.distanceTo((Entity)pearl)));
            pearl.setOwner((Entity)sortedPlayers.getFirst());
        }
        ArrayList<Vec3d> positions = new ArrayList<Vec3d>();
        Vec3d lastPos = entity.getPos();
        Vec3d lastMotion = entity.getVelocity();
        Entity collidedEntity = null;
        int ticks = 0;
        BlockHitResult blockHitResult = null;
        int i = 0;
        while (i < 150) {
            Vec3d motion = this.predictMotion(entity, lastMotion);
            Vec3d pos = lastPos.add(motion);
            ticks = i++;
            blockHitResult = Prediction.mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
            Entity collided = this.checkEntityCollision(entity, pos);
            if (collided != null) {
                positions.add(pos);
                collidedEntity = collided;
                break;
            }
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                positions.add(blockHitResult.getPos());
                break;
            }
            positions.add(pos);
            lastPos = pos;
            lastMotion = motion;
        }
        if (!positions.isEmpty()) {
            if (inHand) {
                this.landed.add(new Landed(entity, (Vec3d)positions.getLast(), ticks, collidedEntity, blockHitResult));
            } else {
                this.predicted.add(new Predicted(entity, positions, ticks, collidedEntity));
            }
        }
    }

    private void drawGlow(MatrixStack ms, Vec3d pos, BufferBuilder buffer, float size, float alpha) {
        ms.push();
        ms.translate(pos);
        ms.multiply(Prediction.mc.gameRenderer.getCamera().getRotation());
        DrawUtility.drawImage(ms, buffer, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0f * alpha));
        ms.pop();
    }

    private boolean isValid(Entity entity) {
        boolean valid = false;
        for (SelectSetting.Value selectedValue : this.entities.getSelectedValues()) {
            PredicateValue predicateValue = (PredicateValue)selectedValue;
            if (!predicateValue.predicated(entity)) continue;
            valid = true;
        }
        if (entity instanceof TridentEntity) {
            TridentEntity trident = (TridentEntity)entity;
            if (trident.returnTimer > 0) {
                return false;
            }
        }
        return valid && (Math.abs(entity.getVelocity().x + entity.getVelocity().z) > (double)0.01f || Math.abs(entity.getVelocity().y) > (double)0.2f);
    }

    private Entity checkEntityCollision(Entity movingEntity, Vec3d predictedPos) {
        Vec3d currentPos = movingEntity.getPos();
        Vec3d direction = predictedPos.subtract(currentPos);
        if (direction.lengthSquared() == 0.0) {
            return null;
        }
        EntityHitResult hitResult = ProjectileUtil.raycast((Entity)movingEntity, (Vec3d)currentPos, (Vec3d)predictedPos, (Box)movingEntity.getBoundingBox().stretch(direction).expand(0.5), entity -> Prediction.mc.player != entity && entity.isAlive() && !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity) && entity != movingEntity, (double)direction.lengthSquared());
        return hitResult != null ? hitResult.getEntity() : null;
    }

    private void setVelocity(ProjectileEntity entity, double x, double y, double z, float power) {
        Vec3d vec3d = this.calculateVelocity(entity, x, y, z, power);
        entity.setVelocity(vec3d);
        entity.velocityDirty = true;
        double d = vec3d.horizontalLength();
        entity.setYaw((float)(MathHelper.atan2((double)vec3d.x, (double)vec3d.z) * 57.2957763671875));
        entity.setPitch((float)(MathHelper.atan2((double)vec3d.y, (double)d) * 57.2957763671875));
        entity.prevYaw = entity.getYaw();
        entity.prevPitch = entity.getPitch();
    }

    private void setVelocity(ProjectileEntity entity, Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float f = -MathHelper.sin((float)(yaw * ((float)Math.PI / 180))) * MathHelper.cos((float)(pitch * ((float)Math.PI / 180)));
        float g = -MathHelper.sin((float)((pitch + roll) * ((float)Math.PI / 180)));
        float h = MathHelper.cos((float)(yaw * ((float)Math.PI / 180))) * MathHelper.cos((float)(pitch * ((float)Math.PI / 180)));
        this.setVelocity(entity, f, g, h, speed);
        Vec3d vec3d = shooter.getMovement();
        entity.setVelocity(entity.getVelocity().add(vec3d.x, shooter.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    private Vec3d calculateVelocity(ProjectileEntity entity, double x, double y, double z, float power) {
        return new Vec3d(x, y, z).normalize().multiply((double)power);
    }

    private Vec3d predictMotion(Entity entity, Vec3d motion) {
        return motion.multiply(0.99).add(0.0, -entity.getFinalGravity(), 0.0);
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return String.format("0:%02d", remainingSeconds);
    }

    record Landed(Entity entity, Vec3d pos, int ticks, Entity collidedEntity, BlockHitResult hitResult) {
    }

    record Predicted(Entity entity, List<Vec3d> vectors, int ticks, Entity collidedEntity) {
    }
}
