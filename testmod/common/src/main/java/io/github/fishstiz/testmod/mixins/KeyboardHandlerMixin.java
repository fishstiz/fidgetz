package io.github.fishstiz.testmod.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.testmod.Testmod;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (action != 0) {
            switch (event.key()) {
                case InputConstants.KEY_F7 -> Testmod.renderFocusPath = !Testmod.renderFocusPath;
                case InputConstants.KEY_F8 -> Testmod.renderHovered = !Testmod.renderHovered;
            }
        }
    }
}
