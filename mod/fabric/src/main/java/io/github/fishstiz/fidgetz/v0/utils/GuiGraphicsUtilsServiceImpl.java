package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class GuiGraphicsUtilsServiceImpl implements GuiGraphicsUtils.Service {
    @Override
    public void addGuiElement(@NonNull GuiGraphics graphics, @NonNull GuiElementRenderState blitState) {
        graphics.guiRenderState.submitGuiElement(blitState);
    }

    @Override
    public @Nullable ScreenRectangle peekScissorStack(@NonNull GuiGraphics graphics) {
        return graphics.scissorStack.peek();
    }
}
