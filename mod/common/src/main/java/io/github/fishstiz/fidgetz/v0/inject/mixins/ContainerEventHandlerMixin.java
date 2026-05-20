package io.github.fishstiz.fidgetz.v0.inject.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.fishstiz.fidgetz.v0.gui.components.FZDialog;
import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenuEntry;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(ContainerEventHandler.class)
interface ContainerEventHandlerMixin extends GuiEventListener, FZHoverableContainer, FZContextMenuEntry.Source {
    @Shadow
    List<? extends GuiEventListener> children();

    @Override
    default @Nullable GuiEventListener fidgetz$getHovered() {
        return null;
    }

    @Override
    default boolean fidgetz$isHovered() {
        return FZHoverableContainer.super.fidgetz$isHovered();
    }

    @Override
    default void fidgetz$setHovered(boolean hovered) {
        FZHoverableContainer.super.fidgetz$setHovered(hovered);
    }

    @Override
    default boolean fidgetz$updateHovered(double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            for (GuiEventListener child : children()) {
                if (((FZHoverableElement) child).fidgetz$updateHovered(mouseX, mouseY)) {
                    fidgetz$setHovered(child);
                    return true;
                }
            }
            fidgetz$setHovered(null);
            return true;
        }
        fidgetz$setHovered(null);
        return false;
    }

    @WrapMethod(method = "nextFocusPath")
    private ComponentPath initializeExcludedAreas(FocusNavigationEvent navigationEvent, Operation<ComponentPath> original) {
        if (this instanceof FZDialogContainer container) {
            for (FZDialog dialog : container.fidgetz$Dialogs()) {
                if (dialog.isOpen() && dialog.shouldCaptureFocus()) {
                    ComponentPath path = dialog.nextFocusPath(navigationEvent);
                    if (path != null) {
                        return ComponentPath.path((ContainerEventHandler) this, path);
                    }
                }
            }
        }

        return original.call(navigationEvent);
    }

    @ModifyExpressionValue(
            method = {"handleTabNavigation", "nextFocusPathInDirection", "nextFocusPathVaguelyInDirection"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/ContainerEventHandler;children()Ljava/util/List;")
    )
    private List<? extends GuiEventListener> removeOccludedChildren(List<? extends GuiEventListener> original) {
        if (!(this instanceof FZDialogContainer container)) return original;

        List<? extends FZDialog> dialogs = container.fidgetz$Dialogs();
        if (dialogs.isEmpty()) return original;

        List<GuiEventListener> potentialChildren = new ArrayList<>(original.size());
        for (GuiEventListener child : original) {
            boolean visible = true;
            for (FZDialog dialog : dialogs) {
                if (child == dialog) {
                    break;
                }
                if (dialog.isOpen() && dialog.getRectangle().encompasses(child.getRectangle())) {
                    visible = false;
                    break;
                }
            }

            if (visible) {
                potentialChildren.add(child);
            }
        }

        return potentialChildren;
    }
}
