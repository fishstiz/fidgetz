package io.github.fishstiz.fidgetz.v0.gui.layouts;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

public final class FZClampedLayout extends ComposedLayout {
    private final Supplier<ScreenRectangle> screenArea;

    FZClampedLayout(Supplier<ScreenRectangle> screenArea, Layout layout) {
        super(layout);
        this.screenArea = screenArea;
    }

    @Override
    public void setX(int x) {
        ScreenRectangle bounds = screenArea.get();
        layout.setX(Math.clamp(x, bounds.left(), Math.max(bounds.left(), bounds.right() - layout.getWidth())));
    }

    @Override
    public void setY(int y) {
        ScreenRectangle bounds = screenArea.get();
        layout.setY(Math.clamp(y, bounds.top(), Math.max(bounds.top(), bounds.bottom() - layout.getHeight())));
    }

    @Override
    public void arrangeElements() {
        ScreenRectangle area = screenArea.get();
        layout.arrangeElements();
        fidgetz$setSize(Math.clamp(getWidth(), 0, area.width()), Math.clamp(getHeight(), 0, area.height()));
        setPosition(layout.getX(), layout.getY());
    }
}

