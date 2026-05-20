package io.github.fishstiz.fidgetz.v0.inject.interfaces;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;
import java.util.function.UnaryOperator;

public interface WidgetOperator {
    default void fidgetz$modifyWidgets(UnaryOperator<List<GuiEventListener>> modifier) {
    }

    default void fidgetz$modifyNarratables(UnaryOperator<List<NarratableEntry>> modifier) {
    }

    default void fidgetz$modifyRenderables(UnaryOperator<List<Renderable>> modifier) {
    }
}
