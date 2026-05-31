package io.github.fishstiz.fidgetz.v0.inject.interfaces;

import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenu;
import io.github.fishstiz.fidgetz.v0.gui.components.FZPopoverMenuItem;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface ContextMenuSourceConsumer {
    default void fidgetz$setContextMenuSource(@Nullable Consumer<FZContextMenu.Collector> contextMenuSource) {
    }
}
