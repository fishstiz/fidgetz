package io.github.fishstiz.testmod.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fishstiz.testmod.Testmod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
    ))
    public void extractTestmodRenderState(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            CallbackInfo ci,
            @Local(ordinal = 0) GuiGraphics graphics
    ) {
        Testmod.extractRenderState(graphics);
    }
}
