package io.github.fishstiz.testmod.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.FZModal;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.testmod.gui.Screens;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.NonNull;

public class ScreenNavigatorModal extends FZModal {
    private final FZFlexLayout contents;

    private ScreenNavigatorModal(FZDialogContainer container, Layout layout, FZFlexLayout contents) {
        super(container, layout);
        this.contents = contents;
        backdrop = null;
        captureClick = false;
        captureFocus = false;
        closeAfterClickOutOfBounds = false;
    }

    @Override
    protected void onOpen() {
        contents.clear();
        Screens.addScreenButtons(contents);
        layout.arrangeElements();
        super.onOpen();
    }

    public static ScreenNavigatorModal create(FZDialogContainer screen) {
        FZFlexLayout contents = FZFlexLayout.vertical().spacing(2);
        Layout container = FZComposedLayout.contain(screen, contents)
                .scrollable()
                .padding(8)
                .clamp()
                .get();
        
        return new ScreenNavigatorModal(screen, container, contents);
    }

    private void drag(double dx, double dy) {
        layout.setX(layout.getX() + (int) dx);
        layout.setY(layout.getY() + (int) dy);
    }

    @Override
    public @NonNull ScreenRectangle getRectangle() {
        return layout.getRectangle();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick) || areCoordinatesInBounds(event.x(), event.y());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (super.mouseDragged(event, dx, dy)) {
            return true;
        }

        if (!isDragging() && event.button() == 0) {
            drag(dx, dy);
            return true;
        }
        return false;
    }
}
