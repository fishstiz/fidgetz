package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.fidgetz.v0.utils.NavigationUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class FZDialog extends FZContainer implements FZComponent, FZPopover {
    protected static final int DEFAULT_POPOVER_ORDER = 0;
    protected final ContainerEventHandler container;
    protected @Nullable String componentId;
    protected int popoverOrder = DEFAULT_POPOVER_ORDER;
    private @Nullable ComponentPath lastContainerFocusPath;
    private boolean open;

    protected FZDialog(ContainerEventHandler container) {
        this.container = container;
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return isOpen();
    }

    public boolean shouldCloseOnEscape() {
        return true;
    }

    public boolean shouldCaptureClick() {
        return true;
    }

    public boolean shouldCloseAfterClickOutOfBounds() {
        return true;
    }

    public boolean shouldCaptureFocus() {
        return true;
    }

    public boolean shouldFocusOnOpen() {
        return true;
    }

    public boolean shouldRefocusLastPath() {
        return true;
    }

    public boolean isOpen() {
        return open;
    }

    protected void setOpen(boolean open) {
        boolean previous = isOpen();
        this.open = open;

        if (previous != isOpen()) {
            if (isOpen()) {
                lastContainerFocusPath = container.getCurrentFocusPath();
                onOpen();
            } else {
                onClose();
            }
        }
    }

    protected void refocusLastContainerPath() {
        if (shouldRefocusLastPath() && lastContainerFocusPath != null) {
            MutableObject<@Nullable List<? extends GuiEventListener>> current = new MutableObject<>();
            ComponentPath path = NavigationUtils.takeWhile(lastContainerFocusPath, component -> {
                List<? extends GuiEventListener> children = current.get();
                if (children == null || children.contains(component)) {
                    current.setValue(component instanceof ContainerEventHandler subContainer ? subContainer.children() : null);
                    return true;
                }
                return false;
            });
            if (path != null) {
                path.applyFocus(true);
            }

            lastContainerFocusPath = null;
        }
    }

    protected void onClose() {
        boolean focused = NavigationUtils.inFocusPath(container, this);

        ComponentPath path = getCurrentFocusPath();
        if (path != null) {
            path.applyFocus(false);
        }

        if (focused) {
            container.setFocused(null);
        }
    }

    protected void onOpen() {
        if (shouldFocusOnOpen()) {
            ComponentPath path = NavigationUtils.findPath(container, this);
            if (path != null) {
                path.applyFocus(true);
            }
        }
    }

    @Override
    public abstract ScreenRectangle getRectangle();

    protected boolean areCoordinatesInBounds(double x, double y) {
        return getRectangle().containsPoint((int) x, (int) y);
    }

    protected void extractDialogRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : renderables()) {
            renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (isOpen()) {
            if (shouldCaptureClick() || (graphics.containsPointInScissor(mouseX, mouseY) && areCoordinatesInBounds(mouseX, mouseY))) {
                graphics.requestCursor(CursorType.DEFAULT);
            }
            extractDialogRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isOpen()) return false;
        if (super.mouseClicked(event, doubleClick)) return true;
        return shouldCaptureClick();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (event.isEscape() && shouldCloseOnEscape()) {
            setOpen(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isOpen() && (shouldCaptureClick() || areCoordinatesInBounds(mouseX, mouseY));
    }

    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (!isOpen()) return null;

        ComponentPath next = super.nextFocusPath(event);
        if (next != null) return next;

        if (event instanceof FocusNavigationEvent.InitialFocus && !children().isEmpty()) {
            return NavigationUtils.initialFocus(this);
        }

        return shouldCaptureFocus() ? getCurrentFocusPath() : null;
    }

    public void repositionElements() {
    }

    @Override
    public void fidgetz$visitWidgets(WidgetVisitor visitor) {
        visitor.visitWidget(this);
    }

    @Override
    public void fidgetz$visitRenderables(Consumer<Renderable> visitor) {
        visitor.accept(this);
    }

    @Override
    public int fidgetz$popoverOrder() {
        return popoverOrder;
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return componentId;
    }
}
