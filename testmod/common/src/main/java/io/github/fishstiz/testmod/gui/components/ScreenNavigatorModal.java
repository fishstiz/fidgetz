package io.github.fishstiz.testmod.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.FZModal;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.testmod.gui.Screens;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;

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
    public ScreenRectangle getRectangle() {
        return layout.getRectangle();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || areCoordinatesInBounds(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (super.mouseDragged(mouseX, mouseY, button, dx, dy)) {
            return true;
        }

        if (!isDragging() && button == 0) {
            drag(dx, dy);
            return true;
        }
        return false;
    }
}
