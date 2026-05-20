package io.github.fishstiz.fidgetz.v0.gui.layouts;

import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class FZPaddedLayout extends ComposedLayout {
    private ScreenRectangle padding = ScreenRectangle.empty();

    FZPaddedLayout(Layout layout) {
        super(layout);
    }

    public FZPaddedLayout padding(int padding) {
        this.padding = ScreenRectangleUtils.insets(padding);
        return this;
    }

    public FZPaddedLayout padding(int left, int top, int right, int bottom) {
        this.padding = ScreenRectangleUtils.insets(left, top, right, bottom);
        return this;
    }

    @Override
    public void fidgetz$setWidth(int width) {
        super.fidgetz$setWidth(width - (padding.left() + padding.right()));
    }

    @Override
    public void fidgetz$setHeight(int height) {
        super.fidgetz$setHeight(height - (padding.top() + padding.bottom()));
    }

    @Override
    public void fidgetz$setSize(int width, int height) {
        super.fidgetz$setSize(width - (padding.left() + padding.right()), height - (padding.top() + padding.bottom()));
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
        layout.setPosition(getX() + padding.left(), getY() + padding.top());
    }

    @Override
    public void setX(int x) {
        layout.setX(x + padding.left());
    }

    @Override
    public void setY(int y) {
        layout.setY(y + padding.top());
    }

    @Override
    public void setPosition(int x, int y) {
        layout.setPosition(x + padding.left(), y + padding.top());
    }

    @Override
    public int getX() {
        return layout.getX() - padding.left();
    }

    @Override
    public int getY() {
        return layout.getY() - padding.top();
    }

    @Override
    public int getWidth() {
        return layout.getWidth() + padding.left() + padding.right();
    }

    @Override
    public int getHeight() {
        return layout.getHeight() + padding.top() + padding.bottom();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return ScreenRectangleUtils.expand(layout.getRectangle(), padding);
    }
}
