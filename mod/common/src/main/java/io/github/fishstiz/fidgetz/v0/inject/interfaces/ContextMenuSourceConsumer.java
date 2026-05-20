package io.github.fishstiz.fidgetz.v0.inject.interfaces;

import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenuEntry;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface ContextMenuSourceConsumer {
    default void fidgetz$setContextMenuSource(@Nullable Consumer<FZContextMenuEntry.Collector> contextMenuSource) {
    }
}
