package io.github.fishstiz.fidgetz.v0.inject.mixins.debug;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.debug.FZDebugOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), require = 0)
    private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (Math.abs(action) > 0) {
            switch (event.key()) {
                case InputConstants.KEY_F7 -> FZDebugOverlay.focusPath = !FZDebugOverlay.focusPath;
                case InputConstants.KEY_F8 -> FZDebugOverlay.hovered = !FZDebugOverlay.hovered;
            }
        }
    }
}
