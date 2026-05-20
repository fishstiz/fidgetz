package io.github.fishstiz.fidgetz.v0.gui.layouts;

import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Supplier;

public final class FZAlignedLayout extends ComposedLayout {
    private final Supplier<ScreenRectangle> screenArea;
    private float alignX;
    private float alignY;

    FZAlignedLayout(Supplier<ScreenRectangle> screenArea, Layout layout) {
        super(layout);
        this.screenArea = screenArea;
    }

    public FZAlignedLayout align(float alignX, float alignY) {
        this.alignX = Math.clamp(alignX, 0f, 1f);
        this.alignY = Math.clamp(alignY, 0f, 1f);
        return this;
    }

    public FZAlignedLayout centered() {
        return align(0.5f, 0.5f);
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, screenArea.get(), alignX, alignY);
    }
}
