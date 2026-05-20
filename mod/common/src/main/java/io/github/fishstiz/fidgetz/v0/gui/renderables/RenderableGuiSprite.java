package io.github.fishstiz.fidgetz.v0.gui.renderables;

import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

record RenderableGuiSprite(Identifier sprite) implements RenderableRectangle {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsUtils.sprite(graphics, sprite, left, top, width, height);
    }
}
