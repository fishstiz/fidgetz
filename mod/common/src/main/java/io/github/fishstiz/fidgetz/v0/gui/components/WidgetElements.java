package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.ResourceLocation;

public record WidgetElements(WidgetRenderables elements, int width, int height, ScreenRectangle margin) {
    public WidgetElements(WidgetRenderables elements, int width, int height) {
        this(elements, width, height, ScreenRectangle.empty());
    }

    public WidgetElements(RenderableRectangle renderable, int width, int height) {
        this(new WidgetRenderables(renderable), width, height);
    }

    public WidgetElements(ResourceLocation sprite, int width, int height) {
        this(Renderables.sprite(sprite), width, height);
    }

    public WidgetElements(RenderableRectangle sprite, RenderableRectangle focused, int width, int height) {
        this(new WidgetRenderables(sprite, focused), width, height);
    }

    public WidgetElements(RenderableRectangle enabled, RenderableRectangle disabled, RenderableRectangle focused, int width, int height) {
        this(new WidgetRenderables(enabled, disabled, focused), width, height);
    }

    public static WidgetElements noFocus(RenderableRectangle enabled, RenderableRectangle disabled, int width, int height) {
        return new WidgetElements(WidgetRenderables.noFocus(enabled, disabled), width, height);
    }

    public static WidgetElements noFocus(ResourceLocation enabled, ResourceLocation disabled, int width, int height) {
        return new WidgetElements(WidgetRenderables.noFocus(enabled, disabled), width, height);
    }

    public WidgetElements margin(int margin) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(margin));
    }

    public WidgetElements margin(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(marginLeft, marginTop, marginRight, marginBottom));
    }

    public WidgetElements marginLeft(int marginLeft) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(marginLeft, margin.top(), margin.right(), margin.bottom()));
    }

    public WidgetElements marginTop(int marginTop) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(margin.left(), marginTop, margin.right(), margin.bottom()));
    }

    public WidgetElements marginRight(int marginRight) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(margin.left(), margin.top(), marginRight, margin.bottom()));
    }

    public WidgetElements marginBottom(int marginBottom) {
        return new WidgetElements(elements, width, height, ScreenRectangleUtils.insets(margin.left(), margin.top(), margin.right(), marginBottom));
    }
}
