package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import net.minecraft.client.gui.components.WidgetSprites;

public record WidgetRenderables(
        RenderableRectangle enabled,
        RenderableRectangle disabled,
        RenderableRectangle enabledFocused,
        RenderableRectangle disabledFocused
) {
    public WidgetRenderables(RenderableRectangle sprite) {
        this(sprite, sprite, sprite, sprite);
    }

    public WidgetRenderables(RenderableRectangle sprite, RenderableRectangle focused) {
        this(sprite, sprite, focused, focused);
    }

    public WidgetRenderables(RenderableRectangle enabled, RenderableRectangle disabled, RenderableRectangle focused) {
        this(enabled, disabled, focused, disabled);
    }

    public static WidgetRenderables sprites(WidgetSprites sprites) {
        return new WidgetRenderables(
                Renderables.sprite(sprites.enabled()),
                Renderables.sprite(sprites.disabled()),
                Renderables.sprite(sprites.enabledFocused()),
                Renderables.sprite(sprites.disabledFocused())
        );
    }

    public RenderableRectangle get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.enabledFocused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }
}
