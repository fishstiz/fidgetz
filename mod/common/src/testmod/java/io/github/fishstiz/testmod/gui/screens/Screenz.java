package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class Screenz extends FZScreen {
    private final FZMutableRef<@Nullable String> state = new FZMutableRef<>(null);

    public Screenz() {
        super(CommonComponents.EMPTY);
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        collector.renderableWidget(FZButton.bind("deactivate", state.map(s -> FZButton.builder()
                .active(s != null)
                .message(Component.literal("Deactivate"))
                .onPress(() -> state.set((String) null))
                .toProps())));
        collector.renderableWidget(FZButton.bind("activate", state.map(s -> FZButton.builder()
                .active(s == null)
                .message(Component.literal("Activate"))
                .onPress(() -> state.set(""))
                .toProps()))).setPosition(0, 40);
    }
}
