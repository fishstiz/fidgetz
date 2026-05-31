package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class GuiGraphicsUtils {
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

    private GuiGraphicsUtils() {
    }
}
