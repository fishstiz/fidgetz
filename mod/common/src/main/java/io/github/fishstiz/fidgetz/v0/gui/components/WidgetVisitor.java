package io.github.fishstiz.fidgetz.v0.gui.components;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.function.Consumer;

@FunctionalInterface
public interface WidgetVisitor {
    <T extends GuiEventListener & NarratableEntry> void fidgetz$visitWidget(T widget);
}
