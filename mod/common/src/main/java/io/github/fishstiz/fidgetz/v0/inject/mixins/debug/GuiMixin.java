package io.github.fishstiz.fidgetz.v0.inject.mixins.debug;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fishstiz.fidgetz.v0.gui.debug.FZDebugOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    public void extractTestmodRenderState(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            boolean resourcesLoaded,
            CallbackInfo ci,
            @Local(name = "graphics") GuiGraphicsExtractor graphics,
            @Local(name = "xMouse") int xMouse,
            @Local(name = "yMouse") int yMouse
    ) {
        FZDebugOverlay.extractRenderState(graphics, xMouse, yMouse);
    }
}
