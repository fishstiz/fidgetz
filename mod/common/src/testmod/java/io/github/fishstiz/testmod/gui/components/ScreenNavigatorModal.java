package io.github.fishstiz.testmod.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.FZModal;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
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
        Minecraft minecraft = Minecraft.getInstance();
        contents.child(Button.builder(Component.literal("Test Screen"), _ -> minecraft.setScreen(new TestmodScreen())).build());
        contents.child(Button.builder(Component.literal("Flex Screen"), _ -> minecraft.setScreen(new FlexScreen())).build());
        contents.child(Button.builder(Component.literal("FZ Screen"), _ -> minecraft.setScreen(new FZTestScreen())).build());
        contents.child(Button.builder(Component.literal("Wrap Screen"), _ -> minecraft.setScreen(new FlexWrapScreen())).build());
        contents.child(Button.builder(Component.literal("State Screen"), _ -> minecraft.setScreen(new StatefulScreen())).build());
        contents.child(Button.builder(Component.literal("List Screen"), _ -> minecraft.setScreen(new ListScreen())).build());
        contents.child(Button.builder(Component.literal("AbstractListScreen"), _ -> minecraft.setScreen(new AbstractListScreen())).build());
        layout.arrangeElements();
        super.onOpen();
    }

    public static ScreenNavigatorModal create(FZDialogContainer screen) {
        FZFlexLayout contents = FZLayouts.flexVertical().spacing(2);
        Layout container = FZLayouts.composer(screen, contents)
                .scrollable()
                .padded(8)
                .clamped()
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
