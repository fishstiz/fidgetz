package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.CommonColors;

public class GradientScreen extends FZScreen {
    public GradientScreen() {
        super(CommonComponents.EMPTY);
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);

        int width = 50;
        int height = 100;
        int x = 0;
        int y = 0;
        int spacing = 8;

        int colorFrom = CommonColors.GREEN;
        int colorTo = CommonColors.RED;

        graphics.fillGradient(x, y, x + width, height, colorFrom, colorTo);
        x += width + spacing;

        graphics.fillGradient(x, y, x + width, height, colorTo, colorFrom);
        x += width + spacing;

        GuiGraphicsUtils.fillHorizontal(graphics, x, y, x + width, height, colorFrom, colorTo);
        x += width + spacing;

        GuiGraphicsUtils.fillHorizontal(graphics, x, y, x + width, height, colorTo, colorFrom);
    }
}
