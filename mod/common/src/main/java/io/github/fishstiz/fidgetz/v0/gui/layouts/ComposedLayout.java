package io.github.fishstiz.fidgetz.v0.gui.layouts;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Consumer;

abstract class ComposedLayout implements FZLayout {
    protected final Layout layout;

    ComposedLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public void fidgetz$setWidth(int width) {
        if (layout instanceof FZFlexElement flexElement) {
            flexElement.fidgetz$setWidth(width);
        }
    }

    @Override
    public void fidgetz$setHeight(int height) {
        if (layout instanceof FZFlexElement flexElement) {
            flexElement.fidgetz$setHeight(height);
        }
    }

    @Override
    public void fidgetz$setSize(int width, int height) {
        if (layout instanceof FZFlexElement flexElement) {
            flexElement.fidgetz$setSize(width, height);
        }
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
        layout.visitChildren(layoutElementVisitor);
    }

    @Override
    public void setX(int x) {
        layout.setX(x);
    }

    @Override
    public void setY(int y) {
        layout.setY(y);
    }

    @Override
    public int getX() {
        return layout.getX();
    }

    @Override
    public int getY() {
        return layout.getY();
    }

    @Override
    public int getWidth() {
        return layout.getWidth();
    }

    @Override
    public int getHeight() {
        return layout.getHeight();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return layout.getRectangle();
    }

    @Override
    public abstract void arrangeElements();
}
