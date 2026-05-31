package io.github.fishstiz.fidgetz.v0.inject.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenu;
import io.github.fishstiz.fidgetz.v0.gui.components.FZPopoverMenuItem;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.inject.interfaces.ContextMenuSourceConsumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.TriState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(AbstractWidget.class)
abstract class AbstractWidgetMixin implements GuiEventListener, FZHoverableContainer, FZContextMenu.Source, ContextMenuSourceConsumer {
    @Shadow
    protected boolean isHovered;

    @Shadow
    protected abstract boolean areCoordinatesInRectangle(double x, double y);

    @Shadow
    public boolean visible;

    @Shadow
    public abstract boolean isHovered();

    @Unique
    private TriState fidgetz$hovered = TriState.DEFAULT;

    @Unique
    @Nullable
    private GuiEventListener fidgetz$hoveredElement;

    @Unique
    @Nullable
    private Consumer<FZContextMenu.Collector> fidgetz$contextMenuSource;

    @Override
    public boolean fidgetz$isVisible() {
        return visible;
    }

    @Override
    public boolean fidgetz$isHovered() {
        return fidgetz$hovered == TriState.TRUE || fidgetz$getHovered() != null;
    }

    @Override
    public void fidgetz$setHovered(boolean hovered) {
        fidgetz$hovered = TriState.from(hovered);
        if (!hovered) {
            fidgetz$setHovered(null);
        }
    }

    @Override
    public void fidgetz$setHovered(@Nullable GuiEventListener hovered) {
        GuiEventListener previous = fidgetz$getHovered();
        if (previous != hovered) {
            if (previous instanceof FZHoverableElement previousElement) {
                previousElement.fidgetz$setHovered(false);
            }

            if (hovered instanceof FZHoverableElement hoveredElement) {
                hoveredElement.fidgetz$setHovered(true);
            }

            fidgetz$hoveredElement = hovered;
        }
    }

    @Override
    public @Nullable GuiEventListener fidgetz$getHovered() {
        return fidgetz$hoveredElement;
    }

    @ModifyReturnValue(method = "isHovered", at = @At("RETURN"))
    private boolean isHoveredHovered(boolean original) {
        return original && fidgetz$hovered != TriState.FALSE;
    }

    @Override
    public boolean fidgetz$updateHovered(double mouseX, double mouseY) {
        fidgetz$setHovered(areCoordinatesInRectangle(mouseX, mouseY) && fidgetz$isVisible());
        boolean hovered = fidgetz$isHovered();
        if (hovered && this instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                if (((FZHoverableElement) child).fidgetz$updateHovered(mouseX, mouseY)) {
                    fidgetz$setHovered(child);
                    return true;
                }
            }
            fidgetz$setHovered(null);
            return true;
        }
        fidgetz$setHovered(null);
        return hovered;
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenu.@NonNull Collector collector) {
        if (this.fidgetz$contextMenuSource != null) {
            this.fidgetz$contextMenuSource.accept(collector);
            collector.nextSection();
        }

        FZContextMenu.Source.super.fidgetz$updateContextEntries(x, y, collector);
    }

    @Override
    public void fidgetz$setContextMenuSource(Consumer<FZContextMenu.Collector> contextMenuSource) {
        this.fidgetz$contextMenuSource = contextMenuSource;
    }
}
