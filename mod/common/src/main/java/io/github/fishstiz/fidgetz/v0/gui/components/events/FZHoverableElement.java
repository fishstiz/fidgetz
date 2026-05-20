package io.github.fishstiz.fidgetz.v0.gui.components.events;

public interface FZHoverableElement {
    private static UnsupportedOperationException notImplemented(String method) {
        return new UnsupportedOperationException("""
                FZHoverableElement is only implemented for GuiEventListeners, \
                override %s if implementing on a custom class.
                """.formatted(method)
        );
    }

    default boolean fidgetz$isHovered() {
        throw notImplemented("fidgetz$isHovered");
    }

    default void fidgetz$setHovered(boolean hovered) {
        throw notImplemented("fidgetz$setHovered");
    }

    default boolean fidgetz$updateHovered(double mouseX, double mouseY) {
        throw notImplemented("fidgetz$updateHovered");
    }
}
