package io.github.fishstiz.fidgetz.v0.gui.components;

import org.jspecify.annotations.Nullable;

public interface FZComponent {
    default @Nullable String fidgetz$componentId() {
        return null;
    }
}
