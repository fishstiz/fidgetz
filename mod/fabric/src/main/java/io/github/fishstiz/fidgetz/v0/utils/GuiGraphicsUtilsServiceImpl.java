package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class GuiGraphicsUtilsServiceImpl implements GuiGraphicsUtils.Service {
    @Override
    public void addGuiElement(@NonNull GuiGraphicsExtractor graphics, @NonNull GuiElementRenderState blitState) {
        graphics.guiRenderState.addGuiElement(blitState);
    }

    @Override
    public @Nullable ScreenRectangle peekScissorStack(@NonNull GuiGraphicsExtractor graphics) {
        return graphics.scissorStack.peek();
    }
}
