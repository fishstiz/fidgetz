package io.github.fishstiz.fidgetz.v0.gui.renderables;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

record TextRectangle(Supplier<Component> text) implements RenderableRectangle {
    @Override
    public void extractRenderState(GuiGraphics graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        graphics.textRenderer().acceptScrolling(text.get(), left + (width / 2), left, left + width, top, top + height);
    }
}
