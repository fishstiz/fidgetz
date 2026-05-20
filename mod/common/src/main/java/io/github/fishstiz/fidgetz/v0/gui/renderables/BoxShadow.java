package io.github.fishstiz.fidgetz.v0.gui.renderables;

import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class BoxShadow implements RenderableRectangle {
    private static final Identifier SHADOW_SPRITE = Fidgetz.id("box_shadow");
    private static final float SHADOW_BORDER = 48f;
    private final int offsetX;
    private final int offsetY;

    BoxShadow(float size, float xOffset, float yOffset) {
        float scale = size / SHADOW_BORDER;
        int baseOffset = Math.round(SHADOW_BORDER * scale);
        this.offsetX = Math.round(baseOffset + xOffset);
        this.offsetY = Math.round(baseOffset + yOffset);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsUtils.sprite(graphics, SHADOW_SPRITE, left - offsetX, top - offsetY, width + offsetX * 2, height + offsetY * 2);
    }
}
