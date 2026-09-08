package io.github.fishstiz.fidgetz.v0.gui.renderables;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public record SimpleGuiRenderState(
        BiConsumer<SimpleGuiRenderState, VertexConsumer> verticesBuilder,
        Matrix3x2f pose,
        @Nullable ScreenRectangle bounds,
        @Nullable ScreenRectangle scissorArea
) implements GuiElementRenderState {
    public static SimpleGuiRenderState from(
            GuiGraphicsExtractor graphics,
            ScreenRectangle bounds,
            BiConsumer<SimpleGuiRenderState, VertexConsumer> verticesBuilder
    ) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissorArea = GuiGraphicsUtils.peekScissorStack(graphics);
        bounds = bounds.transformMaxBounds(pose);
        ScreenRectangle intersection = scissorArea == null ? bounds : scissorArea.intersection(bounds);
        return new SimpleGuiRenderState(verticesBuilder, pose, intersection, scissorArea);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        verticesBuilder.accept(this, vertexConsumer);
    }

    public void addQuadWith2DPose(
            VertexConsumer vertexConsumer,
            ScreenRectangle bounds,
            int color1,
            int color2,
            int color3,
            int color4
    ) {
        vertexConsumer.addVertexWith2DPose(pose, bounds.left(), bounds.top()).setColor(color1);
        vertexConsumer.addVertexWith2DPose(pose, bounds.left(), bounds.bottom()).setColor(color2);
        vertexConsumer.addVertexWith2DPose(pose, bounds.right(), bounds.bottom()).setColor(color3);
        vertexConsumer.addVertexWith2DPose(pose, bounds.right(), bounds.top()).setColor(color4);
    }
}
