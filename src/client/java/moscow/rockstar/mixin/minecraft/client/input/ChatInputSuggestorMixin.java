/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.suggestion.Suggestions
 *  javax.annotation.Nullable
 *  net.minecraft.client.gui.screen.ChatInputSuggestor
 *  net.minecraft.client.gui.screen.ChatInputSuggestor$SuggestionWindow
 *  net.minecraft.client.gui.widget.TextFieldWidget
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.input;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import moscow.rockstar.Rockstar;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChatInputSuggestor.class})
public abstract class ChatInputSuggestorMixin {
    @Shadow
    @Final
    TextFieldWidget field_21599;
    @Shadow
    private CompletableFuture<Suggestions> field_21611;
    @Shadow
    @Nullable
    private ChatInputSuggestor.SuggestionWindow field_21612;

    @Shadow
    public abstract void method_23920(boolean var1);

    @Inject(method={"refresh"}, at={@At(value="INVOKE", target="Lcom/mojang/brigadier/StringReader;canRead()Z", remap=false)}, cancellable=true)
    private void injectAutoCompletion(CallbackInfo ci) {
        String prefix;
        String text = this.field_21599.getText();
        if (text.startsWith(prefix = Rockstar.getInstance().getCommandManager().getPrefix())) {
            this.field_21611 = Rockstar.getInstance().getCommandManager().autoComplete(text, this.field_21599.getCursor());
            this.field_21611.thenRun(() -> {
                try {
                    if (this.field_21611.isDone() && !this.field_21611.get().isEmpty() && this.field_21612 == null) {
                        this.method_23920(false);
                        ci.cancel();
                    }
                }
                catch (InterruptedException | ExecutionException exception) {
                    // empty catch block
                }
            });
        }
    }
}

