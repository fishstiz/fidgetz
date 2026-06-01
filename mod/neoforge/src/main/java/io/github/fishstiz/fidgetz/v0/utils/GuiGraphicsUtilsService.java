package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@ApiStatus.Internal
public class GuiGraphicsUtilsService implements GuiGraphicsUtils.Service {
    @Override
    public void addGuiElement(@NonNull GuiGraphics graphics, @NonNull GuiElementRenderState blitState) {
        graphics.submitGuiElementRenderState(blitState);
    }

    @Override
    public @Nullable ScreenRectangle peekScissorStack(@NonNull GuiGraphics graphics) {
        return graphics.peekScissorStack();
    }
}
