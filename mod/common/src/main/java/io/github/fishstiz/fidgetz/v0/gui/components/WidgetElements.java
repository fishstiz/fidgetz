package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import net.minecraft.resources.Identifier;

public record WidgetElements(WidgetRenderables sprites, int width, int height) {
    public WidgetElements(RenderableRectangle renderable, int width ,int height) {
        this(new WidgetRenderables(renderable), width, height);
    }

    public WidgetElements(Identifier sprite, int width, int height) {
        this(Renderables.sprite(sprite), width, height);
    }
}
