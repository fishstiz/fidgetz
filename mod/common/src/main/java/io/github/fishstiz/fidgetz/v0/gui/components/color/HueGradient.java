package io.github.fishstiz.fidgetz.v0.gui.components.color;

import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.SimpleGuiRenderState;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import io.github.fishstiz.fidgetz.v0.gui.color.HSVA;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;

public record HueGradient() implements RenderableRectangle {
    private static final int GRADIENTS = 6;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, float partialTick) {
        extractRenderState(graphics, left, top, width, height);
    }

    public static void extractRenderState(GuiGraphicsExtractor graphics, int left, int top, int width, int height) {
        ScreenRectangle bounds = new ScreenRectangle(left, top, width, height);

        GuiGraphicsUtils.addGuiElement(graphics, SimpleGuiRenderState.from(graphics, bounds, (state, vertexConsumer) -> {
            ScreenRectangle area = state.bounds();
            if (area == null) return;

            Matrix3x2f pose = state.pose();
            float gradientWidth = (float) area.width() / GRADIENTS;

            for (int i = 0; i < GRADIENTS; i++) {
                int colorLeft = HSVA.toARGB(i * 60f, 1.0f, 1.0f, 1.0f);
                int colorRight = HSVA.toARGB((i + 1) * 60f, 1.0f, 1.0f, 1.0f);

                float x0 = area.left() + i * gradientWidth;
                float x1 = area.left() + (i + 1) * gradientWidth;

                vertexConsumer.addVertexWith2DPose(pose, x0, area.top()).setColor(colorLeft);
                vertexConsumer.addVertexWith2DPose(pose, x0, area.bottom()).setColor(colorLeft);
                vertexConsumer.addVertexWith2DPose(pose, x1, area.bottom()).setColor(colorRight);
                vertexConsumer.addVertexWith2DPose(pose, x1, area.top()).setColor(colorRight);
            }
        }));
    }
}
