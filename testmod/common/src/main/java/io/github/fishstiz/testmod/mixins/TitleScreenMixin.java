package io.github.fishstiz.testmod.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
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
    private final FZContextMenu fidgetz$contextMenu = FZContextMenu.builder(this).build();

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Override
    public @NonNull List<FZDialog> fidgetz$Dialogs() {
        return List.of(fidgetz$contextMenu);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void beforeInit(CallbackInfo ci) {
        addWidget(fidgetz$contextMenu);

        LinearLayout layout = LinearLayout.vertical();
        layout.addChild(Button.builder(Component.literal("Test Screen"), ignored -> minecraft.setScreen(new TestmodScreen())).build());
        layout.addChild(Button.builder(Component.literal("Flex Screen"), ignored -> minecraft.setScreen(new FlexScreen())).build());
        layout.addChild(Button.builder(Component.literal("FZ Screen"), ignored -> minecraft.setScreen(new FZTestScreen())).build());
        layout.addChild(Button.builder(Component.literal("Wrap Screen"), ignored -> minecraft.setScreen(new FlexWrapScreen())).build());
        layout.addChild(Button.builder(Component.literal("State Screen"), ignored -> minecraft.setScreen(new StatefulScreen())).build());
        layout.addChild(Button.builder(Component.literal("List Screen"), ignored -> minecraft.setScreen(new ListScreen())).build());
        layout.addChild(Button.builder(Component.literal("AbstractListScreen"), ignored -> minecraft.setScreen(new AbstractListScreen())).build());
        layout.addChild(Button.builder(Component.literal("GradientScreen"), ignored -> minecraft.setScreen(new GradientScreen())).build());
        layout.addChild(Button.builder(Component.literal("Screenz"), ignored -> minecraft.setScreen(new Screenz())).build());
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
            target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"
    ))
    private boolean closeDialogs(TitleScreen instance, MouseButtonEvent event, boolean doubleClick, Operation<Boolean> original) {
        if (fidgetz$captureEventForDialogs(event)) {
            return true;
        }
        return original.call(instance, event, doubleClick);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void openContextMenu(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            List<FZPopoverMenuItem> items = new ArrayList<>();
            items.add(FZPopoverMenuItem.builder().message(Component.literal("Hello World!")).build());
            items.add(FZPopoverMenuItem.builder().message(Component.literal("Parent Item"))
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 1")).build())
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 2")).build())
                    .child(FZPopoverMenuItem.builder().message(Component.literal("Item 3")).build())
                    .build());

            fidgetz$contextMenu.open(event.x(), event.y(), items);
        }
    }
}
