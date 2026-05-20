package io.github.fishstiz.fidgetz.v0.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class FZScreen extends Screen implements FZDialogContainer, FZHoverableContainer, FZContextMenuEntry.Source {
    protected static final String GLOBAL_CONTEXT_MENU_ID = "FZScreen:FZContextMenu";
    protected final FZDialogManager dialogManager = new FZDialogManager(this);
    protected ScreenRectangle screenRectangle = ScreenRectangle.empty();

    protected FZScreen(Component title) {
        super(title);
    }

    protected abstract void onInitialize(GuiComponentCollector collector);

    protected void openContextMenu(double x, double y) {
        if (dialogManager.get(GLOBAL_CONTEXT_MENU_ID).map(menu -> !menu.isMouseOver(x, y)).orElse(true)) {
            dialogManager.addOrReplace(GLOBAL_CONTEXT_MENU_ID, 0, FZContextMenu.builder(this)
                    .buildAndOpen(x, y, fidgetz$collectContextEntries(x, y))
            );
        }
    }

    @Override
    protected final void init() {
        this.screenRectangle = super.getRectangle();
        GuiComponentCollector collector = new GuiComponentCollector();
        onInitialize(collector);
        collector.flushTo(this::addWidget, this::addRenderableOnly);
    }

    protected boolean canOpenContextMenu(MouseButtonEvent mouseButtonEvent) {
        return mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    protected boolean shouldCloseOnEscape() {
        return true;
    }

    @Override
    @Deprecated
    public final boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        fidgetz$updateHovered(mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (fidgetz$captureEventForDialogs(event)) {
            return true;
        }
        if (canOpenContextMenu(event)) {
            openContextMenu(event.x(), event.y());
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (fidgetz$captureEventForDialogs(event) || super.keyPressed(event)) {
            return true;
        }
        if (event.isEscape() && shouldCloseOnEscape()) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public List<? extends FZDialog> fidgetz$Dialogs() {
        return dialogManager.dialogs();
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(screenRectangle, 0, 0, width, height)) {
            this.screenRectangle = super.getRectangle();
        }
        return screenRectangle;
    }
}
