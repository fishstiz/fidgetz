package io.github.fishstiz.fidgetz.v0.gui.renderables;

import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

record RenderableGuiSprite(Identifier sprite, int color) implements RenderableRectangle {
    @Override
    public void extractRenderState(GuiGraphics graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsUtils.sprite(graphics, sprite, left, top, width, height, color);
    }
}
