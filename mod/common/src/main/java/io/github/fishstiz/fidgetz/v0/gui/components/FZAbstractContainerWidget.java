package io.github.fishstiz.fidgetz.v0.gui.components;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

// backported from 26.1
public abstract class FZAbstractContainerWidget extends FZAbstractScrollArea implements ContainerEventHandlerPatch {
    private @Nullable GuiEventListener focused;
    private boolean isDragging;

    public FZAbstractContainerWidget(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        super(x, y, width, height, message, scrollbarSettings);
    }

    public final boolean isDragging() {
        return this.isDragging;
    }

    public final void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    public @Nullable GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (getFocused() != focused) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }

            if (focused != null) {
                focused.setFocused(true);
            }

            this.focused = focused;
        }
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return ContainerEventHandlerPatch.super.nextFocusPath(navigationEvent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean scrolling = this.updateScrolling(mouseX, mouseY, button);
        return ContainerEventHandlerPatch.super.mouseClicked(mouseX, mouseY, button) || scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        return ContainerEventHandlerPatch.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        super.mouseDragged(mouseX, mouseY, button, dx, dy);
        return ContainerEventHandlerPatch.super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    public boolean isFocused() {
        return ContainerEventHandlerPatch.super.isFocused();
    }

    public void setFocused(boolean focused) {
        ContainerEventHandlerPatch.super.setFocused(focused);
    }
}
