package io.github.fishstiz.testmod.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.components.FZModal;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.List;

public class TestmodScreen extends Screen implements FZDialogContainer, FZContextMenu.Source, FZHoverableContainer {
    private static final String CONTEXT_MENU_ID = "context-menu";
    private static final String MODAL_ID = "modal";
    private static final String MODAL_TWO_ID = "modal2";
    private final FZDialogManager dialogManager = new FZDialogManager(this);

    public TestmodScreen() {
        super(CommonComponents.EMPTY);
    }

    private void openContextMenu(double x, double y) {
        if (dialogManager.get(CONTEXT_MENU_ID).map(dialog -> !dialog.isMouseOver(x, y)).orElse(true)) {
            dialogManager.put(FZContextMenu.builder(this)
                    .id(CONTEXT_MENU_ID)
                    .popoverOrder(0)
                    .buildAndOpen(x, y, fidgetz$collectContextEntries(x, y)));
        }
    }

    private void openModal() {
        dialogManager.putIfClosed(MODAL_ID, () -> {
            LinearLayout modalLayout = LinearLayout.vertical().spacing(8);
            modalLayout.addChild(Button.builder(Component.literal("Modal Button 7"), _ -> IO.println("7")).build());
            modalLayout.addChild(Button.builder(Component.literal("Modal Button 8"), _ -> IO.println("8")).build());
            modalLayout.addChild(Button.builder(Component.literal("Close"), _ -> dialogManager.remove(MODAL_ID)).build());
            return FZModal.builder(this, modalLayout).id(MODAL_ID).popoverOrder(1).buildAndOpen();
        });
    }

    private void openModal2() {
        dialogManager.putIfClosed(MODAL_TWO_ID, () -> {
            LinearLayout modalLayout2 = LinearLayout.vertical().spacing(8);
            modalLayout2.addChild(Button.builder(Component.literal("Modal Button 9"), _ -> IO.println("9")).build());
            modalLayout2.addChild(Button.builder(Component.literal("Modal Button 10"), _ -> IO.println("10")).build());
            modalLayout2.addChild(Button.builder(Component.literal("Close"), _ -> dialogManager.remove(MODAL_TWO_ID)).build());
            return FZModal.builder(this, modalLayout2)
                    .id(MODAL_TWO_ID)
                    .popoverOrder(2)
                    .background(Renderables.boxShadow(24)
                            .then(Renderables.fill(CommonColors.DARK_GRAY))
                            .then(Renderables.outline(CommonColors.GRAY)))
                    .uncentered()
                    .height(height)
                    .backdrop(null)
                    .captureFocus(false)
                    .captureClick(false)
                    .buildAndOpen();
        });
    }

    @Override
    protected void init() {
        dialogManager.clear();

        LinearLayout layout = LinearLayout.vertical();
        layout.addChild(Button.builder(Component.literal("Button 1"), _ -> IO.println(1)).build());
        layout.addChild(Button.builder(Component.literal("Button 2"), _ -> openModal()).build());
        layout.addChild(Button.builder(Component.literal("Button 3"), _ -> IO.println(2)).build()).active = false;
        LinearLayout horizontalLayout = LinearLayout.horizontal();
        horizontalLayout.addChild(Button.builder(Component.literal("Button 4.1"), _ -> IO.println(3)).build());
        horizontalLayout.addChild(Button.builder(Component.literal("Button 4.2"), _ -> IO.println(3)).build());
        horizontalLayout.addChild(Button.builder(Component.literal("Button 4.3"), _ -> IO.println(3)).build());
        layout.addChild(horizontalLayout);
        layout.addChild(Button.builder(Component.literal("Button 5"), _ -> openModal2()).build());

        layout.visitWidgets(this::addRenderableWidget);

        LinearLayout argggLayout = LinearLayout.vertical();
        argggLayout.addChild(Button.builder(Component.literal("Button 5"), _ -> IO.println(5)).build());
        argggLayout.addChild(Button.builder(Component.literal("Button 6"), _ -> IO.println(6)).build());

        Layout midLayout = FZComposedLayout.compose(argggLayout).center(this);
        midLayout.visitWidgets(this::addRenderableWidget);

        midLayout.arrangeElements();
        layout.arrangeElements();
    }

    @Override
    public void fidgetz$updateContextEntries(double mouseX, double mouseY, FZContextMenu.Collector collector) {
        FZContextMenu.Source.super.fidgetz$updateContextEntries(mouseX, mouseY, collector);

        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("f"))
                .preventPress()
                .height(24)
                .background(new WidgetRenderables(
                        Renderables.fill(CommonColors.LIGHT_GRAY),
                        Renderables.fill(CommonColors.RED)
                ))
                .build());

        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("entry 2")).build());

        collector.addEntry(FZPopoverMenuItem.fromWidget(FZButton.builder()
                .message(Component.literal("CLose Screen"))
                .onPress(this::onClose)
                .build()));

        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("inactive"))
                .active(false)
                .build());

        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("entry 2 with a super duper long name")).build());

        collector.nextSection();

        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 3")).build());

        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry me"))
                .child(FZPopoverMenuItem.builder().message(Component.literal("entry")).build())
                .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                .child(FZPopoverMenuItem.builder()
                        .message(Component.literal("entry 2"))
                        .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                        .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                        .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                        .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                        .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                        .nextSection()
                        .child(FZPopoverMenuItem.builder().message(Component.literal("open modal 2"))
                                .onPress(this::openModal2)
                                .build())
                        .build())
                .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                .child(FZPopoverMenuItem.builder().message(Component.literal("entry 2")).build())
                .child(FZPopoverMenuItem.builder()
                        .message(Component.literal("open modal"))
                        .onPress(this::openModal)
                        .build())
                .build());

        collector.nextSection();
        collector.nextSection();
        collector.nextSection();
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("entry 4"))
                .background(Renderables.fill(CommonColors.BLACK))
                .build());
        collector.addEntry(FZPopoverMenuItem.builder()
                .message(Component.literal("entry 4"))
                .background(Renderables.fill(CommonColors.BLACK))
                .build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder().message(Component.literal("entry 4")).build());
        collector.addEntry(FZPopoverMenuItem.builder()
                .onPress(this::openModal2)
                .message(Component.literal("open modal 2")).build());
    }

    @Override
    public List<FZDialog> fidgetz$Dialogs() {
        return dialogManager.dialogs();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return fidgetz$captureEventForDialogs(event) || super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (fidgetz$captureEventForDialogs(event)) {
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            openContextMenu(event.x(), event.y());
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        fidgetz$updateHovered(mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
