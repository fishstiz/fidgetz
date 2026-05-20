package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;
import java.util.function.Consumer;

public interface FZDialogContainer extends ContainerEventHandler, FZPopoverContainer {
    default List<? extends FZDialog> fidgetz$Dialogs() {
        return children().stream().filter(FZDialog.class::isInstance).map(FZDialog.class::cast).toList();
    }

    default boolean fidgetz$captureEventForDialogs(KeyEvent event) {
        if (event.isEscape()) {
            for (FZDialog dialog : fidgetz$Dialogs()) {
                if (dialog.isOpen() && dialog.shouldCloseOnEscape()) {
                    GuiEventListener focused = this.getFocused();
                    dialog.setOpen(false);
                    if (focused == dialog) {
                        dialog.refocusLastContainerPath();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    default boolean fidgetz$captureEventForDialogs(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            for (FZDialog dialog : fidgetz$Dialogs()) {
                if (!dialog.isOpen()) continue;

                if (dialog.shouldCloseAfterClickOutOfBounds() && !dialog.areCoordinatesInBounds((int) event.x(), (int) event.y())) {
                    dialog.setOpen(false);
                    return dialog.shouldCaptureClick();
                }

                if (dialog.isMouseOver(event.x(), event.y())) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    default void fidgetz$visitPopovers(Consumer<FZPopover> visitor) {
        fidgetz$Dialogs().forEach(visitor);
    }

    @Override
    ScreenRectangle getRectangle();
}
