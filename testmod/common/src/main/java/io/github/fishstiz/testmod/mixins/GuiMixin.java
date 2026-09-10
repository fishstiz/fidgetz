package io.github.fishstiz.testmod.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.fishstiz.testmod.Testmod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractTestmodRenderState(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            boolean resourcesLoaded,
            CallbackInfo ci,
            @Local(name = "graphics") GuiGraphicsExtractor graphics,
            @Local(name = "xMouse") int xMouse,
            @Local(name = "yMouse") int yMouse
    ) {
        Testmod.extractRenderState(graphics, xMouse, yMouse);
    }
}
