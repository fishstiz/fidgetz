package io.github.fishstiz.fidgetz.v0.gui.renderables;

import net.minecraft.client.gui.GuiGraphicsExtractor;

record Outline(int color) implements RenderableRectangle {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        graphics.outline(left, top, width, height, color);
    }
}
