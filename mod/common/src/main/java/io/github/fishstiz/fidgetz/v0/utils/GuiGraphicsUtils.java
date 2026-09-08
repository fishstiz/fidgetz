package io.github.fishstiz.fidgetz.v0.utils;

import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.renderables.SimpleGuiRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ServiceLoader;

public final class GuiGraphicsUtils {
    private static final Service GUI_GRAPHICS_SERVICE = ServiceLoader.load(Service.class).findFirst().orElseThrow();
    private static final Identifier BOX_SHADOW_SPRITE = Fidgetz.id("box_shadow");
    private static final float BOX_SHADOW_BORDER = 48f;
    private static final Identifier CHECKERBOARD_TEXTURE = Fidgetz.id("textures/gui/checkerboard.png");
    private static final int CHECKERBOARD_TEXTURE_SIZE = 16;
    private static final int CHECKERBOARD_TEXEL_SIZE = 8;

    public static void addGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState blitState) {
        GUI_GRAPHICS_SERVICE.addGuiElement(graphics, blitState);
    }

    public static @Nullable ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics) {
        return GUI_GRAPHICS_SERVICE.peekScissorStack(graphics);
    }

    public static void sprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height, int color) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
    }

    public static void sprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height) {
        sprite(graphics, sprite, x, y, width, height, CommonColors.WHITE);
    }

    public static void texture(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int uWidth,
            int vHeight,
            int textureWidth,
            int textureHeight
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x,
                y,
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

    public static void texture(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        texture(
                graphics,
                texture,
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight,
                textureWidth,
                textureHeight
        );
    }

    public static void texture(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        texture(
                graphics,
                texture,
                x,
                y,
                0,
                0,
                width, height,
                textureWidth, textureHeight
        );
    }

    public static void texture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int textureWidth, int textureHeight) {
        texture(graphics, texture, x, y, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    public static void fillFloat(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom, int color) {
        ScreenRectangle bounds = new ScreenRectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top));

        addGuiElement(graphics, SimpleGuiRenderState.from(graphics, bounds, (renderState, vertexConsumer) -> {
            ScreenRectangle intersection = renderState.bounds();
            if (intersection != null) {
                vertexConsumer.addVertexWith2DPose(renderState.pose(), left, top).setColor(color);
                vertexConsumer.addVertexWith2DPose(renderState.pose(), left, bottom).setColor(color);
                vertexConsumer.addVertexWith2DPose(renderState.pose(), right, bottom).setColor(color);
                vertexConsumer.addVertexWith2DPose(renderState.pose(), right, top).setColor(color);
            }
        }));
    }

    public static void fillHorizontal(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int colorFrom, int colorTo) {
        ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top);

        addGuiElement(graphics, SimpleGuiRenderState.from(graphics, bounds, (renderState, vertexConsumer) -> {
            ScreenRectangle intersection = renderState.bounds();
            if (intersection != null) {
                renderState.addQuadWith2DPose(vertexConsumer, intersection, colorFrom, colorFrom, colorTo, colorTo);
            }
        }));
    }

    public static void text(GuiGraphicsExtractor graphics, Component text, int x, int y, int color) {
        graphics.text(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void scrollingText(
            ActiveTextCollector textCollector,
            Font font,
            Component component,
            int left,
            int top,
            int right,
            int bottom
    ) {
        int textWidth = font.width(component);
        int textY = (top + bottom - font.lineHeight) / 2 + 1;
        int availableWidth = right - left;

        if (textWidth > availableWidth) {
            int overflowWidth = textWidth - availableWidth;
            double timeSeconds = Util.getMillis() / 1000.0;
            double scrollDuration = Math.max(overflowWidth * 0.5, 3.0);
            double scrollFactor = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * timeSeconds / scrollDuration)) / 2.0 + 0.5;
            double scrollOffset = Mth.lerp(scrollFactor, 0.0, overflowWidth);

            ActiveTextCollector.Parameters localParameters = textCollector.defaultParameters().withScissor(left, right, top, bottom);
            textCollector.accept(TextAlignment.LEFT, left - (int) scrollOffset, textY, localParameters, component.getVisualOrderText());
        } else {
            textCollector.accept(TextAlignment.LEFT, left, textY, component.getVisualOrderText());
        }
    }

    public static void scrollingText(ActiveTextCollector textCollector, Component component, int left, int top, int right, int bottom) {
        scrollingText(textCollector, Minecraft.getInstance().font, component, left, top, right, bottom);
    }

    public static void scrollingText(
            GuiGraphicsExtractor graphics,
            Font font,
            Component component,
            int left,
            int top,
            int right,
            int bottom,
            int color,
            boolean shadow
    ) {
        int textWidth = font.width(component);
        int textY = (top + bottom - font.lineHeight) / 2 + 1;
        int availableWidth = right - left;

        if (textWidth > availableWidth) {
            int overflowWidth = textWidth - availableWidth;
            double timeSeconds = Util.getMillis() / 1000.0;
            double scrollDuration = Math.max(overflowWidth * 0.5, 3.0);
            double scrollFactor = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * timeSeconds / scrollDuration)) / 2.0 + 0.5;
            double scrollOffset = Mth.lerp(scrollFactor, 0.0, overflowWidth);

            graphics.enableScissor(left, top, right, bottom);
            graphics.text(font, component, left - (int) scrollOffset, textY, color, shadow);
            graphics.disableScissor();
        } else {
            graphics.text(font, component, left, textY, color, shadow);
        }
    }

    public static void scrollingText(GuiGraphicsExtractor graphics, Component component, int left, int top, int right, int bottom, int color, boolean shadow) {
        scrollingText(graphics, Minecraft.getInstance().font, component, left, top, right, bottom, color, shadow);
    }

    public static void scrollingText(GuiGraphicsExtractor graphics, Font font, Component component, int left, int top, int right, int bottom, int color) {
        scrollingText(graphics, font, component, left, top, right, bottom, color, true);
    }

    public static void scrollingText(GuiGraphicsExtractor graphics, Component component, int left, int top, int right, int bottom, int color) {
        scrollingText(graphics, Minecraft.getInstance().font, component, left, top, right, bottom, color);
    }

    public static void boxShadow(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            float shadowSize,
            float shadowOffsetX,
            float shadowOffsetY
    ) {
        float scale = shadowSize / BOX_SHADOW_BORDER;
        int baseOffset = Math.round(BOX_SHADOW_BORDER * scale);
        int offsetX = Math.round(baseOffset + shadowOffsetX);
        int offsetY = Math.round(baseOffset + shadowOffsetY);

        sprite(graphics, BOX_SHADOW_SPRITE, x - offsetX, y - offsetY, width + offsetX * 2, height + offsetY * 2);
    }

    public static void checkerboard(
            GuiGraphicsExtractor graphics,
            int left,
            int top,
            int width,
            int height,
            int cellSize,
            int uOffset,
            int vOffset
    ) {
        float scale = (float) CHECKERBOARD_TEXEL_SIZE / cellSize;

        float uTexelStart = (uOffset * scale) % CHECKERBOARD_TEXTURE_SIZE;
        float vTexelStart = (vOffset * scale) % CHECKERBOARD_TEXTURE_SIZE;

        if (uTexelStart < 0) uTexelStart += CHECKERBOARD_TEXTURE_SIZE;
        if (vTexelStart < 0) vTexelStart += CHECKERBOARD_TEXTURE_SIZE;

        float uTexelEnd = uTexelStart + (width * scale);
        float vTexelEnd = vTexelStart + (height * scale);

        float u0 = uTexelStart / CHECKERBOARD_TEXTURE_SIZE;
        float v0 = vTexelStart / CHECKERBOARD_TEXTURE_SIZE;
        float u1 = uTexelEnd / CHECKERBOARD_TEXTURE_SIZE;
        float v1 = vTexelEnd / CHECKERBOARD_TEXTURE_SIZE;

        graphics.blit(
                CHECKERBOARD_TEXTURE,
                left,
                top,
                left + width,
                top + height,
                u0,
                u1,
                v0,
                v1
        );
    }

    public static void checkerboard(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int cellSize) {
        checkerboard(graphics, left, top, width, height, cellSize, 0, 0);
    }

    private GuiGraphicsUtils() {
    }

    interface Service {
        void addGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState blitState);

        @Nullable
        ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics);
    }
}
