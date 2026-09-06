package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.*;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class WrappedComponent extends AbstractWidget implements ContainerEventHandler, FZComponent, FZHoverableElement {
    private List<AbstractWidget> children;
    private AbstractWidget widget;
    private boolean dragging;

    public WrappedComponent(AbstractWidget widget) {
        super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage());
        this.widget = widget;
        this.children = List.of(widget);
    }

    public void set(AbstractWidget widget) {
        if (this.widget != widget) {
            boolean focused = isFocused();
            ScreenRectangle rectangle = getRectangle();
            this.widget = widget;
            this.children = List.of(widget);
            setRectangle(rectangle.width(), rectangle.height(), rectangle.left(), rectangle.top());
            setFocused(focused ? widget : null);
        }
    }

    public AbstractWidget get() {
        return this.widget;
    }

    public static WrappedComponent bind(String key, FZRef<? extends AbstractWidget> ref) {
        WrappedComponent wrapped = new WrappedComponent(ref.value());
        ref.subscribe(key, wrapped::set);
        return wrapped;
    }

    @Override
    public List<AbstractWidget> children() {
        return children;
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public @Nullable GuiEventListener getFocused() {
        return widget.isFocused() ? widget : null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focused == null) {
            widget.setFocused(false);
        } else if (focused == widget) {
            widget.setFocused(true);
        }
    }

    // delegate to widget

    @Override
    public @Nullable String fidgetz$componentId() {
        return widget instanceof FZComponent component ? component.fidgetz$componentId() : null;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        widget.render(graphics, mouseX, mouseY, a);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        widget.updateNarration(output);
    }

    @Override
    public int getHeight() {
        return widget.getHeight();
    }

    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        super.setTooltip(tooltip);
        widget.setTooltip(tooltip);
    }

    @Override
    public void setTooltipDelay(Duration delay) {
        super.setTooltipDelay(delay);
        widget.setTooltipDelay(delay);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        widget.onClick(event, doubleClick);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        widget.onRelease(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return widget.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return widget.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return widget.mouseDragged(event, dx, dy);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return ComponentPath.path(this, widget.nextFocusPath(navigationEvent));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return widget.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        widget.playDownSound(soundManager);
    }

    @Override
    public int getWidth() {
        return widget.getWidth();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        widget.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        widget.setHeight(height);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        widget.setAlpha(alpha);
    }

    @Override
    public float getAlpha() {
        return widget.getAlpha();
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        widget.setMessage(message);
    }

    @Override
    public Component getMessage() {
        return widget.getMessage();
    }

    @Override
    public boolean isFocused() {
        return widget.isFocused();
    }

    @Override
    public boolean isHovered() {
        return widget.isHovered();
    }

    @Override
    public boolean isHoveredOrFocused() {
        return widget.isHoveredOrFocused();
    }

    @Override
    public boolean isActive() {
        return widget.isActive();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        widget.setFocused(focused);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return widget.narrationPriority();
    }

    @Override
    public int getX() {
        return widget.getX();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        widget.setX(x);
    }

    @Override
    public int getY() {
        return widget.getY();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        widget.setY(y);
    }

    @Override
    public int getRight() {
        return widget.getRight();
    }

    @Override
    public int getBottom() {
        return widget.getBottom();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        widget.setSize(width, height);
    }

    @Override
    public ScreenRectangle getRectangle() {
        return widget.getRectangle();
    }

    @Override
    public void setRectangle(int width, int height, int x, int y) {
        super.setRectangle(width, height, x, y);
        widget.setRectangle(width, height, x, y);
    }

    @Override
    public boolean fidgetz$isVisible() {
        return widget.visible;
    }

    @Override
    public int getTabOrderGroup() {
        return widget.getTabOrderGroup();
    }

    @Override
    public void setTabOrderGroup(int tabOrderGroup) {
        super.setTabOrderGroup(tabOrderGroup);
        widget.setTabOrderGroup(tabOrderGroup);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        widget.setPosition(x, y);
    }

    @Override
    public void mouseMoved(double x, double y) {
        widget.mouseMoved(x, y);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return widget.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return widget.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return widget.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return widget.charTyped(event);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return widget.shouldTakeFocusAfterInteraction();
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return ComponentPath.path(this, widget.getCurrentFocusPath());
    }

    @Override
    public ScreenRectangle getBorderForArrowNavigation(ScreenDirection opposite) {
        return widget.getBorderForArrowNavigation(opposite);
    }

    @Override
    public Collection<? extends NarratableEntry> getNarratables() {
        return widget.getNarratables();
    }
}
