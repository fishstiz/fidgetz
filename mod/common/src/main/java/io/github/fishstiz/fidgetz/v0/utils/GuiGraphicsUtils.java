package io.github.fishstiz.fidgetz.v0.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.fishstiz.fidgetz.v0.Fidgetz;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class GuiGraphicsUtils {
    private static final ResourceLocation BOX_SHADOW_SPRITE = Fidgetz.id("box_shadow");
    private static final float BOX_SHADOW_BORDER = 48f;
    private static final ResourceLocation CHECKERBOARD_TEXTURE = Fidgetz.id("textures/gui/checkerboard.png");
    private static final int CHECKERBOARD_TEXTURE_SIZE = 16;
    private static final int CHECKERBOARD_TEXEL_SIZE = 8;

    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    public static void texture(
            GuiGraphics graphics,
            ResourceLocation texture,
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
                texture,
                x,
                y,
                width,
                height,
                u,
                v,
                uWidth,
                vHeight,
                textureWidth,
                textureHeight
        );
    }

    public static void texture(
            GuiGraphics graphics,
            ResourceLocation texture,
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
            GuiGraphics graphics,
            ResourceLocation texture,
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

    public static void texture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int textureWidth, int textureHeight) {
        texture(graphics, texture, x, y, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    public static void fillFloat(GuiGraphics graphics, float left, float top, float right, float bottom, int color) {
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = graphics.pose().last().pose();
        vertexConsumer.addVertex(matrix4f, left, top, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, left, bottom, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, right, bottom, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, right, top, 0).setColor(color);
    }

    public static void fillHorizontal(GuiGraphics graphics, int left, int top, int right, int bottom, int colorFrom, int colorTo) {
        VertexConsumer vertexConsumer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix4f = graphics.pose().last().pose();
        vertexConsumer.addVertex(matrix4f, left, top, 0).setColor(colorFrom);
        vertexConsumer.addVertex(matrix4f, left, bottom, 0).setColor(colorFrom);
        vertexConsumer.addVertex(matrix4f, right, bottom, 0).setColor(colorTo);
        vertexConsumer.addVertex(matrix4f, right, top, 0).setColor(colorTo);
    }

    public static void text(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color);
    }

    public static void scrollingText(
            GuiGraphics graphics,
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
            graphics.drawString(font, component, left - (int) scrollOffset, textY, color, shadow);
            graphics.disableScissor();
        } else {
            graphics.drawString(font, component, left, textY, color, shadow);
        }
    }

    public static void scrollingText(GuiGraphics graphics, Component component, int left, int top, int right, int bottom, int color, boolean shadow) {
        scrollingText(graphics, Minecraft.getInstance().font, component, left, top, right, bottom, color, shadow);
    }

    public static void scrollingText(GuiGraphics graphics, Font font, Component component, int left, int top, int right, int bottom, int color) {
        scrollingText(graphics, font, component, left, top, right, bottom, color, true);
    }

    public static void scrollingText(GuiGraphics graphics, Component component, int left, int top, int right, int bottom, int color) {
        scrollingText(graphics, Minecraft.getInstance().font, component, left, top, right, bottom, color);
    }

    public static void scrollingCenteredText(
            GuiGraphics graphics,
            Component component,
            int centerX,
            int left,
            int right,
            int top,
            int bottom,
            int color
    ) {
        Font font = Minecraft.getInstance().font;
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
            graphics.drawString(font, component, left - (int) scrollOffset, textY, color);
            graphics.disableScissor();
        } else {
            int i1 = Mth.clamp(centerX, left + textWidth / 2, right - textWidth / 2);
            graphics.drawCenteredString(font, component, i1, textY, color);
        }
    }

    public static void boxShadow(
            GuiGraphics graphics,
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

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        sprite(graphics, BOX_SHADOW_SPRITE, x - offsetX, y - offsetY, width + offsetX * 2, height + offsetY * 2);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public static void checkerboard(
            GuiGraphics graphics,
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

        graphics.blit(
                CHECKERBOARD_TEXTURE,
                left,
                top,
                width,
                height,
                uTexelStart,
                vTexelStart,
                (int) (width * scale),
                (int) (height * scale),
                CHECKERBOARD_TEXTURE_SIZE,
                CHECKERBOARD_TEXTURE_SIZE
        );
    }

    public static void checkerboard(GuiGraphics graphics, int left, int top, int width, int height, int cellSize) {
        checkerboard(graphics, left, top, width, height, cellSize, 0, 0);
    }

    private GuiGraphicsUtils() {
    }
}
