package io.github.fishstiz.testmod.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.FZDialog;
import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.testmod.gui.components.ScreenNavigatorModal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractContainerEventHandler implements FZDialogContainer {
    @Shadow
    @Final
    private List<GuiEventListener> children;

    @Shadow
    @Final
    private List<NarratableEntry> narratables;

    @Unique
    @Nullable
    private ScreenNavigatorModal fidgetz$modal;

    @Inject(method = "init(II)V", at = @At("TAIL"))
    private void onInit(int width, int height, CallbackInfo ci) {
        fidgetz$modal = ScreenNavigatorModal.create((Screen & FZDialogContainer) (Object) this);
    }

    @Inject(method = "clearWidgets", at = @At("TAIL"))
    private void clearDebugger(CallbackInfo ci) {
        if (fidgetz$modal != null && fidgetz$modal.isOpen()) {
            children.remove(fidgetz$modal);
            narratables.remove(fidgetz$modal);

            children.addFirst(fidgetz$modal);
            narratables.addFirst(fidgetz$modal);
        }
    }

    @WrapMethod(method = "keyPressed")
    private boolean openDebugger(KeyEvent event, Operation<Boolean> original) {
        if (event.input() == InputConstants.KEY_F10 && fidgetz$modal != null) {
            if (!fidgetz$modal.isOpen()) {
                children.remove(fidgetz$modal);
                narratables.remove(fidgetz$modal);

                children.addFirst(fidgetz$modal);
                narratables.addFirst(fidgetz$modal);
                fidgetz$modal.open();
            } else {
                fidgetz$modal.close();
            }
            return true;
        }
        return original.call(event);
    }

    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("TAIL"))
    private void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (fidgetz$modal != null) {
            fidgetz$modal.render(graphics, mouseX, mouseY, a);
        }
    }

    @Override
    public List<? extends FZDialog> fidgetz$Dialogs() {
        return fidgetz$modal != null ? List.of(fidgetz$modal) : List.of();
    }
}
