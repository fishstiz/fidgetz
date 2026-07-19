package io.github.fishstiz.testmod.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.testmod.gui.Screens;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen implements FZDialogContainer, FZHoverableContainer {
    @Unique
    private final FZContextMenu fidgetz$contextMenu = FZContextMenu.builder(this)
            .focusOnOpen(false)
            .build();

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Override
    public List<FZDialog> fidgetz$Dialogs() {
        return List.of(fidgetz$contextMenu);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void beforeInit(CallbackInfo ci) {
        addWidget(fidgetz$contextMenu);

        FZFlexLayout layout = FZFlexLayout.vertical();
        Screens.addScreenButtons(layout);
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void beforeExtractRenderState(
            CallbackInfo ci,
            @Local(argsOnly = true, ordinal = 0) int mouseX,
            @Local(argsOnly = true, ordinal = 1) int mouseY
    ) {
        fidgetz$updateHovered(mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void afterExtractRenderState(
            CallbackInfo ci,
            @Local(argsOnly = true) GuiGraphics graphics,
            @Local(argsOnly = true, ordinal = 0) int mouseX,
            @Local(argsOnly = true, ordinal = 1) int mouseY,
            @Local(argsOnly = true) float a
    ) {
        fidgetz$contextMenu.render(graphics, mouseX, mouseY, a);
    }

    @WrapOperation(method = "mouseClicked", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"
    ))
    private boolean closeDialogs(TitleScreen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
        if (fidgetz$captureEventForDialogs(mouseX, mouseY, button)) {
            return true;
        }
        return original.call(instance, mouseX, mouseY, button);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void openContextMenu(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            List<FZPopoverMenuItem> items = new ArrayList<>();
            items.add(FZPopoverMenuItem.builder().message(Component.literal("Hello World!")).build());
            items.add(FZPopoverMenuItem.builder().message(Component.literal("Parent Item"))
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 1")).build())
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 2")).build())
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 3")).build())
                    .build());

            fidgetz$contextMenu.open(mouseX, mouseY, items);
        }
    }
}
