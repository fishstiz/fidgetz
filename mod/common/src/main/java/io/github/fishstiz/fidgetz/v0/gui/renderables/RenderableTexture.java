package io.github.fishstiz.fidgetz.v0.gui.renderables;

import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

record RenderableTexture(
        Identifier texture,
        int textureWidth,
        int textureHeight,
        float u,
        float v,
        int uWidth,
        int vHeight
) implements RenderableRectangle {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
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
}
