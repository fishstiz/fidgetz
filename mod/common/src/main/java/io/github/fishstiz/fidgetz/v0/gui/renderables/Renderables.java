package io.github.fishstiz.fidgetz.v0.gui.renderables;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

public final class Renderables {
    private Renderables() {
    }

    public static RenderableRectangle fill(int color) {
        return new ColoredRectangle(color);
    }

    public static RenderableRectangle outline(int color) {
        return new Outline(color);
    }

    public static RenderableRectangle text(Component text) {
        return new TextRectangle(() -> text);
    }

    public static RenderableRectangle text(Supplier<Component> text) {
        return new TextRectangle(text);
    }

    public static RenderableRectangle boxShadow(float scale) {
        return new BoxShadow(scale, 0, 0);
    }

    public static RenderableRectangle boxShadow(float scale, int xOffset, int yOffset) {
        return new BoxShadow(scale, xOffset, yOffset);
    }

    public static RenderableRectangle sprite(Identifier sprite) {
        return new RenderableGuiSprite(Objects.requireNonNull(sprite, "sprite is null"));
    }

    public static RenderableRectangle texture(
            Identifier texture,
            int textureWidth,
            int textureHeight,
            float u,
            float v,
            int uWidth,
            int vHeight
    ) {
        return new RenderableTexture(Objects.requireNonNull(texture, "texture is null"), textureWidth, textureHeight, u, v, uWidth, vHeight);
    }

    public static RenderableRectangle texture(Identifier texture, int textureWidth, int textureHeight, int width, int height) {
        return texture(texture, textureWidth, textureHeight, 0, 0, width, height);
    }

    public static RenderableRectangle texture(Identifier texture, int textureWidth, int textureHeight) {
        return texture(texture, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }
}
