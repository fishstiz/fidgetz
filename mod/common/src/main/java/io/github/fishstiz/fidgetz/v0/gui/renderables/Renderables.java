package io.github.fishstiz.fidgetz.v0.gui.renderables;

import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Renderables {
    private static final RenderableRectangle EMPTY = (ignoredG, ignoredL, ignoredT, ignoredW, ignoredH, ignoreMX, ignoredMY, ignoredD) -> {
    };

    private Renderables() {
    }

    public static RenderableRectangle empty() {
        return EMPTY;
    }

    public static RenderableRectangle wrap(Supplier<RenderableRectangle> delegate) {
        return (graphics, left, top, width, height, mouseX, mouseY, partialTick) ->
                delegate.get().extractRenderState(graphics, left, top, width, height, mouseX, mouseY, partialTick);
    }

    public static RenderableRectangle fill(int color) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                graphics.fill(left, top, left + width, top + height, color);
    }

    public static RenderableRectangle fill(IntSupplier color) {
        Objects.requireNonNull(color, "color cannot be null");
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                graphics.fill(left, top, left + width, top + height, color.getAsInt());
    }

    public static RenderableRectangle outline(int color) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                graphics.renderOutline(left, top, width, height, color);
    }

    public static RenderableRectangle outline(IntSupplier color) {
        Objects.requireNonNull(color, "color cannot be null");
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                graphics.renderOutline(left, top, width, height, color.getAsInt());
    }

    public static RenderableRectangle text(String text) {
        return text(text, false);
    }

    public static RenderableRectangle text(String text, boolean leftAligned) {
        return text(Component.literal(Objects.requireNonNull(text, "text cannot be null")), leftAligned);
    }

    public static RenderableRectangle text(Component text) {
        return text(text, false);
    }

    public static RenderableRectangle text(Component text, boolean leftAligned) {
        Objects.requireNonNull(text, "text cannot be null");
        return text(() -> text, leftAligned);
    }

    public static RenderableRectangle text(Supplier<Component> text) {
        return text(Objects.requireNonNull(text, "text cannot be null"), false);
    }

    public static RenderableRectangle text(Supplier<Component> text, boolean leftAligned) {
        return leftAligned
                ? (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.scrollingText(graphics.textRenderer(), text.get(), left, top, left + width, top + height)
                : (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                graphics.textRenderer().acceptScrolling(text.get(), left + (width / 2), left, left + width, top, top + height);
    }

    public static RenderableRectangle boxShadow(float scale) {
        return boxShadow(scale, 0, 0);
    }

    public static RenderableRectangle boxShadow(float scale, int xOffset, int yOffset) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.boxShadow(graphics, left, top, width, height, scale, xOffset, yOffset);
    }

    public static RenderableRectangle sprite(Identifier sprite) {
        Objects.requireNonNull(sprite, "sprite cannot be null");
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.sprite(graphics, sprite, left, top, width, height);
    }

    public static RenderableRectangle sprite(Identifier sprite, int color) {
        Objects.requireNonNull(sprite, "sprite cannot be null");
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.sprite(graphics, sprite, left, top, width, height, color);
    }

    public static RenderableRectangle sprite(Supplier<Identifier> sprite) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.sprite(graphics, sprite.get(), left, top, width, height);
    }

    public static RenderableRectangle sprite(Supplier<Identifier> sprite, int color) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.sprite(graphics, sprite.get(), left, top, width, height, color);
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
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) ->
                GuiGraphicsUtils.texture(
                        graphics,
                        texture,
                        left,
                        top,
                        u,
                        v,
                        width,
                        height,
                        uWidth,
                        vHeight,
                        textureWidth,
                        textureHeight
                );
    }

    public static RenderableRectangle texture(Identifier texture, int textureWidth, int textureHeight, int width, int height) {
        return texture(texture, textureWidth, textureHeight, 0, 0, width, height);
    }

    public static RenderableRectangle texture(Identifier texture, int textureWidth, int textureHeight) {
        return texture(texture, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }

    public static RenderableRectangle texture(
            Supplier<Identifier> texture,
            int textureWidth,
            int textureHeight,
            float u,
            float v,
            int uWidth,
            int vHeight
    ) {
        return (graphics, left, top, width, height, ignoreMx, ignoreMy, ignoreD) -> GuiGraphicsUtils.texture(
                graphics,
                texture.get(),
                left,
                top,
                u,
                v,
                width,
                height,
                uWidth,
                vHeight,
                textureWidth,
                textureHeight
        );
    }

    public static RenderableRectangle texture(Supplier<Identifier> texture, int textureWidth, int textureHeight, int width, int height) {
        return texture(texture, textureWidth, textureHeight, 0, 0, width, height);
    }

    public static RenderableRectangle texture(Supplier<Identifier> texture, int textureWidth, int textureHeight) {
        return texture(texture, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }
}
