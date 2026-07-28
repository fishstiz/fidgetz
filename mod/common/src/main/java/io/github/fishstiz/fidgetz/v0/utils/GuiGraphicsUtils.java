package io.github.fishstiz.fidgetz.v0.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.ServiceLoader;

public final class GuiGraphicsUtils {
    private static final Service GUI_GRAPHICS_SERVICE = ServiceLoader.load(Service.class).findFirst().orElseThrow();

    public static void sprite(GuiGraphics graphics, Identifier sprite, int x, int y, int width, int height, int color) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
    }

    public static void sprite(GuiGraphics graphics, Identifier sprite, int x, int y, int width, int height) {
        sprite(graphics, sprite, x, y, width, height, CommonColors.WHITE);
    }

    public static void texture(
            GuiGraphics graphics,
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
            GuiGraphics graphics,
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
            GuiGraphics graphics,
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

    public static void texture(GuiGraphics graphics, Identifier texture, int x, int y, int textureWidth, int textureHeight) {
        texture(graphics, texture, x, y, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    public static void fillFloat(GuiGraphics graphics, float left, float top, float right, float bottom, int color) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissorArea = GUI_GRAPHICS_SERVICE.peekScissorStack(graphics);
        ScreenRectangle bounds = new ScreenRectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top)).transformMaxBounds(pose);
        ScreenRectangle intersection = scissorArea == null ? bounds : scissorArea.intersection(bounds);

        GUI_GRAPHICS_SERVICE.addGuiElement(graphics, new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, left, top).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, left, bottom).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, right, bottom).setColor(color);
                vertexConsumer.addVertexWith2DPose(pose, right, top).setColor(color);
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public @Nullable ScreenRectangle scissorArea() {
                return scissorArea;
            }

            @Override
            public @Nullable ScreenRectangle bounds() {
                return intersection;
            }
        });
    }

    public static void fillHorizontal(GuiGraphics graphics, int left, int top, int right, int bottom, int colorFrom, int colorTo) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissorArea = GUI_GRAPHICS_SERVICE.peekScissorStack(graphics);
        ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top).transformMaxBounds(pose);
        ScreenRectangle intersection = scissorArea == null ? bounds : scissorArea.intersection(bounds);

        GUI_GRAPHICS_SERVICE.addGuiElement(graphics, new GuiElementRenderState() {
            @Override
            public void buildVertices(VertexConsumer vertexConsumer) {
                vertexConsumer.addVertexWith2DPose(pose, left, top).setColor(colorFrom);
                vertexConsumer.addVertexWith2DPose(pose, left, bottom).setColor(colorFrom);
                vertexConsumer.addVertexWith2DPose(pose, right, bottom).setColor(colorTo);
                vertexConsumer.addVertexWith2DPose(pose, right, top).setColor(colorTo);
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipelines.GUI;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public @Nullable ScreenRectangle scissorArea() {
                return scissorArea;
            }

            @Override
            public @Nullable ScreenRectangle bounds() {
                return intersection;
            }
        });
    }

    public static void text(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color);
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

    private GuiGraphicsUtils() {
    }

    interface Service {
        void addGuiElement(GuiGraphics graphics, GuiElementRenderState blitState);

        @Nullable
        ScreenRectangle peekScissorStack(GuiGraphics graphics);
    }
}
