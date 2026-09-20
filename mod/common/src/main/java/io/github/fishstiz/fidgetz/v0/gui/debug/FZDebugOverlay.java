package io.github.fishstiz.fidgetz.v0.gui.debug;

import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class FZDebugOverlay {
    private static final float SCALE = 0.8f;
    private static final String FOCUS_LABEL = "Focus Path: ";
    private static final String HOVERED_LABEL = "Hovered Path: ";

    public static boolean hovered = System.getProperty("fidgetz.debug.hovered") != null;
    public static boolean focusPath = System.getProperty("fidgetz.debug.focusPath") != null;

    private static void pushScale(GuiGraphicsExtractor graphics) {
        graphics.nextStratum();
        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE);
    }

    public static void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;

        if (screen != null) {
            int y = 0;

            Font font = minecraft.font;
            boolean scaled = false;

            if (focusPath) {
                pushScale(graphics);
                scaled = true;
                y += extractFocusRenderState(graphics, font, screen, y) + font.lineHeight;
            }
            if (hovered) {
                if (!scaled) pushScale(graphics);
                scaled = true;

                extractHoveredRenderState(graphics, font, screen, y, mouseX, mouseY);
            }

            if (scaled) {
                graphics.pose().popMatrix();
            }
        }
    }

    private static int extractFocusRenderState(GuiGraphicsExtractor graphics, Font font, Screen screen, int y) {
        graphics.fill(0, y, font.width(FOCUS_LABEL), y + font.lineHeight, 0x5FFF0000);
        graphics.text(font, FOCUS_LABEL, 0, y, 0xFFFFFFFF);
        return extractFocusRenderState(graphics, font, screen.getFocused(), y + font.lineHeight);
    }

    private static int extractFocusRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            @Nullable GuiEventListener focused,
            int y
    ) {
        if (focused == null) return y;

        String name = focused.getClass().getName();
        graphics.fill(0, y, font.width(name), y + font.lineHeight, 0x5FFF0000);
        graphics.text(font, name, 0, y, 0xFFFFFFFF);

        if (focused instanceof ContainerEventHandler container) {
            return extractFocusRenderState(graphics, font, container.getFocused(), y + font.lineHeight);
        }

        return y + font.lineHeight;
    }

    private static void extractHoveredRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            Screen screen,
            int y,
            int mouseX,
            int mouseY
    ) {
        FZHoverableContainer hoverable = ((FZHoverableContainer) screen);
        hoverable.fidgetz$updateHovered(mouseX, mouseY);

        graphics.fill(0, y, font.width(HOVERED_LABEL), y + font.lineHeight, 0x5F0000FF);
        graphics.text(font, HOVERED_LABEL, 0, y, 0xFFFFFFFF);
        extractHoveredRenderState(graphics, font, hoverable.fidgetz$getHovered(), y + font.lineHeight, mouseX, mouseY);
    }

    private static int extractHoveredRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            @Nullable GuiEventListener hovered,
            int y,
            int mouseX,
            int mouseY
    ) {
        if (hovered == null) {
            extractMousePosRenderState(
                    graphics,
                    font,
                    (int) (mouseX / SCALE) + 16,
                    clampY(graphics, font, (int) (mouseY / SCALE), 1),
                    mouseX,
                    mouseY
            );
            return y;
        }

        String name = hovered.getClass().getName();
        graphics.fill(0, y, font.width(name), y + font.lineHeight, 0x5F0000FF);
        graphics.text(font, name, 0, y, 0xFFFFFFFF);

        if (hovered instanceof FZHoverableContainer container) {
            GuiEventListener child = container.fidgetz$getHovered();
            if (child != null) {
                return extractHoveredRenderState(
                        graphics,
                        font,
                        container.fidgetz$getHovered(),
                        y + font.lineHeight,
                        mouseX,
                        mouseY
                );
            }
            extractHoveredBoundsRenderState(graphics, font, hovered, mouseX, mouseY);
        } else {
            extractHoveredBoundsRenderState(graphics, font, hovered, mouseX, mouseY);
        }

        return y + font.lineHeight;
    }

    private static void extractHoveredBoundsRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            GuiEventListener hovered,
            int mouseX,
            int mouseY
    ) {
        ScreenRectangle bounds = getBounds(hovered);
        String label = hovered.getClass().getName();

        int left = (int) (bounds.left() / SCALE);
        int top = (int) (bounds.top() / SCALE);
        int width = (int) (bounds.width() / SCALE);
        int height = (int) (bounds.height() / SCALE);

        int labelX = clampX(graphics, font, (int) (mouseX / SCALE) + 16, label);
        int labelY = clampY(graphics, font, (int) (mouseY / SCALE), 3);

        graphics.fill(labelX, labelY, labelX + font.width(label), labelY + font.lineHeight, 0xAF0000FF);
        graphics.outline(left, top, width, height, 0xFF0000FF);

        graphics.fill(left, top, left + width, top + height, 0x1F0000FF);
        graphics.text(font, label, labelX, labelY, 0xFFFFFFFF);

        String boundsString = "x=%s,y=%s,w=%s,h=%s".formatted(bounds.left(), bounds.top(), bounds.width(), bounds.height());
        int boundsY = labelY + font.lineHeight;

        graphics.fill(labelX, boundsY, labelX + font.width(boundsString), boundsY + font.lineHeight, 0xAF0000FF);
        graphics.text(font, boundsString, labelX, boundsY, 0xFFFFFFFF);

        extractMousePosRenderState(graphics, font, labelX, boundsY + font.lineHeight, mouseX, mouseY);
    }

    private static void extractMousePosRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        String mousePos = "mx=%s,my=%s".formatted(mouseX, mouseY);
        x = clampX(graphics, font, x, mousePos);

        graphics.fill(x, y, x + font.width(mousePos), y + font.lineHeight, 0xAF0000FF);
        graphics.text(font, mousePos, x, y, 0xFFFFFFFF);
    }

    private static int clampX(GuiGraphicsExtractor graphics, Font font, int x, String label) {
        int textWidth = font.width(label);
        int screenWidth = (int) (graphics.guiWidth() / SCALE);
        if (x + textWidth > screenWidth) {
            x = screenWidth - textWidth - 2;
        }
        return Math.max(0, x);
    }

    private static int clampY(GuiGraphicsExtractor graphics, Font font, int y, int lineCount) {
        int totalHeight = lineCount * font.lineHeight;
        int screenHeight = (int) (graphics.guiHeight() / SCALE);
        if (y + totalHeight > screenHeight) {
            y = screenHeight - totalHeight - 2;
        }
        return Math.max(0, y);
    }

    private static ScreenRectangle getBounds(@Nullable GuiEventListener element) {
        if (element instanceof LayoutElement layoutElement) return layoutElement.getRectangle();
        return element != null ? element.getRectangle() : ScreenRectangle.empty();
    }

    private FZDebugOverlay() {
    }
}
