package io.github.fishstiz.fidgetz.v0.gui.renderables;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class BoxShadow implements RenderableRectangle {
    private static final ResourceLocation SHADOW_SPRITE = Fidgetz.id("box_shadow");
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
    public void extractRenderState(GuiGraphics graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        GuiGraphicsUtils.sprite(graphics, SHADOW_SPRITE, left - offsetX, top - offsetY, width + offsetX * 2, height + offsetY * 2);
        RenderSystem.disableBlend();
    }
}
